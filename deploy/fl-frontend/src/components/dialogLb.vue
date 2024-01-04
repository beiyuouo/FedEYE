<template>
    <div class="form">
        <el-dialog
            title="请绑定联邦方"
            :visible.sync="bindlianbangVisible"
            :before-close='showClose'
            :append-to-body='true'
            width="30%"
            center>
            <el-form  @submit.native.prevent  status-icon 
             ref="ruleForm" label-width="0px" class="demo-ruleForm">
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
                <div class="sub-title">新建联邦方</div>
                <el-form-item  :error="errorMsg2">
                    <el-input
                        clearable
                        @focus='creatfocus'
                        placeholder="输入联邦方名称"
                        v-model="creatfederal">
                    </el-input>
                </el-form-item>
            </el-form>
            <div class="loginbtn">
                    <el-button @click="confirm" type="primary">确定</el-button>
                </div>
        </el-dialog>

    </div>
</template>
<script>
import {checkOnlyParty } from '@/api/login/index.js'
import {joinParty } from '@/api/index.js'
export default {
    props: {
        visible: Boolean
    },
    computed: {
        bindlianbangVisible: {
            get() {
                return this.visible
            },
            set (val) {
                this.$emit('updateBindlianbangVisible',val)
            }
        }
    },
    data() {
        return {
            joinfederal:'',
            creatfederal:'',
            errorMsg1: '',
            errorMsg2: ''
        }
    },
    created() {
        this.joinfederal = ''
        this.creatfederal = ''
        this.errorMsg1 = ''
        this.errorMsg2 = ''
    },
    methods: {
        showClose() {
            this.$emit(
                'handleClickbd',
            )
        },
         // 检查联邦方是否存在
        async checkOnlyParty() {
            let params = {
                name: this.creatfederal
            }
            await checkOnlyParty(params).then(res=>{
                if(res.data.success) {
                    //新建联邦方通过
                    this.$emit(
                        'partyCheck',
                        this.creatfederal
                    )
                    this.creatfederal = ''
                } else {
                }
               

            })
            .catch(res=>{
                this.errorMsg2 = res.message
            })
        },
        // 通过邀请码加入联邦方
        async joinParty() {
            let data = new URLSearchParams()
            data.append('inviteCode', this.joinfederal)
            await joinParty(data).then(res=>{
                if(res.data.success) {
                    this.$message.success(res.data.message)
                    // 通过邀请码加入
                    this.$emit(
                        'joincode',
                        res.data.result
                    )
                     this.joinfederal = ''
                }
            })
            .catch(res=>{
                
            })
        },
        confirm() {
            if(this.joinfederal == ''&& this.creatfederal == '') {
                this.errorMsg1 = '请输入邀请码'
                this.errorMsg2 = '请输入新建联邦方名称'
                return
            } else {
                if(this.joinfederal == '') {
                    this.checkOnlyParty()
                } else  {
                    this.joinParty()
                }
            }
            
        },
        creatfocus() {
            this.joinfederal = ''
        },
        joinfocus() {
            this.creatfederal= ''
        },
    }
}
</script>
<style lang="scss" scoped>
        ::v-deep .el-dialog__body{
            padding: 40px 100px;
        }
        ::v-deep .el-dialog__title{
            font-weight: 700;
        }
        ::v-deep .el-divider--horizontal{
                height: 0;
        }
        ::v-deep .el-divider{
                background-color: #fff;
        }
        ::v-deep .el-button {
            width: 100%;
            height: 50px;
            background-color: #e01622;
            font-size: 18px;
        }
        .sub-title{
            color: #484848;
            font-style: 18px;
            margin-bottom: 20px;
        }
        .line{
            border: 1px dashed  #CACACA;
            color: #9a9a9a;
            font-size: 14px;
            margin: 40px 0 20px;
            // margin: 0px 60px 50px;
        }
    .form{
        margin-top: 60px;
        
       
    }
    .loginbtn{
        margin-top: 50px;
        cursor: pointer;
        width: 100%;
        
    }
    .tourist{
        text-align: right;
        font-size: 14px;
        color: #9a9a9a;
        margin-top: 20px;
        cursor: pointer;
    }
</style>
