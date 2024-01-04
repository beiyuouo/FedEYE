<template>
    <div>
    </div>
</template>

<script>
import {keycloakFn,queryPartyInfo,getFlUserPermission,showFlUserRole } from '@/api/index.js'
import { getAuth, setAuth, setTenantId , setPartyInfo, setIndex , setUserRole} from '@/utils/auth.js'
export default {
    name: 'FlFederalIndex',

    data() {
        return {
            loading: null
        };
    },
    created() {
        this.openFullScreen2()
    },
    mounted() {
        this.KeycloakLogin(this.$route.query)
    },

    methods: { 
        openFullScreen2() {
            this.loading = this.$loading({
                lock: true,
                text: 'Loading',
                spinner: 'el-icon-loading',
                background: 'rgba(0, 0, 0, 0.7)'
            });
        },
        async KeycloakLogin(params) {
            let data = params
            await keycloakFn(data).then(res=>{
                console.log(res)
                this.$store.dispatch('user/ssoLogin',res.data.result).then((res) => {
                    this.queryPartyInfo()
                    this.$message.success(res.data.message)
                }).catch((res) => {
                    // this.refreshCode()
                    // this.loading = false
                })
            
            })
            .catch(error=>{

            })
       },
         // 获取用户角色及状态
        async showFlUserRole(id) {
            await showFlUserRole(id).then(res=>{
                
                if(res.data.success) {
                    // 用户角色
                    setUserRole(JSON.stringify(res.data.result))
                }
            })
            .catch(res=>{
                
            })
        },
         // 获取用户权限
        async getFlUserPermission(id,name) {
            await getFlUserPermission(id).then(res=>{
                // 权限
                setAuth(JSON.stringify(res.data.result))
                this.Auth =JSON.parse(getAuth()) 
                setIndex('')
                this.$router.push({
                    name: 'newhome'
                })
            })
            .catch(res=>{
                
            }) 
        },
         // 获取用户所属联邦方
        async queryPartyInfo() {
            await queryPartyInfo().then(res=>{
                if(res.data.success) {
                    // 用户所属联邦方
                    setPartyInfo(JSON.stringify(res.data.result))
                        this.loading.close()
                        sessionStorage.setItem('tourist',false)
                        if(res.data.result.length==1){
                            let id = res.data.result[0].id
                            let name = res.data.result[0].name
                            let uploadUrl = res.data.result[0].uploadUrl
                            let nameEn = res.data.result[0].nameEn
                            sessionStorage.setItem('showname',name)
                            sessionStorage.setItem("uploadUrl", uploadUrl);
                            sessionStorage.setItem("nameEn", nameEn);
                            // sessionStorage.setItem("userName", updateBy);
                            
                            setTenantId(id)
                            this.showFlUserRole(id)
                            this.getFlUserPermission(id)
                        } else if(res.data.result.length>1) {
                            setIndex('index')
                            sessionStorage.setItem('showHeader',false)
                            this.$router.push({
                                name: 'Index'
                            })
                        } 
                    
                    
                    
                    
                }
            })
            .catch(res=>{
                
            })
        },
    },
};
</script>

<style lang="scss" scoped>

</style>