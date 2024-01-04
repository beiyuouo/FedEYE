<template>
    <div class="data-box">
         
        <bread-crumb :breadArray='breadArray'></bread-crumb>
       
        <div class="box">
            <div class="info-box">
                <div v-if="showDeleBtn" class="invcodejoin">
                    <el-button  @click='deleteDataClick' type="text">删除数据</el-button>
                </div>
                <div class="title">联邦方信息：</div>
               
                <el-table
                    :data="infoParty"
                    border
                    :header-cell-style="{background:'#D2E4F5',color:'#484848'}"
                    style="width: 100%">
                        <el-table-column
                            prop="name"
                            label="联邦方名称"
                            >
                        </el-table-column>
                        <el-table-column
                            v-if="!showMyFlag"
                            prop="createBy"
                            label="上传者"
                            >
                        </el-table-column>
                        <el-table-column
                            prop="createTime"
                            label="创建时间"
                            >
                        </el-table-column>
                        
                </el-table>
            </div>
            <div class="header-box">
                <div class="title">基本信息：</div>
                <el-table
                    :data="baseInfoData"
                    border
                    :header-cell-style="{background:'#D2E4F5',color:'#484848'}"
                    style="width: 100%">
                        <el-table-column
                            prop="createTime"
                            label="上传日期"
                            >
                        </el-table-column>
                        <el-table-column
                            prop="name"
                            label="文件名称"
                            width="180">
                        </el-table-column>
                        <el-table-column
                            prop="size"
                            
                            label="文件大小">
                            <!-- label="文件大小(Bytes)" -->
                        </el-table-column>
                        <el-table-column
                            prop="contentType"
                            label="数据集类型">
                        </el-table-column>
                        <el-table-column
                            prop="rowsNum"
                            label="数据行数">
                        </el-table-column>
                        <el-table-column
                            prop="note"
                            label="数据描述">
                        </el-table-column>
                </el-table>
            </div>
            <div v-if="!showMyFlag&&(contentType!='nlp'&&contentType!='graph')">
                    <div v-if="contentType!='vision'" class="content-box">
                    <div class="title">字段信息：</div>
                    <div class="table-box">
                        <table  class="myTable">
                            <tbody>
                                <tr>
                                    <td class="theader">数据名称</td>
                                    <td v-for="(item,index) in tableData1" :key="index" class="column">{{ item }}</td>
                                </tr>
                                <tr>
                                    <td class="theader">数据格式</td>
                                    <td v-for="(item,index) in tableData2" :key="index" class="column">{{ item }}</td>
                                </tr>
                            </tbody>
                        
                        </table>
                    </div>
                </div>
            </div>
            
        </div>
        <div  v-if="showMyFlag">
            <div class='box-detail' v-if="showMyFlag&&(contentType!='nlp'&&contentType!='graph')">
                <div v-if="!showPicTableFlg">
                    <el-table
                        :data="tableData"
                        v-loading="loading1"
                        border
                        ref='multipleTable'
                        style="width: 100%"
                    
                        >   
                        <el-table-column
                            v-for="(item,index) in labelData"
                            :key=index
                            :prop="item.label"
                            :label="item.label"
                            width="100">
                        </el-table-column>
                    </el-table>
                    
                </div>
                <div class="pic-table" v-if="showPicTableFlg">
                    <el-table
                        :data="picListData"
                        v-loading="loading1"
                        ref='multipleTable'
                        style="width: 100%"
                        >   
                        <div>
                            <el-table-column
                                v-for="(item,index) in picLabelData"
                                :key='index'
                                :prop="item"
                                :label="item"
                                >
                            </el-table-column>
                            <el-table-column
                                fixed
                                >
                                <template slot-scope="scope">
                                    <span style="margin-left: 10px">
                                        <el-image 
                                            style="width: 100px; height: 100px"
                                            :src="`${imgUrl}`+ scope.row.path_small +'&token='+ `${token}`"
                                            @click.capture="bigImg(scope.row.path)"
                                        
                                            :preview-src-list="srcList">
                                        </el-image>
                                    </span>
                                </template>
                            </el-table-column>
                            
                        
                        </div>  
                    </el-table>
                        <!-- <el-row :gutter="10">
                            <el-col :span="6"
                                v-for="(item) in picListData"
                                :key=item.id
                                >
                                <div class="img-box">
                                    <div class="img-item">
                                        <el-image 
                                            style="width: 100px; height: 100px"
                                            :src="`${imgUrl}`+ item.path_small "
                                            @click.capture="bigImg(item.path)"
                                            :preview-src-list="srcList">
                                        </el-image>
                                    
                                        <div class="img-box-footer">
                                            {{item.name}}
                                        </div>
                                    </div>
                                </div>
                            </el-col>
                        </el-row> -->
                </div>
                <div class="block">
                    <el-pagination
                        @size-change="handleSizeChange"
                        @current-change="handleChangePage"
                        :current-page.sync="pagination.currentPage"
                        :page-sizes="[10, 20, 30, 40]"
                        :page-size="pagination.limit"
                        
                        :total="pagination.total">
                    </el-pagination>
                    <!-- <el-pagination
                        background
                        @current-change="handleChangePage"
                        :current-page.sync="pagination.currentPage"
                        :page-size="pagination.limit"
                        layout="prev, pager, next"
                        :total="pagination.total">
                    </el-pagination> -->
                    
                </div>
            </div>
            <div class='box-showInfo' v-else>
                该数据集不支持展示
            </div>
        </div>
        
       
    </div>
</template>
<script>
import BreadCrumb from '@/components/breadcrumb.vue'
import { queryDataId } from '@/api/index'
import { queryById } from '@/api/info'
import { getTenantId,getToken} from '@/utils/auth'
import { listOfMata } from '@/api/index'
import { deleteData } from '@/api/task' 
export default {
    components:{
        BreadCrumb
    },
    data() {
        return {
            tableName: '',
            breadArray: [
                {
                    name: '我的数据',
                    url: '/mydata'
                },
                {
                    name: '数据集名称'
                }
        
            ],
            tableData:[
            ],
            tableData1:[
            ],
            tableData2:[
            ],
            showInfoFlag: false,
            infoParty: [],
            contentType: '',
            showPicTableFlg: false,
            pagination: {
                offset: 0,
                total: 0,
                currentPage: 1,
                limit: 10
            },
            labelData:[],
            imgUrl: '',
            picListData: [],
            picLabelData:[],
            tableBaseData:  [],
            baseInfoData: [],
            srcList:[],
            dataId: '',
            flag: false,
            showMyFlag: false,
            showDeleBtn: false,
            loading1: true
        }
    },
    created() {
    
        this.token  =  getToken()
        this.imgUrl = sessionStorage.getItem("uploadUrl")+'/viewpic?path=';
        this.dataId = this.$route.query.id
        if(this.$route.query.from =='Home' ) {
            // 我的
            this.breadArray = [
                {
                    name: '数据集名称'
                }
            ]
            this.queryDataId(this.$route.query.id)
            this.queryById()
            this.showMyFlag = true
            this.showDeleBtn = false
        } else if(this.$route.query.from =="HomeTask") {
            // 数据池
            this.breadArray = [
                {
                    name: '数据池',
                    url: '/data'
                },
                {
                    name: '数据集名称'
                }
            ]
            this.showMyFlag = false
            this.showDeleBtn = false
            this.queryDataId(this.$route.query.id)
            this.queryById()
            // this.infoParty = getPartyInfo()
        } else if(this.$route.query.from =="mytask") {

            this.breadArray = [
                {
                    name: '我的任务',
                    url: '/mytask'
                },
                {
                    name: '数据集名称'
                }
            ]
            this.queryDataId(this.$route.query.id)
            this.queryById()
            this.showDeleBtn = false
            if(this.$route.query.flag == "true") {
                // 我的
                this.showMyFlag = true
             
            } else {
                // 数据池
                this.showMyFlag = false
             
            }
        } else{
            // 我的
            this.breadArray = [
                {
                    name: '我的数据',
                    url: '/mydata'
                },
                {
                    name: '数据集名称'
                }
            ]
            this.showDeleBtn = true
            this.queryDataId(this.$route.query.id)
            this.queryById()
            this.showMyFlag = true
        }
        // if(this.$route.query.from =="HomeTask") {
           
        // } else if(this.$route.query.from =="mytask") {
        //     // 我的任务
        //     this.queryDataId(this.$route.query.id)
        //     this.queryById()
        //     this.showInfoFlag = true
           
        // } else {
        //     // 我的数据里面
        //     this.queryDataId(this.$route.query.id)
        //     this.queryById()
            
        //     this.showInfoFlag = this.$route.query.flag
        // }
    },
    methods: {
         // 删除我的数据
        deleteDataClick() {
            this.$confirm('确定删除吗?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.deleteData()
            });
        },
        async deleteData() {
            let params = {
                id:  this.dataId    
            }
            await deleteData(params).then(res=>{
                if(res.data.code == 200) {
                    this.$message.success('删除数据成功')
                    this.$router.go(-1)
                }
            })
            .catch(err=>{
                // this.$message.error(err.message)
            })
        },
        // 放大图片
        bigImg(row) {
            let url = this.imgUrl + row +'&token=' + this.token
            this.srcList= []
            this.srcList.push(url)
            // console.log(row)
        },
        handleSizeChange(val) {
            this.pagination.limit = val
            // console.log(val)
            this.listOfMata()
        },
        handleChangePage(current) {
            this.pagination.currentPage = current;
            // console.log(current)
            this.listOfMata()
        },
        // 查询联邦方信息
        async queryById() {
            let id = getTenantId()
            let params = {
                id: this.$route.query.partyId
            }
            await queryById(params).then(res=>{
                if(res.data.success) {
                    this.infoParty.push(res.data.result)
                }
            })
            .catch(res=>{

            })
        },
        // 深拷贝
        deepCopy(obj) {
            var newobj = obj.constructor === Array ? [] : {}
            let vm = this
            if (typeof obj !== "object") {
                return obj
            }
            for (var attr in obj) {
                newobj[attr] = vm.deepCopy(obj[attr])
            }
            return newobj
        },
        async queryDataId(id) {
            let params = {
                id: id
            }
            await queryDataId(params).then(res=>{
                if(res.data.success) {
                    this.baseInfoData.push(res.data.result)
                    this.contentType = res.data.result.contentType
                    this.tableName = res.data.result.tableName
                    if(this.contentType == "tabular") {
                        this.showPicTableFlg = false
                    } else {
                        this.showPicTableFlg = true
                    }
                    // if(this.$route.query.from =="HomeTask") {
                    //     this.tableData1 = res.data.result.columns.split(",")
                    //     this.tableData1.filter(item=>{
                    //         this.tableData2.push(JSON.parse(res.data.result.columnType)[item])
                    //     })
                        
                    // } else {
                    //     this.listOfMata()
                    // }
                    if(this.showMyFlag&&this.contentType!='nlp'&&this.contentType!='graph') {
                         this.listOfMata()
                    } else {
                        this.tableData1 = res.data.result.columns.split(",")
                        this.tableData1.filter(item=>{
                            this.tableData2.push(JSON.parse(res.data.result.columnType)[item])
                        })
                    }
                    

                  
                }
                
            })
            .catch(res=>{
                
            })
        },
        // 数据集展示
        async listOfMata() {
            let Url =  sessionStorage.getItem("uploadUrl");
            
            let params = {
                pageNo: this.pagination.currentPage,
                pageSize: this.pagination.limit,
                tableName: this.tableName
            }
            await listOfMata(params,Url,getTenantId()).then(res=>{
                // let that = this
                if(res.data.code == 200 ) {
                    this.loading1 = false
                    if(this.contentType == "tabular") {
                        this.showPicTableFlg = false;
                        this.labelData = []
                        let firstCopy = JSON.parse(JSON.stringify(res.data.result.data))
                        this.pagination.total  = res.data.result.total
                        this.tableData = res.data.result.data
                        let headerObj = JSON.parse(JSON.stringify( this.tableData[0]))
                    
                        let arr = Object.keys(headerObj)
                        arr.forEach((item)=>{
                            this.labelData.push({
                                label: item,
                                prop: headerObj[item]
                            })
                        })
                    } else if(this.contentType == "vision") {
                        this.showPicTableFlg = true;
                        this.picLabelData = []
                        // this.showPicTableFlg = false;
                        // this.labelData = []
                        this.picLabelData = res.data.result.colums
                        this.picListData = JSON.parse(JSON.stringify(res.data.result.data))
                        this.pagination.total  = res.data.result.total
                        // this.tableData = res.data.result.data
                        // let headerObj = JSON.parse(JSON.stringify( this.tableData[0]))
                    
                        // let arr = Object.keys(headerObj)
                        // arr.forEach((item)=>{
                        //     this.labelData.push({
                        //         label: item,
                        //         prop: headerObj[item]
                        //     })
                        // })
                        // this.$nextTick(function() {
                        //     that.toggle(this.tableData)
                        // })
                        // this.creatdataVisible = true 
                    }
                    
                }
            })
            .catch(err=>{
                this.loading1 = false
            })
        },
    }
}
</script>
<style lang="scss" scoped>
.data-box{
    
    .box {
        height: 100%;
        background: #FFFFFF;
        border-radius: 10px;
        padding: 40px 50px ;
        .info-box{
            .invcodejoin{
                text-align:right;
            }
            .title{
                margin-bottom: 30px;
                font-weight: 700;
                font-size: 16px;
                
            }
            ::v-deep .el-table th > .cell {
                text-align: center;
            }

            ::v-deep .el-table .cell {
                text-align: center;
            }   
        }
        .header-box{
        
           
            .title{
                margin-bottom: 30px;
                margin-top: 30px;
                font-weight: 700;
                font-size: 16px;
                
            }
            ::v-deep .el-table th > .cell {
                text-align: center;
            }

            ::v-deep .el-table .cell {
                text-align: center;
            }
        }
        .content-box{
            .title{
                margin-bottom: 30px;
                margin-top: 60px;
                font-weight: 700;
                font-size: 16px;
                
            }
            ::v-deep .el-table th > .cell {
                text-align: center;
            }

            ::v-deep .el-table .cell {
                text-align: center;
            }
        }
    }
    .box-detail{
        padding: 40px 50px ;
        background-color: #fff;
    }
    .box-showInfo {
        padding: 40px 50px ;
        background-color: #fff;
        text-align: center;
    }
    .pic-table{
        .img-box {
            .img-item{
                text-align: center;
                margin-bottom: 20px;
                .img-box-footer{
                    font-weight: 700;
                    margin-top: 5px;
                }
            }
        }
    }
}
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
        overflow: auto;
        
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
::-webkit-scrollbar {
    width: 3px;
    height: 2px;
}
::-webkit-scrollbar-track {
    background-color: rgba(102, 103, 104, 0.5);
}
.table-box{
    overflow: auto;
}
.block{
    margin-top:20px;
    margin-bottom: 20px;
    text-align: center;
    display: flex;
    justify-content:center;
    ::v-deep .el-pagination {
        flex: 1;
    }
  }
  
</style>
<style>
.el-image-viewer__wrapper{
    /* height: 80%; */
  }
 .el-image-viewer__canvas{
    height: 80%;
    margin-top:5%;
  }
</style>
