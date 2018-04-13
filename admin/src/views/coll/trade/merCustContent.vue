<template>
    <div>
        <Row>
            <Col span="24">

            <Card>
                <div>
                    <div>
                        <Row>
                            <Col span="8" align="left">
                            <Button type="primary" icon="person-add" @click="initiate">发起交易</Button>
                            <Button type="primary" @click="refresh" icon="refresh">刷新</Button>
                            </Col>
                            <Col span="16" align="right">
                            <Select v-model="finalCode" style="width: 120px; text-align: center;" placeholder="最终处理结果">
                                <Option v-for="item in finalCodeList" :value="item.value" :key="item.value">{{ item.label }}</Option>
                            </Select>
                            <DatePicker type="date" placeholder="开始日期" style="width: 200px" v-model="bTime" format="yyyy-MM-dd" :clearable="false"></DatePicker>
                            <DatePicker type="date" placeholder="结束日期" style="width: 200px" v-model="eTime" format="yyyy-MM-dd" :clearable="false"></DatePicker>
                            <Input v-model="searchKey" placeholder="请输入..." style="width: 200px" />
                            <span @click="search" style="margin: 0 10px;">
                                <Button type="primary" icon="search">搜索</Button>
                            </span>
                            </Col>
                        </Row>

                    </div>

                    <Row class="margin-top-10">
                        <Table border :data="tradeList" :columns="tableColums" stripe></Table>
                    </Row>
                    <div style="margin: 10px;overflow: hidden">
                        <div style="float: right;">
                            <Page :total="total" :current="pageNumber" @on-change="search" show-total show-elevator></Page>
                        </div>
                    </div>
                </div>
            </Card>
            </Col>
        </Row>
        <addForm ref="aem" :pageSize="pageSize" :finalCode="finalCode" :bTime="bTime" :eTime="eTime" :searchKey="searchKey"></addForm>
    </div>
</template>

<script>
    import { mapState } from 'vuex'
    import dateKit from '../../../libs/date'
    import addTradeModal from './addTradeForm.vue'

    export default {
        computed: {
            ...mapState({
                'tradeList': state => state.collTrade.tradeList,
                'totalPage': state => state.collTrade.totalPage,
                'pageNumber': state => state.collTrade.pageNumber,
                'total': state => state.collTrade.totalRow,
                'collTrade': state => state.collTrade.collTrade,
            })
        },
        methods: {
            search(pn) {
                let param = {
                    finalCode: this.finalCode,
                    'bTime': dateKit.formatDate(this.bTime, 'yyyy-MM-dd'),
                    'eTime': dateKit.formatDate(this.eTime, 'yyyy-MM-dd'),
                    search: this.searchKey,
                    pn: pn,
                    ps: this.pageSize
                }
                this.$store.dispatch('trade_list', param)
            },
            refresh() {
                let param = {
                    finalCode: this.finalCode,
                    'bTime': dateKit.formatDate(this.bTime, 'yyyy-MM-dd'),
                    'eTime': dateKit.formatDate(this.eTime, 'yyyy-MM-dd'),
                    search: this.searchKey,
                    ps: this.pageSize
                }
                this.$store.dispatch('trade_list', param)
            },
            initiate() {
                this.$refs.aem.open();
            },
        },
        components: {
            addForm: addTradeModal
        },
        mounted() {
            let param = {
                finalCode: this.finalCode,
                'bTime': dateKit.formatDate(this.bTime, 'yyyy-MM-dd'),
                'eTime': dateKit.formatDate(this.eTime, 'yyyy-MM-dd'),
                search: this.searchKey,
                ps: this.pageSize
            }
            this.$store.dispatch('trade_list', param)
        },
        data() {
            return {
                finalCode: '',
                bTime: new Date(),
                eTime: new Date(),
                searchKey: '',
                pageSize: 10,
                finalCodeList: [
                    {
                        value: '',
                        label: '全部'
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
                tableColums: [
                    {
                        title: '交易流水号',
                        key: 'tradeNo',
                        width: '245px',
                    },
                    {
                        title: '交易时间',
                        key: 'tradeTime',
                        width: '150px',
                    },
                    {
                        title: '业务类型',
                        key: 'bussType',
                        render: (h, params) => {
                            const row = params.row;
                            const type = row.bussType === '1' ? '加急' : '批量';
                            return h('span', type);
                        }
                    },
                    {
                        title: '金额',
                        key: 'amount',
                    },
                    {
                        title: '身份证号',
                        key: 'cardID',
                        width: '160px',
                    },
                    {
                        title: '客户姓名',
                        key: 'custName',
                    },
                    {
                        title: '最终处理结果',
                        key: 'finalCode',
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
                        },
                        width: '120px',
                    },
                    {
                        title: '清分状态',
                        key: 'clearStatus',
                        render: (h, params) => {
                            const row = params.row;
                            var clearStatus = '';
                            if (row.clearStatus === '0') {
                                clearStatus = '已清分'
                            } else if (row.clearStatus === '1') {
                                clearStatus = '未清分'
                            }
                            return h('span', clearStatus);
                        }
                    },
                    {
                        title: '创建时间',
                        key: 'cat',
                        width: '150px',
                    },
                ]
            }
        }
    }
</script>
<style lang="less">
    @import '../../../styles/common.less';
</style>