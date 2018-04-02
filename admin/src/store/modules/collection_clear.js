import kit from '../../libs/kit';
const collectionClear = {
    state: {
        cctList: [],
        ccList: [],
        totalPage: 0,
        pageNumber: 1,
        totalRow: 0,
        totalPage_cc: 0,
        pageNumber_cc: 1,
        totalRow_cc: 0,
        collectionClear: {},
        collectionClearTotle: {}
    },
    mutations: {
        set_cct_list(state, page){
            state.cctList = page.list
            state.totalPage = page.totalPage
            state.totalRow = page.totalRow
            state.pageNumber = page.pageNumber
        },
        set_cct_list_sum(state, obj){
            if(state.cctList.length>0)
                state.cctList.push(obj)
        },
        set_cc_list(state, page){
            state.ccList = page.list
            state.totalPage_cc = page.totalPage
            state.totalRow_cc = page.totalRow
            state.pageNumber_cc = page.pageNumber
        },
        set_cc_list_sum(state, obj){
            if(state.ccList.length>0)
                state.ccList.push(obj)
        },
        set_collectionClear(state, obj){
            if (obj != undefined)
                state.collectionClear = Object.assign({},obj);
            else {
                state.collectionClear = {};
            }
        },

    },
    actions: {
        cct_list: function ({commit, state}, param) {
            let self=this;
            this._vm.$axios.post('/cc/totalList', param).then((res) => {
                commit('set_cct_list', res)
                self.dispatch('cct_sum',param)
            });
        },
        cct_sum: function ({commit, state}, param) {
            this._vm.$axios.post('/cc/sumTotal', param).then((res) => {
                commit('set_cct_list_sum', res)

            });
        },
        cc_list: function ({commit, state}, param) {
            let self=this;
            this._vm.$axios.post('/cc/list', param).then((res) => {
                commit('set_cc_list', res)
                self.dispatch('cct_sum',param)
            });
        },
        cc_sum: function ({commit, state}, param) {
            this._vm.$axios.post('/cc/sum', param).then((res) => {
                commit('set_cc_list_sum', res)

            });
        },
        hmClear: function ({commit, state}, param) {
            var vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/cc/hmClear', param).then((res) => {
                    resolve(res);
                })
            });
        },
        debit: function ({commit, state}, param) {
            var vm=this._vm;
            param=this.state.collectionClear;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/cc/debit', param).then((res) => {
                    resolve(res);
                })
            });
        },

        cct_list_export: function ({commit, state}, param) {
            let self=this;
            return new Promise(function (resolve, reject) {
                self._vm.$axios.post('/cc/exportTotalExcel', param).then((res) => {
                   resolve(res)
                });
            });
        },

        cc_list_export: function ({commit, state}, param) {
            let self=this;
            return new Promise(function (resolve, reject) {
                self._vm.$axios.post('/cc/exportExcel', param).then((res) => {
                    resolve(res)
                });
            });
        },
        cc_list_export_4mer: function ({commit, state}, param) {
            let self=this;
            return new Promise(function (resolve, reject) {
                self._vm.$axios.post('/cc/exportExcel4Mer', param).then((res) => {
                    resolve(res)
                });
            });
        },


    }
}
export default collectionClear