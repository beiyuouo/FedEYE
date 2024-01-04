<template>
    <div class="mytaks-box">
        <div class="mytaks-header">
            <div class="title">
                <div>
                    我的推理任务
                </div>
            </div>
            <div class="header-right" v-if="!touristFlag" >
                <div class="more">
                    <el-input
                        placeholder="请输入推理任务名称"
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
                        :current-page.sync="pagination.currentPage"
                        :page-size="pagination.limit"
                        layout="prev, pager, next"
                        :total="pagination.total">
                    </el-pagination>
                </div>
            </div>
        </div>
        <div v-if="!touristFlag" class="task-item">
            <el-row :gutter="30">
                <el-col class="col-box" v-for="(item,index) in DataList"
                        :key="index" :span="6" >
                    <my-task-tl v-on:click.native = "gotoDetil(item.id,item.modelInfoId)"   :item='item'></my-task-tl>
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
    </div>
</template>
<script>
import MyTaskTl from '@/components/MyTask.vue'
import BreadCrumb from '@/components/breadcrumb.vue'
import { getTenantId  } from "@/utils/auth";
import { ferenceList}  from '@/api/index'
export default {
    components:{
        MyTaskTl,
        BreadCrumb
    },
    created() {
        
        if(sessionStorage.getItem('tourist')== 'true') {
             this.touristFlag = true
        } else {
             this.touristFlag = false
            this.ferenceList()
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
            hasMore: true
        }
    },
    methods:{
        searchMydata() {
            this.pagination.currentpage = 1
            this.ferenceList()
        },
        dataClearClick() {
            this.input = ''
            this.ferenceList()
        },
        handleChangePage(current) {
            this.pagination.currentpage = current;
            this.ferenceList()
        },
        // 我的推理任务
        async ferenceList() {
            let params = {
                name: this.input,
                pageNo:this.pagination.currentpage,
                pageSize:this.pagination.limit

            }
            let id = getTenantId()
            await ferenceList(params,id).then(res=>{
                if(res.data.success) {
                    this.DataList = res.data.result.records
                    this.pagination.total = res.data.result.total
                 
                }
            })
            .catch(res=>{
                
            })
        },
        gotoDetil(id,modelInfoId) {
            this.$router.push({
                name: 'Tuili',
                query: {
                    id: id,
                    modalId: modelInfoId
                }
                
            })
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
