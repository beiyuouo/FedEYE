<template>
    <div class="success-box">
        <div class="title">
            注册成功，<br>请绑定联邦方
        </div>
        <div class="form">
            
            <el-form  status-icon 
                @submit.native.prevent
                ref="ruleForm" 
                label-width="0px" 
                class="demo-ruleForm"
                :rules="rules">
                <div class="sub-title">加入联邦方</div>
                <el-form-item :error="errorMsg1" >
                   <el-input
                        clearable
                        @focus='joinfocus'
                        placeholder="请输入联邦方邀请码"
                        v-model="joinfederal">
                    </el-input>
                </el-form-item>
                <el-divider class="line" >或</el-divider>
                <!-- <div class="sub-title">新建联邦方</div> -->
                
            </el-form>
            <el-form 
                label-width="auto" 
                @submit.native.prevent
                ref='lianbangForm'
                :model="lianbangForm"
                label-position="top"
                class="demo-ruleForm"
                :rules="rules">
                <el-form-item prop="creatfederal" label='新建联邦方'  :error="errorMsg2">
                    <el-input
                        clearable
                        @focus='creatfocus'
                        placeholder="输入联邦方名称"
                        v-model="lianbangForm.creatfederal">
                    </el-input>
                </el-form-item>
                <el-form-item prop="nameEn" label="命名空间">
                    <el-input v-model="lianbangForm.nameEn"></el-input>
                </el-form-item>
                <el-form-item prop="id" label="联邦方id">
                    <el-input type='id' autocomplete="new-password" v-model.number="lianbangForm.id"></el-input>
                </el-form-item>
            </el-form>
        </div>
        <div class="loginbtn">
            <el-button @click="confirm" type="primary">确定</el-button>
        </div>
        <div v-if="!secondFlag" @click="ordinary" class="tourist">
            跳过，游客身份浏览
        </div>
        <div v-else @click="secondClick" class="tourist">
            已现有联邦方身份加入
        </div>
    </div>
</template>
<script>
import {checkOnlyParty , lbregister } from '@/api/login/index.js'
import {joinParty , Partyedit, getFlUserPermission , queryPartyInfo , showFlUserRole} from '@/api/index.js'
import { getAuth, setAuth, setTenantId , setPartyInfo, setIndex , setUserRole} from '@/utils/auth.js'
export default {
    data() {
        return {
            joinfederal: '',
            creatfederal: '',
            errorMsg1: '',
            errorMsg2: '',
            secondFlag: false,
            rules: {
                nameEn: [
                    { required: true, message: '请输入联邦方英文名称', trigger: 'blur' },
                ],
                id: [
                    { required: true, message: '请输入联邦方id'},
                    { type: 'number', message: 'id必须为数字值'}
                ],
                creatfederal: [
                    { required: true, message: '请输入新建联邦方名称', trigger: 'blur' }
                ]
            },
            lianbangForm: {
                nameEn: '', 
                id: '',
                creatfederal: ''
            }
            
        }
    },
    created() {
        if(this.$route.query.second == 1) {
            this.secondFlag =  true
        }
    },
    methods: {
        // 现有身份进入主页
        secondClick() {
            this.queryPartyInfo()
        },
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
        },
        // 普通游客
        ordinary() {
            setTenantId(0)
            sessionStorage.setItem('tourist',true)
            this.$router.push({
                name: 'newhome'
            })
        },
        // 检查联邦方是否存在
        async checkOnlyParty() {
            let params = {
                name: this.lianbangForm.creatfederal,
                nameEn: this.lianbangForm.nameEn,
                id: this.lianbangForm.id
            }
            await checkOnlyParty(params).then(res=>{
                if(res.data.success) {
                     // 完善联邦方信息 
                    if(this.secondFlag){
                        this.$router.push({
                            name: "Info",
                            params: { 
                                name: this.lianbangForm.creatfederal,
                                nameEn: this.lianbangForm.nameEn,
                                id: this.lianbangForm.id,
                                query: {
                                    second: 1
                                }
                            }
                        }) 
                    } else {
                        this.$router.push({
                            name: "Info",
                            params: { 
                                name: this.lianbangForm.creatfederal,
                                nameEn: this.lianbangForm.nameEn,
                                id: this.lianbangForm.id,
                            }
                        })
                    }
                    
                }
               

            })
            .catch(res=>{
                
            })
        },
        // 联邦方注册
        async lbregister() {
            await lbregister().then(res=>{
                // this.$router.push({
                //     name: "BindSuccess"
                // })
            })
            .catch(res=>{
                
            })
        },
        // 通过邀请码加入联邦方
        async joinParty() {
            let data = new URLSearchParams()
            data.append('inviteCode', this.joinfederal)
            await joinParty(data).then(res=>{
                if(res.data.success) {
                    this.$message.success(res.data.message)
                    this.$router.push({
                        name: "BindJoin",
                        query:{
                            name: res.data.result
                        }
                    })
                }
                
            })
            .catch(res=>{
                
            })
        },
        //联邦方信息编辑
        async Partyedit () {
            await Partyedit().then(res=>{
                
            })
            .catch(res=>{
                
            })
        },
        creatfocus() {
            this.joinfederal = ''
        },
        joinfocus() {
            this.$refs["lianbangForm"].resetFields();
        },
        confirm() {
            if(this.joinfederal == ''&& this.lianbangForm.creatfederal == '') {
                this.errorMsg1 = '请输入邀请码'
                this.errorMsg2 = '请输入联邦方名称'
            } else {
                if(this.joinfederal == '') {
                    this.$refs["lianbangForm"].validate((valid) => {
                        if (valid) {
                            this.checkOnlyParty()
                        } else {
                            return false;
                        }
                    }) 
                } else  {
                    this.joinParty()
                }
            }
            
            
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
        margin-bottom: 60px;
    }
    .sub-title{
        margin-bottom: 20px;
    }
    .line{
        margin: 40px 0 30px;
    }
    .loginbtn{
        margin-top: 50px;
        .el-button{
            width: 100%;
            height: 60px;
            
        }
        .el-button--primary{
            font-size: 18px;
        }
        span{
                font-size: 18px;
            }
    }
    .tourist{
        text-align: right;
        margin-top: 20px;
        color: #0b80ea;
        cursor: pointer;
        text-decoration:underline;
    }
    
}
</style>
