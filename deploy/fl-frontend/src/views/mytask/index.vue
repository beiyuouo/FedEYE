<template>
    <div class="mytaks-box">
        <div class="mytaks-header">
            <div class="title">
                <div>
                    我的任务
                </div>
                <el-button v-if="!touristFlag" plain size="small"  @click="handleClick"  icon="el-icon-plus" round>新建</el-button>
                 <el-button v-if="!touristFlag" plain size="small"  @click="handleTlClick"  icon="el-icon-plus" round>新建推理任务</el-button>
            </div>
            <div class="header-right" v-if="!touristFlag" >
                <div class="icon-box">
                    <svg-icon :class="showCard?'active':''"  @click="cardShowClick" icon-class="card" /> 
                    <svg-icon :class="showList?'active':''" @click="listShowClick" icon-class="list" /> 
                </div>
                <el-select  @change='changeClick' v-model="value" filterable placeholder="请选择">
                    <el-option
                        v-for="item in options"
                        :key="item.value"
                        :label="item.label"
                       
                        :value="item.value">
                    </el-option>
                </el-select>
                
                <div class="more">
                    <el-input
                        placeholder="请输入任务名称"
                        v-model="input"
                        @clear='dataClearClick'
                        clearable>
                        <el-button @click="searchMydata" class="btn" slot="append" icon="el-icon-search"></el-button>
                    </el-input>
                </div>
                <div class="block">
                    <el-pagination
                        background
                        @current-change="handleChangePage"
                        :current-page.sync.sync="pagination.currentPage"
                        :page-size="pagination.limit"
                        layout="prev, pager, next"
                        :total="pagination.total">
                    </el-pagination>
                </div>
            </div>
        </div>
        <div v-if="!touristFlag" class="task-item">
            <my-task-table :DataList='DataList' v-show="showList"></my-task-table>
            <el-row  v-show="showCard" :gutter="30">
                <el-col class="col-box" v-for="(item,index) in DataList"
                        :key="index" :span="6">
                    <my-task v-on:click.native = "gotoDetail(item)"   :item='item'></my-task>
                </el-col>
            </el-row>
            <div v-if="DataList.length == 0" class="img-box"  >
                <img src="@/assets/nodataset.png" class="img">
                <div>
                    暂无数据
                </div>
            </div>
        </div>
        
        <div v-if="touristFlag"  class="img-box">
            <img src="@/assets/binding.png" class="img">
            <div>
                绑定联邦方后激活
            </div>
        </div>  
        <create-task   :slectData='slectData' @cancleCreateClick='cancleCreateClick' :visible.sync='creatVisible' :typeList='typeList'></create-task>
        <create-task-tl @loadMoreData='loadMoreData' :allData='allData' @cancleCreateTlClick='cancleCreateTlClick' :visible.sync='tuiVisible' ></create-task-tl>
    </div>
</template>
<script>
import MyTask from '@/components/MyTask.vue'
import MyTaskTable from '@/components/MyTaskTable.vue'
import BreadCrumb from '@/components/breadcrumb.vue'
import { getTenantId  } from "@/utils/auth";
import { mytask, listMyModel}  from '@/api/index'
import CreateTask from '@/components/createTask'
import CreateTaskTl from '@/components/createTl'
import {softWareStatus, listCurrent, queryAlgorithm, createJob} from '@/api/task'  
export default {
    name: 'Mytask',
    components:{
        MyTask,
        BreadCrumb,
        CreateTask,
        MyTaskTable,
        CreateTaskTl
    },
   
    created() {
        if(sessionStorage.getItem('tourist')== 'true') {
             this.touristFlag = true
        } else {
             this.touristFlag = false
            this.mytask()
        }
    
    },
    data() {
        return {
            count: 1,
            typeList:[],
            touristFlag:false,
            slectData: [],
            creatVisible: false,
            currentPage: 0,
            input: '',
            breadArray: [{
                name: '我的任务'
            }],
            pagination: {
                offset: 0,
                total: 0,
                currentPage: 1,
                limit: 12
            },
            DataList:[],
            hasMore: true,
            options: [{
                value: 'ALL',
                label: '全部'
                }, {
                value: 'TRAIN',
                label: '训练任务'
                }, {
                value: 'INFERENCE',
                label: '推理任务'
            }],
            value: 'ALL',
            showCard: true,
            showList: false,
            tuiVisible: false,
            allData: []
        }
    },
    methods:{
         //模型池数据
        async listMyModel() {
            let id = getTenantId()
            let params ={
                name: '',
                pageNo: this.count,
                pageSize: '10',
            }
            await listMyModel(params,id).then(res=>{
                if(res.data.code == 200) {
                     if(res.data.result.records.length==0) {
                      
                        this.hasMore = false
                        return
                    }
                    this.allData = [...this.allData,...res.data.result.records]
                }
            })
        },
        cardShowClick() {
            this.showCard = true
            this.showList = false
        },
        listShowClick() {
            this.showCard = false
            this.showList = true
        },
        // 选择推理 训练任务
        changeClick() {
            this.pagination.currentPage = 1
            this.mytask()
        },
        // 滑动加载更多
        // loadMoreData() {
        //     this.count += 1
        //     if(this.hasMore) {
        //         this.listCurrent()
        //     }
        //     return 
            
        // },
        // 滑动加载更多模型
        loadMoreData() {
            
            this.count += 1
            if(this.hasMore) {
                this.listMyModel()
            }
            return 
            
        },
        // 新建任务弹框取消
        cancleCreateClick() {
            this.creatVisible = false
        },
        searchMydata() {
            this.pagination.currentPage = 1
            this.mytask()
        },
        dataClearClick() {
            this.input = ''
            this.mytask()
        },
        handleChangePage(current) {
            this.pagination.currentPage = current;
            this.mytask()
        },
        // 我的任务
        async mytask() {
            let params = {
                name: this.input,
                pageNo:this.pagination.currentPage,
                pageSize:this.pagination.limit,
                type: this.value

            }
            let id = getTenantId()
            await mytask(params,id).then(res=>{
                if(res.data.success) {
                    this.DataList = res.data.result.records
                    this.pagination.total = res.data.result.total
                 
                }
            })
            .catch(res=>{
                
            })
        },
        handleClick(e) {
            
            this.softWareStatus()
            let target = e.target
            target.blur()
            
        },
        // 新建推理任务
        handleTlClick() {
            this.tuiVisible = true
            this.listMyModel()
        },
        cancleCreateTlClick() {
            this.tuiVisible = false
            
        },
         // 环境检查
        async softWareStatus() {
            let id  = getTenantId()
            await softWareStatus(id).then(res=>{
                if(res.data.success) {
                    if(res.data.result.length != 0){
                        this.$message.warning('请部署环境之后，再创建任务')
                    } else {
                        this.listCurrent()
                        this.creatVisible = true
                        this.popVisible = false
                    }
                    
                }
            })
            .catch(res=>{

            })
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
        // 创建任务
        async createJob() {
            let id  = getTenantId()
            let params = {
                avatar: this.imgurl,  // 联邦方头像
                compoments:JSON.stringify([{name: this.form.type}]),  // 	算法组件
                content: this.form.text,   // 任务描述
                name: this.form.name,   // 任务名称
                partyId: id,   // 联邦方id
                permission: this.form.resource,   // 任务权限   0 任意加入  1 申请加入  2  私密
                recruitStatus: 0,   // 招募状态，0招募中、1任务提交成功、2任务提交失败、3训练成功、4训练失败
                // 加入任务使用下面参数
                flJobRegistList: [{dataId:this.form.value,partyId:id}] ,
                
            }
            await createJob(params).then(res=>{
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
                    this.typeList = res.data.result
                }

            })
            .catch(res=>{
                
            })
        },
        gotoDetail(item) {
            const { id,role,type,modelInfoId,flType,inferenceRole } = item
            if(type == 'INFERENCE') {
                // 推理详情
                this.$router.push({
                    name: 'Tuili',
                    query: {
                        id: id,
                        modalId: modelInfoId,
                        flType: flType,
                        inferenceRole: inferenceRole
                    }
                    
                })
            } else {
                // 联邦任务详情
                this.$router.push({
                    name: 'MytaskDetail',
                    query: {
                        id: id,
                        role: role
                    }
                })
            }
            
        }
    }
}
</script>
<style lang="scss" scoped>
.mytaks-box{
    .img-box{
        // height: 236px;
        padding: 12px;
        display: flex;
        justify-content: center;
        flex-direction: column;
        align-items: center;
        .img{
            height: 100px;
        }
        div{
            color: #9a9a9a;
            font-size: 20px;
            font-weight: 700;
            margin-top: 14px;
        }
    }
    .mytaks-header{
        height: 40px;
        // line-height: 50px;
        margin: 20px 0;
        overflow: hidden;
        display: flex;
        align-items: center;
        justify-content: space-between;
        .title{
            cursor: pointer;
            display: flex;
            align-items: center;
            float: left;
            font-size: 24px;
            font-weight: 700;
            color: #484848;
            div{
                margin-right: 20px;
            }
        }
        .header-right {
            display: flex;
            align-items: center;
            .icon-box{
                .svg-icon{
                    font-size: 40px;
                    cursor: pointer;
                    margin-right: 10px;
                }
                .active{
                    color: #e01622;
                }
            }
            .block{
               ::v-deep.el-pagination .el-icon-arrow-left{
                    font-size: 20px !important;
                    // color: #000;
                    font-weight: 700 !important;
                }
                ::v-deep.el-pagination .el-icon-arrow-right{
                    font-size: 20px !important;
                    // color: #000;
                    font-weight: 700 !important;
                }
                ::v-deep .el-pagination{
                    overflow: hidden;
                    // float: right;
                    display: flex;
                    align-items: center;
                    height: 50px;
                }
                ::v-deep .btn-prev{
                    background-color: #fff;
                }
                ::v-deep .btn-next{
                    background-color: #fff;
                }
                ::v-deep .el-pager{
                    // display: none;
                }
            
                
            }
        }
    }
    .task-item{
        padding: 0 20px;
        .col-box{
            // transform: skewX(-10deg);
        }
    }
}

.more{
    width: 240px;
    .btn{
        background: #e01622;
        color: #fff; 
    }
}
</style>
