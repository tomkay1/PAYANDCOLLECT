<template>
    <div>
        <Row>
            <Col span="24">
            <Card>
                <p slot="title">
                    <Icon type="help-buoy"></Icon>
                    客户列表
                </p>
                <Row>
                    <Col span="24" align="right">
                    <Input v-model="searchKey" placeholder="输入客户姓名/手机号" style="width: 200px"/>
                    <span @click="search" style="margin: 0 10px;">
                        <Button type="primary" icon="search">搜索</Button></span>
                    </Col>
                </Row>
                <Row class="margin-top-10">
                    <Table :context="self" border :data="merCustList" :columns="tableColums" stripe></Table>
                </Row>
                <Row class="margin-top-10">
                    <Col span="24" align="right">
                    <Page :total="total" :current="pageNumber" @on-change="search" show-total show-sizer ></Page>
                    </Col>
                </Row>
            </Card>
            </Col>
        </Row>
        <Modal v-model="merCustModal" :mask-closable="false">
            <p slot="header">
                <Icon type="information-circled"></Icon>
                <span>{{modalTitle}}</span>
            </p>


            <div slot="footer">

                <Button type="error" @click="merCustModal=false">关闭</Button>
            </div>
        </Modal>
    </div>

</template>

<script>
    import {mapState} from 'vuex'

    const delBtn = (vm, h, param) => {
        return h('Poptip', {
            props: {
                confirm: '',
                title: '您确定要删除吗？'
            },
            style: {
                marginRight: '5px'
            },
            on: {
                'on-ok': () => {
                    vm.del(param.row.id)
                }
            }
        }, [h('Button', {
            props: {
                type: 'error',
                size: 'small'
            }
        }, '删除')]);
    }


    export default {
        computed: {
            ...mapState({
                'merCustList': state => state.merCust.merCustList,
                'totalPage': state => state.merCust.totalPage,
                'pageNumber': state => state.merCust.pageNumber,
                'total': state => state.merCust.totalRow,
                'merCust': state => state.merCust.merCust,
            })
        },
        methods: {

            del(i) {
                let vm = this;
                this.$store.dispatch('merCust_del', {id: i}).then((res) => {
                    setTimeout(vm.search, 1000)
                    //vm.search()
                })
            },


            search(pn) {
                this.$store.dispatch('merCust_list', {search: this.searchKey, pn: pn})
            },

        },
        mounted() {
            //页面加载时或数据方法
            this.$store.dispatch('merCust_list')

        },

        data() {
            return {
                self: this,
                searchKey: '',
                modalTitle: '客户详细信息',
                merCustModal:false,
                tableColums: [
                    {
                        title: '客户名称',
                        key: 'custName',
                        align: 'center',
                    },
                    {
                        title:'所属商户',
                        align: 'center',
                        render: (h, param) => {
                                return param.row.merchantInfo.merchantName
                        }
                    },
                    {
                        title:'商户编号',
                        align: 'center',
                        render: (h, param) => {
                            return param.row.merchantInfo.merchantNo
                        }
                    },

                    {
                        title: '身份证号',
                        key: 'cardID',
                        align: 'center',
                    },
                    {
                        title: '银行预留手机号',
                        key: 'mobileBank',
                        align: 'center',
                    },
                    {
                        title: '银行卡卡号',
                        key: 'bankcardNo',
                        align: 'center',
                    },


                    {
                        title: '创建时间',
                        key: 'catTxt',
                        align: 'center',
                    },


                    {
                        title: '操作',
                        key: 'action',
                        width: 180,
                        align: 'center',
                        render: (h, param) => {
                            if (!param.row.dAt) {
                                return h('div', [
                                    delBtn(this, h, param),
                                ]);
                            }
                        }
                    }

                ]
            }
        }

    }
</script>
<style lang="less">
    @import '../../../styles/common.less';
</style>