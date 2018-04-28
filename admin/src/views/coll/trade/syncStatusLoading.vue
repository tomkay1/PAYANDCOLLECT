<template>
    <div class="ssl_loading">
        <div class="ssl-loading-center">
            <div class="ssl-object ssl-object-three" v-bind:class="{ 'ssl-object-active': isActive }"></div>
            <div class="ssl-object ssl-object-two" v-bind:class="{ 'ssl-object-active': isActive }"></div>
            <div class="ssl-object ssl-object-one" v-bind:class="{ 'ssl-object-active': isActive }"></div>
        </div>
        <Tooltip placement="top" :transfer="transfer" :delay="1000" class="ssl-tooltip">
            <div slot="content">
                <p>{{contentText}}</p>
            </div>
            <div class="ssl-tooltip-holder"></div>
        </Tooltip>
    </div>
</template>

<script>
    export default {
        computed: {
            contentText: function () {
                return this.isActive ? '同步中...' : '订单不可同步';
            }
        },
        data() {
            return {
                transfer: true,
                isActive: true
            }
        }
    }
</script>
<style>
    .ssl-tooltip {
        position: absolute;
        top: 0px;
        left: 0px;
        height: 30px;
        width: 30px;
    }

    .ssl-tooltip-holder {
        height: 30px;
        width: 30px;
        position: relative;
    }

    .ssl_loading {
        background-color: #FFF;
        height: 30px;
        width: 30px;
        position: absolute;
        z-index: 999;
        margin-top: 0px;
        top: 0px;
    }

    .ssl-loading-center {
        width: 100%;
        height: 100%;
        position: relative;
        -ms-transform: rotate(-135deg);
        -webkit-transform: rotate(-135deg);
        transform: rotate(-135deg);
    }

    .ssl-object {
        -moz-border-radius: 50% 50% 50% 50%;
        -webkit-border-radius: 50% 50% 50% 50%;
        border-radius: 50% 50% 50% 50%;
        position: absolute;
        border-top: 2px solid #ed3f14;
        border-bottom: 2px solid transparent;
        border-left: 2px solid #ed3f14;
        border-right: 2px solid transparent;
    }

    .ssl-object-active {
        -webkit-animation: animate 2s infinite;
        animation: animate 2s infinite;
    }

    .ssl-object-one {
        left: 10px;
        bottom: 10px;
        width: 10px;
        height: 10px;
    }

    .ssl-object-two {
        left: 5px;
        bottom: 5px;
        width: 20px;
        height: 20px;
        -webkit-animation-delay: 0.2s;
        animation-delay: 0.2s;
    }

    .ssl-object-three {
        left: 0px;
        bottom: 0px;
        width: 30px;
        height: 30px;
        -webkit-animation-delay: 0.4s;
        animation-delay: 0.4s;
    }

    @-webkit-keyframes animate {
        50% {
            -ms-transform: rotate(360deg) scale(0.8);
            -webkit-transform: rotate(360deg) scale(0.8);
            transform: rotate(360deg) scale(0.8);
        }
    }

    @keyframes animate {
        50% {
            -ms-transform: rotate(360deg) scale(0.8);
            -webkit-transform: rotate(360deg) scale(0.8);
            transform: rotate(360deg) scale(0.8);
        }
    }
</style>