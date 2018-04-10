package com.mybank.pc.collection.trade;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.mybank.pc.collection.model.UnionpayBatchCollection;
import com.mybank.pc.collection.model.UnionpayBatchCollectionBatchno;
import com.mybank.pc.collection.model.UnionpayCollection;
import com.mybank.pc.exception.TxnKey;
import com.mybank.pc.kits.unionpay.acp.SDK;

public class CBatchTradeSrv {

	public void sendOrder() {
		Date now = new Date();
		String txnTime = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
		String nextBatchNo = UnionpayBatchCollectionBatchno.getNextBatchNoToString(txnTime);

		UnionpayBatchCollection unionpayBatchCollection = buildUnionpayBatchCollection(now, txnTime, nextBatchNo);
		unionpayBatchCollection.save();

		Kv kv = Kv.create().set("batchId", unionpayBatchCollection.getId()).set("merId",
				SDK.getSDK(SDK.MER_CODE_BATCH).getMerId());
		SqlPara sqlPara = Db.getSqlPara("collection_trade.updateToBeSentUnionpayCollectionBatchId", kv);
		int count = Db.update(sqlPara);

		if (count > 0) {
			sqlPara = Db.getSqlPara("collection_trade.findToBeSentUnionpayCollectionByBatchId", kv.set("mat", now));
			List<UnionpayCollection> toBeSentOrder = UnionpayCollection.dao.find(sqlPara);

			unionpayBatchCollection.claimToBeSentOrder(toBeSentOrder);
			unionpayBatchCollection.assemblyBatchRequest();

			saveBatchOrder(unionpayBatchCollection);
			sendBatchOrder(unionpayBatchCollection, toBeSentOrder);
			handlingBatchTradeResult(unionpayBatchCollection, toBeSentOrder);
		} else {

		}

	}

	public UnionpayBatchCollection buildUnionpayBatchCollection(Date now, String txnTime, String batchNo) {
		// 银联调用相关参数
		String txnType = "21"; // 交易类型
		String txnSubType = "02"; // 交易子类型
		String channelType = "07";// 渠道类型
		String accessType = "0";// 接入类型，商户接入固定填0，不需修改
		String bizType = "000501";// 业务类型

		// 平台调用相关参数
		SDK sdk = SDK.getSDK(SDK.MER_CODE_BATCH);
		String finalCode = "3";// 最终处理结果：0成功 1处理中 2失败 3待初始化

		UnionpayBatchCollection unionpayBatchCollection = new UnionpayBatchCollection();
		unionpayBatchCollection.setTxnType(txnType);
		unionpayBatchCollection.setTxnSubType(txnSubType);
		unionpayBatchCollection.setBizType(bizType);
		unionpayBatchCollection.setChannelType(channelType);
		unionpayBatchCollection.setAccessType(accessType);
		unionpayBatchCollection.setMerId(sdk.getMerId());
		unionpayBatchCollection.setBatchNo(batchNo);
		unionpayBatchCollection.setTxnTime(txnTime);
		unionpayBatchCollection.setQueryResultCount(0);
		unionpayBatchCollection.setFinalCode(finalCode);
		unionpayBatchCollection.setCat(now);
		unionpayBatchCollection.setMat(now);

		return unionpayBatchCollection;
	}

	@Before({ Tx.class })
	@TxnKey("saveBatchOrder")
	public void saveBatchOrder(UnionpayBatchCollection unionpayBatchCollection) {
		try {
			unionpayBatchCollection.save();
		} catch (Exception e) {
			unionpayBatchCollection.setFinalCode("2");
			throw e;
		}
	}

	@Before({ Tx.class })
	@TxnKey("batch-sendOrder")
	public void sendBatchOrder(UnionpayBatchCollection unionpayBatchCollection,
			List<UnionpayCollection> toBeSentOrder) {
		try {
			unionpayBatchCollection.sendBatchOrder();
		} catch (Exception e) {
			e.printStackTrace();
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
					unionpayCollection.setStatus("1");// 已发送
					unionpayCollection.setMat(now);
					unionpayCollection.update();
				}
				isSuccess = true;
			} else {
				// 其他应答码为失败请排查原因
				unionpayBatchCollection.setFinalCode("2");
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
