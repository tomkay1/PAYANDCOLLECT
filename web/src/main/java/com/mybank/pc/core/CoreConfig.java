package com.yhh.csap.core;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;
import com.jfinal.config.*;
import com.jfinal.core.JFinal;
import com.jfinal.json.FastJsonFactory;
import com.jfinal.kit.PathKit;
import com.jfinal.log.Log4jLogFactory;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.template.Engine;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import com.yhh.csap.CMNCtr;
import com.yhh.csap.admin.LoginCtr;
import com.yhh.csap.admin.res.ResCtr;
import com.yhh.csap.admin.art.ArtCtr;
import com.yhh.csap.admin.param.ParamCtr;
import com.yhh.csap.admin.role.RoleCtr;
import com.yhh.csap.admin.taxonomy.TaxCtr;
import com.yhh.csap.admin.user.UserCtr;
import com.yhh.csap.interceptors.AdminIAuthInterceptor;
import com.yhh.csap.interceptors.ExceptionInterceptor;
import com.yhh.csap.interceptors.WwwInterceptor;
import com.yhh.csap.kits.DateKit;
import com.yhh.csap.kits.ResKit;
import com.yhh.csap.www.IndexCtr;

/**
 * Created by yuhaihui8913 on 2017/11/14.
 */
public class CoreConfig extends JFinalConfig{
    @Override
    public void configConstant(Constants constants) {
        constants.setDevMode(ResKit.getConfigBoolean("devMode"));
        //constants.setMainRenderFactory(new BeetlRenderFactory());
        constants.setError500View("/WEB-INF/template/common/500.html");
        constants.setError404View("/WEB-INF/template/common/404.html");
        constants.setError403View("/WEB-INF/template/common/403.html");
        constants.setError401View("/WEB-INF/template/common/401.html");
        constants.setEncoding("UTF-8");
        constants.setJsonFactory(new FastJsonFactory());
        constants.setLogFactory(new Log4jLogFactory());
        constants.setJsonDatePattern(DateKit.STR_DATEFORMATE);

    }

    @Override
    public void configRoute(Routes routes) {
        routes.add(new Routes() {
            @Override
            public void config() {
//                addInterceptor(new AdminAAuthInterceptor());
                add("/ad00", ParamCtr.class);
                add("/ad01", UserCtr.class);
                add("/ad02", RoleCtr.class);
                add("/ad03", ResCtr.class);
                add("/ad04", ArtCtr.class);
                add("/ad05", TaxCtr.class);
                add("/ad06", LoginCtr.class);

            }
        });



        routes.add(new Routes() {
            @Override
            public void config() {
                add("/cmn", CMNCtr.class);
            }
        });


        routes.add(new Routes() {
            @Override
            public void config() {

            }
        });

        routes.add(new Routes() {
            @Override
            public void config() {
                setBaseViewPath("/WEB-INF/template/www/");
                addInterceptor(new WwwInterceptor());
                add("/", IndexCtr.class);

            }
        });

    }

    @Override
    public void configEngine(Engine engine) {
        engine.addSharedObject("ctx", JFinal.me().getContextPath());
        engine.addSharedMethod( new StrUtil());
        engine.addSharedObject("cKit",new CollectionUtil());
        engine.setDevMode(ResKit.getConfigBoolean("devMode", true));
        //使用JF模板渲染通用页面
//        engine.addSharedFunction("/WEB-INF/template/www/css.html");
//        engine.addSharedFunction("/WEB-INF/template/www/js.html");
//        engine.addSharedFunction("/WEB-INF/template/admin/_layout.html");
        engine.addSharedFunction("/WEB-INF/template/www/_layout.html");
    }

    @Override
    public void configPlugin(Plugins plugins) {
        //开启druid数据库连接池
        DruidPlugin druidPlugin = createDruidPlugin();
        // StatFilter提供JDBC层的统计信息
        druidPlugin.addFilter(new StatFilter());
        // WallFilter的功能是防御SQL注入攻击
        WallFilter wallDefault = new WallFilter();
        wallDefault.setDbType("mysql");
        druidPlugin.addFilter(wallDefault);
        druidPlugin.setInitialSize(ResKit.getConfigInt("db.default.poolInitialSize"));
        druidPlugin.setMaxPoolPreparedStatementPerConnectionSize(ResKit.getConfigInt("db.default.poolMaxSize"));
        druidPlugin.setTimeBetweenConnectErrorMillis(ResKit.getConfigInt("db.default.connectionTimeoutMillis"));
        plugins.add(druidPlugin);
        //开启DB+record 映射关系插件
        ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
        _MappingKit.mapping(arp);
        arp.getEngine().setDevMode(true);
        arp.getEngine().addSharedMethod(new StrUtil());
        arp.setBaseSqlTemplatePath(PathKit.getRootClassPath()+"/sql");
        arp.addSqlTemplate("all.sql");
        arp.setShowSql(true);
        plugins.add(arp);
        //开启eheache缓存
        plugins.add(new EhCachePlugin());
    }

    private DruidPlugin createDruidPlugin() {
        DruidPlugin druidDefault = new DruidPlugin(ResKit.getConfig("db.default.url"), ResKit.getConfig("db.default.user"),
                ResKit.getConfig("db.default.password"),ResKit.getConfig("db.default.driver"));
        return druidDefault;
    }

    @Override
    public void configInterceptor(Interceptors interceptors) {
        interceptors.add(new ExceptionInterceptor());
        interceptors.add(new AdminIAuthInterceptor());
    }

    @Override
    public void configHandler(Handlers handlers) {

    }

    @Override
    public void afterJFinalStart() {
        super.afterJFinalStart();
        CoreData.loadAllCache();//省市区 Cache 初始化
    }
}
