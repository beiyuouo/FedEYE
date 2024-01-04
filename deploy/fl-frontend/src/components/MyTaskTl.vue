<template>
    <el-card shadow="always" class="box-card">
        <div class="item"
            >
            <div class="main-header">
                
                <div class="main-title">
                    <span class="point"></span>
                    <span>{{item.name}}</span>
                </div>
                <span v-if="item.type=='INFERENCE'" :class="item.jobStatus=='CREATED'?'bgg':item.jobStatus=='COMPLETED'?'bgf':'bgb'"></span>
                <span v-if="item.type=='INFERENCE'" :class="[item.jobStatus=='CREATED'?'green':item.jobStatus=='COMPLETED'?'finish':'blue','status']">
                    {{item.jobStatus=='CREATED'?'已创建':item.jobStatus=='INFERENCEING'?'推理中':'推理完成'}}
                </span>
                <div class="time">更新于{{item.createTime.substring(0,10)}}</div>
            </div>
            <div class="main-middle">
                <div class="desc">
                    {{item.name}}
                </div>
                <el-divider></el-divider>
                <div class="data-box">
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
</template>
<script>
import imgs from '@/assets/avatar.png'
export default {
    props:{
        item: Object
    },
    data() {
        return {
            imgs:imgs,
            status: 3
        }
    },
    created () {
    
    },
    methods: {
       
    }
}
</script>
<style lang="scss" scoped>
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
                    font-size: 16px;
                    color: #A7A7DB;
                    margin-top: 5px;
                    margin-bottom: 30px;
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
                    .bgr{
                        width: 10px;
                        display: inline-block;
                        height: 10px;
                        opacity: 1;
                        border-radius: 40px;
                        background: red; 
                    }
                    .bgf{
                        width: 10px;
                        display: inline-block;
                        height: 10px;
                        opacity: 1;
                        border-radius: 40px;
                        background: #05A8DB; 
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
                        color:red;
                    }
                }
            }
            .main-middle{
                align-items: center;
                margin-top: 30px;
                // display: flex;
                .desc{
                    font-size: 16px;
                    color: #B8B8B8;
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
            color: #0B80EA;
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
</style>
