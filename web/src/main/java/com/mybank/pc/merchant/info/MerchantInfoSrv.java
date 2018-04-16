package com.mybank.pc.merchant.info;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.ehcache.CacheKit;
import com.mybank.pc.merchant.model.MerchantInfo;
import com.mybank.pc.merchant.model.MerchantUser;

import java.util.List;

public class MerchantInfoSrv {
    public synchronized String  getMerchantNo(String merType){
            Integer maxID = Db.queryInt("select max(id) from merchant_info");
            return String.valueOf(Integer.valueOf(merType)*100000+((maxID==null?0:maxID))+1);
    }
    public MerchantInfo getMerchantInfoByUserID(Integer userID){
        MerchantUser merchantUser = MerchantUser.dao.findFirst("select * from merchant_user mu where mu.userID=? ",userID);
        if(merchantUser!=null){
            return MerchantInfo.dao.findFirstByCache("merInfo","getMerchantInfoByUserID_"+userID,"select * from merchant_info mi where mi.ID=?",merchantUser.getMerchantID());
        }else{
            return null;
        }

    }
    public void removeCacheMerchantInfo(Integer merID){
      List<MerchantUser> list=  MerchantUser.dao.find("select * from merchant_user mu where mu.merchantID=? ",merID);
      if(!CollectionUtil.isEmpty(list)){
          for(MerchantUser mu :list){
              CacheKit.remove("merInfo","getMerchantInfoByUserID_"+mu.getUserID());
          }
      }
    }
}
