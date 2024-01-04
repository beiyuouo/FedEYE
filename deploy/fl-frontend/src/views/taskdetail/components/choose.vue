<template>
    <div class="container">
        <div class='left'>
            <div :class="showSf?'top':'htop'">
                <div class="top-title">选择数据：</div>
                <el-row :gutter="10">
                    <el-col :span="12"
                        
                        v-for="(item,index) in fileMeta"
                        :key="index">
                        <div class="content-item">
                            <img @click='chooseDataClick(item,index)' class="dataImg" :src='item.selected?img2:img1' alt="">
                            <!-- <div @click='chooseDataClick(item,index)' class="item"  :class='item.selected?"active-data-box":"data-box"' ></div> -->
                            <div class="dataName">
                                <el-tooltip  effect="dark" :content="item.name" placement="top-start">
                                    <span>
                                        {{item.name}}
                                    </span>
                                 </el-tooltip>
                            </div>
                        </div>

                    </el-col>
                </el-row>
            </div>
            <el-divider v-if="showSf"></el-divider>
            <div v-if="showSf" class="bottom">
                <div class="bottom-title">选择算法：</div>
                <el-row :gutter="10">
                    <el-col :span="12"
                        v-for="(item,index) in algorithm"
                        :key="index">
                        <div class="content-sf-item">
                            <img @click='chooseSf(item,index)' class="sfImg" :src='item.selected?img3:img4' alt="">
                            <!-- <div @click='chooseSf(item,index)' class="item" :class='item.selected?"active-suanfa":"suanfa-box"' ></div> -->
                            <div class="dataName">
                                <el-tooltip  effect="dark" :content="item.nameCn" placement="top-start">
                                   
                                    <span>
                                        {{item.nameCn}}
                                    </span>
                                 </el-tooltip>
                               
                                
                            </div>
                        </div>  
                        
                    </el-col>
                </el-row>
            </div>
        </div>
        <el-dialog
            title="数据对比错误"
            :visible.sync="creatVisible"
            width="1200"
            :before-close='beforeCloseClick'
            :show-close='false'
           
            >
            <div  class="content-box">
                <div class="title">请与发起方格式进行对比，确保该数据集的数据类型、格式符合预期：</div>
                <div class="table-box">
                    <table  class="myTable">
                        <tbody>
                            <tr>
                                <td  class="theader">数据名称</td>
                                <td v-for="(item,index) in tableData1" :key="index" class="column">{{ item }}</td>
                            </tr>
                            <tr>
                                <td  class="theader">数据格式</td>
                                <td v-for="(item,index) in tableData2" :key="index" class="column">{{ item }}</td>
                            </tr>
                        </tbody>
                    
                    </table>
                </div>
            </div>
            <div slot="footer" class="dialog-footer">
                <el-button class="save" @click='beforeCloseClick' type="primary" >确定</el-button>
                <el-button plain  @click="beforeCloseClick">取消</el-button>
            </div>
        </el-dialog>
        <el-dialog
            :visible.sync="creatdataVisible"
            width="60%"
            :before-close='beforeCloseDataClick'
            :show-close='true'
            
            >
            <span slot="title" class="dialog-title">
                <span class="dialogTitle">数据选择</span>
                <!-- <span v-show="!showSf" class="">
                    <el-link type="primary" :underline="false">发起方要求格式</el-link>
                </span> -->
            </span>
            <div v-if="!showPicTableFlg">
                <el-table
                    :data="tableData"
                    border
                    ref='multipleTable'
                    :row-key="getKeys"
                    @selection-change='userSelectionChange'
                    style="width: 100%"
                    >
                        <el-table-column
                            fixed
                            type="selection"
                            :reserve-selection='true'
                            width="55">
                        </el-table-column>
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
                    ref='multipleTable'
                    :row-key="getKeys"
                    @selection-change='userSelectionChange'
                    style="width: 50%"
                    >   
                    <div>
                        <el-table-column
                            fixed
                            type="selection"
                            :reserve-selection='true'
                            width="55">
                        </el-table-column>
                        <el-table-column
                            prop="name"
                            label="名称"
                            >
                        </el-table-column>
                        <el-table-column>
                            <template slot-scope="scope">
                               
                                <span style="margin-left: 10px">
                                    <el-image 
                                        style="width: 100px; height: 100px"
                                        :src="`${imgUrl}`+ scope.row.path_small+'&token='+ `${token}` "
                                        @click.capture="bigImg(scope.row.path)"
                                       
                                        :preview-src-list="srcList">
                                    </el-image>
                                    <!-- <img @click="bigImg(scope.row)" class="img" :src="`${imgUrl}`+ scope.row.path_small "  alt=""> -->
                                    <!-- <div>{{scope.row.name+'.jpg'}}</div> -->
                                </span>
                            </template>
                            
                        </el-table-column>
                    </div>  
                </el-table>
            </div>
            <div class="block">
                <div>
                    <el-button plain v-if="!showPicTableFlg"  @click='chooseAllClick(tableData)' >全选本页</el-button>
                    <el-button plain v-if="showPicTableFlg"  @click='chooseAllClick(picListData)' >全选本页</el-button>
                </div>
                <el-pagination
                    v-if="!showPicTableFlg"
                    @size-change="handleSizeChange"
                    @current-change="handleChangePage"
                    :current-page.sync="pagination.currentPage"
                    :page-size="pagination.limit"
                    :total="pagination.total">
                </el-pagination>
                <el-pagination
                    v-if="showPicTableFlg"
                    background
                    @current-change="handleChangePage1"
                    :current-page.sync="pagination1.currentPage"
                    :page-size="pagination1.limit"
                    layout="prev, pager, next"
                    :total="pagination1.total">
                </el-pagination>
                
                <div>
                    <el-button plain  @click='chooseAllDataClick'  >全量数据集</el-button>
                    <el-button class="save" @click='chooseDataSaveClick' type="primary" >确定</el-button>
                </div>
            </div>
        </el-dialog>
    </div>
</template>
<script>
import {queryDataId} from '@/api/index'
import { summaryCompare, queryFlJobDataInfo} from '@/api/task'
import { getTenantId ,getToken} from "@/utils/auth"
import { listOfMata ,selectedMeta} from '@/api/index'
import img1 from '@/assets/tool_bg_sensor_dataset.svg'
import img2 from '@/assets/tool_bg_image_active_dataset.svg'   
import img3 from '@/assets/tool_btn_active_model.svg'
import img4 from '@/assets/tool_bg_model.svg'
export default {
    props: {
        fileMeta: Array,
        algorithm: Array,
        id: String,
        partInfo: String,
        algFlag: Boolean,
        flType: Number

    },
    data() {
        return {
            activesf: false,
            activedata: false,
            currentSfIndex: -1,
            currentDataIndex: -1,
            creatVisible: false,
            creatdataVisible: false,
            tableData1: [],
            tableData2: [],
            dataId: '',
            suanfaCom: '',
            dataName: '',
            showSf: true,
            titleText: '',
            createParty: '',
            path: '',
            params: { 

            },
            contentType: '',
            pagination: {
                offset: 0,
                total: 0,
                currentPage: 1,
                limit: 10
            },
            pagination1: {
                offset: 0,
                total: 0,
                currentPage: 1,
                limit: 3
            },
            tableData: [{
            }],
            labelData:[],
            newData:[],
            chosetItemId: '',
            tableName: '',
            dataContentType: '',
            showPicTableFlg: false,
            listchoose: [],
            imgUrl: '',
            picListData: [],
            srcList:[],
            fqId: '',
            targetself: '',
            originContentType: '',
            img1: img1,
            img2: img2,
            img3: img3,
            img4: img4,
            selectAll: true
            

        }
    },
    mounted() {
        this.queryFlJobDataInfo(this.id)
        this.token = getToken()
        this.imgUrl = sessionStorage.getItem("uploadUrl")+'/viewpic?path=';
        let that = this
        let arr = this.algorithm.filter(function(item) {
            if(item.selected) {
                
                that.$emit('choosesf',item.name,item.id, item.nameCn)
                return
            }
        })
        if(this.partInfo == 'guest') {
            // 联邦方
            this.showSf = true;
        } else {
            // 参与方
            this.showSf = false
        }
    },
    methods: {
        // 选择全量数据
        chooseAllDataClick() {
            this.newData = []
            this.creatdataVisible = false
            this.$confirm('是否使用全量数据集作为训练数据？点击确定完成数据选择，选择部分数据请点击取消', '提示', {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning'
                }).then(() => {
                    //点击确定
                    this.newData = []
                    this.selectedMeta()
                }).catch(() => {
                    //点击取消
                    this.creatdataVisible = true
                    // this.$message.error('请勾选数据集')      
                });
        },
        // 选择本页数据
        chooseAllClick(rows) {
            
            if (rows) {
                // 全选
                if(this.selectAll) {
                    rows.forEach(row => {
                        this.$refs.multipleTable.toggleRowSelection(row,true);
                    });
                    // this.selectAll = !this.selectAll
                } else {
                    this.$refs.multipleTable.clearSelection();
                    // this.selectAll = !this.selectAll
                }
                
            } else {
                this.$refs.multipleTable.clearSelection();
            }
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
            this.listOfMata()
        },
        // 更新数据集
        async selectedMeta() {
            let Url =  sessionStorage.getItem("uploadUrl");
            let data = {
                jobId: this.id,
                tableName: this.tableName,
                ids: this.newData,
                createParty: sessionStorage.getItem("nameEn")

            }
            
            await selectedMeta(data, Url, getTenantId()).then(res=>{
                if(res.data.code == 200) {
                    this.creatdataVisible = false
                    if(this.partInfo == 'guest') {
                        // 联邦方
                        this.queryDataId(this.chosetItemId)
                    } else {
                        this.$emit('reastChoose',this.dataId,this.dataName,this.compareFlag)
                    }
                    
                }
                
            })
            .catch(err=>{

            })
        },
        // 选择数据条数
        chooseDataSaveClick() {
            // 点击确认按钮，选中数据
            if(this.newData.length > 0) {
                // console.log(this.newData)
                this.selectedMeta()
            } else {
                this.creatdataVisible = false
                this.$confirm('未选择任何数据，系统将默认您使用全量数据集，确定使用吗?', '提示', {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning'
                }).then(() => {
                    //点击确定
                    this.newData = []
                    this.selectedMeta()
                }).catch(() => {
                    //点击取消
                    this.creatdataVisible = true
                    // this.$message.error('请勾选数据集')      
                });
                
            }
            
            // this.creatdataVisible = false
            // this.queryDataId(this.chosetItemId)
          
        },
        //获取选择的key
        getKeys(row) {
            return row.id
            // console.log(row)
        },
        userSelectionChange(arr) {
            this.newData = []
            arr.map((item)=> {
                this.newData.push(Object.assign({}, {id: item.id}))
            })
           
        },
        handleChangePage(current) {
            this.pagination.currentPage = current;
            this.listOfMata()
        },
        handleChangePage1(current) {
            this.pagination1.currentPage = current;
            this.listOfMata()
        },
        tableRowClassName({row, rowIndex}) {
            if (rowIndex%2==0) {
                return 'warning-row';
            } else  {
                return 'success-row';
            }
        },
        // 数据集展示
        async listOfMata() {
            let Url =  sessionStorage.getItem("uploadUrl");
            
            let params = {}
            if(this.dataContentType == 'vision') {
                params = {
                    pageNo: this.pagination1.currentPage,
                    pageSize: this.pagination1.limit,
                    tableName: this.tableName
                }
            } else {
                params = {
                    pageNo: this.pagination.currentPage,
                    pageSize: this.pagination.limit,
                    tableName: this.tableName
                }
            }
            await listOfMata(params,Url,getTenantId()).then(res=>{
                if(res.data.code == 200 ) {
                    if(this.dataContentType == "tabular") {
                        // this.showPicTableFlg = false;
                        this.labelData = []
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
                        this.creatdataVisible = true 
                    } else if(this.dataContentType == "vision") {
                        // this.showPicTableFlg = true;
                        this.pagination1.total  = res.data.result.total
                        this.picListData = res.data.result.data
                        this.creatdataVisible = true 
                    }
                    
                }
            })
            .catch(err=>{

            })
        },
        toggle(arr) {
            arr.forEach(item=>{
                this.$refs.multipleTable.toggleRowSelection(item)
            })
        },
        // 确认数据选择
        saveDataClick () {
            //点击确定 要重新调用
            
            if(this.showSf) {
                // 发起方
                this.$emit('reastChoose',this.dataId,this.dataName,this.compareFlag,this.contentType)
                this.creatVisible = false
            } else {
                if(this.contentType != 'vision') {
                    // 参与方
                    this.summaryCompare(this.params)
                } else {
                    this.creatVisible = false
                    this.compareFlag = true
                    this.$emit('reastChoose',this.dataId,this.dataName,this.compareFlag)
                }
                
            }
        },
        // 关闭dig
        beforeCloseClick() {
            this.dataId = ''
            this.creatVisible = false;
            this.datatypeFlag = false
        },
        beforeCloseDataClick() {
            this.creatdataVisible = false;
        },
        // 查询数据具体信息
        async queryDataId(id) {
            let params = {
                id: id 
            }
            await queryDataId(params).then(res=>{
                if(res.data.code == 200) {
                    this.dataName =  res.data.result.name
                    this.contentType = res.data.result.contentType
                    this.targetself = res.data.result.createParty
                    // nlp数据的情况
                    if(this.contentType == 'nlp') {
                        if(this.partInfo == 'guest') {
                            // 发起方
                            this.dataId = res.data.result.id
                            this.saveDataClick()
                        } else {
                            // 参与方
                            this.dataId = id 
                            this.params = {
                        
                                    self:{
                                        createParty: this.createParty, 
                                        id: this.fqId
                                    },
                                    // 发起方
                                    target: {
                                        createParty: this.targetself, 
                                        id: this.dataId
                                    }
                            }
                            this.flTypeFn()
                          
                        }
                    } 
                    else if(this.contentType == 'graph') {
                        if(this.partInfo == 'guest') {
                            // 发起方
                            this.dataId = res.data.result.id
                            this.saveDataClick()
                        } else {
                            // 参与方
                            this.dataId = id 
                            this.params = {
                        
                                    self:{
                                        createParty: this.createParty, 
                                        id: this.fqId
                                    },
                                    // 发起方
                                    target: {
                                        createParty: this.targetself, 
                                        id: this.dataId
                                    }
                            }
                            this.flTypeFn()
                        }
                    }
                    else if(this.contentType == 'yolo') {
                        if(this.partInfo == 'guest') {
                            // 发起方
                            this.dataId = res.data.result.id
                            this.saveDataClick()
                        } else {
                            // 参与方
                            this.dataId = id 
                            this.params = {
                        
                                    self:{
                                        createParty: this.createParty, 
                                        id: this.fqId
                                    },
                                    // 发起方
                                    target: {
                                        createParty: this.targetself, 
                                        id: this.dataId
                                    }
                            }
                            this.flTypeFn()
                        }
                    }
                    // 表格数据的情况
                    else if(this.contentType == 'tabular') {
                        if(this.partInfo == 'guest') {
                            // 联邦方
                             // csv数据
                            this.dataId = id 
                            this.saveDataClick()
                        } else {
                            // 参与方
                            // this.showSf = false
                            this.slectDataItem(res.data.result,id)
                        }
                    }
                    // 图片数据的情况 
                    else if (this.contentType == 'vision') {
                        if(this.partInfo == 'guest') {
                            // 联邦方
                            // 图片数据
                            this.dataId = res.data.result.id
                            this.saveDataClick()
                        } else {
                            // 参与方
                            // this.showSf = false
                            // this.slectDataItem(res.data.result,id)
                            this.dataId = id 
                            this.params = {
                        
                                    self:{
                                        createParty: this.createParty, 
                                        id: this.fqId
                                    },
                                    // 发起方
                                    target: {
                                        createParty: this.targetself, 
                                        id: this.dataId
                                    }
                            }
                            this.flTypeFn()
                           
                        }
                    }


                    // if(this.contentType != 'vision') {
                    //     if(this.partInfo == 'guest') {
                    //         // 联邦方
                    //          // csv数据
                    //         this.dataId = id 
                    //         this.saveDataClick()
                    //     } else {
                    //         // 参与方
                    //         // this.showSf = false
                    //         this.slectDataItem(res.data.result,id)
                    //     }
                       
                        
                        
                    // } else {
                    //     if(this.partInfo == 'guest') {
                    //         // 联邦方
                    //         // 图片数据
                    //         this.dataId = res.data.result.id
                    //         this.saveDataClick()
                    //     } else {
                    //         // 参与方
                    //         // this.showSf = false
                    //         // this.slectDataItem(res.data.result,id)
                    //         this.dataId = id 
                    //         this.params = {
                        
                    //                 self:{
                    //                     createParty: this.createParty, 
                    //                     id: this.fqId
                    //                 },
                    //                 // 发起方
                    //                 target: {
                    //                     createParty: this.targetself, 
                    //                     id: this.dataId
                    //                 }
                    //         }
                    //         this.summaryCompare(this.params)
                    //     }
                       
                    // }
                    
                }
            })
            .catch(err=>{

            })
        },
        chooseDataClick(item) {
           
            this.chosetItemId = item.id
            this.dataContentType = item.contentType
            this.tableName = item.tableName
            // 判断类型 当前数据类型
            if(item.contentType == "nlp") {
                
                if(this.partInfo == 'guest') {
                    this.queryDataId(item.id)
                    // 联邦方
                } else {
                    // 参与方
                    this.queryDataId(item.id)
                }
                
            } else if(item.contentType == 'graph') {
                if(this.partInfo == 'guest') {
                    this.queryDataId(item.id)
                    // 联邦方
                } else {
                    // 参与方
                    this.queryDataId(item.id)
                }
            } else if(item.contentType == 'yolo') {
                if(this.partInfo == 'guest') {
                    this.queryDataId(item.id)
                    // 联邦方
                } else {
                    // 参与方
                    this.queryDataId(item.id)
                }
            } 
            else {
                // 之前图片数据和表格数据格式
                this.pagination = {
                    offset: 0,
                    total: 0,
                    currentPage: 1,
                    limit: 10
                }
                this.pagination1 = {
                    offset: 0,
                    total: 0,
                    currentPage: 1,
                    limit: 3
                }
                this.newData  = []
                
                
            
                if(this.partInfo == 'guest') {
                    // 联邦方
                    if(this.dataContentType == "tabular") {
                        this.showPicTableFlg = false;
                        this.listOfMata()
                    } else {
                        this.showPicTableFlg = true;
                        this.listOfMata()
                    }
                } else {
                    // 参与方
                    this.queryDataId(item.id)
                }
            }
           
            
        },
        chooseSf(item,index) {
            if(this.algFlag) {
                return 
            }
            // console.log(item)
            // this.currentSfIndex = index
            // this.activesf = true
           
            this.$emit('choosesf',item.name,item.id,item.nameCn)
        },
        slectDataItem(obj,id) {
            
         
            this.tableData1 = []
            this.tableData2 = []
            this.tableData1 = obj.columns.split(",")
            this.tableData1.filter(item=>{
                this.tableData2.push(JSON.parse(obj.columnType)[item])
            })
            this.dataId = id 
            this.params = {
        
                    self:{
                        createParty: this.createParty, 
                        id: this.fqId
                    },
                    // 发起方
                    target: {
                        createParty: this.targetself, 
                        id: this.dataId
                    }
            }
            this.flTypeFn()
            // this.datatypeFlag = true
        },
         // 对比信息
        async summaryCompare(params) {
            await summaryCompare(params).then(res=>{
                if(res.data.success) {
                   this.descFlag = true
                   if(res.data.result == null)  {
                        // this.creatVisible = false
                        this.compareFlag = true
                        // this.$emit('reastChoose',this.dataId,this.dataName,this.compareFlag)
                        // nlp的数据情况
                        if(this.contentType == "nlp") {
                            // 直接
                            this.$emit('reastChoose',this.dataId,this.dataName,this.compareFlag)
                        }
                        // 图神经数据 
                        else if(this.contentType == "graph") {
                             // 直接
                            this.$emit('reastChoose',this.dataId,this.dataName,this.compareFlag)
                        }
                        // yolo 数据
                        else if(this.contentType == 'yolo') {
                            
                            this.$emit('reastChoose',this.dataId,this.dataName,this.compareFlag)
                        }
                         else {
                            if(this.dataContentType == "tabular") {
                                this.showPicTableFlg = false;
                                this.listOfMata()
                            } else {
                                this.showPicTableFlg = true;
                                this.listOfMata()
                            }
                        }
                        
                   } else {
                        this.compareFlag = false
                        this.creatVisible = false;
                        this.titleText = JSON.stringify(res.data.result)
                        
                   } 
                   
                }
            })
            .catch(res=>{
                if(this.originContentType != 'vision') {
                    // 不是图片数据集
                    if(res.message=='请等待发起方上传数据!') {
                        return
                    } else {
                        this.creatVisible = true;
                    }
                }  else {
                    return
                }
                // console.log(this.originContentType)
                // console.log(res)
                
            })
        },
        // 发起方数据格式
        async queryFlJobDataInfo(id) {
            let params = {
                id: id
            }
            await queryFlJobDataInfo(params).then(res=>{
                // this.tableData2 = []
                if(res.data.success) {
                    this.createParty = res.data.result.createParty
                    this.originContentType = res.data.result.contentType
                    this.path = res.data.result.path
                    this.fqId = res.data.result.id
                     this.tableData1 = []
                    this.tableData2 = []
                    this.tableData1 = res.data.result.columns.split(",")
                    this.tableData1.filter(item=>{
                        this.tableData2.push(JSON.parse(res.data.result.columnType)[item])
                    })
                    // this.tableData = res.data.result.columns.split(",")
                    // this.tableData.filter(item=>{
                    //     this.tableData2.push(JSON.parse(res.data.result.columnType)[item])
                    // })
                }
            })
            .catch(res=>{
                
            })
        },
        // 纵向任务流程
        flTypeFn(){
            if(this.flType == '2') {
                // 纵向联邦
                this.compareFlag = true
                if(this.contentType == "nlp") {
                    // 直接
                    this.$emit('reastChoose',this.dataId,this.dataName,this.compareFlag)
                }
                // 图神经数据 
                else if(this.contentType == "graph") {
                        // 直接
                    this.$emit('reastChoose',this.dataId,this.dataName,this.compareFlag)
                }
                // yolo 数据
                else if(this.contentType == 'yolo') {
                            
                    this.$emit('reastChoose',this.dataId,this.dataName,this.compareFlag)
                }
                else {
                    if(this.dataContentType == "tabular") {
                        this.showPicTableFlg = false;
                        this.listOfMata()
                    } else {
                        this.showPicTableFlg = true;
                        this.listOfMata()
                    }
                }
            } else {
                // 横向联邦
                this.summaryCompare(this.params)
            }
        }
    },
    watch: {
        // tableData: {
        //     handler(value) {
        //         let that = this 
        //         value.forEach(item=>{
        //             console.log(this.$refs.multipleTable)
        //             // that.$refs.multipleTable.toggleRowSelection(item)
        //         })
                
        //         // console.log(value)
        //     },
        //     deep: true
        // }
    }
}
</script>
<style lang="scss" scoped>
    .left {
        width: 232px;
        // height: 1052px;
        height: 100%;
        background: #FFFFFF;
        border-radius: 3px;
        .top{
            height: 60vh;
            overflow: auto;
            padding: 0 30px;
            ::v-deep .el-row{
                display:flex;
                flex-wrap: wrap;
            }
            .top-title{
                padding-top: 50px;
                font-weight: 700;
                font-style: 18px;
            }
            // .dataName{
            //     display: -webkit-box;
            //     -webkit-box-orient: vertical;
            //     -webkit-line-clamp: 4;
            //     overflow: hidden;
            // }
            .content-box{
                display: flex;
                flex-wrap: wrap;
               
            }
        }
        .htop{
            height: 10  0vh;
            overflow: auto;
            padding: 0 30px;
            ::v-deep .el-row{
                display:flex;
                flex-wrap: wrap;
            }
            .top-title{
                padding-top: 50px;
                font-weight: 700;
                font-style: 18px;
            }
            .content-box{
                display: flex;
                flex-wrap: wrap;
               
            }
        }
        .bottom{
           
            height: 60vh;
            overflow: auto;
            overflow-y: auto;
            padding: 0 20px;
            ::v-deep .el-row{
                display:flex;
                flex-wrap: wrap;
            }
            .content-sf-item{
                text-align: center;
                 .item {
                    // line-height: 80px;
                    // overflow: hidden;
                    // text-align: center;
                    width: 80px;
                    height: 80px;
                    font-size: 12px;
                    display: flex;
                    align-items: center;
                    background-size: 80px 80px;
                    cursor: pointer;
                
                
                }
                .sfImg{
                    width: 80px;
                    height: 80px;
                    cursor: pointer;
                }
                .dataName{
                    // padding: 0 10px;
                    // width: 80px;
                    span{
                        text-align: center;
                        display: -webkit-box;
                        -webkit-box-orient: vertical;
                        -webkit-line-clamp: 3;
                        word-break:break-all;
                        overflow: hidden;
                    }
                    margin-bottom: 20px;
                }
            }
        }
    }
    .content-item{
        text-align: center;
          .item {
            // line-height: 80px;
            // overflow: hidden;
            // text-align: center;
            width: 80px;
            height: 80px;
            font-size: 12px;
            display: flex;
            align-items: center;
            // background-size: 80px 80px;
            cursor: pointer;
        
        
        }
        .dataImg{
            width: 80px;
            height: 80px;
            cursor: pointer;
        }
        .dataName{
            // padding: 0 10px;
            // width: 80px;
            // text-align: center;
            span{
                display: -webkit-box;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: 3;
            word-break:break-all;
            overflow: hidden;
            }
            
            margin-bottom: 20px;
        }
    }
  
    .data-box {
        
        background-image: url('../../../assets/tool_bg_sensor_dataset.svg');
    }
    .active-data-box {
        background-image: url('../../../assets/tool_bg_image_active_dataset.svg');
    }
    .suanfa-box {
        background-image: url('../../../assets/tool_bg_model.svg');
    }
    .active-suanfa {
        background-image: url('../../../assets/tool_btn_active_model.svg');
    }
    .right{
        margin-left: 20px;
        width: 100%;
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
.save{
       
    margin-left: 20px;
}
::v-deep .el-card__body{
    padding: 0;
}
::v-deep .el-select{
    width: 100%;
}
.digbox{
    height: 400px;
    overflow: auto;
}
::v-deep .el-dialog__body {
    overflow: auto;
}
.photo-box{
    height: 300px;
    padding: 30px;
    display: flex;
    overflow: auto;
    // background-color: #ccc;
    border: 1px solid #ccc;
    border-radius: 10px;
    .photo-left {
        flex: 1;
        .radiu-box{ 
            position: relative; 
            width: 100px;
            height: 92px;
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
                    font-size: 12px;
                    // font-weight: 700;
                }
                .content-footer{
                    
                    margin-top: 50px;
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
            width: 100px; 
            height: 20px; 
            border-radius:50%; 
            background: #fff;
            box-shadow: 0px 3px 15px 9px rgba(226,226,226,0.29);
        }

        .radiu-box:after{
            position: absolute; 
            bottom:-10px; 
            content: ""; 
            width: 100px; 
            height: 20px; 
            border-radius:50%; 
            background: linear-gradient(to right, #ededf6,#fff);
            // box-shadow: 0px 3px 15px 9px rgba(226,226,226,0.29);
        }
    }
    ::v-deep .el-divider--vertical{
        height: 100%;
    }
    .photo-right {
        flex: 1;
    }

}
  ::v-deep .el-table .warning-row {
    background: oldlace;
  }

  ::v-deep .el-table .success-row {
    background: #f0f9eb;
  }
  .block{
    margin-top:20px;
    text-align: center;
    display: flex;
    justify-content:center;
    align-items: center;
    ::v-deep .el-pagination {
        flex: 1;
    }
  }
  .dialogTitle{
    font-weight: 700;
    // font-size: 20px;
    margin-right: 30px;
  }
  ::v-deep .el-link--inner{
    text-decoration:underline;
  }
  .pic-table{
    ::v-deep .el-table{
        
    }
    // ::v-deep .el-table__body:hover{
    //     background: rgba(255,255,255,1);
    // }
    ::v-deep .el-table .cell{
        text-align: center;
    }
    ::v-deep .el-table__row{
        // display: flex;
    }
    ::v-deep .el-table__header-wrapper{
        // display:none;
    }
    // ::v-deep .el-table_1_column_2  {
    //     display:none;
    // }
    // ::v-deep .el-table_1_column_3 {
    //     display:none;
    // }
    // ::v-deep .el-table_1_column_5 {
    //     display:none;
    // }
    // ::v-deep .el-table_1_column_6{
    //     display:none;
    // }
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