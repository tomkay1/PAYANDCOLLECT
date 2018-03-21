<template>
    <div>
        <Row>
            <Col span="24">
            <Card>
                <p slot="title">
                    <Icon type="help-buoy"></Icon>
                    商户列表
                </p>
                <Row>
                    <Col span="8">
                    <Button type="primary" icon="person-add" @click="add">新增商户</Button>

                    </Col>
                    <Col span="8" offset="8" align="right">
                    <Input v-model="searchKey" placeholder="请输入..." style="width: 200px"/>
                    <span @click="search" style="margin: 0 10px;">
                        <Button type="primary" icon="search">搜索</Button></span>
                    </Col>
                </Row>
                <Row class="margin-top-10">
                    <Table :context="self" border :data="merInfoList" :columns="tableColums" stripe></Table>
                </Row>
                <div style="margin: 10px;overflow: hidden">
                    <div style="float: right;">
                        <Page :total="total" :current="pageNumber" @on-change="search"  show-total show-elevator></Page>
                    </div>
                </div>
            </Card>
            </Col>
        </Row>
        <Modal v-model="userModal" @on-visible-change="vChange" :mask-closable="false" >
            <p slot="header">
                <Icon type="information-circled"></Icon>
                <span>{{modalTitle}}</span>
            </p>
            <Form ref="formValidate" :label-width="80" :model="merInfo" :rules="ruleValidate">
                <FormItem label="商户名称" prop="merchantName">
                    <Input v-model="merInfo.merchantName" :disabled="!isAdd" placeholder="请输入..." style="width: 300px"></Input>
                </FormItem>
                <FormItem label="商户类型" prop="merchantType">
                    <Input v-model="merInfo.merchantType" placeholder="请输入..." style="width: 300px"></Input>
                </FormItem>
                <FormItem label="负责人名称" prop="perName">
                    <Input v-model="merInfo.perName" placeholder="请输入..." style="width: 300px"></Input>
                </FormItem>
                <FormItem label="身份证号码" prop="cardID">
                    <Input v-model="merInfo.cardID" placeholder="请输入..." style="width: 300px"></Input>
                </FormItem>
                <FormItem label="负责人联系方式" prop="mobile">
                    <Input v-model="merInfo.mobile" placeholder="请输入..." style="width: 300px"></Input>
                </FormItem>
                 <FormItem label="负责人Email" prop="email">
                      <Input v-model="merInfo.email" placeholder="请输入..." style="width: 300px"></Input>
                 </FormItem>
                 <FormItem label="负责人联系地址" prop="address">
                      <Input v-model="merInfo.address" placeholder="请输入..." style="width: 300px"></Input>
                 </FormItem>
                 <FormItem label="负责人联系地址（备用1）" prop="mobile1">
                       <Input v-model="merInfo.mobile1" placeholder="请输入..." style="width: 300px"></Input>
                 </FormItem>
                 <FormItem label="负责人联系地址（备用2）" prop="mobile2">
                       <Input v-model="merInfo.mobile2" placeholder="请输入..." style="width: 300px"></Input>
                 </FormItem>
                 <FormItem label="预存手续费余额" prop="feeAmount">
                      <Input v-model="merInfo.feeAmount" placeholder="请输入..." style="width: 300px"></Input>
                 </FormItem>
                 <FormItem label="手持身份证照片" prop="cardImg">
                 <Input v-model="merInfo.cardImg" placeholder="请输入..." style="width: 300px"></Input>
                 </FormItem>
                 <FormItem label="身份证正面" prop="cardZ">
                 <Input v-model="merInfo.cardZ" placeholder="请输入..." style="width: 300px"></Input>
                 </FormItem>
                 <FormItem label="身份证背面" prop="cardF">
                 <Input v-model="merInfo.cardF" placeholder="请输入..." style="width: 300px"></Input>
                 </FormItem>
            </Form>
            <div slot="footer">
                <Button type="success" :loading="modalLoading" @click="save">保存</Button>
                <Button @click="reset" v-show="isAdd">重置</Button>
                <Button type="error" @click="userModal=false">关闭</Button>
            </div>
        </Modal>
    </div>

</template>

<script>
    import {mapState} from 'vuex'
const delBtn=(vm,h,param)=>{
        return h('Poptip', {
            props: {
                confirm: '',
                title: '您确定要删除这个用户信息吗？'
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

const editBtn=(vm,h,param)=>{
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
                    vm.edit(param.row)
                }
            }
        }, '编辑')
    }
const stopBtn=(vm,h,param)=>{
        return h('Poptip', {
            props: {
                confirm: '',
                title: '您确定要禁用这个用户吗？'
            },
            style: {
                marginRight: '5px'
            },
            on: {
                'on-ok': () => {
                    vm.stop(param.row.id)
                }
            }
        }, [h('Button', {
            props: {
                type: 'error',
                size: 'small'
            }
        }, '禁用')]);
    }
    const actBtn=(vm,h,param)=>{
        return h('Poptip', {
            props: {
                confirm: '',
                title: '您确定要激活这个用户吗？'
            },
            style: {
                marginRight: '5px'
            },
            on: {
                'on-ok': () => {
                    vm.active(param.row.id)
                }
            }
        }, [h('Button', {
            props: {
                type: 'success',
                size: 'small'
            }
        }, '激活')]);
    }
    export default {
        computed: {
            ...mapState({
                'merInfoList': state => state.merInfo.merInfoList,
                'totalPage': state => state.merInfo.totalPage,
                'pageNumber': state => state.merInfo.pageNumber,
                'total': state => state.merInfo.totalRow,
                'merInfo': state => state.merInfo.merInfo,
            })
        },
        methods: {
            add(){
                this.isAdd=true
                this.modalTitle="新增商户"
                let vm = this;
                    vm.userModal = true;
                    vm.$store.commit('user_reset');

            },
            save(){
                            let vm = this;
                            this.modalLoading = true;
                            this.$refs['formValidate'].validate((valid) => {
                                if (valid) {
                                    let action='save';
                                    if(!vm.isAdd)
                                        action='update';
                                    this.$store.dispatch('user_save',action).then((res) => {
                                        if (res && res == 'success') {
                                            vm.userModal = false;
                                            this.$store.dispatch('user_list')
                                        } else {
                                            this.modalLoading = false;
                                        }
                                    })
                                } else {
                                    this.modalLoading = false;
                                }
                            })
                        },
            reset(){
                            this.$refs['formValidate'].resetFields()
                        },
            vChange(b){
                if (!b) {
                    this.$refs['formValidate'].resetFields()
                    this.modalLoading = false;
                }
            },
            search(pn){
                this.$store.dispatch('merInfo_list',{search:this.searchKey,pn:pn})
            },

            refresh(){
                this.$store.dispatch('merInfo_list',{search:this.searchKey})
            },

        },
        mounted () {
            //页面加载时或数据方法
           this.$store.dispatch('merInfo_list')
        },
        data () {
            return {
                self: this,
                searchKey: '',
                userModal: false,
                isAdd:true,
                modalTitle: '新增用户',
                modalLoading: false,
                ruleValidate: {
                    loginname: [
                        {type: 'string', required: true, message: '用户名不能为空', trigger: 'blur'},
                        {type: 'string', max: 50, message: '用户名长度不能超过50', trigger: 'blur'}
                    ],
                    nickname: [
                        {required: true, message: '姓名不能为空', max: 50, trigger: 'blur'},
                        {type: 'string', message: '姓名长度不能超过50', max: 50, trigger: 'blur'}
                    ],
                    phone: [
                        {required: true, message: '手机号不能为空', max: 20, trigger: 'blur'},
                        {type: 'string', message: '请输入11位手机号', len: 11, trigger: 'blur'},
                        {
                            type: 'string',
                            message: '手机号码无效',
                            pattern: /^((13|14|15|17|18)[0-9]{1}\d{8})$/,
                            trigger: 'blur'
                        }
                    ],
                    email: [
                        {type: 'email', message: 'email格式不正确', max: 255, trigger: 'blur'},
                        {type: 'string', message: 'email长度不能超过255', max: 255, trigger: 'blur'}
                    ], idcard: [
                        {type: 'string', max: 50, message: '证件号长度不能超过50', trigger: 'blur'}
                    ], roleIds: [
                        {required: true, type: 'array', min: 1, message: '至少选择一个角色', trigger: 'change'},
                    ],
                },
                tableColums: [

                    {
                        title: '商户名称',
                        key: 'merchantName',
                    },
                    {
                        title: '负责人姓名',
                        key: 'perName',
                    },
                    {
                        title: '身份证号码',
                        key: 'cardID',
                    },
                    {
                        title: '手机',
                        key: 'mobile',
                    },

                    {
                        title: '创建时间',
                        key: 'catTxt',
                    },

                    {
                        title: '状态',
                        key: 'statusTxt',
                        width:120,
                        render:(h, param)=>{
                            if (param.row.status == '0') {
                                return h('Tag', {
                                    props: {
                                        type: 'dot',
                                        color: 'blue'
                                    },
                                }, param.row.statusTxt)
                            } else if (param.row.status == '1') {
                                return h('Tag', {
                                    props: {
                                        type: 'dot',
                                        color: 'red'
                                    },
                                }, param.row.statusTxt)
                            }
                        }
                    },
                    {
                        title: '操作',
                        key: 'action',
                        width: 180,
                        align: 'center',
                        render: (h, param) =>{
                            if (!param.row.dAt) {
                                if (param.row.status == '0') {


                                        return h('div', [
                                            editBtn(this,h,param),
                                            delBtn(this,h,param),
                                      stopBtn(this,h,param),
                                        ]);

                                } else {


                                        return h('div', [
                                            editBtn(this,h,param),
                                            delBtn(this,h,param),
                                            actBtn(this,h,param),

                                        ]);


                                }

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