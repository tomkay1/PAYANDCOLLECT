package com.mybank.pc.collection.trade;

import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;
import com.mybank.pc.collection.model.CollectionTrade;
import com.mybank.pc.core.CoreController;

public class CTradeCtr extends CoreController {

	@ActionKey("/coll/trade/list")
	public void list() {
		Page<CollectionTrade> page;
		//String serach = getPara("search");
		StringBuffer where = new StringBuffer("from collection_clear");
		page = CollectionTrade.dao.paginate(getPN(), getPS(), "select * ", where.toString());
		renderJson(page);
	}
}
