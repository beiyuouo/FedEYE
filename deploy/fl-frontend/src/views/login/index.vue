<template>
    <div class="login_box">
        <div class="title">
            用户登录/注册
        </div>
        <div class="form">
            <el-tabs v-model="activeName">
                <el-tab-pane label="密码登录" name="first">
                    <el-form @submit.native.prevent :model="numberValidateForm"  :rules="rules" ref="numberForm" label-width="0px">
                        <div class="btn">
                            <el-form-item prop="phone">
                                <el-input
                                    autocomplete="new-password"
                                    placeholder="请输入手机号/用户名"
                                    v-model.number="numberValidateForm.phone">
                                    <i slot="prefix">
                                        <img src="@/assets/phone.png" alt="">
                                    </i>
                                </el-input>
                            </el-form-item>
                            
                        </div>
                        <div class="btn">
                            <el-form-item prop="password">
                                <el-input
                                    autocomplete="new-password"
                                    type="password"
                                    placeholder="请输入密码"
                                    v-model="numberValidateForm.password">
                                    <i slot="prefix">
                                        <img src="@/assets/password.png" alt="">
                                    </i>
                                </el-input>
                            </el-form-item>
                            
                        </div>
                        <div class="btn">
                            <el-form-item prop="vercode">
                                <el-input
                                    placeholder="请输入验证码"
                                    v-model="numberValidateForm.vercode">
                                    <i slot="prefix">
                                        <img src="@/assets/vercode.png" alt="">
                                    </i>
                                </el-input> 
                            </el-form-item>
                            <img  @click="refreshCode" class="img" :src="captchaimg" alt="">
                        </div>
                    </el-form>
                    
                    <!-- <div class="forgetpsd">
                        <div>
                            忘记密码?
                        </div>
                    </div> -->
                    <div class="loginbtn">
                        <div @click="loginKeyCloak" class="keycloak">
                            使用keycloak登录
                        </div>
                        <el-button :loading="loading" @click="login" type="primary">登录</el-button>
                        
                        <!-- <el-button :loading="loading" @click="loginKeyCloak" type="info">keycloak</el-button> -->
                        <div class='footer-box'> 
                            <span class="register" @click="forgetPassWord">忘记密码</span>
                            <el-divider direction="vertical"></el-divider>
                            <span @click="register" class="register">
                                免费注册
                            </span>
                        </div>
                        <div ></div>
                    </div>
                </el-tab-pane>
                <!-- <el-tab-pane label="动态登录" name="second">动态登录</el-tab-pane> -->
            </el-tabs>
        </div>
    </div>
</template>
<script>
import {getCaptcha} from '@/api/login/index.js'
import {getFlUserPermission , queryPartyInfo , showFlUserRole, passwordChange ,keycloakFn} from '@/api/index.js'
import { getAuth, setAuth, setTenantId , setPartyInfo, setIndex , setUserRole} from '@/utils/auth.js'
// import Vue from 'vue'
// import Keycloak from '@dsb-norge/vue-keycloak-js';
import crypto from '@/utils/crypto.js'
export default {
    data() {
        return {
            activeName: 'first',
            numberValidateForm:{
                phone:'',
                password:'',
                vercode: '',
            },
            
            rules: {
                phone: [
                    { required: true, message: '请输入手机号/用户名' },
                    // { type: 'number', message: '手机号必须为数字值'},
                    // { pattern: /^1(3|4|5|6|7|8|9)\d{9}$/,message: '手机号格式不对'}
                ],
                password: [
                    { required: true, message: '请输入密码'}
                ],
                vercode: [
                    
                    { required: true, message: '请输入验证码' }
                ]
                
            },
            captchaimg:'',
            loading: false,
            key: '',
            

            
        }
    },
    created() {
        this.getCaptcha()
        sessionStorage.removeItem("Auth")
        sessionStorage.removeItem("showname")
        sessionStorage.removeItem("tourist")
    },
    methods:{
       loginKeyCloak() {
            // Vue.use(Keycloak, {
            //     init: { // 初始化Keycloak实例的选项
            //         onLoad: 'login-required'
            //     },
            //     config: { // Keycloak配置选项
            //         url: 'http://192.168.66.1:8080',
            //         realm: 'fids',
            //         clientId: 'fl_palform_front'
            //     },
            //     onReady: () => { // Keycloak初始化完成后的回调函数
            //         this.KeycloakLogin()
            //         // this.$keycloak.login();
            //     }
            // })
            window.location.href= 'http://192.168.66.1:8080/realms/fids/protocol/openid-connect/auth?response_type=code&client_id=fl-platform&redirect_uri=http://sdaict-fml-console-frontend-test/callback&state=c418e175-b6f4-4072-9777-2442adfd5dbd&login=true&scope=openid'
            // window.location.href='http://192.168.66.1:8080/realms/fids/protocol/openid-connect/auth?response_type=code&client_id=fl-platform&redirect_uri='+'http://localhost:8888/callback'+'&state=c418e175-b6f4-4072-9777-2442adfd5dbd&login=true&scope=openid'
           
       },
       async KeycloakLogin() {
            await keycloakFn().then(res=>{
                console.log(res)
            })
            .catch(error=>{

            })
       },
        // 用户登录
        login() {
            let loginData = {
                "captcha": this.numberValidateForm.vercode,
	            "checkKey": this.key,
                "password": crypto.set(this.numberValidateForm.password),
                "username": crypto.set( this.numberValidateForm.phone)
            }
            this.$refs['numberForm'].validate((valid) => {
                if (valid) {
                    this.loading = true;
                    this.$store.dispatch('user/login', loginData).then((res) => {
                        this.queryPartyInfo()
                        this.loading = false
                        this.$message.success(res.data.message)
                    }).catch((res) => {
                        this.refreshCode()
                        this.loading = false
                    })
                } else {
                   
                   
                    return false;
                }
            });
        },
        // 获取图形验证码
        async getCaptcha(){
            this.key = parseInt(Math.random()* 100000) + 1
            await getCaptcha(this.key).then(res=>{
                let resdata = res.data
                this.captchaimg = resdata.result
            })
            .catch(res=>{
                
            })
        },
        // 用户忘记密码
        forgetPassWord() {
            this.$router.push({
                name: "ForgetPassWord"
            })
        },
        async passwordChange() {
            let params = {

            }
            await passwordChange().then(res=>{

            })
        },
        //  用户注册
        register() {
            this.$router.push({
                name: "Register"
            })
        },
        // 刷新验证码
        refreshCode() {
            this.getCaptcha()
        },
        // 获取用户所属联邦方
        async queryPartyInfo() {
            await queryPartyInfo().then(res=>{
                if(res.data.success) {
                    // 用户所属联邦方
                    setPartyInfo(JSON.stringify(res.data.result))
                  
                    if(res.data.result == null) {
                        //  游客状态
                        this.$router.push({
                            name: 'newhome'
                        })
                        setTenantId(0)
                        sessionStorage.setItem('tourist',true)
                    
                    } else {
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
.login_box{
    height: 100%;
    background-color: #fff;
    width: 400px;
    padding: 0 100px;
    .title{
        color: #484848;
        font-size: 40px;
        font-weight: 700;
        padding-top: 140px;
        text-align: center;
    }
    .form{
        margin-top: 70px;
        .btn:first-child{
            margin-top: 40px;
        }
        .btn:last-child{
            display: flex;
            ::v-deep .el-input {
                width: 204px;
                margin-right: 16px;
            }
            .img{
                width:144px;
                height: 40px;
                background-color: #ccc;
                cursor: pointer;
            }
            
        }
        .btn{
            margin-bottom: 30px;
        }
        .forgetpsd{
            text-align: right;
            font-size: 14px;
            color: #484848;
            cursor: pointer;
            overflow: hidden;
        }
        .loginbtn{
            margin-top: 40px;
            cursor: pointer;
            width: 100%;
            ::v-deep .el-button {
                width: 100%;
                height: 50px;
                background-color: #e01622;
                font-size: 18px;
            }
            .keycloak{
                text-align: center;
                background-color: #ccc;
                height: 40px;
                line-height: 40px;
                margin-bottom: 20px;
                font-size: 16px;
            }
            .footer-box{
                 margin-top: 20px;
                 text-align: center;
                //  height:30px;
                //  line-height:30px;
                  font-size: 16px;
                .register{
                    margin: 0 5px;
                    color: #484848;
                }
            }
            
        }
        // margin-top: 30px;
        ::v-deep .el-tabs__item{
            color: #9A9A9A;
            font-size: 20px;
            font-weight: 700;
            height: 50px;
        }
        ::v-deep .el-tabs__item.is-active{
            color: #e01622;
            font-size: 20px;
            font-weight: 700;
        }
        ::v-deep .el-tabs__active-bar{
            color: #484848;
            background-color: #e01622;
            height: 5px;
        }
        ::v-deep .el-input__prefix, .el-input__suffix{
            display: flex;
            align-items: center;
            padding-left: 15px;
        }
        ::v-deep .el-input--prefix .el-input__inner{
            padding-left: 50px;
        }
        ::v-deep .el-form-item__content{
            line-height: 0;
        }
    }
}
</style>
