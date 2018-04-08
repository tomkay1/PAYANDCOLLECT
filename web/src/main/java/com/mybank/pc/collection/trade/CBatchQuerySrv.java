package com.mybank.pc.collection.trade;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Kv;
import com.jfinal.kit.LogKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.mybank.pc.collection.model.UnionpayBatchCollection;
import com.mybank.pc.collection.model.UnionpayCollection;
import com.mybank.pc.exception.TxnKey;
import com.mybank.pc.kits.unionpay.acp.AcpService;
import com.mybank.pc.kits.unionpay.acp.SDK;
import com.mybank.pc.kits.unionpay.acp.SDKConfig;
import com.mybank.pc.kits.unionpay.acp.file.collection.model.BatchCollectionRequest;
import com.mybank.pc.kits.unionpay.acp.file.collection.model.RequestContent;
import com.mybank.pc.kits.unionpay.acp.file.collection.model.RequestHead;

public class CBatchQuerySrv {

	public void sendOrder() {
		SqlPara sqlPara = Db.getSqlPara("collection_trade.findToBeSentUnionpayCollection");
		List<UnionpayCollection> toBeSentOrder = UnionpayCollection.dao.find(sqlPara);
		if (CollectionUtils.isNotEmpty(toBeSentOrder)) {
			UnionpayBatchCollection unionpayBatchCollection = buildUnionpayBatchCollection(toBeSentOrder);
			Map<String, String> batchReqData = assemblyBatchRequest(unionpayBatchCollection);
			saveBatchOrder(unionpayBatchCollection);
			sendBatchOrder(batchReqData, unionpayBatchCollection, toBeSentOrder);
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
		SDK sdk = SDK.BATCH_SDK;
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

	public Map<String, String> assemblyBatchRequest(UnionpayBatchCollection unionpayBatchCollection) {
		Map<String, String> contentData = new HashMap<String, String>();

		SDK sdk = SDK.BATCH_SDK;
		SDKConfig sdkConfig = sdk.getSdkConfig();
		AcpService acpService = sdk.getAcpService();
		String encoding = "UTF-8";

		/*** 银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改 ***/
		contentData.put("version", sdkConfig.getVersion()); // 版本号
		contentData.put("encoding", encoding); // 字符集编码 可以使用UTF-8,GBK两种方式
		contentData.put("signMethod", sdkConfig.getSignMethod()); // 签名方法
																	// 目前只支持01-RSA方式证书加密
		contentData.put("txnType", unionpayBatchCollection.getTxnType()); // 取值：21
																			// 批量交易
		contentData.put("txnSubType", unionpayBatchCollection.getTxnSubType()); // 填写02：代收
		contentData.put("bizType", unionpayBatchCollection.getBizType()); // 代收
																			// 000501
		contentData.put("channelType", unionpayBatchCollection.getChannelType()); // 渠道类型

		/*** 商户接入参数 ***/
		contentData.put("accessType", unionpayBatchCollection.getAccessType()); // 接入类型，商户接入填0
																				// ，不需修改（0：直连商户
																				// 2：平台商户）
		contentData.put("merId", sdk.getMerId()); // 商户号码，请改成自己申请的商户号，【测试777开通的商户号不支持代收产品】

		/** 与批量文件内容相关的参数 **/
		contentData.put("batchNo", unionpayBatchCollection.getBatchNo()); // 批量交易时填写，当天唯一,0001-9999，商户号+批次号+上交易时间确定一笔交易
		contentData.put("txnTime", unionpayBatchCollection.getTxnTime()); // 前8位需与文件中的委托日期保持一致
		contentData.put("totalQty", unionpayBatchCollection.getTotalQty()); // 批量交易时填写，填写批量文件中总的交易比数
		contentData.put("totalAmt", unionpayBatchCollection.getTotalAmt()); // 批量交易时填写，填写批量文件中总的交易金额

		// 使用DEFLATE压缩算法压缩后，Base64编码的方式传输经压缩编码的文件内容，文件中的商户号必须与merId一致
		// 示例文件位置在src/assets下
		contentData.put("fileContent",
				AcpService.enCodeFileContentByString(unionpayBatchCollection.getRequestFileContent(), encoding));
		contentData.put("backUrl", sdkConfig.getBackUrl());

		Map<String, String> reqData = acpService.sign(contentData, encoding); // 报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
		unionpayBatchCollection.setReq(JsonKit.toJson(contentData));
		return reqData;
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
	public void sendBatchOrder(Map<String, String> batchReqData, UnionpayBatchCollection unionpayBatchCollection,
			List<UnionpayCollection> toBeSentOrder) {
		try {
			SDK sdk = SDK.BATCH_SDK;
			SDKConfig sdkConfig = sdk.getSdkConfig();
			AcpService acpService = sdk.getAcpService();
			String encoding = "UTF-8";

			String requestBatchTransUrl = sdkConfig.getBatchTransUrl(); // 交易请求url从配置文件读取对应属性文件acp_sdk.properties中的acpsdk.batchTransUrl
			Map<String, String> rspData = acpService.post(batchReqData, requestBatchTransUrl, encoding); // 发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过

			unionpayBatchCollection.setResp(JsonKit.toJson(rspData));
			handlingBatchTradeResult(rspData, acpService, encoding, unionpayBatchCollection, toBeSentOrder);
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
	private boolean handlingBatchTradeResult(Map<String, String> rspData, AcpService acpService, String encoding,
			UnionpayBatchCollection unionpayBatchCollection, List<UnionpayCollection> toBeSentOrder) {
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

			Date now = new Date();
			String respCode = rspData.get("respCode");
			String respMsg = rspData.get("respMsg");

			unionpayBatchCollection.setRespCode(respCode);
			unionpayBatchCollection.setRespMsg(respMsg);

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
