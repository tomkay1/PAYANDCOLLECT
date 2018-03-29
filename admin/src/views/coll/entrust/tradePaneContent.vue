<template>
    <div>
        <Row>
            <Col span="24">
            <Card>
                <p slot="title">
                    <Icon type="help-buoy"></Icon>
                    交易流水
                </p>
                <Row>
                    <Col span="24" align="right">
                    <DatePicker type="date" placeholder="开始日期" style="width: 200px" v-model="bTime" format="yyyy-MM-dd" :clearable="false"></DatePicker>
                    <DatePicker type="date" placeholder="结束日期" style="width: 200px" v-model="eTime" format="yyyy-MM-dd" :clearable="false"></DatePicker>
                    <Input v-model="searchKey" placeholder="请输入..." style="width: 200px" />
                    <span @click="search" style="margin: 0 10px;">
                        <Button type="primary" icon="search">搜索</Button>
                    </span>
                    </Col>
                </Row>
                <Row class="margin-top-10">
                    <Table border :data="entrustTradeList" :columns="tableColums" stripe width="100%"></Table>
                </Row>
                <div style="margin: 10px;overflow: hidden">
                    <div style="float: right;">
                        <Page page-size="10" :total="total" :current="pageNumber" @on-change="search" show-total show-elevator></Page>
                    </div>
                </div>
            </Card>
            </Col>
        </Row>
    </div>
</template>

<script>
    import { mapState } from 'vuex'
    import dateKit from '../../../libs/date'
    export default {
        name: 'entrustTradePaneContent',
        computed: {
            ...mapState({
                'entrustTradeList': state => state.unionpayEntrust.entrustTradeList,
                'totalPage': state => state.unionpayEntrust.totalPage,
                'pageNumber': state => state.unionpayEntrust.pageNumber,
                'total': state => state.unionpayEntrust.totalRow,
                'unionpayEntrust': state => state.unionpayEntrust.unionpayEntrust,
            })
        },
        methods: {
            search(pn) {
                let param = {
                    'bTime': dateKit.formatDate(this.bTime, 'yyyy-MM-dd'),
                    'eTime': dateKit.formatDate(this.eTime, 'yyyy-MM-dd'),
                    search: this.searchKey,
                    pn: pn,
                    ps: '10'
                }
                this.$store.dispatch('get_entrust_trade_list', param)
            }
        },
        mounted() {
            this.$store.dispatch('get_entrust_trade_list')
        },
        data() {
            return {
                bTime: new Date(),
                eTime: new Date(),
                searchKey: '',
                tableColums: [
                    {
                        title: '姓名',
                        key: 'customerNm'
                    },
                    {
                        title: '证件号码',
                        key: 'certifId',
                    },
                    {
                        title: '卡号',
                        key: 'accNo',
                    },
                    {
                        title: '手机号',
                        key: 'phoneNo',
                    },
                    {
                        title: '订单号',
                        key: 'orderId',
                    },
                    {
                        title: '应答码',
                        key: 'respCode',
                    },
                    {
                        title: '应答信息',
                        key: 'respMsg',
                    },
                    {
                        title: '创建时间',
                        key: 'cat',
                    }
                ]
            }
        }
    }
</script>
<style lang="less">
    @import '../../../styles/common.less';
</style>