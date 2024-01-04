<template>
    <div>
        <el-dialog
            title=""
            :visible.sync="joinVisible"
            :show-close='false'
            :append-to-body='true'
            width="30%"
            center>
            <div class="success-box">
                <div v-if="!registerSuccessFlag" class="join">
                    <div class="img">
                        <img src="@/assets/success.png" alt="">
                    </div>
                    <div class="title">
                        成功加入联邦方<br>"{{partyInfoName}}"
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
                <div  v-if="registerSuccessFlag" class="create">
                    <div class="img">
                        <img src="@/assets/success.png" alt="">
                    </div>
                    <div class="title">
                        成功新建联邦方"{{registerSuccessName}}"
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
            </div>
        </el-dialog>
    </div>
</template>
<script>
import {getFlUserPermission , queryPartyInfo , showFlUserRole} from '@/api/index.js'
import { getAuth, setAuth, getTenantId, setTenantId, removeAuth , setPartyInfo, setIndex , setUserRole} from '@/utils/auth.js'
export default {
    props: {
        visible: Boolean,
        registerSuccessName: String,
        registerSuccessFlag: Boolean,
        partyInfoName: String
    },
    inject:['reload'],
    computed: {
        joinVisible: {
            get() {
                return this.visible
            },
            set (val) {
                this.$emit('updateJoinVisible',val)
            }
        }
    },
    data() {
        return {
            name: '',
        }
    },
    methods: {
         // 获取用户所属联邦方
        async queryPartyInfo() {
            await queryPartyInfo().then(res=>{
                if(res.data.success) {
                    // 用户所属联邦方
                    setPartyInfo(JSON.stringify(res.data.result))
                   
                        sessionStorage.setItem('tourist',false)
                        if(res.data.result.length==1){
                            let id = res.data.result[0].id
                            let name = res.data.result[0].name
                            sessionStorage.setItem('showname',name)
                            setTenantId(id)
                            this.showFlUserRole(id)
                            this.getFlUserPermission(id)
                        } else if(res.data.result.length>1) {
                            this.reload()
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
        async getFlUserPermission(id,name) {
            await getFlUserPermission(id).then(res=>{
                // 权限
                setAuth(JSON.stringify(res.data.result))
                this.Auth =JSON.parse(getAuth()) 
                setIndex('')
                this.reload()
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
        },
        showClose() {
            this.$emit(
                'handleClickinjoin',
            )
        },
        gotohome() {
            this.$emit(
                'gotoHome'
            )
            this.queryPartyInfo()
        },
        nextbind() {
            this.$emit(
                'continueClick'
            )
        },
    }
}
</script>
<style lang="scss" scoped>
.success-box{
    height: 100%;
    background-color: #fff;
    padding: 0 50px;
    .img{
        text-align: center;
    }
    .title{
        color: #484848;
        font-size: 20px;
        font-weight: 700;
        padding-top: 40px;
        text-align: center;
        margin-bottom: 30px;
    }
    .desc{
        font-size: 20px;
        line-height: 1.5
    }
    .loginbtn{
        margin-top: 155px;
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
