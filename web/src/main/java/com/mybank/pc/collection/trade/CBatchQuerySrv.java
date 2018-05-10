package com.mybank.pc.collection.trade;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.SqlPara;
import com.mybank.pc.collection.model.CollectionTrade;
import com.mybank.pc.collection.model.UnionpayBatchCollection;
import com.mybank.pc.collection.model.UnionpayBatchCollectionQuery;
import com.mybank.pc.collection.model.UnionpayCollection;
import com.mybank.pc.exception.BaseCollectionRuntimeException;
import com.mybank.pc.kits.unionpay.acp.AcpService;
import com.mybank.pc.kits.unionpay.acp.SDKConstants;
import com.mybank.pc.kits.unionpay.acp.file.collection.model.BatchCollectionRequest;
import com.mybank.pc.kits.unionpay.acp.file.collection.model.BatchCollectionResponse;
import com.mybank.pc.kits.unionpay.acp.file.collection.model.RequestContent;
import com.mybank.pc.kits.unionpay.acp.file.collection.model.ResponseContent;
import com.mybank.pc.kits.unionpay.acp.file.collection.model.ResponseHead;

public class CBatchQuerySrv {

	public void batchQuery() {
		Date now = new Date();
		Kv kv = Kv.create();
		String sysQueryId = UUID.randomUUID().toString();
		kv.set("sysQueryId", sysQueryId).set("mat", now);

		int count = UnionpayBatchCollection.updateNeedQueryBatchCollectionPrepare(kv);
		batchQuery(count, sysQueryId);
	}

	public void batchQueryOne(Map<String, String> reqParam) {
		Date now = new Date();
		Kv kv = Kv.create().set(reqParam);
		String sysQueryId = UUID.randomUUID().toString();
		kv.set("sysQueryId", sysQueryId).set("mat", now);

		int count = UnionpayBatchCollection.updateNeedQueryBatchCollectionPrepareOne(kv);
		batchQuery(count, sysQueryId);
	}

	private void batchQuery(int count, String sysQueryId) {
		if (count <= 0) {
			return;
		}
		List<UnionpayBatchCollection> ubcList = null;
		try {
			ubcList = UnionpayBatchCollection
					.findNeedQueryBatchCollectionBySysQueryId(Kv.create().set("sysQueryId", sysQueryId));
			if (CollectionUtils.isNotEmpty(ubcList)) {
				for (UnionpayBatchCollection unionpayBatchCollection : ubcList) {
					batchQuery(unionpayBatchCollection);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (ubcList != null) {
				for (UnionpayBatchCollection unionpayBatchCollection : ubcList) {
					try {
						unionpayBatchCollection.setStatus("0");
						unionpayBatchCollection.setSysQueryId("");
						unionpayBatchCollection.update();
					} catch (Exception ubce) {
						ubce.printStackTrace();
					}
				}
			}
		}
	}

	public void batchQuery(UnionpayBatchCollection unionpayBatchCollection) {
		UnionpayBatchCollectionQuery unionpayBatchCollectionQuery = null;
		boolean isSaved = false;
		try {
			unionpayBatchCollectionQuery = unionpayBatchCollection.buildQueryResult();
			if (isSaved = unionpayBatchCollectionQuery.save()) {
				unionpayBatchCollection.queryResult();
				handlingBatchQueryResult(unionpayBatchCollection);
			}
		} catch (Exception e) {
			e.printStackTrace();

			if (unionpayBatchCollectionQuery != null) {
				unionpayBatchCollectionQuery
						.setExceInfo(JsonKit.toJson(BaseCollectionRuntimeException.getExceptionInfo(e)));
				if (!isSaved) {
					unionpayBatchCollectionQuery.save();
				} else {
					unionpayBatchCollectionQuery.update();
				}
			}
		} finally {
			unionpayBatchCollection.setStatus("0");
			unionpayBatchCollection.setSysQueryId("");
			unionpayBatchCollection.update();
		}
	}

	private boolean handlingBatchQueryResult(UnionpayBatchCollection unionpayBatchCollection) throws Exception {
		boolean isSuccess = false;
		try {
			Integer queryCount = unionpayBatchCollection.getQueryResultCount();
			queryCount = queryCount == null ? 0 : queryCount;
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

			// 成功 落地查询结果
			if (("00").equals(respCode)) {
				String fileContent = rspData.get("fileContent");
				String queryResult = AcpService.getFileContent(fileContent, SDKConstants.UTF_8_ENCODING);

				unionpayBatchCollectionQuery.setRespFileContent(queryResult);
				unionpayBatchCollection.setResultFileContent(queryResult);

				BatchCollectionResponse batchCollectionResponse = new BatchCollectionResponse(queryResult);
				ResponseHead responseHead = batchCollectionResponse.getHead();

				List<ResponseContent> responsesContents = batchCollectionResponse.getContents();
				for (ResponseContent responseContent : responsesContents) {
					try {
						updateOrderStatus(responseContent);
					} catch (Exception e) {
						e.printStackTrace();
					}
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

				// 批次（xxxx）不存在
				if ("34".equals(respCode)) {
					if (unionpayBatchCollectionQuery.isTimeout()) {
						BatchCollectionRequest batchCollectionRequest = unionpayBatchCollection
								.toBatchCollectionRequest();
						if (batchCollectionRequest != null) {
							List<RequestContent> requestContents = batchCollectionRequest.getContents();
							for (RequestContent requestContent : requestContents) {
								updateOrderStatusToFail(requestContent, unionpayBatchCollectionQuery);
							}
						}
					}
				}

			}

			unionpayBatchCollection.setQueryResultCount(queryCount + 1);
			unionpayBatchCollection.setNextAllowQueryDate();

			Date now = new Date();
			unionpayBatchCollectionQuery.setMat(now);
			unionpayBatchCollectionQuery.update();

			unionpayBatchCollection.setMat(now);
		} catch (Exception e) {
			e.printStackTrace();
			isSuccess = false;
			throw e;
		}
		return isSuccess;
	}

	public void updateOrderStatus(ResponseContent responseContent) {
		String orderId = responseContent.getOrderId();
		if (StringUtils.isNotBlank(orderId)) {

			String respCode = responseContent.getRespCode();
			String respMsg = responseContent.getRespMsg();
			String queryId = responseContent.getQueryId();
			String traceNo = responseContent.getSysTraNo();
			String traceTime = responseContent.getSysTm();
			String settleDate = responseContent.getSettleDate();

			Date now = new Date();

			Kv kv = Kv.create();
			kv.set("orderId", orderId);
			SqlPara sqlPara = Db.getSqlPara("collection_trade.findUnionpayCollection", kv);
			UnionpayCollection unionpayCollection = UnionpayCollection.dao.findFirst(sqlPara);
			if (unionpayCollection != null) {
				if ("00".equals(respCode)) {
					// 成功
					unionpayCollection.setFinalCode("0");
				} else if ("03".equals(respCode) || "04".equals(respCode) || "05".equals(respCode)) {
					// 订单处理中或交易状态未明
				} else {
					// 失败
					unionpayCollection.setFinalCode("2");
				}

				unionpayCollection.setResultCode(respCode);
				unionpayCollection.setResultMsg(respMsg);
				unionpayCollection.setResult(JsonKit.toJson(responseContent));
				unionpayCollection.setSettleDate(settleDate);
				unionpayCollection.setTraceNo(traceNo);
				unionpayCollection.setTraceTime(traceTime);
				unionpayCollection.setQueryId(queryId);
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

	public void updateOrderStatusToFail(RequestContent requestContent,
			UnionpayBatchCollectionQuery unionpayBatchCollectionQuery) {
		String orderId = requestContent.getOrderId();
		if (StringUtils.isNotBlank(orderId)) {
			String respCode = unionpayBatchCollectionQuery.getRespCode();
			String respMsg = unionpayBatchCollectionQuery.getRespMsg();

			Date now = new Date();
			Kv kv = Kv.create();
			kv.set("orderId", orderId);
			SqlPara sqlPara = Db.getSqlPara("collection_trade.findUnionpayCollection", kv);
			UnionpayCollection unionpayCollection = UnionpayCollection.dao.findFirst(sqlPara);
			if (unionpayCollection != null) {
				unionpayCollection.setFinalCode("2");// 失败
				unionpayCollection.setResultCode(respCode);
				unionpayCollection.setResultMsg(respMsg);
				unionpayCollection.setMat(now);
				unionpayCollection.update();

				sqlPara = Db.getSqlPara("collection_trade.findByTradeNo",
						kv.set("tradeNo", unionpayCollection.getTradeNo()));
				CollectionTrade collectionTrade = CollectionTrade.dao.findFirst(sqlPara);
				if (collectionTrade != null) {
					collectionTrade.setResultCode(respCode);
					collectionTrade.setResultMsg(respMsg);
					collectionTrade.setFinalCode("2");// 失败
					collectionTrade.setMat(now);
					collectionTrade.update();
				}
			}
		}
	}

}
