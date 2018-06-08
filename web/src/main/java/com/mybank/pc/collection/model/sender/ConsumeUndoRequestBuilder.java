package com.mybank.pc.collection.model.sender;

import java.util.HashMap;
import java.util.Map;

import com.mybank.pc.collection.model.UnionpayCollectionConsumeUndo;
import com.mybank.pc.collection.model.sender.SendProxy.SenderBuilder;
import com.mybank.pc.kits.unionpay.acp.AcpService;
import com.mybank.pc.kits.unionpay.acp.SDKConfig;
import com.mybank.pc.kits.unionpay.acp.SDKConstants;

public class ConsumeUndoRequestBuilder implements SenderBuilder {

	private UnionpayCollectionConsumeUndo unionpayCollectionConsumeUndo;

	public ConsumeUndoRequestBuilder(UnionpayCollectionConsumeUndo unionpayCollectionConsumeUndo) {
		this.unionpayCollectionConsumeUndo = unionpayCollectionConsumeUndo;
	}

	@Override
	public SendProxy build() {
		SendProxy sendProxy = new SendProxy(unionpayCollectionConsumeUndo.getMerId());
		SDKConfig sdkConfig = sendProxy.getSdkConfig();
		AcpService acpService = sendProxy.getAcpService();

		Map<String, String> contentData = new HashMap<String, String>();
		/*** 银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改 ***/
		// 版本号
		contentData.put("version", unionpayCollectionConsumeUndo.getVersion());
		// 字符集编码 可以使用UTF-8,GBK两种方式
		contentData.put("encoding", unionpayCollectionConsumeUndo.getEncoding());
		// 签名方法 目前只支持01-RSA方式证书加密
		contentData.put("signMethod", sendProxy.getSignMethod());
		// 交易类型
		contentData.put("txnType", unionpayCollectionConsumeUndo.getTxnType());
		// 交易子类型
		contentData.put("txnSubType", unionpayCollectionConsumeUndo.getTxnSubType());
		// 业务类型
		contentData.put("bizType", unionpayCollectionConsumeUndo.getBizType());
		// 渠道类型
		contentData.put("channelType", unionpayCollectionConsumeUndo.getChannelType());

		/*** 商户接入参数 ***/
		// 商户号码
		contentData.put("merId", unionpayCollectionConsumeUndo.getMerId());
		// 接入类型，商户接入固定填0，不需修改
		contentData.put("accessType", unionpayCollectionConsumeUndo.getAccessType());
		// 商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则
		contentData.put("orderId", unionpayCollectionConsumeUndo.getOrderId());
		// 订单发送时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
		contentData.put("txnTime", unionpayCollectionConsumeUndo.getTxnTime());
		// 撤销金额，消费撤销时必须和原消费金额相同
		contentData.put("txnAmt", unionpayCollectionConsumeUndo.getTxnAmt());
		// 【原始交易流水号】，原消费交易返回的的queryId，可以从消费交易后台通知接口中或者交易状态查询接口中获取
		contentData.put("origQryId", unionpayCollectionConsumeUndo.getOrigQryId());
		// 交易币种（境内商户一般是156人民币）
		contentData.put("currencyCode", unionpayCollectionConsumeUndo.getCurrencyCode());
		// 后台通知地址，后台通知参数详见open.unionpay.com帮助中心 下载 产品接口规范 网关支付产品接口规范 消费撤销交易
		// 商户通知,其他说明同消费交易的商户通知
		contentData.put("backUrl", sdkConfig.getBackUrl());
		// 请求方保留域，透传字段（可以实现商户自定义参数的追踪）本交易的后台通知,对本交易的交易状态查询交易、对账文件中均会原样返回，商户可以按需上传，长度为1-1024个字节。出现&={}[]符号时可能导致查询接口应答报文解析失败，建议尽量只传字母数字并使用|分割，或者可以最外层做一次base64编码(base64编码之后出现的等号不会导致解析失败可以不用管)。
		contentData.put("reqReserved", unionpayCollectionConsumeUndo.getReqReserved());

		sendProxy.setReqData(acpService.sign(contentData, SDKConstants.UTF_8_ENCODING));
		// 交易请求url从配置文件读取对应属性文件acp_sdk.properties中的acpsdk.backTransUrl
		sendProxy.setReqUrl(sdkConfig.getBackRequestUrl());

		return sendProxy;
	}

}
