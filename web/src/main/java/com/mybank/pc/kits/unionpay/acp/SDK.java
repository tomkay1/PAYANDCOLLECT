package com.mybank.pc.kits.unionpay.acp;

import org.apache.commons.lang.StringUtils;

public class SDK {

	/** 春城实时商户 945230148160197 */
	public static String MER_CODE_REALTIME_CH = "0";
	/** 春城批量商户 945230148160204 */
	public static String MER_CODE_BATCH_CH = "1";
	/** 银盛实时商户 826410173920016 手续费 4元 */
	public static String MER_CODE_REALTIME_YS_4 = "2";
	/** 银盛实时商户 826410173920015 手续费 2元 */
	public static String MER_CODE_REALTIME_YS_2 = "3";

	private static SDK REALTIME_CH_SDK;
	private static SDK REALTIME_YS_4_SDK;
	private static SDK REALTIME_YS_2_SDK;
	private static SDK BATCH_CH_SDK;

	static {
		REALTIME_CH_SDK = new SDK("acp_sdk_945230148160197.properties", "945230148160197", MER_CODE_REALTIME_CH);
		REALTIME_YS_4_SDK = new SDK("acp_sdk_826410173920016.properties", "826410173920016", MER_CODE_REALTIME_YS_4);
		REALTIME_YS_2_SDK = new SDK("acp_sdk_826410173920015.properties", "826410173920015", MER_CODE_REALTIME_YS_2);
		BATCH_CH_SDK = new SDK("acp_sdk_945230148160204.properties", "945230148160204", MER_CODE_BATCH_CH);
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
			if (MER_CODE_REALTIME_CH.equals(merCode)) {
				return REALTIME_CH_SDK;
			}
			if (MER_CODE_BATCH_CH.equals(merCode)) {
				return BATCH_CH_SDK;
			}
			if (MER_CODE_REALTIME_YS_4.equals(merCode)) {
				return REALTIME_YS_4_SDK;
			}
			if (MER_CODE_REALTIME_YS_2.equals(merCode)) {
				return REALTIME_YS_2_SDK;
			}
		}
		return null;
	}

	public static SDK getByMerId(String merId) {
		if (REALTIME_CH_SDK.getMerId().equals(merId)) {
			return REALTIME_CH_SDK;
		}
		if (BATCH_CH_SDK.getMerId().equals(merId)) {
			return BATCH_CH_SDK;
		}
		if (REALTIME_YS_4_SDK.getMerId().equals(merId)) {
			return REALTIME_YS_4_SDK;
		}
		if (REALTIME_YS_2_SDK.getMerId().equals(merId)) {
			return REALTIME_YS_2_SDK;
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
