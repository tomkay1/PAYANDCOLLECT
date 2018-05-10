package com.mybank.pc.collection.api;

import com.jfinal.aop.Clear;
import com.jfinal.aop.Duang;
import com.jfinal.core.ActionKey;
import com.jfinal.kit.Kv;
import com.jfinal.kit.LogKit;
import com.mybank.pc.collection.entrust.CEntrustSrv;
import com.mybank.pc.core.CoreController;
import com.mybank.pc.exception.EntrustRuntimeException;
import com.mybank.pc.exception.ValidateEERException;
import com.mybank.pc.interceptors.AdminIAuthInterceptor;

@Clear(AdminIAuthInterceptor.class)
public class CEntrustInterface extends CoreController {

	private CEntrustSrv cEntrustSrv = Duang.duang(CEntrustSrv.class);

	@ActionKey("/coll/api/entrust/establish")
	public void establish() {
		String merCode = getPara("merCode");
		String merchantID = getPara("merchantID");

		String accNo = getPara("accNo");
		String certifTp = getPara("certifTp");
		String certifId = getPara("certifId");
		String customerNm = getPara("customerNm");
		String phoneNo = getPara("phoneNo");
		String cvn2 = getPara("cvn2");
		String expired = getPara("expired");

		Kv reqParam = Kv.by("merCode", merCode).set("merchantID", merchantID).set("accNo", accNo)
				.set("certifTp", certifTp).set("certifId", certifId).set("customerNm", customerNm)
				.set("phoneNo", phoneNo).set("cvn2", cvn2).set("expired", expired);

		LogKit.info("req-CEntrustInterface-establish[" + reqParam + "]");
		Kv resp = null;
		String errorMsg = "";
		try {
			resp = cEntrustSrv.establish(reqParam);
		} catch (ValidateEERException e) {
			resp = Kv.create().set("isSuccess", false).set("unionpayEntrust", null);
			errorMsg = e.getMessage();
		} catch (EntrustRuntimeException e) {
			resp = Kv.create().set("isSuccess", false).set("unionpayEntrust", null);
			errorMsg = e.getMessage();
		} catch (Exception e) {
			resp = Kv.create().set("isSuccess", false).set("unionpayEntrust", null);
			errorMsg = "系统异常";
		}
		resp.set("errorMsg", errorMsg);

		LogKit.info("resp-CEntrustInterface-establish[" + resp + "]");
		renderJson(resp);
	}

}
