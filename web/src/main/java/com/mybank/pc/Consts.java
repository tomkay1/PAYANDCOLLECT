package com.yhh.csap;

import org.jsoup.safety.Whitelist;

/**
 * Created by yuhaihui8913 on 2017/11/14.
 * 常量类
 */
public class Consts {

    public static final Whitelist basicWithImages=Whitelist.basicWithImages();

    public static final String ENCRYPT_KEY="yuhaihui3435-never-late";

    public static final String USER_ACCESS_TOKEN="userId-csap";

    public static final int COOKIE_TIMEOUT=24*60*60*10;

    public static final int COOKIE_FOREVER=24*60*60*6*365*50;

    public static final String CURR_USER="currUser";

    public static final String CURR_USER_ROLES="currUserRoles";

    public static final String CURR_USER_RESES="currUserReses";

    public static final String T_CATALOG_CK="catalog";

    public static final String T_TAG_CK="tag";

    public enum YORN {
        yes(true), no(false);
        boolean val;

        private YORN(boolean val) {
            this.val = val;
        }

        public String getLabel() {
            return (val) ? "否" : "是";
        }

        public boolean isVal() {
            return val;
        }
    }

    public enum YORN_STR {
        yes("0"), no("1");
        String val;

        private YORN_STR(String val) {
            this.val = val;
        }

        public String getLabel() {
            return (val.equals("0")) ? "是" : "否";
        }

        public String getVal() {
            return val;
        }
    }

    /**
     * @param
     * @author: 于海慧  2016/12/10
     * @Description:  状态枚举
     * @return void
     * @throws
     **/
    public enum STATUS {
        enable("0"), forbidden("1");
        String val;

        private STATUS(String val) {
            this.val = val;
        }
        public String getVal(){
            return this.val;
        }
        public String getValTxt(){
            return (val.equals("0")?"正常":"禁用");
        }
    }

    public enum REQ_JSON_CODE {
        success, fail,unauthorized;
    }

    public enum CHECK_STATUS{
        normal("00"), waitingCheck("01"),revokeCheck("02");
        String val;

        private CHECK_STATUS(String val) {
            this.val = val;
        }
        public String getVal(){
            return this.val;
        }
        public String getValTxt(){
            if(val.equals("00")){
                return "正常";
            }else if(val.equals("01")){
                return "等待审批";
            }else if(val.equals("02")){
                return "未通过审批";
            }
            return "";
        }
    }

    public enum CACHE_NAMES {
        paramCache,ssq,userRoles,user,userReses,taxonomy,art,carouselsetting
    }

}
