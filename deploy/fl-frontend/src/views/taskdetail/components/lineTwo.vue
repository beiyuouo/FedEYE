<template>
    <div class="line-box">
        <div class="title">
            任务进度
        </div>
        <div class="step">
            <el-divider class="one">
                <span class="num">1</span>
                <span class="blue">招募中</span>
            </el-divider>
            <el-divider class="one" v-if="active==2||active==3||active==4">
                <span class="num">2</span>
                <span :class="active==2||active==3||active==4?'blue':'hui'">模型准备&训练</span>
            </el-divider>
            <el-divider v-else class="">
                <span class="huinum">2</span>
                <span class="hui">模型准备&训练</span>
            </el-divider>
            <el-divider v-if="active==4" class="one">
                <span class="num ">3</span>
                <span :class="[active==4?'blue':'hui']">训练完成</span>
            </el-divider>
            <el-divider v-else class="">
                <span class="huinum">3</span>
                <span class="hui">训练完成</span>
            </el-divider>
        </div>
    </div>
</template>
<script>
export default {
    props: {
        active: String
    },
    data() {
        return {
            
        }
    }
}
</script>
<style lang="scss" scoped>
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
            padding: 0 0px;
            left: 20%;
            top: -18px;
        }
        ::v-deep .el-divider--horizontal{
            height: 10px;
            margin-right: 20px;
        }
    }
    
}
</style>
