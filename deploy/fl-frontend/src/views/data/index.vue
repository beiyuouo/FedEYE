<template>
    <div class="data-main">
        <div class="mytaks-header">
            <div class="title">
                <div>
                    数据池
                </div>
            </div>
            <div class="header-right">
                <div class="more">
                    <el-input
                        placeholder="请输入数据名称"
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
        <div class="content">
            <el-row v-if="allData.length>0" :gutter="30">
                <el-col class='col-box' v-for="(item,index) in allData"
                        :key="index" :lg="6" :xl= '6'>
                    <div class="radiu-box"
                        @click="gotoDataDetail(item)"
                        >
                        
                        <div class="content-box">
                            <el-tooltip class="item" effect="dark" :content="item.name" placement="top">
                               <div class="content-title">
                                    {{item.name}}
                                </div>
                            </el-tooltip>
                            
                            <div class="content-partyName">
                                {{item.partyName}}
                            </div>
                            <div class="content-footer">
                                <el-tag>{{item.rowsNum}}条数据</el-tag>
                                <!-- <el-tag>{{item.contentType}}</el-tag> -->
                            </div>
                        </div>
                    </div>
                    
                </el-col>
            </el-row>
            <div v-else class="img-box"  >
                <img src="@/assets/nodataset.png" alt="">
                <div>
                    暂无数据
                </div>
            </div>
        </div>
    </div>
</template>
<script>
import {listAllData} from '@/api/index'
import { getTenantId} from "@/utils/auth";
export default {
    name: 'Data',
    data() {
        return {
            allData: [],
            allDataObj : {},
            pagination: {
                offset: 0,
                total: 0,
                currentPage: 1,
                limit: 16
            },
            input:''
        }
    },
    created() {
        this.listAllData()
    },
    methods: {
         // 任务查询
        searchMydata() {
            this.pagination.currentPage = 1
            this.listAllData()
        },
        // 数据清空
        dataClearClick() {
            this.input = ''
            this.listAllData()
        },
        gotoDataDetail(row) {
            
            this.$router.push({
                name: 'DataDetail',
                query: { 
                    partyId: row.partyId,
                    id: row.id,
                    from: 'HomeTask',
                }
            })
        },
        // 分页:当前页(current-page)改变时会触发
        handleChangePage(current) {
            this.pagination.currentPage = current;
            this.pagination.offset = current
            this.listAllData()
        },
        // 数据池
        async listAllData() {
            let params = {
                name: this.input,
                pageNo:this.pagination.currentPage,
                pageSize: this.pagination.limit
            }
            await listAllData(params,getTenantId()).then(res=>{
                this.allDataObj = res.data.result
                this.allData = res.data.result.records
                this.pagination.total = res.data.result.total
            })
            .catch(res=>{
                
            })
        },
    }
}
</script>
<style lang="scss" scoped>
 .data-main{
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
    .box-card{
        height: 80px;
        line-height: 80px;
        padding: 0 30px;
        margin-bottom: 30px;
        ::v-deep .el-card__body {
            padding: 0;
        //    display: flex;
        }
        .title{
            float: left;
            font-size: 20px;
            font-family: Microsoft YaHei;
            font-weight: bold;
            color: #484848;
        }
        .fr{
            float: right;
            display: flex;
            .arrow{
                font-weight: 700;
                font-size: 22px;
                .el-icon-arrow-right{
                    font-weight: 700;
                    color: #9A9A9A;
                }
            }
            .num-box {
                .num {
                    display: inline-block;
                    width: 50px;
                    height: 26px;
                    line-height: 26px;
                    background: #E6E7FF;
                    border-radius: 40px;
                    text-align: center;
                    font-size: 14px;
                    font-family: Microsoft YaHei;
                    font-weight: 400;
                    color: #777BF3;
                    margin: 0 10px;
                }
                span{
                    margin: 0 10px;
                        font-size: 20px;
                }
            }
        }
        
    }
        
   
    .content{
        // background-color: #fff;
        padding: 30px;
        .radiu-box{ 
            position: relative; 
            width: 244px;
            height: 184px;
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
                    font-size: 20px;
                    font-weight: 700;
                   
                    display: -webkit-box;
                    -webkit-box-orient: vertical;
                    -webkit-line-clamp: 1;
                    word-break:break-all;
                    overflow: hidden;
                }
                .content-partyName{
                    margin-top: 20px;
                    ::v-deep .el-tag {
                        margin-right: 10px;
                    }
                }
                .content-footer{
                    margin-top: 30px;
                    ::v-deep .el-tag {
                        margin-right: 10px;
                    }
                }
            }
        }
        .img-box{
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
        .radiu-box:before{
            position: absolute; 
            top:-10px; 
            content: "";
            width: 244px; 
            height: 20px; 
            border-radius:50%; 
            background: #fff;
            box-shadow: 0px 3px 15px 9px rgba(226,226,226,0.29);
        }

        .radiu-box:after{
            position: absolute; 
            bottom:-10px; 
            content: ""; 
            width: 244px; 
            height: 20px; 
            border-radius:50%; 
            background: linear-gradient(to right, #ededf6,#fff);
            // box-shadow: 0px 3px 15px 9px rgba(226,226,226,0.29);
        }
        ::v-deep .el-table th > .cell {
            text-align: center;
        }
    
        ::v-deep .el-table .cell {
            text-align: center;
        }
        .block{
            margin-bottom: 30px;
            margin-top: 30px;
            overflow: hidden;
            ::v-deep .el-pagination{
                float: right;
            }
        }
    }
}


</style>
