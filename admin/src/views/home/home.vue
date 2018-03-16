<style lang="less">
    @import "./home.less";
    @import "../../styles/common.less";
</style>
<template>
    <div class="home-main">
        <Row :gutter="10">
            <Col :md="24" :lg="24">

                    <Col :xs="24" :sm="12" :md="5" :style="{marginBottom: '10px'}">
                    <infor-card
                            id-name="actNotExportCount"
                            :end-val="countStatisticsData.actNotExportCount==undefined?0:countStatisticsData.actNotExportCount"
                            iconType="fireball"
                            color="#f25e43"
                            intro-text="截止到现在激活未导出数量"
                    ></infor-card>
                    </Col>
                    <Col :xs="24" :sm="12" :md="5" :style="{marginBottom: '10px'}">
                        <infor-card
                            id-name="applyCount"
                            :end-val="countStatisticsData.applyCount==undefined?0:countStatisticsData.applyCount"
                            iconType="card"
                            color="#2d8cf0"
                            intro-text="今日卡申请数量"
                        ></infor-card>
                    </Col>
                    <Col :xs="24" :sm="12" :md="5" :style="{marginBottom: '10px'}">
                        <infor-card
                            id-name="outDepotCount"
                            :end-val="countStatisticsData.unlock==undefined?0:countStatisticsData.unlock"
                            iconType="ios-cart"
                            color="#64d572"
                            :iconSize="50"
                            intro-text="今日开卡数量"
                        ></infor-card>
                    </Col>
                    <Col :xs="24" :sm="12" :md="5" :style="{marginBottom: '10px'}">
                        <infor-card
                            id-name="actCount"
                            :end-val="countStatisticsData.actCount==undefined?0:countStatisticsData.actCount"
                            iconType="ios-bolt"
                            color="#ffd572"
                            intro-text="今日卡激活数量"
                        ></infor-card>
                    </Col>
                    <Col :xs="24" :sm="12" :md="4" :style="{marginBottom: '10px'}">
                        <infor-card
                            id-name="exportCount"
                            :end-val="countStatisticsData.exportCount==undefined?0:countStatisticsData.exportCount"
                            iconType="shuffle"
                            color="#8A2BE2"
                            intro-text="今日卡导出数量"
                        ></infor-card>
                    </Col>



            </Col>
        </Row>
        <Row :gutter="10" class="margin-top-10">
            <Col :md="24" :lg="8" :style="{marginBottom: '10px'}">
                <Card>
                    <p slot="title" class="card-title">
                        <Icon type="ios-pulse-strong"></Icon>
                        上周每日开卡量
                    </p>
                    <div class="data-source-row">
                        <last-week-unlock></last-week-unlock>
                    </div>
                </Card>
            </Col>
            <Col :md="24" :lg="8" :style="{marginBottom: '10px'}">
            <Card>
                <p slot="title" class="card-title">
                    <Icon type="ios-pulse-strong"></Icon>
                    上周每日激活量
                </p>
                <div class="data-source-row">
                    <last-week-act></last-week-act>
                </div>
            </Card>
            </Col>
            <Col :md="24" :lg="8" :style="{marginBottom: '10px'}">
                <Card>
                    <p slot="title" class="card-title">
                        <Icon type="pie-graph"></Icon>
                        上周销售人员开卡量
                    </p>
                    <div class="data-source-row">
                        <data-source-pie ></data-source-pie>
                    </div>
                </Card>
            </Col>

        </Row>

    </div>
</template>

<script>
import dataSourcePie from './components/dataSourcePie.vue';
import lastWeekUnlock from './components/lastWeekUnlock.vue';
import lastWeekAct from './components/lastWeekAct.vue';
import countUp from './components/countUp.vue';
import inforCard from './components/inforCard.vue';
import {mapState} from 'vuex'
import kit from '../../libs/kit'

export default {
    name: 'home',
    components: {
        dataSourcePie,
        lastWeekUnlock,
        lastWeekAct,
//        lastWeekCardsUnlock,
        countUp,
        inforCard,
    },
    data () {
        return {

        }
    },
    mounted () {
        let vm=this;
        this.$store.dispatch('countStatisticsData_query').then(()=> {})
    },
    computed: {
        ...mapState({
            'countStatisticsData': state => state.home.countStatisticsData,
        })
    },
    methods: {

    }
};


</script>
