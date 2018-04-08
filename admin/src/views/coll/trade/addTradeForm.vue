<template>
    <div>
        <Modal v-model="initiateTradeModal" @on-visible-change="vChange" :mask-closable="false" width="500">
            <p slot="header">
                <Icon type="information-circled"></Icon>
                <span>{{modalTitle}}</span>
            </p>

            <Form ref="formValidate" :label-width="80" :model="trade" :rules="ruleValidate">
                <FormItem label="业务类型" prop="bussType">
                    <Select v-model="trade.bussType">
                        <Option v-for="item in bussTypeList" :value="item.value" :key="item.value">{{ item.label }}</Option>
                    </Select>
                </FormItem>
                <FormItem label="客户" prop="custID">
                    <Select ref="merCustSelect" placeholder="输入 姓名/身份证号/手机号/卡号 进行搜索..." v-model="trade.custID" clearable filterable remote :remote-method="selectCustID"
                        :loading="selectCustIdLoading">
                        <Option v-for="(option, index) in custIDOptions" :value="option.id" :key="index" :label="option.custName">
                            <div style="margin: 5px auto;">
                                <div>
                                    <span style="font-weight:bold; margin-right: 10px;">{{option.custName}}</span>
                                    <span style="color:#ccc">{{option.cardID}}</span>
                                </div>
                                <div>
                                    <span style="margin-right: 10px;">{{option.bankcardNo}}</span>
                                    <span v-if="option.cardBin.cardName" style="color:#ccc">{{option.cardBin.cardName}}</span>
                                </div>
                            </div>
                        </Option>
                    </Select>
                </FormItem>
                <FormItem label="金额" prop="txnAmt" required>
                    <Input v-model="trade.txnAmt" placeholder="请输入..."></Input>
                    <!-- <InputNumber :min='0.01' v-model="trade.txnAmt" :step="1" style="width: 100%;"></InputNumber> -->
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
    import dateKit from '../../../libs/date'

    export default {
        name: 'initiateTradeModal',
        props: [
            'pageSize','finalCode','bTime','eTime','searchKey'
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
                this.$axios.post('/coll/trade/getMerCust').then((res) => {
                    this.custIDOptionsList = res;
                });
            },
            close() {
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
                this.modalLoading = true;
                this.$refs['formValidate'].validate((valid) => {
                    if (valid) {
                        this.$store.dispatch('trade_save').then((res) => {
                            let param = {
                                finalCode: this.finalCode,
                                'bTime': dateKit.formatDate(this.bTime, 'yyyy-MM-dd'),
                                'eTime': dateKit.formatDate(this.eTime, 'yyyy-MM-dd'),
                                search: this.searchKey,
                                ps: this.pageSize
                            }
                            this.$store.dispatch('trade_list', param)
                            this.close()
                        })
                    } else {
                        this.modalLoading = false;
                    }
                })
            },
            reset() {
                this.$store.dispatch('collTrade_set', {})
            },
            selectCustID(query) {
                if (query !== '') {
                    this.selectCustIdLoading = true;
                    setTimeout(() => {
                        this.selectCustIdLoading = false;
                        this.custIDOptions = this.custIDOptionsList.filter(item =>
                            (item.custName.toLowerCase().indexOf(query.toLowerCase()) > -1) ||
                            (item.cardID.toLowerCase().indexOf(query.toLowerCase()) > -1) ||
                            (item.mobileBank.toLowerCase().indexOf(query.toLowerCase()) > -1) ||
                            (item.bankcardNo.toLowerCase().indexOf(query.toLowerCase()) > -1));
                    }, 200);
                } else {
                    this.custIDOptions = [];
                }
            }
        },
        data() {
            return {
                self: this,
                initiateTradeModal: false,
                modalTitle: '发起交易',
                modalLoading: false,
                custIDOptions: [],
                selectCustIdLoading: false,
                custIDOptionsList: [],
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
                        { type: 'string', required: true, message: '请选择业务类型', trigger: 'blur' },
                    ],
                    custID: [
                        { type: 'number', required: true, message: '请选择客户', trigger: 'blur' },
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
    @import '../../../styles/common.less';
</style>