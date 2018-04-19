package com.mybank.pc.collection.model;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.SqlPara;
import com.mybank.pc.collection.model.base.BaseUnionpayBatchCollection;
import com.mybank.pc.kits.unionpay.acp.AcpService;
import com.mybank.pc.kits.unionpay.acp.SDK;
import com.mybank.pc.kits.unionpay.acp.SDKConfig;
import com.mybank.pc.kits.unionpay.acp.SDKConstants;
import com.mybank.pc.kits.unionpay.acp.file.collection.model.BatchCollectionRequest;
import com.mybank.pc.kits.unionpay.acp.file.collection.model.RequestContent;
import com.mybank.pc.kits.unionpay.acp.file.collection.model.RequestHead;

/**
 * Generated by JFinal.
 */
public class UnionpayBatchCollection extends BaseUnionpayBatchCollection<UnionpayBatchCollection> {
	private static final long serialVersionUID = 1L;

	public static final UnionpayBatchCollection dao = new UnionpayBatchCollection().dao();

	private UnionpayBatchCollectionQuery query = null;
	private Map<String, String> batchReqData = null;
	private Map<String, String> batchRspData = null;

	public Map<String, String> assemblyBatchRequest() {
		Map<String, String> contentData = new HashMap<String, String>();

		SDK sdk = SDK.getByMerId(getMerId());
		SDKConfig sdkConfig = sdk.getSdkConfig();
		AcpService acpService = sdk.getAcpService();

		/*** 银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改 ***/
		// 版本号
		contentData.put("version", sdkConfig.getVersion());
		// 字符集编码 可以使用UTF-8,GBK两种方式
		contentData.put("encoding", SDKConstants.UTF_8_ENCODING);
		// 签名方法 目前只支持01-RSA方式证书加密
		contentData.put("signMethod", sdkConfig.getSignMethod());
		// 取值：21 批量交易
		contentData.put("txnType", getTxnType());
		// 填写02：代收
		contentData.put("txnSubType", getTxnSubType());
		// 代收 000501
		contentData.put("bizType", getBizType());
		// 渠道类型
		contentData.put("channelType", getChannelType());

		/*** 商户接入参数 ***/
		// 接入类型，商户接入填0，不需修改（0：直连商户 2：平台商户）
		contentData.put("accessType", getAccessType());
		// 商户号码
		contentData.put("merId", sdk.getMerId());

		/** 与批量文件内容相关的参数 **/
		// 批量交易时填写，当天唯一,0001-9999，商户号+批次号+上交易时间确定一笔交易
		contentData.put("batchNo", getBatchNo());
		// 前8位需与文件中的委托日期保持一致
		contentData.put("txnTime", getTxnTime());
		// 批量交易时填写，填写批量文件中总的交易比数
		contentData.put("totalQty", getTotalQty());
		// 批量交易时填写，填写批量文件中总的交易金额
		contentData.put("totalAmt", getTotalAmt());

		// 使用DEFLATE压缩算法压缩后，Base64编码的方式传输经压缩编码的文件内容，文件中的商户号必须与merId一致
		// 示例文件位置在src/assets下
		contentData.put("fileContent",
				AcpService.enCodeFileContentByString(getRequestFileContent(), SDKConstants.UTF_8_ENCODING));
		contentData.put("backUrl", sdkConfig.getBackUrl());

		setReq(JsonKit.toJson(contentData));
		batchReqData = acpService.sign(contentData, SDKConstants.UTF_8_ENCODING); // 报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。
		return batchReqData;
	}

	public Map<String, String> sendBatchOrder() throws Exception {
		SDK sdk = SDK.getByMerId(getMerId());
		SDKConfig sdkConfig = sdk.getSdkConfig();
		AcpService acpService = sdk.getAcpService();

		// 交易请求url从配置文件读取对应属性文件acp_sdk.properties中的acpsdk.batchTransUrl
		String requestBatchTransUrl = sdkConfig.getBatchTransUrl();
		// 发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;
		// 这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
		if (batchReqData == null) {
			assemblyBatchRequest();
		}
		batchRspData = acpService.post(batchReqData, requestBatchTransUrl, SDKConstants.UTF_8_ENCODING);
		this.setResp(JsonKit.toJson(batchRspData));
		return batchRspData;
	}

	public boolean validateBatchOrderResp() {
		return SDK.validateResp(batchRspData, getMerId(), SDKConstants.UTF_8_ENCODING);
	}

	public UnionpayBatchCollectionQuery buildQueryResult() {
		Date now = new Date();
		query = new UnionpayBatchCollectionQuery();

		query.setTxnType("22");
		query.setTxnSubType("02");
		query.setBizType("000501");
		query.setChannelType("07");
		query.setAccessType("0");
		query.setMerId(getMerId());
		query.setBatchNo(getBatchNo());
		query.setTxnTime(getTxnTime());
		query.setCat(now);
		query.setMat(now);

		query.assemblyBatchQueryRequest();
		return query;
	}

	public Map<String, String> queryResult() throws Exception {
		if (this.query == null) {
			buildQueryResult();
		}
		return this.query.queryResult();
	}

	public static UnionpayBatchCollection buildUnionpayBatchCollection(Date now, String txnTime, String batchNo,
			List<UnionpayCollection> toBeSentOrder) {
		// 银联调用相关参数
		String txnType = "21"; // 交易类型
		String txnSubType = "02"; // 交易子类型
		String channelType = "07";// 渠道类型
		String accessType = "0";// 接入类型，商户接入固定填0，不需修改
		String bizType = "000501";// 业务类型

		// 平台调用相关参数
		SDK sdk = SDK.getSDK(SDK.MER_CODE_BATCH_CH);
		String finalCode = "3";// 最终处理结果：0成功 1处理中 2失败 3待初始化
		String status = "0";// 0 待查询 1 查询中

		UnionpayBatchCollection unionpayBatchCollection = new UnionpayBatchCollection();
		unionpayBatchCollection.setTxnType(txnType);
		unionpayBatchCollection.setTxnSubType(txnSubType);
		unionpayBatchCollection.setBizType(bizType);
		unionpayBatchCollection.setChannelType(channelType);
		unionpayBatchCollection.setAccessType(accessType);
		unionpayBatchCollection.setMerId(sdk.getMerId());
		unionpayBatchCollection.setBatchNo(batchNo);
		unionpayBatchCollection.setTxnTime(txnTime);
		unionpayBatchCollection.setQueryResultCount(0);
		unionpayBatchCollection.setFinalCode(finalCode);
		unionpayBatchCollection.setStatus(status);
		unionpayBatchCollection.setCat(now);
		unionpayBatchCollection.setMat(now);

		if (CollectionUtils.isNotEmpty(toBeSentOrder)) {
			unionpayBatchCollection.claimToBeSentOrder(toBeSentOrder);
		}

		return unionpayBatchCollection;
	}

	public UnionpayBatchCollection claimToBeSentOrder(List<UnionpayCollection> toBeSentOrder) {
		BatchCollectionRequest batchCollectionRequest = buildBatchCollectionRequest(toBeSentOrder);
		RequestHead requestHead = batchCollectionRequest.getHead();

		setTotalQty(requestHead.getTotalNumber());
		setTotalAmt(requestHead.getTotalAmount());
		setRequestFileContent(batchCollectionRequest.getTxtFileContent());
		setQueryResultCount(0);
		return this;
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

	static int[] blankingTime = new int[] { 120, 30, 100, 50, 60, 90 };

	public void setNextAllowQueryDate() {
		Date nextQueryTime = null;
		if ((nextQueryTime = getNextQueryTime()) == null) {
			nextQueryTime = new Date();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(nextQueryTime);

		Integer queryResultCount = getQueryResultCount();
		if (queryResultCount == null || queryResultCount < 1) {
			queryResultCount = 1;
		}
		calendar.add(Calendar.MINUTE, blankingTime[(queryResultCount - 1) % blankingTime.length]);
		setNextQueryTime(calendar.getTime());
	}

	public static int updateNeedQueryBatchCollectionPrepareOne(Kv kv) {
		SqlPara sqlPara = Db.getSqlPara("collection_batch.updateNeedQueryBatchCollectionPrepareOne", kv);
		return Db.update(sqlPara);
	}

	public static int updateNeedQueryBatchCollectionPrepare(Kv kv) {
		SqlPara sqlPara = Db.getSqlPara("collection_batch.updateNeedQueryBatchCollectionPrepare", kv);
		return Db.update(sqlPara);
	}

	public static List<UnionpayBatchCollection> findNeedQueryBatchCollectionBySysQueryId(Kv kv) {
		SqlPara sqlPara = Db.getSqlPara("collection_batch.findNeedQueryBatchCollectionBySysQueryId", kv);
		return UnionpayBatchCollection.dao.find(sqlPara);
	}

	public UnionpayBatchCollectionQuery getQuery() {
		return query;
	}

	public void setQuery(UnionpayBatchCollectionQuery query) {
		this.query = query;
	}

	public Map<String, String> getBatchReqData() {
		return batchReqData;
	}

	public void setBatchReqData(Map<String, String> batchReqData) {
		this.batchReqData = batchReqData;
	}

	public Map<String, String> getBatchRspData() {
		return batchRspData;
	}

	public void setBatchRspData(Map<String, String> batchRspData) {
		this.batchRspData = batchRspData;
	}

}
