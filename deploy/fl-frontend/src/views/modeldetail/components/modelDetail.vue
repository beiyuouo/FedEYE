<template>
    <div class="detail-container">
        <div class="header">
            <div class="header-left">
                <img src="@/assets/taskicon.png" alt="">
                <span class="content-title">{{flModelInfoObj.modelName}}</span>
            </div>
            <div v-if="canDeleteFlag" class="invcodejoin">
                <el-button   @click='deleteModelClick' type="text">删除模型</el-button>
            </div>
        </div>
        <div class="detail-box">
            <div class="left">
                <div class="left-introduce">
                    <div class="title">
                        模型介绍
                    </div>
                    <div class="desc">
                        <el-input
                            type="textarea"
                            placeholder=""
                            v-model="introduce"
                            maxlength="500"
                            :rows='4'
                            resize='none'
                            :disabled="disabledFlag"
                            show-word-limit
                        >
                        </el-input>
                    </div>
                </div>

                <div class="left-performance">
                    <div class="title">
                        模型性能
                    </div>
                    <div class="desc">
                        {{flModelInfoObj.metric}}
                    </div>
                   
                </div>
                <div class="left-nums">
                    <div class="title">
                        训练数据量
                    </div>
                    <div class="desc">
                        {{rowNums}}
                    </div>
                   
                </div>
                <div class="left-user">
                    <div class="title">
                        模型贡献者
                    </div>
                    <div class="desc">
                        {{copyPartyNameList.join(' ; ').toString()}}
                        <!-- {{partyNameList.toString()}} -->
                    </div>
                   
                </div>
                 <div class="left-footer">
                    <div class="title">
                        使用说明
                    </div>
                    <div class="desc">
                        {{explain}}
                        <!-- {{flModelInfoObj.remark}} -->
                    </div>
                   
                </div>
                <div class="btn">
                    <el-button :loading="loading"   @click="downloadModal()"  type="primary">下载模型</el-button>
                </div>
            </div>
           
            <div class="right">
                <div class="right-top">
                    <div class="titile">
                        基础信息
                    </div>
                    <div class="base-info">
                        <div class="nums">
                            <div class="sub-title">下载次数</div>
                            <div class="number">{{usedNums}}</div>
                        </div>
                        <div class="nums">
                            <div class="sub-title">下载方</div>
                            <div class="number">{{partyNums}}</div>
                        </div>
                    </div>
                    <div class="progress">
                        <div class="sub-title">适用情况</div>
                        <div class="number">{{flModelInfoObj.instruction?flModelInfoObj.instruction:'暂无信息'}}</div>
                    </div>
                    <div class="time-info">
                        <div class="nums">
                            <div class="sub-title">最近使用</div>
                            <span class="number">{{flModelInfoObj.updateTime}}</span>
                        </div>
                    </div>
                    <div class="party-info">
                        <div class="nums">
                            <div class="sub-title">其他使用方</div>
                            <div class="img" v-if="usedUserList.length!=0">
                                <el-avatar 
                                    v-for="(items,index) in usedUserList"
                                    :key="index"
                                    class="avatar " 
                                    :src="items==null?img:items.avatar.indexOf('http')!=-1?items.avatar:`${$url}${items.avatar}`"
                                >
                                </el-avatar>
                               
                            </div>
                            <div class="no-info" v-else>
                                暂无数据
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
                            <div v-if="listUseArray.length>0">
                                <div class="item" v-for="(items,index) in listUseArray" :key="index">
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
                           <div v-else>
                                暂无数据
                            </div>
                            <!-- <div class="item" v-for="(items,index) in recentData.records" :key="index">
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
                            </div> -->
                           
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
import {  modalDownload } from '@/api/index'
import { deleteModelPub } from '@/api/task'
export default {
    components:{
        BreadCrumb,
    },
    props: {
        flModelInfoObj: Object,
        partyNameList: Array,
        rowNums: Number,
        usedUserList: Array,
        partyNums: Number,
        usedNums: Number,
        canDeleteFlag: Boolean,
    },
    inject:['reload'],
    data() {
        return {
            disabledFlag:true,
            introduce: '暂无信息',
            modalId:'',
            id:'',
            loading: false,
            copyPartyNameList: [],
            explain: '', 
            listUseArray:[]
        }
    },
    watch: {
        flModelInfoObj(newValue, oldValue) {
            this.modalName = newValue.modelName?newValue.modelName:'暂无数据'
            this.explain =  newValue.instruction? newValue.instruction:'暂无数据'
            this.introduce =  newValue.modelDescribe?newValue.modelDescribe:'暂无数据'
            let deepData = JSON.parse(newValue.metric)
        
            let newObjs= Object.keys(deepData).reduce((newData, key) => {
                // let newKey = this.$MODEL_DESC[key] || key
                let newKey = this.$MODEL_DESC[key] + `（${key}）` || key
                newData[newKey] = (deepData[key] * 100 ).toFixed(2)+'%'
                return newData
            }, {})
            const arr = Object.keys(newObjs).map(key => `${key}:${newObjs[key]}`);
            newValue.metric = arr.join('；')
        },
        partyNameList: {
            handler(newValue) {
                this.copyPartyNameList = []
                newValue.forEach(item=>{
                    this.copyPartyNameList.push(item.name +'('+'贡献了'+item.rowNum+'条数据'+')')
                })
            },
            // immediate: true,
            deep: true
        },
         recentData: {
            handler(newValue) {
                this.listUseArray = newValue.records
            },
            // immediate: true,
            deep: true
        }
       
    },
    created() {
        this.id = getTenantId()
        this.modalId = this.$attrs.id
    },
    methods: {
        // 删除模型
        deleteModelClick() {
            this.$confirm('确定删除吗?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.deleteModelPub()
            });
            
        },
        async deleteModelPub() {
             let params = {
                id:  this.modalId
            }
            await deleteModelPub(params).then(res=>{
                if(res.data.code == 200) {
                    this.$message.success('删除训练任务成功')
                    this.$router.go(-1)
                }
            })
            .catch(err=>{
                  this.$message.error(err.message)
            })
        },
        async modalDownload() {
           
            let id = getTenantId()
            let data = new URLSearchParams()
            data.append('id',this.modalId)
            data.append('retry',1)
            await modalDownload(data,this.id).then(res=>{
                if(res.data.code== 200) {
                    this.$message.success('模型下载成功')
                    this.loading = false
                    this.reload()
                    
                } else {
                     this.$message.error('模型下载失败')
                      this.loading = false
                }
            })
            .catch(res=>{
                this.$message.error(res.data.message)
                 this.loading = false
            })
        },
        // 下载模型
        downloadModal() {
            if(sessionStorage.getItem('tourist')== 'true') {
                this.$message.error('请绑定联邦方')
                return false
            } else {
                this.loading = true
                this.modalDownload()
            }
            
            
        },
    },
}
</script>
<style lang="scss" scoped>
.detail-container{
    .header{
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 40px;
        .header-left{
            display: flex;
            align-items: center;
        }
        .content-title{
            font-weight: 700;
            font-size: 40px;
            margin-left: 20px;
        }
        .edit{
            font-size: 22px;
            color: #B3B3B3;
            cursor: pointer;
            span{
                margin-left: 10px;
                margin-right: 30px;
            }
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
            .left-introduce{
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
                    word-break: break-all;
                    overflow: hidden;
                    ::v-deep .el-textarea.is-disabled .el-textarea__inner {
                        background-color: #fff;
                        border-color: #fff;
                        color: #C0C4CC;
                        cursor: default;
                        padding: 0;
                        font-size: 20px;
                    }
                    ::v-deep .el-textarea__inner{
                        background-color: #F5F7FA;
                        border-color: #E4E7ED;
                        color: #C0C4CC;
                        font-size: 20px;
                        // padding: 0;
                    }
                }   
            }
            .left-performance{
                .title{
                    font-size: 23px;
                    font-weight: bold;
                    color: #3A4755;
                    margin: 30px 0;
                }
                .desc{
                    font-size: 20px;
                    font-family: MicrosoftYaHei;
                    color: #3A4755;
                    word-break: break-all;
                    overflow: hidden;
                }   
            }
            .left-nums{
                .title{
                    font-size: 23px;
                    font-weight: bold;
                    color: #3A4755;
                     margin: 30px 0;
                }
                .desc{
                    font-size: 20px;
                    font-family: MicrosoftYaHei;
                    color: #3A4755;
                    word-break: break-all;
                    overflow: hidden;
                }   
            }
            .left-user{
                .title{
                    font-size: 23px;
                    font-weight: bold;
                    color: #3A4755;
                    margin: 30px 0;
                }
                .desc{
                    font-size: 20px;
                    font-family: MicrosoftYaHei;
                    color: #3A4755;
                    word-break: break-all;
                    overflow: hidden;
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
                        .no-info{
                            font-size: 17px;
                            color: #3A4755; 
                            font-weight: 700;
                            margin-top: 10px;
                        }
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