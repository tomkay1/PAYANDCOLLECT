package com.mybank.pc.merchant.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseCollectionTrade<M extends BaseCollectionTrade<M>> extends Model<M> implements IBean {

	public void setId(java.lang.Integer id) {
		set("id", id);
	}
	
	public java.lang.Integer getId() {
		return getInt("id");
	}

	public void setTradeNo(java.lang.String tradeNo) {
		set("tradeNo", tradeNo);
	}
	
	public java.lang.String getTradeNo() {
		return getStr("tradeNo");
	}

	public void setTradeTime(java.util.Date tradeTime) {
		set("tradeTime", tradeTime);
	}
	
	public java.util.Date getTradeTime() {
		return get("tradeTime");
	}

	public void setTradeType(java.lang.String tradeType) {
		set("tradeType", tradeType);
	}
	
	public java.lang.String getTradeType() {
		return getStr("tradeType");
	}

	public void setBussType(java.lang.String bussType) {
		set("bussType", bussType);
	}
	
	public java.lang.String getBussType() {
		return getStr("bussType");
	}

	public void setAmount(java.math.BigDecimal amount) {
		set("amount", amount);
	}
	
	public java.math.BigDecimal getAmount() {
		return get("amount");
	}

	public void setCustID(java.lang.Integer custID) {
		set("custID", custID);
	}
	
	public java.lang.Integer getCustID() {
		return getInt("custID");
	}

	public void setCustName(java.lang.String custName) {
		set("custName", custName);
	}
	
	public java.lang.String getCustName() {
		return getStr("custName");
	}

	public void setCardID(java.lang.String cardID) {
		set("cardID", cardID);
	}
	
	public java.lang.String getCardID() {
		return getStr("cardID");
	}

	public void setMobileBank(java.lang.String mobileBank) {
		set("mobileBank", mobileBank);
	}
	
	public java.lang.String getMobileBank() {
		return getStr("mobileBank");
	}

	public void setBankcardNo(java.lang.String bankcardNo) {
		set("bankcardNo", bankcardNo);
	}
	
	public java.lang.String getBankcardNo() {
		return getStr("bankcardNo");
	}

	public void setResCode(java.lang.String resCode) {
		set("resCode", resCode);
	}
	
	public java.lang.String getResCode() {
		return getStr("resCode");
	}

	public void setResMsg(java.lang.String resMsg) {
		set("resMsg", resMsg);
	}
	
	public java.lang.String getResMsg() {
		return getStr("resMsg");
	}

	public void setResultCode(java.lang.String resultCode) {
		set("resultCode", resultCode);
	}
	
	public java.lang.String getResultCode() {
		return getStr("resultCode");
	}

	public void setResultMsg(java.lang.String resultMsg) {
		set("resultMsg", resultMsg);
	}
	
	public java.lang.String getResultMsg() {
		return getStr("resultMsg");
	}

	public void setFinalCode(java.lang.String finalCode) {
		set("finalCode", finalCode);
	}
	
	public java.lang.String getFinalCode() {
		return getStr("finalCode");
	}

	public void setBankFee(java.math.BigDecimal bankFee) {
		set("bankFee", bankFee);
	}
	
	public java.math.BigDecimal getBankFee() {
		return get("bankFee");
	}

	public void setMerFee(java.math.BigDecimal merFee) {
		set("merFee", merFee);
	}
	
	public java.math.BigDecimal getMerFee() {
		return get("merFee");
	}

	public void setClearStatus(java.lang.String clearStatus) {
		set("clearStatus", clearStatus);
	}
	
	public java.lang.String getClearStatus() {
		return getStr("clearStatus");
	}

	public void setCat(java.util.Date cat) {
		set("cat", cat);
	}
	
	public java.util.Date getCat() {
		return get("cat");
	}

	public void setMat(java.util.Date mat) {
		set("mat", mat);
	}
	
	public java.util.Date getMat() {
		return get("mat");
	}

	public void setDat(java.util.Date dat) {
		set("dat", dat);
	}
	
	public java.util.Date getDat() {
		return get("dat");
	}

	public void setOperID(java.lang.String operID) {
		set("operID", operID);
	}
	
	public java.lang.String getOperID() {
		return getStr("operID");
	}

}
