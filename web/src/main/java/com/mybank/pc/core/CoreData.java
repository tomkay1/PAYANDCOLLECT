package com.yhh.csap.core;

import com.jfinal.plugin.ehcache.CacheKit;
import com.xiaoleilu.hutool.log.StaticLog;
import com.yhh.csap.Consts;
import com.yhh.csap.admin.model.Param;
import com.yhh.csap.admin.model.Taxonomy;

import java.util.List;

/**
 * Created by yuhaihui8913 on 2017/11/16.
 */
public class CoreData {


    public static void loadAllCache(){
        loadParam();
        loadTax();
    }

    public static void loadParam(){
        List<Param> list=Param.dao.find("select * from "+Param.TABLE);

        for(Param p:list){

//            RenderManager.me().getEngine().getEngineConfig().addSharedObject(p.getK(),p.getVal());
            CacheKit.put(Consts.CACHE_NAMES.paramCache.name(),p.getK(),p.getVal());
        }
        StaticLog.info("系统参数加载成功");
    }

    public static void loadTax(){
        CacheKit.removeAll(Consts.CACHE_NAMES.taxonomy.name());
        List<Taxonomy> list =Taxonomy.dao.findAllModule();
        List<Taxonomy> list1=null;
        for (Taxonomy taxonomy:list){
            list1=Taxonomy.dao.findByModuleExcept(taxonomy.getModule());
            CacheKit.put(Consts.CACHE_NAMES.taxonomy.name(),taxonomy.getModule().concat("List"),list1);
            for(Taxonomy taxonomy1:list1){
                CacheKit.put(Consts.CACHE_NAMES.taxonomy.name(),taxonomy1.getId().toString(),taxonomy1);
            }
        }
    }
}
