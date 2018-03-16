<template>
    <div style="width:100%;height:100%;" id="last_week_unlock"></div>
</template>

<script>
import echarts from 'echarts';
import {mapState} from 'vuex'
let timerId='';
function createLwc(vm) {

    if(vm.lastWeekUnlock.tooltip!=undefined) {
        var chart = echarts.init(document.getElementById('last_week_unlock'));
        const option = vm.lastWeekUnlock;
        chart.setOption(option);
        window.addEventListener('resize', function () {
            chart.resize();
        });
        if(timerId!='')
            clearInterval(timerId);
    }
}
export default {
    computed: {
        ...mapState({
            'lastWeekUnlock': state => state.home.lastWeekUnlock,
        })
    },
    name: 'lastWeekUnlock',
    data () {
        return {
            //
        };
    },
    mounted () {
        this.$nextTick(() => {
            let vm=this;
            timerId=setInterval(function () {

                createLwc(vm);

            },500)
        });
    }
};
</script>
