package com.yhh.csap.interceptors;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;
import com.yhh.csap.Consts;
import com.yhh.csap.admin.model.Res;
import com.yhh.csap.admin.model.User;
import com.yhh.csap.core.CoreController;
import com.yhh.csap.core.CoreException;
import com.yhh.csap.kits.ReqKit;
import com.yhh.csap.kits.ResKit;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by yuhaihui8913 on 2017/11/16.
 * 请求访问控制前置处理
 *
 */
public class AdminAAuthInterceptor implements Interceptor{
    @Override
    public void intercept(Invocation invocation) {
        CoreController controller=(CoreController) invocation.getController();
        String ak=invocation.getActionKey();
        User user=controller.getAttr(Consts.CURR_USER);
        HttpServletRequest request=controller.getRequest();
        boolean flag=false;
        List<Res> resList=controller.getAttr(Consts.CURR_USER_RESES);
        for (Res res:resList){
            if(res.getUrl().equals(ak)){
                flag=true;
            }
        }

        //是否需要用户身份认证,方便测试
        if(ResKit.getConfigBoolean("userAuth"))
            flag=true;

        if(flag) {
            invocation.invoke();
        } else {
            if(ReqKit.isAjaxRequest(controller.getRequest())){
                controller.renderUnauthorizationJSON("admin");
            }else {
                throw new CoreException("访问权限认证失败！");
            }
        }


    }
}
