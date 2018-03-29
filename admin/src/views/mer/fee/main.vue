<template>
    <div>
        <Modal v-model="merFeeModal"  :mask-closable="false" >
            <p slot="header">
                <Icon type="information-circled"></Icon>
                <span>{{merFeeTitle}}</span>
            </p>
            <div slot="footer">
                <Button type="success" :loading="feeModalLoading" @click="save">保存</Button>
                <Button type="error" @click="merInfoModal=false">关闭</Button>
            </div>
        </Modal>
    </div>

</template>

<script>
    import {mapState} from 'vuex'



    export default {
        computed: {
            ...mapState({

            })
        },
        methods: {

        },
        mounted () {
            this.$store.dispatch('user_list')
        },
        data () {

        }
    }
</script>
<style lang="less">
    @import '../../../styles/common.less';
</style>