package com.mybank.pc.advance.trade;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jfinal.aop.Duang;
import com.jfinal.core.ActionKey;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Page;
import com.mybank.pc.Consts;
import com.mybank.pc.advance.model.UnionpayAdvance;
import com.mybank.pc.core.CoreController;
import com.mybank.pc.exception.ValidateCTRException;
import com.mybank.pc.kits.CookieKit;

public class ATradeCtr extends CoreController {

	private ATradeSrv aTradeSrv = Duang.duang(ATradeSrv.class);

	@ActionKey("/advance/trade/list")
	public void list() {
		Page<UnionpayAdvance> page;

		String finalCode = getPara("finalCode");
		String bTime = getPara("bTime");
		String eTime = getPara("eTime");
		String serach = getPara("search");

		Kv kv = Kv.by("search", serach).set("finalCode", finalCode).set("bTime", bTime).set("eTime", eTime);
		page = UnionpayAdvance.findPage(getPN(), getPS(), kv);

		renderJson(JSON.toJSONString(Kv.by("pageInfo", page), SerializerFeature.DisableCircularReferenceDetect));
	}

	@ActionKey("/advance/trade/validate")
	public void tradeValidate() {
		String operID = CookieKit.get(this, Consts.USER_ACCESS_TOKEN);

		String merCode = getPara("merCode");
		String customerNm = getPara("customerNm");
		String certifId = getPara("certifId");
		String accNo = getPara("accNo");
		String txnAmt = getPara("txnAmt");

		Kv kv = Kv.create();
		kv.set("customerNm", customerNm).set("certifId", certifId).set("accNo", accNo).set("txnAmt", txnAmt)
				.set("operID", operID).set("merCode", merCode);

		Kv initiateRequest = null;
		try {
			initiateRequest = aTradeSrv.validateAndBuildInitiateRequest(kv);
		} catch (Exception e) {
			e.printStackTrace();
		}

		renderJson(initiateRequest);
	}

	@ActionKey("/advance/trade/initiate")
	public void initiate() {
		String operID = CookieKit.get(this, Consts.USER_ACCESS_TOKEN);

		String merCode = getPara("merCode");
		String customerNm = getPara("customerNm");
		String certifId = getPara("certifId");
		String accNo = getPara("accNo");
		String txnAmt = getPara("txnAmt");

		Kv kv = Kv.create();
		kv.set("customerNm", customerNm).set("certifId", certifId).set("accNo", accNo).set("txnAmt", txnAmt)
				.set("operID", operID).set("merCode", merCode);

		boolean isSuccess = false;
		String errorMsg = null;
		try {
			Kv result = aTradeSrv.initiate(kv);
			isSuccess = result.getBoolean("isSuccess");
			if (!isSuccess) {
				UnionpayAdvance unionpayAdvance = (UnionpayAdvance) result.get("unionpayAdvance");
				if (unionpayAdvance != null && "2".equals(unionpayAdvance.getFinalCode())) {
					errorMsg = unionpayAdvance.getRespMsg();
				}
			}
		} catch (ValidateCTRException ve) {
			ve.printStackTrace();
			isSuccess = false;
			errorMsg = "发起交易失败，" + ve.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
			isSuccess = false;
			errorMsg = "发起交易失败，系统内部错误";
		}

		if (isSuccess) {
			renderSuccessJSON("发起交易成功");
		} else {
			renderFailJSON(errorMsg);
		}
	}

}
