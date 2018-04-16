package com.mybank.pc;

import com.jfinal.plugin.ehcache.CacheKit;
import com.mybank.pc.core.CoreController;

public class IndexCtr extends CoreController{
    public void index(){
        String domain= CacheKit.get(Consts.CACHE_NAMES.paramCache.name(),"siteDomain");
        redirect(domain+"/ad/index.html");
    }
}
