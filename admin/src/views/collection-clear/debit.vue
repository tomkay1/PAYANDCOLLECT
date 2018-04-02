<template>
    <div>
    <Tabs type="card">


        <TabPane label="出账处理" style="min-height: 400px">
            <Card>
                <Row>
                    <Col span="20"  align="right">
                    <Select v-model="chargeOff" style="width:200px" :clearable="true">
                        <Option v-for="item in chargetOffData" :value="item.value" :key="item.value">{{item.label}}
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
                    </Col>
                    <Col  span="4"  align="left">
                    <span  style="margin: 0 0px;">
                        <Upload :action="env+'/cc/batchDebit'" :format="['xls','xlsx']"
                                :on-success="handleSuccess" :on-error="handleError" :before-upload="handleBeforUpload"
                                :show-upload-list="false" :on-format-error="handleFormatError">
                            <Button type="primary" icon="ios-upload">出账文件上传</Button>
                        </Upload>
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
    <Modal
            v-model="debitModal"
            @on-visible-change="vChange"
            :mask-closable="false"
    >
        <p slot="header">
            <Icon type="information-circled"></Icon>
            <span>{{modalTitle}}</span>
        </p>
        <Form ref="formValidate" :label-width="80" :model="collectionClear" :rules="ruleValidate">
            <FormItem label="商户编号" >
                <Input v-model="collectionClear.merNO"  placeholder="请输入..." style="width: 300px" disabled></Input>
            </FormItem>
            <FormItem label="商户名称" >
                <Input v-model="collectionClear.merName" placeholder="请输入..." style="width: 300px" disabled></Input>
            </FormItem>
            <FormItem label="出账金额" prop="amountOff">
                <Input v-model="collectionClear.amountOff" disabled placeholder="请输入..." style="width: 300px"></Input>
            </FormItem>
            <FormItem label="出账时间" prop="chargeAt">
                <DatePicker type="datetime" placeholder="出账时间" style="width: 300px" v-model="collectionClear.chargeAt"
                            format="yyyy-MM-dd HH:mm:ss" :clearable="false"></DatePicker>
            </FormItem>
            <FormItem label="出账凭证号" prop="chargeOffTradeNo">
                <Input v-model="collectionClear.chargeOffTradeNo" placeholder="请输入..." style="width: 300px"></Input>
            </FormItem>
        </Form>
        <div slot="footer">
            <Button type="success" :loading="modalLoading" @click="save">保存</Button>
            <Button type="error" @click="debitModal=false">关闭</Button>
        </div>
    </Modal>
    </div>
</template>

<script>
    import {mapState} from 'vuex'
    import dateKit from '../../libs/date'
    import Input from "iview/src/components/input/input";
    import consts from '../../libs/consts'

    export default {

        computed: {
            ...mapState({
                'ccList': state => state.cc.ccList,
                'totalPage_cc': state => state.cc.totalPage_cc,
                'total_cc': state => state.cc.totalRow_cc,
                'pageNumber_cc': state => state.cc.pageNumber_cc,
                'collectionClear': state => state.cc.collectionClear,
            })
        },
        data() {
            const openDebitDialog=(vm,h,param)=>{
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
                            vm.debit(param.row)
                        }
                    }
                }, '出账处理');
            }
            return {
                now: new Date(),
                self: this,
                merNO: '',
                env:consts.env,
                debitModal:false,
                modalLoading:false,
                chargeOff: '',
                clearDate: '',
                modalTitle:'出账信息录入',
                bTime_cc: new Date(),
                eTime_cc: new Date(),
                ruleValidate: {
                    amountOff: [
                        {required: true, message: '出账金额必填', trigger: 'blur'},
                        {
                            type: 'string',
                            message: '请填写正确的出账金额格式，支持到小数点后2位',
                            pattern: /^(^[1-9](\d+)?(\.\d{1,2})?$)|(^(0){1}$)|(^\d\.\d{1,2}?$)$/,
                            trigger: 'blur'
                        }
                    ],
                    chargeAt: [
                        {type:'date',required: true, message: '出账时间', trigger: 'change'},
                    ],
                    chargeOffTradeNo: [
                        {required: true, message: '出账凭证号', max: 50, trigger: 'blur'},

                    ]
                },
                chargetOffData: [
                {
                    value: '0',
                    label: '已出账'
                },
                {
                    value: '1',
                    label: '未出账'
                }
                ],
                tableColums_cc: [

                    {
                        title: '商户号',
                        key: 'merNO',
                        fixed: 'left',
                        width:100
                    },
                    {
                        title: '商户名',
                        key: 'merName',
                        fixed: 'left',
                        width:100
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
                        key: 'accountFee',
                    },
                    {
                        title: '交易抵扣手续费金额',
                        key: 'tradeFee',
                    },
                    {
                        title: '出账金额',
                        key: 'chargeOff',
                    },
                    {
                        title: '出账时间',
                        key: 'chargeAtTxt',
                    },
                    {
                        title: '出账单据流水号',
                        key: 'chargeAOffTradeNo',
                    },

                    {
                        title: '操作',
                        key: 'action',
                        fixed: 'right',
                        width: 200,
                        align: 'center',
                        render: (h, param) =>{
                                        return h('div', [
                                            this.openDebitDialog(this,h,param),
                                        ]);

                        }
                    }

                ]
            }
        },
        methods: {
            vChange(b){
                if (!b) {
                    this.$refs['formValidate'].resetFields()
                    this.modalLoading = false;
                }
            },
            search_cc() {
                let param = {
                    'bTime': dateKit.formatDate(this.bTime_cc, 'yyyy-MM-dd'),
                    'eTime': dateKit.formatDate(this.eTime_cc, 'yyyy-MM-dd'),
                    merNO: this.merNO,
                    chargetOff: this.chargeOff
                }
                this.$store.dispatch("cc_list", param);
            },

            debit(obj){
                this.$store.commit('set_collectionClear',obj);
                this.debitModal=true;

            },
            batchDebit(){
                this.debitModal=true
            },
            save(){
                let vm = this;
                this.modalLoading = true;
                this.$refs['formValidate'].validate((valid) => {
                    if (valid) {
                        this.$store.dispatch('debit','').then((res) => {
                            if (res && res == 'success') {
                                vm.debitModal = false;
                                vm.search_cc()
                            } else {
                                this.modalLoading = false;
                            }
                        })
                    } else {
                        this.modalLoading = false;
                    }
                })
            },
            handleFormatError(file){
                this.$store.commit('upadteSpinshow',false);
                this.$Notice.warning({
                    title: '上传文件格式错误',
                    desc: '文件 ' + file.name + ' 格式不正确, 请选择 xls 或者 xlsx.'
                });
            },
            handleSuccess(response, file, fileList){
                this.$store.commit('upadteSpinshow',false);
                if(response&&response.resCode&&response.resCode=='success'){
                    this.$Message.success({
                        content: response.resMsg,
                        duration: 3,
                        closable: true
                    });
                    this.search_cc();
                }else if(response&&response.resCode&&response.resCode=='fail'){
                    this.$Message.error({
                        content: response.resMsg,
                        duration: 10,
                        closable: true
                    });
                }
            },
            handleError(error, file, fileList){
                this.$store.commit('upadteSpinshow',false);
                this.$Message.error('网络错误，请稍后再重试');
            },
            handleBeforUpload(){
                this.$store.commit('upadteSpinshow',true);
            },
        },
        components: {Input},
        mounted() {
        }

    }
</script>

<style lang="less">
    @import '../../styles/common.less';
</style>