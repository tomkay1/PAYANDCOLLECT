import kit from '../../libs/kit';
const merCust = {
    state: {
        merCustList:[],
        totalPage:0,
        pageNumber:1,
        totalRow:0,
        merCust:{},

    },
    mutations: {

        set_merCust_list(state,page){
            state.merCustList=page.list
            state.totalPage=page.totalPage
            state.pageNumber=page.pageNumber
            state.totalRow=page.totalRow

        },
        merCust_info:function(state,param){
            state.merCust = kit.clone(param)
        },

    },
    actions:{
        merCust_list:function ({ commit,state },param) {
            if(param&&!param.pn){
                param.pn=state.pageNumber;
            }
            this._vm.$axios.post('/mer01/list',param).then((res)=>{
                commit('set_merCust_list',res)
            });
        },


        merCust_del:function({commit,state},param){
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/mer01/del', param).then((res) => {
                    resolve(res.resCode)
                })
            });
        },



    }
};

export default merCust;
