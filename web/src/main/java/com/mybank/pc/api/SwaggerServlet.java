package com.mybank.pc.api;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import cn.hutool.setting.dialect.Props;
import com.alibaba.fastjson.JSON;
import com.mybank.swagger.fz.annotation.*;
import com.mybank.swagger.fz.model.SwaggerDoc;
import com.mybank.swagger.fz.model.SwaggerPath;
import com.mybank.swagger.fz.model.SwaggerResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by yuhaihui8913 on 2017/11/29.
 */
public class SwaggerServlet extends HttpServlet {


    private static final Set<Class<?>> CLASS_SET;
    private static final Props props;

    static {

        props = new Props("swagger.properties","UTF-8");
        if (props == null) {
            StaticLog.error("没有找到swagger.properties文件");
        }
        String basePackage = props.getStr("basePackage");
        if (StrUtil.isEmpty(basePackage)) {
            StaticLog.error("没有找到要扫描的包");
        }
        CLASS_SET = ClassUtil.scanPackageByAnnotation(basePackage, Api.class);
        StaticLog.info("包扫描结束");
    }

    @Override
    public void init() throws ServletException {
        super.init();

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        super.doGet(req, resp);
        String DEF_BLK = "";
        StaticLog.info("开始获取所有的API数据");
        SwaggerDoc doc = new SwaggerDoc();
        doc.setSwagger("2.0");
        SwaggerDoc.InfoBean infoBean = new SwaggerDoc.InfoBean();
        if(StrUtil.isNotBlank(props.getStr("api.title", DEF_BLK)))
        infoBean.setTitle(props.getStr("api.title", DEF_BLK));
        if(StrUtil.isNotBlank(props.getStr("api.desc", DEF_BLK)))
        infoBean.setDescription(props.getStr("api.desc", DEF_BLK));
        if(StrUtil.isNotBlank(props.getStr("api.version", DEF_BLK)))
        infoBean.setVersion(props.getStr("api.version", DEF_BLK));
        infoBean.getContact().put("email", props.getStr("api.contact.email", DEF_BLK));
        infoBean.getLicense().put("name", props.getStr("api.license.name", DEF_BLK));
        infoBean.getLicense().put("url", props.getStr("api.license.url", DEF_BLK));
        doc.setInfo(infoBean);
        doc.setHost(props.getStr("api.host", DEF_BLK));
        doc.setBasePath(props.getStr("api.basePath", DEF_BLK));
        doc.getExternalDocs().put("description", props.getStr("api.externalDocs.desc", DEF_BLK));
        doc.getExternalDocs().put("url", props.getStr("api.externalDocs.url", DEF_BLK));
        String schemesStr = props.getStr("api.schemes", DEF_BLK);
        doc.setSchemes(StrUtil.split(schemesStr, '|'));
        Map<String, Map<String, SwaggerPath.ApiMethod>> paths = new HashMap<>();
        Map<String, Class<?>> resClzMap = new HashMap<>();
        Map<String, String> classMap = new HashMap<>();
        for (Class<?> cls : CLASS_SET) {
            if (cls.isAnnotationPresent(Api.class)) {
                Api api = cls.getAnnotation(Api.class);

                if (!classMap.containsKey(api.tag())) {
                    classMap.put(api.tag(), api.description());
                }


                Method[] methods = cls.getMethods();

                for (Method method : methods) {

                    Annotation[] annotations = method.getAnnotations();
                    SwaggerPath.ApiMethod apiMethod = new SwaggerPath.ApiMethod();
                    apiMethod.setOperationId(apiMethod.getOperationId());
                    apiMethod.addProduce("application/json");


                    for (Annotation annotation : annotations) {
                        Class<? extends Annotation> annotationType = annotation.annotationType();
                        if (ApiOperation.class == annotationType) {

                            ApiOperation apiOperation = (ApiOperation) annotation;

                            Map<String, SwaggerPath.ApiMethod> methodMap = new HashMap<>();
                            apiMethod.setSummary(apiOperation.summary());
                            apiMethod.setDescription(apiOperation.description());
                            apiMethod.addTag(apiOperation.tag());
                            apiMethod.addConsume(apiOperation.consume());
                            methodMap.put(apiOperation.httpMethod(), apiMethod);
                            paths.put(apiOperation.url(), methodMap);
                        } else if (Params.class == annotationType) {
                            Params apiOperation = (Params) annotation;
                            Param[] params = apiOperation.value();
                            for (Param apiParam : params) {
                                apiMethod.addParameter(new SwaggerPath.Parameter(apiParam.name(),apiParam.in(), apiParam.description(), apiParam.required(), apiParam.dataType(), apiParam.format(), apiParam.defaultValue()));
                            }
                        } else if (Param.class == annotationType) {
                            Param apiParam = (Param) annotation;
                            apiMethod.addParameter(new SwaggerPath.Parameter(apiParam.name(),apiParam.in(), apiParam.description(), apiParam.required(), apiParam.dataType(), apiParam.format(), apiParam.defaultValue()));
                        } else if (ApiResponses.class == annotationType) {
                            ApiResponses apiResponses = (ApiResponses) annotation;
                            ApiResponse[] apiResponseArray = apiResponses.responses();
                            SwaggerResponse swaggerResponse = null;
                            SwaggerResponse.Schema ss = null;
                            SwaggerResponse.Items si = null;
                            Schema schema = null;


                            if(apiResponseArray.length>0) {
                                for (ApiResponse ar : apiResponseArray) {
                                    swaggerResponse = new SwaggerResponse(ar.desc());
                                    ss = new SwaggerResponse.Schema();
                                    schema = ar.schema();
                                    ss.setType("string");
//                                if (!schema.isObject()) {//返回String 字符串
//                                    ss.setType("string");
//                                } else if (schema.isArray()) {//返回 结构化对象list
//                                    ss.setType("array");
//                                    si = new SwaggerResponse.Items("#/definitions/" + schema.ref().getSimpleName());
//                                    ss.setItems(si);
//                                } else {//返回 结构化数据对象
//                                    ss.set$ref("#/definitions/" + schema.ref().getSimpleName());
//                                }
                                    swaggerResponse.setSchema(ss);
                                    apiMethod.addResponse(ar.code(), swaggerResponse);
                                    //搜集所有的结构化对象类型
//                                if (schema.ref()!=Void.class&&!resClzMap.containsKey(schema.ref().getSimpleName())) {
//                                    resClzMap.put(schema.ref().getSimpleName(), schema.ref());
//                                }
                                }
                            }else{
                                swaggerResponse = new SwaggerResponse("");
                                ss = new SwaggerResponse.Schema();
                                ss.setType("string");
                                swaggerResponse.setSchema(ss);
                                apiMethod.addResponse("200",swaggerResponse);
                            }


                        }
                    }
                }
            }
        }
        StaticLog.info("API获取成功");
        if (classMap.size() > 0) {
            for (String key : classMap.keySet()) {
                doc.addTags(new SwaggerDoc.TagBean(key, classMap.get(key)));
            }
        }
//        if(resClzMap.size()>0){
//            for(String key:resClzMap.keySet()){
//                Class<?> clz=resClzMap.get(key);
//
//                JSONObject jo=JSONUtil.createObj();
//
//            }
//        }

        doc.setPaths(paths);

        // swaggerUI 使用Java的关键字default作为默认值,此处将生成的JSON替换
        String jsonStr = JSON.toJSONString(doc,true).replaceAll("\"defaultValue\"", "\"default\"");
        StaticLog.info("API数据为:{}", jsonStr);
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter out = null;
        try {
            out = resp.getWriter();
            out.write(jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }

    }
}
