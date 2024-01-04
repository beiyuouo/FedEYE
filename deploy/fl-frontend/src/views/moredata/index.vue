<template>
    <div class="box">
        
        <div class="header">
            <div class="title">
                <div>我的数据</div>
                <el-button v-if="!touristFlag" plain size="small"  @click="dataClick"  icon="el-icon-plus" round>上传</el-button>
            </div>
            <div class='header-right'>
                 <div class="icon-box">
                    <svg-icon :class="showCard?'active':''"  @click="cardShowClick" icon-class="card" /> 
                    <svg-icon :class="showList?'active':''" @click="listShowClick" icon-class="list" /> 
                </div>
                <div class="more">
                    <el-input
                        placeholder="请输入数据名称"
                        v-model="input"
                        @clear='dataClearClick'
                        clearable>
                        <el-button type="primary" @click="searchMydata" class="btn" slot="append" icon="el-icon-search"></el-button>
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
        <div v-if="!touristFlag" class="content">
            <keep-alive>
                <my-data-table v-if="tableData.length>0" :tableData='tableData' v-show="showList"></my-data-table>
                <div v-else class="img-box"  >
                    <img src="@/assets/nodataset.png" alt="">
                    <div>
                        暂无数据
                    </div>
                </div>
            </keep-alive>
            <keep-alive>
                <el-row v-show="showCard" v-if="tableData.length>0" :gutter="30">
                    <el-col class='col-box' v-for="(item,index) in tableData"
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
            </keep-alive>
            
        </div>
        <div v-if="touristFlag"  class="img-box">
            <img src="@/assets/binding.png" class="img">
            <div>
                绑定联邦方后激活
            </div>
        </div>  
        <upload @uploadSuccess='uploadSuccess' @cancleCreateClick='cancleCreateClick' :uploadVisible='uploadVisible'></upload>
        <upload-step :successObj='successObj' @cancleCreateClickStep='cancleCreateClickStep' :uploadStepVisible='uploadStepVisible'></upload-step>
    </div>
</template>
<script>
import BreadCrumb from '@/components/breadcrumb.vue'
import upload from '@/components/upload'
import uploadStep from '@/components/uploadDialog'
import { listCurrent} from '@/api/index'
import MyDataTable from '@/components/MyDataTable'
import { getAuth , getTenantId , setIndex } from "@/utils/auth";
export default {
    name: 'Mydata',
    components:{
        BreadCrumb,
        upload,
        uploadStep,
        MyDataTable
    },
    data() {
        return {
            uploadVisible: false,
            uploadStepVisible: false,
            successObj: null,
            breadArray: [
                {
                    name: '我的数据',

                }
        
            ],
            pagination: {
                offset: 0,
                total: 0,
                currentPage: 1,
                limit: 16
            },
            input: '',
            tableData:[
            ],
            touristFlag: false,
            showCard: true,
            showList: false
        }
    },
    created() {
        if(sessionStorage.getItem('tourist')== 'true') {
             this.touristFlag = true
        } else {
             this.touristFlag = false
             this.listCurrent()
        }
       
    },
    methods: {
        cardShowClick() {
            this.showCard = true
            this.showList = false
        },
        listShowClick() {
            this.showCard = false
            this.showList = true
        },
        // 数据查询
        searchMydata() {
            this.pagination.currentPage = 1
            this.listCurrent()
        },
        // 数据清空
        dataClearClick() {
            this.input = ''
            this.listCurrent()
        },
        dataClick() {
            this.uploadVisible =  true
            // this.uploadStepVisible =true
        },
        dataClearClick() {
            this.input = ''
            this.listCurrent()
        },
        cancleCreateClick(){
            this.uploadVisible = false;
        },
        cancleCreateClickStep() {
            this.uploadStepVisible = false
        },
        uploadSuccess(obj) {
            this.uploadVisible = false
            this.uploadStepVisible = true
            this.successObj = obj
        },
        search() {
            this.pagination.currentPage = 1
            this.listCurrent()
            // this.input = ''
        },
        handleChangePage(current) {
            this.pagination.currentPage = current
            this.listCurrent()
        },
        // 我的数据
        async listCurrent() {
            let params = {
                name: this.input,
                pageNo:this.pagination.currentPage,
                pageSize: this.pagination.limit

            }
            let id = getTenantId()
            await listCurrent(params,id).then(res=>{
                this.tableData = res.data.result.records
                this.pagination.total = res.data.result.total
            }) 
            .catch(res=>{
                
            })
        },
        gotoDataDetail(row) {
           
            this.$router.push({
                name: 'DataDetail',
                query: { 
                    partyId: row.partyId,
                    id: row.id
                }
            })
        }
    }
}
</script>
<style lang="scss" scoped>
.box{
    height: 100%;
    // background: #F4F6F8;
    .header{
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
        .header-right{
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
        .more{
            width: 240px;
            .btn{
                color: #fff; 
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
                .content-footer{
                    // width: 200px;
                    // text-align: center;
                    // position: absolute;
                    // /* margin: 0 auto; */
                    // bottom: 30px;
                    // left: 50%;
                    // margin-left: -100px;
                      margin-top: 30px;
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
}
</style>
