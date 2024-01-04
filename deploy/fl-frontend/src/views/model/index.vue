<template>
    <div class="mytaks-box">
        <div class="mytaks-header">
            <div class="title">
                <div>
                    模型池
                </div>
            </div>
            <div class="header-right">
                <div class="more">
                    <el-input
                        placeholder="请输入模型名称"
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
        <div class="task-item">
            <el-row v-if="allData.length>0" :gutter="30">
                <el-col class='col-box' v-for="(item,index) in allData"
                        :key="index" :lg="8" :xl= '8'>
                    <modal-box  v-on:click.native = "gotoDetil(item.id)"  :paramsDesc='paramsDesc'  :item='item'></modal-box>
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
import ModalBox from '@/components/model.vue'
import { listPoolModel , parameterDesc }  from '@/api/index'
import { getTenantId} from "@/utils/auth";
export default {
    components:{
        ModalBox,
    },
    created() {
        this.listPoolModel()
        this.parameterDesc()
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
                limit: 9
            },
            DataList:[],
            listAllAlgorithmname:'',
            allAlgorithmObj: null,
            allData: [],
            paramsDesc: []
        }
    },
    methods:{
        // 模型参数说明
        async parameterDesc() {
            await parameterDesc().then(res=>{
                if(res.data.code == 200) {
                    this.paramsDesc = res.data.result
                }
                
            })
            .catch(res=>{
                 console.log(res)
            })
        },
        // 模型查询
        searchMydata() {
            this.pagination.currentPage = 1
            this.listPoolModel()
        },
        // 数据清空
        dataClearClick() {
            this.input = ''
            this.listPoolModel()
        },
        //模型池数据
        async listPoolModel() {
            // let id = getTenantId()
            let params ={
                name: this.input,
                pageNo:this.pagination.currentPage,
                pageSize: this.pagination.limit
            }
            await listPoolModel(params,getTenantId()).then(res=>{
                
                if(res.data.code == 200) {
                    this.allData = res.data.result.records
                    this.pagination.total = res.data.result.total
                }
            })
        },
        handleChangePage(current) {
            this.pagination.currentpage = current;
            this.listPoolModel()
        },
     
        gotoDetil(id) {
            this.$router.push({
                name:'modalDetail',
                query: {
                    id:  id
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
.mytaks-box{
    border-radius: 10px;
    padding: 30px;
    height: 100%;
    overflow: hidden;
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
                    background: #0B80EA;
                    color: #fff; 
                }
            }
        }
      
    }
    .task-item{
        .col-box{
            
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
        .col-box:before {
           
        }
        .col-box:after {
            
        }
    }
}

</style>

