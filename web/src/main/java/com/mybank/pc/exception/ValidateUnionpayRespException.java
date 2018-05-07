package com.mybank.pc.exception;

import java.util.Map;

public class ValidateUnionpayRespException extends BaseCollectionRuntimeException {

	private static final long serialVersionUID = 1L;

	private Map<String, String> rspData;

	private String merid;

	public ValidateUnionpayRespException() {
		super();
	}

	public ValidateUnionpayRespException(String message) {
		super(message);
	}

	public ValidateUnionpayRespException(String message, Throwable cause) {
		super(message, cause);
	}

	public ValidateUnionpayRespException(Throwable cause) {
		super(cause);
	}

	protected ValidateUnionpayRespException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public Map<String, String> getRspData() {
		return rspData;
	}

	public void setRspData(Map<String, String> rspData) {
		this.rspData = rspData;
	}

	public String getMerid() {
		return merid;
	}

	public void setMerid(String merid) {
		this.merid = merid;
	}

	/**
	 * 版本号，交易类型、子类，签名方法，签名值等关键域未上送，返回“Invalid request.”<br>
	 * 交易类型和请求地址校验有误，返回“Invalid request URI.”
	 * 
	 * @return
	 */
	public boolean isInvalidRequestOrURI() {
		return rspData != null
				&& (rspData.containsKey("Invalid request.") || rspData.containsKey("Invalid request URI."));
	}

}
