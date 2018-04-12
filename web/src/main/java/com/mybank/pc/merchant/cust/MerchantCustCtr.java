package com.mybank.pc.merchant.cust;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.json.JSONNull;
import cn.hutool.json.XML;
import com.jfinal.aop.Before;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Kv;
import com.jfinal.kit.LogKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import com.mybank.pc.CMNSrv;
import com.mybank.pc.Consts;
import com.mybank.pc.collection.entrust.CEntrustSrv;
import com.mybank.pc.collection.model.UnionpayEntrust;
import com.mybank.pc.core.CoreController;
import com.mybank.pc.interceptors.AdminIAuthInterceptor;
import com.mybank.pc.merchant.info.MerchantInfoSrv;
import com.mybank.pc.merchant.model.MerchantCust;
import com.mybank.pc.merchant.model.MerchantInfo;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathConstants;
import java.io.File;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 商户信息管理
 */

public class MerchantCustCtr extends CoreController {
    private MerchantInfoSrv merchantInfoSrv = enhance(MerchantInfoSrv.class);
    private CEntrustSrv cEntrustSrv = enhance(CEntrustSrv.class);

    public void list() {
        Page<MerchantCust> page;
        String search = getPara("search");
        String search1 = getPara("search1");

        Kv kv = Kv.create();
        BigInteger i = currUser().getId();
        Boolean isOper = true;

        //获取当前登录用户信息
        MerchantInfo merInfo = getAttr(Consts.CURR_USER_MER);
        if (merInfo != null) {
            search1 = merInfo.getMerchantNo();
            isOper = false;
        }
        kv.set("search", search);
        kv.set("search1", search1);
        SqlPara sqlPara = Db.getSqlPara("merchant.custList", kv);

        page = MerchantCust.dao.paginate(getPN(), getPS(), sqlPara);

        Map map = new HashMap();
        map.put("page", page);
        map.put("isOper", isOper);
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
    public void cust() {
        String merNo = getPara("merNo");
      MerchantInfo merchantInfo =  MerchantInfo.dao.findFirst("select * from merchant_info mi where merchantNo=?",merNo);
        String url =  "/WEB-INF/template/www/cust.html";
      if (ObjectUtil.isNull(merchantInfo)){
            String resCode = "error";
            String resMsg = "商户不存在,请与商户客户核对！";
            setAttr("resCode", resCode);
            setAttr("resMsg", resMsg);
          url="/WEB-INF/template/www/cust-res.html";
        }else{
          setAttr("merchantInfo" ,merchantInfo);
      }

        render(url);

    }
    @com.jfinal.aop.Clear(AdminIAuthInterceptor.class)
    public void custAgree() {
        String merNo = getPara("merNo");
        MerchantInfo merchantInfo =  MerchantInfo.dao.findFirst("select * from merchant_info mi where merchantNo=?",merNo);
        String url =  "/WEB-INF/template/www/cust-agree.html";
        if (ObjectUtil.isNull(merchantInfo)){
            String resCode = "error";
            String resMsg = "商户不存在,请与商户客户核对！";
            setAttr("resCode", resCode);
            setAttr("resMsg", resMsg);
            url="/WEB-INF/template/www/cust-res.html";
        }else{
            setAttr("merchantInfo" ,merchantInfo);
        }

        render(url);

    }

    @com.jfinal.aop.Clear(AdminIAuthInterceptor.class)
    public void addCust() {
        String resCode = "success";
        String resMsg = "绑定成功";

        try {
            //此处一定优先处理上传文件，后处理其它参数
            UploadFile cardImgZ = getFile("cardImgZ", "", 10 * 1024 * 1000);
            UploadFile selfImg = getFile("selfImg", "", 10 * 1024 * 1000);
            File cardImgZFile = cardImgZ.getFile();
            File selfImgFile = selfImg.getFile();
            System.out.println("上传文件1大小：" + cardImgZFile.length());
            System.out.println("上传文件2大小：" + selfImgFile.length());
            String cardImgZID = CMNSrv.saveFile(cardImgZFile, FileUtil.getType(cardImgZFile));
            String selfImgID = CMNSrv.saveFile(selfImgFile, FileUtil.getType(selfImgFile));
            String merNo = getPara("merNo");
            String custName = getPara("custName");
            String cardID = getPara("cardID");
            String mobileBank = getPara("mobileBank");
            String bankcardNo = getPara("bankcardNo");
            String is = getPara("is");
            String code = getPara("code");
            MerchantInfo mf = MerchantInfo.dao.findFirst("select * from merchant_info mi where mi.merchantNo=? and mi.dat is null ", merNo);


            if (mf != null) {
                List<MerchantCust> mcList = MerchantCust.dao.find("select * from merchant_cust mc where mc.dat is null and  mc.merNo=?  and mc.cardID=? and mc.mobileBank=? and mc.bankcardNo=?", merNo, cardID, mobileBank, bankcardNo);
                if (CollectionUtil.isEmpty(mcList)) {
                    Kv kv = Kv.create();
                    kv.set("customerNm",custName);
                    kv.set("certifTp","01");
                    kv.set("certifId",cardID);
                    kv.set("phoneNo",mobileBank);
                    kv.set("accNo",bankcardNo);
                    kv.set("merchantID",mf.getId());
                    kv.set("cvn2","");
                    kv.set("expired","");
                    Kv[] resKv = cEntrustSrv.establishAll(kv);

                    if (resKv[0].getBoolean("isSuccess") ) {
                        if (resKv[1].getBoolean("isSuccess") ) {
                            //调用四要素验证接口进行绑卡，如果成功则记录
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
                            resCode = "error";
                            if(ObjectUtil.isNotNull(resKv[1].get("unionpayEntrust"))){
                                resMsg = ((UnionpayEntrust)resKv[1].get("unionpayEntrust")).getRespMsg();
                                if (ObjectUtil.isNull(resMsg)){
                                    resMsg ="远程调用失败（系统内部异常）！";
                                }
                            }else{
                                resMsg ="远程调用异常！";
                            }
                        }
                    }else{
                        resCode = "error";
                        if(ObjectUtil.isNotNull(resKv[0].get("unionpayEntrust"))){
                            resMsg = ((UnionpayEntrust)resKv[0].get("unionpayEntrust")).getRespMsg();
                            if (ObjectUtil.isNull(resMsg)){
                                resMsg ="远程调用失败（系统内部异常）！";
                            }
                        }else{
                            resMsg ="远程调用异常！";
                        }
                    }

                } else {
                    //同意商户下已经绑定过银行卡
                    resCode = "error";
                    resMsg = "该客户银行卡已经绑定！";
                }

            } else {
                //商户编号输入错误，商户不存在
                resCode = "error";
                resMsg = "商户不存在！";
            }

        } catch (Exception e) {
            e.printStackTrace();
            resCode = "error";
            resMsg = "系统异常，绑定失败！";
        }
        setAttr("resCode", resCode);
        setAttr("resMsg", resMsg);
        render("/WEB-INF/template/www/cust-res.html");

    }

    //客户绑定银行卡发送短信验证码
    @com.jfinal.aop.Clear(AdminIAuthInterceptor.class)
    public void sendCode(){
        String flag = "0";
        String url ="http://dx1.xitx.cn:8888/sms.aspx";
        String account= "a10165";
        String password = "154986";
        String id = "5409";
        String mobile = "13897939740";
        String rand =  RandomUtil.randomNumbers(6);
        String content= "【MyBank】验证码为："+rand+"， 请输入此验证码完成绑卡。";
        Map<String,String> map = new HashMap<>();
        map.put("action","send");
        map.put("userid",id);
        map.put("account",account);
        map.put("password",password);
        map.put("mobile",mobile);
        map.put("content",content);
        String resMsg ;
        String message;
        try {
            LogKit.info("发送短信"+mobile+"内容："+content);
            resMsg = HttpKit.post(url, map,"");
            LogKit.info("返回信息："+resMsg);
            Document docResult= XmlUtil.readXML(resMsg);
            message = XmlUtil.getByXPath("//returnsms/message", docResult, XPathConstants.STRING).toString();
            if(!"ok".equals(message)){
                //短信发送失败
                flag="1";
            }
        }catch (Exception e){
            e.printStackTrace();
            //短信发送失败
            flag="1";
        }





        renderJson(flag);
    }
}
