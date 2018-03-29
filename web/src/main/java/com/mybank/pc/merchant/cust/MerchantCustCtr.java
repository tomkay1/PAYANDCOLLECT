package com.mybank.pc.merchant.cust;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.ehcache.CacheKit;
import com.mybank.pc.Consts;
import com.mybank.pc.admin.model.Taxonomy;
import com.mybank.pc.core.CoreController;
import com.mybank.pc.merchant.info.MerchantInfoSrv;
import com.mybank.pc.merchant.info.MerchantInfoValidator;
import com.mybank.pc.merchant.model.MerchantCust;
import com.mybank.pc.merchant.model.MerchantInfo;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 商户信息管理
 */

public class MerchantCustCtr extends CoreController {

    public void list() {
        Page<MerchantCust> page;
        String serach = getPara("search");
        StringBuffer where = new StringBuffer("from merchant_cust mc left join merchant_info mi on mc.merID=mi.id where 1=1 and mc.dat is null  ");
//        if (!isParaBlank("search")) {
//            where.append(" and (instr(mi.merchantNo,?)>0 or instr(mi.merchantName,?)>0 or instr(mi.perName,?)>0) ");
//            where.append(" ORDER BY mi.cat");
//            page = MerchantInfo.dao.paginate(getPN(), getPS(), "select * ", where.toString(), serach, serach, serach);
//        } else {
            where.append(" ORDER BY mc.cat");
            page = MerchantCust.dao.paginate(getPN(), getPS(), "select mi.merchantName  merchantName,mc.* ", where.toString());
//        }

        renderJson(page);

    }



    @Before({Tx.class})
    public void del(){
        int id=getParaToInt("id");
        MerchantCust merchantCust=MerchantCust.dao.findById(BigInteger.valueOf(id));
        merchantCust.setDat(new Date());
        merchantCust.update();
        renderSuccessJSON("删除商户信息成功。");
    }






}
