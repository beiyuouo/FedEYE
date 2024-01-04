<template>
    <div class="detail-container">
        <bread-crumb :breadArray='breadArray'></bread-crumb>
        <div class="header">
            <div class="img">
                <img src="@/assets/taskicon.png" alt="">
            </div>
            <div class="content-title">
                <span >{{ferenceObj.name}}</span>
                <el-tag>{{inferenceRole}}</el-tag>
            </div>
            <div class="invcodejoin">
                <el-button v-if="!creating&&showBtn" @click='deleteTlTaskClick' type="text">删除任务</el-button>
            </div>
            
        </div>
        <div class="content-box">
            <div class="detail-box">
                <div class="left">
                    <div v-if="!creating" class="detail-footer">
                        <datachoose  :contentType='contentType' @taskSuccess='taskSuccess' v-if="startFlag&&!directionFlag" :taskId='id'  :modelName='flModelInfoObj.modelName' :ferenceObj='ferenceObj'></datachoose>
                        <finish :startIng='startIng' :type='type' :contentType='contentType' v-if="finishFlag&&!directionFlag" :flInferenceResultVo='flInferenceResultVo' :ferenceObj='ferenceObj' :modelName='flModelInfoObj.modelName' ></finish>
                        <direction-choose @taskSuccess='taskSuccess' :flType='flType' :role='role' v-if="directionFlag" :taskId='id'  :modelName='flModelInfoObj.modelName' :ferenceObj='ferenceObj'></direction-choose>
                    </div>
                    <div style="text-align:center;" v-if="creating">
                        <img  class="img" src="@/assets/modeltraining.png" alt="">
                        <div  class="text">
                            任务创建中,请稍后查看
                        </div>
                        <div v-loading="loading">
                            
                        </div>
                        
                    </div>
                </div>
            
                <div class="right"  v-if="!nojoinFlag">
                    <div class="right-top">
                        <div class="titile">
                            基础信息
                        </div>
                        <div class="base-info">
                            <div class="nums">
                                <div class="sub-title">使用次数</div>
                                <div class="number">{{usedNums==''?0:usedNums}}</div>
                            </div>
                            <div class="nums">
                                <div class="sub-title">使用方</div>
                                <div class="number">{{partyNums==''?0:partyNums}}</div>
                            </div>
                            <div class="nums">
                                <div class="sub-title">最近更新</div>
                                <div class="number">{{flModelInfoObj.updateTime}}</div>
                            </div>
                        </div>
                       <div class="progress">
                            <div class="sub-title">适用情况</div>
                            <div class="number">{{application}}</div>
                        </div> 
                        <div class="progress">
                            <div class="sub-title">使用说明</div>
                            <div class="number">{{explain}}</div>
                        </div> 
                        <div class="progress">
                            <div class="sub-title">模型性能</div>
                            <div class="number">{{flModelInfoObj.metric}}</div>
                        </div> 
                        <div class="progress">
                            <div class="sub-title">模型介绍</div>
                            <div class="number">{{introduce}}</div>
                        </div> 
                    </div>
                    <el-divider></el-divider>
                    <div class="right-footer">
                        <div class="right-title">
                            最近更新
                        </div>
                        <div class="carousel">
                            <div class="carousel-left">
                                <div class="item" v-for="(items,index) in recentData.records" :key="index">
                                    <div class="item-left">
                                        <span class="point"></span>
                                        <span class="name-hos">{{JSON.parse(items.content).partyName}}的{{items.createBy}}</span>
                                        <div class="text">在任务{{JSON.parse(items.content).jobName}}中使用</div>
                                    </div>
                                    <div class="item-right">
                                        <span class="num">
                                            {{items.createTime.substring(5,10)}}
                                        </span>
                                    </div>
                                </div>
                                <!-- <div class="item">
                                    <div class="item-left">
                                        <span class="point"></span>
                                        <span class="name-hos">北京三院的李四</span>
                                        <div class="text">在任务xxxx推理任务中使用</div>
                                    </div>
                                    <div class="item-right">
                                        <span class="num">
                                            
                                        </span>
                                    </div>
                                     <div class="">
                                        暂无数据
                                    </div>
                                </div> -->
                                
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
                                <div class="item" v-for="(items,index) in recentData.records" :key="index">
                                    <div class="item-left">
                                        <span class="point"></span>
                                        <span class="name-hos">{{JSON.parse(items.content).partyName}}的{{items.createBy}}</span>
                                        <div class="text">在任务{{JSON.parse(items.content).missionName}}中使用</div>
                                    </div>
                                    <div class="item-right">
                                        <span class="num">
                                            {{items.createTime.substring(5,10)}}
                                        </span>
                                    </div>
                                </div>
                                <!-- <div class="item">
                                    <div class="item-left">
                                        <span class="point"></span>
                                        <span class="name-hos">北京三院的李四</span>
                                        <div class="text">在任务xxxx推理任务中使用</div>
                                    </div>
                                    <div class="item-right">
                                        <span class="num">
                                            {{items.createTime.substring(5,10)}}
                                        </span>
                                    </div>
                                     <div class="">
                                        暂无数据
                                    </div>
                                </div> -->
                                
                            </div>
                        </div>
                    </div>
                </div>
            
            </div>
        </div>
    </div>
</template>
<script>
import BreadCrumb from '@/components/breadcrumb.vue'
import { getTenantId } from "@/utils/auth";
import img  from '@/assets/avatar.png'
import  imgUrl  from  '@/assets/jointask.png'
import datachoose from './components/datachoose.vue'
import finish from './components/finish.vue'
import directionChoose from './components/directionChoose.vue'
import { ferenceQueryById, modelDetail, modelOpLog } from '@/api/index'
import { deleteTlTask } from '@/api/task'
export default {
    components:{
        BreadCrumb,
        datachoose,
        finish,
        directionChoose
    },
    inject:['reload'],
    data() {
        return {
            breadArray: [
                {
                    name: '我的任务',
                    url: '/mytask'
                },
                {
                    name: '推理任务详情'
                }
            ],
            id: '', 
            genId: '',
            modalId: '',
            ferenceObj: {},
            flModelInfoObj: {},
            partyNameList: [],
            rowNums: '',
            explain: '',
            introduce: '',
            application: '',
            nojoinFlag: false,
            partyNums: '',
            usedNums: '',
            usedUserList: [],
            startFlag: false,
            startIng: false,
            finishFlag: false,
            flInferenceResultVo: {},
            recentData: [],
            contentType: '',
            type: '',
            creating: false,
            loading: true,
            refreshData: null,
            flType: '',
            directionFlag: false,
            showBtn: false,
            role: ''
            
        }
    },
    created() {
       
        this.id = this.$route.query.id
        this.role = this.$route.query.inferenceRole
        this.modalId = this.$route.query.modalId
        this.genId  = getTenantId()
        this.modelDetail()
        this.modelOpLog()
       
        clearInterval(this.refreshData)
        this.refreshData = null
        // this.refreshData()
        this.refreshData =  setInterval(()=>{
            this.ferenceQueryById()
        },10000)
    },
    beforeDestroy() {
        clearInterval(this.refreshData)
        this.refreshData = null
    },
    computed: {
        inferenceRole() {
            if(this.$route.query.inferenceRole == 'GUEST') {
                this.showBtn = true
                return '发起方'
            } else if(this.$route.query.inferenceRole == 'HOST') {
                    this.showBtn = false
                   return '参与方'
            } else {
                this.showBtn = true
                this.role ='GUEST'
                return '发起方'
            }
        }
    },
    methods: {
        // 删除推理任务
        deleteTlTaskClick() {
            this.$confirm('确定删除吗?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.deleteTlTask()
            });
        },
        async deleteTlTask() {
            let params = {
                id:  this.id
            }
            await deleteTlTask(params).then(res=>{
                if(res.data.code == 200) {
                    this.$message.success('删除推理任务成功')
                    this.$router.go(-1)
                }
            })
            .catch(err=>{
                this.$message.error(err.message)
            })
        },
        // 模型的最近使用
        async modelOpLog() {
            let id = getTenantId()
            let data = new URLSearchParams()
            data.append('id',this.modalId)
            await modelOpLog(data,id).then(res=>{
                if(res.data.code == 200 ) {
                    this.recentData = res.data.result
                }
            })
            .catch(err=>{

            })
        },
         // 模型详情接口
        async modelDetail() {
            let params = {
                id : this.modalId
            }
            await modelDetail(params,this.genId).then(res=>{
                if(res.data.code == 200) {
                    this.ferenceQueryById()
                    this.flModelInfoObj = res.data.result.flModelInfo
                    this.flType = this.flModelInfoObj.flType
                    this.partyNameList = res.data.result.partyNameList
                    this.rowNums = res.data.result.rowNums
                    this.contentType = res.data.result.flModelInfo.contentType
                    if(res.data.result.partyNums) {
                         this.partyNums = res.data.result.partyNums
                    }
                    if(res.data.result.usedNums) {
                          this.usedNums = res.data.result.usedNums
                    }
                    if(res.data.result.usedUserList) {
                          this.usedUserList = res.data.result.usedUserList
                    }
                    
               
                }
            })
        },
        // 推理任务详情查询
        async ferenceQueryById() {
            let params = {
                id: this.id
            }
            await ferenceQueryById(params, this.genId).then(res=>{
                if(res.data.code == 200) {
                    this.ferenceObj = res.data.result.flJobInference
                    
                    this.flInferenceResultVo = res.data.result.flInferenceResultVo
                    this.type = res.data.result.type
                    const FL_TYPE = {
                        // 纵向推理
                        direction: '2',
                        // 横向推理
                        transverse: '1'
                    }
                    if(this.flType == FL_TYPE.direction) {
                        // 说明是纵向推理
                        if(res.data.result.flJobInference.status == 'CREATED') {
                            // 任务未开始
                            this.directionFlag = true 
                            this.startFlag = false
                            this.startIng = false
                            this.finishFlag = false
                            this.creating = false
                            clearInterval(this.refreshData)
                        } else if(res.data.result.flJobInference.status == 'INFERENCEING') {
                            // 任务推理中
                            this.startFlag = false
                            this.directionFlag = false
                            this.startIng = true
                            this.finishFlag = true
                            this.creating = false
                        } else if(res.data.result.flJobInference.status == 'CREATING') {
                            // 任务创建中
                            this.startFlag = false
                            this.startIng = false
                            this.finishFlag = false
                            this.creating = true
                            this.directionFlag = false
                        } else {
                            // 任务已完成
                            this.startFlag = false
                            this.startIng = false
                            this.finishFlag = true
                            this.creating = false
                            this.directionFlag = false
                            clearInterval(this.refreshData)
                        }
                    } else if(this.flType == FL_TYPE.transverse || this.flType == null) {
                        // 横向推理 flType = 1 或者 flType = null
                        this.directionFlag = false 
                        if(res.data.result.flJobInference.status == 'CREATED') {
                            // 任务未开始
                            this.startFlag = true
                            this.startIng = false
                            this.finishFlag = false
                            this.creating = false
                            clearInterval(this.refreshData)
                        } else if(res.data.result.flJobInference.status == 'INFERENCEING') {
                            // 任务推理中
                            this.startFlag = false
                            this.startIng = true
                            this.finishFlag = true
                            this.creating = false
                        } else if(res.data.result.flJobInference.status == 'CREATING') {
                            // 任务创建中
                            this.startFlag = false
                            this.startIng = false
                            this.finishFlag = false
                            this.creating = true
                        } else {
                            // 任务推理完成
                            this.startFlag = false
                            this.startIng = false
                            this.finishFlag = true
                            this.creating = false
                            clearInterval(this.refreshData)
                        }
                    }
                }
            })
            .catch(err=>{

            })
        },
        taskSuccess() {
            this.reload()
        },
        // refreshData() {
        //     setInterval(()=>{
        //         this.ferenceQueryById()
        //     },10000)
        // }
    },
    watch: {
        flModelInfoObj(newValue, oldValue) {
            this.modalName = newValue.modelName?newValue.modelName:'暂无数据'
            this.explain =  newValue.instruction? newValue.instruction:'暂无数据'
            this.introduce =  newValue.modelDescribe?newValue.modelDescribe:'暂无数据'
            this.application = newValue.application?newValue.application:'暂无数据'
            let deepData = JSON.parse(newValue.metric)
        
            let newObjs= Object.keys(deepData).reduce((newData, key) => {
                let newKey = this.$MODEL_DESC[key] || key
                newData[newKey] = (deepData[key] * 100 ).toFixed(2)+'%'
                return newData
            }, {})
            const arr = Object.keys(newObjs).map(key => `${key}:${newObjs[key]}`);
            newValue.metric = arr.join('；')
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
                background-color: #e01622;
                color: #fff;
                border-radius: 10px;
            }
        }
    }
    .content-box{
        display: flex;
    }
    .detail-box{
        overflow: hidden;
        display: flex;
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
           
            border-right: 1px solid #AEAEAE;
            overflow: hidden;
            padding-right: 50px;
            .title{
                font-size: 23px;
                font-weight: bold;
                color: #3A4755;
                margin-bottom: 40px;
            }
            .text{
                margin-bottom: 30px;
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

                .progress {
                    margin-bottom: 40px;
                    .number{
                        text-align: left;
                        font-weight: 700;
                        font-size: 17px;
                        word-break: break-all;
                        overflow: hidden;
                    }
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
                            .item-left{
                                flex: 1;
                                text-align: left;
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
                                }
                                .name-hos{
                                    color: #9898F1;
                                    margin-right: 5px;
                                }
                                .num{
                                    color: #808096;
                                }
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
