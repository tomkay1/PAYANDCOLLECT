<template>
    <div>
        <Row>
            <Col span="24">
            <Card>
                <p slot="title">
                    <Icon type="ionic"></Icon>
                    交易查询
                </p>
                <Row>
                    <Col span="8">
                    <Button type="primary" icon="person-add" @click="initiate">发起交易</Button>
                    <Button type="primary" @click="refresh" icon="refresh">刷新</Button>
                    </Col>
                    <Col span="8" offset="8" align="right">
                    <Input v-model="searchKey" placeholder="请输入..." style="width: 200px" />
                    <span @click="search" style="margin: 0 10px;">
                        <Button type="primary" icon="search">搜索</Button>
                    </span>
                    </Col>
                </Row>
                <Row class="margin-top-10">
                    <Table border :data="tradeList" :columns="tableColums" stripe></Table>
                </Row>
                <div style="margin: 10px;overflow: hidden">
                    <div style="float: right;">
                        <Page :total="total" :current="pageNumber" @on-change="search" show-total show-elevator></Page>
                    </div>
                </div>
            </Card>
            </Col>
        </Row>
        <addForm ref="aem" :pageSize="pageSize"></addForm>
    </div>
</template>

<script>
    import { mapState } from 'vuex'
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
                this.$store.dispatch('trade_list', { search: this.searchKey, pn: pn, ps: this.pageSize })
            },
            refresh() {
                this.$store.dispatch('trade_list', { search: this.searchKey, ps: this.pageSize })
            },
            initiate() {
                this.$refs.aem.open();
            },
        },
        components: {
            addForm: addTradeModal
        },
        mounted() {
            this.$store.dispatch('trade_list')
        },
        data() {
            return {
                searchKey: '',
                pageSize: 10,
                tableColums: [
                    {
                        title: '交易流水号',
                        key: 'tradeNo',
                    },
                    {
                        title: '交易时间',
                        key: 'tradeTime',
                    },
                    {
                        title: '类型',
                        key: 'bussType',
                    },
                    {
                        title: '金额',
                        key: 'amount',
                    },
                    {
                        title: '身份证号',
                        key: 'cardID',
                    },
                    {
                        title: '客户姓名',
                        key: 'custName',
                    },
                    {
                        title: '最终处理结果',
                        key: 'finalCode',
                    },
                    {
                        title: '清分状态',
                        key: 'clearStatus',
                    },
                    {
                        title: '创建时间',
                        key: 'cat',
                    },
                ]
            }
        }
    }
</script>
<style lang="less">
    @import '../../../styles/common.less';
</style>