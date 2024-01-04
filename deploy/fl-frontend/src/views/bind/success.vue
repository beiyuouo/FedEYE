<template>
    <div class="success-box">
        <div class="title">
            成功新建联邦方<br>"{{name}}"
        </div>
        <div class="desc">
            您将作为超级管理员加入，可邀请他人加入联邦方，或对"{{name}}"联邦方进行更改信息等操作，可进入主页后，在“用户管理”中找到。
        </div>
        <div class="loginbtn">
            <div class="one">
                <el-button  @click="gotohome" type="primary">进入主页</el-button>
            </div>
            <div class='two'>
                <el-button  @click="nextbind">继续绑定</el-button>
            </div>
            
        </div>
    </div>
</template>
<script>
import {getFlUserPermission , queryPartyInfo , showFlUserRole} from '@/api/index.js'
import { getAuth, setAuth, setTenantId , setPartyInfo, setIndex , setUserRole} from '@/utils/auth.js'
export default {
    data() {
        return {
            name: ''
        }
    },
    created() {
        this.name = this.$route.params.name
    },
    methods: {
        nextbind() {
            this.$router.push({
                name: 'Bind',
                query: {
                    second: 1
                }
            })
        },
        gotohome() {
            this.queryPartyInfo()
        },
        // 获取用户所属联邦方
        async queryPartyInfo() {
            await queryPartyInfo().then(res=>{
                if(res.data.success) {
                    // 用户所属联邦方
                    setPartyInfo(JSON.stringify(res.data.result))
                    if(res.data.result.length==1){
                        let id = res.data.result[0].id
                        let name = res.data.result[0].name
                        let uploadUrl = res.data.result[0].uploadUrl
                        let nameEn = res.data.result[0].nameEn
                        setTenantId(id)
                        sessionStorage.setItem('showname',name)
                        sessionStorage.setItem("uploadUrl", uploadUrl);
                        sessionStorage.setItem("nameEn", nameEn);
                        // sessionStorage.setItem("userName", updateBy);
                        this.showFlUserRole(id)
                        this.getFlUserPermission(id)
                    } else if(res.data.result.length>1) {
                        setIndex('index')
                        this.$router.push({
                            name: 'Index'
                        })
                    }
                    
                }
            })
            .catch(res=>{
                
            })
        },
        // 获取用户权限
        async getFlUserPermission(id) {
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
        }
    }
}
</script>
<style lang="scss" scoped>
.success-box{
    height: 100%;
    background-color: #fff;
    padding: 0 100px;
    width: 400px;
    .title{
        color: #484848;
        font-size: 40px;
        font-weight: 700;
        padding-top: 140px;
        text-align: center;
    }
    .desc{
        margin-top: 156px;
        font-size: 20px;
        font-weight: 400;
        line-height: 40px;
        color: #484848;
    }
    .loginbtn{
        margin-top: 50px;
        cursor: pointer;
        width: 100%;
        .one{
            margin-bottom: 40px;
            ::v-deep .el-button {
                width: 100%;
                height: 50px;
                background-color: #e01622;
                font-size: 18px;
            }
        }
        .two{
            ::v-deep .el-button {
                width: 100%;
                height: 50px;
                font-size: 18px;
            }
        }
        
    }

}
</style>
