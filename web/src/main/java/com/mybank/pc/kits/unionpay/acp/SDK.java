package com.mybank.pc.kits.unionpay.acp;

public class SDK {

	public static SDK REALTIME_SDK;
	public static SDK BATCH_SDK;

	static {
		REALTIME_SDK = new SDK("acp_sdk_945230148160197.properties", "945230148160197");
		BATCH_SDK = new SDK("acp_sdk_945230148160204.properties", "945230148160204");
	}

	private SDKConfig sdkConfig;
	private CertUtil certUtil;
	private SDKUtil sdkUtil;
	private AcpService acpService;
	private String merId;

	private SDK(String propFileName, String merId) {
		this.sdkConfig = new SDKConfig(propFileName);
		this.certUtil = new CertUtil(sdkConfig);
		this.sdkUtil = new SDKUtil(sdkConfig, certUtil);
		this.acpService = new AcpService(certUtil, sdkConfig, sdkUtil);
		this.merId = merId;
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

}
