
const home = {
    state: {

    },
    mutations: {

    },
    actions:{
        home_merFee_list:function({commit,state},param){
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/home/fee', param).then((res) => {
                    resolve(res);
                })
            });
        },
        home_total:function({commit,state},param){
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/home/total', param).then((res) => {
                    resolve(res);
                })
            });
        },
    }
};

export default home;