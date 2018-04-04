package com.mybank.pc.collection.trade;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Duang;
import com.jfinal.core.ActionKey;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;
import com.mybank.pc.Consts;
import com.mybank.pc.collection.model.CollectionTrade;
import com.mybank.pc.core.CoreController;
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

	@ActionKey("/coll/trade/initiate")
	public void initiate() {
		MerchantInfo merInfo = getAttr(Consts.CURR_USER_MER);
		String merchantID = merInfo == null ? "" : String.valueOf(merInfo.getId());

		String bussType = getPara("bussType");
		String accNo = getPara("accNo");
		String txnAmt = getPara("txnAmt");
		String custID = getPara("custID");

		if (StringUtils.isBlank(custID)) {
			SqlPara sqlPara = Db.getSqlPara("collection_trade.findCustByBankcardNo",
					Kv.create().set("bankcardNo", accNo));
			MerchantCust cust = MerchantCust.dao.findFirst(sqlPara);
			if (cust != null) {
				custID = String.valueOf(cust.getId());
			}
		}

		Kv kv = Kv.create();
		kv.set("bussType", bussType).set("accNo", accNo).set("txnAmt", txnAmt).set("custID", custID).set("merchantID",
				merchantID);

		String userId = CookieKit.get(this, Consts.USER_ACCESS_TOKEN);

		boolean isSuccess = false;
		try {
			if ("1".equals(bussType)) {
				cCTradeSrvSrv.realtime(kv, userId, merInfo);
			}
		} catch (Exception e) {
			isSuccess = false;
		}

		if (isSuccess) {
			renderSuccessJSON("交易成功");
		} else {
			renderFailJSON("交易失败");
		}

	}

}
