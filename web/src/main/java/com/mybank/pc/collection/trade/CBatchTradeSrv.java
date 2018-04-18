package com.mybank.pc.collection.trade;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.SqlPara;
import com.mybank.pc.collection.model.CollectionTrade;
import com.mybank.pc.collection.model.UnionpayBatchCollection;
import com.mybank.pc.collection.model.UnionpayBatchCollectionBatchno;
import com.mybank.pc.collection.model.UnionpayCollection;
import com.mybank.pc.exception.ValidateUnionpayRespException;
import com.mybank.pc.kits.unionpay.acp.SDK;

public class CBatchTradeSrv {

	public void sendBatchOrder() {
		String txnTime = null;
		String nextBatchNo = null;
		UnionpayBatchCollection unionpayBatchCollection = null;

		int count = 0;
		boolean isSaved = false;
		List<UnionpayCollection> toBeSentOrder = null;
		Map<String, String> sendResult = null;
		Kv kv = Kv.create();
		Date now = new Date();
		try {
			txnTime = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
			nextBatchNo = UnionpayBatchCollectionBatchno.getNextBatchNoToString(txnTime);

			kv.set("batchNo", nextBatchNo).set("txnTime", txnTime)
					.set("merId", SDK.getSDK(SDK.MER_CODE_BATCH_CH).getMerId()).set("mat", now);
			count = UnionpayCollection.updateToBeSentUnionpayCollectionBatchNo(kv);
			if (count > 0) {
				toBeSentOrder = UnionpayCollection.findToBeSentUnionpayCollectionByBatchNo(kv);
				unionpayBatchCollection = UnionpayBatchCollection.buildUnionpayBatchCollection(now, txnTime,
						nextBatchNo, toBeSentOrder);
				unionpayBatchCollection.assemblyBatchRequest();

				if (isSaved = unionpayBatchCollection.save()) {
					sendResult = unionpayBatchCollection.sendBatchOrder();
					handlingBatchTradeResult(unionpayBatchCollection, toBeSentOrder);
				}
			} else {
				UnionpayBatchCollectionBatchno.decrementBatchNo(txnTime, nextBatchNo);
			}
		} catch (ValidateUnionpayRespException vure) {
			if (unionpayBatchCollection != null && isSaved) {
				unionpayBatchCollection.setExceInfo(JsonKit.toJson(vure.getExceptionInfo()));
				unionpayBatchCollection.update();
			}
		} catch (Exception e) {
			e.printStackTrace();

			try {
				if ((!isSaved) && StringUtils.isNotBlank(txnTime) && StringUtils.isNotBlank(nextBatchNo)
						&& nextBatchNo.length() == 4) {
					UnionpayBatchCollectionBatchno.decrementBatchNo(txnTime, nextBatchNo);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			try {
				if (count != 0 && CollectionUtils.isEmpty(toBeSentOrder)) {
					toBeSentOrder = UnionpayCollection.findToBeSentUnionpayCollectionByBatchNo(kv);
				}
				if (CollectionUtils.isNotEmpty(toBeSentOrder) && sendResult != null) {
					String respCode = sendResult.get("respCode");
					if (!isSuccessed(respCode) && !isFailed(respCode)) {
						for (UnionpayCollection unionpayCollection : toBeSentOrder) {
							if ("2".equals(unionpayCollection.getFinalCode())// 已失败或为待发送状态则不需要更新订单状态
									|| "0".equals(unionpayCollection.getStatus())) {
								continue;
							}
							try {
								unionpayCollection.resetBatchStatus();
								unionpayCollection.setMat(now);
								unionpayCollection.update();
							} catch (Exception uce) {
								uce.printStackTrace();
							}
						}
					}
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}

			try {
				if (isSaved) {
					if (sendResult == null || sendResult.isEmpty()) {
						unionpayBatchCollection.setMat(now);
						unionpayBatchCollection.setFinalCode("2");
					}
					unionpayBatchCollection.update();
				}
			} catch (Exception e3) {
				e3.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param rspData
	 * @param acpService
	 * @param encoding
	 * @param unionpayBatchCollection
	 * @return
	 */
	private boolean handlingBatchTradeResult(UnionpayBatchCollection unionpayBatchCollection,
			List<UnionpayCollection> toBeSentOrder) {
		boolean isSuccess = false;
		try {
			unionpayBatchCollection.validateBatchOrderResp();
			Map<String, String> rspData = unionpayBatchCollection.getBatchRspData();
			String respCode = rspData.get("respCode");
			String respMsg = rspData.get("respMsg");

			Date now = new Date();

			unionpayBatchCollection.setRespCode(respCode);
			unionpayBatchCollection.setRespMsg(respMsg);
			unionpayBatchCollection.setMat(now);

			if (CollectionUtils.isNotEmpty(toBeSentOrder)) {
				for (UnionpayCollection unionpayCollection : toBeSentOrder) {
					unionpayCollection.setRespCode(respCode);
					unionpayCollection.setRespMsg(respMsg);
					unionpayCollection.setMat(now);
				}
			}

			// 00：交易已受理
			// 其他：03：交易通讯超时，请发起查询交易 04：交易状态未明，请查询对账结果 05：交易已受理，请稍后查询交易结果
			// 都需发起交易批量状态查询交易确定交易状态
			if (isSuccessed(respCode)) {
				if (CollectionUtils.isNotEmpty(toBeSentOrder)) {
					for (UnionpayCollection unionpayCollection : toBeSentOrder) {
						unionpayCollection.setStatus("2");// 已发送
						unionpayCollection.update();
					}
				}
				isSuccess = true;
				unionpayBatchCollection.setFinalCode("1");// 处理中
				unionpayBatchCollection.setNextAllowQueryDate();
			} else {
				unionpayBatchCollection.setFinalCode("2");// 失败
				isSuccess = false;

				// 10-29 有关商户端上送报文格式检查导致的错误，不认为与批量请求相关的交易订单的最终处理结果为失败，需后续处理
				// 其他的应答码则认为与批量请求相关的交易订单的最终处理结果为失败
				boolean isFailed = isFailed(respCode);
				if (CollectionUtils.isNotEmpty(toBeSentOrder)) {
					for (UnionpayCollection unionpayCollection : toBeSentOrder) {
						try {
							unionpayCollection.setRespCode(respCode);
							unionpayCollection.setRespMsg(respMsg);
							unionpayCollection.setMat(now);

							updateOrderStatus(unionpayCollection, isFailed);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			unionpayBatchCollection.update();

			return isSuccess;
		} catch (Exception e) {
			isSuccess = false;
			throw e;
		}
	}

	public static boolean isSuccessed(String respCode) {
		// 00：交易已受理
		// 其他：03：交易通讯超时，请发起查询交易 04：交易状态未明，请查询对账结果 05：交易已受理，请稍后查询交易结果
		return ("00".equals(respCode) || "03".equals(respCode) || "04".equals(respCode) || "05".equals(respCode));
	}

	/**
	 * 10-29 有关商户端上送报文格式检查导致的错误，不认为与批量请求相关的交易订单的最终处理结果为失败，需后续处理<br>
	 * 其他的应答码则认为与批量请求相关的交易订单的最终处理结果为失败
	 * 
	 * @param respCode
	 * @return
	 */
	public static boolean isFailed(String respCode) {
		// 10：报文格式错误 11：验证签名失败 12：重复交易 13：报文交易要素缺失 14：批量文件格式错误
		return !("10".equals(respCode) || "11".equals(respCode) || "12".equals(respCode) || "13".equals(respCode)
				|| "14".equals(respCode));
	}

	public void updateOrderStatus(UnionpayCollection unionpayCollection, boolean isFailed) {
		if (isFailed) {
			unionpayCollection.setFinalCode("2");// 失败
			unionpayCollection.update();

			SqlPara sqlPara = Db.getSqlPara("collection_trade.findByTradeNo", unionpayCollection);
			CollectionTrade collectionTrade = CollectionTrade.dao.findFirst(sqlPara);
			if (collectionTrade != null) {
				collectionTrade.setResCode(unionpayCollection.getRespCode());
				collectionTrade.setResMsg(unionpayCollection.getRespMsg());
				collectionTrade.setFinalCode("2");// 失败

				collectionTrade.setMat(unionpayCollection.getMat());
				collectionTrade.update();
			}
		} else {
			unionpayCollection.resetBatchStatus();
			unionpayCollection.update();
		}
	}

}
