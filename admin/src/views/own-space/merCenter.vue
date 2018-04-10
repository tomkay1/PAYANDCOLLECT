<style lang="less">
    @import './own-space.less';
</style>

<template>
    <div>
        <Card>
            <p slot="title">
                <Icon type="person"></Icon>
                商户中心
            </p>
            <div>

                <Form :label-width="200" label-position="right" >
                    <FormItem label="商户编号"  >
                        {{merInfo.merchantNo}}
                    </FormItem>
                    <FormItem label="商户名称"  >
                        {{merInfo.merchantName}}
                    </FormItem>

                    <FormItem label="商户类型"  >
                        {{merInfo.merTypeTxt}}
                    </FormItem>

                    <FormItem label="负责人名称"  >
                        {{merInfo.perName}}
                    </FormItem>

                    <FormItem label="身份证号码"  >
                        {{merInfo.cardID}}
                    </FormItem>

                    <FormItem label="负责人手机号"  >
                        {{merInfo.mobile}}
                    </FormItem>

                    <FormItem label="负责人Email"  >
                        {{merInfo.email}}
                    </FormItem>
                    <FormItem label="负责人联系地址"  >
                        {{merInfo.address}}
                    </FormItem>
                    <FormItem label="备用联系地址1"  >
                        {{merInfo.mobile1}}
                    </FormItem>
                    <FormItem label="备用联系地址2"  >
                        {{merInfo.mobile2}}
                    </FormItem>
                    <FormItem label="预存手续费余额"  >
                        {{merInfo.feeAmount}}
                    </FormItem>
                    <FormItem label="手持身份证照片"  >

                        <img :src="urlCard"  width="500">
                    </FormItem>
                    <FormItem label="身份证正面"  >
                        <img :src="urlCardZ"  width="500">
                    </FormItem>
                    <FormItem label="身份证反面"  >
                        <img :src="urlCardF"  width="500">
                    </FormItem>


                </Form>
                <div slot="footer">

                </div>
            </div>
        </Card>
    </div>
</template>

<script>
    import consts from '../../libs/consts'
    import ICol from "iview/src/components/grid/col";
export default {
    components: {ICol},
    name: 'merCenter',
    data () {

        return {
            merInfo:{},
            urlCard: '',
            urlCardZ: '',
            urlCardF: '',
        };
    },
    methods: {

    },
    mounted () {
        this.$store.dispatch('login_merInfo').then((res)=> {
            this.merInfo = res
            this.urlCard = consts.devLocation + "/cmn/act04?picid=" + this.merInfo.cardImg;
            this.urlCardF = consts.devLocation + "/cmn/act04?picid=" + this.merInfo.cardF;
            this.urlCardZ = consts.devLocation + "/cmn/act04?picid=" + this.merInfo.cardZ;
        });
    }
};
</script>
