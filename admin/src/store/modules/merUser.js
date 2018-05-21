import Cookies from 'js-cookie';
import kit from '../../libs/kit';

const merUser = {
    state: {
        userList:[],
        totalPage:0,
        pageNumber:1,
        totalRow:0,
        pageSize:15,
        user:{}
    },
    mutations: {
        set_merUser_list(state,page){
            state.userList=page.list
            state.totalPage=page.totalPage
            state.pageNumber=page.pageNumber
            state.pageSize=page.pageSize;
            state.totalRow=page.totalRow
        },
        merUser_reset(state,param){

            if(param) {
                state.user = kit.clone(param)
            }
            else
                state.user={roleIds:[],isAdmin:'1'};

        }
    },
    actions:{
        merUser_list:function ({ commit,state },param) {
            if(param&&!param.pn){
                param.pn=state.pageNumber;
            }
            this._vm.$axios.post('/mer2/list',param).then((res)=>{
                commit('set_merUser_list',res)
            });
        },

        merUser_save:function ({ commit,state },action) {
            let vm=this._vm;
            let p=kit.clone(state.user)
            let rIds=p.roleIds;
            let rIds_str=rIds.join(",");
            p.roleIds=rIds_str;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/mer2/'+action, p).then((res) => {
                    if(res.resCode&&res.resCode=='success'){
                        commit('merUser_reset');
                    }
                    resolve(res.resCode);
                });
            });
        },
        merUser_del:function({commit,state},param){
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/mer2/del', param).then((res) => {
                    resolve(res.resCode)
                })
            });
        },
        merUser_stop:function({commit,state},param){
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/mer2/forbidden', param).then((res) => {
                    resolve(res.resCode)
                })
            });
        },
        merUser_active:function({commit,state},param){
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/mer2/resumed', param).then((res) => {
                    resolve(res.resCode)
                })
            });
        },
        merUser_reset_pwd:function ({ commit,state },param) {
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/mer2/resetPwd', param).then((res) => {
                    resolve(res);
                });
            });
        },


    }
};

export default merUser;
