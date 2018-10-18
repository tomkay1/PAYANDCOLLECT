package com.mybank.pc.advance.model.sender;

import java.util.HashMap;
import java.util.Map;

import com.mybank.pc.advance.model.UnionpayAdvanceQuery;
import com.mybank.pc.collection.model.sender.SendProxy;
import com.mybank.pc.collection.model.sender.SendProxy.SenderBuilder;
import com.mybank.pc.kits.unionpay.acp.AcpService;
import com.mybank.pc.kits.unionpay.acp.SDKConfig;
import com.mybank.pc.kits.unionpay.acp.SDKConstants;

public class AdvanceQueryRequestBuilder implements SenderBuilder {

	private UnionpayAdvanceQuery unionpayAdvanceQuery;

	public AdvanceQueryRequestBuilder(UnionpayAdvanceQuery unionpayAdvanceQuery) {
		this.unionpayAdvanceQuery = unionpayAdvanceQuery;
	}

	@Override
	public SendProxy build() {
		SendProxy sendProxy = new SendProxy(unionpayAdvanceQuery.getMerId());
		SDKConfig sdkConfig = sendProxy.getSdkConfig();
		AcpService acpService = sendProxy.getAcpService();

		Map<String, String> contentData = new HashMap<String, String>();
		/*** 银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改 ***/
		// 版本号
		contentData.put("version", unionpayAdvanceQuery.getVersion());
		// 字符集编码 可以使用UTF-8,GBK两种方式
		contentData.put("encoding", unionpayAdvanceQuery.getEncoding());
		// 签名方法 目前只支持01-RSA方式证书加密
		contentData.put("signMethod", sendProxy.getSignMethod());
		// 交易类型
		contentData.put("txnType", unionpayAdvanceQuery.getTxnType());
		// 交易子类型
		contentData.put("txnSubType", unionpayAdvanceQuery.getTxnSubType());
		// 业务类型
		contentData.put("bizType", unionpayAdvanceQuery.getBizType());

		/*** 商户接入参数 ***/
		// 商户号码
		contentData.put("merId", unionpayAdvanceQuery.getMerId());
		// 接入类型，商户接入固定填0，不需修改
		contentData.put("accessType", unionpayAdvanceQuery.getAccessType());

		// 商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则
		contentData.put("orderId", unionpayAdvanceQuery.getOrderId());
		// 订单发送时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
		contentData.put("txnTime", unionpayAdvanceQuery.getTxnTime());
		// 请求方保留域
		contentData.put("reqReserved", unionpayAdvanceQuery.getReqReserved());

		sendProxy.setReqData(acpService.sign(contentData, SDKConstants.UTF_8_ENCODING));
		// 交易请求url从配置文件读取对应属性文件acp_sdk.properties中的acpsdk.backTransUrl
		sendProxy.setReqUrl(sdkConfig.getSingleQueryUrl());

		return sendProxy;
	}

}
