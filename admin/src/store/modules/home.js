import kit from '../../libs/kit'
const home = {
    state: {
        countStatisticsData:{},
        dspOption:{},
        lastWeekUnlock:{},
        lastWeekAct:{}
    },
    mutations: {
        setCountStatisticsData:function (state,obj) {
            state.countStatisticsData=Object.assign({},obj);
            let label=[];
            let data=[];
            let records=[];
            let ss=state.countStatisticsData.salesmanStatistics;
            let colors=[];
            for(let i=0;i<ss.length;i++){
                label.push(ss[i][1]);
                let colorCode='';
                let ac=true;
                while(ac){
                    colorCode=kit.getColor();
                    ac=kit.arrayContains(colors,colorCode);
                }
                colors.push(colorCode);
                records.push({value:ss[i][0],name:ss[i][1],itemStyle: {normal: {color: colorCode}}})
            }
            state.dspOption={
                tooltip: {
                    trigger: 'item',
                    formatter: '{a} <br/>{b} : {c} ({d}%)'
                },
                legend: {
                    orient: 'vertical',
                    left: 'right',
                    data: label
                },
                series: [
                    {
                        name: '销售人员销售量',
                        type: 'pie',
                        radius: '66%',
                        center: ['50%', '60%'],
                        data: records,
                        itemStyle: {
                            emphasis: {
                                shadowBlur: 10,
                                shadowOffsetX: 0,
                                shadowColor: 'rgba(0, 0, 0, 0.5)'
                            }
                        }
                    }
                ]
            }

            this.commit("setLastWeekUnlock",obj.lastWeekUnlockCount)
            this.commit("setLastWeekAct",obj.lastWeekActCount)

        },
        setLastWeekUnlock:function (state,list) {
            let dateArray=[];
            let dataArray=[];
            for(let i=0;i<list.length;i++){
                dateArray.push(list[i][1]);
                dataArray.push({value:list[i][0],name:list[i][1],itemStyle: {normal: {color: '#2d8cf0'}}})
            }
            const option = {
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'shadow'
                    }
                },
                grid: {
                    top: 0,
                    left: '2%',
                    right: '4%',
                    bottom: '3%',
                    containLabel: true
                },
                xAxis: {
                    type: 'value',
                    splitNumber:1,
                    boundaryGap: [0, 0.01]
                },
                yAxis: {
                    type: 'category',
                    data: dateArray,
                    nameTextStyle: {
                        color: '#c3c3c3'
                    }
                },
                series: [
                    {
                        name: '开卡数量',
                        type: 'bar',
                        data: dataArray
                    }
                ]
            };

            state.lastWeekUnlock=Object.assign({},option);

        },
        setLastWeekAct:function (state,list) {
            let dateArray=[];
            let dataArray=[];
            for(let i=0;i<list.length;i++){
                dateArray.push(list[i][1]);
                dataArray.push({value:list[i][0],name:list[i][1],itemStyle: {normal: {color: '#ff8718'}}})
            }
            const option = {
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'shadow'
                    }
                },
                grid: {
                    top: 0,
                    left: '2%',
                    right: '4%',
                    bottom: '3%',
                    containLabel: true
                },
                xAxis: {
                    type: 'value',
                    splitNumber:1,
                    boundaryGap: [0, 0.01]
                },
                yAxis: {
                    type: 'category',
                    data: dateArray,
                    nameTextStyle: {
                        color: '#c3c3c3'
                    }
                },
                series: [
                    {
                        name: '卡激活数量',
                        type: 'bar',
                        data: dataArray
                    }
                ]
            };

            state.lastWeekAct=Object.assign({},option);

        }
    },
    actions:{
        countStatisticsData_query :function ({ commit,state }) {
            let vm=this._vm;
            return new Promise(function (resolve, reject) {
                vm.$axios.get('/homeCountStatistics').then((res) => {
                    commit('setCountStatisticsData', res)
                    resolve()
                });
            });
        },
    }
};

export default home;