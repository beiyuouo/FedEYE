<template>
    <div class="detail-container">
        <bread-crumb :breadArray='breadArray'></bread-crumb>
        <div class="header">
            <div class="img">
                <img src="@/assets/taskicon.png" alt="">
            </div>
            <div class="content-title">
                <span>{{algObject.nameCn}}</span>
            </div>
        </div>
        <div v-if="!flTypeFlag" class="detail-box">
            <div class="left">
                <div class="left-top">
                    <div class="title">
                        算法说明
                    </div>
                    <div class="desc">
                        {{algObject.introduction}}
                    </div>
                </div>

                <div class="left-footer">
                    <div class="title">
                        示例
                    </div>
                    <div class="desc">
                        {{algObject.remark}}
                    </div>
                </div>
                <div class="left-middle">
                    <div class="title">
                        算法描述
                    </div>
                    <div class="desc">
                        <img :src="algObject.describeBase64" alt="">
                       
                    </div>
                </div>
                <div class="btn">
                    <el-button   @click="userBtnClick(algObject,$event)"  type="primary">新建任务</el-button>
                </div>
            </div>
           
            <div class="right">
                <div class="right-top">
                    <div class="titile">
                        基础信息
                    </div>
                    <div class="base-info">
                        <div class="nums">
                            <div class="sub-title">使用次数</div>
                            <div class="number">{{baseData.usedNums}}</div>
                        </div>
                        <div class="nums">
                            <div class="sub-title">使用方</div>
                            <div class="number">{{baseData.partyNums}}</div>
                        </div>
                    </div>
                    <div class="progress">
                        <div class="sub-title">适用情况</div>
                        <div class="number">{{algObject.remark}}</div>
                    </div>
                    <div class="time-info">
                        <div class="nums">
                            <div class="sub-title">最近更新</div>
                            <span class="number">{{algObject.createTime}}</span>
                        </div>
                    </div>
                    <div class="party-info">
                        <div class="nums">
                            <div class="sub-title">其他使用方</div>
                            <el-tooltip effect="light"  placement="top">
                                <div slot="content">
                                    {{showOtherName}}
                                </div>
                                <div class="img">
                                    <el-avatar 
                                        v-for="(items,index) in baseData.userList"
                                        :key="index"
                                        class="avatar " 
                                        :src="items==null?img:items.avatar.indexOf('http')!=-1?items.avatar:`${$url}${items.avatar}`"
                                    >
                                    </el-avatar>
                                
                                </div>
                            </el-tooltip>
                           
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
                            
                        </div>
                    </div>
                </div>
            </div>
            <create-task :algId='algId' :flType='flType' :slectData='slectData' @cancleCreateClick='cancleCreateClick' :visible.sync='creatVisible' :typeList='typeList'></create-task>
        </div>
        <div v-if="flTypeFlag" class="currency">
            <div class="left">
                <el-input
                    type="textarea"
                    rows='6'
                    placeholder="请输入内容"
                    v-model="textValue"
                    maxlength="500"
                    resize='none'
                    show-word-limit
                    >
                </el-input>
                <div class="btn">
                    <el-button  @click="translateBtnClick()"  type="primary">翻译</el-button>
                </div>
            </div>
            <div class="right">
                <el-input
                     rows='6'
                      resize='none'
                     type="textarea"
                    placeholder=""
                    v-model="enTextValue"
                    maxlength="30"
                    show-word-limit
                    :disabled="true"
                    >
                </el-input>
            </div>
        </div>
        
    </div>
</template>
<script>
import BreadCrumb from '@/components/breadcrumb.vue'
import CreateTask from '@/components/createTask'
import { getTenantId , getPartyInfo} from "@/utils/auth";
import { queryAlgById,algOpLog, queryData ,algUseInfo} from '@/api/index'
import { softWareStatus ,algorithmCall, queryFileMetaAndAlgorithm} from '@/api/task'  

export default {
    components:{
        BreadCrumb,
        CreateTask
    },
    inject:['reload'],
    data() {
        return {
            textValue: '',
            enTextValue:'',
            breadArray: [
            ],
            id: '',
            touristFlag: false,
            algObject: {},
            typeList:[],
            slectData:[],
            creatVisible: false,
            baseData:[],
            recentData: [],
            tableData:[],
            tableData2:[],
            flType:'',
            flTypeFlag: false,
            algId: '',
            showOtherName: ''
        }
    },
    created() {
        if(this.$route.query.from == 'Home') {
            this.breadArray = [
            {
                name: '算法详情',
                
            }
        ]
        } else {
            this.breadArray = [
                {
                    name: '算法池',
                    url: '/algorithm'
                },
                {
                    name: '算法详情'
                }
            ]
        }
        

        this.id = this.$route.query.id
        this.flType = this.$route.query.flType
        if(this.flType ==5) {
            this.flTypeFlag = true
        } else {
            this.flTypeFlag = false
        }
        this.queryAlgById()
        this.algUseInfo()
        this.algOpLog()
        this.partInfo = JSON.parse(getPartyInfo())
        this.partyname = sessionStorage.getItem('showname')
        this.touristFlag = sessionStorage.getItem('tourist')
        
    },
    methods: {
        // 点击进行翻译
        translateBtnClick() {
            this.algorithmCall()
        },
        // 通用算法
        async algorithmCall() {
            let id = getTenantId()
            let params = {
                // id: this.id,
                
                "doctype":"json",
                "type": "AUTO",
                "i": this.textValue
                
            }
            await algorithmCall(this.id,params,id).then(res=>{
                let result = res.data.translateResult[0]
               
                this.enTextValue  = result[0].tgt
            })
        },
        // 新建任务弹框取消
        cancleCreateClick() {
            this.creatVisible = false
        },
        //算法的基础信息
        async algUseInfo() {
            let id = getTenantId()
            let params = {
                id: this.id,
            }
            await algUseInfo(params,id).then(res=>{
                if(res.data.code == 200) {
                    this.baseData = JSON.parse(JSON.stringify(res.data.result))
                    let showNameObj = JSON.parse(JSON.stringify(this.baseData.userList))
                    let array = []
                    this.showOtherName = ''
                    showNameObj.forEach((item)=>{
                        array.push(item.name).toString()
                    }) 
                    this.showOtherName = array.toString()
                }
               
               
            })
        },
        // 算法的最近使用
        async algOpLog() {
            let id = getTenantId()
            let data = new URLSearchParams()
            data.append('id',this.id)
            // let params = {
            //     opType: 1,
            //     pageNo: 1,
            //     pageSize: 10,
            //     id: this.id,
            // }
            await algOpLog(data,id).then(res=>{
                if(res.data.code == 200 ) {
                    this.recentData = res.data.result
                    
                }
            })
        },
        // 算法查询
        async queryAlgById() {
            let id = getTenantId()
            let params = {
                id: this.id
            }
            await queryAlgById(params,id).then(res=>{
                if(res.data.code==200) {
                    this.algObject = res.data.result
                    let tempObj = JSON.parse(this.algObject.params)
                }
            })
        },
        // 新建任务
        userBtnClick(item,e) {
            let target = e.target
            this.algId = item.id
           
            if(sessionStorage.getItem('tourist')== 'true') {
                this.$message.error('请绑定联邦方')
                return false
            } else {
                this.softWareStatus(item)
                target.blur()
            }
            
        },
         // 环境检查
        async softWareStatus(item) {
            let id  = getTenantId()
            await softWareStatus(id).then(res=>{
                if(res.data.success) {
                    if(res.data.result.length != 0){
                        this.$message.warning('请部署环境之后，再创建任务')
                    } else {
                        this.typeList = []
                        this.typeList.push(item)
                        this.queryFileMetaAndAlgorithm()
                    }
                    
                }
            })
            .catch(res=>{

            })
        },
         // 查询算法池数据
        async queryData(item) {
            let id  = getTenantId()
            let params = {
                evalTypes: item.evalTypes
            }
            await queryData(params,id).then(res=>{
                if(res.data.success) {
                    this.slectData = res.data.result
                    this.creatVisible = true
                }
            })
        },
           // 查询数据和算法
        async queryFileMetaAndAlgorithm() {
            let id = getTenantId()
            let params = {
                algorithmId: this.algId,
                fileMetaId: '',
            }
            await queryFileMetaAndAlgorithm(params,id).then(res=>{
                if(res.data.code == 200) {
                    this.slectData = res.data.result.fileMeta
                    this.creatVisible = true
                    // this.algorithm = res.data.result.algorithm
                }
                
            })
            .catch(err=>{

            })
        },
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
            font-weight: 700;
            font-size: 40px;
            margin-left: 20px;
        }
    }
    .detail-box{
        display: flex;
        padding: 50px 60px;
        background-color: #fff; 
        .left{
            flex: 2;
            border-right: 1px solid #AEAEAE;
            padding-right: 50px;
            .left-top{
                .title{
                    font-size: 23px;
                    font-weight: bold;
                    color: #3A4755;
                    margin-bottom: 30px;
                }
                .desc{
                    font-size: 20px;
                    font-family: MicrosoftYaHei;
                    color: #3A4755;
                }   
            }
            .left-middle{
                margin-top: 30px;
                .title{
                    font-size: 23px;
                    font-weight: bold;
                    color: #3A4755;
                    margin-bottom: 30px;
                }
                .desc{
                    font-size: 20px;
                    font-family: MicrosoftYaHei;
                    color: #3A4755;
                    img{
                        width: 100%;
                    }
                }
            }
            .left-footer{
                margin-top: 30px;
                .title{
                    font-size: 23px;
                    font-weight: bold;
                    color: #3A4755;
                    margin-bottom: 30px;
                }
                .desc{
                    font-size: 20px;
                    font-family: MicrosoftYaHei;
                    color: #3A4755;
                }
                .detail-table{
                    display: flex;
                    .de-item{
                        
                        .de-one{
                            width: 60px;
                            height: 60px;
                            line-height: 60px;
                            font-size: 18px;
                            text-align: center;
                            border: 1px solid #808096;
                            border-bottom: none;
                            border-right: none;
                        }
                        .de-two{
                            width: 60px;
                            height: 60px;
                            line-height: 60px;
                            font-size: 18px;
                            text-align: center;
                            border: 1px solid #808096;
                            border-right: none;
                        }
                    }
                    .de-item:last-child{
                        .de-one{
                            border: 1px solid #808096;
                            border-bottom: none;
                            border-right: 1px solid #808096;
                        }
                        .de-one:nth-child(2n){
                            border-right: 1px solid #808096;;
                        }
                        .de-two{
                            border: 1px solid #808096;
                            // border-bottom: none;
                             border-right: 1px solid #808096;
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
            
        }
        .right{
            flex: 1;
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
                    .sub-title {
                        font-size: 17px;
                        color: #6b6868;
                        margin-bottom: 10px;
                    }
                    .number{
                        text-align: left;
                        font-weight: 700;
                        font-size: 17px;
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
                .right-title {
                          
                    font-size: 22px;
                    font-weight: bold;
                    color: #3A4755;
                 
                }
                .carousel{
                    margin-top: 10px;
                    height: 90px;
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
                            margin-right:10px;
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
    .currency{
        display: flex;
        padding: 50px 60px;
        background-color: #fff;
        .btn{
            text-align: right;
            margin-top: 30px;
        }
        .left{
            flex: 1;
            border-right: 1px solid #AEAEAE;
            padding-right: 50px;
        }
        .right{
            flex: 1;
            margin-left: 50px;
        }
    }
}
</style>
