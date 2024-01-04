<template>
    <div class="mytaks-box">
        <div class="mytaks-header">
            <div class="title">
                <div>
                    我的模型
                </div>
            </div>
            <div class="header-right" v-if="!touristFlag" >
                 <div class="icon-box">
                    <svg-icon :class="showCard?'active':''"  @click="cardShowClick" icon-class="card" /> 
                    <svg-icon :class="showList?'active':''" @click="listShowClick" icon-class="list" /> 
                </div>
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
        <div v-if="!touristFlag" class="task-item">
            <my-model-table v-show="showList" :allData='allData'></my-model-table>
            <el-row v-show="showCard" :gutter="30">
                <el-col v-for="(item,index) in allData"
                        :key="index" :lg="8" :xl= '8'>
                    <modal-box :paramsDesc='paramsDesc'  v-on:click.native = "gotoDetil(item.id)"   :item='item'></modal-box>
                </el-col>
            </el-row>
            <div v-if="allData.length == 0" class="img-box"  >
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
import { getTenantId } from "@/utils/auth";
import { listMyModel,parameterDesc}  from '@/api/index'
import ModalBox from '@/components/model.vue'
import MyModelTable from '@/components/MyModelTable.vue'
export default {
    name: 'Mymodal',
    components:{
        ModalBox,
        MyModelTable
    },
    created() {
        if(sessionStorage.getItem('tourist')== 'true') {
             this.touristFlag = true
        } else {
             this.touristFlag = false
             this.listMyModel()
             this.parameterDesc()
        }
        
    },
    data() {
        return {
            touristFlag: false,
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
            paramsDesc: [],
            showCard: true,
            showList: false
        }
    },
    methods:{
        cardShowClick() {
            this.showCard = true
            this.showList = false
        },
        listShowClick() {
            this.showCard = false
            this.showList = true
        },
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
         // 任务查询
        searchMydata() {
            this.pagination.currentPage = 1
            this.listMyModel()
        },
        // 数据清空
        dataClearClick() {
            this.input = ''
            this.listMyModel()
        },
        //模型池数据
        async listMyModel() {
            let id = getTenantId()
            let params ={
                name: this.input,
                pageNo:this.pagination.currentPage,
                pageSize: this.pagination.limit
            }
            await listMyModel(params,id).then(res=>{
                if(res.data.code == 200) {
                    this.allData = res.data.result.records
                    this.pagination.total = res.data.result.total
                }
            })
        },
        handleChangePage(current) {
            this.pagination.currentpage = current;
            this.listMyModel()
        },
     
        gotoDetil(id) {
            this.$router.push({
                name:'modalDetail',
                query: {
                    id:  id,
                    from: 'myModal'
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
    .mytaks-header {
        height: 40px;
        // line-height: 50px;
        margin: 20px 0;
        overflow: hidden;
        display: flex;
        align-items: center;
        justify-content: space-between;
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
                   
                    color: #fff; 
                }
            }
        }
        
    }
}

</style>
