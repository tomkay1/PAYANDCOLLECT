package com.mybank.pc.collection.trade;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.mybank.pc.collection.model.CollectionTrade;
import com.mybank.pc.collection.model.UnionpayBatchCollection;
import com.mybank.pc.collection.model.UnionpayBatchCollectionQuery;
import com.mybank.pc.collection.model.UnionpayCollection;
import com.mybank.pc.exception.BaseCollectionRuntimeException;
import com.mybank.pc.exception.TxnKey;
import com.mybank.pc.kits.unionpay.acp.AcpService;
import com.mybank.pc.kits.unionpay.acp.SDKConstants;
import com.mybank.pc.kits.unionpay.acp.file.collection.model.BatchCollectionResponse;
import com.mybank.pc.kits.unionpay.acp.file.collection.model.ResponseContent;
import com.mybank.pc.kits.unionpay.acp.file.collection.model.ResponseHead;

public class CBatchQuerySrv {

	public void batchQuery(UnionpayBatchCollection unionpayBatchCollection) {
		UnionpayBatchCollectionQuery unionpayBatchCollectionQuery = unionpayBatchCollection.buildQueryResult();
		saveBatchQueryRecord(unionpayBatchCollectionQuery);
		sendBatchQueryOrder(unionpayBatchCollection);
	}

	@Before({ Tx.class })
	@TxnKey("saveBatchQueryOrder")
	public void saveBatchQueryRecord(UnionpayBatchCollectionQuery unionpayBatchCollectionQuery) {
		try {
			unionpayBatchCollectionQuery.save();
		} catch (Exception e) {
			unionpayBatchCollectionQuery
					.setExceInfo(JsonKit.toJson(BaseCollectionRuntimeException.getExceptionInfo(e)));
			throw e;
		}
	}

	@Before({ Tx.class })
	@TxnKey("batch-sendQueryOrder")
	public void sendBatchQueryOrder(UnionpayBatchCollection unionpayBatchCollection) {
		try {
			unionpayBatchCollection.queryResult();
			handlingBatchQueryResult(unionpayBatchCollection);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean handlingBatchQueryResult(UnionpayBatchCollection unionpayBatchCollection) {
		boolean isSuccess = false;
		try {
			UnionpayBatchCollectionQuery unionpayBatchCollectionQuery = unionpayBatchCollection.getQuery();
			unionpayBatchCollectionQuery.validateBatchQueryResp();
			Map<String, String> rspData = unionpayBatchCollectionQuery.getQueryRspData();

			String respCode = rspData.get("respCode");
			String respMsg = rspData.get("respMsg");

			unionpayBatchCollectionQuery.setRespCode(respCode);
			unionpayBatchCollectionQuery.setRespMsg(respMsg);
			unionpayBatchCollection.setResultCode(respCode);
			unionpayBatchCollection.setResultMsg(respMsg);
			unionpayBatchCollection.setResult(JsonKit.toJson(rspData));

			if (("00").equals(respCode)) {
				// 成功 落地查询结果样例
				String fileContent = rspData.get("fileContent");
				String queryResult = AcpService.getFileContent(fileContent, SDKConstants.UTF_8_ENCODING);

				unionpayBatchCollectionQuery.setRespFileContent(queryResult);
				unionpayBatchCollection.setResultFileContent(queryResult);

				BatchCollectionResponse batchCollectionResponse = new BatchCollectionResponse(queryResult);
				ResponseHead responseHead = batchCollectionResponse.getHead();

				List<ResponseContent> responsesContents = batchCollectionResponse.getContents();
				for (ResponseContent responseContent : responsesContents) {
					updateOrderStatus(responseContent);
				}

				String successCount = responseHead.getSuccessCount();
				String successAmount = responseHead.getSuccessAmount();

				unionpayBatchCollectionQuery.setSuccessQty(successCount);
				unionpayBatchCollectionQuery.setSuccessAmt(successAmount);
				unionpayBatchCollection.setSuccessQty(successCount);
				unionpayBatchCollection.setSuccessAmt(successAmount);

				isSuccess = true;
				unionpayBatchCollection.setFinalCode("0");
			} else {
				// 其他应答码为失败请排查原因
				isSuccess = false;
			}

			Integer queryCount = unionpayBatchCollection.getQueryResultCount();
			unionpayBatchCollection.setQueryResultCount(queryCount == null ? 1 : queryCount + 1);

			Date now = new Date();
			unionpayBatchCollectionQuery.setMat(now);
			unionpayBatchCollectionQuery.update();

			unionpayBatchCollection.setMat(now);
			unionpayBatchCollection.update();
		} catch (Exception e) {
			isSuccess = false;
			// throw e;
		}
		return isSuccess;
	}

	public void updateOrderStatus(ResponseContent responseContent) {
		String orderId = responseContent.getOrderId();
		if (StringUtils.isNotBlank(orderId)) {

			String respCode = responseContent.getRespCode();
			String respMsg = responseContent.getRespMsg();
			String settleDate = responseContent.getSettleDate();
			String traceNo = responseContent.getSysTraNo();
			String traceTime = responseContent.getSysTm();

			Date now = new Date();

			Kv kv = Kv.create();
			kv.set("orderId", orderId);
			SqlPara sqlPara = Db.getSqlPara("collection_trade.findUnionpayCollection", kv);
			UnionpayCollection unionpayCollection = UnionpayCollection.dao.findFirst(sqlPara);
			if (unionpayCollection != null) {
				if ("00".equals(respCode)) {
					unionpayCollection.setFinalCode("0");// 成功
				} else {
					unionpayCollection.setFinalCode("2");// 失败
				}

				unionpayCollection.setResultCode(respCode);
				unionpayCollection.setResultMsg(respMsg);
				unionpayCollection.setResult(JsonKit.toJson(responseContent));
				unionpayCollection.setSettleDate(settleDate);
				unionpayCollection.setTraceNo(traceNo);
				unionpayCollection.setTraceTime(traceTime);
				unionpayCollection.setMat(now);
				unionpayCollection.update();

				sqlPara = Db.getSqlPara("collection_trade.findByTradeNo",
						kv.set("tradeNo", unionpayCollection.getTradeNo()));
				CollectionTrade collectionTrade = CollectionTrade.dao.findFirst(sqlPara);
				if (collectionTrade != null) {
					collectionTrade.setResultCode(respCode);
					collectionTrade.setResultMsg(respMsg);
					if ("00".equals(respCode)) {
						collectionTrade.setFinalCode("0");// 成功
					} else {
						collectionTrade.setFinalCode("2");// 失败
					}
					collectionTrade.setMat(now);
					collectionTrade.update();
				}

			}
		}
	}

}
