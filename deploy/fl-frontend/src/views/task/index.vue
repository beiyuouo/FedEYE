<template>
    <div class="main-task">
        <div class="mytaks-header">
            <div class="title">
                <div>
                    任务池
                </div>
            </div>
            <div class="block">
                <el-select  @change='changeClick' v-model="value" filterable placeholder="请选择">
                    <el-option
                        v-for="item in options"
                        :key="item.value"
                        :label="item.label"
                        
                        :value="item.value">
                    </el-option>
                </el-select>
                <el-select  @change='changeClick1' v-model="value1" filterable placeholder="请选择">
                    <el-option
                        v-for="item in options1"
                        :key="item.value"
                        :label="item.label"
                        
                        :value="item.value">
                    </el-option>
                </el-select>
                <div class="more">
                    <el-input
                        placeholder="请输入任务名称"
                        v-model="input"
                        @clear='dataClearClick'
                        clearable>
                        <el-button @click="searchMydata" class="btn" slot="append" icon="el-icon-search"></el-button>
                    </el-input>
                </div>
                <el-button v-if="codeFlag" @click="joinCodeHandleClick" class="invacode" type="text">邀请码加入</el-button>
                <el-pagination
                    background
                    @current-change="handleChangeTaskPage"
                    :current-page.sync="pagination.currentpage"
                    :page-size="pagination.limit"
                    layout="prev, pager, next"
                    :total="pagination.total">
                </el-pagination>
            </div>
        </div>
        <el-row v-if="allTaskData.length > 0" :gutter="30">
            <el-col :span="6" 
                v-for="item in allTaskData"
                :key="item.id"
                class="col-box"
                @click.native="gotoTaskDetail(item.id)"
                >
                <el-card shadow="hover" class="box-card">
                    <div class="item"
                        >
                        <div class="main-header">
                            
                            <div class="main-title">
                                <!-- <span class="point"></span> -->
                                <el-tooltip class="item" effect="dark" :content="item.name" placement="top-start">
                                     <span>{{item.name}}</span>
                                </el-tooltip>
                               
                            </div>
                            <span v-if="item.type!='INFERENCE'" :class="item.jobStatus=='0'?'bgg':item.jobStatus=='3'?'bgf':item.jobStatus=='4'?'bgr':'bgb'"></span>
                            <span v-if="item.type!='INFERENCE'" :class="[item.jobStatus=='0'?'green':item.jobStatus=='3'?'finish':item.jobStatus=='4'?'red':'blue','status']">
                                {{item.jobStatus=='0'?'招募中':item.jobStatus=='3'?'训练完成':item.jobStatus=='4'?'训练失败':'训练中'}}
                            </span>
                            <div class="time">更新于{{item.createTime.substring(0,10)}}</div>
                            <span class="role" :class="[item.jobType=='1'?'role':'hide']">{{item.jobType=='1'?'联邦':''}}</span>
                        </div>
                        <div class="part-info">
                            <!-- <el-tag>  -->
                                任务发起方：{{item.partyId_dictText}}

                            <!-- </el-tag> -->
                            
                        </div>
                        <div class="main-middle">
                            <el-tooltip class="item" effect="dark" :content="item.content?item.content:'暂无描述'" placement="top-start">
                                <div class="desc">
                                    {{item.content?item.content:'暂无描述'}}
                                </div>
                            </el-tooltip>
                            <!-- <div class="desc">
                                {{item.name}}
                            </div> -->
                            <el-divider></el-divider>
                            <div class="data-box">
                                <!-- <el-tag>
                                    {{item.type=='INFERENCE'?'推理':'训练'}}
                                </el-tag> -->
                                <span class="artil">
                                    {{item.partyNums}} 联邦方
                                </span>
                                <span class="data">
                                    {{item.rowNUms}} 数据
                                </span>
                            </div>
                            
                        </div>
                    </div>
                </el-card> 
            </el-col>
            
        </el-row>
        <div v-else class="img-box"  >
            <img src="@/assets/notask.png" alt="">
            <div>
                暂无数据
            </div>
        </div>
        <join-code @updateInfoVisible='updateInfoVisible'  @handleClickJoinCode='handleClickJoinCode'  :visible.sync='joinDialogVisible'></join-code>

    </div>
</template>
<script>
import {listAllJob } from '@/api/index'
import imgs from '@/assets/taskicon.png'
import { getTenantId} from "@/utils/auth";
import joinCode from '@/components/joinCode'
export default {
    name: 'task',
    components: {
        joinCode
    },
    data() {
        return {
            allTaskData: [],
            imgs:imgs,
            allTaskObj : {},
            pagination: {
				offset: 1,  //no
				currentpage: 1,
				limit: 16,  //size
                total: 0
			},
            codeFlag: true,
            joinDialogVisible: false,
            input: '',
            options: [{
                value: '',
                label: '全部任务'
                }, {
                value: '1',
                label: '联邦任务'
                }, {
                value: '2',
                label: '独立科研'
            }],
            value: '',
            options1: [{
                value: '',
                label: '所有状态'
                }, {
                value: '0',
                label: '招募中'
                }, 
                {
                value: '3',
                label: '训练完成'
                },
                {
                value: '6',
                label: '训练中'
                }
            ],
            value1: ''
        }
    },
    created() {
        this.listAllJob()
        if(sessionStorage.getItem('tourist') == 'true') {
            this.codeFlag = false
        } else  {
            this.codeFlag = true
        }
    },
    methods: {
        // 任务类型查询
        changeClick() {
            this.pagination.currentpage = 1
            this.listAllJob()
        },
        // 任务状态查询
        changeClick1() {
            this.pagination.currentpage = 1
            this.listAllJob()
        },
        // 任务查询
        searchMydata() {
            this.pagination.currentpage = 1
            this.listAllJob()
        },
        // 数据清空
        dataClearClick() {
            this.input = ''
            this.listAllJob()
        },
        // 任务池
        async listAllJob() {
            let params = {
                name: this.input,
                pageNo:this.pagination.offset,
                pageSize:this.pagination.limit,
                type: this.value,
                status: this.value1
            }
            await listAllJob(params,getTenantId()).then(res=>{
                this.allTaskObj = res.data.result
                this.allTaskData = res.data.result.records
                this.pagination.total = res.data.result.total
            })
            .catch(res=>{
                
            })
        },
        // 任务详情
        gotoTaskDetail(id,partyId){
            this.$router.push({
                name: 'MytaskDetail',
                query:{
                    id: id,
                    from: "home"
                }
            })
            
        },
        updateInfoVisible(val){
            this.joinDialogVisible = val
        },
        handleClickJoinCode() {
            this.joinDialogVisible = false
        },
        // 邀请码加入任务
        joinCodeHandleClick() {
            this.joinDialogVisible = true
        },
        handleChangeTaskPage(current) {
            this.pagination.currentpage = current;
            this.pagination.offset = current
            // this.pagination.offset = (current - 1) * this.pagination.limit;
            this.listAllJob()
        },
        // 分页:当前页(current-page)改变时会触发
        handleChangePage(current) {
            this.pagination.currentpage = current;
            
            this.pagination.offset = (current - 1) * this.pagination.limit;
            // this.loadingFlag = true;
            this.$emit(
                "searchData",
                this.pagination.currentpage,
                this.pagination.offset,
            );
        },
    }
}
</script>
<style lang="scss" scoped>
.main-task{
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
        .block{
            // margin-bottom: 30px;
            overflow: hidden;
            text-align: right;
            display: flex;
            justify-content: flex-end;
            align-items: center;
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
                // overflow: hidden;
                float: right;
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
            .more{
                width: 240px;
                .btn{
                    background: #e01622;
                    color: #fff; 
                }
            }
            
        }
        .header-right{
            display: flex;
            align-items: center;
            
            .more{
                width: 240px;
                .btn{
                    background: #e01622;
                    color: #fff; 
                }
            }
        }
        
    }
    .col-box{
        // transform: skewX(-10deg);
        
        .box-card{
            height: 236px;
            border-radius: 10px;
            // margin: 20px 0  0;
            margin-bottom: 30px;
            padding:30px;
            box-sizing: border-box;
            cursor: pointer;
            
            ::v-deep .el-card__body{
                padding: 0;
                // transform: skewX(10deg);
            }
            .item{
        
                font-size: 20px;
                .main-header{
                    position: relative;
                    // display: flex;
                    // align-items: center;
                    .time{
                        margin-left: 14px;
                        display: inline-block;
                        font-size: 16px;
                        color: #A7A7DB;
                        margin-top: 5px;
                        // margin-bottom: 30px;
                    }
                    .main-title{
                        display: flex;
                        align-items: center;
                        overflow: hidden;
                        text-overflow: ellipsis;
                        white-space: nowrap;
                        color: #484848;
                        font-size: 20px;
                        font-weight: 700;
                        margin-right: 20px;
                        span{
                            overflow: hidden;
                            text-overflow: ellipsis;
                            white-space: nowrap;
                        }
                        .point{
                            width: 4px;
                            height: 4px;
                            background-color: #9898F1;
                            display: block;
                            margin-right: 10px;
                        }
                        
                    }
                    .point {
                        width: 10px;
                        display: inline-block;
                        height: 10px;
                        background: #16CC28;
                        opacity: 1;
                        border-radius: 40px;
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
                    .bgr{
                        width: 10px;
                        display: inline-block;
                        height: 10px;
                        opacity: 1;
                        border-radius: 40px;
                        background: red; 
                    }
                    .status{
                        font-size: 12px;
                        font-weight: 700;
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
                        color: red;
                    }
                }
                .part-info{
                    font-size: 16px;
                    // color: #409EFF;
                    display: -webkit-box;
                    -webkit-box-orient: vertical;
                    -webkit-line-clamp: 1;
                    overflow: hidden;
                    margin-top: 10px;
                }
                .main-middle{
                    align-items: center;
                    margin-top: 10px;
                    // display: flex;
                    
                    .desc{
                        font-size: 16px;
                        color: #B8B8B8;
                        display: -webkit-box;
                        -webkit-box-orient: vertical;
                        -webkit-line-clamp: 1;
                        overflow: hidden;
                    }
                    .img{
                        display: flex;
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
                    .data-box{
                        font-size: 12px;
                        .artil{
                            width: 97px;
                            height: 33px;
                            line-height: 33px;
                            text-align: center;
                            background: #F6F6FB;
                            border-radius: 8px;
                            margin-right: 10px;
                            display: inline-block;
                            // margin-left: 10px;
                        }
                        .data{
                            width: 97px;
                            height: 33px;
                            line-height: 33px;
                            text-align: center;
                            background: #F6F6FB;
                            border-radius: 8px;
                            display: inline-block;
                        }   
                    }
                    
                }
                .footer{
                    height: 40px;
                    display: flex;
                    margin-top: 40px;
                    align-items: center;
                    padding: 0 30px 0 20px;
                    background: #EEF4FE;
                    border-radius: 30px;
                    .lf{
                        flex: 2;
                        display: flex;
                        font-size: 14px;
                        align-items: center;
                        .point{
                            display: block;
                            width: 4px;
                            height: 4px;
                            margin-right: 5px;
                            background: #707070;
                            border-radius: 50%;
                        }
                    }
                    .fr{
                        float: right;
                        font-size: 14px;
                        color: #0B80EA;
                    }
                }
            }
            .role{
                position: absolute;
                right: -78px;
                top: -20px;
                transform:rotate(45deg);
                width: 150px;
                text-align: center;
                background-color: #f3f8fd;
                height: 35px;
                line-height: 35px;
                font-size: 18px;
                font-weight: 700;
                color: #e01622;
                // margin-right: 10px;
                // display: inline-block;
            }
            .hide{
                display: none;
            }
            .wait{
                background-color: #777BF3;
            }
            .ready{
                background-color: #0B80EA;
            }
            .complete{
                background-color: #05A8DB;
            }
        }
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
}
</style>
