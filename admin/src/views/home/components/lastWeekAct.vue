<template>
    <div style="width:100%;height:100%;" id="last_week_act"></div>
</template>

<script>
    import echarts from 'echarts';
    import {mapState} from 'vuex'
    let timerId='';
    function createLwa(vm) {

        if(vm.lastWeekAct.tooltip!=undefined) {
            var chart = echarts.init(document.getElementById('last_week_act'));
            const option = vm.lastWeekAct;
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
                'lastWeekAct': state => state.home.lastWeekAct,
            })
        },
        name: 'lastWeekAct',
        data () {
            return {
                //
            };
        },
        mounted () {
            this.$nextTick(() => {
                let vm=this;
                timerId=setInterval(function () {

                    createLwa(vm);

                },500)
            });
        }
    };
</script>
