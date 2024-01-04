<template>
    <div class="box">
        <line-box :logUrl='logUrl' :active='active'></line-box>
        <div class="content-box">
            <div class="content-box-top">
                <div v-if="jobType==1" class='left'>
                    <div class="title">
                        发起方
                    </div>
                    <el-card 
                        shadow="never"
                        @click.native='guestClick(items)'
                        class="box-card"
                        v-for="(items,index) in fljob.slice(0,1)"
                        :key="index">
                        <div :class="[items.envStatus==1?'status':'error']"></div>
                        <div class='name'>
                            {{items.partyName}}
                        </div>
                    </el-card>
                </div>
                <div v-if="jobType==1" class='middle'>
                    <div class="title">联邦方</div>
                    <div class='lbbox' v-if="fljob.slice(1).length!=0">
                        <el-card  
                            style="margin-right:10px;"
                            class="box-card"
                            shadow="never"
                            v-for="(items,index) in fljob.slice(1)"
                            @click.native='noGuestClick(items)'
                            :key="index">
                    
                            <div :class="[items.envStatus==1?'status':'error']"></div>
                            <div class='name' >
                                {{items.partyName}}
                            </div>
                        
                            
                        </el-card>
                    </div>
                    
                    <el-card shadow="never" class="box-card" v-else>
                        <div>
                            
                            <div class='name' >
                                暂无参与方
                            </div>
                        </div>
                        
                    </el-card>
                </div>
                <div v-if="jobType==1">
                    <div v-if="showStart" @click="invcodeJoinClick" class='right'>
                        <div class="title">邀请联邦方</div>
                        <el-card  shadow="never" class="box-card">
                        <img :src="img3" alt="">
                        </el-card>
                    </div>
                </div>
                
            </div>
            <div v-if="jobType==1" class="quote">
                <div></div>
            </div>
            <div class='img-box'>
                <span  class="active img-name">
                </span>
                <div class="suanfa-box">
                    <span>
                        {{suanfaNameCn}}
                    </span>
                </div>
                
            </div>
            <!-- <div class='img-box'>
                <span class="active img-name">
                    
                </span>
                <div>
                    {{suanfaNameCn}}
                </div>
            </div> -->
            <div class="icon-box">
                <i class="el-icon-bottom"></i>
            </div>
            <div class='img-box'>
                <div  class=" img-name"></div>
                <div class="name-box">  
                    {{taskname}}模型
                </div>
            </div>
        </div>
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
                         <div class="data-name"> {{dataname}}</div>
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
        <div  v-if="showStart" class="btn">
            <el-button :loading="loading" @click="taskStartClick"  type="primary">开始训练模型</el-button>
        </div>
        <div v-if="!showStart">
                        <div class="notask-box">
                            <div class="btn">
                                <el-button @click="sendMegStart" type="primary">提醒发起方开始</el-button>
                            </div>
                        </div>
                    </div>  
        <div class="content">
            <!-- <div class="center">
                <el-card class="box-card">
                    <div class="title">
                        <span class="sub-title">当前环境检测:</span>
                        <span v-if="hardWareFlag!=true||softWareFlag!=true" class="red">尚未部署成功，请排查标红项</span>
                        <span class="btn">
                            <el-button @click="reloadClick" type="text">重新检测</el-button>
                        </span>
    
                        
                    </div>
                    <div class="itembox">
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
                    <div v-if="!showStart">
                        <div class="notask-box">
                            <div class="btn">
                                <el-button @click="sendMegStart" type="primary">提醒发起方开始</el-button>
                            </div>
                        </div>
                    </div>
                </el-card>
               
            </div> -->
            
            <!-- <div v-if="showStart" class="footer">
                <el-card class="box-card">
                    <div class="task-box" v-if="showStart">
                        <div class="title">
                            <span>联邦方准备情况:</span>
                        </div>
                        <div class="itemBox">
                            <div 
                                v-for="(item) in fljob"
                                :key="item.id"
                                class="item">
                                    
                                    <span class="point"  :class="[item.envStatus==1?'success':'error']"></span>
                                    <span class="sub-title">{{item.partyName}}</span>
                                    1都ok，2数据不ok环境ok，3都不ok，4数据ok环境不ok
                                    <div class="sub-item">
                                        <img :src="item.envStatus==1?img1:item.envStatus==2?img2:item.envStatus==3?img2:img1" alt="">
                                        <span> 数据</span>
                                    </div>
                                    <div class="sub-item">
                                        <img :src="item.envStatus==1?img1:item.envStatus==2?img1:item.envStatus==3?img2:img2" alt="">
                                        <span>环境</span>
                                    </div>
                                    <div v-if="item.role!='guest'&&item.envStatus!=1"  class="sub-item">
                                        <span @click="noticeClick(item.id)" class="hidetext">去催促></span>
                                    </div>
                            </div>
                        </div>
                       
                        
                    </div>
                </el-card>
            </div> -->
            <!-- <div  v-if="showStart" class="btn">
                <el-button :loading="loading" @click="taskStartClick"  type="primary">开始训练模型</el-button>
            </div> -->
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
                <div class="btn">
                    <el-input placeholder="" disabled="" v-model="vcode">
                        <el-button @click="createVode" type="primary" slot="append" size="mini">复制</el-button>
                        <!-- <el-button type="primary" slot="append">复制</el-button> -->
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
        <el-dialog
            title="开始模型训练"
            :visible.sync="taskStartFlag"
            :close-on-click-modal='false'
            width="30%"
            :before-close='beforeCloseClick'
            :show-close='true'>
            <div>
                已有
                <span>
                    {{reslutMesg}}
                </span>
                个联邦方准备好，是否开启新任务，并保留原任务，可等待全部联邦方准备好再次开始。
            </div>
            <div slot="footer" class="dialog-footer">
                <el-button class="cancle"  @click="deleteTaskClick">删除原任务</el-button>
                <el-button class="save" type="primary"  @click="saveTaskClick">保留原任务</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
 // 1就是数据和环境都ok，2是数据ok环境不ok，3数据和环境都不ok
import img1  from '@/assets/0.png'
import img2  from '@/assets/1.png'
import img  from '@/assets/avatar.png'
import img3 from '@/assets/newtask_nor.png'
import  imgDialog  from '@/components/imgDialog'
import LineBox from './line'
import {  getTenantId } from "@/utils/auth";
import { hardWareStatus , softWareStatus ,taskSubmit,taskInviteCode} from  '@/api/task'
import { sendJobNotic} from  '@/api/info'
import  imgUrl  from  '@/assets/jointask.png'
import { submitNotAllReady } from '@/api/index'
export default {
    props:{
        showStart: Boolean,
        fljob: Array,
        id: String,
        taskname: String,
        comSuanfaname: String,
        dataName: String,
        suanfaNameCn: String,
        logUrl: String,
        jobType: Number
    },
    components:{
        LineBox,
        imgDialog
    },
    inject:['reload'],
    data() {
        return {
            imgUrlImg :imgUrl,
            active: "2",
            img:img,
            img1:img1,
            img2: img2,
            img3: img3,
            partyName: '',
            hardWareData:[
            ],
            softWareData: [],
            titleFlag: false,
            hardWareFlag: true,
            softWareFlag: true,
            registIds:[],
            loading: false,
            partName: '我方',
            statusText: '尚未部署成功，请排查',
            btnText: '重新检测',
            infoid: '',
            partyId: '',
            dataname: '',
            invcodeFlag: false,
            vcode: '',
            imgShowVisible: false,
            dataText : '导入完成',
            taskStartFlag: false,
            reslutMesg: '',
            reserveFlag: false,
            
        }
    },
    created() {
        this.partyName = sessionStorage.getItem('showname')
      
        let id = getTenantId()
        this.hardWareStatus(id)
        this.softWareStatus(id)
        
    },
    methods: {
        // 删除原任务
        deleteTaskClick() {
            this.$confirm('此操作将删除原任务，并将无效联邦法移出任务, 是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    // 点击确定之后
                    // this.$message({
                    //     type: 'success',
                    //     message: '删除成功!'
                    // });
                    this.reserveFlag = false
                    this.submitNotAllReady()
                }).catch(() => {
                    this.taskStartFlag = false
                    // this.$message({
                    //     type: 'info',
                    //     message: '已取消删除'
                    // });
                    // this.reserveFlag = false         
            });
           
        },
        saveTaskClick() {
            this.$confirm('此操作将保留原任务，并启动新的任务副本进行任务训练, 是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    // 确定按钮
                    // this.$message({
                    //     type: 'success',
                    //     message: '保留成功!'
                    // });
                    this.reserveFlag = true
                    
                    this.submitNotAllReady()
                }).catch(() => {
                    this.taskStartFlag = false
                    // 取消按钮
                    // this.$message({
                    //     type: 'info',
                    //     message: '已取消删除'
                    // });     
            });
          
        },
        beforeCloseClick() {
            this.taskStartFlag = false
        },
        // 是否保留联邦任务
        async submitNotAllReady() {
            let data = new URLSearchParams()
            data.append('id',this.id)
            data.append('reserveFlag',this.reserveFlag)
            // let data = {
            //     id: this.id,
            //     reserveFlag: this.reserveFlag
            // }
            await submitNotAllReady(data,getTenantId()).then(res=>{
                 if(res.data.code == 200) {
                    this.taskStartFlag = false
                    this.$message.success('提交模型训练成功')
                    if(this.reserveFlag) {
                        this.$router.push({
                            name: 'MytaskDetail',
                            query: {
                                id: res.data.result
                            }
                        })
                        this.$emit('taskSubmitSuccess')
                    } else {
                        this.$emit('taskSubmitSuccess')
                    }
                    
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
        //  参与方
        noGuestClick(item) {
            if(this.showStart) {
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
            
            }
           
                
            
            // else if(item.envStatus==2) {
            //     // 数据没有上传  环境ok
            //     this.statusText = '尚未部署成功，请排查'
            //     this.btnText = '去催促'

            // } else if(item.envStatus==3) {
            //     // 数据没有上传  环境没有ok
            //     this.statusText = '尚未部署成功，请排查'
            //     this.btnText = '去催促'
            // } else {
            //     // 数据上传 环境不ok
            //     this.statusText = '尚未部署成功，请排查'
            //     this.btnText = '去催促'
            // }
            // console.log(item)
        },
        guestClick(item) {
            // <!-- 1都ok，2数据不ok环境ok，3都不ok，4数据ok环境不ok -->
            if(this.showStart) {
                this.partName = '我方'
                this.infoid = item.id
                this.dataname = item.name
                this.partyId = item.partyId
                this.hardWareStatus(item.partyId)
                this.softWareStatus(item.partyId)
                // if(item.envStatus==1) {
                //     //  环境  和 数据ok
                //     this.statusText = '部署成功'
                //     this.btnText = '重新检测'
                // }  else {
                //     this.statusText = '尚未部署成功，请排查'
                //     this.btnText = '去催促'
                // }
                if(item.envStatus==1) {
                    //  环境  和 数据ok
                    this.statusText = '部署成功'
                    this.btnText = '重新检测'
                    this.dataText = '导入完成'
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
                
            
            }
            
            // console.log(item.envStatus)
            // if(item.envStatus==1) {
            //     //  环境  和 数据ok
            //     this.statusText = '部署成功'
            //     console.log(1231321)

            // } else if(item.envStatus==2) {
            //     // 数据没有上传  环境ok
            //     this.statusText = '尚未部署成功，请排查'

            // } else if(item.envStatus==3) {
            //     // 数据没有上传  环境没有ok
            //     this.statusText = '尚未部署成功，请排查'
            // } else {
            //     // 数据上传 环境不ok
            //     this.statusText = '尚未部署成功，请排查'
            // }
            // console.log(item)
        },
        //去催促
        noticeClick(ID) {
            let data = new URLSearchParams()
            data.append('busType','regist')
            data.append('id',ID)
            data.append('templateCode','rwzbtx')
            this.sendJobNotic(data)
        },
        // 开始模型训练
        taskStartClick() {
            this.taskSubmit()
        },
        // 任务提交
        async taskSubmit() {
            this.loading = true
            this.fljob.forEach(item=>{
                this.registIds.push(item.id)
            })
            let data = new URLSearchParams()
            data.append('id',this.id)
            data.append('registIds',this.registIds.toString())
            await taskSubmit(data).then(res=>{
                if(res.data.code == 200) {
                    this.loading = false
                    if(res.data.message == '成功') {
                         this.$message.success('提交模型训练成功')
                        this.$emit('taskSubmitSuccess')
                    } else {
                        this.taskStartFlag = true 
                        this.reslutMesg = res.data.result
                    }
                   
                }
            })
            .catch(res=>{
                // console.log(9999999)
                this.loading = false
            })
        },
        // 任务提醒
        async sendJobNotic(data) {
           
            // let data = new URLSearchParams()
            // data.append('busType','recruit')
            // data.append('id',ID)
            // data.append('templateCode','rwkstx')
            //  registid，regist，rwzbtx
            let id = getTenantId()
            // let params = {
            //     busType: 'recruit',
            //     id: this.id,
            //     'templateCode': 'rwkstx'
            // }
            await sendJobNotic(data,id).then(res=>{
                if(res.data.success) {
                    this.$message.success('提醒成功')
                }
            })
            .catch(res=>{

            })
        },
        sendMegStart() {
            let data = new URLSearchParams()
            data.append('busType','recruit')
            data.append('id',this.id)
            data.append('templateCode','rwkstx')
            this.sendJobNotic(data)
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
       
        reloadClick() {
            if(this.btnText == '去催促') {
                this.noticeClick(this.infoid)
                
            } else {
                let id = getTenantId()
                this.hardWareStatus(id)
                this.softWareStatus(id)
            }
           
        }
       
    },
    watch: {
        dataName: {
            handler(newvalue,oldvalue) {
                this.dataname = newvalue
            },
            immediate: true
        },
        fljob: {
            handler(newvalue,oldvalue) {
                if(newvalue[0].envStatus==1) this.statusText = '部署成功'
                else  this.statusText = '尚未部署成功，请排查'
                
            },
            immediate: true,
            deep: true
        }
        
    }
}
</script>
<style lang="scss" scoped>
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
            }
        }
    }
}
.one{     
    background-color: #0B80EA;  
}
.box{
    // background-color: #f9f9f9;
    height: 100%;
    padding: 20px 0px;
    .content{
        // display: flex;
        height: 100%;
        overflow: hidden;
        .center{
            margin-bottom: 20px;
            // border: 1px solid #D2E4F5;
            ::v-deep .el-card__body{
                padding: 40px;
            }
            .title{
                font-size: 18px;
                font-weight: 700;
            }
            .itembox{
                    display: flex;
                    // margin-top: 40px;
                    .item{
                        // border: 1px solid #D2E4F5;
                        // flex: 2;
                        width: 50%;
                        // padding: 35px;
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
                    }
                    .item:first-child{
                        margin-right: 30px;
                    }
                }
        }
        
        .footer{
            // margin-right: 30px;
            // flex: 1;
            min-width: 430px;
             .box-card{
                ::v-deep .el-card__body{
                    padding: 0;
                }
                // width: 620px;
                padding: 30px;
                height: 100%;
                .itemBox{
                    display: flex;
                    justify-content: space-between;
                    flex-wrap: wrap;
                    .item{
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
                }
                .task-box{
                    .title{
                        display: flex;
                        align-items: center;
                        margin-bottom: 20px;
                    }
                   
                }
                .notask-box{
                    margin-top: 60px;
                    display: flex;
                    align-items: center;
                    flex-direction:column;
                    justify-content: center;
                    .text {
                        font-size: 16px;
                        color: #9a9a9a;
                        margin: 20px 0 30px;
                    }
                }
                .title{
                    font-weight: 700;
                    font-size: 16px;
                    height: 20px;
                    line-height: 20px;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                    img{
                        width: 16px;
                        height: 16px;
                        margin-right: 20px;
                        // vertical-align: middle;
                    }
                }
                .btn{
                    margin-top: 30px;
                }
                .item{
                    display: flex;
                    align-items: center;
                    margin-top: 10px;
                    .sub-item{
                        display: flex;
                        margin-right: 12px;
                        align-items: center;
                        img{
                            margin-right: 10px;
                        }
                        .hidetext{
                            cursor: pointer;
                            color: #0B80EA;
                            font-size: 14px;
                        }
                    }
                    .avatar{
                        width: 40px;
                        height: 40px;
                        border-radius: 20px;
                        vertical-align: middle;
                        margin-right: 16px;
                    }
                    .point{
                        display: inline-block;
                        width: 10px;
                        height: 10px;
                        margin-right: 10px;
                        border-radius: 50%;
                    }
                    .success{
                        background-color: #8EE3B1;
                    }
                    .error{
                        background-color: #F38383;
                    }
                    .sub-title{
                        font-size: 16px;
                        font-weight: 700;
                        margin-right: 16px;
                        
                    }
                }
            }
        }
        .right{
            flex: 2;
            width: 100%;
            .box-card{
                padding: 30px;
                height: 100%;
                ::v-deep .el-card__body{
                    padding: 0;
                }
                .title{
                    display: flex;
                    align-items: center;
                    img{
                        margin-right: 10px;
                    }
                }
                .sub-title{
                    margin-right: 10px;
                    font-weight: 700;
                }
                .red{
                    color: #ff4a4a;
                    margin-right: 40px;
                }
                
                .itembox{
                    display: flex;
                    margin-top: 40px;
                    .item{
                        border: 1px solid #D2E4F5;
                        flex: 1;
                        padding: 35px;
                        .title-item{
                            margin-left: 16px;
                            display: flex;
                            align-items: center;
                            img{
                                margin-right: 15px;
                            }
                            span{
                                font-size: 20px;
                                font-weight: 700;
                            }
                        }
                    }
                    .item:first-child{
                        margin-right: 30px;
                    }
                }
            }
        }
    }
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
                overflow: auto;
                .lbbox{
                    display:flex;
                
                }
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
            // display: flex;
            // justify-content: center;
            // align-items: center;
            //  text-align: center;
            // display: flex;
            // justify-content: center;
            // align-items: center;
            .name-box{
                     display: -webkit-box;
                    -webkit-box-orient: vertical;
                    -webkit-line-clamp: 1;
                    overflow: hidden;
                    font-weight: 700;
            }
            .suanfa-box{
                // display: flex;
                cursor: pointer;
                 display: -webkit-box;
                    -webkit-box-orient: vertical;
                    -webkit-line-clamp: 1;
                    overflow: hidden;
                font-weight: 700;
            }
            .img-name{
                width: 100px;
                height: 80px;
                
                // display: flex;
                display: inline-block;
                justify-content: center;
                align-items: center;
                // line-height: 80px;
                font-size: 12px;
                background-size: 100px 80px;
                cursor: pointer;
                // display: inline-block;
                background-image: url('../../../assets/tool_bg_model.svg');
                span{
                    display: -webkit-box;
                    -webkit-box-orient: vertical;
                    -webkit-line-clamp: 2;
                    overflow: hidden;
                }
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
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 12px;
                        background-size: 100px 80px;
                        cursor: pointer;
                        // display: inline-block;
                        // background-image: url('../../../assets/tool_bg_model.svg');
                        span{
                            display: -webkit-box;
                            -webkit-box-orient: vertical;
                            -webkit-line-clamp: 2;
                            overflow: hidden;
                        }
                    }
                    .data-name{
                         width: 100px;
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

    .success1{
        ::v-deep .el-dialog__header{
            padding: 0;
        }
        .el-button{
                // width: 50%;
                margin: 0;
                
            }
        .right{
            
            .desc{
                font-weight: 600;
            }
            .btn{
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
    .dialog-footer {
        text-align: center;
        .save {
            background: #9898F1;
        }
        .cancle {

        }
    }
}
</style>
