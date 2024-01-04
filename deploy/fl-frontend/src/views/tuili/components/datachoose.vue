<template>
    <div class="container">
        <line-box :active='active'></line-box>
        <div class="header">
            <div class='img-box'>
                <span class="img-name active-modal">
                    {{modelName}} 
                </span>
                
            </div>
            <span class="line"></span>
            <div class='img-box'>
                <span class="img-name data-box">
                    推理数据
                </span>
            </div>
        </div>
        <div class="content">
            <div class="content-header">
                请填入数据 ：
            </div>
            <div class="radio-box">
                <el-radio-group  v-model="resource">
                    <el-radio label="0">
                       逐条录入
                    </el-radio>
                    <!-- <el-radio label="1">json格式录入</el-radio> -->
                    <el-radio v-if="!showNlpFlag" label="2">我的数据导入</el-radio>
                </el-radio-group>
            </div>
            <el-tabs v-model="resource">
                <el-tab-pane name="0">
                     <content-box v-if="showTabFlag">
                         <el-form @submit.native.prevent label-position="left" :inline="true" ref="form">
                            <el-form-item 
                                v-for="(item,index) in copyheader"
                                :key="index"
                                :label="item.label">
                                <el-input v-model="item.value"></el-input>
                            </el-form-item>
                        </el-form>
                        <!-- <div>
                            <el-divider></el-divider>
                            <span @click='addForm'>输入另一条数据</span>
                        </div> -->
                        
                    </content-box>
                    <content-box v-if="showPicFlag">
                        <div class="box">
                            <el-upload
                                class="upload-demo"
                                ref="upload"
                                action="."
                                :http-request="uploadFile"
                                accept=".jpg,.jpep,.png"
                                :on-preview="handlePreview"
                                :on-remove="handleRemove"
                                :on-change="uploadChange"
                                :file-list="fileList"
                                list-type="picture"
                                :auto-upload="false">
                                <img slot="trigger"  style="width:100%;height:100%;" src="@/assets/uploadpic.jpg" >
                            </el-upload>
                        </div>
                    </content-box>
                    <content-box v-if="showNlpFlag">
                        <div>
                            <el-input
                                type="textarea"
                                :rows="2"
                                placeholder="请输入nlp内容"
                                v-model="nlpValue">
                            </el-input>
                        </div>
                    </content-box>
                    <content-box v-if="showGrashFlag">
                        <div>
                            <el-form label-width="80px" @submit.native.prevent label-position="left" ref="form">
                                <el-form-item 
                                    v-for="(item,index) in grashObj"
                                    :key="index"
                                    :label="item.label">
                                    <el-input type="textarea"  v-model="item.value"></el-input>
                                </el-form-item>
                            </el-form>
                        </div>
                    </content-box>
                </el-tab-pane>
                <!-- <el-tab-pane name="1">
                    <content-box>
                        json格式录入
                    </content-box>
                </el-tab-pane> -->
                <el-tab-pane name="2">
                    <content-box>
                        <div class="form-box">
                            <el-form @submit.native.prevent>
                                <el-form-item label="选择数据">
                                    <el-select v-model="datavalue" placeholder="请选择数据">
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
                                </el-form-item>
                            </el-form>
                        </div>
                    </content-box>
                </el-tab-pane>
            </el-tabs>
        </div>
        <div class="btn">
            <el-button :loading="loading"  @click="startTaskClick()"  type="primary">开始推理任务</el-button>
        </div>
    </div>
</template>

<script>
    import lineBox from './line.vue'
    import contentBox from './content.vue'
    import { listCurrent } from '@/api/task' 
    import {  getTenantId } from "@/utils/auth"
    import { inference ,inferenceImage, inferenceByDataCollection ,inferenceNlp,inferenceGraph} from '@/api/index'
    export default {
        props: {
            modelName: String,
            ferenceObj: Object,
            taskId: String,
            contentType: String

        },
        components:{
            lineBox,
            contentBox
        },
        data() {
            return {
                resource: '',
                active: '1',
                datavalue: '',
                slectData: [],
                tableData1: [],
                tableData2: [],
                datatypeFlag: false,
                hasMore: true,
                count: 1,
                copyheader:[],
                serviceId: '',
                featureData: {},
                showPicFlag: false,
                fileList:[],
                copyFileList: [],
                copyData:{},
                CopyContentType: '',
                dataId: '',
                firstHeader: [],
                loading: false,
                showTabFlag: false,
                showNlpFlag: false,
                showGrashFlag: false,
                nlpValue: '',
                edgeValue: '',
                xValue:'',
                grashObj: [
                    {
                        label: 'x',
                        value: ''
                    },
                    {
                        label: 'edge_index',
                        value: ''
                    }
                ]
                // headers: {
                //     'Content-Type' : 'application/json'
                // },
                // formData: {

                // }
            }
        },
        created() {
            this.listCurrent()
            
        },
        methods: {
            // 
            addForm() {
                this.copyheader.push(...this.firstHeader)
            },
            // 数据集推理
            async inferenceByDataCollection() {
                let data = new URLSearchParams()
                data.append('id',this.taskId)
                data.append('dataId',this.dataId)
                await inferenceByDataCollection(data,getTenantId()).then(res=>{
                    if(res.data.code == 200) {
                        this.loading = false;
                        this.$message.success('开始推理任务成功')
                        this.$emit('taskSuccess')
                    }
                })
                .catch(err=>{
                    this.loading = false
                })
            },
            // uploadFile
            uploadFile(file) {
                this.formData.append('files', file.file);
            },
            async inferenceImage() {
                this.formData = new FormData();
                this.$refs.upload.submit()
                this.formData.append('id', this.taskId);
                await inferenceImage(this.formData,getTenantId()).then(res=>{
                    if(res.data.code == 200) {
                        this.loading = false;
                        this.$message.success('开始推理任务成功')
                        this.$emit('taskSuccess')
                    }
                })
                .catch(err=>{
                    this.fileList = []
                    this.copyFileList= []
                    this.loading = false
                })
            },
             // npl 推理
            async inferenceNlp() {
                let arr = []
                arr.push(this.featureData)
                let data= {
                    id: this.taskId,
                    content: this.nlpValue
                }
                await inferenceNlp(data,getTenantId()).then(res=>{
                    if(res.data.code == 200) {
                        this.loading = false;
                        this.$message.success('开始推理任务成功')
                        this.$emit('taskSuccess')
                    } else {
                        this.$message.error(data.msg)
                    }
                })
                .catch(err=>{
                    this.loading = false
                })
            },
             // 图神经 推理
            async inferenceGraph() {
                let arrList = {}
                this.grashObj.forEach((item)=>{
                    console.log(typeof(item.value))
                    arrList[item.label] = JSON.parse(item.value) 
                })
                let featureDataArray = []
                featureDataArray.push(arrList)
                let data= {
                    id: this.taskId,
                    featureDataList: featureDataArray
                }
                await inferenceGraph(data,getTenantId()).then(res=>{
                    if(res.data.code == 200) {
                        this.loading = false;
                        this.$message.success('开始推理任务成功')
                        this.$emit('taskSuccess')
                    } else {
                        this.$message.error(res.data.msg)
                        this.loading = false;
                    }
                })
                .catch(err=>{

                }) 
            },
            uploadChange(file, fileList) {
                
                this.fileList = []
                this.copyFileList = []
                this.fileList = fileList;
            },
            handleRemove(file, fileList) {
                this.fileList = []
                this.copyFileList= []
                this.fileList = fileList;
            },
            handlePreview(file) {
                
            },
            // 开启推理任务
            async inference() {
                let arr = []
                arr.push(this.featureData)
                let data= {
                    id: this.taskId,
                    featureDataList: arr
                }
                // let data = new URLSearchParams()
                // data.append('id',this.taskId)
                // data.append('featureData',this.featureData)
                await inference(data,getTenantId()).then(res=>{
                    if(res.data.code == 200) {
                        this.loading = false;
                        this.$message.success('开始推理任务成功')
                        this.$emit('taskSuccess')
                    }
                })
                .catch(res=>{
                    this.loading = false
                })
            },
            // 开始推理任务
            startTaskClick () {
                this.loading = true
                if(this.resource == 0) {
                    if(this.CopyContentType == 'tabular') {
                        // csv 文件
                        this.copyheader.forEach((item)=>{
                            this.featureData[item.label] = item.value-0
                        
                        })
                        
                        this.inference()
                    } else if(this.CopyContentType == 'vision') {
                        //图片文件
                        this.fileList.forEach((item)=>{
                            // console.log(item)
                            this.copyFileList.push(item.raw)
                        })
                        // console.log(this.copyFileList)
                        this.inferenceImage()
                    } else if(this.CopyContentType == 'nlp') {
                        this.inferenceNlp()
                    }  else if (this.CopyContentType == 'graph') {
                        this.inferenceGraph()
                    }
               
                } else if(this.resource == 2) {
                    this.inferenceByDataCollection()
                }

            },
            // 数据选择
            async listCurrent() {
                let id  = getTenantId()
                let params = {
                    name: '',
                    pageNo: this.count,
                    pageSize:'10'

                }
                await listCurrent(params,id).then(res=>{
                    if(res.data.success) {
                        if(res.data.result.records.length==0) {
                            this.hasMore = false
                            return
                        }
                        this.slectData = [...this.slectData,...res.data.result.records]
                    }
                })
                .catch(res=>{

                })
            },
            slectDataItem(obj) {
                console.log(obj.contentType)
                if(obj.contentType == "vision") {
                    // 图像数据
                    this.dataId = obj.id
                    this.datatypeFlag = false
                } else if(obj.contentType == "tabular")  {
                    this.dataId = obj.id
                    this.tableData1 = []
                    this.tableData2 = []
                    this.tableData1 = obj.columns.split(",")
                    this.tableData1.filter(item=>{
                        this.tableData2.push(JSON.parse(obj.columnType)[item])
                    })
                    this.datatypeFlag = true
                } else if(obj.contentType=='nlp') {
                    this.dataId = obj.id
                    this.datatypeFlag = false
                }
                // 图神经 
                else if(obj.contentType == 'graph') {
                    this.dataId = obj.id
                    this.datatypeFlag = false
                }
               
                
            },
            loadMore() {
                this.loadMoreData()
            },
            // 滑动加载更多
            loadMoreData() {
                this.count += 1
                if(this.hasMore) {
                    this.listCurrent()
                }
                return 
                
            },
        },
        watch: {
            ferenceObj: {
                handler(newvalue) {
                    this.copyData = JSON.parse(JSON.stringify(newvalue))
                    this.serviceId = this.copyData.serviceId
                },
                deep: true,
                immediate: true
            },
            contentType: {
                handler(newvalue) {
                    
                    setTimeout(() => {
                        this.CopyContentType = newvalue
                        console.log(newvalue)
                        if(newvalue == 'tabular') {
            
                            // 填入csv 数据
                            this.showTabFlag = true
                            this.showPicFlag = false
                            this.showNlpFlag = false
                            this.showGrashFlag = false
                            let arr =JSON.parse(this.copyData.header)
                            arr.forEach((item)=>{
                                this.copyheader.push({
                                    label: item,
                                    value: ''
                                })
                            })
                            this.firstHeader = JSON.parse(JSON.stringify(this.copyheader))
                        } else if(newvalue == 'vision') {
                          
                            // 上传图片数据
                             this.showPicFlag = true
                            this.showTabFlag = true
                            this.showNlpFlag = false
                        } else if(newvalue == 'nlp') {
                            // npl的数据格式
                            this.showNlpFlag = true
                            this.showTabFlag = false
                            this.showPicFlag = false
                        } else if(newvalue == 'graph') {
                            // 图神经
                            this.showNlpFlag = false
                            this.showTabFlag = false
                            this.showPicFlag = false
                            this.showGrashFlag = true
                        }
                    }, 100);
                    
                },
                deep: true,
                immediate: true
            }
        }
    }
</script>

<style lang="scss" scoped>
.container {
    .header {
        margin: 50px 0;
        text-align: center;
        display: flex;
        justify-content: center;
        align-items: center;
        .line{
            width: 100px;
            height: 2px;
            background-color: blue;
        }
        .img-box {
            text-align: center;
            display: flex;
            justify-content: center;
            align-items: center;
            .img-name {
                width: 100px;
                height: 80px;
                 display: flex;
                justify-content: center;
                align-items: center;
                font-size: 12px;
                background-size: 100px 80px;
                cursor: pointer;
            }
            .data-box {
                background-image: url('../../../assets/tool_bg_sensor_dataset.svg');
            }
            .active{
                background-image: url('../../../assets/tool_bg_image_active_dataset.svg');
            }
            .active-modal{
                background-image: url('../../../assets/tool_btn_active_model.svg');
            }
        }
        .line {

        }
    }
    .content {
        border: 1px solid #ccc;
        padding: 30px;
        ::v-deep .el-tabs__header{
            display: none;
        }
        .content-header {
            margin-bottom: 20px;
            font-size: 14px;
            font-weight: bold;
            color: #3A4755;
            
        }
        .radio-box{
            margin-bottom: 15px;
            ::v-deep .el-radio__label{
                font-style: 14px;
            }
        }
    }
    .btn{
        margin-top: 20px;
        text-align: right;
        ::v-deep .el-button--primary{
            padding: 12px 30px;
            border-radius: 10px;
            // width: 257px;
            // height: 67px;
            background: #e01622;
            border-radius: 4px;
        }
    }
}
.myTable {
    border-collapse: collapse;
    border-spacing: 0;
    // margin: 0 auto;
    width: 600px;
    text-align: center;
    .theader{
        background-color: #D2E4F5;
    }
    tbody {
        display: block;
        height: 100px;
        width: 100px;
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
.form-box{
    // width: 600px;
}
</style>