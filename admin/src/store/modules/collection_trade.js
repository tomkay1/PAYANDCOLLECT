
const collTrade = {
    state: {
        tradeList: [],
        totalPage: 0,
        pageNumber: 1,
        totalRow: 0,
        collTrade: {}
    },
    mutations: {
        set_trade_list(state, page) {
            state.tradeList = page.list;
            state.totalPage = page.totalPage;
            state.pageNumber = page.pageNumber;
            state.totalRow = page.totalRow;
        }
    },
    actions: {
        trade_list: function ({ commit, state }, param) {
            if (param && !param.pn) {
                param.pn = state.pageNumber;
            }
            this._vm.$axios.post('/coll/trade/list', param).then((res) => {
                commit('set_trade_list', res);
            });
        }
    }
};

export default collTrade;
