

<template>
    <Row>
        <Col span="24">
            <Card>
                <p slot="title">手续费信息</p>
                <Row>
                <Col span="12" align="center">
                <div style="font-size: 13px; font-weight: bold; background-color: #ccc; padding: 5px; ;margin: 0px 1px; ">加急</div>
                <table>
                    <div v-for="item in merFeeListJ" class="feeList">
                        <tr>
                            <td width="230" align="center">

                                <span v-if="item.amountLower > '0'">{{ item.amountLower }}<</span>
                                交易金额
                                <span v-if="item.amountUpper > '0'"><={{ item.amountUpper }}</span>
                            </td>
                            <td width="80" align="center">
                                <span v-if="item.feeType === '1'">每笔{{ item.amount }}元</span>
                                <span v-else-if="item.feeType  === '2'">{{item.ratio*100}}%</span>
                            </td>

                        </tr>
                    </div>
                </table>

                </Col>
                <Col span="12" align="center">
                <div style="font-size: 13px; font-weight: bold; background-color: #ccc; padding: 5px;margin: 0px 1px;">标准</div>
                <table>
                    <div v-for="item in merFeeListB" class="feeList">
                        <tr>
                            <td width="230" align="center">

                                <span v-if="item.amountLower > '0'">{{ item.amountLower }}<</span>
                                交易金额
                                <span v-if="item.amountUpper > '0'"><={{ item.amountUpper }}</span>
                            </td>
                            <td width="80" align="center">
                                <span v-if="item.feeType === '1'">每笔{{ item.amount }}元</span>
                                <span v-else-if="item.feeType  === '2'">{{item.ratio*100}}%</span>
                            </td>

                        </tr>
                    </div>
                </table>
                </Col>
                    </Row>
                <div style="font-size: 13px; font-weight: bold; padding-right: 10px" align="right">
                 手续费账户余额：{{merInfo.feeAmount}}元
                </div>
            </Card>
        </Col>

    </Row>
</template>

<script>

export default {


    name: 'feeCard',

    mounted() {
        //页面加载时或数据方法
        this.$store.dispatch('home_merFee_list').then((res) => {
            this.merFeeListJ = res.feeListJ;
            this.merFeeListB = res.feeListB;
            this.merInfo = res.merInfo;

            //vm.search()
        })

    },
    data () {
        return {
            merFeeListJ: [],
            merFeeListB: [],
            merInfo:{},
        }
    },
};
</script>
<style lang="less">
    .feeList {
        font-size: 12px;
        line-height: 30px;
    }
</style>
