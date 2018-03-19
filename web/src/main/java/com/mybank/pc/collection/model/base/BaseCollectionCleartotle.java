package com.mybank.pc.collection.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseCollectionCleartotle<M extends BaseCollectionCleartotle<M>> extends Model<M> implements IBean {

	public void setId(java.lang.Integer id) {
		set("id", id);
	}
	
	public java.lang.Integer getId() {
		return getInt("id");
	}

	public void setCleartotleTime(java.util.Date cleartotleTime) {
		set("cleartotleTime", cleartotleTime);
	}
	
	public java.util.Date getCleartotleTime() {
		return get("cleartotleTime");
	}

	public void setTradeCount(java.lang.Integer tradeCount) {
		set("tradeCount", tradeCount);
	}
	
	public java.lang.Integer getTradeCount() {
		return getInt("tradeCount");
	}

	public void setAmountSum(java.math.BigDecimal amountSum) {
		set("amountSum", amountSum);
	}
	
	public java.math.BigDecimal getAmountSum() {
		return get("amountSum");
	}

	public void setAmountFeeSum(java.math.BigDecimal amountFeeSum) {
		set("amountFeeSum", amountFeeSum);
	}
	
	public java.math.BigDecimal getAmountFeeSum() {
		return get("amountFeeSum");
	}

	public void setAccountFee(java.math.BigDecimal accountFee) {
		set("accountFee", accountFee);
	}
	
	public java.math.BigDecimal getAccountFee() {
		return get("accountFee");
	}

	public void setTradeFee(java.math.BigDecimal tradeFee) {
		set("tradeFee", tradeFee);
	}
	
	public java.math.BigDecimal getTradeFee() {
		return get("tradeFee");
	}

	public void setAmountOff(java.math.BigDecimal amountOff) {
		set("amountOff", amountOff);
	}
	
	public java.math.BigDecimal getAmountOff() {
		return get("amountOff");
	}

	public void setChargeOff(java.lang.String chargeOff) {
		set("chargeOff", chargeOff);
	}
	
	public java.lang.String getChargeOff() {
		return getStr("chargeOff");
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
