import Cookies from 'js-cookie';
import kit from '../../libs/kit';

const merInfo = {
    state: {
        merInfoList:[],
        totalPage:0,
        pageNumber:1,
        pageSize:15,
        totalRow:0,
        merInfo:{},
        merchantTypeList:[],
        merFeeListJ:[],
        merFeeListB:[],

    },
    mutations: {
        set_merInfo_list(state,page){
            state.merInfoList=page.page.list
            state.totalPage=page.page.totalPage
            state.pageSize=page.page.pageSize
            state.pageNumber=page.page.pageNumber
            state.totalRow=page.page.totalRow
            state.merchantTypeList =page.tList
        },
        merInfo_reset(state,param){
            if(param) {
                state.merInfo = kit.clone(param)
            }
        },
        set_merFee_list(state,map){
            state.merFeeListJ=map.feeListJ
            state.merFeeListB=map.feeListB

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

        merInfo_save:function ({ commit,state },action) {
            let vm=this._vm;
            let p=kit.clone(state.merInfo)
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/mer00/'+action, p).then((res) => {
                    // if(res.resCode&&res.resCode=='success'){
                    //     commit('user_reset');
                    // }
                    resolve(res.resCode);
                });
            });
        },
        merInfo_del:function({commit,state},param){
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/mer00/del', param).then((res) => {
                    resolve(res.resCode)
                })
            });
        },
        merInfo_stop:function({commit,state},param){
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/mer00/forbidden', param).then((res) => {
                    resolve(res.resCode)
                })
            });
        },
        merInfo_active:function({commit,state},param){
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/mer00/enable', param).then((res) => {
                    resolve(res.resCode)
                })
            });
        },

        merFee_list:function({commit,state},param){
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/mer00/listFee', param).then((res) => {
                  commit("set_merFee_list",res)
                    resolve(res);
                })
            });
        },
        add_merFee:function({commit,state},p){
            let vm=this._vm;

            return new Promise(function (resolve, reject) {
                vm.$axios.post('/mer00/addFee',p).then((res) => {
                    if(res.resCode&&res.resCode=='success'){
                        commit("set_merFee_list",res)
                        resolve(res);
                    }


                })
            });
        },
        del_merFee:function({commit,state},p){
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/mer00/delFee',{id:p}).then((res) => {
                    if(res.resCode&&res.resCode=='success'){
                        commit("set_merFee_list",res)
                    }
                })
            });
        },
        login_merInfo:function({commit,state}){
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/mer00/loginMerInfo').then((res) => {
                    resolve(res);
                })
            });
        },


    }
};

export default merInfo;
