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
                    <Col span="24" >
                    <Input v-model="searchKey" placeholder="输入客户姓名/手机号" style="width: 200px"/>
                    <Input v-model="searchKey1" placeholder="商户编号" style="width: 200px"/>
                    <span @click="search" style="margin: 0 10px;">
                        <Button type="primary" icon="search">搜索</Button></span>
                    </Col>
                </Row>
                <Row class="margin-top-10">
                    <Table :context="self" border :data="merCustList" :columns="tableColums" stripe></Table>
                </Row>
                <Row class="margin-top-10">
                    <Col span="24" align="right">
                    <Page :total="total" :current="pageNumber" @on-change="search" show-total show-sizer></Page>
                    </Col>
                </Row>
            </Card>
            </Col>
        </Row>
        <Modal  v-model="merCustInfoModal" :mask-closable="true" >
            <p slot="header">
                <Icon type="information-circled"></Icon>
                <span>{{modalTitle}}</span>
            </p>
            <Row>
                <Col span="8" align="right">
                <div class="span-lb">客户名称：</div>
                </Col>
                <Col span="16">
                <div class="span-lc">{{merCust.custName}}</div>
                </Col>
            </Row>
            <Row>
                <Col span="8" align="right">
                <div class="span-lb">所属商户：</div>
                </Col>
                <Col span="16">
                <div class="span-lc" v-if="merCustInfoModal">{{merCust.merchantInfo.merchantName}}</div>
                </Col>
            </Row>
            <Row>
                <Col span="8" align="right">
                <div class="span-lb">商户编号：</div>
                </Col>
                <Col span="16">
                <div class="span-lc" v-if="merCustInfoModal">{{merCust.merchantInfo.merchantNo}}</div>
                </Col>
            </Row>
            <Row>
                <Col span="8" align="right">
                <div class="span-lb">身份证号码：</div>
                </Col>
                <Col span="16">
                <div class="span-lc">{{merCust.cardID}}</div>
                </Col>
            </Row>
            <Row>
                <Col span="8" align="right">
                <div class="span-lb">银行预留手机号：</div>
                </Col>
                <Col span="16">
                <div class="span-lc">{{merCust.mobileBank}}</div>
                </Col>
            </Row>
            <Row>
                <Col span="8" align="right">
                <div class="span-lb">银行卡号：</div>
                </Col>
                <Col span="16">
                <div class="span-lc">{{merCust.bankcardNo}}</div>
                </Col>
            </Row>
            <Row>
                <Col span="8" align="right">
                <div class="span-lb">创建时间：</div>
                </Col>
                <Col span="16">
                <div class="span-lc">{{merCust.catTxt}}</div>
                </Col>
            </Row>
            <!--<img :src="urlCard" v-show="showImgCard" width="300" >-->
            <Row>
                <Col span="8" align="right">
                <div class="span-lb">本人现场照片：</div>
                </Col>
                <Col span="16">
                <div class="span-lc"><img v-if="merCustInfoModal" width="300" :src="imgUrl+merCust.selfImg"/></div>
                </Col>
            </Row>
            <Row>
                <Col span="8" align="right">
                <div class="span-lb">身份证正面照片：</div>
                </Col>
                <Col span="16">
                <div class="span-lc"><img v-if="merCustInfoModal" width="300" :src="imgUrl+merCust.cardImgZ"/></div>
                </Col>
            </Row>
            <div slot="footer">
                <Button type="error" @click="merCustInfoModal=false">关闭</Button>
            </div>
        </Modal>
    </div>

</template>

<script>
    import {mapState} from 'vuex'
    import consts from '../../../libs/consts'
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

    const infoBtn = (vm, h, param) => {
        return h('Button', {
            props: {
                type: 'primary',
                size: 'small'
            },
            style: {
                marginRight: '5px'
            },
            on: {
                click: () => {
                    vm.info(param.row)
                }
            }
        }, '查看');
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
            info(i) {
                this.$store.commit('merCust_info', i)
                this.merCustInfoModal =true

            },


            search(pn) {
                this.$store.dispatch('merCust_list', {search: this.searchKey,search1: this.searchKey1, pn: pn})
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
                searchKey1: '',
                modalTitle: '客户详细信息',
                merCustInfoModal: false,
                imgUrl:consts.devLocation+"/cmn/act04?picid=",
                tableColums: [
                    {
                        title: '客户名称',
                        key: 'custName',
                        align: 'center',
                    },
                    {
                        title: '所属商户',
                        align: 'center',
                        render: (h, param) => {
                            return param.row.merchantInfo.merchantName
                        }
                    },
                    {
                        title: '商户编号',
                        align: 'center',
                        render: (h, param) => {
                            return param.row.merchantInfo.merchantNo
                        }
                    },

                    // {
                    //     title: '身份证号',
                    //     key: 'cardID',
                    //     align: 'center',
                    // },
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
                        width: 130,
                        align: 'center',
                        render: (h, param) => {
                            if (!param.row.dAt) {
                                return h('div', [
                                    infoBtn(this, h, param),
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

    .span-lb {
        margin: 5px 0px;
        padding: 5px 5px;
        font-weight: bold;

    }

    .span-lc {
        margin: 5px 0px;
        padding: 5px 5px;
    }
</style>