package com.mybank.pc.collection.trade;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.kit.Kv;
import com.mybank.pc.collection.model.UnionpayBatchCollection;
import com.mybank.pc.collection.model.UnionpayBatchCollectionBatchno;
import com.mybank.pc.collection.model.UnionpayCollection;
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
						nextBatchNo);
				unionpayBatchCollection.claimToBeSentOrder(toBeSentOrder);
				unionpayBatchCollection.assemblyBatchRequest();

				isSaved = unionpayBatchCollection.save();
				sendResult = unionpayBatchCollection.sendBatchOrder();
				handlingBatchTradeResult(unionpayBatchCollection, toBeSentOrder);
			} else {
				UnionpayBatchCollectionBatchno.decrementBatchNo(txnTime);
			}
		} catch (Exception e) {
			e.printStackTrace();

			try {
				if ((!isSaved) && StringUtils.isNotBlank(txnTime) && StringUtils.isNotBlank(nextBatchNo)
						&& nextBatchNo.length() == 4) {
					UnionpayBatchCollectionBatchno.decrementBatchNo(txnTime);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			try {
				if (count != 0 && CollectionUtils.isEmpty(toBeSentOrder)) {
					toBeSentOrder = UnionpayCollection.findToBeSentUnionpayCollectionByBatchNo(kv);
				}
				if (CollectionUtils.isNotEmpty(toBeSentOrder)) {
					for (UnionpayCollection unionpayCollection : toBeSentOrder) {
						unionpayCollection.resetBatchStatus();
						unionpayCollection.setMat(now);
						unionpayCollection.update();
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
	 * 应答码规范参考open.unionpay.com帮助中心 下载 产品接口规范 《平台接入接口规范-第5部分-附录》
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

			unionpayBatchCollection.setRespCode(respCode);
			unionpayBatchCollection.setRespMsg(respMsg);

			Date now = new Date();

			// 00：交易已受理
			// 其他：03 04 05
			// 都需发起交易批量状态查询交易（Form10_6_6_BatchQuery）确定交易状态
			if (("00").equals(respCode) || ("03").equals(respCode) || ("04").equals(respCode)
					|| ("05").equals(respCode)) {
				for (UnionpayCollection unionpayCollection : toBeSentOrder) {
					unionpayCollection.setRespCode(respCode);
					unionpayCollection.setRespMsg(respMsg);
					unionpayCollection.setStatus("2");// 已发送
					unionpayCollection.setMat(now);
					unionpayCollection.update();
				}
				isSuccess = true;
				unionpayBatchCollection.setFinalCode("1");// 处理中
			} else {
				// 其他应答码为失败请排查原因
				unionpayBatchCollection.setFinalCode("2");// 失败
				isSuccess = false;
			}

			unionpayBatchCollection.setMat(now);
			unionpayBatchCollection.update();
		} catch (Exception e) {
			isSuccess = false;
			throw e;
		}
		return isSuccess;
	}

}
