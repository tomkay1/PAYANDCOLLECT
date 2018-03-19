package com.mybank.pc.merchant.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseMerchantInfo<M extends BaseMerchantInfo<M>> extends Model<M> implements IBean {

	public void setId(java.lang.Integer id) {
		set("id", id);
	}
	
	public java.lang.Integer getId() {
		return getInt("id");
	}

	public void setMerchantNo(java.lang.String merchantNo) {
		set("merchantNo", merchantNo);
	}
	
	public java.lang.String getMerchantNo() {
		return getStr("merchantNo");
	}

	public void setMerchantName(java.lang.String merchantName) {
		set("merchantName", merchantName);
	}
	
	public java.lang.String getMerchantName() {
		return getStr("merchantName");
	}

	public void setMerchantType(java.lang.Integer merchantType) {
		set("merchantType", merchantType);
	}
	
	public java.lang.Integer getMerchantType() {
		return getInt("merchantType");
	}

	public void setPerName(java.lang.String perName) {
		set("perName", perName);
	}
	
	public java.lang.String getPerName() {
		return getStr("perName");
	}

	public void setCardID(java.lang.String cardID) {
		set("cardID", cardID);
	}
	
	public java.lang.String getCardID() {
		return getStr("cardID");
	}

	public void setMobile(java.lang.String mobile) {
		set("mobile", mobile);
	}
	
	public java.lang.String getMobile() {
		return getStr("mobile");
	}

	public void setEmail(java.lang.String email) {
		set("email", email);
	}
	
	public java.lang.String getEmail() {
		return getStr("email");
	}

	public void setAddress(java.lang.String address) {
		set("address", address);
	}
	
	public java.lang.String getAddress() {
		return getStr("address");
	}

	public void setMobile1(java.lang.String mobile1) {
		set("mobile1", mobile1);
	}
	
	public java.lang.String getMobile1() {
		return getStr("mobile1");
	}

	public void setMobile2(java.lang.String mobile2) {
		set("mobile2", mobile2);
	}
	
	public java.lang.String getMobile2() {
		return getStr("mobile2");
	}

	public void setFeeAmount(java.math.BigDecimal feeAmount) {
		set("feeAmount", feeAmount);
	}
	
	public java.math.BigDecimal getFeeAmount() {
		return get("feeAmount");
	}

	public void setCardImg(java.lang.String cardImg) {
		set("cardImg", cardImg);
	}
	
	public java.lang.String getCardImg() {
		return getStr("cardImg");
	}

	public void setCardZ(java.lang.String cardZ) {
		set("cardZ", cardZ);
	}
	
	public java.lang.String getCardZ() {
		return getStr("cardZ");
	}

	public void setCardF(java.lang.String cardF) {
		set("cardF", cardF);
	}
	
	public java.lang.String getCardF() {
		return getStr("cardF");
	}

	public void setStatus(java.lang.String status) {
		set("status", status);
	}
	
	public java.lang.String getStatus() {
		return getStr("status");
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
