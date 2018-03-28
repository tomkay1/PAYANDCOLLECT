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
        <Modal v-model="merInfoModal" @on-visible-change="vChange" :mask-closable="false" >
            <p slot="header">
                <Icon type="information-circled"></Icon>
                <span>{{modalTitle}}</span>
            </p>
            <Form ref="formValidate" :label-width="150" :model="merInfo" :rules="ruleValidate">
                <FormItem label="商户名称" prop="merchantName">
                    <Input v-model="merInfo.merchantName"  placeholder="请输入..." style="width: 300px"/>
                </FormItem>
                <FormItem label="商户类型" prop="merchantType">
                    <Select v-model="merInfo.merchantType" style="width:300px">
                        <Option  v-for="item in merchantTypeList" :value="item.text"  :key="item.text">{{ item.title }}</Option>
                    </Select>
                    {{merInfo.merchantType}}
                </FormItem>
                <FormItem label="负责人名称" prop="perName">
                    <Input v-model="merInfo.perName" placeholder="请输入..." style="width: 300px"/>
                </FormItem>
                <FormItem label="身份证号码" prop="cardID">
                    <Input v-model="merInfo.cardID" placeholder="请输入..." style="width: 300px"/>
                </FormItem>
                <FormItem label="负责人手机号" prop="mobile">
                    <Input v-model="merInfo.mobile" placeholder="请输入..." style="width: 300px"/>
                </FormItem>
                 <FormItem label="负责人Email" prop="email">
                     <Input v-model="merInfo.email" placeholder="请输入..." style="width: 300px"/>
                 </FormItem>
                 <FormItem label="负责人联系地址" prop="address">
                     <Input v-model="merInfo.address" placeholder="请输入..." style="width: 300px"/>
                 </FormItem>
                 <FormItem label="备用联系地址1" prop="mobile1">
                     <Input v-model="merInfo.mobile1" placeholder="请输入..." style="width: 300px"/>
                 </FormItem>
                 <FormItem label="备用联系地址2" prop="mobile2">
                     <Input v-model="merInfo.mobile2" placeholder="请输入..." style="width: 300px"/>
                 </FormItem>
                 <FormItem label="预存手续费余额" prop="feeAmount">
                     <Input v-model="merInfo.feeAmount" placeholder="请输入..." style="width: 300px"/>
                 </FormItem>
                 <FormItem label="手持身份证照片" prop="cardImg">
                     <!--<Input v-model="merInfo.cardImg" placeholder="请输入..." style="width: 300px"/>-->

                     <Upload
                             ref="upload"
                             :show-upload-list="false"
                             :on-success="handleSuccessCard"
                             :format="['jpg','jpeg','png']"
                             :max-size="4096"
                             :on-format-error="handleFormatError"
                             :on-exceeded-size="handleMaxSize"
                             type="drag"
                             :action="uploadAction"
                             style="display: inline-block;width:300px;">
                         点击上传手持身份证照片

                         <img :src="urlCard" v-show="showImgCard" width="300" >
                     </Upload>
                 </FormItem>



                 <FormItem label="身份证正面" prop="cardZ">
                     <Upload
                             ref="upload"
                             :show-upload-list="false"
                             :on-success="handleSuccessCardZ"
                             :format="['jpg','jpeg','png']"
                             :max-size="4096"
                             :on-format-error="handleFormatError"
                             :on-exceeded-size="handleMaxSize"
                             type="drag"
                             :action="uploadAction"
                             style="display: inline-block;width:300px;">

                         点击上传身份证正面
                         <img :src="urlCardZ" v-show="showImgCardZ" width="300"  >
                     </Upload>
                 </FormItem>
                 <FormItem label="身份证背面" prop="cardF">
                     <Upload
                             ref="upload"
                             :show-upload-list="false"
                             :on-success="handleSuccessCardF"
                             :format="['jpg','jpeg','png']"
                             :max-size="4096"
                             :on-format-error="handleFormatError"
                             :on-exceeded-size="handleMaxSize"
                             type="drag"
                             :action="uploadAction"
                             style="display: inline-block;width:300px;">

                         点击上传身份证背面
                         <img :src="urlCardF" v-show="showImgCardF" width="300" >
                     </Upload>
                 </FormItem>
            </Form>
            <div slot="footer">
                <Button type="success" :loading="modalLoading" @click="save">保存</Button>
                <Button type="error" @click="merInfoModal=false">关闭</Button>
            </div>
        </Modal>
    </div>

</template>

<script>
    import {mapState} from 'vuex'
    import consts from '../../../libs/consts'
const delBtn=(vm,h,param)=>{
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
                title: '您确定要禁用吗？'
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
                title: '您确定要激活吗？'
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
                'merchantTypeList' :state => state.merInfo.merchantTypeList,
            })
        },
        methods: {
            add(){
                this.isAdd=true
                this.modalTitle="新增商户"
                let vm = this;
                    vm.merInfoModal = true;
                    this.$store.commit('merInfo_reset',{"merchantType+''":'',merchantType:''})
            },
            del(i){
                let vm=this;
                this.$store.dispatch('merInfo_del',{id:i}).then((res)=>{
                    setTimeout(vm.search,1000)
                    //vm.search()
                })
            },
            edit(merInfo){
                this.modalTitle="修改商户"
                this.isAdd=false;
                let vm=this

                vm.$store.commit('merInfo_reset',merInfo)
                vm.merInfoModal = true

            },
            stop(i){
                let vm=this;
                this.$store.dispatch('merInfo_stop',{id:i}).then((res)=>{
                    setTimeout(vm.search,1000)
                        //vm.search()
                    //this.$store.dispatch('merInfo_list',{search:this.searchKey,pn:pn})
                })
            },
            active(i){
                let vm=this;
                this.$store.dispatch('merInfo_active',{id:i}).then((res)=>{
                    setTimeout(vm.search,1000)
                    //vm.search()
                    //this.$store.dispatch('merInfo_list',{search:this.searchKey,pn:pn})
                })
            },
            save(){
                let vm = this;
                this.modalLoading = true;
                this.$refs['formValidate'].validate((valid) => {
                    if (valid) {
                        let action='save';
                        if(!vm.isAdd)
                            action='update';
                        this.$store.dispatch('merInfo_save',action).then((res) => {
                            if (res && res == 'success') {
                                vm.merInfoModal = false;
                                this.$store.dispatch('merInfo_list')
                            } else {
                                this.modalLoading = false;
                            }
                        })
                    } else {
                        this.modalLoading = false;
                    }
                })
            },
            vChange(b){
                if (!b) {
                    this.$refs['formValidate'].resetFields()
                    this.modalLoading = false;
                    this.showImgCard =false;
                    this.showImgCardF =false;
                    this.showImgCardZ =false;
                    this.urlCard='';
                    this.urlCardF='';
                    this.urlCardZ='';
                }
            },
            search(pn){
                this.$store.dispatch('merInfo_list',{search:this.searchKey,pn:pn})
            },

            refresh(){
                this.$store.dispatch('merInfo_list',{search:this.searchKey})
            },
            handleSuccessCard (res, file) {
                this.merInfo.cardImg =res.resData;
                this.urlCard = consts.devLocation+"/cmn/act04?picid="+this.merInfo.cardImg
                this.showImgCard=true;

            },
            handleSuccessCardZ (res, file) {
                this.merInfo.cardZ =res.resData;
                this.urlCardZ = consts.devLocation+"/cmn/act04?picid="+this.merInfo.cardZ
                this.showImgCardZ=true;

            },
            handleSuccessCardF (res, file) {
                this.merInfo.cardF =res.resData;
                this.urlCardF = consts.devLocation+"/cmn/act04?picid="+this.merInfo.cardF
                this.showImgCardF=true;

            },
            handleFormatError (file) {
                this.$Notice.warning({
                    title: '文件格式不正确',
                    desc: '文件 ' + file.name + ' 格式不正确，请上传 jpg 或 png 格式的图片。'
                });
            },
            handleMaxSize (file) {
                this.$Notice.warning({
                    title: '超出文件大小限制',
                    desc: '文件 ' + file.name + ' 太大，不能超过 4M。'
                });
            },

        },
        mounted () {
            //页面加载时或数据方法
           this.$store.dispatch('merInfo_list')

        },
        data () {
            return {
                selected: 'C',   // 比如想要默认选中为 Three 那么就把他设置为C
                options: [
                    { text: 'One', value: 'A' },  //每个选项里面就不用在多一个selected 了
                    { text: 'Two', value: 'B' },
                    { text: 'Three', value: 'C' }],
                showImgCard: false,
                showImgCardZ: false,
                showImgCardF: false,
                urlCard:'',
                urlCardZ:'',
                urlCardF:'',
                self: this,
                searchKey: '',
                merInfoModal: false,
                isAdd:true,
                modalTitle: '新增用户',
                uploadAction:consts.env+'/cmn/act03',
                modalLoading: false,
                ruleValidate: {
                    merchantName: [
                        {type: 'string', required: true, message: '商户名称不能为空', trigger: 'blur'},
                        {type: 'string', max: 100, message: '用户名长度不能超过100', trigger: 'blur'}
                    ],
                    merchantType: [
                        {type: 'string', required: true, message: '请选择商户类型', trigger: 'blur'}
                    ],
                    perName: [
                        {required: true, message: '负责人名称不能为空', trigger: 'blur'},
                        {type: 'string', message: '负责人名称长度不能超过50', max: 50, trigger: 'blur'}
                    ],
                    mobile: [
                        {required: true, message: '负责人手机号不能为空', trigger: 'blur'},
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
                    ],
                    cardID: [
                        {required: true, message: '身份证号不能为空',  trigger: 'blur'},
                        {type: 'string', max: 50, message: '身份证号长度不能超过50', trigger: 'blur'}
                    ],
                },



                tableColums: [

                    {
                        title: '商户名称',
                        key: 'merchantName',
                    },
                    {
                        title: '商户编号',
                        key: 'merchantNo',
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