<template>
    <div>
        <el-dialog
            title="新建任务"
            :visible.sync="creatVisible"
            width="1200"
            :before-close='beforeCloseClick'
            :show-close='false'
            center
            >
            <el-form @submit.native.prevent ref="form" :model="form" :rules="rules" label-width="auto">
                <el-form-item label="任务名称" prop="name">
                    <el-input show-word-limit  maxlength="20" clearable placeholder='请输入任务名称' v-model="form.name"></el-input>
                </el-form-item>
                <el-form-item label="任务描述" prop="text">
                    <el-input
                        type="textarea"
                        placeholder="请输入内容"
                        v-model="form.text"
                        maxlength="300"
                        :rows="4"
                        show-word-limit
                        >
                    </el-input>
                </el-form-item>
                <el-form-item label="科研类型" prop="sctype">
                    <el-radio-group v-model="form.sctype">
                        <el-radio :label='2'>独立科研</el-radio>
                        <el-radio :label='1'>联邦科研</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="是否公开" prop="resource">
                    <el-radio-group v-if="flType==4" v-model="form.resource">
                        <el-radio label="0">公开</el-radio>
                        <el-radio label="2">秘密</el-radio>
                    </el-radio-group>
                    <el-radio-group v-else v-model="form.resource">
                        <el-radio label="0">公开-任意加入</el-radio>
                        <el-radio label="1">公开-申请/邀请加入</el-radio>
                        <el-radio label="2">秘密</el-radio>
                    </el-radio-group>
                </el-form-item>
                <!-- <el-form-item label="选择数据" prop="value">
                    <el-select v-model="form.value" placeholder="请选择数据">
                        <div v-if="slectData" 
                            v-infinite-scroll='loadMore'
                            infinite-scroll-delay='200'
                            >
                            <el-option v-for="(item,index) in slectData "
                                @click.native="slectDataItem(item)"
                               
                                :key="index"
                                :label="item.name"
                                :value="item.id"
                                >
                            </el-option>
                        </div>
                    </el-select>
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
                </el-form-item> -->
                <el-form-item label="上传头像">
                    <el-upload
                        class="avatar-uploader"
                        action="123"
                        :headers="headers"
                        :http-request="upLoadAvatarImg"
                        :show-file-list="false"
                        :on-success="handleAvatarSuccess"
                        :before-upload="beforeUpload"
                        >
                        <el-avatar :src="imgurl">
                            <img src="@/assets/avatar.png" alt="">
                        </el-avatar>
                    </el-upload>
                </el-form-item>
                <!-- <el-form-item label="选择模型" prop="type">
                    <el-select  v-model="form.type" placeholder="请选择模型">
                        <el-option v-for="(item,index) in typeList1" 
                            :key="index"
                            :label="item.name" 
                            :value="item.name"></el-option>
                    </el-select>
                </el-form-item> -->
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button class="save" type="primary"  @click="createTask">{{flType==4?'提交':"保存"}}</el-button>
                <el-button  plain  @click="beforeCloseClick">取消</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import { queryAlgorithm, createJob , createLocalJob} from '@/api/task'
import { upload } from '@/api/index'
import { getTenantId } from '@/utils/auth.js'
export default {
    inject:['reload'],
    props: {
        visible: Boolean,
        slectData: Array,
        typeList: Array,
        flType: String,
        algId: String,
    },
    data() {
        return {
            algid: '',
            flagQuest: true,
            activeName: 'first',
            form:{
               name:'',
               resource:'0',
               type:'',
               data:'',
               text:'',
               value: '',
               sctype:1
            },
            headers: {
                'Content-Type' : 'application/x-www-form-urlencoded'
            },
            typeList1:[],
            imgurl: '',
            tableData1: [],
            tableData2: [],
            datatypeFlag: false,
            rules: {
                name: [
                    { required: true, message: '请输入任务名称', trigger: 'blur' },
                ],
                text: [
                    { required: true, message: '请输入任务描述', trigger: 'blur' }
                ],
                resource: [
                    { required: true, message: '请选择是否公开', trigger: 'change' }
                ],
                value: [
                    { required: true, message: '请选择数据', trigger: 'change' }
                ],
                type: [
                   { required: true, message: '请选择模型', trigger: 'change' }
                ],
                sctype: [
                   { required: true, message: '请选择科研类型', trigger: 'change' }
                ],
               
            }
        }
    },
    
    computed: {
        creatVisible: {
            get() {
                return this.visible
            },
            set (val) {
                this.$emit('updateJoinVisible',val)
            }
        }
    },
    watch:{
        typeList(newValue,oldValue) {
            if(newValue.length==0) {
                this.flagQuest= true
            } else {
                this.flagQuest= false
                this.typeList1  = newValue
                this.form.type = this.typeList[0].name
            }
            
        },
        algId: {
            handler(newValue,oldValue) {
                this.algid = newValue
               
            }
        }
    },
    methods: {
        handleClick() {

        },
        //
        loadMore() {
            this.$emit('loadMoreData')
        },
         // 关闭dig
        beforeCloseClick() {
             this.$refs["form"].resetFields();
             this.$emit('cancleCreateClick')
             this.datatypeFlag = false
        },
        // 新建任务btn
        createTask() {
            this.$refs["form"].validate((valid) => {
                if (valid) {
                    if(this.flType==4){
                        // 非联邦任务
                        this.createLocalJob()
                    } else if(this.flType ==5) {
                        
                    } else {
                        // 联邦任务  以前的流程
                        this.createJob()
                    }
                    
                } else {
                    return false;
                }
            });
            
        },
        // 创建任务
        async createJob() {
            let id  = getTenantId()
            let params = {
                avatar: this.imgurl,  // 联邦方头像
                // compoments:JSON.stringify([{name: this.form.type}]),  // 	算法组件
                content: this.form.text,   // 任务描述
                name: this.form.name,   // 任务名称
                partyId: id,   // 联邦方id
                permission: this.form.resource,   // 任务权限   0 任意加入  1 申请加入  2  私密
                recruitStatus: 0,   // 招募状态，0招募中、1任务提交成功、2任务提交失败、3训练成功、4训练失败
                jobType: this.form.sctype  // 1 联邦 2 非联邦
                // 加入任务使用下面参数
                // flJobRegistList: [{dataId:this.form.value,partyId:id}] ,
                
            }
            await createJob(params).then(res=>{
                if(res.data.success) {
                    this.$message.success(res.data.message)
                    this.dialogVisible = false
                    // this.recruitId = 
                    this.$router.push({
                        name: 'MytaskDetail',
                        query: {
                            id: res.data.result,
                            role:'guest',
                            algid: this.algid
                        }
                    })
                    // this.reload()
                }
            })
            .catch(res=>{
                
            })
        },
        async createLocalJob() {
            let id  = getTenantId()
            let params = {
                avatar: this.imgurl,  // 联邦方头像
                // compoments:JSON.stringify([{name: this.form.type}]),  // 	算法组件
                content: this.form.text,   // 任务描述
                name: this.form.name,   // 任务名称
                partyId: id,   // 联邦方id
                permission: this.form.resource,   // 任务权限   0 任意加入  1 申请加入  2  私密
                recruitStatus: 0,   // 招募状态，0招募中、1任务提交成功、2任务提交失败、3训练成功、4训练失败
                // 加入任务使用下面参数
                // flJobRegistList: [{dataId:this.form.value,partyId:id}] ,
                
            }
            await createLocalJob(params).then(res=>{
                if(res.data.success) {
                    this.$message.success(res.data.message)
                    this.dialogVisible = false
                    this.reload()
                }
            })
            .catch(res=>{
                
            })
        },
        // 算法选择
        async queryAlgorithm(evalType) {
            let params ={
                evalType: evalType
            }
            await queryAlgorithm(params).then(res=>{
                if(res.data.success) {
                    this.typeList1 = res.data.result
                }

            })
            .catch(res=>{
               
            })
        },
        slectDataItem(obj) {
            this.tableData1 = []
            this.tableData2 = []
           
            if(this.typeList.length==0) {
                this.queryAlgorithm(obj.evalType)
            }
            
            this.tableData1 = obj.columns.split(",")
            this.tableData1.filter(item=>{
                this.tableData2.push(JSON.parse(obj.columnType)[item])
            })
            this.datatypeFlag = true
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
        handleAvatarSuccess(res, file) {
            this.imgurl = URL.createObjectURL(file.raw);
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
            return isLt3M
        },
    }
    
}
</script>

<style lang="scss" scoped>
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
.save{
       
    margin-right: 20px;
    background: #e01622;
    ::v-deep .el-button--primary{
        // padding: 12px 30px;
        // border-radius: 10px;
        
        // border-radius: 4px;
    }
}
::v-deep .el-card__body{
    padding: 0;
}
::v-deep .el-select{
    width: 100%;
}
.digbox{
    height: 400px;
    overflow: auto;
}
::v-deep .el-dialog__body {
    overflow: auto;
}
.photo-box{
    height: 300px;
    padding: 30px;
    display: flex;
    overflow: auto;
    // background-color: #ccc;
    border: 1px solid #ccc;
    border-radius: 10px;
    .photo-left {
        flex: 1;
        .radiu-box{ 
            position: relative; 
            width: 100px;
            height: 92px;
            cursor: pointer;
            background: linear-gradient(to right, #ededf6,#fff);
            box-shadow: 0px 3px 15px 9px rgba(226,226,226,0.29);
            margin-bottom: 40px;
            .content-box{ 
                position: absolute;
                top: 20px;
                width: 100%;
                height: 100%;
                text-align: center;
                .content-title{
                        margin-top: 20px;
                    // height: 40px;
                    font-size: 12px;
                    // font-weight: 700;
                }
                .content-footer{
                    
                    margin-top: 50px;
                    ::v-deep .el-tag {
                        margin-right: 10px;
                    }
                }
            }
        }

        .radiu-box:before{
            position: absolute; 
            top:-10px; 
            content: "";
            width: 100px; 
            height: 20px; 
            border-radius:50%; 
            background: #fff;
            box-shadow: 0px 3px 15px 9px rgba(226,226,226,0.29);
        }

        .radiu-box:after{
            position: absolute; 
            bottom:-10px; 
            content: ""; 
            width: 100px; 
            height: 20px; 
            border-radius:50%; 
            background: linear-gradient(to right, #ededf6,#fff);
            // box-shadow: 0px 3px 15px 9px rgba(226,226,226,0.29);
        }
    }
    ::v-deep .el-divider--vertical{
        height: 100%;
    }
    .photo-right {
        flex: 1;
    }

}

</style>
