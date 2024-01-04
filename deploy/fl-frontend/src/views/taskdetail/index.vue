<template>
    <div class="detail-container">
        <bread-crumb :breadArray='breadArray'></bread-crumb>
        <div class="header">
            <div class="img">
                <img src="@/assets/taskicon.png" alt="">
            </div>
            <div class="content-title">
                <span class="name-title" >{{taskObj.name}}</span>
                <el-tag v-if="jobType==1" :class="role==undefined?'hide':''" >{{role=='guest'?'发起方':role==undefined?'':'参与方'}}</el-tag>
                <el-tag v-if="jobType==1" :class="[RecruitStatus=='0'?'green':RecruitStatus=='3'?'finish':RecruitStatus=='4'?'red':'blue','status']">
                    {{RecruitStatus=='0'?'招募中':RecruitStatus=='3'?'训练完成':RecruitStatus=='4'?'训练失败':'训练中'}}
                </el-tag>
                <el-tag >{{taskObj.permission==0?'公开':taskObj.permission==1?'邀请加入':'私密'}}</el-tag>
            </div>
            <div  v-if="createByFlag&&RecruitStatus=='0'"  class="invcodejoin">
                <el-button v-if="jobType==1" @click="invcodeJoinClick" type="text">邀请联邦方加入</el-button>
            </div>
            <div v-if="(RecruitStatus==3||RecruitStatus==4)&& showDeteBtn" class="invcodejoin">
                <el-button  @click='deleteTaskClick' type="text">删除任务</el-button>
            </div>
            <div v-if="RecruitStatus==0" class="btnbox"> 
               
                <el-popover
                    placement="bottom"
                    width="180"
                    v-model="taskVisible"
                    v-if="taskObj.permission!=0||!nojoinFlag"
                    >
                    <div  v-if="jointext=='加入任务'" v class="item" style="text-align:center;">
                        <span @click="join">申请加入</span>
                        <div class="line">
                            <span></span>

                        </div>  
                        <span @click="joinCodeHandleClick">邀请码加入</span>
                    </div>
                    <div v-if="jointext!='加入任务'&&jointext!='申请中'" class="item" style="text-align:center;">
                        <el-popconfirm
                            
                            :title="role=='guest'?'确定解散任务吗？':'确定退出任务吗？'"
                            :hide-icon='false'
                            placement="top-end"
                            @confirm='exitClick'
                            >
                            <span slot="reference" @click="exitTaskClick">{{role=='guest'?"解散任务":'退出任务'}}</span>
                        </el-popconfirm>
                        
                    </div>
                    <el-button type="primary" slot="reference">{{jointext}}</el-button>
                </el-popover>
                <div v-else class="btn">
                    <el-button slot="reference"  @click="joinClick"  type="primary">加入任务</el-button>
                </div>
                
                <!-- <el-button  @click="joinClick" slot="reference">加入任务</el-button> -->
            </div>
        </div>
        <div class="content-box">
            <choose :flType="flType" :algFlag='algFlag' :partInfo='partInfo'  @reastChoose='reastChoose' v-if="dataFalg" :id="id" :fileMeta='fileMeta' :algorithm='algorithm'  @choosesf='choosesf'></choose>
            <choose :flType="flType" :algFlag='algFlag' :partInfo='partInfo' v-if="guestdataFalg" @reastChoose='reastChoose' :id="id" :fileMeta='fileMeta' :algorithm='algorithm'  @choosesf='choosesf'></choose>
            <div class="detail-box">
                {{taskObj.jobStatus}}
                <span  class="role" :class="[taskObj.jobType=='1'?'role':'hide']">{{taskObj.jobType=='1'?'联邦':''}}</span>
                <div class="left">
                    
                    <div class="detail-footer">
                        <gueset-choose :contentType="contentType" :jobType='jobType' :logUrl='logUrl'  :suanfaNameCn='suanfaNameCn' :algorithmId='algorithmId' v-if='guestdataFalg' :fileMetaId='fileMetaId' :suanfaName='comSuanfaname' :dataName="dataName" :taskname='taskObj.name' :fljob='fljob' :registid='registid' :id="id"></gueset-choose>
                        <no-join  :showDataName='showDataName' :createBy='createBy' :dataId='dataId' :id='id' :RecruitStatus='RecruitStatus' :PartyNums='PartyNums' :rowsNum='rowsNum' :taskObj='taskObj' v-if="nojoinFlag" :fljob='fljob'></no-join>
                        <!-- 环境未部署  -->
                        <env-not-use v-if="envStatusFlag"></env-not-use>
                        <!-- 数据准备阶段 -->
                        <data-read :logUrl='logUrl'  :suanfaNameCn='suanfaNameCn' :dataFalg='dataFalg' :completeFlag='completeFlag'  :compareFlag='compareFlag' :fileMetaId='fileMetaId' :dataName='dataName' :comSuanfaname='comSuanfaname' :taskname='taskObj.name' v-if="dataFalg" :registid='registid' :fljob='fljob' :createByFlag='createByFlag'  :id="id"></data-read>
                        <!-- 模型训练阶段  未开始训练 -->

                        <model-train-start :jobType='jobType' :logUrl='logUrl'  :suanfaNameCn='suanfaNameCn' :dataName='dataName' :comSuanfaname='comSuanfaname' :taskname='taskObj.name' @taskSubmitSuccess='taskSubmitSuccess' v-if='dataStartFlag' :fljob='fljob' :id="id" :showStart='showStart'></model-train-start>

                        <!-- 训练中状态 -->
                        <model-training :logUrl='logUrl'  @checkoutRes='checkoutRes'  :registid='registid' v-if='trainingFlag' ></model-training>
                        <train-error :logUrl='logUrl' v-if="traniningError"></train-error>
                        <!-- 训练结束状态 -->
                        <training-finanth :modelId='modelId' :logUrl='logUrl'  :registid='registid' :id='id' :createByFlag='createByFlag'  v-if="trainingFinanthFlag" ></training-finanth>
                    </div>
                    
                    
                </div>
            
                <div class="right"  v-if="!nojoinFlag">
                    <div class="right-top">
                        <div class="titile">
                            基础信息
                        </div>
                        <!-- <div class="utils">
                            <span class="sub-title">工具</span>
                            <div class="sub-box">
                                <el-tag effect="plain">工具</el-tag>
                                <el-tag effect="plain">线性分析</el-tag>
                            </div>
                        </div> -->
                        <div class="base-info">
                            <div class="nums">
                                <div class="sub-title">数据量</div>
                                <div class="number">{{rowsNum}}</div>
                            </div>
                            <div class="nums">
                                <div class="sub-title">联邦方</div>
                                <div class="number">{{PartyNums}}</div>
                            </div>
                            <div class="nums">
                                <div class="sub-title">训练时长</div>
                                <div class="number">{{fElapsed}}</div>
                            </div>
                        </div>
                        <div class="task-desc">
                            <div class="sub-title">任务描述</div>
                            <div class="task-text">{{taskObj.content}}</div>
                        </div>
                        <div class="progress">
                            <div class="sub-title">进度</div>
                            <el-progress :text-inside="true" :stroke-width="26" :percentage="fProgress"></el-progress>
                        </div>
                        <div class="time-info">
                            <div class="nums">
                                <div class="sub-title">建立时间</div>
                                <span>{{taskObj.createTime}}</span>
                            </div>
                            <div class="nums">
                                <div class="sub-title1">数据集链接</div>
                                <div v-for="(items,index) in fljob" :key="index">
                                    <el-link  @click="gotoDataDetail(items.canShowDetail,items.dataId,items.partyId)" :underline="false" type="primary">{{items.name}}</el-link>
                                </div>
                                <!-- <el-link  @click="gotoDataDetail" :underline="false" type="primary">{{showDataName}}</el-link> -->
                                
                                <!-- <span></span> -->
                            </div>
                        </div>
                        <div class="party-info">
                            <div v-if="jobType==1" class="nums">
                                <div  class="sub-title">发起方</div>
                                <div class="user-img"
                                    v-for="(items,index) in fljob.slice(0,1)"
                                    :key="index">
                                    <el-avatar 
                                        class="avatar " 
                                        :src="items==null?img:items.partyAvatar==null?img:items.partyAvatar.indexOf('http')!=-1?items.partyAvatar:`${$url}${items.partyAvatar}`"
                                    >
                                    </el-avatar>
                                    <div class="party-name">{{items.partyName}}</div>
                                </div>
                            </div>
                            <div class="nums">
                                <div v-if="jobType==1" class="sub-title1">其他参与方</div>
                                <div class="img">
                                    <el-tooltip effect="light"  placement="top">
                                        <div slot="content">
                                            {{showOtherName}}
                                        </div>
                                        <div> 
                                            <el-avatar 
                                                v-for="(items,index) in fljob.slice(1,4)"
                                                :key="index"
                                                class="avatar " 
                                                :src="items==null?img:items.partyAvatar==null?img:items.partyAvatar.indexOf('http')!=-1?items.partyAvatar:`${$url}${items.partyAvatar}`"
                                            >
                                            </el-avatar>
                                          
                                        </div>
                                        
                                    </el-tooltip>
                                    <span v-if="fljob.slice(1).length>3" class="dian">
                                                ...
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <el-divider></el-divider>
                    <div class="right-footer">
                        <div class="right-title">
                            最近更新
                        </div>
                        <div class="carousel">
                            <div class="carousel-left">
                                <div class="item" v-for="(items,index) in fljob" :key="index">
                                    <span class="point"></span>
                                    <span class="name-hos">{{items.partyName}}</span>
                                    <!-- 1都ok，2数据不ok环境ok，3都不ok，4数据ok环境不ok -->
                                    <span class="text">
                                        {{items.envStatus==1||items.envStatus==4?'数据已上传':'数据未上传'}}
                                    </span>
                                    <span class="num">
                                        {{items.createTime.substring(5,10)}}
                                    </span>
                                </div>
                                
                            </div>
                        </div>
                    </div>
                </div>
                <div class="right"  v-else>
                    <div class="right-footer">
                        <div class="right-title">
                            最近更新
                        </div>
                        <div class="carousel">
                            <div class="carousel-left">
                                <div class="item" v-for="(items,index) in fljob" :key="index">
                                    <span class="point"></span>
                                    <span class="name-hos">{{items.partyName}}</span>
                                    <!-- 1都ok，2数据不ok环境ok，3都不ok，4数据ok环境不ok -->
                                    <span class="text">
                                        {{items.envStatus==1||items.envStatus==4?'数据已上传':'数据未上传'}}
                                    </span>
                                    <span class="num">
                                        {{items.createTime.substring(5,10)}}
                                    </span>
                                </div>
                                
                            </div>
                        </div>
                    </div>
                </div>
            
            </div>
        </div>
       
        
       
        <el-dialog
            :visible.sync="codedialogVisible"
            width="20%"
            top='25vh'
            class="join"
            :show-close='false'
            >
            <div  class="one" v-if="showStepone">
                <div class="diatitter">以{{partyname}}身份加入吗？</div>
                <div  slot="footer" class="dialog-footer">
                    <el-button  type="text" class="confirm" @click="stepOne">确 认</el-button>
                    <el-button  type="text" class="cancle" @click="codedialogVisible = false">取 消</el-button>
                </div>
            </div>
            <div class="two" v-else>
                <div class="diatitter">
                    留言给{{taskObj.name}}任务
                    <span>(可选)</span>
                </div>
                <div class="liuyan">
                    <el-input v-model="message" :placeholder="`留言给${taskObj.name}任务`"></el-input>
                </div>
                <div  slot="footer" class="dialog-footer">
                    <el-button  type="text" class="confirm" @click="stepTwo">确 认</el-button>
                    <el-button type="text" class="cancle" @click="twocancle">取 消</el-button>
                </div>
            </div>
            
        </el-dialog>
        <!-- <el-dialog
            :visible.sync="dialogVisible"
            width="20%"
            top='25vh'
            class="joincode"
            :show-close='false'
            >
            <div class="diatitter">请输入邀请码</div>
            <div>
               <el-input v-model="code" placeholder="邀请码"></el-input>
            </div>
            <div  slot="footer" class="dialog-footer">
                <el-button  type="text" class="confirm" @click="joinCodeClick">确 认</el-button>
                <el-button type="text" class="cancle" @click="dialogVisible = false">取 消</el-button>
            </div>
        </el-dialog> -->
        <el-dialog
            :visible.sync="sendMsg"
            width="20%"
            top='25vh'
            class="codeSend"
            :show-close='false'
            >
            <div class="diatitter">已向"{{taskObj.name}}"任务发发出加入请求，任务负责人会尽快回复~</div>
            <div  slot="footer" class="dialog-footer">
                <el-button  type="text" class="confirm" @click="sendKnowClick">知道了</el-button>
            </div>
        </el-dialog>
        <el-dialog
            :visible.sync="successTask"
            width="20%"
            class="success"
            top='25vh'
            :show-close='false'
            >
            <div class="diatitter">成功加入任务'{{taskObj.name}}'</div>
            <!-- <div>
               <p class="text">但您的环境尚未部署成功，需完成后才能进行任务，这可能需 要一定时间完成，为不影响您后续进行任务，建议您尽快进行部署~</p>
            </div> -->
            <div  slot="footer" class="dialog-footer">
                <!-- <el-button  type="text" class="confirm" @click="successTask = false">现在部署</el-button> -->
                <el-button type="text" class="cancle" @click="knowClick">知道了</el-button>
            </div>
        </el-dialog>
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
        <join-code @updateInfoVisible='updateInfoVisible'  @handleClickJoinCode='handleClickJoinCode'  :visible.sync='joinDialogVisible'></join-code>
        <img-dialog @handleClickinImg='handleClickinImg'  :imgUrlImg='imgUrlImg' :visible.sync='imgShowVisible'></img-dialog>
    </div>
</template>
<script>
import BreadCrumb from '@/components/breadcrumb.vue'
import ModelTrainStart  from './components/modelTrainStart'
import ModelTraining from './components/modelTraining.vue'
import TrainingFinanth from './components/trainingFinanth.vue'
import TrainError from './components/error.vue'
import EnvNotUse from './components/envnotuse'
import DataRead from './components/dataread'
import NoJoin from './components/nojoin'
import Choose from './components/choose.vue'
import GuesetChoose from './components/guestchoose.vue'
import {queryByIdTask , queryFlJobRegistByMainId  , registJob, exitRegist, taskInviteCode} from '@/api/task'
import { getTenantId , getPartyInfo} from "@/utils/auth";
import { queryDataId,queryAlgById} from '@/api/index'
import { sendJobNotic} from  '@/api/info'
import img  from '@/assets/avatar.png'
import  imgDialog  from '@/components/imgDialog'
import  imgUrl  from  '@/assets/jointask.png'
import {jobInfoByRecruitId, queryFileMetaAndAlgorithm, deleteTask} from '@/api/task'
import joinCode from '@/components/joinCode'
import {formatTime} from '@/utils/validate.js'
export default {
    components:{
        BreadCrumb,
        ModelTraining,
        TrainingFinanth,
        EnvNotUse,
        ModelTrainStart,
        DataRead,
        NoJoin,
        imgDialog,
        Choose,
        GuesetChoose,
        joinCode,
        TrainError
    },
    inject:['reload'],
    data() {
        return {
            imgShowVisible: false,
            imgUrlImg :imgUrl,
            img: img,
            jointext: '加入任务',
            breadArray: [
                {
                    name: '我的任务',
                    url: '/mytask'
                },
                {
                    name: '任务详情'
                }
            ],
            dialogVisible:false,
            codedialogVisible: false,
            code: '',
            showStepone: true,
            successTask: false,
            sendMsg: false,
            id: '',
            taskObj: {},
            partyname:'',
            message:"",
            rowsNum: 0,
            PartyNums:0,
            envStatus: 0,
            showStart: false,
            RecruitStatus: 0,
            fljob: [],
            registid: '',
            trainingFlag: false,
            createByFlag: false,
            trainingFinanthFlag: false,
            envStatusFlag: false,
            dataFalg: false,
            dataStartFlag: false,
            showname: '',
            invcodeFlag: false,
            vcode: '',
            nojoinFlag: false,
            touristFlag: false,
            joinvisible: false,
            taskVisible: false,
            role: '',
            fProgress: 0,
            fElapsed: 0,
            suanfaName: '',
            dataName: '',
            algorithmId: '',
            fileMetaId: '',
            algorithm: [],
            fileMeta: [],
            guestdataFalg: false,
            partInfo: '',
            comSuanfaname:'',
            compareFlag: false,
            algFlag: false,
            joinDialogVisible: false,
            dataId: '',
            showDataName: '',
            completeFlag: false,
            suanfaNameCn: '',
            traniningError:  false,
            showOtherName: '',
            showDeteBtn: false,
            flJobRegistByMainId: {},
            myFlJobRegistByMainId: {},
            logUrl: '',
            jobType: 0,
            modelId: '',
            flType: '',
            contentType: ''
            
        }
    },
    created() {
        if(this.showname=='') {
            this.showname = sessionStorage.getItem('showname')
        }
        if(this.$route.query.from =='home') {
            this.showDeteBtn = false
           this.breadArray = [
                {
                    name: '任务池',
                    url: '/task'
                },
                {
                   name: '任务详情'
                }
            ]
        } else {
            this.showDeteBtn = true
            this.breadArray = [
                {
                    name: '我的任务',
                    url: '/mytask'
                },
                {
                    name: '任务详情'
                }
            ]
        }
        this.id = this.$route.query.id
        this.role = this.$route.query.role
       
        if(this.$route.query.role) {
            
        }
        if(this.$route.query.algid!=undefined&&this.$route.query.algid!='') {
           
            this.algorithmId = this.$route.query.algid
          
            this.algFlag = true
            this.queryAlgById()
        }
        
        
        this.queryByIdTask()
        // this.jobInfoByRecruitId()
        this.partInfo = JSON.parse(getPartyInfo())
        this.partyname = sessionStorage.getItem('showname')
        this.touristFlag = sessionStorage.getItem('tourist')
       
       
    },
   
    methods: {
        // 算法查询
        async queryAlgById() {
            let id = getTenantId()
            let params = {
                id: this.algorithmId
            }
            await queryAlgById(params,id).then(res=>{
                if(res.data.code==200) {
                    // this.algObject = res.data.result
                    this.suanfaNameCn= res.data.result.nameCn
                    this.comSuanfaname = res.data.result.name
              
                }
            })
        },
        // 训练中任务查询
        checkoutRes () {
            this.queryByIdTask()
            // this.queryFlJobRegistByMainId()
        },
        // 删除训练任务
        deleteTaskClick() {
            this.$confirm('确定删除吗?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.deleteTask()
            });
        },
        async deleteTask() {
            let params = {
                id:  this.id
            }
            await deleteTask(params).then(res=>{
                if(res.data.code == 200) {
                    this.$message.success('删除训练任务成功')
                    this.$router.go(-1)
                }
            })
            .catch(err=>{
                this.$message.error(err.message)
            })
        },
        gotoDataDetail(flag,id,partyId) {
            
            if(flag == true) {
                this.$router.push({
                    name: 'DataDetail',
                    query: { 
                        partyId: partyId,
                        id: id,
                        from: 'mytask',
                        flag: 'true'
                    }
                })
            } else {
                this.$router.push({
                    name: 'DataDetail',
                    query: { 
                        partyId: partyId,
                        id: id,
                        from: 'mytask',
                        flag: 'false'
                    }
                })
            }
            
        },
        async queryDataId(id) {
            let params = {
                id: id
            }
            await queryDataId(params).then(res=>{
                if(res.data.code==200) {
                    this.showDataName = res.data.result.name 
                }
                
            })
            .catch(res=>{
               
            })
        },
        // 邀请码加入任务
        joinCodeHandleClick() {
            this.joinDialogVisible = true
        },
        updateInfoVisible(val){
            this.joinDialogVisible = val
        },
        handleClickJoinCode() {
            this.joinDialogVisible = false
        },
        // 重新查询
        reastChoose(id,name,compareFlag,type) {
            this.contentType = type
            this.fileMetaId = id
            this.dataName = name
            this.compareFlag = compareFlag
            this.queryFileMetaAndAlgorithm()
        },
        // 查询数据和算法
        async queryFileMetaAndAlgorithm() {
            let id = getTenantId()
            let params = {
                algorithmId: this.algorithmId,
                fileMetaId: this.fileMetaId,
            }
            await queryFileMetaAndAlgorithm(params,id).then(res=>{
                if(res.data.code == 200) {
                    this.fileMeta = res.data.result.fileMeta
                    this.algorithm = res.data.result.algorithm
                }
                
            })
            .catch(err=>{

            })
        },
        // 选择算法
        choosedata (item){
            this.dataName = item
        },
        choosesf (name,id,nameCn) {
            this.comSuanfaname = name
            this.suanfaNameCn = nameCn
            this.algorithmId = id
            this.queryFileMetaAndAlgorithm()
        },
        // 查询任务执行信息
        async jobInfoByRecruitId() {
            let id = getTenantId()
            let data = new URLSearchParams()
            data.append('recruitId',this.id)
            await jobInfoByRecruitId(data,id).then(res=>{
                if(res.data.success) {
                    
                    if(res.data.code == 200) {
                        this.fProgress = res.data.result.fProgress
                       
                        this.fElapsed  = formatTime(res.data.result.fElapsed) 
                    }
                    
                }
            })
            .catch(res=>{

            })
        },
        // 退出任务
        exitTaskClick() {
            this.taskVisible = false
        },
        handleClickinImg(){
          
            this.imgShowVisible = false
        },
        // 复制图片
        copyImg() {
            this.invcodeFlag = false
            this.imgShowVisible = true
        },
        // 模型训练开始
        taskSubmitSuccess() {
            this.reload()
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
         // 申请任务提醒
        async sendJobNotic(data) {
            let id = getTenantId()
            await sendJobNotic(data,id).then(res=>{
                if(res.data.success) {
                    this.$message.success('提醒成功')
                    this.registJob()
                }
            })
            .catch(res=>{

            })
        },
        // 公开加入任务
        joinClick() {
            if(sessionStorage.getItem('tourist')=='true') {
                this.$message.error('请绑定联邦方')
                return
            } else {
                if(this.RecruitStatus == 0) {
                    this.registJob()
                } else {
                    this.$message.error('任务已开启，无法加入任务')
                }
                
                
            }
            
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
        // 邀请联邦方加入
        invcodeJoinClick() {
            this.taskInviteCode()
            
        },
        //  加入任务
        async registJob() {
            let data = new URLSearchParams()
            data.append('recruitId',this.id)
            data.append('messsage',this.message)
            // let data = {
            //     recruitId: this.id,
            //     messsage: this.message

            // }
            let id = getTenantId()
            await registJob(data,id).then(res=>{
                if(res.data.success) {
                    // 申请中
                    if(res.data.result.registStatus == 0){
                        this.codedialogVisible = false
                        this.sendMsg = true
                        this.jointext ='申请中'
                        this.message = ''
                    } 
                        // 已经通过
                    else if (res.data.result.registStatus == 1){
                        this.jointext ='已加入，修改信息' 
                        this.codedialogVisible = false
                        this.successTask = true
                        this.message = ''
                    }    
                    
                    
                    
                }
            })
            .catch(res=>{
                
            })
        },
        // 获取任务名称 任务状态
        async queryByIdTask() {
            let params = {
                id: this.id 
            }
            let id = getTenantId()
            await queryByIdTask(params,id).then(res=>{
                this.flType = res.data.result.flType
              
                this.jobType = res.data.result.jobType
                let compoments = JSON.parse(res.data.result.compoments)
                if(compoments!= null) {
                    this.comSuanfaname = compoments[0].name
                    this.suanfaNameCn = compoments[0].nameCn
                }
                if(res.data.code == 200) {
                    
                    this.taskObj = JSON.parse(JSON.stringify(res.data.result)) 
                    this.logUrl = this.taskObj.logUrl
                    this.modelId = this.taskObj.modelId
                    //recruitStatus字段是状态 0招募中，1任务提交成功，2任务提交失败，3任务训练成功，4任务训练失败，5任务取消，6任务正在运行中，7任务保存成功，8任务等待调度，9任务解散
                    this.RecruitStatus = res.data.result.recruitStatus
                    
                    this.createBy = res.data.result.partyId
                    this.completeFlag = res.data.result.completeFlag
                    if (this.RecruitStatus== 9) {
                            // 任务解散
                            this.$message.error('任务已解散')
                            this.$router.push({
                                name: 'newhome'      
                            })
                    }  else {
                        // if(this.trainingFlag) {
                        //     return 
                        // } else {
                        //     this.queryFlJobRegistByMainId()
                        // }
                        this.queryFlJobRegistByMainId()
                      
                    }
                        
                    
                    
                   
                }
            })
            .catch(err=>{
                if(err.message == '未找到对应数据') {
                    this.$router.go(-1)
                }
               
            })
        },
        // 获取所有联邦方情况，个数
        async queryFlJobRegistByMainId() {
            let params = {
                id: this.id 
            }
            let tenant_id = getTenantId()
            await queryFlJobRegistByMainId(params,tenant_id).then(res=>{
                if(res.data.code==200) {
                    // if(res.data.result.flJobRegistList[0].envStatus==1||res.data.result.flJobRegistList[0].envStatus==4){
                    //     // this.dataName = res.data.result.flJobRegistList[0].name
                    //     // this.dataId = res.data.result.flJobRegistList[0].dataId
                    //     // this.queryDataId(this.dataId)
                        
                    // }
                    this.flJobRegistByMainId = JSON.parse(JSON.stringify(res.data.result.flJobRegistByMainId)) 
                    if(JSON.stringify(res.data.result.jobInfoByRecruitId )== '{}') {

                        this.jobInfoByRecruitId  = {}
                        this.fProgress = 0
                        this.fElapsed  = formatTime(0) 
                    } else {
                        this.jobInfoByRecruitId = JSON.parse(JSON.stringify(res.data.result.jobInfoByRecruitId)) 
                        this.fProgress =  this.jobInfoByRecruitId.fProgress   
                        this.fElapsed  = formatTime( this.jobInfoByRecruitId.fElapsed) 
                    }
                  
                    
                    if(res.data.result.myFlJobRegistByMainId == null) {
                        this.myFlJobRegistByMainId  = null
                    } else {
                        this.myFlJobRegistByMainId = JSON.parse(JSON.stringify(res.data.result.myFlJobRegistByMainId))
                    }
                    this.PartyNums = this.flJobRegistByMainId.partyNums
                    this.rowsNum = this.flJobRegistByMainId.rowNums
                    this.fljob = this.flJobRegistByMainId.flJobRegistList
                    let showNameObj = JSON.parse(JSON.stringify(this.fljob.slice(1))) 
                    let array = []
                    this.showOtherName = ''
                    showNameObj.forEach((item)=>{
                        array.push(item.partyName).toString()
                    }) 
                    this.showOtherName = array.toString()
                    if(this.myFlJobRegistByMainId == null) {
                        // 说明当前任务没有我的任务信息，也就是没有加入该任务
                        this.nojoinFlag = true
                        if (this.RecruitStatus== 9) {
                            // 任务解散
                            this.$message.error('任务已解散')
                            this.$router.push({
                                name: 'newhome'      
                            })
                        } 
                    } else {
                        if(this.myFlJobRegistByMainId.role=='guest') {
                            this.role = 'guest'
                            // 发起方
                            this.createByFlag = true
                        }else {
                            // 参与方
                            this.role = 'host'
                            this.createByFlag = false
                        }
                        if(this.myFlJobRegistByMainId.envStatus==1||this.myFlJobRegistByMainId.envStatus==4){
                            this.dataName = this.myFlJobRegistByMainId.name
                            this.dataId = this.myFlJobRegistByMainId.dataId
                            this.showDataName = this.myFlJobRegistByMainId.name 
                            // this.queryDataId(this.dataId)
                        }
                        
                        if(this.myFlJobRegistByMainId.registStatus==0) {
                            
                            this.jointext ='申请中'
                            this.nojoinFlag = true
                            if (this.RecruitStatus== 9) {
                                // 任务解散
                                this.$message.error('任务已解散')
                                this.$router.push({
                                    name: 'newhome'      
                                })
                            } 
                            return
                        } else if (this.myFlJobRegistByMainId.registStatus==1) {
                            
                            this.jointext ='已加入，修改信息'
                            // 说明加入该任务
                            this.registid = this.myFlJobRegistByMainId.id
                            //   RecruitStatus为4，模型训练失败
                            //   RecruitStatus为3，模型训练成功
                            //   RecruitStatus为2，任务提交失败
                            //   RecruitStatus为1，模型训练中
                            // RecruitStatus为0招募中时，再判断EnvStatus，1等待启动训练（目前TODO提醒任务发起方开始训练），2数据准备中（目前TODO数据上传），3环境部署（目前TODO环境部署）
                            this.envStatus = this.myFlJobRegistByMainId.envStatus
                            // if(this.RecruitStatus== 1) {
                            //     // 模型训练中，发起方 参与方 同一个页面
                            //     this.trainingFlag = true
                            // } else if(this.RecruitStatus== 0) {
                            //     if(this.createBy==this.myFlJobRegistByMainId.partyId) {
                            //         // 发起方
                            //         if(this.envStatus==1) {
                            //             // 1等待启动训练  发起方可以开启训练
                            //             this.showStart = true
                            //             this.dataStartFlag = true
                            //         }
                            //     } else {
                            //         // 参与发
                            //         if(this.envStatus==3) {
                            //             // 3环境部署
                            //             this.envStatusFlag = true
                            //         }
                            //         if(this.envStatus==1) {
                            //             // 1等待启动训练
                            //             // 参与方提醒用户开始训练
                            //             this.showStart = false
                            //             this.dataStartFlag = true
                            //         }
                            //         //1等待启动训练（目前TODO提醒任务发起方开始训练），2数据准备中（目前TODO数据上传），3环境部署（目前TODO环境部署）
                                    
                                    
                            //         // 2 环境部署成功，等待上传数据
                            //         if(this.envStatus==2) {
                            //             this.dataFalg = true
                            //             // 2数据准备中
                            //         }          
                            //     }
                            // } else {
                            //     // 训练结束
                            //     this.trainingFinanthFlag = true
                            //     this.active = "3"
                                
                            // }
                            //recruitStatus字段是状态 0招募中，1任务提交成功，2任务提交失败，3任务训练成功，4任务训练失败，5任务取消，6任务正在运行中，7任务保存成功，8任务等待调度，9任务解散
                            if(this.RecruitStatus== 0) {
                                this.queryFileMetaAndAlgorithm()
                                if(this.createBy==this.myFlJobRegistByMainId.partyId) {
                                    this.partInfo = 'guest'
                                    // 发起方
                                    // envStatus  环境变量   1 数据准备成功，等待任务开始，2 环境部署成功，等待上传数据，3环境未部署
                                    if(this.envStatus==1) {
                                        // 1等待启动训练  发起方可以开启训练
                                        this.showStart = true
                                        this.dataStartFlag = true
                                    } else {
                                        // 数据没有上传
                                        this.guestdataFalg = true
                                        this.$store.dispatch('app/closeSideBar', { withoutAnimation: false })
                                    }
                                } else {
                                    this.partInfo = 'rose'
                                    // 参与发
                                    if(this.envStatus==3) {
                                        // 3环境部署
                                        this.envStatusFlag = true
                                    }
                                    if(this.envStatus==1) {
                                        // 1等待启动训练
                                        // 参与方提醒用户开始训练
                                        this.showStart = false
                                        this.dataStartFlag = true
                                    }
                                    //1等待启动训练（目前TODO提醒任务发起方开始训练），2数据准备中（目前TODO数据上传），3环境部署（目前TODO环境部署）
                                    
                                    
                                    // 2 环境部署成功，等待上传数据
                                    if(this.envStatus==2) {
                                        this.dataFalg = true
                                        this.$store.dispatch('app/closeSideBar', { withoutAnimation: false })
                                        // 2数据准备中
                                    }          
                                }
                            } else if(this.RecruitStatus== 3) {
                                // 训练结束
                                this.trainingFlag = false
                                this.trainingFinanthFlag = true
                                
                                this.active = "3"
                            } else if(this.RecruitStatus== 4) {
                                
                                // 任务训练失败
                                this.trainingFlag = false
                                this.traniningError = true
                            } else if(this.RecruitStatus== 6||this.RecruitStatus==7||this.RecruitStatus==1||this.RecruitStatus==8) {
                                // 模型训练中，发起方 参与方 同一个页面
                                this.trainingFlag = true
                                this.refreshData = null

                            } else if (this.RecruitStatus== 9) {
                                // 任务解散
                            
                                this.$message.error('任务已解散')
                                this.$router.push({
                                    name: 'newhome'      
                                })
                            } 
                        } else if (this.myFlJobRegistByMainId.registStatus==2){
                            
                            this.jointext ='加入任务'
                            this.nojoinFlag = true
                            return
                        }else if (this.myFlJobRegistByMainId.registStatus==3){
                            
                            // 退出任务
                            this.nojoinFlag = true
                            return
                        }  
                        
                    }
                }
                
                  
            })
        },
        // 退出任务
        async exitRegist() {
            let params = new URLSearchParams()
            params.append('registId',this.registid)
            let tenant_id = getTenantId()
            await exitRegist(params,tenant_id).then(res=>{
                if(res.data.success) {
                    this.$message.success(res.data.message)
                    if(this.createByFlag = true) {
                        // 发起方退出任务
                        this.$router.push({
                            name: 'newhome'
                        })
                    }
                }
            })
            .catch(res=>{
                
            })
        },
        // 邀请码的确认
        joinCodeClick() {
            
            this.dialogVisible = false
            this.sendMsg = true
        },
        // 申请加入
        join() {
            if(sessionStorage.getItem('tourist')=='true') {
                this.$message.error('请绑定联邦方')
                return
            } else {
                if(this.RecruitStatus == 0) {
                    this.codedialogVisible = true
                } else {
                    this.$message.error('任务已开启，无法加入任务')
                }
              
            }
            
        },
        joincode() {
             if(sessionStorage.getItem('tourist')=='true') {
                this.$message.error('请绑定联邦方')
                return
            } else {
                this.dialogVisible = true
            }
            
            
        },
        // 第一步的确认
        stepOne() {
            this.showStepone = !this.showStepone
        },
        // 第二步的确认
        stepTwo() {
            // let data = new URLSearchParams()
            // data.append('busType','regist')
            // data.append('id',this.id)
            // data.append('templateCode','rwkstx')
            // this.sendJobNotic(data)
            this.$message.success('提醒成功')
            this.registJob()
            
        },
        // 第二步的取消 
        twocancle() {
            this.showStepone = true;
            this.message = ''
            this.codedialogVisible = false
        },
        // 申请知道了 
        sendKnowClick() {
            this.sendMsg = false;
            this.reload()
        },
        exitClick() {
            if(sessionStorage.getItem('tourist')=='true') {
                this.$message.error('请绑定联邦方')
                return
            } else {
                if(this.trainingFlag) {
                    // 任务已经开始不能退出
                    if(this.role == 'guest') {
                        this.$message.error('任务正在训练中，不能解散任务')
                    } else {
                        this.$message.error('任务正在训练中，不能退出任务')
                    }
                } else if(this.trainingFinanthFlag) {
                    // 任务已经开始不能退出
                    if(this.role == 'guest') {
                        this.$message.error('任务训练已结束，不能解散任务')
                    } else {
                        this.$message.error('任务训练已结束，不能退出任务')
                    }
                } else {
                    this.exitRegist()
                }
                
            }
        },
        // 成功知道了
        knowClick() {
            this.showStepone = true;
            this.successTask = false;
            this.reload()
        }
    }
}
</script>
<style lang="scss" scoped>
.detail-container{
    .header{
        display: flex;
        align-items: center;
        margin-bottom: 40px;
        .content-title{
            flex: 2;
            font-weight: 700;
            font-size: 40px;
            margin-left: 20px;
            display: flex;
            align-items: center;
            .name-title {
                
                    display: -webkit-box;
                    -webkit-box-orient: vertical;
                    -webkit-line-clamp: 1;
                    overflow: hidden;
                
            }
            ::v-deep .el-tag{
                margin-left: 20px;
            }
            .hide{
                display: none;
            }
            .green{
                color: #16CC28;
            }
            .blue{
                color: #0B80EA;
            }
            .finish{
                color: #05A8DB;
            }
            .red{
                color:red;
            }
            .status {
                
                // font-size: 16px;
                font-family: Microsoft YaHei;
                font-weight: 700;
                // color: #16CC28;
                margin: 0 10px;
            }
        }
        .invcodejoin{
            margin-right: 20px;
            // flex: 1;
        }
        .btnbox{
            // width: 200px;
            // flex: 1;
            text-align: right;
            .el-button{
                width: 200px;
                padding: 12px 30px;
                background-color: #0b80ea;
                color: #fff;
                border-radius: 10px;
            }
        }
    }
    .content-box{
        display: flex;
    }
    .detail-box{
        display: flex;
        overflow: hidden;
        padding: 50px 60px;
        margin-left: 30px;
        background-color: #fff; 
        position: relative;
        width: 100%;
        .role{
            position: absolute;
            left: -40px;
            top: 15px;
            transform:rotate(-45deg);
            width: 150px;
            text-align: center;
            background-color: #f3f8fd;
            height: 35px;
            line-height: 35px;
            font-size: 18px;
            font-weight: 700;
            color: #0B80EA;
            // margin-right: 10px;
            // display: inline-block;
        }
        .hide{
            display: none;
        }
        .left{
            flex: 2;
            overflow: hidden;
            border-right: 1px solid #AEAEAE;
            padding-right: 50px;
            .title{
                font-size: 23px;
                font-weight: bold;
                color: #3A4755;
                margin-bottom: 40px;
            }
        }
        .right{
            flex: 1;
            flex-shrink: 0;
            margin-left: 50px;
            .number{
                text-align: center;
                font-weight: 700;
                font-size: 17px;
            }
             .right-top {
                 
                .titile {
                    font-size: 23px;
                    font-weight: bold;
                    color: #3A4755;
                    margin-bottom: 40px;
                }

                .utils {
                    margin-bottom: 40px;
                    .sub-title {
                        font-size: 17px;
                        color: #6b6868;
                    }

                    .sub-box {
                        .el-tag {
                            margin-left: 10px;
                            margin-top: 10px;
                        }
                        .el-tag:first-child{
                            margin-left: 0;
                        }
                    }
                }

                .base-info {
                    margin-bottom: 40px;
                    display: flex;
                    justify-content: space-between;
                    .nums {

                        .sub-title {
                            font-size: 17px;
                            color: #6b6868;
                            margin-bottom: 10px;
                        }
                        
                    }
                    
                }
                .task-desc{
                    margin-bottom: 40px;
                    // display: flex;
                    justify-content: space-between;
                    .sub-title {
                        font-size: 17px;
                        color: #6b6868;
                        margin-bottom: 10px;
                    }
                    .task-text {
                        font-weight: 700;
                        font-size: 17px;
                    }
                }
                .progress {
                    margin-bottom: 40px;
                    .sub-title {
                        font-size: 17px;
                        color: #6b6868;
                        margin-bottom: 10px;
                    }
                    ::v-deep .el-progress-bar__outer{
                        border-radius: 0;
                        
                    }
                    ::v-deep .el-progress-bar__inner{
                        border-radius: 0;
                        background-color:#9898F1;
                    }
                }

                .time-info {
                    margin-bottom: 40px;
                    display: flex;
                    justify-content: space-between;
                    .nums {
                        
                        .sub-title {
                            font-size: 17px;
                            color: #6b6868;
                            margin-bottom: 10px;
                        }
                        .sub-title1 {
                            font-size: 17px;
                            color: #6b6868;
                            margin-bottom: 10px;
                            text-align: right;
                        }

                        span {
                            font-size: 17px;
                            color: #3A4755;
                        }
                    }
                }

                .party-info {
                    display: flex;
                    justify-content: space-between;
                    margin-bottom: 40px;
                    .nums {
                        .sub-title {
                            font-size: 17px;
                            
                            color: #6b6868;
                        }
                        .sub-title1 {
                            font-size: 17px;
                            text-align:right;
                            color: #6b6868;
                        }
                        .user-img{
                            margin-top: 20px;
                            display: flex;
                            align-items: center;
                            .party-name{
                                margin-left: 10px;
                                font-size: 17px;
                                font-weight: 700;
                            }
                        }
                        .img{
                            display: flex;
                            margin-top: 20px;
                            // overflow: hidden;/*超出部分隐藏*/
                            white-space: nowrap;/*不换行*/
                            text-overflow:ellipsis;/*超出部分文字以...显示*/   
                            // flex-direction: row-reverse;
                            .avatar{
                                // box-shadow: 0px 0 0 5px #fff;
                                position: relative;
                                margin-right: -10px;
                                color: #fff;
                                border: 2px solid #fff;
                            } 
                           
    
                        }
                        .dian{
                            margin-left: 10px;
                            display: flex;
                            align-items: center;
                            font-size: 20px;
                        }
                    }
                }
            }

            .right-footer {
                .carousel{
                    margin-top: 10px;
                    height: 85px;
                    background: #F6F7FB;
                    border-radius: 11px;
                    padding: 20px;
                    overflow: hidden;
                    display: flex;
                    .carousel-left{
                        overflow: auto;
                        flex: 2;
                        .item{
                            display: flex;
                            align-items: center;
                            font-size: 14px;
                            margin-top: 10px;
                            margin-bottom: 15px;
                            .point{
                                width: 4px;
                                height: 4px;
                                
                                background-color: #9898F1;
                                display: inline-block;
                                margin-right: 10px;
                            }
                            .text{
                                margin-right: 5px;
                                color: #808096;
                                overflow: hidden;
                                text-overflow: ellipsis;
                                white-space: nowrap;
                            }
                            .name-hos{
                                color: #9898F1;
                                margin-right: 5px;
                                overflow: hidden;
                                text-overflow: ellipsis;
                                white-space: nowrap;
                            }
                            .num{
                                color: #808096;
                                overflow: hidden;
                                text-overflow: ellipsis;
                                white-space: nowrap;
                            }
                        }
                    }
                }
            }
        }
    }
}
.popver-box{
    // padding: 0 ;
    .avatarbox{
        display: flex;
        align-items: center;
        height: 60px;
        border-bottom: 2px solid #f9f9f9;
        div{
            margin-left: 10px;
        }
    }
    .avatarbox:last-child{
        border-bottom: none;
    }

}
.join{
    ::v-deep .el-dialog__body{
        padding: 0;
    }
    .one{
        ::v-deep .el-dialog__body{
            padding: 0;
        }
        ::v-deep .dialog-footer{
            padding: 0;
            border-top: 1px solid #dcdcdc;
        }
        .diatitter{
            margin-bottom: 20px;
            text-align: center;
            font-weight: 700;
            font-size: 18px;
            margin: 30px 0 50px;
        }
    }
    .two{
        ::v-deep .dialog-footer{
            padding: 0;
            border-top: 1px solid #dcdcdc;
        } 
        .diatitter{
            margin-bottom: 20px;
            margin-left: 20px;
            font-weight: 700;
            font-size: 18px;
        }
        .liuyan{
           padding: 0 20px;
           margin-bottom: 30px;
        }
    }
    .confirm{
        border-right: 1px solid #dcdcdc;
    }
    .cancle{
        color: #000000;
    }
    
    .dialog-footer{
        text-align: center;
        
    }
    .el-button{
        width: 50%;
        margin: 0;
        padding: 20px;
    }
}
.codeSend{
    ::v-deep .el-dialog__header{
        padding: 0;
    }
    ::v-deep .el-dialog__footer{
        padding: 0;
        border-top: 1px solid #dcdcdc;
    }
    .confirm{
        border-right: 1px solid #dcdcdc;
    }
    .cancle{
        color: #484848;
    }
    .diatitter{
        margin-bottom: 20px;
        color: #484848;
        font-size: 18px;
        font-weight: 700;
    }
    .dialog-footer{
        text-align: center;
        
    }
    .el-button{
        width: 100%;
        margin: 0;
        padding: 20px;
    }
}
.success{
    ::v-deep .el-dialog__header{
        padding: 0;
    }
    ::v-deep .el-dialog__footer{
        padding: 0;
        border-top: 1px solid #dcdcdc;
    }
    .confirm{
        border-right: 1px solid #dcdcdc;
    }
    .cancle{
        color: #484848;
    }
    .diatitter{
        margin-bottom: 20px;
        color: #484848;
        font-size: 18px;
        font-weight: 700;
    }
    .dialog-footer{
        text-align: center;
        
    }
    .el-button{
        width: 50%;
        margin: 0;
        padding: 20px;
    }
    .text{
        line-height: 1.5;
    }
}
.joincode{
    ::v-deep .el-dialog__header{
        padding: 0;
    }
    ::v-deep .el-dialog__footer{
        padding: 0;
        border-top: 1px solid #dcdcdc;
    }
    .confirm{
        border-right: 1px solid #dcdcdc;
    }
    .cancle{
        color: #000000;
    }
    .diatitter{
        margin-bottom: 20px;
        color: #484848;
        font-size: 18px;
        font-weight: 700;
    }
    .dialog-footer{
        text-align: center;
        
    }
    .el-button{
        width: 50%;
        margin: 0;
        padding: 20px;
    }
}
.item{
        text-align: center;
        cursor: pointer;
        .line{
            span{
                display: inline-block;
                width: 140px;
                margin: 15px 0;
                height: 1px;
                background-color: #DCDFE6;
            }
           
            
        }
    }
.detail-box{
//   height: 700px;
    height: 100%;
    // height: calc(100vh - 300px);
    // height: 75vh;
    
    .detail-header{
      display: flex;
      align-items: center;
       .img{
            width: 90px;
            height: 90px;
            margin-right:30px;
            img{
                width: 90px;
                height: 90px;
            }
        }
        .content{
            flex: 1;
            .content-title{
                display: flex;
                color: #484848;
                font-size: 20px;
                font-weight: 700;
            }
            .content-footer{
                display: flex;
                align-items:center;
                margin-top: 14px;
                .imgs{
                    // width: 150px;
                    display: flex;
                    margin-right: 30px;
                    .avatar{
                        // box-shadow: 0px 0 0 5px #fff;
                        position: relative;
                        margin-right: -10px;
                        color: #fff;
                        border: 2px solid #fff;
                    } 
                }
                .point-dian{
                    font-weight: 700;
                    // margin-left: -15px;
                    color: #0B80EA;
                }
                .status-box {
                    // margin-left: 40px;
                    .point {
                        width: 10px;
                        display: inline-block;
                        height: 10px;
                        background: #16CC28;
                        opacity: 1;
                        border-radius: 40px;
                    }
                    .status {
                        
                        font-size: 16px;
                        font-family: Microsoft YaHei;
                        font-weight: 400;
                        color: #16CC28;
                        margin: 0 10px;
                    }
                    .bgg{
                            width: 10px;
                            display: inline-block;
                            height: 10px;
                            opacity: 1;
                            border-radius: 40px;
                            background: #16CC28; 
                        }
                        .bgb{
                            width: 10px;
                            display: inline-block;
                            height: 10px;
                            opacity: 1;
                            border-radius: 40px;
                            background: #0B80EA; 
                        }
                        .bgf{
                            width: 10px;
                            display: inline-block;
                            height: 10px;
                            opacity: 1;
                            border-radius: 40px;
                            background: #05A8DB; 
                        }
                        .green{
                            color: #16CC28;
                        }
                        .blue{
                            color: #0B80EA;
                        }
                        .finish{
                            color: #05A8DB;
                        }
                        .status {
                           
                            font-size: 16px;
                            font-family: Microsoft YaHei;
                            font-weight: 400;
                            // color: #16CC28;
                            margin: 0 10px;
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
                    }
                }
            }
        }
       
        
    }
    .content{
        line-height: 2;
    }
   
    
}
.btnbox{
    // width: 200px;
    .el-button{
        width: 200px;
        padding: 12px 30px;
        background-color: #0b80ea;
        color: #fff;
        border-radius: 10px;
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
        background: #9898F1;
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
</style>
