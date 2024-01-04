<template>
    <div class="app-warpper">
        <section class="app-main">
            <transition name="fade-transform" mode="out-in">
                <router-view :key="key" />
            </transition>
        </section>
        <login-bar class="login_wrapper"></login-bar>
    </div>
</template>
 
 
<script>
import LoginBar from "./components/LoginBar"
export default {
    components:{
        LoginBar
    },
    computed: {
        key() {
            return this.$route.path
        }
    }
}
</script>
 <style lang="scss" scoped>
//  @media screen and (min-width:1800px)
//  {
    .app-warpper{
        display: flex;
        justify-content: flex-start;
        // height: 100%;
        //  height: 100%;
        .login_wrapper{
            // width: 80%;
            width: 1320px;
            height:100vh;
            // overflow: auto;
        }
        .app-main{
            // flex: 1;
            // width: 20%;
            width:600px;
            // margin-left: 50px;
        }
    }
//  }
// @media screen  and (max-width:1799px){
//     .app-warpper{
//         display: flex;
//         justify-content: flex-start;
//         // height: 100%;
//         //  height: 100%;
//         .login_wrapper{
//             // width: 80%;
//             width: 1320px;
//             height:100vh;
//             // overflow: auto;
//         }
//         .app-main{
//             // flex: 1;
//             // width: 20%;
//             width: 600px;
//             // margin-left: 50px;
//         }
//     }
// }
 </style>
 