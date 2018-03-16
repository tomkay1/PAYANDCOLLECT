package com.yhh.csap.admin;

import com.alibaba.fastjson.JSON;
import com.jfinal.aop.Clear;
import com.jfinal.kit.StrKit;
import com.xiaoleilu.hutool.util.StrUtil;
import com.yhh.csap.Consts;
import com.yhh.csap.admin.model.Res;
import com.yhh.csap.admin.model.User;
import com.yhh.csap.core.CoreController;
import com.yhh.csap.interceptors.AdminAAuthInterceptor;
import com.yhh.csap.interceptors.AdminIAuthInterceptor;
import com.yhh.csap.kits.CookieKit;
import com.yhh.csap.kits.ext.BCrypt;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuhaihui8913 on 2018/1/26.
 * 登录请求 处理
 *
 *
 */
@Clear({AdminIAuthInterceptor.class, AdminAAuthInterceptor.class})
public class LoginCtr extends CoreController{

    public void login(){
        String username=getPara("user");
        String password=getPara("password");
        String rm=getPara("rememberMe");
        if(StrUtil.isBlank(username)){
            renderFailJSON("用户名不能为空");
            return;
        }

        if (StrUtil.isBlank(password)){
            renderFailJSON("密码不能为空");
            return;
        }


        User user = com.yhh.csap.admin.model.User.dao.findFirst("select * from s_user where loginname=? ", username);

        if(user==null){
            renderFailJSON("用户不存在!");
            return;
        }

        if (BCrypt.checkpw(password, user.getPassword())) {
            if (user.getStatus().equals(Consts.STATUS.enable.getVal())) {

                Map<String, Object> data = new HashMap<String, Object>();
                List<Res> resList=Res.dao.findResesByUserId(user.getId());
                data.put("resList",resList);
                data.put("username",user.getNickname());
                data.put("loginname",user.getLoginname());
                user.setLogged(new Date());
                user.update();
                if(StrKit.notBlank(rm)&&rm.equals("0"))
                    CookieKit.put(this, Consts.USER_ACCESS_TOKEN, user.getId().toString(), 60*60*24*14);
                else
                    CookieKit.put(this, Consts.USER_ACCESS_TOKEN, user.getId().toString(), Consts.COOKIE_TIMEOUT);
                renderSuccessJSON("登录成功", JSON.toJSONString(data));
                return;
            } else {
                renderFailJSON("该用户被禁用", "");
                return;
            }
        } else {
            renderFailJSON("密码不正确", "");
            return;
        }

    }

    public void logout(){
        CookieKit.remove(this,Consts.USER_ACCESS_TOKEN);
        renderSuccessJSON("退出系统成功");
    }

}
