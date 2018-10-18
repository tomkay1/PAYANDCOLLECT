<template>
    <div>
        <Modal v-model="addAdvanceModal" @on-visible-change="vChange" :mask-closable="false" width="500">
            <p slot="header">
                <Icon type="information-circled"></Icon>
                <span>{{modalTitle}}</span>
            </p>
            <Form ref="formValidate" :label-width="120" :model="unionpayAdvance" :rules="ruleValidate">
                <FormItem label="商户" prop="merCode">
                    <Select v-model="unionpayAdvance.merCode">
                        <Option v-for="item in merCodeList" :value="item.value" :key="item.value">{{ item.label }}</Option>
                    </Select>
                </FormItem>
                <FormItem label="姓名" prop="customerNm">
                    <Input v-model="unionpayAdvance.customerNm" placeholder="请输入..."></Input>
                </FormItem>
                <FormItem label="卡号" prop="accNo">
                    <Input v-model="unionpayAdvance.accNo" placeholder="请输入..."></Input>
                </FormItem>
                <FormItem label="证件类型" prop="certifTp">
                    <Select v-model="unionpayAdvance.certifTp">
                        <Option value="01" label="身份证">身份证</Option>
                    </Select>
                </FormItem>
                <FormItem label="证件号码" prop="certifId">
                    <Input v-model="unionpayAdvance.certifId" placeholder="请输入..."></Input>
                </FormItem>
                <FormItem label="金额" prop="txnAmt" required>
                    <Input v-model="unionpayAdvance.txnAmt" placeholder="请输入..."></Input>
                </FormItem>
            </Form>
            <div slot="footer">
                <Button type="success" :loading="modalLoading" @click="save">发起代付</Button>
                <Button @click="reset">重置</Button>
                <Button type="error" @click="addAdvanceModal=false">关闭</Button>
            </div>
        </Modal>

        <Modal v-model="addConfirmModal" :closable="false" :mask-closable="false">
                <p slot="header" style="color:#f60;text-align:center">
                    <Icon type="information-circled"></Icon>
                    <span>发起确认</span>
                </p>
                <div style="height: 200px;font-size: 1.2em; padding-left:30px;">
                    <div style="margin-top: 20px;">
                        <span style="width: 5em; display: inline-block; text-align: right;">姓名</span>
                        <span style="font-size: 1.8em; margin-left:0.8em;">
                            {{unionpayAdvance.customerNm}}
                        </span>
                    </div>
                    <div style="margin-top: 20px;">
                        <span style="width: 5em; display: inline-block; text-align: right;">卡号</span>
                        <span style="font-size: 1.8em; margin-left:0.8em;">
                            {{unionpayAdvance.accNo}}
                        </span>
                    </div>
                    <div style="margin-top: 20px;">
                        <span style="width: 5em; display: inline-block; text-align: right;">交易金额</span>
                        <span style="font-size: 1.8em; color: rgb(220, 147, 135); margin-left:0.8em;">
                            <span>{{unionpayAdvance.txnAmt}}</span>
                        </span>
                    </div>
                </div>
                <div slot="footer">
                    <Button type="ghost" @click="cancelConfirm">取消</Button>
                    <Button type="primary" ref="confirmButton" :disabled="disableConfirmButton" @click="confirmed">确认</Button>
                </div>
            </Modal>
    </div>
</template>


<script>
    import { mapState } from 'vuex'
    import dateKit from '../../libs/date'

    export default {
        name: 'addAdvanceModal',
        props: [
            'pageSize', 'finalCode', 'bTime', 'eTime', 'searchKey'
        ],
        computed: {
            ...mapState({
                'unionpayAdvance': state => state.unionpayAdvance.unionpayAdvance,
            }),
            disableConfirmButton: function(){
                return this.validateResult.errorMessage ?true :false;
            }
        },
        methods: {
            open() {
                this.addAdvanceModal = true;
                this.$store.commit('unionpay_advance_set', {});
                this.modalLoading = false;
            },
            close(){
                this.addAdvanceModal = false;
                this.$store.commit('unionpay_advance_set', {});
                this.modalLoading = false;
            },
            vChange(b) {
                if (!b) {
                    this.$refs['formValidate'].resetFields()
                }
            },
            confirmed() {
                this.addConfirmModal = false;
                this.$store.dispatch('advance_save', this.unionpayAdvance).then((res) => {
                    let param = {
                        finalCode: this.finalCode,
                        'bTime': dateKit.formatDate(this.bTime, 'yyyy-MM-dd'),
                        'eTime': dateKit.formatDate(this.eTime, 'yyyy-MM-dd'),
                        search: this.searchKey,
                        ps: this.pageSize,
                    }
                    this.$store.dispatch('get_advance_trade_list', param)
                    this.close()
                })
            },
            cancelConfirm() {
                this.addConfirmModal = false;
                this.modalLoading = false;
            },
            save() {
                let vm = this;
                this.modalLoading = true;
                this.$refs['formValidate'].validate((valid) => {
                    if (valid) {
                        this.$axios.post('/advance/trade/validate', this.unionpayAdvance).then((res) => {
                            this.validateResult = res
                            if (this.validateResult.errorMessage) {
                                this.modalLoading = false;
                                this.$Message.error({
                                    content: this.validateResult.errorMessage,
                                    duration: 5
                                });
                            }else{
                                this.addConfirmModal = true;
                            }
                        });
                    } else {
                        this.modalLoading = false;
                    }
                })
            },
            reset() {
                this.unionpayAdvance={}
            }
        },
        data() {
            return {
                self: this,
                addAdvanceModal: false,
                modalTitle: '发起代付',
                modalLoading: false,
                addConfirmModal: false,
                validateResult: {},
                merCodeList: [
                    {
                        value: '0',
                        label: '银盛代付商户'
                    },
                    {
                        value: '1',
                        label: '测试商户'
                    }
                ],
                ruleValidate: {
                    merCode: [
                        { type: 'string', required: true, message: '商户类型不能为空', trigger: 'blur' },
                    ],
                    customerNm: [
                        { type: 'string', required: true, message: '姓名不能为空', trigger: 'blur' },
                        { type: 'string', max: 50, message: '姓名长度不能超过50', trigger: 'blur' }
                    ],
                    accNo: [
                        { type: 'string', required: true, message: '卡号不能为空', trigger: 'blur' },
                        { type: 'string', max: 50, message: '卡号长度不能超过50', trigger: 'blur' }
                    ],
                    certifTp: [
                        { type: 'string', required: true, message: '证件类型不能为空', trigger: 'blur' },
                    ],
                    certifId: [
                        { type: 'string', required: true, message: '证件号码不能为空', trigger: 'blur' },
                        { type: 'string', max: 18, message: '证件号码长度不能超过18', trigger: 'blur' }
                    ],
                    txnAmt: [
                        {
                            type: 'string', required: true, message: '请输入有效金额', trigger: 'blur'
                        },
                    ]
                }
            }
        }
    }
</script>

<style lang="less">
    @import '../../styles/common.less';
</style>