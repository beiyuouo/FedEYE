<template>
    <div class="container-box">
        <div class="mytaks-header">
            <div class="title">
                <div>
                    算法池
                </div>
            </div>
            <div class="header-right" >
                <div class="more">
                    <el-input
                        placeholder="请输入算法名称"
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
        <div class="center">
            <el-row :gutter="30">
                <el-col 
                    :span="6" 
                    v-for="(item,index) in allData"
                    :key="index">
                    <div class="item" @click="gotoDataDetail(item.id,item.flType)">
                        <el-card shadow="hover" class="box-card">
                            <img src="@/assets/suanfa1.png" alt="">
                            <div class="content">
                                <div class="ct">
                                    <div class="ct-name">{{item.nameCn}}</div>
                                    <!-- <div class="ct-use">最近使用</div> -->
                                </div>
                                <p class="desc">{{item.introduction}}</p>
                            </div>
                        </el-card>
                    </div>
                </el-col>
            </el-row>
        </div>
       
    </div>
</template>
<script>
import { listAllAlgorithm }  from '@/api/index'
import { getTenantId} from "@/utils/auth";
export default {
    name: 'algorithm',
    created() {
        this.listAllAlgorithm()
    },
    data() {
        return {
            pagination: {
                offset: 0,
                total: 0,
                currentPage: 1,
                limit: 8
            },
            listAllAlgorithmname:'',
            allAlgorithmObj: null,
            allData: [],
            input: ''
        }
    },
    methods:{
         // 任务查询
        searchMydata() {
            this.pagination.currentPage = 1
            this.listAllAlgorithm()
        },
        // 数据清空
        dataClearClick() {
            this.input = ''
            this.listAllAlgorithm()
        },
        handleChangePage(current) {
            this.pagination.currentPage = current;
            this.listAllAlgorithm()
        },
        // 算法池数据
        async listAllAlgorithm() {
            let params = {
                name: this.input,
                pageNo:this.pagination.currentPage,
                pageSize: this.pagination.limit
            }
            await listAllAlgorithm(params,getTenantId()).then(res=>{
                if(res.data.success) {
                    this.allAlgorithmObj = res.data.result
                }
            })
            .catch(res=>{
                
            })
        },
        // 跳转详情页
        gotoDataDetail(id,flType) {
            
            this.$router.push({
                name: 'algorithmDetail',
                query: {
                    id:  id,
                    flType:flType
                }
            })
        }
    },
    watch:{
        allAlgorithmObj(newData,oldData) {
            this.allData = newData.records
            this.pagination.total = newData.total
           
        }
    }
}
</script>
<style lang="scss" scoped>
.container-box{
    height: 100%;
    border-radius: 10px;
    padding: 30px;
    .mytaks-header{
        height: 40px;
        // line-height: 40px;
        margin: 20px 0;
        overflow: hidden;
        overflow: hidden;
        display: flex;
        align-items: center;
        justify-content: space-between;
        .title{
            cursor: pointer;
            display: flex;
            float: left;
            font-size: 24px;
            font-weight: 700;
            color: #484848;
            div{
                margin-right: 20px;
            }
        }
        .header-right{
            display: flex;
            align-items: center;
           .block{
                // text-align: right;
                // margin-top: 30px;
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
                    align-items: center;
                    // height: 50px;
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
            .more{
                width: 240px;
                .btn{
                    background: #e01622;
                    color: #fff; 
                }
            }
        }
        
    }
    .center{
        .item{
            margin: 50px 40px  50px 0;
            ::v-deep .el-card{
                width: 335px;
                height: 375px;
                padding: 0;
                img{
                    width: 335px;
                    height: 233px;
                }
            }
            ::v-deep .el-card__body{
                padding: 0;
            }
            .content{
                .ct{
                    margin: 20px 26px;
                
                    
                    overflow: hidden;
                    font-size: 20px;
                    .ct-name{
                        // float: left;
                        font-weight: 700; 
                        display: -webkit-box;
                        -webkit-box-orient: vertical;
                        -webkit-line-clamp: 1;
                        overflow: hidden;
                    }
                    .ct-use{
                        float: right;
                        color: #9898F1;
                    }
                }
                .desc{
                        margin: 20px 26px;
                        color: #C5C5C5;
                        // 多行显示。。。。
                        display: -webkit-box;
                        -webkit-box-orient: vertical;
                        -webkit-line-clamp: 4;
                        overflow: hidden;
                }
            }
            
        }
    } 
}

    
</style>
