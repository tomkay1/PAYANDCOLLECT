package com.mybank.pc.collection.trade;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.aop.Invocation;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.ehcache.CacheKit;
import com.mybank.pc.Consts;
import com.mybank.pc.admin.model.CardBin;
import com.mybank.pc.collection.model.CollectionEntrust;
import com.mybank.pc.collection.model.CollectionTrade;
import com.mybank.pc.collection.model.UnionpayCallbackLog;
import com.mybank.pc.collection.model.UnionpayCollection;
import com.mybank.pc.collection.model.UnionpayCollectionQuery;
import com.mybank.pc.exception.BaseCollectionRuntimeException;
import com.mybank.pc.exception.TradeRuntimeException;
import com.mybank.pc.exception.TxnKey;
import com.mybank.pc.exception.ValidateCTRException;
import com.mybank.pc.interceptors.TradeExceptionInterceptor;
import com.mybank.pc.kits.FeeKit;
import com.mybank.pc.kits.unionpay.acp.SDK;
import com.mybank.pc.merchant.model.MerchantCust;
import com.mybank.pc.merchant.model.MerchantInfo;

public class CTradeSrv {

	public boolean initiate(Kv kv) {
		boolean isSuccess = false;

		Kv initiateRequest = validateAndBuildInitiateRequest(kv);
		if (isSuccess = initiateRequest.getBoolean("isValidate")) {
			UnionpayCollection unionpayCollection = (UnionpayCollection) initiateRequest.get("unionpayCollection");
			CollectionTrade collectionTrade = (CollectionTrade) initiateRequest.get("collectionTrade");

			// 需先保存订单信息后，再在需要时发起请求
			try {
				boolean isRealtime = "1".equals(kv.getStr("bussType"));
				// 加急需发起实时交易请求
				if (isRealtime) {
					unionpayCollection.assemblyRealtimeRequest();
				}
				if (saveOrder(unionpayCollection, collectionTrade)) {
					isSuccess = isRealtime ? sendRealtimeOrder(unionpayCollection, collectionTrade) : true;
				}
			} catch (TradeRuntimeException e) {
				throw e;
			} catch (Exception e) {
				TradeRuntimeException xe = new TradeRuntimeException(e);
				xe.setCollectionTrade(collectionTrade);
				xe.setUnionpayCollection(unionpayCollection);
				throw xe;
			}
		} else {
			if (initiateRequest.containsKey("exception")) {
				Exception e = (Exception) initiateRequest.get("exception");
				if (e instanceof ValidateCTRException) {
					throw (ValidateCTRException) e;
				} else {
					throw new TradeRuntimeException(e);
				}
			}
		}
		return isSuccess;
	}

	public Kv validateAndBuildInitiateRequest(Kv kv) {
		// 银联调用相关参数
		String txnType = null; // 交易类型

		// 平台调用相关参数
		SDK sdk = null;
		String tradeType = "1";// 1代收 2代付
		String finalCode = "1";// 最终处理结果：0成功 1处理中 2失败
		String clearStatus = "1";// 清分标识：0已清分 1未清分
		String merchantFeeTradeType = null;// 商户手续费交易类型 1加急 2标准

		Kv result = Kv.create();
		boolean isValidate = false;

		UnionpayCollection unionpayCollection = new UnionpayCollection();
		CollectionTrade collectionTrade = new CollectionTrade();

		try {
			String bussType = kv.getStr("bussType");
			String accNo = kv.getStr("accNo");
			String txnAmt = kv.getStr("txnAmt");
			String custID = kv.getStr("custID");
			String merchantID = kv.getStr("merchantID");
			String operID = kv.getStr("operID");

			if (StringUtils.isBlank(bussType) || (!(bussType = bussType.trim()).equals("1") && !bussType.equals("2"))) {
				throw new ValidateCTRException("非法的业务类型[" + bussType + "]");
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
			Object minAmt = CacheKit.get(Consts.CACHE_NAMES.paramCache.name(), "minAmt");
			if (minAmt != null) {
				try {
					long numMiniAmt = new BigDecimal(String.valueOf(minAmt).trim()).multiply(new BigDecimal(100))
							.longValue();
					if (numTxnAmt < numMiniAmt) {
						throw new RuntimeException();
					}
				} catch (Exception e) {
					throw new ValidateCTRException("交易金额不得小于[" + minAmt + "]");
				}
			}
			if (StringUtils.isBlank(merchantID)) {
				throw new ValidateCTRException("商户ID不能为空");
			}
			MerchantInfo merchantInfo = MerchantInfo.dao.findById(merchantID = merchantID.trim());
			if (merchantInfo == null || "1".equals(merchantInfo.getStatus()) || merchantInfo.getDat() != null) {
				throw new ValidateCTRException("商户[" + merchantID + "]" + "不存在或已停用/已删除");
			}
			if (StringUtils.isBlank(custID)) {
				throw new ValidateCTRException("客户ID不能为空");
			}
			MerchantCust merchantCust = MerchantCust.dao.findById(custID = custID.trim());
			if (merchantCust == null || merchantCust.getDat() != null) {
				throw new ValidateCTRException("客户[" + custID + "]" + "不存在或已删除");
			}
			if (StringUtils.isBlank(accNo) && StringUtils.isBlank(accNo = merchantCust.getBankcardNo())) {
				throw new ValidateCTRException("账号不能为空");
			}
			CardBin cardBin = FeeKit.getCardBin(accNo = accNo.trim());
			if (cardBin == null || !"0".equals(cardBin.getCardType())) {
				throw new ValidateCTRException("不支持的卡类型!!");
			}
			boolean isRealtimeBuss = "1".equals(bussType) ? true : false;
			String merId = null;
			if (isRealtimeBuss) {// 加急
				if (Long.valueOf(txnAmt) > 500000) {
					sdk = SDK.getSDK(SDK.MER_CODE_REALTIME_YS_4);
				} else {
					sdk = SDK.getSDK(SDK.MER_CODE_REALTIME_YS_2);
				}
				merchantFeeTradeType = "1";
				txnType = "11";

				unionpayCollection.setMerId(merId = sdk.getMerId());
				unionpayCollection.toCollection();
			} else if ("2".equals(bussType)) {// 批量
				sdk = SDK.getSDK(SDK.MER_CODE_BATCH_CH);
				merchantFeeTradeType = "2";
				txnType = "21"; // 取值：21 批量交易

				unionpayCollection.setMerId(merId = sdk.getMerId());
				unionpayCollection.toEntrustCollection();
			}

			CollectionEntrust query = new CollectionEntrust();
			query.setAccNo(accNo);
			query.setMerId(merId);
			CollectionEntrust collectionEntrust = query.findOne();
			if (collectionEntrust == null || !"0".equals(collectionEntrust.getStatus())) {
				throw new ValidateCTRException("客户委托信息不存在或未处于已委托状态");
			}

			String formattedTradeType = StringUtils.leftPad(tradeType, 2, '0');
			String formattedBussType = StringUtils.leftPad(bussType, 2, '0');
			String formattedCustID = StringUtils.leftPad(custID, isRealtimeBuss ? 10 : 8, '0');

			Date now = new Date();
			String txnTime = new SimpleDateFormat("yyyyMMddHHmmss").format(now);

			// 订单号 实时最大长度40，批量最大长度32
			String orderId = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(now) + txnType
					+ unionpayCollection.getTxnSubType() + formattedBussType + formattedCustID;
			int maxLength = "1".equals(bussType) ? 40 : 32;
			if (orderId.length() > maxLength) {
				orderId = orderId.substring(0, maxLength);
			}
			// 流水号
			String tradeNo = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(now) + formattedTradeType
					+ formattedBussType + formattedCustID;

			// 银行手续费
			collectionTrade.setBankFee(FeeKit.getBankFeeByYuan(originaltxnAmt, cardBin, merId));
			// 商户手续费
			if ("2".equals(merchantInfo.getMerchantType())) {// 外部商户
				collectionTrade.setMerFee(
						FeeKit.getMerchantFee(originaltxnAmt, Integer.valueOf(merchantID), merchantFeeTradeType));
			} else if ("1".equals(merchantInfo.getMerchantType())) {// 内部商户
				collectionTrade.setMerFee(new BigDecimal(0));
			}

			unionpayCollection.setCustomerNm(collectionEntrust.getCustomerNm());
			unionpayCollection.setCertifTp(collectionEntrust.getCertifTp());
			unionpayCollection.setCertifId(collectionEntrust.getCertifId());
			unionpayCollection.setAccNo(accNo);
			unionpayCollection.setPhoneNo(collectionEntrust.getPhoneNo());
			unionpayCollection.setCvn2(collectionEntrust.getCvn2());
			unionpayCollection.setExpired(collectionEntrust.getExpired());
			unionpayCollection.setTradeNo(tradeNo);
			unionpayCollection.setOrderId(orderId);
			unionpayCollection.setTxnType(txnType);
			unionpayCollection.setTxnTime(txnTime);
			unionpayCollection.setTxnAmt(txnAmt);
			unionpayCollection.setMerchantID(merchantID);
			unionpayCollection.setFinalCode(finalCode);
			unionpayCollection.setStatus("2".equals(bussType) ? "0" : null);
			unionpayCollection.setCat(now);
			unionpayCollection.setMat(now);
			unionpayCollection.setOperID(operID);
			unionpayCollection.setQueryResultCount(0);

			collectionTrade.setTradeNo(tradeNo);
			collectionTrade.setTradeTime(now);
			collectionTrade.setTradeType(tradeType);
			collectionTrade.setBussType(bussType);
			collectionTrade.setAmount(originaltxnAmt);
			collectionTrade.setCustID(Integer.valueOf(custID));
			collectionTrade.setCustName(collectionEntrust.getCustomerNm());
			collectionTrade.setCardID(collectionEntrust.getCertifId());
			collectionTrade.setMobileBank(collectionEntrust.getPhoneNo());
			collectionTrade.setBankcardNo(accNo);
			collectionTrade.setFinalCode(finalCode);
			collectionTrade.setClearStatus(clearStatus);
			collectionTrade.setCat(now);
			collectionTrade.setMat(now);
			collectionTrade.setOperID(operID);
			collectionTrade.setMerchantID(Integer.valueOf(merchantID));

			isValidate = true;
		} catch (Exception e) {
			e.printStackTrace();
			isValidate = false;
			result.set("errorMessage", e.getMessage());
			result.set("exception", e);
		} finally {
			result.set("isValidate", isValidate);
			result.set("unionpayCollection", unionpayCollection).set("collectionTrade", collectionTrade);
		}
		return result;
	}

	/**
	 * @param unionpayCollection
	 * @param collectionTrade
	 */
	@Before({ TradeExceptionInterceptor.class, Tx.class })
	@TxnKey("saveOrder")
	public boolean saveOrder(UnionpayCollection unionpayCollection, CollectionTrade collectionTrade) {
		try {
			return unionpayCollection.save() && collectionTrade.save();
		} catch (Exception e) {
			collectionTrade.setFinalCode("2");
			unionpayCollection.setFinalCode("2");

			TradeRuntimeException xe = new TradeRuntimeException(e);
			xe.setCollectionTrade(collectionTrade);
			xe.setUnionpayCollection(unionpayCollection);
			throw xe;
		}
	}

	@Before({ TradeExceptionInterceptor.class, Tx.class })
	@TxnKey("realtime-sendOrder")
	public boolean sendRealtimeOrder(UnionpayCollection unionpayCollection, CollectionTrade collectionTrade) {
		try {
			unionpayCollection.sendRealtimeOrder();
			return handlingTradeResult(unionpayCollection, collectionTrade);
		} catch (TradeRuntimeException e) {
			throw e;
		} catch (Exception e) {
			TradeRuntimeException xe = new TradeRuntimeException(e);
			xe.setCollectionTrade(collectionTrade);
			xe.setUnionpayCollection(unionpayCollection);
			throw xe;
		}
	}

	/**
	 * 
	 * 应答码规范参考open.unionpay.com帮助中心 下载 产品接口规范 《平台接入接口规范-第5部分-附录》
	 * 
	 * @param rspData
	 * @param acpService
	 * @param encoding
	 * @param unionpayCollection
	 * @param collectionTrade
	 * @return
	 */
	private boolean handlingTradeResult(UnionpayCollection unionpayCollection, CollectionTrade collectionTrade) {
		boolean isSuccess = false;
		try {
			unionpayCollection.validateRealtimeResp();
			Map<String, String> rspData = unionpayCollection.getRealtimeRspData();
			String respCode = rspData.get("respCode");
			String respMsg = rspData.get("respMsg");
			String queryId = rspData.get("queryId");

			unionpayCollection.setRespCode(respCode);
			unionpayCollection.setRespMsg(respMsg);
			unionpayCollection.setQueryId(queryId);

			collectionTrade.setResCode(respCode);
			collectionTrade.setResMsg(respMsg);

			// 00 交易已受理(不代表交易已成功），等待接收后台通知更新订单状态,也可以主动发起 查询交易确定交易状态。
			//// 后续需发起交易状态查询交易确定交易状态
			if (("00").equals(respCode) || ("03").equals(respCode) || ("04").equals(respCode)
					|| ("05").equals(respCode)) {
				// 如果是配置了敏感信息加密，如果需要获取卡号的铭文，可以按以下方法解密卡号
				// String accNo1 = resmap.get("accNo");
				// String accNo2 = AcpService.decryptData(accNo1, "UTF-8");
				// //解密卡号使用的证书是商户签名私钥证书acpsdk.signCert.path
				// LogUtil.writeLog("解密后的卡号："+accNo2);
				isSuccess = true;
			} else {
				isSuccess = false;
				collectionTrade.setFinalCode("2");
				unionpayCollection.setFinalCode("2");
			}

			unionpayCollection.update();
			collectionTrade.update();
			return isSuccess;
		} catch (Exception e) {
			TradeRuntimeException xe = new TradeRuntimeException(e);
			xe.setCollectionTrade(collectionTrade);
			xe.setUnionpayCollection(unionpayCollection);
			throw xe;
		}
	}

	public void updateOrderStatus(Map<String, String> reqParam) {
		String respCode = reqParam.get("respCode");
		String orderId = reqParam.get("orderId");
		try {
			UnionpayCollection unionpayCollection = UnionpayCollection.findByOrderId(orderId);
			if ("00".equals(respCode) || "A6".equals(respCode)) {// 交易成功
				syncOrderStatus(unionpayCollection);
			} else if ("03".equals(respCode) || "04".equals(respCode) || "05".equals(respCode)) {// 订单处理中或交易状态未明
				setResultByCallback(unionpayCollection, reqParam, "1");// 处理中
			} else {// 交易失败
				setResultByCallback(unionpayCollection, reqParam, "2");// 失败
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setResultByCallback(UnionpayCollection unionpayCollection, Map<String, String> reqParam,
			String finalCode) {
		Date now = new Date();
		String respCode = reqParam.get("respCode");
		String respMsg = reqParam.get("respMsg");

		String queryId = reqParam.get("queryId");
		String settleAmt = reqParam.get("settleAmt");
		String settleCurrencyCode = reqParam.get("settleCurrencyCode");
		String settleDate = reqParam.get("settleDate");
		String traceNo = reqParam.get("traceNo");
		String traceTime = reqParam.get("traceTime");
		if (unionpayCollection != null) {
			unionpayCollection.setFinalCode(finalCode);
			unionpayCollection.setResultCode(respCode);
			unionpayCollection.setResultMsg(respMsg);
			unionpayCollection.setResult(JsonKit.toJson(reqParam));
			unionpayCollection.setQueryId(queryId);
			unionpayCollection.setSettleAmt(settleAmt);
			unionpayCollection.setSettleCurrencyCode(settleCurrencyCode);
			unionpayCollection.setSettleDate(settleDate);
			unionpayCollection.setTraceNo(traceNo);
			unionpayCollection.setTraceTime(traceTime);
			unionpayCollection.setMat(now);
			unionpayCollection.update();
		}
		CollectionTrade collectionTrade = CollectionTrade.findByTradeNo(unionpayCollection);
		if (collectionTrade != null) {
			collectionTrade.setFinalCode(finalCode);
			collectionTrade.setResultCode(respCode);
			collectionTrade.setResultMsg(respMsg);
			collectionTrade.setMat(now);
			collectionTrade.update();
		}
	}

	public void syncOrderStatus(UnionpayCollection unionpayCollection) throws Exception {
		if (unionpayCollection != null) {
			try {
				UnionpayCollectionQuery query = new UnionpayCollectionQuery();
				query.setOrderId(unionpayCollection.getOrderId());
				List<UnionpayCollectionQuery> queryHistory = query.findUnionpayCollectionQuery();

				if (CollectionUtils.isEmpty(queryHistory)) {
					queryResult(unionpayCollection);
				} else {
					CollectionTrade collectionTrade = CollectionTrade.findByTradeNo(unionpayCollection);
					UnionpayCollectionQuery lastsQuery = queryHistory.get(0);

					String respCode = lastsQuery.getRespCode();
					String origRespCode = lastsQuery.getOrigRespCode();
					String origRespMsg = lastsQuery.getOrigRespMsg();
					String resp = lastsQuery.getResp();
					String queryId = query.getQueryId();
					String settleAmt = query.getSettleAmt();
					String settleCurrencyCode = query.getSettleCurrencyCode();
					String settleDate = query.getSettleDate();
					String traceNo = query.getTraceNo();
					String traceTime = query.getTraceTime();

					boolean isFail = false;
					boolean origRespCodeIsNotBlank = StringUtils.isNotBlank(origRespCode);
					boolean isUnkonwOrigRespCode = "03".equals(origRespCode) || "04".equals(origRespCode)
							|| "05".equals(origRespCode);// 订单处理中或交易状态未明

					// 查询成功
					if ("00".equals(respCode)) {
						if ("00".equals(origRespCode)) {// 成功
							Date now = new Date();
							unionpayCollection.setFinalCode("0");// 成功
							unionpayCollection.setResultCode(origRespCode);
							unionpayCollection.setResultMsg(origRespMsg);
							unionpayCollection.setResult(resp);
							unionpayCollection.setQueryId(queryId);
							unionpayCollection.setSettleAmt(settleAmt);
							unionpayCollection.setSettleCurrencyCode(settleCurrencyCode);
							unionpayCollection.setSettleDate(settleDate);
							unionpayCollection.setTraceNo(traceNo);
							unionpayCollection.setTraceTime(traceTime);
							unionpayCollection.setMat(now);
							unionpayCollection.update();
							if (collectionTrade != null) {
								collectionTrade.setFinalCode("0");// 成功
								collectionTrade.setResultCode(origRespCode);
								collectionTrade.setResultMsg(origRespMsg);
								collectionTrade.setMat(now);
								collectionTrade.update();
							}
						} else if (origRespCodeIsNotBlank && (!isUnkonwOrigRespCode)) {// 失败
							Date now = new Date();
							unionpayCollection.setFinalCode("2");// 失败
							unionpayCollection.setResultCode(origRespCode);
							unionpayCollection.setResultMsg(origRespMsg);
							unionpayCollection.setResult(resp);
							unionpayCollection.setQueryId(queryId);
							unionpayCollection.setSettleAmt(settleAmt);
							unionpayCollection.setSettleCurrencyCode(settleCurrencyCode);
							unionpayCollection.setSettleDate(settleDate);
							unionpayCollection.setTraceNo(traceNo);
							unionpayCollection.setTraceTime(traceTime);
							unionpayCollection.setMat(now);
							unionpayCollection.update();

							if (collectionTrade != null) {
								collectionTrade.setFinalCode("0");// 失败
								collectionTrade.setResultCode(origRespCode);
								collectionTrade.setResultMsg(origRespMsg);
								collectionTrade.setMat(now);
								collectionTrade.update();
							}
						}
					} else if ("34".equals(respCode)) {// 订单不存在
						isFail = syncInRespCode34(unionpayCollection);
					}

					// 订单处理中或交易状态未明
					if (isUnkonwOrigRespCode || ((!"00".equals(respCode)) && !isFail)) {
						queryResult(unionpayCollection);
					}
				}
			} catch (Exception e) {
				if (unionpayCollection != null) {
					unionpayCollection.update();
				}
				throw e;
			}
		}
	}

	private boolean syncInRespCode34(UnionpayCollection unionpayCollection) {
		boolean isFail = false;
		UnionpayCallbackLog lastsCallbackLog = null;
		try {
			List<UnionpayCallbackLog> callbackLog = UnionpayCallbackLog
					.findCallbackByOrderId(unionpayCollection.getOrderId());
			if (CollectionUtils.isNotEmpty(callbackLog)) {
				lastsCallbackLog = callbackLog.get(0);
				isFail = UnionpayCollection.isFail(lastsCallbackLog.getRespCode());
			} else {
				String respCode = unionpayCollection.getRespCode();
				String resultCode = unionpayCollection.getResultCode();
				isFail = UnionpayCollection.isFail(respCode) || UnionpayCollection.isFail(resultCode);
			}
			Date now = new Date();
			if (isFail) {
				unionpayCollection.setFinalCode("2");// 失败
				if (lastsCallbackLog != null) {
					unionpayCollection.setResultCode(lastsCallbackLog.getRespCode());
					unionpayCollection.setResultMsg(lastsCallbackLog.getRespMsg());
					unionpayCollection.setResult(lastsCallbackLog.getInfo());
				}
				unionpayCollection.setMat(now);
				unionpayCollection.update();

				CollectionTrade collectionTrade = CollectionTrade.findByTradeNo(unionpayCollection);
				if (collectionTrade != null) {
					collectionTrade.setFinalCode("2");// 失败
					if (lastsCallbackLog != null) {
						collectionTrade.setResultCode(lastsCallbackLog.getRespCode());
						collectionTrade.setResultMsg(lastsCallbackLog.getRespMsg());
					}
					collectionTrade.setMat(now);
					collectionTrade.update();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isFail;
	}

	public void queryResult(UnionpayCollection unionpayCollection) throws Exception {
		boolean isSaved = false;
		UnionpayCollectionQuery query = null;
		try {
			query = unionpayCollection.buildQueryResult();
			if (isSaved = query.save()) {
				unionpayCollection.queryResult();
				handlingQueryResult(unionpayCollection);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (query != null) {
				query.setExceInfo(JsonKit.toJson(BaseCollectionRuntimeException.getExceptionInfo(e)));
			}
			throw e;
		} finally {
			if (query != null) {
				if (!isSaved) {
					query.save();
				} else {
					query.update();
				}
			}
		}
	}

	public void handlingQueryResult(UnionpayCollection unionpayCollection) {
		UnionpayCollectionQuery query = unionpayCollection.getQuery();
		query.validateQueryResp();
		query.setFieldFromQueryResp();

		Date now = new Date();
		String respCode = query.getRespCode();
		String origRespCode = query.getOrigRespCode();
		String origRespMsg = query.getOrigRespMsg();
		if (("00").equals(respCode)) {// 如果查询交易成功

			Integer queryCount = unionpayCollection.getQueryResultCount();
			unionpayCollection.setQueryResultCount(queryCount == null ? 1 : queryCount + 1);
			unionpayCollection.setResultCode(origRespCode);
			unionpayCollection.setResultMsg(origRespMsg);
			unionpayCollection.setQueryId(query.getQueryId());
			unionpayCollection.setSettleAmt(query.getSettleAmt());
			unionpayCollection.setSettleCurrencyCode(query.getSettleCurrencyCode());
			unionpayCollection.setSettleDate(query.getSettleDate());
			unionpayCollection.setTraceNo(query.getTraceNo());
			unionpayCollection.setTraceTime(query.getTraceTime());
			unionpayCollection.setResult(query.getResp());
			unionpayCollection.setMat(now);

			CollectionTrade collectionTrade = CollectionTrade.findByTradeNo(unionpayCollection);
			if (collectionTrade != null) {
				collectionTrade.setResultCode(origRespCode);
				collectionTrade.setResultMsg(origRespMsg);
				collectionTrade.setMat(now);
			}

			if (("00").equals(origRespCode)) {
				// 交易成功，更新商户订单状态
				unionpayCollection.setFinalCode("0");// 成功
				unionpayCollection.update();
				if (collectionTrade != null) {
					collectionTrade.setFinalCode("0");// 成功
					collectionTrade.update();
				}
			} else if (("03").equals(origRespCode) || ("04").equals(origRespCode) || ("05").equals(origRespCode)) {
				// 订单处理中或交易状态未明，需稍后发起交易状态查询交易 【如果最终尚未确定交易是否成功请以对账文件为准】
				unionpayCollection.update();
				if (collectionTrade != null) {
					collectionTrade.update();
				}
			} else {
				// 其他应答码为交易失败
				unionpayCollection.setFinalCode("2");// 失败
				unionpayCollection.update();
				if (collectionTrade != null) {
					collectionTrade.setFinalCode("2");// 失败
					collectionTrade.update();
				}
			}
		} else if (("34").equals(respCode)) {
			syncInRespCode34(unionpayCollection);
		} else {
			// 查询交易本身失败，如应答码10/11检查查询报文是否正确
		}
	}

	public void handlingException(Invocation invocation, TradeRuntimeException e) {
		Method method = invocation.getMethod();
		if (!method.isAnnotationPresent(TxnKey.class)) {
			return;
		}

		UnionpayCollection unionpayCollection = e.getUnionpayCollection();
		CollectionTrade collectionTrade = e.getCollectionTrade();

		TxnKey txnKey = method.getAnnotation(TxnKey.class);
		String txnKeyValue = txnKey.value();

		if (txnKeyValue.equals("saveOrder")) {
			try {
				collectionTrade.setFinalCode("2");// 失败
				collectionTrade.save();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				unionpayCollection.setFinalCode("2");// 失败
				unionpayCollection.save();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (txnKeyValue.equals("realtime-sendOrder")) {
			try {
				collectionTrade.update();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				unionpayCollection.setExceInfo(JsonKit.toJson(e.getExceptionInfo()));
				unionpayCollection.update();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

}
