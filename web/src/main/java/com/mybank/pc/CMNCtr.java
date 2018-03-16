package com.yhh.csap;

import com.jfinal.kit.LogKit;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.upload.UploadFile;
import com.qiniu.common.QiniuException;
import com.xiaoleilu.hutool.util.StrUtil;
import com.yhh.csap.core.CoreController;
import com.yhh.csap.core.CoreException;
import com.yhh.csap.kits.DateKit;
import com.yhh.csap.kits.QiNiuKit;
import com.yhh.csap.kits._StrKit;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * 简介       通用的公共的controller
 * <p>
 * 项目名称:   [whbx]
 * 包:        [com.yhh.csap]
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
     *
     * 图片的格式为base64Str上传图片到图片服务器默认七牛，返回图片显示的url
     *
     */
    public void act00(){
        String base64Str=getPara("b64s");
        String savePath=getPara("sp");

        if(StrUtil.isBlank(base64Str)){
            renderFailJSON("图片上传失败，上传的图片数据为空");
            return;
        }

        if(StrUtil.isBlank(savePath))
            savePath="/cmn/pic/";

        String picServerUrl= CacheKit.get(Consts.CACHE_NAMES.paramCache.name(),"qn_url");
        String picName= DateKit.dateToStr(new Date(),DateKit.yyyyMMdd)+"/"+ _StrKit.getUUID()+".jpg";

        String qnRs=null;
        try {
            qnRs= QiNiuKit.put64image(base64Str,savePath+picName);
        } catch (IOException e) {
            LogKit.error("上传base64图片失败:"+e.getMessage());
            throw new CoreException("上传图片到服务器失败>>"+e.getMessage());
        }
        if(qnRs==null) {
            renderFailJSON("图片上传失败");
            return ;
        }else{
            if(qnRs.indexOf("200")>-1){
                renderSuccessJSON("图片上传成功",picServerUrl+savePath+picName);
            }else{
                LogKit.error("base64上传失败:"+qnRs);
                renderFailJSON("图片上传失败", "");
                return ;
            }
        }
    }

    /**
     *
     * 七牛图片上传 ，图片以文件形式上传
     *
     *
     */
    public void act01(){
        UploadFile uf=getFile("file");
        File file=uf.getFile();
        String savePath=getPara("sp");
        if(StrUtil.isBlank(savePath))
            savePath="/cmn/pic/";

        String picServerUrl= CacheKit.get(Consts.CACHE_NAMES.paramCache.name(),"qn_url");
        String picName= DateKit.dateToStr(new Date(),DateKit.yyyyMMdd)+"/"+ _StrKit.getUUID()+".jpg";
        try {
            QiNiuKit.upload(file,savePath+picName);
        } catch (QiniuException e) {
            LogKit.error("七牛上传图片失败>>"+e.getMessage());
            renderFailJSON("图片上传失败");
        }
        renderSuccessJSON("图片上传成功",picServerUrl+savePath+picName);

    }






}
