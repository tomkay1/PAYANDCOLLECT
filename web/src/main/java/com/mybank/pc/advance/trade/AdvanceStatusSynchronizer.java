package com.mybank.pc.advance.trade;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.kit.JsonKit;
import com.mybank.pc.advance.model.UnionpayAdvance;
import com.mybank.pc.advance.model.UnionpayAdvanceQuery;
import com.mybank.pc.collection.model.UnionpayCallbackLog;
import com.mybank.pc.collection.model.sender.SendProxy;
import com.mybank.pc.exception.BaseCollectionRuntimeException;
import com.mybank.pc.kits.unionpay.acp.AcpResponse;

public class AdvanceStatusSynchronizer {

	private UnionpayAdvance unionpayAdvance;

	public AdvanceStatusSynchronizer(UnionpayAdvance unionpayAdvance) {
		this.unionpayAdvance = unionpayAdvance;
	}

	public void sync() throws Exception {
		if (unionpayAdvance != null) {
			String lock = buildSyncLock(unionpayAdvance);
			synchronized (lock) {
				try {
					syncOrderStatus(unionpayAdvance);
				} catch (Exception e) {
					e.printStackTrace();
					if (unionpayAdvance != null) {
						unionpayAdvance.update();
					}
					throw e;
				}
			}
		}
	}

	private static String buildSyncLock(UnionpayAdvance unionpayAdvance) {
		String orderId = unionpayAdvance.getOrderId();
		return ("advance-syncOrderStatus-" + (StringUtils.isNotBlank(orderId) ? orderId : "")).intern();
	}

	public void syncOrderStatus(UnionpayAdvance unionpayAdvance) {
		try {
			UnionpayAdvanceQuery query = new UnionpayAdvanceQuery();
			query.setOrderId(unionpayAdvance.getOrderId());
			List<UnionpayAdvanceQuery> queryHistory = query.findUnionpayAdvanceQuery();
			if (CollectionUtils.isEmpty(queryHistory)) {
				queryResult(unionpayAdvance);
			} else {
				UnionpayAdvanceQuery lastsQuery = queryHistory.get(0);

				String queryRespCode = lastsQuery.getRespCode();
				String origRespCode = lastsQuery.getOrigRespCode();
				String origRespMsg = lastsQuery.getOrigRespMsg();
				String resp = lastsQuery.getResp();
				String queryId = lastsQuery.getQueryId();
				String settleAmt = lastsQuery.getSettleAmt();
				String settleCurrencyCode = lastsQuery.getSettleCurrencyCode();
				String settleDate = lastsQuery.getSettleDate();
				String traceNo = lastsQuery.getTraceNo();
				String traceTime = lastsQuery.getTraceTime();

				boolean isFail = false;
				boolean origRespCodeIsNotBlank = StringUtils.isNotBlank(origRespCode);
				boolean isUnkonwOrigRespCode = "03".equals(origRespCode) || "04".equals(origRespCode)
						|| "05".equals(origRespCode);// 订单处理中或交易状态未明

				// 查询成功
				if ("00".equals(queryRespCode)) {
					Date now = new Date();
					if ("00".equals(origRespCode) || "A6".equals(origRespCode)) {// 成功
						unionpayAdvance.setFinalCode("0");
					} else if (origRespCodeIsNotBlank && (!isUnkonwOrigRespCode)) {// 失败
						unionpayAdvance.setFinalCode("2");
					}

					unionpayAdvance.setResultCode(origRespCode);
					unionpayAdvance.setResultMsg(origRespMsg);
					unionpayAdvance.setResult(resp);
					unionpayAdvance.setQueryId(queryId);
					unionpayAdvance.setSettleAmt(settleAmt);
					unionpayAdvance.setSettleCurrencyCode(settleCurrencyCode);
					unionpayAdvance.setSettleDate(settleDate);
					unionpayAdvance.setTraceNo(traceNo);
					unionpayAdvance.setTraceTime(traceTime);
					unionpayAdvance.setMat(now);
					unionpayAdvance.update();
				} else if ("34".equals(queryRespCode)) {// 订单不存在
					isFail = syncInRespCode34(unionpayAdvance, lastsQuery);
				}
				// 订单处理中或交易状态未明
				if (isUnkonwOrigRespCode || ((!"00".equals(queryRespCode)) && !isFail)) {
					queryResult(unionpayAdvance);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void queryResult(UnionpayAdvance unionpayAdvance) throws Exception {
		boolean isSaved = false;
		UnionpayAdvanceQuery query = null;
		SendProxy sendProxy = null;
		try {
			query = unionpayAdvance.buildQuery();
			if (isSaved = query.save()) {
				sendProxy = query.queryResult();
				handlingQueryResult(unionpayAdvance, query);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (query != null) {
				AcpResponse acpResponse = sendProxy == null ? null : sendProxy.getAcpResponse();
				if (acpResponse != null) {
					query.setResp(JsonKit.toJson(acpResponse));
				}
				query.setExceInfo(JsonKit.toJson(BaseCollectionRuntimeException.getExceptionInfo(e)));
			}
			throw e;
		} finally {
			if (query != null) {
				if (!isSaved) {
					query.save();
				} else {
					query.setMat(new Date());
					query.update();
				}
			}
		}
	}

	public void handlingQueryResult(UnionpayAdvance unionpayAdvance, UnionpayAdvanceQuery query) {
		query.validateQueryResp();
		query.setFieldFromQueryResp();

		Date now = new Date();
		String respCode = query.getRespCode();
		String origRespCode = query.getOrigRespCode();
		String origRespMsg = query.getOrigRespMsg();
		if (("00").equals(respCode)) {// 如果查询交易成功

			Integer queryCount = unionpayAdvance.getQueryResultCount();
			unionpayAdvance.setQueryResultCount(queryCount == null ? 1 : queryCount + 1);
			unionpayAdvance.setResultCode(origRespCode);
			unionpayAdvance.setResultMsg(origRespMsg);
			unionpayAdvance.setQueryId(query.getQueryId());
			unionpayAdvance.setSettleAmt(query.getSettleAmt());
			unionpayAdvance.setSettleCurrencyCode(query.getSettleCurrencyCode());
			unionpayAdvance.setSettleDate(query.getSettleDate());
			unionpayAdvance.setTraceNo(query.getTraceNo());
			unionpayAdvance.setTraceTime(query.getTraceTime());
			unionpayAdvance.setResult(query.getResp());
			unionpayAdvance.setMat(now);

			if ("00".equals(origRespCode) || "A6".equals(origRespCode)) {
				// 交易成功，更新商户订单状态
				unionpayAdvance.setFinalCode("0");// 成功
				unionpayAdvance.update();
			} else if (("03").equals(origRespCode) || ("04").equals(origRespCode) || ("05").equals(origRespCode)) {
				// 订单处理中或交易状态未明，需稍后发起交易状态查询交易 【如果最终尚未确定交易是否成功请以对账文件为准】
				unionpayAdvance.update();
			} else {
				// 其他应答码为交易失败
				unionpayAdvance.setFinalCode("2");// 失败
				unionpayAdvance.update();
			}
		} else if (("34").equals(respCode)) {
			syncInRespCode34(unionpayAdvance, query);
		} else {
			// 查询交易本身失败，如应答码10/11检查查询报文是否正确
		}
	}

	private boolean syncInRespCode34(UnionpayAdvance unionpayAdvance, UnionpayAdvanceQuery query) {
		boolean isFail = false;
		try {
			Date now = new Date();
			int timeoutMinute = UnionpayAdvance.TIMEOUT_MINUTE;
			if (query.isTimeout(timeoutMinute)) {// 订单不存在且超时,视为原交易失败
				unionpayAdvance.setFinalCode("2");// 失败
				unionpayAdvance.setResultCode(query.getRespCode());
				unionpayAdvance.setResultMsg(query.getRespMsg());
				unionpayAdvance.setResult(query.getResp());
				unionpayAdvance.setMat(now);
				unionpayAdvance.update();
				isFail = true;
			} else {
				UnionpayCallbackLog lastsCallbackLog = null;
				List<UnionpayCallbackLog> callbackLog = UnionpayCallbackLog
						.findCallbackByOrderId(unionpayAdvance.getOrderId());
				if (CollectionUtils.isNotEmpty(callbackLog)) {
					lastsCallbackLog = callbackLog.get(0);
					isFail = UnionpayAdvance.isFailCode(lastsCallbackLog.getRespCode());
				} else {
					String respCode = unionpayAdvance.getRespCode();
					String resultCode = unionpayAdvance.getResultCode();
					isFail = UnionpayAdvance.isFailCode(respCode) || UnionpayAdvance.isFailCode(resultCode);
				}

				if (isFail) {
					unionpayAdvance.setFinalCode("2");// 失败
				}
				if (lastsCallbackLog != null) {
					unionpayAdvance.setResultCode(lastsCallbackLog.getRespCode());
					unionpayAdvance.setResultMsg(lastsCallbackLog.getRespMsg());
					unionpayAdvance.setResult(lastsCallbackLog.getInfo());
				} else {
					unionpayAdvance.setResultCode(query.getRespCode());
					unionpayAdvance.setResultMsg(query.getRespMsg());
					unionpayAdvance.setResult(query.getResp());
				}
				unionpayAdvance.setMat(now);
				unionpayAdvance.update();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isFail;
	}

}
