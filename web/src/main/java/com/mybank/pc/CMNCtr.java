package com.mybank.pc;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.upload.UploadFile;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.mybank.pc.core.CoreController;
import com.mybank.pc.core.CoreException;
import com.mybank.pc.interceptors.AdminIAuthInterceptor;
import com.mybank.pc.kits.AppKit;
import com.mybank.pc.kits.DateKit;
import com.mybank.pc.kits.QiNiuKit;
import com.mybank.pc.kits._StrKit;
import com.qiniu.common.QiniuException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * 简介       通用的公共的controller
 * <p>
 * 项目名称:   [mb-pc]
 * 包:        [com.mb.pc]
 * 类名称:    [CMNCtr]
 * 类描述:    []
 * 创建人:    [于海慧]
 * 创建时间:  [2017/12/7]
 * 修改人:    []
 * 修改时间:  []
 * 修改备注:  []
 * 版本:     [v1.0]
 */
public class CMNCtr extends CoreController {


    /**
     * 图片的格式为base64Str上传图片到图片服务器默认七牛，返回图片显示的url
     */
    public void act00() {
        String base64Str = getPara("b64s");
        String savePath = getPara("sp");

        if (StrUtil.isBlank(base64Str)) {
            renderFailJSON("图片上传失败，上传的图片数据为空");
            return;
        }

        if (StrUtil.isBlank(savePath))
            savePath = "/cmn/pic/";

        String picServerUrl = CacheKit.get(Consts.CACHE_NAMES.paramCache.name(), "qn_url");
        String picName = DateKit.dateToStr(new Date(), DateKit.yyyyMMdd) + "/" + _StrKit.getUUID() + ".jpg";

        String qnRs = null;
        try {
            qnRs = QiNiuKit.put64image(base64Str, savePath + picName);
        } catch (IOException e) {
            LogKit.error("上传base64图片失败:" + e.getMessage());
            throw new CoreException("上传图片到服务器失败>>" + e.getMessage());
        }
        if (qnRs == null) {
            renderFailJSON("图片上传失败");
            return;
        } else {
            if (qnRs.indexOf("200") > -1) {
                renderSuccessJSON("图片上传成功", picServerUrl + savePath + picName);
            } else {
                LogKit.error("base64上传失败:" + qnRs);
                renderFailJSON("图片上传失败", "");
                return;
            }
        }
    }

    /**
     * 七牛图片上传 ，图片以文件形式上传
     */
    public void act01() {
        UploadFile uf = getFile("file");
        File file = uf.getFile();
        String savePath = getPara("sp");
        if (StrUtil.isBlank(savePath))
            savePath = "/cmn/pic/";

        String picServerUrl = CacheKit.get(Consts.CACHE_NAMES.paramCache.name(), "qn_url");
        String picName = DateKit.dateToStr(new Date(), DateKit.yyyyMMdd) + "/" + _StrKit.getUUID() + ".jpg";
        try {
            QiNiuKit.upload(file, savePath + picName);
        } catch (QiniuException e) {
            LogKit.error("七牛上传图片失败>>" + e.getMessage());
            renderFailJSON("图片上传失败");
        }
        renderSuccessJSON("图片上传成功", picServerUrl + savePath + picName);

    }


    /**
     * 下载根据excel 路径 下载excel
     */
    public void act02() {
        String ePath = getPara("ePath");
        File file = FileUtil.file(PathKit.getWebRootPath() + AppKit.getExcelPath() + ePath);
        int index = ePath.lastIndexOf("/");
        String str = ePath.substring(index, ePath.length());
        renderFile(file, str);
    }


    /**
     * 图片上传
     */
    public void act03() {
        UploadFile uf = getFile("file");
        File file = uf.getFile();
        String fileID = CMNSrv.saveFile(file, FileUtil.getType(file));
        if (fileID == null) {
            renderFailJSON("图片上传失败");
        } else {
            renderSuccessJSON("图片上传成功", fileID);
        }


    }

    @com.jfinal.aop.Clear(AdminIAuthInterceptor.class)
    public void act04() {
        String picid = getPara("picid");
        //读取本地图片输入流
        InputStream fis = null;
        OutputStream out = null;

        try {

            CMNSrv.MongoFileVO mvo = CMNSrv.loadFile(picid);


            fis = mvo.getInputStream();
            getResponse().setContentType("image/jpeg");
            out = getResponse().getOutputStream();
            IoUtil.copy(fis, out);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            renderNull();
        }


    }
    @com.jfinal.aop.Clear(AdminIAuthInterceptor.class)
    public void act05(){
        String merNo = getPara("merNo");
        String path =  CacheKit.get(Consts.CACHE_NAMES.paramCache.name(),"siteDomain")+"/mer01/cust?merNo="+merNo;
//        QrCodeRender qrCodeRender  = new  QrCodeRender(path,300,300);
//        qrCodeRender.setContext(getRequest(),getResponse());
//        qrCodeRender.render();
        renderQrCode(path,300,300);
    }
    @com.jfinal.aop.Clear(AdminIAuthInterceptor.class)
    public void act06(){
        File f = new File("/Users/xufei/Desktop/demo.doc");
        Document document = new Document(PageSize.A4);
//        try {
//            RtfWriter2.getInstance(document, new FileOutputStream(f));
//            document.open();
//
//
//            //设置合同头
//
//            Paragraph ph = new Paragraph();
//            Font fo  = new Font();
//
//            Paragraph p = new Paragraph("出口合同",
//                    new Font(Font.NORMAL, 18, Font.BOLDITALIC, new Color(0, 0, 0)) );
//            p.setAlignment(1);
//            document.add(p);
//            ph.setFont(fo);
//
//            // 设置中文字体
//            // BaseFont bfFont =
//            // BaseFont.createFont("STSongStd-Light","UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
//            // Font chinaFont = new Font();
//            /*
//             * 创建有三列的表格
//             */
//            Table table = new Table(4);
//            document.add(new Paragraph("生成表格"));
//            table.setBorderWidth(1);
//            table.setBorderColor(Color.BLACK);
//            table.setPadding(0);
//            table.setSpacing(0);
//
//            /*
//             * 添加表头的元素
//             */
//            Cell cell = new Cell("表头");//单元格
//            cell.setHeader(true);
//            cell.setColspan(3);//设置表格为三列
//            cell.setRowspan(3);//设置表格为三行
//            table.addCell(cell);
//            table.endHeaders();// 表头结束
//
//            // 表格的主体
//            cell = new Cell("Example cell 2");
//            cell.setRowspan(2);//当前单元格占两行,纵向跨度
//            table.addCell(cell);
//            table.addCell("1,1");
//            table.addCell("1,2");
//            table.addCell("1,3");
//            table.addCell("1,4");
//            table.addCell("1,5");
//            table.addCell(new Paragraph("用java生成的表格1"));
//            table.addCell(new Paragraph("用java生成的表格2"));
//            table.addCell(new Paragraph("用java生成的表格3"));
//            table.addCell(new Paragraph("用java生成的表格4"));
//            document.add(new Paragraph("用java生成word文件"));
//            document.add(table);
//            document.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (DocumentException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


    //读取本地图片输入流
        InputStream fis = null;
        OutputStream out = null;

        try {
            fis = IoUtil.toStream(f);


            //设置文件MIME类型
            getResponse().setContentType("application/octet-stream");
            //设置Content-Disposition
            getResponse().setHeader("Content-Disposition", "attachment;filename=123");
            out = getResponse().getOutputStream();
            IoUtil.copy(fis, out);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            renderNull();
        }

    }


}
