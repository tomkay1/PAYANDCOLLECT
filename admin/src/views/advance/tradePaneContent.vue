<template>
    <div>
        <Row>
            <Col span="24">
            <Row>
                <Col span="6">
                    <Button type="primary" icon="person-add" @click="add">发起代付</Button>
                    <Button type="primary" @click="refresh" icon="refresh">刷新</Button>
                </Col>

                <Col span="18" align="right">
                <Select v-model="finalCode" style="width: 120px; text-align: center;" placeholder="最终处理结果" :transfer="true">
                    <Option v-for="item in finalCodeList" :value="item.value" :key="item.value">{{ item.label }}</Option>
                </Select>
                <DatePicker type="date" placeholder="开始日期" style="width: 200px" v-model="bTime" format="yyyy-MM-dd" :clearable="false"></DatePicker>
                <DatePicker type="date" placeholder="结束日期" style="width: 200px" v-model="eTime" format="yyyy-MM-dd" :clearable="false"></DatePicker>
                <Input v-model="searchKey" placeholder="请输入客户姓名/身份证号/卡号..." style="width: 240px" />
                <span @click="search" style="margin: 0 10px;">
                    <Button type="primary" icon="search">搜索</Button>
                </span>
                </Col>
            </Row>
            <Row class="margin-top-10">
                <Table border :data="advanceTradeList" :columns="tableColums" stripe width="100%"></Table>
            </Row>
            <div style="margin: 10px;overflow: hidden">
                <div style="float: right;">
                    <Page :page-size="pageSize" :total="total" :current="pageNumber" @on-change="search" show-total show-elevator></Page>
                </div>
            </div>
            </Col>
        </Row>
        <addAdvanceModal ref="aem" :pageSize="pageSize" :finalCode="finalCode" :bTime="bTime" :eTime="eTime" :searchKey="searchKey" ></addAdvanceModal>
    </div>
</template>

<script>
    import { mapState } from 'vuex'
    import addAdvanceModal from './addForm.vue'
    import dateKit from '../../libs/date'

    export default {
        name: 'advanceTradePaneContent',
        components: {
            addAdvanceModal: addAdvanceModal
        },
        computed: {
            ...mapState({
                'advanceTradeList': state => state.unionpayAdvance.advanceTradeList,
                'totalPage': state => state.unionpayAdvance.totalPage,
                'pageNumber': state => state.unionpayAdvance.pageNumber,
                'total': state => state.unionpayAdvance.totalRow,
                'unionpayAdvance': state => state.unionpayAdvance.unionpayAdvance,
            })
        },
        methods: {
            add() {
                this.$refs.aem.open();
            },
            search(pn) {
                let param = {
                    finalCode: this.finalCode,
                    'bTime': dateKit.formatDate(this.bTime, 'yyyy-MM-dd'),
                    'eTime': dateKit.formatDate(this.eTime, 'yyyy-MM-dd'),
                    search: this.searchKey,
                    pn: pn,
                    ps: this.pageSize,
                }
                this.$store.dispatch('get_advance_trade_list', param)
            },
            refresh() {
                let param = {
                    finalCode: this.finalCode,
                    'bTime': dateKit.formatDate(this.bTime, 'yyyy-MM-dd'),
                    'eTime': dateKit.formatDate(this.eTime, 'yyyy-MM-dd'),
                    search: this.searchKey,
                    pn: this.pageNumber,
                    ps: this.pageSize
                }
                this.$store.dispatch('get_advance_trade_list', param)
            },
        },
        mounted() {
            let param = {
                finalCode: this.finalCode,
                'bTime': dateKit.formatDate(this.bTime, 'yyyy-MM-dd'),
                'eTime': dateKit.formatDate(this.eTime, 'yyyy-MM-dd'),
                search: this.searchKey,
                ps: this.pageSize,
                txnType: this.txnType,
            }
            this.$store.dispatch('get_advance_trade_list', param)
        },
        data() {
            return {
                finalCode: '',
                bTime: new Date(),
                eTime: new Date(),
                searchKey: '',
                pageSize: 30,
                txnType: '0',
                tableColums: [
                    {
                        title: '版本',
                        key: 'version',
                        align: 'center',
                        minWidth: 80,
                    },
                    {
                        title: '交易时间',
                        key: 'txnTime',
                        align: 'center',
                        minWidth: 150,
                    },
                    {
                        title: '订单号',
                        key: 'orderId',
                        align: 'center',
                        minWidth: 200,
                    },
                    {
                        title: '交易查询流水号',
                        key: 'queryId',
                        align: 'center',
                        minWidth: 200,
                    },
                    {   
                        title: '交易金额',
                        key: 'txnAmt',
                        align: 'center',
                        minWidth: 80,
                    },
                    {
                        title: '姓名',
                        key: 'customerNm',
                        align: 'center',
                        minWidth: 80,
                    },
                    {
                        title: '证件号码',
                        key: 'certifId',
                        align: 'center',
                        minWidth: 180,
                    },
                    {
                        title: '卡号',
                        key: 'accNo',
                        align: 'center',
                        minWidth: 180,
                    },
                    {
                        title: '最终处理结果',
                        key: 'finalCode',
                        align: 'center',
                        minWidth: 110,
                        render: (h, params) => {
                            const row = params.row;
                            var finalStatus = '';
                            if (row.finalCode === '0') {
                                finalStatus = '成功'
                            } else if (row.finalCode === '1') {
                                finalStatus = '处理中'
                            } else if (row.finalCode === '2') {
                                finalStatus = '失败'
                            }
                            return h('span', finalStatus);
                        }
                    },
                    {
                        title: '应答码',
                        key: 'respCode',
                        align: 'center',
                        minWidth: 80,
                    },
                    {
                        title: '应答信息',
                        key: 'respMsg',
                        align: 'center',
                        minWidth: 220,
                    },
                    {
                        title: '结果码',
                        key: 'resultCode',
                        align: 'center',
                        minWidth: 80,
                    },
                    {
                        title: '结果信息',
                        key: 'resultMsg',
                        align: 'center',
                        minWidth: 220,
                    },
                    {
                        title: '商户',
                        key: 'merId',
                        align: 'center',
                        minWidth: 150,
                    },
                    {
                        title: '系统跟踪号',
                        key: 'traceNo',
                        align: 'center',
                        minWidth: 100,
                    },
                    {
                        title: '交易传输时间',
                        key: 'traceTime',
                        align: 'center',
                        minWidth: 120,
                    },
                    {
                        title: '清算金额',
                        key: 'settleAmt',
                        align: 'center',
                        minWidth: 100,
                    },
                    {
                        title: '清算日期',
                        key: 'settleDate',
                        align: 'center',
                        minWidth: 100,
                    },
                    {
                        title: '创建时间',
                        key: 'cat',
                        align: 'center',
                        minWidth: 150,
                        render: function (h, params) {
                            return h('span', dateKit.formatDate(new Date(params.row.cat), 'yyyy-MM-dd hh:mm:ss'));
                        }
                    },
                    {
                        title: '修改时间',
                        key: 'mat',
                        align: 'center',
                        minWidth: 150,
                        render: function (h, params) {
                            return h('span', dateKit.formatDate(new Date(params.row.mat), 'yyyy-MM-dd hh:mm:ss'));
                        }
                    }
                ],
                finalCodeList: [
                    {
                        value: '',
                        label: '全部',
                    },
                    {
                        value: '0',
                        label: '成功'
                    },
                    {
                        value: '1',
                        label: '处理中'
                    },
                    {
                        value: '2',
                        label: '失败'
                    },
                ],
            }
        }
    }
</script>
<style lang="less">
    @import '../../styles/common.less';
</style>