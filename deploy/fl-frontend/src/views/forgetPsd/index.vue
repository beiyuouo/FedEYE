<template>
    <div class="login_box">
        <div class="title">
           用户找回密码
        </div>
        <div v-if="showstep" class="oneform">
            <div class="step">
                <el-divider class="one">
                    <img src="@/assets/no1-lt.png" alt="">
                </el-divider>
                <el-divider class="two">
                    <img src="@/assets/no2_nor.png" alt="">
                </el-divider>
            </div>
            <div class="step-desc">
                <el-divider class="one">
                    <div>手机验证</div>
                </el-divider>
                <el-divider class="two">
                    <div>设置新密码</div>
                </el-divider>
            </div>
            <div class="btn-box">
                <el-form @submit.native.prevent :model="numberValidateForm" :rules="numberRules"  ref="numberValidateForm" label-width="0px">
                    <div class="btn">
                        <el-form-item
                            prop="phone"
                            >
                            <el-input
                                placeholder="请输入手机号"
                                autocomplete="new-password"
                                v-model.number="numberValidateForm.phone">
                                <i slot="prefix">
                                    <img src="@/assets/phone.png" alt="">
                                </i>
                            </el-input>
                        </el-form-item>
                    </div>
                    <div class="btn">
                        <el-form-item
                            prop="vercode"
                            >
                            <el-input
                                autocomplete="new-password"
                                placeholder="请输入验证码"
                                v-model="numberValidateForm.vercode">
                                <i slot="prefix">
                                    <img src="@/assets/vercode.png" alt="">
                                </i>
                            </el-input> 
                            <el-button  @click="sendMsg" :disabled="isDisabled" type="primary">{{buttonName}}</el-button>
                        </el-form-item>
                    </div>
                </el-form>
                <div class="loginbtn">
                    <el-button @click="nextStep" type="primary">下一步</el-button>
                    <div class="login">已有账户，
                        <span @click="loginClick">立即登录</span>
                    </div>
                </div>
            </div>
        </div>
        <div v-else class="twoform">
            <div class="step">
                <el-divider class="one">
                    <img src="@/assets/no1-lt.png" alt="">
                </el-divider>
                <el-divider class="two">
                    <img src="@/assets/no1_hlt_lt.png" alt="">
                </el-divider>
            </div>
            <div class="step-desc">
                <el-divider class="one">
                    <div>手机验证</div>
                </el-divider>
                <el-divider class="two">
                    <div>设置新密码</div>
                </el-divider>
            </div>
            <div class="btn-box">
                <span class="btn-description">
                    新密码至少8位字符，数字、大小写和特殊字符
                </span>
                <el-form @submit.native.prevent :model="ruleForm" status-icon :rules="rules" ref="ruleForm" label-width="0px" class="demo-ruleForm">
                    <div class="btn">
                        <el-form-item label="" prop="pass">
                            <el-input
                                type="password"
                                autocomplete="new-password"
                                placeholder="请输入密码"
                                v-model="ruleForm.pass">
                                <i slot="prefix">
                                    <img src="@/assets/password.png" alt="">
                                </i>
                            </el-input>
                        </el-form-item>
                    </div>
                    <div class="btn">
                        <el-form-item label="" prop="checkPass">
                            <el-input
                                autocomplete="new-password"
                                type="password"
                                placeholder="请再次输入密码"
                                v-model="ruleForm.checkPass">
                                <i slot="prefix">
                                    <img src="@/assets/password.png" alt="">
                                </i>
                            </el-input> 
                        </el-form-item>
                    </div>
                </el-form>
                <div class="loginbtn">
                    <el-button @click="changePassWord" type="primary">重置密码</el-button>
                    <div class="login">已有账户，
                        <span @click="loginClick">立即登录</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script>
import { getPhoneCode , register , checkOnlyUser , phoneVerification,passwordChange} from '@/api/login/index.js'
import { setToken , setUserInfo } from "@/utils/auth";
export default {
    data() {
        var validatePass = (rule, value, callback) => {
            if (value === '') {
                callback(new Error('请输入密码'));
            } else {
                if (this.ruleForm.checkPass!== '') {
                    this.$refs.ruleForm.validateField('checkPass');
                }
                callback();
                }
            }
        var validatePass2 = (rule, value, callback) => {
            if (value === '') {
                callback(new Error('请再次输入密码'));
            } else if (value !== this.ruleForm.pass) {
              
                callback(new Error('两次输入密码不一致!'));
            } else {
                callback();
            }
        }
        return {
            activeName: 'first',
            numberValidateForm:{
                phone:'',
                vercode:'',
            },
            ruleForm: {
                pass: '',
                checkPass: '',
            },
            numberRules:{
                phone:[
                    { required: true, message: '手机号不能为空'},
                    { type: 'number', message: '手机号必须为数字值', trigger: 'blur'},
                    { pattern: /^1(3|4|5|6|7|8|9)\d{9}$/,message: '手机号格式不对', trigger: 'blur'}
                ],
                vercode:[
                    { required: true, message: '验证码不能为空'}
                ]
            },
            rules: {
                pass: [
                    { validator: validatePass, trigger: 'blur' },
                    { required: true,trigger: 'blur'},
                    { pattern: /^(?=.*[0-9])(?=.*[A-Za-z])(?=.*\W)(?!.* ).{8,16}$/ ,message: '至少8位字符，数字、大小写和特殊字符'}
                ],
                checkPass: [
                    { validator: validatePass2, trigger: 'blur' },
                    { required: true,trigger: 'blur'},
                    { pattern: /^(?=.*[0-9])(?=.*[A-Za-z])(?=.*\W)(?!.* ).{8,16}$/ ,message: '至少8位字符，数字、大小写和特殊字符'}
                ]
            },
            platname: '',
            // vercode: '',
            checked: false,
            showstep:true,
            buttonName: "获取验证码",
			isDisabled: false,
			time: 120

        }
    },
    methods: {
        // 获取手机号验证码
        async getPhoneCode() {
            let params = {
                "mobile": this.numberValidateForm.phone,
	            "smsmode": "2"
            }
            await getPhoneCode(params).then(res=>{
                if(res.data.code == 200 ) {
                    this.$message({
                        message: res.data.message,
                        type: "success",
                        duration: 5 * 1000
                    });
                } 
               
            })
            .catch(res=>{
                
            })
        },
        // 用户注册
       
        //跳转登录页面
        loginClick() {
            this.$router.push({
                name: "Login"
            })
        },
        // 重置密码
        changePassWord() {
            this.$refs["ruleForm"].validate((valid) => {
                if (valid) {
                    this.passwordChange()
                } else {
                    return false;
                }
            });
           
        },
        // 重置密码接口
        async passwordChange() {
            let params = {
                "password": this.ruleForm.checkPass,
                "phone": this.numberValidateForm.phone,
                smscode: this.numberValidateForm.vercode
            }
            await passwordChange(params).then(res=>{
                if(res.data.success) {
                    this.$message.success('修改密码成功')
                    this.$router.push({
                        name: "Login"
                    })
                }
            })
            .catch(res=>{
                
            })
        },
        // 验证码手机号验证
        async phoneVerification() {
            let params = {
                phone: this.numberValidateForm.phone,
                smscode: this.numberValidateForm.vercode
            }
            let that = this
            await phoneVerification(params).then(res=>{
                
                if(res.data.success) {
                    that.showstep = !that.showstep
                } else {
                    this.$message({
                        message: res.data.message,
                        type: "error",
                        duration: 5 * 1000
                    });
                }
            })
            .catch(res=>{
                
            })
        },
        nextStep() {
            
            this.$refs["numberValidateForm"].validate((valid) => {
                if (valid) {
                    this.phoneVerification()
                } else {
                    return false;
                }
            });
            
            
        },
        sendMsg() {
            
            this.$refs.numberValidateForm.validateField('phone',(valid) => {
                if (valid !== '手机号不能为空' && valid !== '手机号必须为数字值' && valid !== '手机号格式不对' ) {
                    let params = {
                        phone: this.numberValidateForm.phone
                    }
                    let that = this;
                    that.$refs.numberValidateForm.clearValidate('vercode')
                    this.getPhoneCode()
                    that.isDisabled = true;
                    let interval = window.setInterval(function() {
                        that.buttonName = ' ' + that.time + '秒重新发送';
                        --that.time;
                        if(that.time < 0) {
                            that.buttonName = "重新发送";
                            that.time = 120;
                            that.isDisabled = false;
                            window.clearInterval(interval);
                        }
                    }, 1000);
                    // // 检查用户账户是否唯一
                    // checkOnlyUser(params).then(res=>{
                    //     if(res.data.success) {
                            
                    //     }
                    // })
                    // .catch(res=>{
                        
                    // })     
                } else {
                    return false;
                }
            });
        }

    }
}
</script>
<style lang="scss" scoped>
.login_box{
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
    .oneform{
        margin-top: 70px;
        .step{
            display: flex;
            .one{     
                background-color: #e01622;
                .desc{
                    position: absolute;
                }
                
            }
        }
        .step-desc{
            display: flex;
            .one{
                background-color: #fff;
                margin: 10px 0 40px;
                ::v-deep .el-divider__text.is-center{
                    padding: 0;
                }
            }
            .two{
                background-color: #fff;
                margin: 10px 0 40px;
                ::v-deep .el-divider__text.is-center{
                    padding: 0;
                }
            }
        }
        .btn-box{
            .btn{
                margin-bottom: 30px;
            }
            .btn:first-child{
                margin-top: 40px;
            }
            .btn:nth-child(2){
                display: flex;
                ::v-deep .el-input{
                    width: 220px;
                    margin-right: 12px;
                }
                ::v-deep .el-button{
                    width: 120px;
                    padding: 12px 8px;
                    background-color: #e01622;
                }
                
            }
        }
        
      
        .forgetpsd{
            div{
                float: right;
                font-size: 14px;
                color: #484848;
        
            }
            cursor: pointer;
            overflow: hidden;
        }
        .loginbtn{
            margin-top: 40px;
            width: 100%;
            ::v-deep .el-button {
                width: 100%;
                height: 50px;
                background-color: #e01622;
                font-size: 18px;
            }
            .login{
                margin-top: 20px;
                text-align: center;
                font-size: 16px;
                color: #484848;
                span{
                    color: #e01622;
                    font-size: 16px;
                    cursor: pointer;
                }
            }
        }
        // margin-top: 30px;
        ::v-deep .el-tabs__item{
            color: #9A9A9A;
            font-size: 20px;
            font-weight: 700;
        }
        ::v-deep .el-tabs__item.is-active{
            color: #0B80EA;
            font-size: 20px;
            font-weight: 700;
        }
        ::v-deep .el-tabs__active-bar{
            color: #484848;
            background-color: #0B80EA;
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
        ::v-deep .el-button.is-disabled, .el-button.is-disabled:focus, .el-button.is-disabled{
            background-color: #F2F3F5 !important;
            border: 1px solid #9A9A9A;
            color: #9A9A9A;
        }
        ::v-deep .el-form-item__content{
            line-height: 0;
        }
    }
    .twoform{
        margin-top: 70px;
        .step{
            display: flex;
            .one{     
                
                background-color: #e01622;
                .desc{
                    position: absolute;
                }
                
            }
            .two{
                background-color: #e01622;
            }
        }
        .step-desc{
            display: flex;
            .one{
                background-color: #fff;
                margin: 10px 0 40px;
                ::v-deep .el-divider__text.is-center{
                    padding: 0;
                }
            }
            .two{
                background-color: #fff;
                margin: 10px 0 40px;
                ::v-deep .el-divider__text.is-center{
                    padding: 0;
                }
            }
        }
        .btn:first-child{
            margin-top: 40px;
        }
       
        .btn{
            margin-bottom: 30px;
        }
        .btn-description{
            font-size: 18px;
        }
        .forgetpsd{
            div{
                float: right;
                font-size: 14px;
                color: #484848;
        
            }
            
            cursor: pointer;
            overflow: hidden;
        }
        .loginbtn{
            margin-top: 40px;
            width: 100%;
            ::v-deep .el-button {
                width: 100%;
                height: 50px;
                background-color: #e01622;
                font-size: 18px;
            }
            .login{
                margin-top: 20px;
                text-align: center;
                font-size: 16px;
                color: #484848;
                span{
                    color: #0B80EA;
                    font-size: 16px;
                }
            }
        }
        // margin-top: 30px;
        ::v-deep .el-tabs__item{
            color: #9A9A9A;
            font-size: 20px;
            font-weight: 700;
        }
        ::v-deep .el-tabs__item.is-active{
            color: #0B80EA;
            font-size: 20px;
            font-weight: 700;
        }
        ::v-deep .el-tabs__active-bar{
            color: #484848;
            background-color: #0B80EA;
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
    .check{
        font-size: 14px;
        color: #0B80EA;
        cursor: pointer;
    }
}
</style>
