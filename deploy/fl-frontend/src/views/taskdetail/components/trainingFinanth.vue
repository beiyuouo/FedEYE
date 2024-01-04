<template>
    <div class="box">
        <line-box :logUrl='logUrl' :active='active'></line-box>
        <div class="content">
            <div class="title">模型训练完成，具体情况如下：</div>
            <!-- 参与发 -->
            <el-table
                :data="tableData"
                v-show="!metricFlag"
                border
                :header-cell-style="{background:'#D2E4F5',color:'#484848'}"
                style="width: 100%">
                    <el-table-column
                        prop="fStartDate"
                        label="开始时间"
                        >
                    </el-table-column>
                    <el-table-column
                        prop="fEndDate"
                        label="结束时间"
                        width="180">
                    </el-table-column>
                    <el-table-column
                        prop="fElapsed"
                        label="耗时">
                    </el-table-column>
            </el-table>
            <el-table
                :data="tableData"
                v-show="metricFlag"
                border
                :header-cell-style="{background:'#D2E4F5',color:'#484848'}"
                >
                    <el-table-column
                        prop="fStartDate"
                        label="开始时间"
                        >
                    </el-table-column>
                    <el-table-column
                        prop="fEndDate"
                        label="结束时间"
                        >
                    </el-table-column>
                    <el-table-column
                        prop="fElapsed"
                        label="耗时">
                    </el-table-column>
                    <el-table-column
                        v-show="metricFlag"
                       
                        label="模型指标">
                        <template slot-scope="scope">
                            <div class="metric">
                                <el-tag v-for="(item,index) in scope.row.metricStr"
                                    :key="index"
                                    style="margin-bottom: 10px;">
                                    {{ item}}
                                </el-tag>
                            </div>
                            
                          
                        </template>
                    </el-table-column>
                   
            </el-table>
            <div class="btn">
                <el-button @click="gotoModelDetail"  type="primary">查看模型详情</el-button>
            </div>
        </div>
    </div>
</template>
<script>
import LineBox from './line'
import {jobInfo, metric} from '@/api/task'
import { getTenantId} from '@/utils/auth.js' 
export default {
    props:{
        createByFlag: Boolean,
        id: String,
        registid: String,
        logUrl: String,
        modelId: String
    },
    components:{
        LineBox
    },
    created() {
        
        this.jobInfo()
        // if(this.createByFlag){
        //     // 发起方
            
        //     this.jobInfo()
        //     this.metric()
        // }else {
        //     // 参与发
        //     this.jobInfo()
           
        // }
    },
    data() {
        return {
            active:"4",
            tableData:[
            ],
            metricStr: '',
            metricFlag: false
        }
    },
    methods: {
        // 模型详情跳转
        gotoModelDetail() {
      
            this.$router.push({
                name:'modalDetail',
                query: {
                    id:  this.modelId,
                    from: 'myModal'
                }
            })
        },
        // 时间转化
        formatData(str) {
            // let days = parseInt(str / (1000 * 60 * 60 * 24));
            let hours = parseInt((str % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            let minutes = parseInt((str % (1000 * 60 * 60)) / (1000 * 60));
            let seconds = parseInt((str % (1000 * 60)) / 1000);
            if(hours<10) {
                hours = '0' + hours
            }
            if(minutes<10) {
                minutes = '0' + minutes
            }
            if(seconds<10) {
                seconds = '0' + seconds
            }
            return hours+ ':' + minutes+ ':' + seconds
        },
        // 基础信息
        async jobInfo() {
            let id = getTenantId()
            let data = new URLSearchParams()
            data.append('registId',this.registid)
            await jobInfo(data,id).then(res=>{
                if(res.data.success) {
                    if(this.createByFlag){
                        // 发起方
                        this.metricFlag = true
                        res.data.result.fElapsed = this.formatData(res.data.result.fElapsed)
                        this.metric(res.data.result)
                    }else {
                        // 参与发
                        this.metricFlag = false
                        
                        res.data.result.fElapsed = this.formatData(res.data.result.fElapsed)
                        this.tableData.push(res.data.result)
                    }
                    
                }
            })
            .catch(res=>{

            })
        },
        // 模型指标
        async metric(item) {
            let id = getTenantId()
            let params = {
                id: this.id
            }
            await metric(params,id).then(res=>{
                if(res.data.success) {
                    
                    this.metricStr = JSON.stringify(res.data.result)
                    let deepData = JSON.parse(JSON.stringify(res.data.result))
                    console.log(deepData)
                    let newObjs= Object.keys(deepData).reduce((newData, key) => {
                        let newKey = this.$MODEL_DESC[key] || key
                        newData[newKey] = (deepData[key] * 100 ).toFixed(2)+'%'
                        return newData
                    }, {})
                    const arr = Object.keys(newObjs).map(key => `${key}:${newObjs[key]}`);
                  
                    // item.metricStr =  JSON.stringify(newObjs).replace('{','').replace('}',"").split(",")
                    // item.metricStr = JSON.stringify(newObjs).replace('{','').replace('}',"").replace("'",'').replace("'","").split(",")
                    item.metricStr = arr
                    this.tableData.push(item)
                }
                 
            })
            .catch(res=>{

            })
        }
    }
}
</script>
<style lang="scss" scoped>
.box{
    // background-color: #f9f9f9;
    height: 100%;
    min-height: 400px;
    padding: 40px;
    .content{
        
        .title{
            margin:20px 0;
            font-size: 16px;
            color: #484848;
        }
        ::v-deep .el-table th > .cell {
            text-align: center;
        }
    
        ::v-deep .el-table .cell {
            text-align: center;
        }
        .btn{
            text-align: right;
            margin-top: 30px;
        }
    }
    .metric{
        display: flex;
        flex-direction: column;
    }
}

</style>
