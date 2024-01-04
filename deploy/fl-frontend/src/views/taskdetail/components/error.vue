<template>
    <div class="box">
        <div class="line-box">
            <div class="title">
                任务进度
                <span class="daily">
                    <el-link @click="gotoOtherParty" type="primary">查看日志</el-link>
                    
                </span>
            </div>
            <div class="step">
                <el-divider class="one">
                    <span class="num">1</span>
                    <span class="blue">任务初始化</span>
                </el-divider>
                <el-divider class="one" v-if="active==2||active==3||active==4">
                    <span class="num">2</span>
                    <span :class="active==2||active==3||active==4?'blue':'hui'">任务准备中</span>
                </el-divider>
                <el-divider v-else class="">
                    <span class="huinum">2</span>
                    <span class="hui">任务准备中</span>
                </el-divider>
                <el-divider class="one" v-if="active==3||active==4">
                    <span class="num">3</span>
                    <span :class="active==3||active==4?'blue':'hui'">模型训练中</span>
                </el-divider>
                <el-divider v-else class="">
                    <span class="huinum">3</span>
                    <span class="hui">模型训练中</span>
                </el-divider>
                <el-divider  @click="gotoOtherParty" v-if="active==4" class="four">
                    <span class="num ">4</span>
                    <span :class="[active==4?'red':'hui']">训练失败</span>
                </el-divider>
                <el-divider v-else class="">
                    <span class="huinum">4</span>
                    <span class="hui">训练失败</span>
                </el-divider>
            </div>
        </div>
        <div class="content">
            
            <div class="content">
                <div class="img-box">
                    <img   class="img" src="@/assets/modeltraining.png" alt="">
                    <div  class="text">
                        模型训练失败
                    </div>
                    
                </div>
            </div>
        </div>
    </div>
</template>
<script>
import LineBox from './line'
export default {
    props: {
        logUrl: String,

    },
    created() {
    },
    data() {
        return {
            active:"4",
        }
    },
    methods: {
        gotoOtherParty() {
            if(this.logUrl) {
                 window.open(this.logUrl)
            }
        }
    },
}
</script>
<style lang="scss" scoped>
.box{
    // background-color: #f9f9f9;
    height: 100%;
    min-height: 400px;
    padding: 40px;
    .content{
        
        .img-box{
            margin-top: 80px;
            text-align: center;
            // display: flex;
            // flex-direction: column;
            // justify-content: center;
            // align-items: center;
            .text{
                margin-top: 20px;
                color: #9a9a9a;
                font-size: 16px;
                margin-bottom: 60px;
            }
        }
    }
}
.one{     
    background-color: #0B80EA;  
}

.line-box{
    // background-color: #f9f9f9;
    .title{
        font-size: 23px;
        font-weight: bold;
        color: #3A4755;
        margin-bottom: 40px;
        .daily{
            font-size: 16px;
            margin-left: 15px;
            cursor: pointer;
            
            ::v-deep .el-link--inner{
                color: #e01622;
            }
        }
    }
    .step{
        display: flex;
        align-items: center;
        height: 62px;
        .blue{
            color: #0B80EA;
            
        }
        .hui{
            color: #a3a3a3;
        }
        .huinum{
            display: inline-block;
            margin-right: 10px;
            width: 22px;
            height: 22px;
            line-height: 22px;
            border-radius: 50%;
            background-color: #a3a3a3;
            color: #fff;
            font-weight: 700;
            text-align: center;
        }
        .num{
            display: inline-block;
            margin-right: 10px;
            width: 22px;
            height: 22px;
            line-height: 22px;
            border-radius: 50%;
            background-color: #0B80EA;
            color: #fff;
            font-weight: 700;
            text-align: center;
        }
        ::v-deep .el-divider__text{
            // background-color: #f9f9f9;
            width: 100%;
            padding: 0 0px;
            left: 50%;
            top: -18px;
        }
        ::v-deep .el-divider--horizontal{
            height: 10px;
            margin-right: 20px;
        }
        .four{
            background-color:red; 
            cursor: pointer;
            .num{
                background-color: red;
            } 
            .red{
                color: red;
                
            }
        }
    }
    
}
</style>
