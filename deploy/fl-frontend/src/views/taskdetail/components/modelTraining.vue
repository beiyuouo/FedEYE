<template>
    <div class="box">
        <line-box :logUrl='logUrl' :active='active'></line-box>
        <div class="content">
            <div class="img-box">
                <img   class="img" src="@/assets/modeltraining.png" alt="">
                <div  class="text">
                    模型正在训练中，请耐心等待，完成后会发送通知
                </div>
                <div v-loading="loading">
                    
                </div>
            </div>
        </div>
        
    </div>
</template>

<script>
import {jobInfo} from '@/api/task'
import LineBox from './line'
import { getTenantId} from '@/utils/auth.js' 
export default {
    props:{
        showStart: Boolean,
        fljob: Array,
        registid: String,
        logUrl: String
    },
    components:{
        LineBox
    },
    data() {
        return {
            active: "3",
            loading: true,
            refreshData: null
        }
    },
    created() {
        this.partyName = sessionStorage.getItem('showname')
        // this.jobInfo()
        clearInterval(this.refreshData)
        this.refreshData = null
        // this.refreshData()
        this.refreshData =  setInterval(()=>{
            this.checkoutRes()
        },10000)
    },
    beforeDestroy() {
        clearInterval(this.refreshData)
        this.refreshData = null
    },
    methods: {
        // 查询任务执行信息
        checkoutRes() {
            this.$emit('checkoutRes')
        }
       
    }
}
</script>
<style lang="scss" scoped>
.timeline{
    margin: 0;
    font-size: 14px;
    list-style: none;
    padding: 0;
    .timeline-item:last-child{
        .timeline-item__tail{
            position: absolute;
            left: 4px;
            height: 0%;
            border-left: 2px solid #E4E7ED 
        }
    }
    .timeline-item{
        position: relative;
        padding-bottom: 20px;
        .timeline-item__tail{
            position: absolute;
            left: 4px;
            height: 110%;
            border-left: 2px solid #E4E7ED 
        }
        .timeline-item__node--normal{
            left: -1px;
            width: 12px;
            height: 12px;
            top: 8px;
        }
        .timeline-item__node{
            position: absolute;
            display: flex;
            justify-content: center;
            align-items: center;
        }
        .timeline-item__wrapper{
            margin-left: 20px;
            .content-item{
                // border: 1px solid #D2E4F5;
                border-radius: 6px;
                margin-left: 16px;
                padding: 10px 20px;
            }
        }
    }
}
.one{     
    background-color: #0B80EA;  
}
.box{
    // background-color: #f9f9f9;
    height: 100%;
    padding: 40px;
    padding-bottom: 100px;
    .content{
        // display: flex;
        height: 100%;
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
</style>
