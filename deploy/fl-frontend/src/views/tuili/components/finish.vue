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
                <span class="img-name active data-box">
                    推理数据
                </span>
            </div>
        </div>
        <div class="content">
            <div class="content-header">
                推理结果如下 ：
            </div>
                <el-table
                    v-if="showCsvFlag"
                    :data="tableData"
                    stripe
                    border
                    style="width: 100%"
                    max-height="250">
                        <el-table-column
                            v-for="(item1,index) in headerCopy"
                            :key="index"
                            :prop="item1"
                            :label="item1"
                            :fixed='item1=="符合预期"?"right":false'
                            width="50">
                        </el-table-column>
                </el-table>
                <el-table
                    v-if="showPicFlag"
                    :data="copyTableData"
                    stripe
                    border
                    style="width: 100%"
                    max-height="250">
                        <el-table-column
                            prop="name"
                            label="文件名称"
                            width="100">
                        </el-table-column>
                        <el-table-column
                            label="图片链接"
                            width="auto">
                             <template slot-scope="scope">
                                <img style="width:100px;" :src ="`data:image/png;base64,${scope.row.base64}`" alt="">
                            </template>
                           
                        </el-table-column>
                        <el-table-column
                            prop="result"
                            label="推理结果"
                            width="100">
                        </el-table-column>
                </el-table>
                <div v-if="showDownFlag">
                    <el-card v-if="!startIng" class="box-card">
                    
                        <el-link type="primary" :underline="false">{{downText}}</el-link>
                        <span class="btn">
                            <el-button  @click="downFile"  type="primary">
                                下载
                                <!-- <a href=""></a> -->
                            </el-button>
                        </span>
                    
                    </el-card>
                    <div style="text-align:center;" v-if="startIng">
                        <img  class="img" src="@/assets/modeltraining.png" alt="">
                        <div  class="text">
                            任务推理中,请稍后查看
                        </div>
                        <div class="loading" v-loading="loading">
                            
                        </div>
                        
                    </div>
                </div>
                <div v-if="showNlpFlag">
                    <div>
                        <el-table
                            stripe
                            border
                            :data="NlpDataObj"
                            style="width: 100%"
                            max-height="250">
                                <el-table-column
                                    prop="data"
                                    label="文本内容"
                                   >
                                
                                </el-table-column>
                    
                                <el-table-column
                                    prop="result"
                                    label="推理结果"
                                    >
                                   
                                </el-table-column>
                        </el-table>
                    </div>
                </div>
                <div v-if="showGraphFlag">
                    <div>
                        <el-table
                            stripe
                            border
                            :data="GraphDataObj"
                            style="width: 100%"
                            >
                                <el-table-column
                                   
                                    label="文本内容"
                                   >
                                    <template slot-scope="scope">
                                        <el-popover trigger="hover" placement="bottom">
                                            <p style="width:60vw;">{{scope.row.data}}</p>
                                        <div slot="reference" class="name-wrapper">
                                            <span>{{ scope.row.data }}</span>
                                        </div>
                                        </el-popover>
                                    </template>
                                </el-table-column>
                    
                                <el-table-column
                                    prop="result"
                                    label="推理结果"
                                    >
                                   
                                </el-table-column>
                        </el-table>
                    </div>
                </div>
            <div>
            

            </div>
        </div>
        <div class="btn">
            <!-- <el-button  @click="startTaskClick()"  type="primary">开始推理任务</el-button> -->
        </div>
    </div>
</template>

<script>
    import lineBox from './line.vue'
    import contentBox from './content.vue'
    import { listCurrent } from '@/api/task' 
    import {  getTenantId } from "@/utils/auth"
    import { inference } from '@/api/index'
    export default {
        props: {
            modelName: String,
            ferenceObj: Object,
            taskId: String,
            flInferenceResultVo: Object,
            contentType: String,
            type: String,
            startIng: Boolean
        },
        components:{
            lineBox,
            contentBox
        },
        data() {
            return {
                resource: '',
                active: '3',
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
                resultData: [],
                headerCopy: [],
                tableData: [],
                opyData:{},
                copyResult: {},
                copyTableData:[],
                showPicFlag: false,
                downText: '',
                showDownFlag: false,
                showCsvFlag: false,
                loading: true,
                showNlpFlag: false,
                showGraphFlag: false,
                NlpDataObj:{},
                GraphDataObj: {}
            }
        },
        created() {
            
            this.listCurrent()
        },
        methods: {
            // 下载文件
            downFile() {
                let Url =  sessionStorage.getItem("uploadUrl")+'/'+this.downText;
                console.log(Url)
                window.open(Url, '_blank');
            },
            // 开启推理任务
            async inference() {
                let data= {
                    id: this.taskId,
                    featureData: this.featureData
                }
                // let data = new URLSearchParams()
                // data.append('id',this.taskId)
                // data.append('featureData',this.featureData)
                await inference(data,getTenantId()).then(res=>{
                   
                })
                .catch(res=>{

                })
            },
            // 开始推理任务
            startTaskClick () {
                this.copyheader.forEach((item)=>{
                    this.featureData[item.label] = item.value
                 
                })
                
                this.inference()
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
                this.tableData1 = []
                this.tableData2 = []
                this.tableData1 = obj.columns.split(",")
                this.tableData1.filter(item=>{
                    this.tableData2.push(JSON.parse(obj.columnType)[item])
                })
                this.datatypeFlag = true
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
            startIng: {
                handler(newvalue) {
                    if(!newvalue) {
                        this.active = '3'
                    } else {
                        this.active = '2'
                    }
                   
                },
                deep: true,
                immediate: true
            },
            ferenceObj: {
                handler(newvalue,oldevalue) {   
                   
                    this.copyData = JSON.parse(JSON.stringify(newvalue))
                    this.serviceId = this.copyData.serviceId
                },
                deep: true,
                immediate: true
            },
            flInferenceResultVo: {
                handler(newvalue,oldevalue) {
                    this.copyResult = JSON.parse(JSON.stringify(newvalue))
                    this.resultData = JSON.parse(JSON.stringify(this.copyResult.flInferenceInfoVoList)) 
                    
                
                },
                deep: true,
                immediate: true
            },
            // contentType: {
            //     handler(newvalue,oldevalue) {
            //         this.CopyContentType = newvalue
            //         if(newvalue == 'tabular') {
            //             console.log(123123)
            //             // 填入csv 数据
            //             this.showPicFlag = false
            //             let arr =JSON.parse(this.copyData.header)
            //             arr.forEach((item)=>{
            //                 this.copyheader.push({
            //                     label: item,
            //                     value: ''
            //                 })
            //             })
            //             let copyItem = {}
            //             this.resultData.forEach((item,index)=>{
            //                 copyItem = JSON.parse(item.data)
            //                 copyItem['符合预期'] = item.result
                        
            //                 this.tableData.push(copyItem)
            //             })
            //             this.headerCopy = JSON.parse(this.copyResult.header)
            //             this.headerCopy.push('符合预期')
            //         } else if(newvalue == 'vision') {
            //             console.log(232323)
            //             // 上传图片数据
            //             this.showPicFlag = true
            //             this.copyTableData = JSON.parse(JSON.stringify(this.resultData)) 
            //         }
            //     },
            //     deep: true,
            //     immediate: true
            // },
            type: {
                handler(newvalue,oldevalue) {
                    console.log(newvalue)
                    this.CopyContentType = newvalue
               
                    if(newvalue == 'tabular') {
                        // 显示填入的csv数据
                        this.showPicFlag = false
                        this.showCsvFlag = true
                        this.showDownFlag = false
                        this.showNlpFlag = false
                        this.showGraphFlag = false
                        let arr =JSON.parse(this.copyData.header)
                        arr.forEach((item)=>{
                            this.copyheader.push({
                                label: item,
                                value: ''
                            })
                        })
                        let copyItem = {}
                        this.resultData.forEach((item,index)=>{
                            copyItem = JSON.parse(item.data)
                            copyItem['符合预期'] = item.result
                        
                            this.tableData.push(copyItem)
                        })
                        this.headerCopy = JSON.parse(this.copyResult.header)
                        this.headerCopy.push('符合预期')
                    } else if(newvalue == 'vision') {
                        // 上传图片数据
                        //显示填入的图片数据
                        this.showPicFlag = true
                        this.showCsvFlag = false
                        this.showDownFlag = false
                        this.showNlpFlag = false
                        this.showGraphFlag = false
                        this.copyTableData = JSON.parse(JSON.stringify(this.resultData)) 
                    } else if (newvalue == 'visionCollection') {
                        // 显示填入的图片数据集数据
                        this.showDownFlag = true
                        this.showPicFlag = false
                        this.showCsvFlag = false
                        this.showNlpFlag = false
                        this.downText  = this.resultData[0].result
                        
                    } else if (newvalue == 'tabularCollection') {
                        // 显示填入的csv的数据集
                        this.showDownFlag = true
                        this.showPicFlag = false
                        this.showCsvFlag = false
                         this.showNlpFlag = false
                        this.downText  = this.resultData[0].result
                       
                    } else if(newvalue == 'nlp') {
                        // nlp文本输入内容推理
                        this.showNlpFlag = true
                        this.showDownFlag = false
                        this.showPicFlag = false
                        this.showCsvFlag = false
                        this.showGraphFlag = false
                        this.NlpDataObj = this.resultData
                        
                    } else if(newvalue=='nlpCollection') {
                        this.showDownFlag = true
                        this.showPicFlag = false
                        this.showCsvFlag = false
                        this.showNlpFlag = false
                        this.showGraphFlag = false
                        this.downText  = this.resultData[0].result
                    } else if(newvalue == 'graph') {
                        // 图神经 文本输入推理结果
                        this.showGraphFlag = true
                        this.showDownFlag = false
                        this.showPicFlag = false
                        this.showCsvFlag = false
                        this.showNlpFlag = false
                        this.GraphDataObj = JSON.parse(JSON.stringify(this.resultData)) 
                    } else if(newvalue =='graphCollection') {
                        // 图神经选择数据推理结果
                         this.showDownFlag = true
                        this.showPicFlag = false
                        this.showCsvFlag = false
                        this.showNlpFlag = false
                        this.downText  = this.resultData[0].result
                    }
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
        overflow: hidden;
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
    }
    .btn{
        margin-top: 20px;
        text-align: right;
        ::v-deep .el-button--primary{
            padding: 12px 30px;
            border-radius: 10px;
            background: #e01622;
            border-radius: 4px;
        }
    }
    .box-card{
        padding:30px;
        text-align:center;
        .btn{
            margin-left: 30px;
        }
    }
    .loading{
        margin-top: 30px;
    }
    .name-wrapper{
        span{
            display: block;
            text-overflow: ellipsis;
            white-space: nowrap;
            overflow: hidden;
        }
    }
}
</style>