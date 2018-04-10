package com.mybank.pc.kits.unionpay.acp;

import org.apache.commons.lang.StringUtils;

public class SDK {

	/** 实时商户 945230148160197 */
	public static String MER_CODE_REALTIME = "0";
	/** 批量商户 945230148160204 */
	public static String MER_CODE_BATCH = "1";

	private static SDK REALTIME_SDK;
	private static SDK BATCH_SDK;

	static {
		REALTIME_SDK = new SDK("acp_sdk_945230148160197.properties", "945230148160197", MER_CODE_REALTIME);
		BATCH_SDK = new SDK("acp_sdk_945230148160204.properties", "945230148160204", MER_CODE_BATCH);
	}

	private SDKConfig sdkConfig;
	private CertUtil certUtil;
	private SDKUtil sdkUtil;
	private AcpService acpService;
	private String merId;
	private String merCode;

	private SDK(String propFileName, String merId, String merCode) {
		this.sdkConfig = new SDKConfig(propFileName);
		this.certUtil = new CertUtil(sdkConfig);
		this.sdkUtil = new SDKUtil(sdkConfig, certUtil);
		this.acpService = new AcpService(certUtil, sdkConfig, sdkUtil);
		this.merId = merId;
		this.merCode = merCode;
	}

	public static SDK getSDK(String merCode) {
		if (StringUtils.isNotBlank(merCode)) {
			if (MER_CODE_REALTIME.equals(merCode)) {
				return REALTIME_SDK;
			}
			if (MER_CODE_BATCH.equals(merCode)) {
				return BATCH_SDK;
			}
		}
		return null;
	}

	public static SDK getByMerId(String merId) {
		if (REALTIME_SDK.getMerId().equals(merId)) {
			return REALTIME_SDK;
		}
		if (BATCH_SDK.getMerId().equals(merId)) {
			return BATCH_SDK;
		}
		return null;
	}

	public SDKConfig getSdkConfig() {
		return sdkConfig;
	}

	public CertUtil getCertUtil() {
		return certUtil;
	}

	public SDKUtil getSdkUtil() {
		return sdkUtil;
	}

	public AcpService getAcpService() {
		return acpService;
	}

	public String getMerId() {
		return merId;
	}

	public String getMerCode() {
		return merCode;
	}

	public void setMerCode(String merCode) {
		this.merCode = merCode;
	}

}
