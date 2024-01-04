<template>
    <header class="header-box">
        <div class="header_container">
            <div class="logo">
                <!-- :src="userInfo.avatar==null?'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg':userInfo.avatar.indexOf('http')!=-1?userInfo.avatar:'http://112.230.202.198:31825/'+userInfo.avatar" -->
                <img  class="img" :src="userInfo.avatar==null?imgs:userInfo.avatar.indexOf('http')!=-1?userInfo.avatar:`${$url}`+userInfo.avatar"  alt="">
                <div class="name">
                    Hi, {{name}} 
                   
                </div>
            </div>
            <div  class="user_box">
                <i class="el-icon-setting"></i>
                <!-- <span class="user_setting">用户设置</span> -->
                <span @click="logout" class="user_setting">退出</span>
            </div>
        </div>
    </header>
    
    
</template>
<script>
import { getUserInfo ,  getIndex, getTenantId ,getPartyInfo , getAuth, setAuth, setIndex,setTenantId, removeAuth , setPartyInfo ,setUserRole } from "@/utils/auth";
import { mapGetters } from 'vuex'
import imgs  from '@/assets/person.png'
export default {
    name: 'HeaderBar',
    props: {
        firstName: String,
    },
    data() {
        return {
            name:"",
            partInfo:[],
            showname:'',
            imgs:imgs
        }
    },
    created() {
        this.partInfo = JSON.parse(getPartyInfo())
        let resdata =JSON.parse(getUserInfo())
        this.name = resdata.username
        if(this.showname=='') {
            this.showname = sessionStorage.getItem('showname')
        }
        
    },
    methods:{
        // async logout() {
        //     await this.$store.dispatch("user/logout").then(() => {
        //         var sevice = "http://"+window.location.host+"/";
        //         var serviceUrl = encodeURIComponent(sevice);
        //         window.location.href = window._CONFIG['casPrefixUrl']+"/logout?service="+serviceUrl;
        //         // location.reload();
        //     });
        //     sessionStorage.removeItem("Auth")
        //     sessionStorage.removeItem("showname")
        //     sessionStorage.removeItem("tourist")
        //     this.$router.push({
        //         path: '/login',
        //     })
        // }
        async logout() {
            await this.$store.dispatch('user/logout')
            this.$router.push(`/login`)
        }
    },
    watch: {
        firstName(newValue,oldValue){
            this.showname = newValue
        }
    },
    computed: {
    // 使用对象展开运算符将 getters 混入 computed 对象中
        ...mapGetters({
            userInfo:'userInfo'
        })
    }
}
</script>
<style lang="scss" scoped>
.header{
    display: flex;
        h3{
            flex: 1;
            padding: 0;
            margin: 0;
            font-size: 14px;
        }
        .read{
            cursor: pointer;
        }
    }

::v-deep .el-badge{
    vertical-align: sub;
}
.header-box{
    height: 80px;
    .header_container{
        padding: 0 55px;
        position: fixed;
        z-index: 5;
        top: 0;
        box-sizing: border-box;
        border-radius: 4px;
        display: flex;
        justify-content: center;
        align-items: center;
        height: 80px;
        overflow: hidden;
        width: 100%;
        background-color: #fff;
        .logo{
            flex: 1;
            display: flex;
            align-items: center;
            overflow: hidden;
            .img{
                width: 50px;
                height: 50px;
            }
            .name{
                margin: 0 20px;
                font-weight: 700;
                font-size: 24px;
            }
        }
        .user_box{
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            
            .notice{
                margin-right: 20px;
            }
            .user_setting{
                margin-left: 10px;
            
                font-size: 14px
            }
        }
    }
}

.hospital-box:hover{
        cursor: pointer;
        background-color: #DAE8F7;
    }
.inve-popover{
    padding: 0 !important;
}



</style>

