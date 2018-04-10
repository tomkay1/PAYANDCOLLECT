package com.mybank.pc.merchant.cust;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.SqlPara;
import com.mybank.pc.merchant.model.MerchantCust;

import java.util.Date;
import java.util.List;

public class MerchantCustSrv {
    public int totalLastCust(Integer merID){

        Kv kv = Kv.create();

        kv.set("merID", merID);
        kv.set("dayDate", DateUtil.offsetDay(new Date(),-1).toString("yyyyMMdd"));
        SqlPara sqlPara = Db.getSqlPara("merchant.totalDayCust", kv);

        List<MerchantCust> list= MerchantCust.dao.find(sqlPara);

        if(CollectionUtil.isNotEmpty(list)){
            return list.size();
        }else{
            return 0;
        }
    }
}
