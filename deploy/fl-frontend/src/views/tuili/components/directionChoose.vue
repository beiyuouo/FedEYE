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
            <content-box>
                <div class="form-box">
                    <el-form label-width="auto" label-position="right" @submit.native.prevent>
                        <el-form-item label="请选择要推理的数据：">
                            <el-select v-model="datavalue" placeholder="请选择数据">
                                <div v-if="selectData" 
                                    v-infinite-scroll='loadMore'
                                    infinite-scroll-delay='200'
                                    >
                                    <el-option v-for="(item,index) in selectData "
                                        @click.native="selectDataItem(item)"
                                    
                                        :key="index"
                                        :label="item.name"
                                        :value="item.id"
                                        >
                                    </el-option>
                                </div>
                            </el-select>
                            <span class="confirm">
                                <el-button @click="chooseData" type="primary">确定</el-button>
                            </span>
                        </el-form-item>
                        <el-form-item label="选择的推理数据：">
                            {{dataName}}
                        </el-form-item>
                        <el-form-item v-if="guestShow" label="数据导入检验：">
                            <div class="checkout">
                                <span class="c-status">
                                    发起方 &nbsp;&nbsp;
                                    <img :src="img1" alt="">
                                </span>
                                <span class="c-status">
                                    参与方 &nbsp;&nbsp;  
                                    <img :src="img2" alt="">
                                </span>
                                <span class="btn">
                                    <el-button  :loading="loading"  @click="startTaskClick()"  type="primary">开始推理任务</el-button>
                                </span>
                            </div>
                           
                        </el-form-item>
                    </el-form>
                </div>
            </content-box>
        
        </div>
    </div>
</template>

<script>
    import img1  from '@/assets/0.png'
    import img2  from '@/assets/1.png'
    import lineBox from './line.vue'
    import contentBox from './content.vue'
    import { getTenantId } from "@/utils/auth"
    import { inferenceByDataCollection } from '@/api/index'
    import { hetero, heteroDataList, heteroInference, selectList } from '@/api/task'
    export default {
        props: {
            modelName: String,
            ferenceObj: Object,
            taskId: String,
            role: String,
            flType: Number

        },
        components:{
            lineBox,
            contentBox
        },
        data() {
            return {
                img1:img1,
                img2: img2,
                active: '1',
                datavalue: '',
                selectData: [],
                tableData1: [],
                tableData2: [],
                datatypeFlag: false,
                hasMore: true,
                count: 1,
                serviceId: '',
                copyData:{},
                dataId: '',
                loading: false,
                hostObj: {},
                guestObj: {},
                dataName: '暂未选择数据'
            }
        },
        created() {
            this.heteroDataList()
        },
        computed: {
            guestShow() {
                if(this.role == 'GUEST') {
                    return true
                } else if( this.role == 'HOST') {
                    return false
                }
            },

        },
        methods: {
            // 查询数据
            async selectList() {
                let data = {
                    pageNo: this.count,
                    pageSize: 10,
                    contentType: 'tabular',
                    selectedId: this.dataId
                }
                await selectList(data,getTenantId()).then(res=>{
                    if(res.data.success) {
                        if(res.data.result.records.length==0) {
                            this.hasMore = false
                            return
                        }
                        this.selectData = [...this.selectData,...res.data.result.records]
                    }
                })
                .catch(err=>{

                })
            },
            // 查询发起方和参与方的数据选择情况
            async heteroDataList() {
                let data  = {
                    jobId: this.taskId,
                }
                await heteroDataList(data,getTenantId()).then(res=>{
                   
                    if(res.data.code == 200) {
                        this.guestObj = res.data.result.guest
                        this.hostObj = res.data.result.host
                        // 判断当前登陆人是否为发起方
                        if(this.role =='GUEST') {
                            // 当前登陆人是发起方
                            if(this.guestObj.dataFlag) {
                                // 发起方已选择数据
                                this.img1 = img1
                                this.datavalue = this.guestObj.dataId
                                this.dataId = this.guestObj.dataId
                                this.dataName = this.guestObj.dataName
                            }else {
                                this.img1 = img2
                            }
                            if(this.hostObj.dataFlag ) {
                                this.img2 = img1
                            } else {
                                this.img2 = img2
                            }
                        } else if(this.role == 'HOST') {
                            // 当前登陆人是参与方
                            if(this.hostObj.dataFlag ) {
                                // 参与方已选择数据
                                this.img2 = img1
                                this.datavalue = this.hostObj.dataId
                                this.dataId = this.hostObj.dataId
                                this.dataName = this.hostObj.dataName
                            } else {
                                this.img2 = img2
                            }
                        }
                        this.selectList() 
                    }
                })
            },
            // 确定按钮选择数据
            chooseData() {
                this.hetero()
            },
            // 开启推理任务
            async heteroInference() {
                let data = {
                    id: this.taskId
                }
                await heteroInference(data,getTenantId()).then(res=>{
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
            // 开始推理任务
            startTaskClick () {
                if(this.guestObj.dataFlag && this.hostObj.dataFlag){
                    this.loading = true
                    this.heteroInference()

                } else {
                    this.$message.warning('请等待数据准备完成')
                }
              
            },
            selectDataItem(obj) {
                this.dataId = obj.id
            },
            loadMore() {
                this.loadMoreData()
            },
            // 滑动加载更多
            loadMoreData() {
                this.count += 1
                if(this.hasMore) {
                    this.selectList()
                }
                return 
                
            },
            // 纵向推理数据集选择
            async hetero() {
                // let data = new URLSearchParams()
                let data = {
                    jobId: this.taskId,
                    dataId: this.dataId,
                    inferenceRole: this.role
                }
                await hetero(data,getTenantId()).then(res=>{
                    if(res.data.code == 200) {
                        this.$message.success(res.data.result)
                        this.heteroDataList()
                    }
                })
                .catch(err=>{

                })
                
            }
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
    }
    .content {
        border: 1px solid #ccc;
        padding: 30px;
        ::v-deep .el-tabs__header{
            display: none;
        }
        .content{
            border: none;
        }
        .confirm{
            margin-left: 40px;
        }
        .checkout{
            font-size: 14px;
            display: flex;
            align-items: center;
            .c-status{
                display: flex;
                align-items: center;
                margin-right: 30px;
            }
            .btn{
                margin: 0 0 0 40px;
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
        height: 200px;
        width: 100px;
        // width: 1400px;

        
        tr {
            display: table;
            width: 100%;
            table-layout: fixed;
            td{
                width: 100px;
                word-wrap: break-word;
                border: 1px solid #cad9ea;
                color: #666;
                height: 100px;
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