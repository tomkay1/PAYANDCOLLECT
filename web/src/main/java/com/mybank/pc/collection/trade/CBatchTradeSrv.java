package com.mybank.pc.collection.trade;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.mybank.pc.collection.model.UnionpayBatchCollection;
import com.mybank.pc.collection.model.UnionpayCollection;
import com.mybank.pc.exception.TxnKey;
import com.mybank.pc.kits.unionpay.acp.SDK;
import com.mybank.pc.kits.unionpay.acp.file.collection.model.BatchCollectionRequest;
import com.mybank.pc.kits.unionpay.acp.file.collection.model.RequestContent;
import com.mybank.pc.kits.unionpay.acp.file.collection.model.RequestHead;

public class CBatchTradeSrv {

	public void sendOrder() {
		SqlPara sqlPara = Db.getSqlPara("collection_trade.findToBeSentUnionpayCollection");
		List<UnionpayCollection> toBeSentOrder = UnionpayCollection.dao.find(sqlPara);
		if (CollectionUtils.isNotEmpty(toBeSentOrder)) {
			UnionpayBatchCollection unionpayBatchCollection = buildUnionpayBatchCollection(toBeSentOrder);
			unionpayBatchCollection.assemblyBatchRequest();
			saveBatchOrder(unionpayBatchCollection);
			sendBatchOrder(unionpayBatchCollection, toBeSentOrder);
		} else {

		}
	}

	public UnionpayBatchCollection buildUnionpayBatchCollection(List<UnionpayCollection> toBeSentOrder) {
		// 银联调用相关参数
		String txnType = "21"; // 交易类型
		String txnSubType = "02"; // 交易子类型
		String channelType = "07";// 渠道类型
		String accessType = "0";// 接入类型，商户接入固定填0，不需修改
		String bizType = "000501";// 业务类型

		// 平台调用相关参数
		SDK sdk = SDK.getSDK(SDK.MER_CODE_BATCH);
		String finalCode = "1";// 最终处理结果：0成功 1处理中 2失败

		UnionpayBatchCollection unionpayBatchCollection = new UnionpayBatchCollection();
		Date now = new Date();
		String txnTime = new SimpleDateFormat("yyyyMMddHHmmss").format(now);

		SqlPara sqlPara = Db.getSqlPara("collection_trade.findMaxBatchNo", Kv.create().set("txnTime", txnTime));
		Record maxBatchNoRecord = Db.findFirst(sqlPara);

		Integer maxBatchNo = maxBatchNoRecord.getInt("maxBatchNo");
		maxBatchNo = maxBatchNo == null ? 1 : maxBatchNo + 1;
		String batchNo = String.format("%04d", maxBatchNo);

		BatchCollectionRequest batchCollectionRequest = buildBatchCollectionRequest(toBeSentOrder);
		RequestHead requestHead = batchCollectionRequest.getHead();

		unionpayBatchCollection.setTxnType(txnType);
		unionpayBatchCollection.setTxnSubType(txnSubType);
		unionpayBatchCollection.setBizType(bizType);
		unionpayBatchCollection.setChannelType(channelType);
		unionpayBatchCollection.setAccessType(accessType);
		unionpayBatchCollection.setMerId(sdk.getMerId());
		unionpayBatchCollection.setBatchNo(batchNo);
		unionpayBatchCollection.setTxnTime(txnTime);
		unionpayBatchCollection.setTotalQty(requestHead.getTotalNumber());
		unionpayBatchCollection.setTotalAmt(requestHead.getTotalAmount());
		unionpayBatchCollection.setRequestFileContent(batchCollectionRequest.getTxtFileContent());
		unionpayBatchCollection.setQueryResultCount(0);
		unionpayBatchCollection.setFinalCode(finalCode);
		unionpayBatchCollection.setCat(now);
		unionpayBatchCollection.setMat(now);

		return unionpayBatchCollection;
	}

	private BatchCollectionRequest buildBatchCollectionRequest(List<UnionpayCollection> unionpayCollections) {
		BatchCollectionRequest batchCollectionRequest = new BatchCollectionRequest();
		RequestHead requestHead = new RequestHead();

		BigDecimal totalAmt = new BigDecimal("0");
		int totalNumber = unionpayCollections.size();
		RequestContent requestContent = null;
		String txnAmt = null;
		for (UnionpayCollection unionpayCollection : unionpayCollections) {
			requestContent = new RequestContent();
			txnAmt = unionpayCollection.getTxnAmt();

			requestContent.setOrderId(unionpayCollection.getOrderId());
			requestContent.setCurrencyCode(unionpayCollection.getCurrencyCode());
			requestContent.setTxnAmt(txnAmt);
			requestContent.setAccType(unionpayCollection.getAccType());
			requestContent.setAccNo(unionpayCollection.getAccNo());
			requestContent.setCustomerNm(unionpayCollection.getCustomerNm());
			requestContent.setBizType(unionpayCollection.getBizType());
			requestContent.setCertifTp(unionpayCollection.getCertifTp());
			requestContent.setCertifId(unionpayCollection.getCertifId());
			requestContent.setPhoneNo(unionpayCollection.getPhoneNo());
			requestContent.setPostscript(unionpayCollection.getPostscript());

			batchCollectionRequest.addContent(requestContent);
			totalAmt = totalAmt.add(new BigDecimal(txnAmt));
		}
		requestHead.setTotalAmount(String.valueOf(totalAmt.longValue()));
		requestHead.setTotalNumber(String.valueOf(totalNumber));
		batchCollectionRequest.setHead(requestHead);

		return batchCollectionRequest;
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
			handlingBatchTradeResult(unionpayBatchCollection, toBeSentOrder);
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
