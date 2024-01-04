<template>
    <div class="app-warpper">
        <header-bar :firstName='firstName' ></header-bar>
        <section class="app-main">
            <transition name="fade-transform" mode="out-in">
                <router-view @tourist='tourist' @name='name' @header='header' :key="key" />
            </transition>
        </section>
    </div>
</template>

<script>
import HeaderBar from "./components/HeaderIndex"
export default {
    data() {
        return {
            firstName: ''
        }
    },
    components:{
        HeaderBar
    },
    created() {
    },
    computed: {
        key() {
            return this.$route.path
        }
    },
    methods: {
        header(bool) {
            this.showHeader = bool
        },
        tourist(bool) {
            this.touristFlag = bool
        },
        name(name) {
            sessionStorage.setItem("showname", name);
            this.firstName = name            
        }
    }
}
</script>

<style lang="scss" scoped>
.app-warpper{
    // margin: 0 20px;
    // padding-top: 10px;
    height: 100%;
    .app-main{
        // background: #f4f6f8;
        // height: 100%;
        padding: 0 55px;
        // overflow: hidden;
    }
}
</style>

