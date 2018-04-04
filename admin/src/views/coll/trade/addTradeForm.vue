<template>
    <div>
        <Modal v-model="initiateTradeModal" @on-visible-change="vChange" :mask-closable="false" width="500">
            <p slot="header">
                <Icon type="information-circled"></Icon>
                <span>{{modalTitle}}</span>
            </p>
            
            <Form ref="formValidate" :label-width="120" :model="trade" :rules="ruleValidate">
                <FormItem label="业务类型" prop="bussType">
                    <Select v-model="trade.bussType">
                        <Option v-for="item in bussTypeList" :value="item.value" :key="item.value">{{ item.label }}</Option>
                    </Select>
                </FormItem>
                <FormItem label="卡号" prop="accNo">
                    <Input v-model="trade.accNo" placeholder="请输入..."></Input>
                </FormItem>
                <FormItem label="金额" prop="txnAmt">
                    <Input v-model="trade.txnAmt" placeholder="请输入..."></Input>
                </FormItem>
                
            </Form>
            <div slot="footer">
                <Button type="success" :loading="modalLoading" @click="save">发起</Button>
                <Button @click="reset">重置</Button>
                <Button type="error" @click="initiateTradeModal=false">关闭</Button>
            </div>
        </Modal>

    </div>
</template>


<script>
    import { mapState } from 'vuex'

    export default {
        name: 'initiateTradeModal',
        props: [
            'pageSize'
        ],
        computed: {
            ...mapState({
                'trade': state => state.collTrade.collTrade,
            })
        },
        methods: {
            open() {
                this.initiateTradeModal = true;
                this.$store.commit('collTrade_set', {});
                this.modalLoading = false;
            },
            close(){
                this.initiateTradeModal = false;
                this.$store.commit('collTrade_set', {});
                this.modalLoading = false;
            },
            vChange(b) {
                if (!b) {
                    this.$refs['formValidate'].resetFields()
                }
            },
            save() {
                let vm = this;
                this.modalLoading = true;
                this.$refs['formValidate'].validate((valid) => {
                    if (valid) {
                        this.$store.dispatch('trade_save').then((res) => {
                            this.$store.dispatch('trade_list', { ps: this.pageSize })
                            this.close()
                        })
                    } else {
                        this.modalLoading = false;
                    }
                })
            },
            reset() {
                this.$store.dispatch('collTrade_set', {})
            }

        },
        data() {
            return {
                self: this,
                initiateTradeModal: false,
                modalTitle: '发起交易',
                modalLoading: false,
                bussTypeList: [
                    {
                        value: '1',
                        label: '加急'
                    },
                    {
                        value: '2',
                        label: '批量'
                    },
                ],
                ruleValidate: {
                    bussType: [
                        { type: 'string', required: true, message: '业务类型不能为空', trigger: 'blur' },
                    ],
                    accNo: [
                        { type: 'string', required: true, message: '卡号不能为空', trigger: 'blur' },
                        { type: 'string', max: 50, message: '卡号长度不能超过50', trigger: 'blur' }
                    ],
                    txnAmt: [
                        { type: 'string', required: true, message: '金额不能为空', trigger: 'blur' },
                    ]
                }
            }
        }
    }

</script>

<style lang="less">
    @import '../../../styles/common.less';
</style>