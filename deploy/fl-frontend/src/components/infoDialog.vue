<template>
    <el-dialog
            title=" 请完善联邦方信息"
            :visible.sync="infoVisible"
            width="30%"
            :show-close='false'
            :before-close='showClose'
            :append-to-body='true'
            center>
            
            <div class="success-box">
                <div class="form">
                    
                    <el-form  @submit.native.prevent :model="ruleFormData"  :rules="rules"  status-icon  ref="Data" label-width="0px" class="demo-Data">
                        <div class="sub-title">新建联邦方</div>
                        <el-form-item prop="name" :error="errorMsg1"  >
                        <el-input
                                clearable
                                v-model="ruleFormData.name"
                                >
                            </el-input>
                        </el-form-item>
                        <el-form-item>
                            <div class="uploadimg">
                                上传头像
                                <span>(可选)</span>
                            </div>
                            <el-upload
                                class="avatar-uploader"
                                action="123"
                                :headers="headers"
                                :http-request="upLoadAvatarImg"
                                :show-file-list="false"
                                :on-success="handleAvatarSuccess"
                                :before-upload="beforeUpload"
                                >
                                <el-avatar :src="imgurl"></el-avatar>
                            </el-upload>
                            
                        </el-form-item>
                    </el-form>
                </div>
                <div class="loginbtn">
                    <el-button @click="confirm" type="primary">确定</el-button>
                </div>
            </div>
        
        </el-dialog>
    
</template>
<script>
import {upload} from '@/api/index.js'
import {checkOnlyParty , lbregister } from '@/api/login/index.js'

export default {
    data() {
        return {
            
            desc: '',
            errorMsg1: '',
            errorMsg2: '',
            imgurl: '',
            headers: {
                'Content-Type' : 'application/x-www-form-urlencoded'
            },
            ruleFormData: {
                name: '',
            },
            rules:{
                name:[
                    { required: true, message: '联邦方名称不能为空'},
                ]
            },
            
            
        }
    },
    props: {
        visible: Boolean,
        dialogName: String
    },
    computed: {
        infoVisible: {
            get() {
                return this.visible
            },
            set (val) {
                this.$emit('updateInfoVisible',val)
            }
        }
    },
    watch:{
        dialogName(newdata,olddata) {
            this.ruleFormData.name = newdata
        }
    },
    created() {
        this.ruleFormData.name = this.$route.params.name
    },
    methods: {
        showClose() {
            this.$emit(
                'handleClickinfo',
            )
        },
        // 检查联邦方是否存在
        async checkOnlyParty() {
            let params = {
                name: this.ruleFormData.name
            }
            await checkOnlyParty(params).then(res=>{
                if(res.data.success) {
                    this.lbregister()
                } else {
                    this.errorMsg1 = res.data.message
                }
            })
            .catch(res=>{
                
            })
        },
        // 联邦方注册
        async lbregister() {
            let params = {
                "avatar": this.imgurl,
	            "partyname": this.ruleFormData.name,
                "nameEn":this.nameEn,
                "id": this.id
            }
            await lbregister(params).then(res=>{
                if(res.data.success) {
                    this.$message.success(res.data.message)
                    this.$emit(
                        'registerSuccess',
                        this.ruleFormData.name
                    )
                } else {
                    this.$message.error(res.data.message)
                }
            })
            .catch(res=>{
                
            })
        },
        confirm() {
            this.$refs.Data.validate((valid) => {
                if (valid) {
                    let that = this;
                    that.checkOnlyParty()
                } else {
                    return false;
                }
            });
           

        },
        handleAvatarSuccess(res, file) {
            this.imgurl = URL.createObjectURL(file.raw);
        },
        upLoadAvatarImg(file) {
            const formData = new FormData();
            formData.append('file', file.file);
            formData.append('biz','party');
            upload(formData).then(res=>{
                if(res.data.success) {
                    this.imgurl = this.$url+res.data.message
                }
            })
            .catch(res=>{
              
            })

        },
        beforeUpload(file) {
            const isLt3M = file.size / 1024 / 1024 < 3
            if (!isLt3M) {
                this.$message.error('上传头像图片大小不能超过 3MB!')
            }
            return isJPG && isLt3M
        }
    }
}
</script>
<style lang="scss" scoped>
.sub-title{ 
    color: #484848;
    font-style: 18px;
    margin-bottom: 20px;
}
::v-deep .el-form-item{
    margin-bottom: 30px;
}
.line{
    border: 1px dashed  #CACACA;
    color: #9a9a9a;
    font-size: 14px;
    margin: 40px 0 20px;
    // margin: 0px 60px 50px;
}
::v-deep .el-divider--horizontal{
        height: 0;
}
::v-deep .el-divider{
        background-color: #fff;
}
::v-deep .el-form-item__error:before{
    display: inline-table !important;
    width: 16px !important;
    vertical-align: bottom !important;
    content: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAAXNSR0IArs4c6QAAAVpJREFUOE+NkjFLAmEYgJ9vUQcXnRoUBMupH6Gbp4GFQo21uDSEIOgkKEEKwi21WD8gqEAHvUn0P9RkCIEONekgyEVwcfd1pwdK3vTed+/zfu+97yNYe4xUag+4Ao4wjH3gByE+gDa6fiv6/S87XdiBoSineL0PKIqfeBzCYflpMoHhEDRtwff3uej1XsxjC7SgQOCRWg2i0fUmVvF4DJUKzOc5ExZWex7PO6rqJxJZJZbLMq7X3XCxaN58YII3ZDJl8nn3Tem0fO923eetFnQ61yb4iqoeEovtBo5GUCi8CUNRlrTbPjye3cDlErLZxXawVJKFGg13QQfc1urm2YLT6rbhbJqqWWxtOHIdzabftcNNrZq7tNfxJ0COYPCJavV/AWazE6Fp7ZVyyeQZPt+9pVwiAaGQ/MvpFAYDqZyuXwhNe3aUc3yVkl8CxxiG1MiWHO5Er/dp5/4Cf2iUZRZaSIcAAAAASUVORK5CYII=) !important;
}
.success-box{
    height: 100%;
    background-color: #fff;
    padding: 0 50px;
    .title{
        color: #484848;
        font-size: 40px;
        font-weight: 700;
        padding-top: 140px;
    }
    .form{
        margin-top: 60px;
        
    }
    .loginbtn{
        margin-top: 50px;
        cursor: pointer;
        width: 100%;
        ::v-deep .el-button {
            width: 100%;
            height: 50px;
            background-color: #e01622;
            font-size: 18px;
        }
    }
    .tourist{
        text-align: right;
        font-size: 14px;
        color: #9a9a9a;
        margin-top: 20px;
        cursor: pointer;
    }
    .uploadimg{
        margin: 0px 0 20px 0;
        font-size: 18px;
        color:#484848;
        span{
            color: #9a9a9a;
        }
    }

    
}
</style>
