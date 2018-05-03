package com.mybank.pc.merchant.info;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.ehcache.CacheKit;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.rtf.RtfWriter2;
import com.mybank.pc.merchant.model.MerchantInfo;
import com.mybank.pc.merchant.model.MerchantUser;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public void createAgreeDoc(MerchantInfo mi ,File docFile){
        Document document = new Document(PageSize.A4);
        try {
            RtfWriter2.getInstance(document, new FileOutputStream(docFile));
            document.open();


            //设置合同头


            Paragraph p = new Paragraph("委托代扣授权书", new Font(Font.NORMAL, 18, Font.BOLD) );
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);

            document.add(new Paragraph("为保护您的权益，以下内容请务必填写完整。"));
            // 设置中文字体
            // BaseFont bfFont =
            // BaseFont.createFont("STSongStd-Light","UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            // Font chinaFont = new Font();
            /*
             * 创建有三列的表格
             */
            Table table = new Table(4);
            table.setWidth(100);
            int width[] = {25,25,20,30};//设置每列宽度比例
            table.setWidths(width);
            table.setBorderWidth(1);
            table.setBorderColor(Color.BLACK);
            table.setPadding(20);
            table.setSpacing(0);


            Cell cell = new Cell("授权人");//单元格
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new Cell("");
            cell.setColspan(3);//设置表格为三列
            table.addCell(cell);

            cell = new Cell("被授权单位");
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new Cell("");
            cell.setColspan(3);
            table.addCell(cell);

            cell = new Cell("被授权业务描述");
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new Cell("");
            cell.setColspan(3);
            table.addCell(cell);

            cell = new Cell("授权人联系电话");
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new Cell("");
            cell.setColspan(3);
            table.addCell(cell);

            cell = new Cell("授权的银行账号");
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new Cell("");
            cell.setColspan(3);
            table.addCell(cell);

            cell = new Cell("开户银行");
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new Cell("");
            table.addCell(cell);

            cell = new Cell("开户行所在地");
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new Cell("");
            table.addCell(cell);


            cell = new Cell("授权人身份证号码");
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new Cell("");
            cell.setColspan(3);
            table.addCell(cell);

            cell = new Cell("授权人声明");
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new Cell(new Paragraph("声明"));

            cell.setColspan(3);
            table.addCell(cell);


            cell = new Cell("授权人签名");
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new Cell("");
            table.addCell(cell);

            cell = new Cell("日期");

            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new Cell("    年    月    日");
            table.addCell(cell);

            cell = new Cell("备注");
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new Cell(new Paragraph("声明"));

            cell.setColspan(3);
            table.addCell(cell);

            document.add(table);
            document.close();
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }
}
