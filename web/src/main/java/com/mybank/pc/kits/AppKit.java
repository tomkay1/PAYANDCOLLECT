package com.mybank.pc.kits;


import cn.hutool.core.util.StrUtil;
import com.jfinal.plugin.ehcache.CacheKit;
import com.mybank.pc.Consts;

public class AppKit {

    /**
     * 通用的Excel保存的根位置，使用的时候还需要根据自己的业务定义之下的具体的文件夹例如:
     *
     * getExcelPath()+'/aa/20160203/'
     * @return
     */
    public static String getExcelPath(){
        String ret= (String)CacheKit.get(Consts.CACHE_NAMES.paramCache.name(),"ePath");
        if(StrUtil.isBlank(ret))
            ret="/WEB-INF/excelLocation/";

        return ret;
    }

}
