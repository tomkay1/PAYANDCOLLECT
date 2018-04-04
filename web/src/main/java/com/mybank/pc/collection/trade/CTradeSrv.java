package com.mybank.pc.collection.trade;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.aop.Invocation;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Kv;
import com.jfinal.kit.LogKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.mybank.pc.admin.model.CardBin;
import com.mybank.pc.collection.model.CollectionEntrust;
import com.mybank.pc.collection.model.CollectionTrade;
import com.mybank.pc.collection.model.UnionpayCollection;
import com.mybank.pc.exception.TradeRuntimeException;
import com.mybank.pc.exception.TxnKey;
import com.mybank.pc.interceptors.TradeExceptionInterceptor;
import com.mybank.pc.kits.FeeKit;
import com.mybank.pc.kits.unionpay.acp.AcpService;
import com.mybank.pc.kits.unionpay.acp.SDK;
import com.mybank.pc.kits.unionpay.acp.SDKConfig;
import com.mybank.pc.merchant.model.MerchantInfo;

public class CTradeSrv {

	@TxnKey("realtime")
	public boolean realtime(Kv kv, String userId, MerchantInfo merInfo) {
		boolean isSuccess = false;

		UnionpayCollection unionpayCollection = new UnionpayCollection();
		CollectionTrade collectionTrade = new CollectionTrade();
		Map<String, String> reqData = null;
		try {
			reqData = saveOrder(kv, userId, merInfo, unionpayCollection, collectionTrade);
		} catch (TradeRuntimeException e) {
			throw e;
		} catch (Exception e) {
			TradeRuntimeException xe = new TradeRuntimeException(e);
			xe.setCollectionTrade(collectionTrade);
			xe.setUnionpayCollection(unionpayCollection);
			throw xe;
		}

		try {
			isSuccess = sendOrder(reqData, kv, unionpayCollection, collectionTrade);
		} catch (TradeRuntimeException e) {
			throw e;
		} catch (Exception e) {
			TradeRuntimeException xe = new TradeRuntimeException(e);
			xe.setCollectionTrade(collectionTrade);
			xe.setUnionpayCollection(unionpayCollection);
			throw xe;
		}

		return isSuccess;
	}

	/**
	 * @param kv
	 * @param userId
	 * @param unionpayCollection
	 * @param collectionTrade
	 * @return 签名的请求参数
	 */
	@Before({ TradeExceptionInterceptor.class, Tx.class })
	@TxnKey("realtime-saveOrder")
	public Map<String, String> saveOrder(Kv kv, String userId, MerchantInfo merInfo,
			UnionpayCollection unionpayCollection, CollectionTrade collectionTrade) {
		// 银联调用相关参数
		String txnType = "11"; // 交易类型
		String txnSubType = "02"; // 交易子类型
		String currencyCode = "156"; // 交易币种（境内商户一般是156 人民币）
		String channelType = "07";// 渠道类型
		String accessType = "0";// 接入类型，商户接入固定填0，不需修改
		String bizType = "000501";// 业务类型
		String accType = "01";// 账号类型

		// 平台调用相关参数
		String tradeType = "1";// 1代收 2代付
		String bussType = "1";// 1加急 2批量
		try {
			unionpayCollection.setFinalCode("1");
			unionpayCollection.setTxnType(txnType);
			unionpayCollection.setTxnSubType(txnSubType);
			unionpayCollection.setCurrencyCode(currencyCode);
			unionpayCollection.setChannelType(channelType);
			unionpayCollection.setAccessType(accessType);

			String accNo = kv.getStr("accNo");
			String txnAmt = kv.getStr("txnAmt");
			String custID = kv.getStr("custID");
			String merchantID = kv.getStr("merchantID");

			BigDecimal originaltxnAmt = new BigDecimal(txnAmt);
			txnAmt = String.valueOf(originaltxnAmt.multiply(new BigDecimal(100)).longValue());

			unionpayCollection.setAccNo(accNo);
			unionpayCollection.setTxnAmt(txnAmt);

			SDK sdk = SDK.REALTIME_SDK;
			SDKConfig sdkConfig = sdk.getSdkConfig();
			AcpService acpService = sdk.getAcpService();
			String encoding = "UTF-8";
			String merId = sdk.getMerId();

			unionpayCollection.setMerId(merId);

			Date now = new Date();
			String txnTime = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
			String orderId = "T" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(now) + accNo;
			if (orderId.length() > 40) {
				orderId = orderId.substring(0, 40);
			}
			String tradeNo = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(now) + merId + txnType + txnSubType
					+ accNo;

			unionpayCollection.setCat(now);
			unionpayCollection.setMat(now);
			unionpayCollection.setOperID(userId);
			unionpayCollection.setMerchantID(merchantID);
			unionpayCollection.setOrderId(orderId);
			unionpayCollection.setTxnTime(txnTime);
			unionpayCollection.setTradeNo(tradeNo);

			collectionTrade.setFinalCode("1");
			collectionTrade.setCat(now);
			collectionTrade.setMat(now);
			collectionTrade.setOperID(userId);
			collectionTrade.setMerchantID(Integer.valueOf(merchantID));
			collectionTrade.setTradeNo(tradeNo);
			collectionTrade.setTradeTime(now);
			collectionTrade.setTradeType(tradeType);
			collectionTrade.setBussType(bussType);
			collectionTrade.setAmount(originaltxnAmt);
			collectionTrade.setCustID(Integer.valueOf(custID));
			collectionTrade.setClearStatus("1");// 0已清分 1未清分

			CollectionEntrust query = new CollectionEntrust();
			query.setAccNo(accNo);
			query.setMerId(merId);
			CollectionEntrust collectionEntrust = query.findOne();
			if (collectionEntrust == null) {
				throw new RuntimeException("客户委托信息不存在!");
			}

			unionpayCollection.setCustomerNm(collectionEntrust.getCustomerNm());
			unionpayCollection.setCertifTp(collectionEntrust.getCertifTp());
			unionpayCollection.setCertifId(collectionEntrust.getCertifId());
			unionpayCollection.setPhoneNo(collectionEntrust.getPhoneNo());
			unionpayCollection.setCvn2(collectionEntrust.getCvn2());
			unionpayCollection.setExpired(collectionEntrust.getExpired());

			collectionTrade.setCustName(collectionEntrust.getCustomerNm());
			collectionTrade.setCardID(collectionEntrust.getCertifId());
			collectionTrade.setMobileBank(collectionEntrust.getPhoneNo());
			collectionTrade.setBankcardNo(accNo);

			CardBin cardBin = FeeKit.getCardBin(accNo);
			if (cardBin == null || "1".equals(cardBin.getCardType())) {
				throw new RuntimeException("不支持的卡类型!!");
			}
			collectionTrade.setBankFee(FeeKit.getBankFeeByYuan(originaltxnAmt, cardBin, merId));
			if (merInfo != null && "1".equals(merInfo.getMerchantType())) {
				collectionTrade.setMerFee(FeeKit.getMerchantFee(originaltxnAmt, Integer.valueOf(merchantID), "1"));
			} else {
				collectionTrade.setMerFee(new BigDecimal(0));
			}

			Map<String, String> contentData = new HashMap<String, String>();

			/*** 银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改 ***/
			contentData.put("version", sdkConfig.getVersion()); // 版本号
			contentData.put("encoding", encoding); // 字符集编码 可以使用UTF-8,GBK两种方式
			contentData.put("signMethod", sdkConfig.getSignMethod()); // 签名方法
																		// 目前只支持01-RSA方式证书加密
			contentData.put("txnType", txnType);
			contentData.put("txnSubType", txnSubType); // 交易子类型
			contentData.put("bizType", bizType); // 业务类型
			contentData.put("channelType", channelType); // 渠道类型

			/*** 商户接入参数 ***/
			contentData.put("merId", merId); // 商户号码（商户号码777290058110097仅做为测试调通交易使用，该商户号配置了需要对敏感信息加密）测试时请改成自己申请的商户号，【自己注册的测试777开头的商户号不支持代收产品】
			contentData.put("accessType", accessType); // 接入类型，商户接入固定填0，不需修改
			contentData.put("orderId", orderId); // 商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则
			contentData.put("txnTime", txnTime); // 订单发送时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
			contentData.put("currencyCode", currencyCode); // 交易币种（境内商户一般是156
															// 人民币）
			contentData.put("txnAmt", txnAmt); // 交易金额，单位分，不要带小数点
			contentData.put("accType", accType); // 账号类型

			///////// 不对敏感信息加密使用：
			// contentData.put("accNo",accNo); //这里测试的时候使用的是测试卡号，正式环境请使用真实卡号
			////////

			////////// 如果商户号开通了 商户对敏感信息加密的权限那么，需要对
			////////// 卡号accNo，pin和phoneNo，cvn2，expired加密（如果这些上送的话），对敏感信息加密使用：
			contentData.put("encryptCertId", acpService.getEncryptCertId());
			String accNoEnc = acpService.encryptData(accNo, encoding);
			contentData.put("accNo", accNoEnc);
			//////////

			// 后台通知地址（需设置为【外网】能访问 http
			// https均可），支付成功后银联会自动将异步通知报文post到商户上送的该地址，失败的交易银联不会发送后台通知
			// 后台通知参数详见open.unionpay.com帮助中心 下载 产品接口规范 代收产品接口规范 代收交易 商户通知
			// 注意:1.需设置为外网能访问，否则收不到通知 2.http https均可
			// 3.收单后台通知后需要10秒内返回http200或302状态码
			// 4.如果银联通知服务器发送通知后10秒内未收到返回状态码或者应答码非http200，那么银联会间隔一段时间再次发送。总共发送5次，每次的间隔时间为0,1,2,4分钟。
			// 5.后台通知地址如果上送了带有？的参数，例如：http://abc/web?a=b&c=d
			// 在后台通知处理程序验证签名之前需要编写逻辑将这些字段去掉再验签，否则将会验签失败
			contentData.put("backUrl", sdkConfig.getBackUrl());

			// 请求方保留域，
			// 透传字段，查询、通知、对账文件中均会原样出现，如有需要请启用并修改自己希望透传的数据。
			// 出现部分特殊字符时可能影响解析，请按下面建议的方式填写：
			// 1. 如果能确定内容不会出现&={}[]"'等符号时，可以直接填写数据，建议的方法如下。
			// contentData.put("reqReserved", "透传信息1|透传信息2|透传信息3");
			// 2. 内容可能出现&={}[]"'符号时：
			// 1) 如果需要对账文件里能显示，可将字符替换成全角＆＝｛｝【】“‘字符（自己写代码，此处不演示）；
			// 2) 如果对账文件没有显示要求，可做一下base64（如下）。
			// 注意控制数据长度，实际传输的数据长度不能超过1024位。
			// 查询、通知等接口解析时使用new String(Base64.decodeBase64(reqReserved),
			// DemoBase.encoding);解base64后再对数据做后续解析。
			// contentData.put("reqReserved",
			// Base64.encodeBase64String("任意格式的信息都可以".toString().getBytes(DemoBase.encoding)));
			unionpayCollection.setReq(JsonKit.toJson(contentData));
			Map<String, String> reqData = acpService.sign(contentData, encoding);

			collectionTrade.save();

			return reqData;
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
	public boolean sendOrder(Map<String, String> reqData, Kv kv, UnionpayCollection unionpayCollection,
			CollectionTrade collectionTrade) {
		try {
			String merCode = kv.getStr("merCode");

			SDK sdk = SDK.REALTIME_SDK;
			SDKConfig sdkConfig = sdk.getSdkConfig();
			AcpService acpService = sdk.getAcpService();
			String encoding = "UTF-8";

			String requestBackUrl = sdkConfig.getBackRequestUrl(); // 交易请求url从配置文件读取对应属性文件acp_sdk.properties中的acpsdk.backTransUrl

			Map<String, String> rspData = acpService.post(reqData, requestBackUrl, encoding); // 发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
			unionpayCollection.setResp(JsonKit.toJson(rspData));
			return handlingTradeResult(rspData, acpService, encoding, unionpayCollection, collectionTrade);
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
	private boolean handlingTradeResult(Map<String, String> rspData, AcpService acpService, String encoding,
			UnionpayCollection unionpayCollection, CollectionTrade collectionTrade) {
		boolean isSuccess = false;
		try {
			boolean isEmpty = rspData.isEmpty();
			boolean isValidate = acpService.validate(rspData, encoding);

			if (isEmpty) {// 未返回正确的http状态
				LogKit.error("未获取到返回报文或返回http状态码非200");
				throw new RuntimeException("未获取到返回报文或返回http状态码非200");
			}
			if (isValidate) {
				LogKit.info("验证签名成功");
			} else {
				LogKit.error("验证签名失败");
				throw new RuntimeException("验证签名失败");
			}

			String respCode = rspData.get("respCode");
			String respMsg = rspData.get("respMsg");
			String queryId = rspData.get("queryId");

			unionpayCollection.setRespCode(respCode);
			unionpayCollection.setRespMsg(respMsg);
			unionpayCollection.setQueryId(queryId);

			SqlPara sqlPara = Db.getSqlPara("collection_trade.findByTradeNo",
					Kv.create().set("tradeNo", collectionTrade.getTradeNo()));
			CollectionTrade savedCollectionTrade = CollectionTrade.dao.findFirst(sqlPara);
			if (savedCollectionTrade == null) {
				throw new RuntimeException("交易流水信息不存在[" + collectionTrade.getTradeNo() + "] !!");
			}
			collectionTrade = savedCollectionTrade;

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

			unionpayCollection.save();
			collectionTrade.update();
			return isSuccess;
		} catch (Exception e) {
			TradeRuntimeException xe = new TradeRuntimeException(e);
			xe.setCollectionTrade(collectionTrade);
			xe.setUnionpayCollection(unionpayCollection);
			throw xe;
		}
	}

	public void handlingException(Invocation invocation, TradeRuntimeException e) {
		Method method = invocation.getMethod();
		if (method.isAnnotationPresent(TxnKey.class)) {
			UnionpayCollection unionpayCollection = e.getUnionpayCollection();
			CollectionTrade collectionTrade = e.getCollectionTrade();

			TxnKey txnKey = method.getAnnotation(TxnKey.class);
			String txnKeyValue = txnKey.value();

			if (txnKeyValue.equals("realtime-saveOrder")) {
				try {
					collectionTrade.save();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else if (txnKeyValue.equals("realtime-sendOrder")) {
				try {
					String finalCode = collectionTrade.getFinalCode();
					if (StringUtils.isNotBlank(finalCode) && finalCode.equals("2")) {
						collectionTrade.update();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			try {
				unionpayCollection.setExceInfo(JsonKit.toJson(e.getExceptionInfo()));
				unionpayCollection.save();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
	}

	private SDK getSDK(String merCode) {
		SDK sdk = null;
		if (merCode.equals("0")) {
			sdk = SDK.REALTIME_SDK;
		} else if (merCode.equals("1")) {
			sdk = SDK.BATCH_SDK;
		}
		return sdk;
	}

}
