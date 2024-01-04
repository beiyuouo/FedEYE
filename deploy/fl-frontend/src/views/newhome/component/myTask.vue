<template>
    <div class="mytaks-box">
        <div class="mytaks-header">
            <div class="title">
                <div>
                    我的联邦任务
                </div>
                <el-button v-if="!touristFlag" plain size="small"  @click="handleClick"  icon="el-icon-plus" round>新建</el-button>
            </div>
            <span v-if="!touristFlag" class="more">
                <el-pagination
                    background
                    @current-change="handleChangePage"
                    :current-page.sync="pagination.currentPage"
                    :page-size="pagination.limit"
                    layout="prev, pager, next"
                    :total="pagination.total">
                </el-pagination>
            </span>
        </div>
        <div v-if="!touristFlag"  class="task-item">
            
            <el-row :gutter="30">
                <el-col v-for="(item,index) in DataList"
                        :key="index" :lg="8" :xl= '8'>
                    <my-task v-on:click.native = "gotoDetil(item.id)"   :item='item'></my-task>
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
        <create-task  @loadMoreData='loadMoreData' :slectData='slectData' @cancleCreateClick='cancleCreateClick' :visible.sync='creatVisible' :typeList='typeList'></create-task>
    </div>
</template>
<script>
import MyTask from '@/components/MyTask.vue'
import BreadCrumb from '@/components/breadcrumb.vue'
import { getTenantId } from "@/utils/auth";
import { mytask}  from '@/api/index'
import CreateTask from '@/components/createTask'
import {softWareStatus, listCurrent, queryAlgorithm, createJob} from '@/api/task'  
export default {
    props:{
        touristFlag: Boolean
    },
    components:{
        MyTask,
        BreadCrumb,
        CreateTask
    },
    created() {
        this.mytask()
    },
    data() {
        return {
            typeList:[],
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
                limit: 6
            },
            DataList:[],
            hasMore: true,
            count: 1
        }
    },
    methods:{
        // 滑动加载更多
        loadMoreData() {
            this.count += 1
            if(this.hasMore) {
                this.listCurrent()
            }
            return 
            
        },
        // 新建任务弹框取消
        cancleCreateClick() {
            this.creatVisible = false
        },
        searchMydata() {
            this.pagination.currentpage = 1
            this.mytask()
        },
        dataClearClick() {
            this.input = ''
            this.mytask()
        },
        handleChangePage(current) {
            this.pagination.currentpage = current;
            this.mytask()
        },
        // 我的任务
        async mytask() {
            let params = {
                name: this.input,
                pageNo:this.pagination.currentpage,
                pageSize:this.pagination.limit,
                type: 'TRAIN'

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
        handleClick() {
            this.softWareStatus()
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
        gotoDetil(id) {
            this.$router.push({
                name: 'MytaskDetail',
                 query: {
                    id: id,
                }
            })
        }
    }
}
</script>
<style lang="scss" scoped>
.mytaks-box{
    padding: 30px;
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
        display: flex;
        height: 40px;
        line-height: 40px;
        margin: 30px 0;
        overflow: hidden;
        .title{
            cursor: pointer;
            display: flex;
            // float: left;
            flex: 2;
            font-size: 24px;
            font-weight: 700;
            color: #484848;
            div{
                margin-right: 20px;
            }
        }
        .more{
            // float: right;
            flex: 2;
            text-align: right;
            // overflow: hidden;
        }
    }
}
.block{
    text-align: right;
    padding-bottom: 50px;
    
}
.more{
    width: 240px;
    .btn{
        background: #e01622;
        color: #fff; 
    }
}
::v-deep .el-pager{
    // display: none;
}
</style>
