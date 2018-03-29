package com.mybank.pc.collection.entrust;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Clear;
import com.jfinal.aop.Duang;
import com.jfinal.core.ActionKey;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;
import com.mybank.pc.Consts;
import com.mybank.pc.collection.model.CollectionEntrust;
import com.mybank.pc.collection.model.UnionpayEntrust;
import com.mybank.pc.core.CoreController;
import com.mybank.pc.core.CoreException;
import com.mybank.pc.interceptors.AdminIAuthInterceptor;
import com.mybank.pc.kits.CookieKit;

@Clear({ AdminIAuthInterceptor.class })
public class CEntrustCtr extends CoreController {

	private CEntrustSrv cEntrustSrv = Duang.duang(CEntrustSrv.class);

	@ActionKey("/coll/entrust/list")
	public void list() {
		Page<CollectionEntrust> page;
		String serach = getPara("search");

		StringBuffer where = new StringBuffer("from collection_entrust where 1=1 ");
		if (StringUtils.isNotBlank(serach)) {
			where.append(
					"AND (instr(customerNm,?)>0 OR instr(certifId,?)>0 OR instr(accNo,?)>0 OR instr(phoneNo,?)>0) ");
			where.append("ORDER BY mat DESC,cat DESC");
			page = CollectionEntrust.dao.paginate(getPN(), getPS(), "select * ", where.toString(), serach, serach,
					serach, serach);
		} else {
			where.append("ORDER BY mat DESC,cat DESC");
			page = CollectionEntrust.dao.paginate(getPN(), getPS(), "select * ", where.toString());
		}

		renderJson(page);
	}

	@ActionKey("/coll/entrust/trade/list")
	public void tradeList() {
		Page<UnionpayEntrust> page;
		String serach = getPara("search");
		String bTime = getPara("bTime");
		String eTime = getPara("eTime");

		// StringBuffer where = new StringBuffer("from unionpay_entrust where
		// 1=1 ");
		// if (StringUtils.isNotBlank(serach)) {
		// where.append(
		// "AND (instr(customerNm,?)>0 OR instr(certifId,?)>0 OR
		// instr(accNo,?)>0 OR instr(phoneNo,?)>0) ");
		// where.append("ORDER BY mat DESC,cat DESC");
		// page = UnionpayEntrust.dao.paginate(getPN(), getPS(), "select * ",
		// where.toString(), serach, serach, serach,
		// serach);
		// } else {
		// where.append("ORDER BY mat DESC,cat DESC");
		// page = UnionpayEntrust.dao.paginate(getPN(), getPS(), "select * ",
		// where.toString());
		// }

		Kv kv = Kv.create();
		if (StringUtils.isNotBlank(serach)) {

		}
		kv.set("serach", serach).set("bTime", bTime).set("eTime", eTime);
		SqlPara sqlPara = Db.getSqlPara("collection_entrust.findUnionpayEntrustPage", kv);
		page = UnionpayEntrust.dao.paginate(getPN(), getPS(), sqlPara);

		renderJson(page);
	}

	@ActionKey("/coll/entrust/establish")
	public void establish() {
		String merCode = getPara("merCode");

		String accNo = getPara("accNo");
		String certifTp = getPara("certifTp");
		String certifId = getPara("certifId");
		String customerNm = getPara("customerNm");
		String phoneNo = getPara("phoneNo");
		String cvn2 = getPara("cvn2");
		String expired = getPara("expired");
		
		try {
			Kv kv = Kv.create();
			kv.set("accNo", accNo).set("certifTp", certifTp).set("certifId", certifId).set("customerNm", customerNm)
					.set("phoneNo", phoneNo).set("cvn2", cvn2 == null ? "" : cvn2)
					.set("expired", expired == null ? "" : expired);

			String userId = CookieKit.get(this, Consts.USER_ACCESS_TOKEN);

			if (merCode.equals("all")) {
				cEntrustSrv.establish(kv.set("merCode", "0"), userId);
				cEntrustSrv.establish(kv.set("merCode", "1"), userId);
			} else {
				cEntrustSrv.establish(kv.set("merCode", merCode), userId);
			}
			renderSuccessJSON("交易成功");
		} catch (Exception e) {
			throw new CoreException("交易失败");
		}
	}
}
