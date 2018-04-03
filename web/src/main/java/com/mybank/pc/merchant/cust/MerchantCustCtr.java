package com.mybank.pc.merchant.cust;

import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.mybank.pc.core.CoreController;
import com.mybank.pc.merchant.info.MerchantInfoSrv;
import com.mybank.pc.merchant.model.MerchantCust;
import com.mybank.pc.merchant.model.MerchantInfo;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 商户信息管理
 */

public class MerchantCustCtr extends CoreController {
    private MerchantInfoSrv merchantInfoSrv =enhance(MerchantInfoSrv.class);
    public void list() {
        Page<MerchantCust> page;
        String search = getPara("search");
        String search1 = getPara("search1");

        Kv kv = Kv.create();
        BigInteger i = currUser().getId();
        Boolean isOper=true;

        //获取当前登录用户信息

        MerchantInfo merInfo = merchantInfoSrv.getMerchantInfoByUserID(currUser().getId().intValue());
        if(merInfo!=null){
            search1 =merInfo.getMerchantNo();
            isOper=false;
        }
        kv.set("search",search);
        kv.set("search1",search1);
        SqlPara sqlPara = Db.getSqlPara("merchant.custList", kv);

        page = MerchantCust.dao.paginate(getPN(), getPS(), sqlPara);

        Map map = new HashMap();
        map.put("page",page);
        map.put("isOper",isOper);
        renderJson(map);

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
