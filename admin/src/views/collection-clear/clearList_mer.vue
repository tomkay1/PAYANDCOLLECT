<template>
    <Tabs type="card">
        <TabPane label="清分明细查询">
            <Card>
                <Row>
                    <Col span="24"  align="right">
                    <Select v-model="chargeOff" style="width:200px" :clearable="true">
                        <Option v-for="item in chargetOffData" :value="item.value" :key="item.value">{{ item.label }}
                        </Option>
                    </Select>
                    <Input v-model="merNO" placeholder="商户号" style="width: 200px"></Input>
                    <DatePicker type="date" placeholder="查询开始日期" style="width: 200px" v-model="bTime_cc"
                                format="yyyy-MM-dd" :clearable="false"></DatePicker>
                    <DatePicker type="date" placeholder="查询结束日期" style="width: 200px" v-model="eTime_cc"
                                format="yyyy-MM-dd" :clearable="false"></DatePicker>
                    <span @click="search_cc" style="margin: 0 10px;">
                        <Button type="primary" icon="search">查询</Button>
                    </span>
                    <span @click="export_cc" style="margin: 0 10px;">
                        <Button type="primary" icon="archive">导出</Button>
                    </span>
                    </Col>
                </Row>
                <Row class="margin-top-10">
                    <Table :context="self" :data="ccList" :columns="tableColums_cc" stripe border></Table>
                </Row>
                <div style="margin: 10px;overflow: hidden">
                    <div style="float: right;">
                        <Page :total="total_cc" :current="pageNumber_cc" @on-change="search_cc" show-total
                              show-elevator></Page>
                    </div>
                </div>
            </Card>
        </TabPane>
    </Tabs>
</template>

<script>
    import {mapState} from 'vuex'
    import dateKit from '../../libs/date'
    import Input from "iview/src/components/input/input";

    export default {
        computed: {
            ...mapState({

                'ccList': state => state.cc.ccList,
                'totalPage_cc': state => state.cc.totalPage_cc,
                'total_cc': state => state.cc.totalRow_cc,
                'pageNumber_cc': state => state.cc.pageNumber_cc,
            })
        },
        data() {
            return {
                now: new Date(),
                self: this,
                merNO: '',
                chargeOff: '',
                bTime_cc: new Date(),
                eTime_cc: new Date(),
                tableColums: [

                    {
                        title: '清分日期',
                        key: 'cleartotleTimeTxt',
                    },
                    {
                        title: '交易笔数',
                        key: 'tradeCount',
                    },
                    {
                        title: '交易金额',
                        key: 'amountSum',
                    },
                    {
                        title: '出账金额',
                        key: 'amountOff',
                    },
                    {
                        title: '交易手续费金额',
                        key: 'amountFeeSum',
                    },
                    {
                        title: '银行代收手续费金额',
                        key: 'bankFee',
                    },
                    {
                        title: '利润',
                        key: 'profit',
                    },

                ],
                tableColums_cc: [

                    {
                        title: '商户号',
                        key: 'merNO',
                        fixed: 'left',
                        width:'100px'
                    },
                    {
                        title: '商户名',
                        key: 'merName',
                        fixed: 'left',
                        width:'100px'
                    },
                    {
                        title: '清分流水号',
                        key: 'clearNo',
                    },

                    {
                        title: '清分日期',
                        key: 'clearDateTxt',
                    },
                    {
                        title: '交易笔数',
                        key: 'tradeCount',
                    },
                    {
                        title: '交易金额',
                        key: 'amountSum',
                    },
                    {
                        title: '出账金额',
                        key: 'amountOff',
                    },
                    {
                        title: '交易手续费金额',
                        key: 'amountFeeSum',
                    },
                    {
                        title: '银行代收手续费金额',
                        key: 'bankFee',
                    },
                    {
                        title: '利润',
                        key: 'profit',
                    },
                    {
                        title: '预存抵扣手续费金额',
                        key: 'profit',
                    },
                    {
                        title: '交易抵扣手续费金额',
                        key: 'profit',
                    },

                ],
                chargetOffData: [
                    {
                        value: '0',
                        label: '已出账'
                    },
                    {
                        value: '1',
                        label: '未出账'
                    }
                ]

            }
        },
        methods: {
            search() {

                let param = {
                    'bTime': dateKit.formatDate(this.bTime, 'yyyy-MM-dd'),
                    'eTime': dateKit.formatDate(this.eTime, 'yyyy-MM-dd')
                }
                this.$store.dispatch("cct_list", param);
            }
            ,
            search_cc() {
                let param = {
                    'bTime': dateKit.formatDate(this.bTime_cc, 'yyyy-MM-dd'),
                    'eTime': dateKit.formatDate(this.eTime_cc, 'yyyy-MM-dd'),
                    merNO: this.merNO,
                    chargetOff: this.chargeOff
                }
                this.$store.dispatch("cc_list", param);
            },
            export_cct(){

            },
            export_cc(){

            }
        },
        components: {Input},
        mounted() {

        }

    }
</script>

<style lang="less">
    @import '../../styles/common.less';
</style>