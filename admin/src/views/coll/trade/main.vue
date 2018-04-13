<template>
    <div>
        <Tabs>
            <TabPane label="交易">
                <tradeContent ref="tc"></tradeContent>
            </TabPane>
            <!-- <TabPane label="客户计划">
                <merCustContent ref="mcc"></merCustContent>
            </TabPane> -->
        </Tabs>
    </div>
</template>

<script>
    import { mapState } from 'vuex'
    import tradeContent from './tradeContent.vue'
    import merCustContent from './merCustContent.vue'

    export default {
        components: {
            tradeContent: tradeContent,
            merCustContent: merCustContent
        },
    }
</script>
<style lang="less">
    @import '../../../styles/common.less';
</style>