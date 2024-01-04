<template>
    <div class="success-box">
        <div class="title">
            请完善联邦方信息
        </div>
        <div class="form">
            
            <el-form @submit.native.prevent :model="ruleFormData"  :rules="rules"  status-icon  ref="Data" label-width="0px" class="demo-Data">
                <div class="sub-title">新建联邦方</div>
                <el-form-item prop="name" :error="errorMsg1"  >
                   <el-input
                        clearable
                        v-model="ruleFormData.name"
                        >
                    </el-input>
                </el-form-item>
                <!-- <div class="sub-title">所需信息2</div> -->
                <!-- <el-form-item  :error="errorMsg2" >
                    <el-input
                        clearable
                        placeholder="提示语"
                        v-model="desc">
                    </el-input>
                </el-form-item> -->
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
import {upload, joinParty , Partyedit, getFlUserPermission , queryPartyInfo , showFlUserRole} from '@/api/index.js'
import { getAuth, setAuth, getTenantId, setTenantId, removeAuth , setPartyInfo, setIndex , setUserRole} from '@/utils/auth.js'
export default {
    
    data() {
        return {
            secondFlag: false,
            desc: '',
            errorMsg1: '',
            errorMsg2: '',
            imgurl: '',
            headers: {
                'Content-Type' : 'application/x-www-form-urlencoded'
            },
            ruleFormData: {
                name: '',
                nameEn:'',
                id: ''
            },
            rules:{
                name:[
                    { required: true, message: '联邦方名称不能为空'},
                ]
            },
        }
    },
    created() {
        this.ruleFormData.name = this.$route.params.name
        this.ruleFormData.nameEn = this.$route.params.nameEn
        this.ruleFormData.id = this.$route.params.id
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
                name: this.ruleFormData.name,
                nameEn: this.ruleFormData.nameEn,
                id: this.ruleFormData.id
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
	            "name": this.ruleFormData.name,
                "nameEn": this.ruleFormData.nameEn,
                "id": this.ruleFormData.id
            }
            await lbregister(params).then(res=>{
                if(res.data.success) {
                    this.$message.success(res.data.message)
                    this.$router.push({
                        name: 'BindSuccess',
                        params: {
                            name: this.ruleFormData.name
                        }
                    })
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
            const isJPG = file.type === 'image/jpeg'
            const isLt3M = file.size / 1024 / 1024 < 3

            // if (!isJPG) {
            //     this.$message.error('上传头像图片只能是 JPG 格式!')
            // }
            if (!isLt3M) {
                this.$message.error('上传头像图片大小不能超过 3MB!')
            }
            return  isLt3M
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
    }
    .form{
        margin-top: 60px;
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
        // color: #9a9a9a;
        cursor: pointer;
        margin-top: 20px;
        color: #0b80ea;
         text-decoration:underline;
    }
    .uploadimg{
        margin: 0px 0 20px 0;
        font-size: 18px;
        color:#484848;
        span{
            color: #9a9a9a;
        }
    }

    ::v-deep .el-form-item__error:before{
        display: inline-table !important;
        width: 16px !important;
        vertical-align: bottom !important;
        content: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAAXNSR0IArs4c6QAAAVpJREFUOE+NkjFLAmEYgJ9vUQcXnRoUBMupH6Gbp4GFQo21uDSEIOgkKEEKwi21WD8gqEAHvUn0P9RkCIEONekgyEVwcfd1pwdK3vTed+/zfu+97yNYe4xUag+4Ao4wjH3gByE+gDa6fiv6/S87XdiBoSineL0PKIqfeBzCYflpMoHhEDRtwff3uej1XsxjC7SgQOCRWg2i0fUmVvF4DJUKzOc5ExZWex7PO6rqJxJZJZbLMq7X3XCxaN58YII3ZDJl8nn3Tem0fO923eetFnQ61yb4iqoeEovtBo5GUCi8CUNRlrTbPjye3cDlErLZxXawVJKFGg13QQfc1urm2YLT6rbhbJqqWWxtOHIdzabftcNNrZq7tNfxJ0COYPCJavV/AWazE6Fp7ZVyyeQZPt+9pVwiAaGQ/MvpFAYDqZyuXwhNe3aUc3yVkl8CxxiG1MiWHO5Er/dp5/4Cf2iUZRZaSIcAAAAASUVORK5CYII=) !important;
    }
}
</style>
