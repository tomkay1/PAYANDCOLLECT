<template>
    <div style="width:100%;height:100%;" id="data_source_con"></div>
</template>

<script>
import echarts from 'echarts';
import {mapState} from 'vuex'
let timerId='';
function createdsp(vm) {

    if(vm.dspOption.tooltip!=undefined) {
        var dataSourcePie = echarts.init(document.getElementById('data_source_con'));
        const option = vm.dspOption;
        dataSourcePie.setOption(option);
        window.addEventListener('resize', function () {
            dataSourcePie.resize();
        });
        if(timerId!='')
        clearInterval(timerId);
    }
}
export default {
    name: 'dataSourcePie',
    data () {
        return {

        };
    },
    computed: {
        ...mapState({
            'dspOption': state => state.home.dspOption,
        })
    },
    props: {

    },
    watch:{

    },
    mounted () {
        this.$nextTick(() => {
            let vm=this;
            timerId=setInterval(function () {

                    createdsp(vm);

            },500)
        });
    }
};
</script>
