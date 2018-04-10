

<template>
    <Row :gutter="10">
        <Col :xs="24" :sm="12" :md="12" :style="{marginBottom: '10px'}">
        <infor-card
                id-name="user_created_count"
                :end-val="lastCust"
                iconType="android-person-add"
                color="#2d8cf0"
                intro-text="昨日新增客户数"
        />
        </Col>
        <Col :xs="24" :sm="12" :md="12" :style="{marginBottom: '10px'}">
        <infor-card
                id-name="visit_count"
                :end-val="lastAmount"
                iconType="social-yen"
                color="#64d572"

                intro-text="昨日交易金额"
        />
        </Col>
        <Col :xs="24" :sm="12" :md="12" :style="{marginBottom: '10px'}">
        <infor-card
                id-name="collection_count"
                :end-val="lastCount"
                iconType="arrow-graph-up-right"
                color="#ffd572"
                intro-text="昨日交易笔数"
        />
        </Col>
        <Col :xs="24" :sm="12" :md="12" :style="{marginBottom: '10px'}">
        <infor-card
                id-name="transfer_count"
                :end-val="lastFee"
                iconType="card"
                color="#f25e43"
                intro-text="昨日手续费总额"
        />
        </Col>
    </Row>
</template>

<script>

    import inforCard from './inforCard.vue';
export default {

    name: 'total',
    components: {
        inforCard
    },
    mounted() {

        //页面加载时或数据方法
        this.$axios.post('/home/total').then((res) => {

            this.lastAmount=res.lastAmount;
            this.lastCust=res.lastCust;
            this.lastCount=res.lastCount;
            this.lastFee=res.lastFee;
        })

    },
    data () {
        return {
            lastAmount:0,
            lastCust:0,
            lastCount:0,
            lastFee:0,

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
