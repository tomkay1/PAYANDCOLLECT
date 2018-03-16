package com.yhh.csap.core;

/**
 * Created by yuhaihui8913 on 2016/12/6.
 */
public class CoreException extends RuntimeException {
    private String code="999";
    private String msg;

    public CoreException(String msg){
        super(msg);
        this.msg=msg;
    }

    public CoreException(String code, String msg){
        this.code=code;
        this.msg=msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
