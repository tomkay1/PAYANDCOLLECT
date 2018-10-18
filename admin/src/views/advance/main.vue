<template>
    <Card>
        <advanceTradePaneContent></advanceTradePaneContent>
    </Card>
</template>

<script>
    import { mapState } from 'vuex'
    import advanceTradePaneContent from './tradePaneContent.vue'
    export default {
        components: {
            advanceTradePaneContent: advanceTradePaneContent,
        }
    }
</script>
<style lang="less">
    @import '../../styles/common.less';
</style>