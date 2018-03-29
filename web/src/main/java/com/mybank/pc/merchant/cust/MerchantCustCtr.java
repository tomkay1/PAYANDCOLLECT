package com.mybank.pc.merchant.cust;

import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.mybank.pc.core.CoreController;
import com.mybank.pc.merchant.model.MerchantCust;

import java.math.BigInteger;
import java.util.Date;


/**
 * 商户信息管理
 */

public class MerchantCustCtr extends CoreController {

    public void list() {
        Page<MerchantCust> page;
        String search = getPara("search");
        String search1 = getPara("search1");

        Kv kv = Kv.create();
        kv.set("search",search).set("search1",search1);
        SqlPara sqlPara = Db.getSqlPara("merchant.custList", kv);

        page = MerchantCust.dao.paginate(getPN(), getPS(), sqlPara);

        renderJson(page);

    }


    @Before({Tx.class})
    public void del() {
        int id = getParaToInt("id");
        MerchantCust merchantCust = MerchantCust.dao.findById(BigInteger.valueOf(id));
        merchantCust.setDat(new Date());
        merchantCust.update();
        renderSuccessJSON("删除商户信息成功。");
    }


}
