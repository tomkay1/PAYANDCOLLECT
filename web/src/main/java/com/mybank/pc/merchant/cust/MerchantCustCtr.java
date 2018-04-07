package com.mybank.pc.merchant.cust;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import com.mybank.pc.CMNSrv;
import com.mybank.pc.Consts;
import com.mybank.pc.core.CoreController;
import com.mybank.pc.interceptors.AdminIAuthInterceptor;
import com.mybank.pc.merchant.info.MerchantInfoSrv;
import com.mybank.pc.merchant.model.MerchantCust;
import com.mybank.pc.merchant.model.MerchantInfo;

import java.io.File;
import java.math.BigInteger;
import java.util.*;


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
        MerchantInfo merInfo = getAttr(Consts.CURR_USER_MER);
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

    @com.jfinal.aop.Clear(AdminIAuthInterceptor.class)
    public void cust(){
        render("/WEB-INF/template/www/cust.html");

    }
    @com.jfinal.aop.Clear(AdminIAuthInterceptor.class)
    public void addCust(){
        String resCode ="success";
        String resMsg = "绑定成功";

        try {
            //此处一定优先处理上传文件，后处理其它参数
            UploadFile cardImgZ =  getFile("cardImgZ","",10*1024*1000);
            UploadFile selfImg = getFile("selfImg","",10*1024*1000);
           File cardImgZFile = cardImgZ.getFile();
           File selfImgFile = selfImg.getFile();
            System.out.println("上传文件1大小："+cardImgZFile.length());
            System.out.println("上传文件1大小："+selfImgFile.length());
            String cardImgZID = CMNSrv.saveFile(cardImgZFile, FileUtil.getType(cardImgZFile));
            String selfImgID = CMNSrv.saveFile(selfImgFile, FileUtil.getType(selfImgFile));
            String merNo =getPara("merNo");
            String custName =getPara("custName");
            String cardID =getPara("cardID");
            String mobileBank =getPara("mobileBank");
            String bankcardNo =getPara("bankcardNo");
           MerchantInfo mf=  MerchantInfo.dao.findFirst("select * from merchant_info mi where mi.merchantNo=? and mi.dat is null ",merNo);


           if (mf !=null){
               List<MerchantCust> mcList = MerchantCust.dao.find("select * from merchant_cust mc where mc.dat is null and  mc.merNo=?  and mc.cardID=? and mc.mobileBank=? and mc.bankcardNo=?", merNo,cardID,mobileBank,bankcardNo);
               if(CollectionUtil.isEmpty(mcList)){
                   if(false){
                       //调用四要素验证接口进行绑卡，如果是失败则返回
                   }
                   MerchantCust mc = new MerchantCust();
                   mc.setMerID(mf.getId());
                   mc.setMerNo(merNo);
                   mc.setCustName(custName);
                   mc.setCardID(cardID);
                   mc.setMobileBank(mobileBank);
                   mc.setBankcardNo(bankcardNo);
                   mc.setCardImgZ(cardImgZID);
                   mc.setSelfImg(selfImgID);
                   mc.setCat(new Date());
                   mc.save();
               }else{
                   //同意商户下已经绑定过银行卡
                   resCode="error";
                   resMsg="绑定失败，该客户银行卡已经绑定！";
               }

           }else{
               //商户编号输入错误，商户不存在
               resCode="error";
               resMsg="绑定失败，商户不存在！";
           }

        } catch (Exception e) {
            e.printStackTrace();
            resCode="error";
            resMsg="系统异常，绑定失败！";
        }
        setAttr("resCode",resCode);
        setAttr("resMsg",resMsg);
        render("/WEB-INF/template/www/cust-res.html");

    }
}
