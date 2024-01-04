<template>
    <div class="hot-box">
        <div class="title">
            热门任务
        </div>
        <div v-if="resdata.length > 0" class="content">
            <div class="item"
                v-for="(item,index) in resdata.slice(0,3)"
                :key="index"
                >
                <div class="header">
                    <span class="subtitle">{{item.flJobRecruit.name}}</span>
                    <span class="status">
                         {{item.flJobRecruit.recruitStatus=='0'?'招募中':item.flJobRecruit.recruitStatus=='3'?'训练完成':'训练中'}}
                    </span>
                </div>
                <div class="sub-title">
                    {{item.flJobRecruit.content}}
                </div>
                <el-collapse class="collbox" v-model="activeNames">
                    <el-collapse-item :name="index">
                        <template slot="title">
                            <span class="artil">
                                {{item.partyNums}} 联邦方
                            </span>
                            <span class="data">
                                {{item.rowNums}} 数据
                            </span>
                        </template>
                        <div class="img">
                            <el-tooltip effect="light"  placement="top-start">
                                <span slot="content"
                                    >
                                    {{item.showOtherName}}
                                </span>
                                <div> 
                                    <el-avatar 
                                        v-for="(items,index) in item.flJobRegistList"
                                        :key="index"
                                        class="avatar " 
                                        :src="items==null?img:items.partyAvatar==null?img:items.partyAvatar.indexOf('http')!=-1?items.partyAvatar:`${$url}${items.partyAvatar}`"
                                    >
                                    </el-avatar>
                                    
                                </div>
                                
                            </el-tooltip>
                        </div>
                        <!-- <div class="img">
                            <el-avatar 
                                v-for="(items,index) in item.flJobRegistList"
                                :key="index"
                                class="avatar " 
                                :src="items==null?img:items.partyAvatar==null?img:items.partyAvatar.indexOf('http')!=-1?items.partyAvatar:`${$url}${items.partyAvatar}`"
                            >
                            </el-avatar>
                                
                        </div> -->
                        <div class="carousel">
                            <div class="left">
                                <div class="item" v-for="(items,index) in item.flJobRegistList" :key="index">
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
                    </el-collapse-item>
            
                </el-collapse>
                <div>   
                    
                </div>
               

            </div>
        </div>

       <div v-else class="content img-box"  >
            <img src="@/assets/notask.png" alt="">
            <div>
                暂无数据
            </div>
        </div>
    </div>
</template>
<script>
import {hotJob } from '@/api/index'
import { getTenantId} from '@/utils/auth.js'
import { utils } from '@/mixins/index'
import img  from '@/assets/avatar.png'
export default {
    mixins: [utils],
    data() {
        
        return {
            img: img,
            activeNames:[0],
            hotdata: [
                {
                    name:'北京1院',
                },
                {
                    name:'北京2院'
                },
                {
                    name:'北京3院'
                },
                {
                    name:'北京4院'
                }
            ],
            resdata:[],
            currentIndex: 0,
            images: [],
            url:'',
            showOtherName: ''
        }
    },
    created() {
        this.hotJob()
        this.url = this.$url
       
    },
    methods: {
        
        async hotJob () {
            let id = getTenantId()
            await hotJob(id).then(res=>{
                this.resdata = this.deepCopy(res.data.result)
                this.resdata.forEach(item=>{
                    let array = []
                    this.showOtherName = ''
                    item.flJobRegistList.forEach(item1=>{
                        array.push(item1.partyName).toString()
                       
                    })
                    this.showOtherName = array.toString()
                    item.showOtherName = this.showOtherName
                    
                })
            })
            .catch(res=>{
                console.log(res)
            })
        },
        gotoPage(items) {
           
            let cur = items.shift()
            let temp  = items
            temp.push(cur)
        },
        
    }
}
</script>
<style lang="scss" scoped>
.hot-box{
    padding: 30px;
    .title{
        text-align: center;
        margin: 0 0 30px 0;
        font-size: 27px;
        font-weight: 700;
    }
    .content{
        .no-content{
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .item{
            margin-bottom: 20px;
            .header{
                display: flex;
                .subtitle{
                    font-size: 21px;
                    font-weight: 700;
                    color: #B3B3BC;
                    white-space:nowrap;
                    overflow:hidden;
                    text-overflow:ellipsis;
                    flex: 3;
                }
                .status{
                    float: right;
                    font-weight: 700;
                    font-size: 21px;
                    color: #9898F1;
                     white-space:nowrap;
                    overflow:hidden;
                    text-overflow:ellipsis;
                    flex: 1;
                }
            }
            .sub-title{
                margin-top: 10px;
                font-size: 23px;
                font-weight: bold;
                color: #3A4755;
                white-space:nowrap;
                overflow:hidden;
                text-overflow:ellipsis;
                
            }
        }
        .item:last-child{
            margin-bottom: 0
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
.el-collapse{
    border-top: none;
    .artil{
        width: 97px;
        height: 33px;
        line-height: 33px;
        text-align: center;
        background: #F6F6FB;
        border-radius: 8px;
        margin-right: 10px;
        
    }
    .data{
        width: 97px;
        height: 33px;
        line-height: 33px;
        text-align: center;
        background: #F6F6FB;
        border-radius: 8px;
    }
    .carousel{
        margin-top: 10px;
        height: 85px;
        background: #F6F7FB;
        border-radius: 11px;
        padding: 20px;
        overflow: hidden;
        display: flex;
        .left{
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
    ::v-deep.el-icon-arrow-right {
        // content: "\e6e1";
        transform: rotate(-90deg)
    }
    ::v-deep.el-icon-arrow-right.is-active{
        transform: rotate(90deg)
    }
   
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
</style>
