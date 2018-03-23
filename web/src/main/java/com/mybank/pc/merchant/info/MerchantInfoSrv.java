package com.mybank.pc.merchant.info;

import com.jfinal.plugin.activerecord.Db;

public class MerchantInfoSrv {
    public synchronized String  getMerchantNo(int merType){
            Integer maxID = Db.queryInt("select max(id) from merchant_info");
            return String.valueOf(merType*100000+maxID+1);
    }
}
