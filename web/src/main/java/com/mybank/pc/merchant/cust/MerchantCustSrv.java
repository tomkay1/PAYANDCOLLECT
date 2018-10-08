package com.mybank.pc.merchant.cust;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.XmlUtil;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.Kv;
import com.jfinal.kit.LogKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.SqlPara;
import com.mybank.pc.merchant.model.MerchantCust;
import com.mybank.pc.merchant.model.MerchantInfo;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathConstants;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MerchantCustSrv {
    public int totalLastCust(Integer merID){

        Kv kv = Kv.create();

        kv.set("merID", merID);
        kv.set("dayDate", new DateTime().toString("yyyyMMdd"));
        SqlPara sqlPara = Db.getSqlPara("merchant.totalDayCust", kv);

        List<MerchantCust> list= MerchantCust.dao.find(sqlPara);

        if(CollectionUtil.isNotEmpty(list)){
            return list.size();
        }else{
            return 0;
        }
    }

    /**
     * 增加客户服务，接口方式新增客户使用
     * @param merchantCust（必输字段，商户ID，商户编号、姓名、身份证号、手机号、卡号）
     * @return 0：成功 1：商户不存在 2：银行卡已经绑定
     */
    public int addCust(MerchantCust merchantCust){

        MerchantInfo mf = MerchantInfo.dao.findFirst("select * from merchant_info mi where mi.merchantNo=? and mi.dat is null ", merchantCust.getMerNo());

        if (ObjectUtil.isNull(mf)) {
            LogKit.info("服务新增客户：商户不存在");
            return 1;
        }


        List<MerchantCust> mcList = MerchantCust.dao.find("select * from merchant_cust mc where mc.dat is null and  mc.merNo=?  and mc.cardID=? and mc.mobileBank=? and mc.bankcardNo=?", merchantCust.getMerNo(), merchantCust.getCardID(), merchantCust.getMobileBank(), merchantCust.getBankcardNo());
        if (!CollectionUtil.isEmpty(mcList)) {
            //同一商户下已经绑定过银行卡
            LogKit.info("服务新增客户：该客户银行卡已经绑定");
            return 2;
        }

        merchantCust.setCat(new Date());
        merchantCust.save();
        return 0;
    }
    public static void main(String[] args) {

        String url = "http://dx1.xitx.cn:8888/sms.aspx";

        String account = "a10165";
        String password = "154986";
        String id = "5409";
        String content = "验证码为：%s,请输入并完成银行卡绑定，10分钟后失效！【MyBank】";
        String rand = RandomUtil.randomNumbers(6);
        content = String.format(content, rand);
        String mobile = "15304105297";

        Map<String, String> map = new HashMap<>();
        map.put("action", "send");
        map.put("userid", id);
        map.put("account", account);
        map.put("password", password);
        map.put("mobile", mobile);
        map.put("content", content);
        String res;
        String message;
        try {
            LogKit.info("发送短信：" + mobile + " 内容：" + content);
            res = HttpKit.post(url, map, "");
            LogKit.info("返回信息：" + res);
            Document doc = XmlUtil.readXML(res);
            message = XmlUtil.getByXPath("//returnsms/message", doc, XPathConstants.STRING).toString();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
