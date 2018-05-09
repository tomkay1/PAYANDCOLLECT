package com.mybank.pc.collection.api;

import com.jfinal.aop.Clear;
import com.jfinal.aop.Duang;
import com.jfinal.core.ActionKey;
import com.jfinal.kit.Kv;
import com.mybank.pc.collection.model.UnionpayCollection;
import com.mybank.pc.core.CoreController;
import com.mybank.pc.exception.ValidateCTRException;
import com.mybank.pc.interceptors.AdminIAuthInterceptor;

@Clear(AdminIAuthInterceptor.class)
public class CTradeInterface extends CoreController {

	private CTradeApiSrv cTradeApiSrv = Duang.duang(CTradeApiSrv.class);

	@ActionKey("/coll/api/trade/initiate")
	public void initiate() {
		String merCode = getPara("merCode");
		String merchantID = getPara("merchantID");
		String bussType = getPara("bussType");
		String orderId = getPara("orderId");
		String txnAmt = getPara("txnAmt");
		String txnSubType = getPara("txnSubType");

		String accNo = getPara("accNo");
		String certifTp = getPara("certifTp");
		String certifId = getPara("certifId");
		String customerNm = getPara("customerNm");
		String phoneNo = getPara("phoneNo");
		String cvn2 = getPara("cvn2");
		String expired = getPara("expired");

		Kv reqParam = Kv.by("merCode", merCode).set("merchantID", merchantID).set("bussType", bussType)
				.set("orderId", orderId).set("txnAmt", txnAmt).set("txnSubType", txnSubType).set("accNo", accNo)
				.set("certifTp", certifTp).set("certifId", certifId).set("customerNm", customerNm)
				.set("phoneNo", phoneNo).set("cvn2", cvn2).set("expired", expired);

		Kv resp = null;
		String errorMsg = "";
		try {
			resp = cTradeApiSrv.initiate(reqParam);
			if (!resp.getBoolean("isSuccess")) {
				UnionpayCollection unionpayCollection = (UnionpayCollection) resp.get("unionpayCollection");
				if (unionpayCollection != null) {
					errorMsg = unionpayCollection.getRespMsg();
				}
			}
		} catch (ValidateCTRException ve) {
			resp = Kv.create().set("isSuccess", false).set("unionpayCollection", null);
			errorMsg = "发起交易失败，" + ve.getMessage();
		} catch (Exception e) {
			resp = Kv.create().set("isSuccess", false).set("unionpayCollection", null);
			errorMsg = "发起交易失败，系统内部错误";
		}

		resp.set("errorMsg", errorMsg);

		renderJson(resp);
	}

	@ActionKey("/coll/api/trade/q")
	public void query() {
		String orderId = getPara("orderId");
		if (orderId != null) {
			orderId = orderId.trim();
		}

		Kv resp = Kv.create();
		UnionpayCollection unionpayCollection = null;
		String errorMsg = "";
		try {
			unionpayCollection = UnionpayCollection.findByOrderId(orderId);
			if (unionpayCollection != null) {
				resp.set("isSuccess", true);
			} else {
				resp.set("isSuccess", false);
				errorMsg = "订单数据不存在[" + orderId + "]";
			}
		} catch (Exception e) {
			resp.set("isSuccess", false);
			errorMsg = "查询失败，系统内部错误";
		}
		resp.set("unionpayCollection", unionpayCollection).set("errorMsg", errorMsg);

		renderJson(resp);
	}

}
