import Cookies from 'js-cookie';
import kit from '../../libs/kit';

const merInfo = {
    state: {
        merInfoList:[],
        totalPage:0,
        pageNumber:1,
        totalRow:0,
        merInfo:{}
    },
    mutations: {

        set_merInfo_list(state,page){
            state.merInfoList=page.list
            state.totalPage=page.totalPage
            state.pageNumber=page.pageNumber
            state.totalRow=page.totalRow
        },

    },
    actions:{
        merInfo_list:function ({ commit,state },param) {
            if(param&&!param.pn){
                param.pn=state.pageNumber;
            }
            this._vm.$axios.post('/mer00/list',param).then((res)=>{
                commit('set_merInfo_list',res)
            });
        },

        user_save:function ({ commit,state },action) {
            let vm=this._vm;
            let p=kit.clone(state.user)
            let rIds=p.roleIds;
            let rIds_str=rIds.join(",");
            p.roleIds=rIds_str;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/ad01/'+action, p).then((res) => {
                    if(res.resCode&&res.resCode=='success'){
                        commit('user_reset');
                    }
                    resolve(res.resCode);
                });
            });
        },
        user_del:function({commit,state},param){
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/ad01/del', param).then((res) => {
                    resolve(res.resCode)
                })
            });
        },
        user_stop:function({commit,state},param){
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/ad01/forbidden', param).then((res) => {
                    resolve(res.resCode)
                })
            });
        },
        user_active:function({commit,state},param){
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/ad01/resumed', param).then((res) => {
                    resolve(res.resCode)
                })
            });
        },
        user_reset_pwd:function ({ commit,state },param) {
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/ad01/resetPwd', param).then((res) => {
                    resolve(res);
                });
            });
        },
        user_login:function ({ commit,state },param) {
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/ad06/login', param).then((res) => {
                    resolve(res);
                });
            });
        },
        user_logout:function ({ commit,state },param) {
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/ad06/logout', param).then((res) => {
                    resolve(res);
                });
            });
        },
        update_pwd:function ({commit,state},param) {
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/ad01/modifyPassword', param).then((res) => {
                    resolve(res);
                });
            });
        }

    }
};

export default merInfo;
