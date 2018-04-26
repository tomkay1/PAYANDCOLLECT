<template>
    <div>
        <Row>
            <Col span="24">
            <Card>
                <div>
                    <div>
                        <Row>
                            <Col span="6" align="left">
                            <Button type="primary" icon="person-add" @click="initiate">发起交易</Button>
                            <Button type="primary" @click="refresh" icon="refresh">刷新</Button>
                            </Col>
                            <Col span="18" align="right">
                            <Select v-model="finalCode" style="width: 120px; text-align: center;" placeholder="最终处理结果">
                                <Option v-for="item in finalCodeList" :value="item.value" :key="item.value">{{ item.label }}</Option>
                            </Select>
                            <DatePicker type="date" placeholder="开始日期" style="width: 200px" v-model="bTime" format="yyyy-MM-dd" :transfer="true" :clearable="false"></DatePicker>
                            <DatePicker type="date" placeholder="结束日期" style="width: 200px" v-model="eTime" format="yyyy-MM-dd" :transfer="true" :clearable="false"></DatePicker>
                            <Input v-model="searchKey" placeholder="请输入..." style="width: 200px" />
                            <span @click="search" style="margin: 0 10px;">
                                <Button type="primary" icon="search">搜索</Button>
                            </span>
                            </Col>
                        </Row>
                    </div>
                    <Row class="margin-top-10">
                        <Table :border="false" :data="tradeList" :columns="tableColums" stripe></Table>
                    </Row>
                    <div style="margin: 10px;overflow: hidden">
                        <div style="float: right;">
                            <Page :total="total" :current="pageNumber" :page-size="pageSize" @on-change="search" show-total show-elevator></Page>
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
    import Vue from 'vue'
    import { mapState } from 'vuex'
    import dateKit from '../../../libs/date'
    import addTradeModal from './addTradeForm.vue'
    import FinalCodeContent from './finalCodeContent.vue'

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
            addForm: addTradeModal,
            FinalCodeContent: FinalCodeContent
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
                pageSize: 30,
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
                tableColums: [
                    {
                        title: '详情',
                        key: 'action',
                        width: 60,
                        align: 'center',
                        fixed: 'right',
                        render: (h, params) => {
                            const row = params.row;
                            return h('div', {
                                style: {
                                    'padding-left': '0px',
                                    'padding-right': '0px',
                                }
                            }, [
                                    h('a', {
                                        style: {
                                            display: 'inline-block',
                                            width: '100%',
                                        },
                                        on: {
                                            click: () => {
                                                // this.remove(params.index)
                                            }
                                        }
                                    }, [
                                            h('Icon', {
                                                props: {
                                                    type: 'chevron-right',
                                                    size: 18
                                                }
                                            })
                                        ]),
                                ]);
                        }
                    },
                    {
                        title: '交易时间',
                        key: 'tradeTime',
                        align: 'center',
                        minWidth: 180,
                    },
                    {
                        title: '交易流水号',
                        key: 'tradeNo',
                        align: 'center',
                        minWidth: 255,
                    },
                    {
                        title: '业务类型',
                        key: 'bussType',
                        render: (h, params) => {
                            const row = params.row;
                            const type = row.bussType === '1' ? '加急' : '批量';
                            return h('span', type);
                        },
                        align: 'center',
                        minWidth: 100,
                    },
                    {
                        title: '交易金额',
                        key: 'amount',
                        align: 'center',
                        minWidth: 100,
                    },
                    {
                        title: '商户手续费',
                        key: 'merFee',
                        align: 'center',
                        minWidth: 100,
                    },
                    {
                        title: '处理结果',
                        key: 'finalCode',
                        render: (h, params) => {
                            const row = params.row;
                            var finalStatus = '';
                            var statusType = '';
                            var color = ''
                            if (row.finalCode === '0') {
                                finalStatus = '成功'
                                statusType = 'success'
                                color = '#19be6b';//绿色
                            } else if (row.finalCode === '1') {
                                finalStatus = '处理中'
                                statusType = 'primary'
                                color = '#2d8cf0';//蓝色
                            } else if (row.finalCode === '2') {
                                finalStatus = '失败'
                                statusType = 'error'
                                color = '#ed3f14';//红色
                            }
                            return h('Poptip', {
                                props: {
                                    trigger: 'click',
                                    placement: 'bottom',
                                    width: '100%'
                                },
                            }, [
                                    h('div', {
                                        style: {
                                            'min-width': '80px',
                                            color: color,
                                            cursor: 'pointer',
                                            padding: '7px 5px'
                                        },
                                    }, [
                                            h('span', {
                                                style: {
                                                    'margin-right': '4px',
                                                }
                                            }, finalStatus),
                                            h('Icon', {
                                                props: {
                                                    type: 'chevron-right',
                                                },
                                                style: {

                                                }
                                            }),
                                        ]
                                    ),
                                    h(FinalCodeContent, {
                                        slot: 'content',
                                        props: {
                                            index: params.index,
                                            tradeInfo: row
                                        },
                                        style: {
                                            padding: '0px'
                                        }
                                    }),
                                ]);
                        },
                        align: 'center',
                        minWidth: 120,
                    },

                    {
                        title: '客户姓名',
                        key: 'custName',
                        align: 'center',
                        minWidth: 100,
                    },
                    {
                        title: '身份证号',
                        key: 'cardID',
                        align: 'center',
                        minWidth: 160,
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
                        },
                        align: 'center',
                        minWidth: 100,
                    },
                ]
            }
        }
    }
</script>
<style lang="less">
    @import '../../../styles/common.less';
</style>