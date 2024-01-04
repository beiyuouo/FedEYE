<template>
    <div>
        <line-box :active='active'></line-box>
        <div class="right-top">
            <div class="right-left">
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
                <div class="progress">
                    <div class="sub-title">进度</div>
                    <el-progress :text-inside="true" :stroke-width="26" :percentage="fProgress"></el-progress>
                </div>
                <div class="time-info">
                    <div class="nums">
                        <div class="sub-title">建立时间</div>
                        <span>{{taskObj.createTime}}</span>
                    </div>
                    <!-- <div class="nums">
                        <div class="sub-title">数据集链接</div>
                        <el-link v-if="showDataName" @click="gotoDataDetail" :underline="false" type="primary">{{showDataName}}</el-link>
                        <el-link v-else disabled  :underline="false" type="primary">暂无数据</el-link>
                    </div> -->
                </div>
            
            </div>
            <div class="right-right">
                <div class="party-info">
                    <div class="nums">
                        <div class="sub-title">发起方</div>
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
                        <div class="sub-title">其他参与方</div>
                        <div class="user-img"
                                v-for="(items,index) in fljob.slice(1)"
                                :key="index">
                            <el-avatar 
                                class="avatar " 
                                :src="items==null?img:items.partyAvatar==null?img:items.partyAvatar.indexOf('http')!=-1?items.partyAvatar:`${$url}${items.partyAvatar}`"
 
                            >
                            </el-avatar>
                            <div class="party-name">{{items.partyName}}</div>
                        </div>
                        
                        <!-- <div class="img"
                            v-for="(items,index) in fljob.slice(1)"
                            :key="index">
                            <el-avatar 
                                class="avatar " 
                                :src="items==null?img:items.partyAvatar.indexOf('http')!=-1?items.partyAvatar:`http://112.230.202.198:31825/${items.partyAvatar}`"
                            >
                            </el-avatar>
                            <div class="party-name">{{items.partyName}}</div>
                           
                        </div> -->
                    </div>
                </div>
            </div>
           
        </div>
        
    </div>
</template>

<script>
import  img from '@/assets/avatar.png'
import LineBox from './lineTwo'
import {jobInfoByRecruitId} from '@/api/task'
import { getTenantId} from '@/utils/auth.js' 
export default {
    name:'processTree',
    components: {
        LineBox
    },
    props:{
        fljob: Array,
        taskObj: Object,
        PartyNums:Number,
        rowsNum: Number,
        RecruitStatus: Number,
        id: String,
        dataId: String,
        createBy: Number,
        showDataName: String
    },
    data(){
        return {
            img: img,
            active: "0",
            fProgress: 0,
            fElapsed: 0
        }
    },
    created() {
     
        if(this.RecruitStatus == "3") {
            this.active = "4"
        } else if(this.RecruitStatus == "1") {
            this.active = "2"
        } else {
            this.active = "1"
        }
        this.jobInfoByRecruitId()
    },
    methods:{
        gotoDataDetail() {
            
            this.$router.push({
                name: 'DataDetail',
                query: { 
                    partyId: this.createBy,
                    id: this.dataId,
                    from: 'mytask',
                }
            })
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
                        this.fElapsed  = res.data.result.fElapsed
                    }
                    
                }
            })
            .catch(res=>{

            })
        }
    }
}
</script>

<style lang="scss" scoped>
.right-top {
    display: flex;
    .right-left{
        flex: 1.5;
        padding-right: 30px;
        border-right: 1px solid #AEAEAE;
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

                span {

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
    }
    .right-right{
        flex: 1.5;
        .party-info {
            // display: flex;
            // justify-content: space-between;
            // margin-bottom: 40px;
            padding-left: 40px;
            margin-top: 40px;
            .nums {
                margin-top: 30px;
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
                    align-items: center;
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
                    .party-name{
                        margin-left: 10px;
                        font-size: 17px;
                        font-weight: 700;
                    }
                }
            }
        }
    }
                 
               

               
            }



.line{
    position: absolute;
    width:142px;
    height:3px;
    background-color:rgba(203,221,238,1);
    top:50%;
    left:-161px
}

</style>
