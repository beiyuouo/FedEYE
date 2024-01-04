<template>
    <div>
        <el-table
                :data="DataList"
                border
                :header-cell-style="{color:'#606266',fontWeight:700}"
                style="width: 100%">
                <el-table-column
                    prop="resourceId"
                    label="任务id"
                    >
                </el-table-column>
                <el-table-column
                    prop="name"
                    label="任务名称"
                    >
                </el-table-column>
               
                <el-table-column
                    label="任务状态">
                    <template slot-scope="scope">
                        <span
                            v-if="scope.row.type!='INFERENCE'"
                        >
                            {{scope.row.jobStatus=='0'?'招募中':scope.row.jobStatus=='3'?'训练完成':scope.row.jobStatus=='4'?'训练失败':'训练中'}}
                        </span>
                        <span v-if="scope.row.type=='INFERENCE'">
                            {{scope.row.jobStatus=='CREATED'?'已创建':scope.row.jobStatus=='CREATING'?'任务创建中':scope.row.jobStatus=='INFERENCE'?'推理中':'推理完成'}}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column
                    prop="partyId_dictText"
                    label="任务发起方"
                    >
                </el-table-column>
                <el-table-column
                    prop="content"
                    label="任务描述"
                    >
                </el-table-column>
                <el-table-column
                    prop="partyNums"
                    label="联邦方个数"
                    >
                </el-table-column>
                <el-table-column
                    prop="rowNUms"
                    label="数据条数"
                    >
                </el-table-column>
                <el-table-column
                    label="任务类型"
                >
                    <template slot-scope="scope">
                        <el-tag :type="scope.row.type=='INFERENCE'?'success':''">
                            {{scope.row.type=='INFERENCE'?'推理':'训练'}}
                        </el-tag>
                    </template>
                   
                </el-table-column>
                <el-table-column
                    prop="createTime"
                    label="更新时间"
                    >
                </el-table-column>
                <el-table-column
                    label="操作">
                    <template slot-scope="scope">
                        <el-button @click="gotoDetil(scope.row.id,scope.row.role,scope.row.type,scope.row.modelInfoId)"  type="text" size="small">查看详情</el-button>
                    </template>
                </el-table-column>
               
        </el-table>
        <!-- <el-card shadow="always" class="box-card">
            <div class="item"
                >
                <div class="main-header">
                    
                    <div class="main-title">
                        <span class="point"></span>
                        <el-tooltip class="item" effect="dark" :content="item.name" placement="top-start">
                            <span>{{item.name}}</span>
                        </el-tooltip>
                                        
                    </div>
                    <span v-if="item.type!='INFERENCE'" :class="item.jobStatus=='0'?'bgg':item.jobStatus=='3'?'bgf':item.jobStatus=='4'?'bgr':'bgb'"></span>
                    <span v-if="item.type!='INFERENCE'" :class="[item.jobStatus=='0'?'green':item.jobStatus=='3'?'finish':item.jobStatus=='4'?'red':'blue','status']">
                        {{item.jobStatus=='0'?'招募中':item.jobStatus=='3'?'训练完成':item.jobStatus=='4'?'训练失败':'训练中'}}
                    </span>
                    <span v-if="item.type=='INFERENCE'" :class="item.jobStatus=='CREATED'?'bgg':item.jobStatus=='COMPLETED'?'bgf':'bgb'"></span>
                    <span v-if="item.type=='INFERENCE'" :class="[item.jobStatus=='CREATED'?'green':item.jobStatus=='COMPLETED'?'finish':item.jobStatus=='INFERENCE'?'red':'blue','status']">
                        {{item.jobStatus=='CREATED'?'已创建':item.jobStatus=='CREATING'?'任务创建中':item.jobStatus=='INFERENCE'?'推理中':'推理完成'}}
                    </span>
                    <div class="time">更新于{{item.createTime.substring(0,10)}}</div>
                    <div class="part-info">
                            任务发起方：{{item.partyId_dictText}}
                        
                    </div>
                    <span class="role" :class="[item.jobType=='1'?'role':'hide']">{{item.jobType=='1'?'联邦':''}}</span>
                </div>
                <div class="main-middle">
                    <el-tooltip class="item" effect="dark" :content="item.content?item.content:'暂无描述'" placement="top-start">
                        <div class="desc">
                            {{item.content?item.content:'暂无描述'}}
                        </div>
                    </el-tooltip>
                    
                    <el-divider></el-divider>
                    <div class="data-box">
                        <el-tag>
                            {{item.type=='INFERENCE'?'推理':'训练'}}
                        </el-tag>
                        <span v-if="item.type!='INFERENCE'" class="artil">
                            {{item.partyNums}} 联邦方
                        </span>
                        <span  v-show="item.type!='INFERENCE'" class="data">
                            {{item.rowNUms?item.rowNUms:'0'}} 数据
                        </span>
                    </div>
                    
                </div>
            </div>
        </el-card>   -->
    </div>
       
</template>
<script>
// import imgs from '@/assets/avatar.png'
export default {
    props:{
        DataList: Array
    },
    data() {
        return {
            // imgs:imgs,
            // status: 3,
          
        }
    },
    created () {
    
    },
    methods: {
        gotoDetil(id,role,type,modelInfoId) {
            if(type == 'INFERENCE') {
                // 推理详情
                this.$router.push({
                    name: 'Tuili',
                    query: {
                        id: id,
                        modalId: modelInfoId
                    }
                    
                })
            } else {
                // 联邦任务详情
                this.$router.push({
                    name: 'MytaskDetail',
                    query: {
                        id: id,
                        role:role
                    }
                })
            }
            
        }
    }
}
</script>
