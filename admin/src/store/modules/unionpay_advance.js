import kit from '../../libs/kit';
const unionpayAdvance = {
    state: {
        advanceTradeList: [],
        totalPage: 0,
        pageNumber: 1,
        totalRow: 0,
        unionpayAdvance: {}
    },
    mutations: {
        set_advance_trade_list(state, page) {
            state.advanceTradeList = page.list;
            state.totalPage = page.totalPage;
            state.pageNumber = page.pageNumber;
            state.totalRow = page.totalRow;
        },
        unionpay_advance_set(state, obj) {
            if (obj !== undefined) {
                state.unionpayAdvance = kit.clone(obj);
            }
        }
    },
    actions: {
        get_advance_trade_list: function ({ commit, state }, param) {
            if (param && !param.pn) {
                param.pn = state.pageNumber;
            }
            this._vm.$axios.post('/advance/trade/list', param).then((res) => {
                commit('set_advance_trade_list', res.pageInfo);
            });
        },
        advance_save: function ({ commit, state }) {
            let vm = this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.post('/advance/trade/initiate', state.unionpayAdvance).then((res) => {
                    resolve(res.resCode);
                });
            });
        }
    }
};

export default unionpayAdvance;
