<template>
<div style="height:100%">
    <div class="data-box">
        <line-box :logUrl='logUrl' :active='active'></line-box>
        <!-- 新版本 -->
        <!-- <div class="content-box">
            <div class="content-box-top">
                <div class='left'>
                    <div class="title">
                        发起方
                    </div>
                    <el-card class="box-card"
                        @click.native='guestClick(items)'
                        v-for="(items,index) in fljob.slice(0,1)"
                        :key="index">
                        <div :class="[items.envStatus==1?'status':'error']"></div>
                        <div class='name'>
                            {{items.partyName}}
                        </div>
                    </el-card>
                </div>
                <div class='middle'>
                    <div class="title">联邦方</div>
                    <div v-if="fljob.slice(1).length!=0">
                        <el-card  
                            
                            class="box-card"
                            v-for="(items,index) in fljob.slice(1)"
                            @click.native='noGuestClick(items)'
                            :key="index">
                            <div :class="[items.envStatus==1?'status':'error']"></div>
                            <div class='name' >
                                {{items.partyName}}
                            </div>
                        
                            
                        </el-card>
                    </div>
                    <el-card class="box-card" v-else>
                        <div>
                            
                            <div class='name' >
                                暂无参与方
                            </div>
                        </div>
                        
                    </el-card>
                </div>
                <div @click="invcodeJoinClick" class='right'>
                    <div  class="title">邀请联邦方</div>
                    <el-card class="box-card">
                       <img :src="img3" alt="">
                    </el-card>
                </div>
            </div>
            <div class="quote">
                <div></div>
            </div>
            <div class='img-box'>
                <span  class="img-name">
                    {{suanfaName}}
                </span>
                
            </div>
            <div class="icon-box">
                <i class="el-icon-bottom"></i>
            </div>
             <div class='img-box'>
                <span class="img-name">
                    {{taskname}}模型 
                </span>
                
            </div>
        </div> -->
        <taskHeader :jobType='jobType' :suanfaNameCn='suanfaNameCn' :id='id' :algorithmId='algorithmId' :comSuanfaname='suanfaName' :taskname='taskname' :invcodeJoinClick='invcodeJoinClick' :guestClick='guestClick' :noGuestClick='noGuestClick' :showStart='showStart' :fljob='fljob'></taskHeader>
        <div class="bottom-box">
            
           <div class="title">
                <span class="sub-title">{{partName}}:</span>
                <span  class="red">{{statusText}}</span>
                <span class="btn">
                    <el-button @click="reloadClick" type="text">{{btnText}}</el-button>
                </span>

                
            </div>
            <div class="itembox">
                <div class='choosedata'>
                    <div class="title-item">
                        <span class="name">数据：</span>
                        <span class="status">{{dataText}}</span>
                    </div>
                    <div class='img-box'>
                        <span :class="dataText=='导入完成'?'active-data-box':''" class="data-box img-name">
                        </span>
                        <div class="data-name">
                            <span @click="editData"  v-if="contentType=='tabular'&&dataname!=''">
                                <i class="el-icon-edit"></i>
                            </span> 
                            {{dataname}}
                        </div>
                    </div>
                </div>
                <div class="item">
                    <ul class="timeline">
                        <div class="title-item">
                            <span class="name">硬件：</span>
                            <span class="status">{{hardWareFlag==true?'准备完成':'准备未完成'}}</span>
                        </div>
                        <li v-for="(item,index) in hardWareData" :key="index" class="timeline-item oneitem">
                            <div class="timeline-item__tail"></div>
                            <div class="timeline-item__node timeline-item__node--normal timeline-item__node--">
                                <img :src="item.success==0?img1:img2" alt="">
                            </div>
                            <div class="timeline-item__wrapper">
                                <div class="content-item">
                                    {{item.name}}
                                </div>
                            </div>
                        </li>
                    </ul>
                </div>
                <div class="item">
                    <ul class="timeline">
                        <div class="title-item">
                            <span class="name">软件：</span>
                            <span class="status">{{softWareFlag==true?'准备完成':'准备未完成'}}</span>
                        </div>
                        
                        <li v-for="(item,index) in softWareData" :key="index" class="timeline-item oneitem">
                            <div class="timeline-item__tail"></div>
                            <div class="timeline-item__node timeline-item__node--normal timeline-item__node--">
                                <img :src="item.success==0?img1:img2" alt="">
                            </div>
                            <div class="timeline-item__wrapper">
                                <div class="content-item">
                                    {{item.name}}
                                </div>
                            </div>
                        </li>
                    
                    </ul>
                </div>
            </div>
        </div>
        <div class="btn">
            <el-button :loading='loadingFlag' type="primary" @click="startClick">初始化完成</el-button>
        </div>
        <el-dialog
            :visible.sync="invcodeFlag"
            width="20%"
            class="success1"
            top='25vh'
         
            >
            <div class="right">
                <h3 class="title">
                    邀请其他用户加入该任务：
                </h3>
                <div class="desc">
                    复制任务邀请码，对方可在主页选择"加入任务"后输入邀请码加入
                </div>
                <div class="btn1">
                    <el-input placeholder="" disabled="" v-model="vcode">
                        <el-button @click="createVode" type="primary" slot="append" size="mini">复制</el-button>
                      
                    </el-input>
                </div>
                <div class="link">
                    <span @click="copyImg">点击查看“加入任务说明”，保存发给受邀联邦方</span>
                    
                </div>
            </div>
            <div ref="foo">
                <img style="display:none" src="@/assets/jointask.png" alt="">
            </div>
        </el-dialog>
        <img-dialog @handleClickinImg='handleClickinImg'  :imgUrlImg='imgUrlImg' :visible.sync='imgShowVisible'></img-dialog>
        <data-handle-dialog :id="id" @cancleCreateClick="cancleCreateClick" :visible.sync='changeVisible'></data-handle-dialog>
    </div>
   
</div>
    
</template>
<script>
import LineBox from './line'
import img1  from '@/assets/0.png'
import img2  from '@/assets/1.png'
import img3 from '@/assets/newtask_nor.png'
import img4 from '@/assets/liu.svg'
import {listCurrent, queryFlJobDataInfo, summaryCompare, updateData, taskInviteCode,update} from '@/api/task' 
import { getTenantId} from '@/utils/auth.js' 
import { hardWareStatus , softWareStatus ,taskSubmit} from  '@/api/task'
import  imgUrl  from  '@/assets/jointask.png'
import  imgDialog  from '@/components/imgDialog'
import taskHeader from '@/components/TaskHeader'
import dataHandleDialog from '@/components/DataHandleDialog'
export default {
    props:{
        id: String,
        registid: String,
        createByFlag: Boolean,
        taskname: String,
        suanfaName: String,
        dataName: String,
        fljob: Array,
        fileMetaId: String,
        algorithmId: String,
        suanfaNameCn: String,
        logUrl: String,
        jobType: Number,
        contentType: String 
    },
    components:{
        LineBox,
        imgDialog,
        taskHeader,
        dataHandleDialog
    },
    inject:['reload'],
    data() {
        return {
            showStart: true,
            img1: img1,
            img2: img2,
            img3: img3,
            img4: img4,
            imgUrlImg :imgUrl,
            active: "1",
            data:'',
            tableData: [
                
            ],
            tableData2:[
                
            ],
            slectData: [],
            compareFlag: false,
            titleText: '',
            dataId:'',
            descFlag: false,
            path: '',
            createParty: '',
             hardWareData:[
            ],
            softWareData: [],
            hardWareFlag: false,
            softWareFlag: false,
            dataname: '',
            partName: '我方',
            statusText: '尚未部署成功，请排查',
            btnText: '重新检测',
            dataText : '导入未完成',
            invcodeFlag: false,
            vcode: '',
            imgShowVisible: false,
            loadingFlag: false,
            changeVisible: false
            
        }
    },
    created() {
       
        // this.listCurrent()
        // this.queryFlJobDataInfo()
        let id = getTenantId()
        this.hardWareStatus(id)
        this.softWareStatus(id)
    },
    methods: {
        cancleCreateClick() {
            this.changeVisible = false
        },
        // 数据预处理点击事件
        editData() {
            this.changeVisible = true
        },
        // 邀请联邦方加入
        invcodeJoinClick() {
            
            this.taskInviteCode()
        },
        // 任务邀请码的生成
        async taskInviteCode() {
            
            let id = getTenantId()
            let params = {
                id: this.id
            }
            await taskInviteCode(params,id).then(res=>{
                if(res.data.success) {
                    this.invcodeFlag = true
                    this.vcode = res.data.result
                }
            })
            .catch(res=>{

            })
        },
         // 创建邀请码
        createVode() {
             this.$copyText(this.vcode).then(res=>{
                this.$message.success('已复制到粘贴板')
                this.invcodeFlag = false
            })
            .catch(res=>{
                this.$message.error('复制失败') 
            })
        },
        handleClickinImg(){
          
            this.imgShowVisible = false
        },
        // 复制图片
        copyImg() {
            this.invcodeFlag = false
            this.imgShowVisible = true
        },
        // 更新算法和数据
        // 更新任务和数据
        async update() {
            let id = getTenantId()
            let data = {
                compoments: JSON.stringify([
                    {
                        name: this.suanfaName,
                        id: this.algorithmId,
                        nameCn: this.suanfaNameCn
                    }
                ]) , //compoments:JSON.stringify([{name: this.form.type}])
                dataId: this.fileMetaId,  // 数据id
                id: this.id    // 任务id
            }
            await update(data,id).then(res=>{
                if(res.data.code == 200 ) {
                    this.$store.dispatch('app/toggleSideBar')
                    this.$message.success(res.data.message)
                    this.reload()
                }
                
            })
            .catch(err=>{

            })
        },
        //开始初始化
        startClick() {
            this.loadingFlag = true
            if(this.suanfaName!=''&&this.fileMetaId!='') {
                this.update()
            } else {
                this.$message.error('请选择数据与算法')
                this.loadingFlag = false
            } 
        },
        //  参与方
        noGuestClick(item) {
            // if(this.showStart) {
                this.dataname = item.name
                this.partName = item.partyName
                this.infoid = item.id
                // partyId 查询环境  recruitId查询数据
                this.hardWareStatus(item.partyId)
                this.softWareStatus(item.partyId)
                if(item.envStatus==1) {
                    //  环境  和 数据ok
                    this.statusText = '部署成功'
                    this.dataText = '导入完成'
                } else if(item.envStatus==4) {
                    
                    // 数据上传了
                    this.dataText = '导入完成'
                    this.statusText = '尚未部署成功，请排查'
                    this.btnText = '去催促'
                }  else {
                    // 数据没有上传
                    this.statusText = '尚未部署成功，请排查'
                    this.btnText = '去催促'
                    this.dataText = '导入未完成'
                }
            
            // }
        },
        guestClick(item) {
            // <!-- 1都ok，2数据不ok环境ok，3都不ok，4数据ok环境不ok -->
            // if(this.showStart) {
                this.partName = '我方'
                this.infoid = item.id
                this.dataname = item.name
                this.partyId = item.partyId
                this.hardWareStatus(item.partyId)
                this.softWareStatus(item.partyId)
                if(item.envStatus==1) {
                    //  环境  和 数据ok
                    this.statusText = '部署成功'
                    this.btnText = '重新检测'
                } else if(item.envStatus==4) {
                    // 数据上传了
                    this.dataText = '导入完成'
                    this.statusText = '尚未部署成功，请排查'
                    this.btnText = '重新检测'
                }  else {
                    // 数据没有上传
                    this.statusText = '尚未部署成功，请排查'
                    this.btnText = '重新检测'
                    this.dataText = '导入未完成'
                }
                
            
            // }
        },
        // 重新检测
        reloadClick() {
            if(this.btnText == '去催促') {
                this.noticeClick(this.infoid)
                
            } else {
                let id = getTenantId()
                this.hardWareStatus(id)
                this.softWareStatus(id)
            }
        },
        // 数据导入btn
        datauploadClick() {
            this.updateData()
        },
        // 保存数据
        async updateData() {
            let data = new URLSearchParams()
            data.append('dataId',this.dataId)
            data.append('registId',this.registid)
            // let params = {
            //     dataId: this.dataId,
            //     registId: this.registid
            // }
            await updateData(data).then(res=>{
                if(res.data.success) {
                    this.loadingFlag = false;
                    this.$message.success(res.data.message)
                    this.reload()
                }
            })
            .catch(res=>{
                
            })
        },
        // 发起方数据格式
        async queryFlJobDataInfo() {
            let params = {
                id: this.id
            }
            await queryFlJobDataInfo(params).then(res=>{
                this.tableData2 = []
                if(res.data.success) {
                    this.createParty = res.data.result.createParty
                    this.path = res.data.result.path
                    this.tableData = res.data.result.columns.split(",")
                    this.tableData.filter(item=>{
                        this.tableData2.push(JSON.parse(res.data.result.columnType)[item])
                    })
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
                pageNo:'1',
                pageSize:'10'

            }
            await listCurrent(params,id).then(res=>{
                if(res.data.success) {
                    this.slectData = res.data.result.records
                   
                }
            })
            .catch(res=>{

            })
        },
        // 选择数据 对比数据信息
        async slectDataItem(item) {
            this.dataId = item.id
            let params = {
        
                    self:{
                        createParty: this.createParty, 
                        path: this.path
                    },
                    // 发起方
                    target: {
                        createParty: item.createParty, 
                        path: item.path
                    }
            }
            this.summaryCompare(params)
        },
        // 对比信息
        async summaryCompare(params) {
            await summaryCompare(params).then(res=>{
               if(res.data.success) {
                   this.descFlag = true
                   if(res.data.result== null)  this.compareFlag = true
                   else{
                       this.compareFlag = false
                       this.titleText = JSON.stringify(res.data.result)
                   } 
                   
               }
            })
            .catch(res=>{
                
            })
        },
        // 硬件检测
        async hardWareStatus(id) {
            
            this.hardWareData = []
            await hardWareStatus(id).then(res=>{
                if(res.data.result.length==0) {
                    this.titleFlag = true 
                    this.hardWareFlag = true
                } else {
                    this.titleFlag = false
                    this.hardWareFlag = false
                }
                // if(res.data.result.indexOf('Ready')!=-1) {
                //     this.hardWareData.push(
                //         {
                //             name: '网络检测',
                //             success: 1
                //         },
                //         {
                //             name: '可用内存检测',
                //             success: 1
                //         },
                //         {
                //             name: '磁盘空间检测',
                //             success: 1
                //         },
                //         {
                //             name: '进程压力检测',
                //             success: 1
                //         }
                //     )
                // } else {
                //     this.hardWareData.push(
                //         {
                //             name: '网络检测',
                //             success: 0
                //         },
                //         {
                //             name: '可用内存检测',
                //             success: 0
                //         },
                //         {
                //             name: '磁盘空间检测',
                //             success: 0
                //         },
                //         {
                //             name: '进程压力检测',
                //             success: 0
                //         }
                //     )
                // }
                if(res.data.result.indexOf('NetworkUnavailable')!=-1) {
                    this.hardWareData.push(
                        {
                            name: '网络检测',
                            success: 1
                        }
                    )
                }else{
                    this.hardWareData.push(
                        {
                            name: '网络检测',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('MemoryPressure')!=-1) {
                    this.hardWareData.push(
                        {
                            name: '可用内存检测',
                            success: 1
                        }
                    )
                }else{
                    this.hardWareData.push(
                        {
                            name: '可用内存检测',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('DiskPressure')!=-1) {
                    this.hardWareData.push(
                        {
                            name: '磁盘空间检测',
                            success: 1
                        }
                    )
                }else{
                    this.hardWareData.push(
                        {
                            name: '磁盘空间检测',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('PIDPressure')!=-1) {
                    this.hardWareData.push(
                        {
                            name: '进程压力检测',
                            success: 1
                        }
                    )
                }else{
                    this.hardWareData.push(
                        {
                            name: '进程压力检测',
                            success: 0
                        }
                    )
                }
            })
            .catch(res=>{
                
            })
        },
        // 软件检测
        async softWareStatus(id) {
           
            this.softWareData = []
            await softWareStatus(id).then(res=>{
                if(res.data.result.length==0) {
                    this.titleFlag = true
                    this.softWareFlag = true
                } else {
                     this.titleFlag = false
                     this.softWareFlag = false
                }
                if(res.data.result.indexOf('python')!=-1) {
                    this.softWareData.push(
                        {
                            name: '联邦训练组件',
                            success: 1
                        }
                    )
                }else{
                    this.softWareData.push(
                        {
                            name: '联邦训练组件',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('board')!=-1) {
                    this.softWareData.push(
                        {
                            name: '训练可视化组件',
                            success: 1
                        }
                    )
                }else{
                    this.softWareData.push(
                        {
                            name: '训练可视化组件',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('rollsite')!=-1) {
                    this.softWareData.push(
                        {
                            name: '联邦通信组件',
                            success: 1
                        }
                    )
                }else{
                    this.softWareData.push(
                        {
                            name: '联邦通信组件',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('clustermanager')!=-1) {
                    this.softWareData.push(
                        {
                            name: '集群管理组件',
                            success: 1
                        }
                    )
                }else{
                    this.softWareData.push(
                        {
                            name: '集群管理组件',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('nodemanager')!=-1) {
                    this.softWareData.push(
                        {
                            name: '节点管理组件',
                            success: 1
                        }
                    )
                }else{
                    this.softWareData.push(
                        {
                            name: '节点管理组件',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('minio')!=-1) {
                    this.softWareData.push(
                        {
                            name: '存储组件',
                            success: 1
                        }
                    )
                }else{
                    this.softWareData.push(
                        {
                            name: '存储组件',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('mysql')!=-1) {
                    this.softWareData.push(
                        {
                            name: '数据库组件',
                            success: 1
                        }
                    )
                }else{
                    this.softWareData.push(
                        {
                            name: '数据库组件',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('fledge')!=-1) {
                    this.softWareData.push(
                        {
                            name: '边缘分析组件',
                            success: 1
                        }
                    )
                }else{
                    this.softWareData.push(
                        {
                            name: '边缘分析组件',
                            success: 0
                        }
                    )
                }
            })
            .catch(res=>{
                
            })
        },
    },
    watch: {
        dataName(newvalue,oldvalue) {
            this.dataname = newvalue
            // console.log(newvalue)
        }
    }
}
</script>
<style lang="scss" scoped>
.chosedata{
    display: flex;
    .el-select{
        width: 400px;
    }
    .desc{
        margin-left: 20px;
        overflow: auto;
        display: flex;
        align-items: center;
        span{
            margin-left: 10px;
        }
    }
}
 .btn{
    margin-top: 20px;
    text-align: right;
    ::v-deep .el-button--primary{
        padding: 12px 30px;
        border-radius: 10px;
        // width: 257px;
        // height: 67px;
        background: #e01622;
        border-radius: 4px;
    }
}
.data-box{
    // background-color: #f9f9f9;
   
    height: 100%;
    // padding: 40px;
    .content-box{
        border: 1px solid #ccc;
        height: 400px;
        padding: 30px 40px;
        .content-box-top{
            display:flex;
            .title{
                font-weight: 700;
            }
            .box-card{
                width: 104px;
                height: 104px;
                margin-top: 10px;
                .status{
                    background-color: green;
                    width: 16px;
                    height: 2px;
                }
                .error{
                    width: 16px;
                    height: 2px;
                    background-color: #F38383;
                }
                .name {
                    margin-top: 5px;
                    font-weight: 700;
                    font-style: 14px;
                }
                                
            }
            .left{
                
                margin-right: 50px;
            }
            .middle{
                flex: 1;
            }
            .right{
                
                margin-left: 50px;
                img{
                    width: 100%;
                }
            }
        }
        .img-box{
            text-align: center;
            display: flex;
            justify-content: center;
            align-items: center;
            .img-name{
                width: 100px;
                height: 80px;
                 display: flex;
                justify-content: center;
                align-items: center;
                // line-height: 80px;
                font-size: 12px;
                background-size: 100px 80px;
                cursor: pointer;
                // display: inline-block;
                background-image: url('../../../assets/tool_bg_model.svg');
            }
           
            .active{
                background-image: url('../../../assets/tool_btn_active_model.svg');
            }
        }
        
        .icon-box{
            text-align: center;
            ::v-deep .el-icon-bottom{
                font-size: 20px;
                color:blue;
            }
        }
        .quote {
            position: relative;
            width: 100%; 
            margin-top: 30px;
            height: 40px;
        }
        .quote::before, .quote::after, .quote ::before, .quote ::after {
            content: '';
            display: block;
            position: absolute;
            width: calc(50% - 20px);
            height: 20px;
            border-style: solid;
            border-color: blue;
            border-width: 0;
        }
        .quote ::before, .quote ::after {
            top: 0;
            border-bottom-width: 1px;
        }
        .quote::before, .quote::after {
            top: 20px;
            border-top-width: 1px;
        }
        .quote ::before {
            left: 0;
            border-bottom-left-radius: 20px;
        }
        .quote ::after {
            right: 0;
            border-bottom-right-radius: 20px;
        }
        .quote::before {
            left: 20px;
            border-top-right-radius: 20px;
        }
        .quote::after{
        right: 20px;
            border-top-left-radius: 20px
        }
    }
    .bottom-box{
        border: 1px solid #ccc;
        margin-top: 20px;
        // height: 400px;
        padding: 30px 40px;
        .title{
            font-size: 18px;
            font-weight: 700;
        }
        .itembox{
            display: flex;
            .item{
    
                width: 33%;
                .title-item{
                    margin-top: 20px;
                    margin-bottom: 20px;
                    display: flex;
                    align-items: center;
                    img{
                        margin-right: 15px;
                    }
                    span{
                        font-size: 16px;
                        font-weight: 700;
                    }
                }
            }
            .item:first-child{
                margin-right: 30px;
            }
            .choosedata {
                width: 33%;
                .title-item{
                    // margin-left: 16px;
                    margin-top: 20px;
                    margin-bottom: 20px;
                    display: flex;
                    align-items: center;
                    img{
                        margin-right: 15px;
                    }
                    span{
                        font-size: 16px;
                        font-weight: 700;
                    }
                }
                .img-box{
                    text-align: center;
                    display: inline-block;

                    .img-name{
                        width: 100px;
                        height: 80px;
                        // line-height: 80px;
                        font-size: 12px;
                        background-size: 100px 80px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        cursor: pointer;
                        // display: inline-block;
                        // background-image: url('../../../assets/tool_bg_model.svg');
                    }
                    .data-name{
                         width: 100px;
                         .el-icon-edit{
                            cursor: pointer;
                         }

                    }
                    .data-box {
                        background-image: url('../../../assets/tool_bg_sensor_dataset.svg');
                    }
                    .active-data-box {
                        background-image: url('../../../assets/tool_bg_image_active_dataset.svg');
                    }
                    .active{
                        background-image: url('../../../assets/tool_bg_image_active_dataset.svg');
                    }
                }
                
            }
        }
    }
    .btn{
        text-align: right;
        
        
        ::v-deep .el-button--primary{
            padding: 12px 50px;
            margin: 30px 0;
            font-size: 16px;
            background-color: #e01622;
        }
        ::v-deep .el-button--primary.is-disabled{
            background-color: #a0cfff;
        }
    }
    .success1{
        ::v-deep .el-dialog__header{
            padding: 0;
        }
        .el-button{
                margin: 0;
            }
        .right{
            
            .desc{
                font-weight: 600;
            }
            .btn1{
                margin-top: 20px;
                margin-bottom: 20px;
                ::v-deep .el-input-group__append, .el-input-group__prepend{
                    // background-color: #9898F1;
                     padding: 0;
                    color: #fff;
                    font-weight: 600;
                }
                ::v-deep .el-input__inner {
                    background-color: #e8edfa;
                }
            }
            .link{
                span {
                    text-decoration:underline;
                    color: #409EFF;
                    cursor: pointer;
                    font-weight: 600;
                }
            }
        }
    }
    .content{
        // display: flex;
        height: 100%;
        
        .title{
            font-weight: 700;
            margin: 30px 0 20px;
            .desc{
                font-weight: 400;
            }
        }
       .one{
           span{
               display: inline-block;
               padding: 10px;
               width: 100px;
               border: 1px solid #000;
           }
       }
       .two{
           span{
               display: inline-block;
               padding: 10px;
               width: 100px;
               border: 1px solid #000;
           }
       }
        
    }
   

}
.myTable {
    border-collapse: collapse;
    border-spacing: 0;
    margin: 0 auto;
    // width: 600px;
    text-align: center;
    .theader{
        background-color: #D2E4F5;
    }
    tbody {
        display: block;
        height: 100px;
        tr {
            display: table;
            width: 100%;
            table-layout: fixed;
            td{
                width: 110px;
                border: 1px solid #cad9ea;
                color: #666;
                height: 40px;
            }
        }
    }
}
.table-box{
    width: 870px;
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
.timeline{
    margin: 0;
    font-size: 14px;
    list-style: none;
    padding: 0;
    .timeline-item:last-child{
        .timeline-item__tail{
            position: absolute;
            left: 4px;
            height: 0%;
            border-left: 2px solid #E4E7ED 
        }
    }
     .timeline-item:first-child{
        .timeline-item__tail{
            position: absolute;
            left: 4px;
            height: 0%;
            border-left: 2px solid #E4E7ED 
        }
    }
    .timeline-item{
        position: relative;
        padding-bottom: 20px;
        .timeline-item__tail{
            position: absolute;
            left: 4px;
            height: 110%;
            top: 10px;
            border-left: 2px solid #E4E7ED 
        }
        .timeline-item__node--normal{
            left: -1px;
            width: 12px;
            height: 12px;
            top: 8px;
        }
        .timeline-item__node{
            position: absolute;
            display: flex;
            justify-content: center;
            align-items: center;
        }
        .timeline-item__wrapper{
            margin-left: 20px;
            .content-item{
                // border: 1px solid #D2E4F5;
                border-radius: 6px;
                margin-left: 16px;
                padding: 10px 20px;
                display: inline-block;
            }
        }
    }
}

</style>
