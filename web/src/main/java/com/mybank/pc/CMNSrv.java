package com.mybank.pc;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import com.jfinal.kit.LogKit;
import com.mybank.pc.admin.model.Ufile;
import com.mybank.pc.kits._StrKit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class CMNSrv {
    private static String  sysPath = "/Users/xufei/Desktop/";
    /**
     * 保存文件服务
     * @param file 需要保存的文件对象
     * @param path 文件路保存的相对路径(结尾需加"/")，格式为：模块名/YYYYMM/
     * @param fileType 文件扩展名
     * @return 文件保存后的唯一标识
     */
    public static String saveFile(File file ,String path, String fileType ){

        String savePath = sysPath + path;
        String fileID = _StrKit.getUUID();
        String picName =  fileID + "."+ FileUtil.getType(file);
        try {
            //检查路径
            if(!FileUtil.exist(savePath)) {
                FileUtil.mkdir(savePath);
            }
            //保存文件到本地
            IoUtil.copy(IoUtil.toStream(file),new FileOutputStream(savePath+picName));
            //保存文件信息
            Ufile ufile= new Ufile();
            ufile.setId(fileID);
            ufile.setPath(path);
            ufile.setType(fileType);
            ufile.setCat(new Date());
            ufile.dao.save();
        } catch (IOException e) {
            LogKit.error("文件保存失败：" + e.getMessage());
        }
      return fileID;
    }

    /**
     * 上传文件读取
     * @param ufileID 文件保存时的唯一标识
     * @return 文件对象
     */
    public static File loadFile(String ufileID){

       Ufile ufile = Ufile.dao.findById(ufileID);

       return  new File(sysPath+ufile.getPath()+"."+ufile.getType());
    }

}
