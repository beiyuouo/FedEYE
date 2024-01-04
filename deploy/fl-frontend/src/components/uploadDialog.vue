<template>
    <el-dialog
        v-loading="loading"
        element-loading-text="数据分析中"
        element-loading-spinner="el-icon-loading"
        element-loading-background="rgba(0, 0, 0, 0.8)"
        :title="title"
        center
        :modal-append-to-body='false'
        :show-close='false'
        :close-on-click-modal='false'
        :close-on-press-escape = 'false'
        :visible.sync="uploadStepVisible"
        width="1200"
        
        :before-close="beforeCloseClickStep">
        <div class="box">
            <el-form @submit.native.prevent ref="ruleForm"  :rules="rules"  label-width="100px" :model="form">
                <el-form-item label="上传数据集">
                    <el-input class="inputUrl" disabled v-model="form.path"></el-input>
                    <div class="btnBox">
                        <el-upload
                            class="upload-demo"
                            :show-file-list='false'
                            :headers="headers"
                            :http-request="upLoadAvatarImg"
                            :on-success="uploadSuccess"
                            action='#'
                            multiple>
                           
                            <el-button class="uploadBtn" type="primary">重新上传</el-button>
                        </el-upload>
                        
                    </div>
                    <div v-show="datatypeFlag" class="content-box">
                        <div class="title">请确认该数据集的数据类型、格式符合预期：</div>
                        <div class="table-box">
                            <table  class="myTable">
                                <tbody>
                                    <tr>
                                        <td  class="theader">数据名称</td>
                                        <td v-for="(item,index) in tableData1" :key="index" class="column">{{ item }}</td>
                                    </tr>
                                    <tr>
                                        <td  class="theader">数据格式</td>
                                        <td v-for="(item,index) in tableData2" :key="index" class="column">{{ item }}</td>
                                    </tr>
                                </tbody>
                            
                            </table>
                        </div>
                    </div>
                </el-form-item>
                <el-form-item label="数据集名称">
                    <el-input v-model="form.name" placeholder="请输入名称"></el-input>
                </el-form-item>
                <el-form-item label="简介">
                    <el-input
                        class="textareaCs"
                        type="textarea"
                        placeholder="请输入数据简介"
                        v-model="form.note"
                        maxlength="300"
                        show-word-limit
                        >
                    </el-input>
                </el-form-item>
            </el-form>
        </div>
        
        
        <div class="btn">
            
            <el-button type="primary" slot="reference"  @click="saveData">确认</el-button>
            <el-button @click="beforeCloseClickStep">取消</el-button>
        </div>
        <el-dialog
            v-loading="loading1"
            element-loading-text="数据分析中"
            element-loading-spinner="el-icon-loading"
            element-loading-background="rgba(0, 0, 0, 0.8)"
            title=''
            center
            append-to-body
            :show-close='false'
            :close-on-click-modal='false'
            :close-on-press-escape = 'false'
            :visible.sync="showDbqIsible"
            width="600"
            >
            <span>数据集已存在，请确认是否覆盖上传！</span>
            <span slot="footer" class="dialog-footer">
                <el-button @click="showDbqIsible = false">取 消</el-button>
                <el-button type="primary" @click="secondClickSave">确 定</el-button>
            </span>
        </el-dialog>
    </el-dialog> 
</template>
<script>
import { saveUploadData,uploadData, searchByName } from '@/api/index'  
import { getTenantId } from '@/utils/auth.js'
export default {
    inject:['reload'],
    props: {
        uploadStepVisible: Boolean,
        successObj: Object
    },
    data() {
        return {
            title: '添加数据集',
            showDbqIsible: false,
            textarea: '',
            form: {
                name: '',
                path: '',
                note: ''
            },
            headers: {
                'Content-Type' : 'application/x-www-form-urlencoded'
            },
            datatypeFlag: false,
            tableData1: [],
            tableData2: [],
            uploadUrl: '',
            rules: {
                name: [
                    { required: true, message: '请输入数据集名称', trigger: 'blur' },
                    
                ]
            },
            loading: false,
            loading1: false
        }
    },
    created() {
        this.uploadUrl = sessionStorage.getItem("uploadUrl")
    },
    methods: {
         // 关闭dig
        beforeCloseClickStep() {
            this.$emit('cancleCreateClickStep')
        },
        upLoadAvatarImg(file) {
            
            let fileName = file.file.name.substring(file.file.name.lastIndexOf('.')+1)
            const extension = fileName === 'zip'
            if(!extension) {
                this.$message({
                    message: '上传文件只能是zip格式!',
                    type:'warning'
                })
                return
            } else {
                this.loading = true
                this.tableData1 = []
                this.tableData2 = []
                const formData = new FormData();
                formData.append('file', file.file);
                uploadData(formData,this.uploadUrl,function(progressEvent){
                    file.onProgress({percent: progressEvent.loaded / progressEvent.total *100})     
                }).then(res=>{
                    if(res.data.success) {
                        let obj = JSON.parse(JSON.stringify(res.data.result))
                        this.form = obj
                        this.loading = false
                        if(obj.contentType=='vision') {
                            // 图片数据集
                            this.datatypeFlag = false
                        } else if(obj.contentType=='nlp') {
                            // 文本数据
                             this.datatypeFlag = false
                        } else if(obj.contentType=='graph')  {
                             // 图神经数据
                             this.datatypeFlag = false
                        } else {
                            this.tableData1 = obj.columns.split(",")
                            this.tableData1.filter(item=>{
                                this.tableData2.push(JSON.parse(obj.columnType)[item])
                            })
                            this.datatypeFlag = true
                        }
                        
                    }
                })
                .catch(res=>{
                    
                })
            }

        },
        uploadSuccess(response, file) {
           
            
        },
        secondClickSave() {
            this.loading1 = true
            this.saveUploadData()
        },
        saveData() {
            this.loading = true
            this.$refs["ruleForm"].validate((valid) => {
                if (valid) {
                    this.searchByName()
                } else {
                    return false;
                }
            })
        },
        async saveUploadData() {
            
            this.form.partyId = getTenantId()
            // partyName
            this.form.createParty = sessionStorage.getItem('nameEn')
            this.form.eggrollNamespace = sessionStorage.getItem('nameEn')
       
            await saveUploadData(this.form,this.uploadUrl).then(res=>{
                if(res.data.code == 200) {
                    this.loading = false
                    this.loading1 = false
                    this.$message.success(res.data.message)
                    this.reload()
                }
            })
            .catch(res=>{
                console.log(res)
            })   
        },
        // 校验数据集名称
        async searchByName() {
            
            let id = getTenantId()
            let params = {
                name: this.form.name,
                
            }
            if(this.form.name == '') {
            }
            await searchByName(params,id).then(res=>{
                console.log(res)
                if(res.data.code == 10010) {
                    // 系统异常  请重新上传
                    this.$message.error('请重新上传')
                    this.reload()
                }  else  if(res.data.result.length==0){
                    // 数据集不存在
                    this.saveUploadData()
                } else {
                    // 数据集存在  弹框让用户确认
                    this.loading = false
                    this.showDbqIsible = true
                }
            })
            .catch(res=>{
                console.log(res)
            })
        }
    },
    watch: {
        successObj(newValue,oldValue) {
            if(newValue) {
                this.form = newValue
                if(newValue.contentType=='vision') {
                    // 图片数据集
                    this.datatypeFlag = false
                } else if(newValue.contentType=='tabular') {
                    //csv数据
                    this.tableData1 = newValue.columns.split(",")
                    this.tableData1.filter(item=>{
                        this.tableData2.push(JSON.parse(newValue.columnType)[item])
                    })
                    this.datatypeFlag = true
                } else if(newValue.contentType=='nlp') {
                    // 文本数据
                    this.datatypeFlag = false
                } else if(newValue.contentType=='graph') {
                    // 图神经 数据
                     this.datatypeFlag = false
                }
            }
            
        }
    }
}
</script>
<style lang="scss" scoped>
::v-deep .el-dialog__title{
    font-weight: 700;
}
::v-deep .el-dialog__body{
    padding: 30px 60px;
}
::v-deep .el-dialog__header{
    padding: 40px 0 0;
}   
.box{
   
    // height: 374px;
    width: 100%;
    .inputUrl{
        width: 80%;
    }
    .btnBox{
        display: inline-block;
        width: 20%;
        text-align: right;
        .uploadBtn{
            width: 120px;
        }
    }
    
    .textareaCs{
        height: 156px;
        ::v-deep .el-textarea__inner{
            height: 100%;
        }
    }
}
    
    .btn{
        text-align: center;
        margin-top: 40px;
        .el-button{
            width: 200px;
        }
    }
.myTable {
    border-collapse: collapse;
    border-spacing: 0;
    margin: 0 auto;
    width: 1400px;
    text-align: center;
    .theader{
        background-color: #D2E4F5;
    }
    tbody {
        display: block;
        height: 100px;
        // width: 1400px;

        
        tr {
            display: table;
            width: 100%;
            table-layout: fixed;
            td{
                width: 100px;
                border: 1px solid #cad9ea;
                color: #666;
                height: 40px;
            }
        }
    }
}
.table-box::-webkit-scrollbar {
    width: 3px;
    height: 5px;
}
.table-box::-webkit-scrollbar-track {
    width: 3px;
    height: 5px;
    background-color: rgba(102, 103, 104, 0.5);
}
.table-box::-webkit-scrollbar-track-piece{
    width: 3px;
    height: 3px;
}
.table-box::-webkit-scrollbar-thumb {
    background-color: rgba(102, 103, 104, 0.5);
}
.table-box{
    overflow: auto;
}
</style>
