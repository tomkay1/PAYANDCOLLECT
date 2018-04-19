package com.mybank.pc.collection.trade;

import java.util.ArrayList;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jfinal.aop.Duang;
import com.jfinal.core.ActionKey;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;
import com.mybank.pc.Consts;
import com.mybank.pc.collection.model.CollectionTrade;
import com.mybank.pc.core.CoreController;
import com.mybank.pc.exception.ValidateCTRException;
import com.mybank.pc.kits.CookieKit;
import com.mybank.pc.merchant.model.MerchantCust;
import com.mybank.pc.merchant.model.MerchantInfo;

public class CTradeCtr extends CoreController {

	private CTradeSrv cCTradeSrvSrv = Duang.duang(CTradeSrv.class);

	@ActionKey("/coll/trade/list")
	public void list() {
		MerchantInfo merInfo = getAttr(Consts.CURR_USER_MER);
		Page<CollectionTrade> page;

		if (merInfo == null || merInfo.getId() == null) {
			page = new Page<CollectionTrade>(new ArrayList<CollectionTrade>(), getPN(), getPS(), 0, 0);
		} else {

			String finalCode = getPara("finalCode");
			String bTime = getPara("bTime");
			String eTime = getPara("eTime");
			String serach = getPara("search");

			Kv kv = Kv.create();
			kv.set("search", serach).set("finalCode", finalCode).set("bTime", bTime).set("eTime", eTime)
					.set("merchantID", merInfo.getId());

			SqlPara sqlPara = Db.getSqlPara("collection_trade.findTradeListPage", kv);
			page = CollectionTrade.dao.paginate(getPN(), getPS(), sqlPara);
		}
		renderJson(page);
	}

	@ActionKey("/coll/trade/getMerCustPage")
	public void merCustPage() {
		MerchantInfo merInfo = getAttr(Consts.CURR_USER_MER);
		Page<MerchantCust> page;

		String serach = getPara("search");

		Kv kv = Kv.create();
		kv.set("search", serach).set("merchantID", merInfo == null ? "" : merInfo.getId());

		SqlPara sqlPara = Db.getSqlPara("collection_trade.findMerchantCustListPage", kv);
		page = MerchantCust.dao.paginate(getPN(), getPS(), sqlPara);
		renderJson(page);
	}

	@ActionKey("/coll/trade/getMerCust")
	public void merCust() {
		MerchantInfo merInfo = getAttr(Consts.CURR_USER_MER);
		if (merInfo == null || merInfo.getId() == null) {
			renderJson(new ArrayList<MerchantCust>());
		} else {
			String serach = getPara("search");

			Kv kv = Kv.create();
			kv.set("search", serach).set("merchantID", merInfo.getId());

			SqlPara sqlPara = Db.getSqlPara("collection_trade.findMerchantCustListPage", kv);
			renderJson(JSON.toJSONString(MerchantCust.dao.find(sqlPara),
					SerializerFeature.DisableCircularReferenceDetect));
		}
	}

	@ActionKey("/coll/trade/fee")
	public void fee() {
		MerchantInfo merInfo = getAttr(Consts.CURR_USER_MER);
		String operID = CookieKit.get(this, Consts.USER_ACCESS_TOKEN);

		String bussType = getPara("bussType");
		String accNo = getPara("accNo");
		String txnAmt = getPara("txnAmt");
		String custID = getPara("custID");
		String merchantID = merInfo == null ? getPara("merchantID") : String.valueOf(merInfo.getId());

		Kv kv = Kv.create();
		kv.set("bussType", bussType).set("accNo", accNo).set("txnAmt", txnAmt).set("custID", custID)
				.set("merchantID", merchantID).set("operID", operID);

		Kv initiateRequest = null;
		try {
			initiateRequest = cCTradeSrvSrv.validateAndBuildInitiateRequest(kv);
		} catch (Exception e) {
			e.printStackTrace();
		}

		renderJson(initiateRequest);
	}

	@ActionKey("/coll/trade/initiate")
	public void initiate() {
		MerchantInfo merInfo = getAttr(Consts.CURR_USER_MER);
		String operID = CookieKit.get(this, Consts.USER_ACCESS_TOKEN);

		String bussType = getPara("bussType");
		String accNo = getPara("accNo");
		String txnAmt = getPara("txnAmt");
		String custID = getPara("custID");
		String merchantID = merInfo == null ? getPara("merchantID") : String.valueOf(merInfo.getId());

		Kv kv = Kv.create();
		kv.set("bussType", bussType).set("accNo", accNo).set("txnAmt", txnAmt).set("custID", custID)
				.set("merchantID", merchantID).set("operID", operID);

		boolean isSuccess = false;
		String errorMsg = null;
		try {
			isSuccess = cCTradeSrvSrv.initiate(kv);
		} catch (ValidateCTRException ve) {
			isSuccess = false;
			errorMsg = "发起交易失败，" + ve.getMessage();
		} catch (Exception e) {
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
