package com.mybank.pc.advance.trade;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.aop.Invocation;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Kv;
import com.jfinal.kit.LogKit;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.mybank.pc.advance.model.UnionpayAdvance;
import com.mybank.pc.collection.model.sender.SendProxy;
import com.mybank.pc.exception.AdvanceRuntimeException;
import com.mybank.pc.exception.TxnKey;
import com.mybank.pc.exception.ValidateCTRException;
import com.mybank.pc.exception.ValidateUnionpayRespException;
import com.mybank.pc.interceptors.AdvanceExceptionInterceptor;
import com.mybank.pc.kits.unionpay.acp.AcpResponse;
import com.mybank.pc.kits.unionpay.acp.SDK;

public class ATradeSrv {

	public Kv initiate(Kv kv) {
		Kv result = Kv.create();
		boolean isSuccess = false;

		Kv initiateRequest = validateAndBuildInitiateRequest(kv);
		if (isSuccess = initiateRequest.getBoolean("isValidate")) {
			UnionpayAdvance unionpayAdvance = (UnionpayAdvance) initiateRequest.get("unionpayAdvance");
			result.set("unionpayAdvance", unionpayAdvance);
			try {
				unionpayAdvance.assemblyRequest();
				if (saveOrder(unionpayAdvance)) {
					isSuccess = sendOrder(unionpayAdvance);
				}
			} catch (AdvanceRuntimeException e) {
				e.printStackTrace();
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
				AdvanceRuntimeException xe = new AdvanceRuntimeException(e);
				xe.setUnionpayAdvance(unionpayAdvance);
				throw xe;
			}
		} else {
			if (initiateRequest.containsKey("exception")) {
				Exception e = (Exception) initiateRequest.get("exception");
				if (e instanceof ValidateCTRException) {
					throw (ValidateCTRException) e;
				} else {
					throw new AdvanceRuntimeException(e);
				}
			}
		}
		return result.set("isSuccess", isSuccess);
	}

	public Kv validateAndBuildInitiateRequest(Kv kv) {
		// 平台调用相关参数
		SDK sdk = null;
		String finalCode = "1";// 最终处理结果：0成功 1处理中 2失败

		Kv result = Kv.create();
		boolean isValidate = false;

		UnionpayAdvance unionpayAdvance = new UnionpayAdvance();
		try {
			String accNo = kv.getStr("accNo");
			String customerNm = kv.getStr("customerNm");
			String certifId = kv.getStr("certifId");
			String txnAmt = kv.getStr("txnAmt");
			String operID = kv.getStr("operID");
			String merCode = kv.getStr("merCode");

			if (StringUtils.isBlank(accNo)) {
				throw new ValidateCTRException("账号不能为空");
			}
			BigDecimal originaltxnAmt = null;
			long numTxnAmt = -1;
			try {
				originaltxnAmt = new BigDecimal(txnAmt = txnAmt.trim());
				numTxnAmt = originaltxnAmt.multiply(new BigDecimal(100)).longValue();
				if (numTxnAmt < 1) {
					throw new RuntimeException();
				}
				txnAmt = String.valueOf(numTxnAmt);
			} catch (Exception e) {
				throw new ValidateCTRException("非法的交易金额[" + txnAmt + "]");
			}
			if (StringUtils.isBlank(customerNm)) {
				throw new ValidateCTRException("姓名不能为空");
			}
			if (StringUtils.isBlank(certifId)) {
				throw new ValidateCTRException("证件号码不能为空");
			}

			if (StringUtils.isNotBlank(merCode)) {
				if ("0".equals(merCode)) {
					sdk = SDK.getSDK(SDK.MER_CODE_ADVANCE_YS);
				} else if ("1".equals(merCode)) {
					sdk = SDK.getSDK(SDK.MER_CODE_TEST);
				} else {
					throw new ValidateCTRException("非法的商户类型[" + merCode + "]");
				}
			} else {
				sdk = SDK.getSDK(SDK.MER_CODE_ADVANCE_YS);
			}

			unionpayAdvance.setMerId(sdk.getMerId());
			unionpayAdvance.toAdvance();

			Date now = new Date();
			String txnTime = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
			String reqReserved = "from=pac";

			unionpayAdvance.setCustomerNm(customerNm);
			unionpayAdvance.setCertifId(certifId);
			unionpayAdvance.setAccNo(accNo);
			unionpayAdvance.setTxnTime(txnTime);
			unionpayAdvance.setTxnAmt(txnAmt);
			unionpayAdvance.setReqReserved(reqReserved);
			unionpayAdvance.setFinalCode(finalCode);
			unionpayAdvance.setCat(now);
			unionpayAdvance.setMat(now);
			unionpayAdvance.setOperID(operID);
			unionpayAdvance.setQueryResultCount(0);

			// 订单号
			String orderId = generateOrderId(now, unionpayAdvance);
			unionpayAdvance.setOrderId(orderId);

			isValidate = true;
		} catch (Exception e) {
			e.printStackTrace();
			isValidate = false;
			result.set("errorMessage", e.getMessage());
			result.set("exception", e);
		} finally {
			result.set("isValidate", isValidate);
			result.set("unionpayAdvance", unionpayAdvance);
		}
		return result;
	}

	public static String generateOrderId(Date now, UnionpayAdvance unionpayAdvance) {
		int maxLength = 40;
		String orderId = null;
		String certifld = unionpayAdvance.getCertifId();
		try {
			orderId = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(now) + unionpayAdvance.getTxnType()
					+ unionpayAdvance.getTxnSubType() + certifld;
			if (orderId.length() > maxLength) {
				orderId = orderId.substring(0, maxLength);
			}
			LogKit.info("生成的orderId[" + orderId + "]");
			if (UnionpayAdvance.findByOrderId(orderId) != null) {
				int tryCount = 10;
				while (--tryCount > 0) {
					orderId = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(now) + unionpayAdvance.getTxnType()
							+ unionpayAdvance.getTxnSubType() + certifld;
					if (orderId.length() > maxLength) {
						orderId = orderId.substring(0, maxLength);
					}
					LogKit.info("生成的orderId[" + orderId + "]");
					if (UnionpayAdvance.findByOrderId(orderId) == null) {
						return orderId;
					}
				}
			} else {
				return orderId;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("系统内部错误，生成订单号失败[" + e.getMessage() + "]");
		}
		throw new RuntimeException("系统内部错误，生成订单号失败[" + orderId + "]");
	}

	@Before({ AdvanceExceptionInterceptor.class, Tx.class })
	@TxnKey("advance-saveOrder")
	public boolean saveOrder(UnionpayAdvance unionpayAdvance) {
		try {
			return unionpayAdvance.save();
		} catch (Exception e) {
			e.printStackTrace();
			unionpayAdvance.setFinalCode("2");

			AdvanceRuntimeException xe = new AdvanceRuntimeException(e);
			xe.setUnionpayAdvance(unionpayAdvance);
			throw xe;
		}
	}

	@Before({ AdvanceExceptionInterceptor.class, Tx.class })
	@TxnKey("advance-sendOrder")
	public boolean sendOrder(UnionpayAdvance unionpayAdvance) {
		try {
			try {
				unionpayAdvance.sendOrder();
			} catch (Exception e) {
				unionpayAdvance.setMat(new Date());
				unionpayAdvance.setFinalCode("3");// 状态未明 需后续处理
				unionpayAdvance.update();

				AdvanceSyncStatusExecutor.scheduleNotClearSingleQuery(unionpayAdvance.getOrderId(), 10,
						TimeUnit.SECONDS);
				throw e;
			}

			return handlingTradeResult(unionpayAdvance);
		} catch (AdvanceRuntimeException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			AdvanceRuntimeException xe = new AdvanceRuntimeException(e);
			xe.setUnionpayAdvance(unionpayAdvance);
			throw xe;
		}
	}

	private boolean handlingTradeResult(UnionpayAdvance unionpayAdvance) {
		boolean isSuccess = false;
		try {
			Date now = new Date();
			try {
				unionpayAdvance.validateRealtimeResp();
			} catch (ValidateUnionpayRespException vure) {
				vure.printStackTrace();
				SendProxy sendProxy = vure.getSendProxy();
				unionpayAdvance.setResp(JsonKit.toJson(sendProxy.getAcpResponse()));
				unionpayAdvance.setExceInfo(JsonKit.toJson(vure.getExceptionInfo()));
				if (sendProxy.isInvalidRequestOrURI()) {
					isSuccess = false;
					unionpayAdvance.setMat(now);
					unionpayAdvance.setFinalCode("2");// 失败
					unionpayAdvance.update();
				} else {
					unionpayAdvance.setMat(now);
					unionpayAdvance.setFinalCode("3");// 状态未明 需后续处理
					unionpayAdvance.update();

					AdvanceSyncStatusExecutor.scheduleNotClearSingleQuery(unionpayAdvance.getOrderId(), 10,
							TimeUnit.SECONDS);
				}
				throw vure;
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}

			Map<String, String> rspData = unionpayAdvance.getRspData();
			String respCode = rspData.get("respCode");
			String respMsg = rspData.get("respMsg");
			String queryId = rspData.get("queryId");

			unionpayAdvance.setRespCode(respCode);
			unionpayAdvance.setRespMsg(respMsg);
			unionpayAdvance.setQueryId(queryId);
			unionpayAdvance.setMat(now);

			// 00 交易已受理(不代表交易已成功），等待接收后台通知更新订单状态,也可以主动发起 查询交易确定交易状态。
			//// 后续需发起交易状态查询交易确定交易状态
			if ("00".equals(respCode) || "03".equals(respCode) || "04".equals(respCode) || "05".equals(respCode)) {
				// 如果是配置了敏感信息加密，如果需要获取卡号的铭文，可以按以下方法解密卡号
				// String accNo1 = resmap.get("accNo");
				// String accNo2 = AcpService.decryptData(accNo1, "UTF-8");
				// //解密卡号使用的证书是商户签名私钥证书acpsdk.signCert.path
				// LogUtil.writeLog("解密后的卡号："+accNo2);
				isSuccess = true;
				AdvanceSyncStatusExecutor.scheduleInProcessSingleQuery(unionpayAdvance.getOrderId(), 0,
						TimeUnit.SECONDS);
			} else {
				isSuccess = false;
				unionpayAdvance.setFinalCode("2");
			}

			unionpayAdvance.update();
			return isSuccess;
		} catch (Exception e) {
			e.printStackTrace();
			AdvanceRuntimeException xe = new AdvanceRuntimeException(e);
			xe.setUnionpayAdvance(unionpayAdvance);
			throw xe;
		}
	}

	public void updateOrderStatus(Map<String, String> reqParam) {
		String respCode = reqParam.get("respCode");
		String orderId = reqParam.get("orderId");
		try {
			UnionpayAdvance unionpayAdvance = UnionpayAdvance.findByOrderId(orderId);
			if ("00".equals(respCode) || "A6".equals(respCode)) {// 交易成功
				syncOrderStatus(unionpayAdvance);
			} else if ("03".equals(respCode) || "04".equals(respCode) || "05".equals(respCode)) {// 订单处理中或交易状态未明
				setResultByCallback(unionpayAdvance, reqParam, "1");// 处理中
				AdvanceSyncStatusExecutor.scheduleInProcessSingleQuery(unionpayAdvance.getOrderId(), 5, 5,
						TimeUnit.SECONDS);
			} else {// 交易失败
				setResultByCallback(unionpayAdvance, reqParam, "2");// 失败
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setResultByCallback(UnionpayAdvance unionpayAdvance, Map<String, String> reqParam, String finalCode) {
		Date now = new Date();
		String respCode = reqParam.get("respCode");
		String respMsg = reqParam.get("respMsg");

		String queryId = reqParam.get("queryId");
		String settleAmt = reqParam.get("settleAmt");
		String settleCurrencyCode = reqParam.get("settleCurrencyCode");
		String settleDate = reqParam.get("settleDate");
		String traceNo = reqParam.get("traceNo");
		String traceTime = reqParam.get("traceTime");
		if (unionpayAdvance != null) {
			unionpayAdvance.setFinalCode(finalCode);
			unionpayAdvance.setResultCode(respCode);
			unionpayAdvance.setResultMsg(respMsg);
			unionpayAdvance.setResult(JsonKit.toJson(reqParam));
			unionpayAdvance.setQueryId(queryId);
			unionpayAdvance.setSettleAmt(settleAmt);
			unionpayAdvance.setSettleCurrencyCode(settleCurrencyCode);
			unionpayAdvance.setSettleDate(settleDate);
			unionpayAdvance.setTraceNo(traceNo);
			unionpayAdvance.setTraceTime(traceTime);
			unionpayAdvance.setMat(now);
			unionpayAdvance.update();
		}
	}

	public void syncOrderStatus(UnionpayAdvance unionpayAdvance) throws Exception {
		if (unionpayAdvance != null) {
			new AdvanceStatusSynchronizer(unionpayAdvance).sync();
		}
	}

	public void handlingException(Invocation invocation, AdvanceRuntimeException e) {
		Method method = invocation.getMethod();
		if (!method.isAnnotationPresent(TxnKey.class)) {
			return;
		}
		UnionpayAdvance unionpayAdvance = e.getUnionpayAdvance();

		TxnKey txnKey = method.getAnnotation(TxnKey.class);
		String txnKeyValue = txnKey.value();
		if (txnKeyValue.equals("advance-saveOrder")) {
			try {
				unionpayAdvance.setExceInfo(JsonKit.toJson(e.getExceptionInfo()));
				unionpayAdvance.setFinalCode("2");// 失败
				unionpayAdvance.save();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (txnKeyValue.equals("advance-sendOrder")) {
			try {
				if (StringUtils.isBlank(unionpayAdvance.getResp())) {
					SendProxy sendProxy = unionpayAdvance.getSendProxy();
					AcpResponse acpResponse = sendProxy == null ? null : sendProxy.getAcpResponse();
					if (acpResponse != null) {
						unionpayAdvance.setResp(JsonKit.toJson(acpResponse));
					}
				}
				unionpayAdvance.setExceInfo(JsonKit.toJson(e.getExceptionInfo()));
				unionpayAdvance.update();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

}
