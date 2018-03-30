<template>
    <div>
        <Row>
            <Col span="24">
            <Card>
                <p slot="title">
                    <Icon type="help-buoy"></Icon>
                    委托状态
                </p>
                <Row>
                    <Col span="8">
                    <Button type="primary" icon="person-add" @click="add">建立委托</Button>
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
                    <Table border :data="entrustList" :columns="tableColums" stripe></Table>
                </Row>
                <div style="margin: 10px;overflow: hidden">
                    <div style="float: right;">
                        <Page :page-size="pageSize" :total="total" :current="pageNumber" @on-change="search" show-total show-elevator></Page>
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
    import addEntrustModal from './addForm.vue'
    export default {
        name: 'entrustStatusPaneContent',
        computed: {
            ...mapState({
                'entrustList': state => state.collEntrust.entrustList,
                'totalPage': state => state.collEntrust.totalPage,
                'pageNumber': state => state.collEntrust.pageNumber,
                'total': state => state.collEntrust.totalRow,
                'collEntrust': state => state.collEntrust.collEntrust,
            })
        },
        methods: {
            search(pn) {
                this.$store.dispatch('get_entrust_list', { search: this.searchKey, pn: pn, ps: this.pageSize })
            },
            refresh() {
                this.$store.dispatch('get_entrust_list', { search: this.searchKey, ps: this.pageSize })
            },
            add() {
                this.$refs.aem.open();
            },
        },
        components: {
            addForm: addEntrustModal
        },
        mounted() {
            this.$store.dispatch('get_entrust_list', { ps: this.pageSize })
        },
        data() {
            return {
                searchKey: '',
                pageSize: 10,
                tableColums: [
                    {
                        title: '姓名',
                        key: 'customerNm',
                    },
                    {
                        title: '证件类型',
                        key: 'certifTp',
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
                        title: '状态',
                        key: 'status',
                    },
                    {
                        title: '创建时间',
                        key: 'cat',
                    },
                    {
                        title: '修改时间',
                        key: 'mat',
                    }
                ]
            }
        }
    }
</script>
<style lang="less">
    @import '../../../styles/common.less';
</style>