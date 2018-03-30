package com.mybank.pc.merchant.info;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.ehcache.CacheKit;
import com.mybank.pc.Consts;
import com.mybank.pc.admin.model.Taxonomy;
import com.mybank.pc.core.CoreController;
import com.mybank.pc.merchant.model.MerchantFee;
import com.mybank.pc.merchant.model.MerchantInfo;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 商户信息管理
 */

public class MerchantInfoCtr extends CoreController {
    private MerchantInfoSrv merchantInfoSrv =enhance(MerchantInfoSrv.class);

    public void list() {
        Page<MerchantInfo> page;
        String serach = getPara("search");
        StringBuffer where = new StringBuffer("from merchant_info mi  where 1=1 and mi.dat is null  ");
        if (!isParaBlank("search")) {
            where.append(" and (instr(mi.merchantNo,?)>0 or instr(mi.merchantName,?)>0 or instr(mi.perName,?)>0) ");
            where.append(" ORDER BY mi.cat desc");
            page = MerchantInfo.dao.paginate(getPN(), getPS(), "select * ", where.toString(), serach, serach, serach);
        } else {
            where.append(" ORDER BY mi.cat desc");
            page = MerchantInfo.dao.paginate(getPN(), getPS(), "select * ", where.toString());
        }
        List<Taxonomy> tlist =CacheKit.get(Consts.CACHE_NAMES.taxonomy.name(),"merTypeList");
        Map map = new HashMap();
        map.put("tList" ,tlist);
        map.put("page",page);
        renderJson(map);

    }

    @Before({MerchantInfoValidator.class, Tx.class})
    public void save() {

        MerchantInfo merInfo = getModel(MerchantInfo.class,"",true);

        String merNo = merchantInfoSrv.getMerchantNo(merInfo.getMerchantType());
        merInfo.setMerchantNo(merNo);
        merInfo.setCat(new Date());

        merInfo.setMat(new Date());
        merInfo.setStatus(Consts.STATUS.enable.getVal());
        merInfo.setOperID(String.valueOf(currUser().getId()));
        merInfo.save();
        renderSuccessJSON("新增商户信息成功。", "");
    }

    @Before({MerchantInfoValidator.class, Tx.class})
    public void update() {
        MerchantInfo merInfo = getModel(MerchantInfo.class,"",true);

        merInfo.setMat(new Date());
        merInfo.setOperID(String.valueOf(currUser().getId()));
        merInfo.update();
        renderSuccessJSON("更新商户信息成功。", "");
    }

    @Before({Tx.class})
    public void del(){
        int id=getParaToInt("id");
        MerchantInfo merInfo=MerchantInfo.dao.findById(BigInteger.valueOf(id));
        merInfo.setDat(new Date());
        merInfo.update();
        renderSuccessJSON("删除商户信息成功。");
    }


    /**
     * 用户禁用操作处理
     **/
    @Before(Tx.class)
    public void forbidden() {
        String merInfoId = getPara("id");
        int id = Integer.parseInt(merInfoId);
        MerchantInfo merInfo = new MerchantInfo();
        merInfo.setId(id);
        merInfo.setStatus(Consts.STATUS.forbidden.getVal());
        merInfo.update();


        renderSuccessJSON("禁用操作执行成功。", "");
    }

    /**
     * 恢复操作处理
     **/
    @Before(Tx.class)
    public void enable() {
        String merInfoId = getPara("id");
        int id = Integer.parseInt(merInfoId);
        MerchantInfo merInfo = new MerchantInfo();
        merInfo.setId(id);
        merInfo.setStatus(Consts.STATUS.enable.getVal());
        merInfo.update();

        renderSuccessJSON("恢复操作执行成功。", "");
    }

    public void listFee() {

        String merID = getPara("id");
        StringBuffer tradeType1 = new StringBuffer("select * from merchant_fee mf  where 1=1 and mf.dat is null  ");
        tradeType1.append(" and mf.merID=? and mf.tradeType='1'");


        StringBuffer tradeType2 = new StringBuffer("select * from merchant_fee mf  where 1=1 and mf.dat is null  ");
        tradeType2.append(" and mf.merID=? and mf.tradeType='2'");
        List<MerchantFee> feeListJ = MerchantFee.dao.find(tradeType1.toString(),merID);
        List<MerchantFee> feeListB = MerchantFee.dao.find(tradeType2.toString(),merID);

        Map map = new HashMap();
        map.put("feeListJ",feeListJ);
        map.put("feeListB",feeListB);

        renderJson(map);

    }



}
