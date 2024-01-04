<template>
    <div class="one">
        <el-card shadow="always" class="box-card">
            <div class="item"
                >
                <div class="main-header">
                    <div class="main-title">
                        <el-tooltip class="item" effect="dark" :content="item.modelName" placement="top-start">
                            <span>{{item.modelName}}</span>
                        </el-tooltip>
                    </div>
                    <div class="sub-title">
                        <el-tag :class="flTypeStatus(item.flType)" :type="flTypeStatus(item.flType)" size="mini"> 
                            {{flTypeText(item.flType)}}
                        </el-tag>
                    </div>
                </div>
                <div class="main-middle">
                    <div class="desc-party">
                        模型提供方；{{item.partyName}}
                    </div>
                    <el-tooltip class="item" effect="dark" :content="item.modelDescribe?item.modelDescribe:'暂无描述'" placement="top-start">
                        <div class="desc">
                            {{item.modelDescribe?item.modelDescribe:'暂无描述'}}
                        </div>
                    </el-tooltip>
                   
                    <div class="main-bottom">
                        <el-divider></el-divider>
                        <div class="data-box">
                            <el-tooltip  
                                v-for="(item, index) in metricKey.slice(0,2)" 
                                :key="index" 
                                class="item" 
                                effect="dark" 
                                :content="metricDesc[index]?metricDesc[index]:'暂无说明'" 
                                placement="top-start">
                                <span class="artil"
                                    >
                                    {{metricKey[index].slice(0,1)}} : {{(metricValue[index]>1?metricValue[index].toFixed(2)+`%`:(metricValue[index]*100).toFixed(1)+`%`)}}
                                </span>
                            </el-tooltip>
                            <!-- <span class="artil"
                                    v-for="(item, index) in metricKey.slice(0,2)" 
                                    :key="index">
                                    {{metricKey[index].slice(0,1)}} : {{(metricValue[index]>1?metricValue[index].toFixed(2)+`%`:(metricValue[index]*100).toFixed(1)+`%`)}}
                            </span> -->
                        </div>
                    </div>
                    
                </div>
            </div>
        </el-card>      
    </div>

</template>
<script>
import imgs from '@/assets/avatar.png'
import FL_TYPE from '@/utils/global.js'
export default {
    props:{
        item: Object,
        paramsDesc: Array
    },
    data() {
        return {
            imgs:imgs,
            status: 3,
            metricKey:[],
            metricValue:[],
            metricDesc: []
        }
    },
    created () {
    },
    methods: {
        // 根据类型返回文字
        flTypeText(flType) {
            if(flType == FL_TYPE.TRANSVERSE) {
                // 横向模型
                return '横向'
            } else if (flType == FL_TYPE.DIRECTION) {
                // 纵向模型
                return '纵向'
            }
        },
        // 根据类型返回状态
        flTypeStatus(flType) {
            if(flType == FL_TYPE.TRANSVERSE) {
                // 横向模型
                return 'success'
            } else if (flType == FL_TYPE.DIRECTION) {
                // 纵向模型
                return 'primary'
            }
        }
    },
    watch: {
        item: {
            handler(val,oldValue) {
                let metricObj = JSON.parse(val.metric)
                for(let obj in metricObj) {
                   
                    this.metricKey.push(obj)
                    this.metricValue.push(metricObj[obj])
                }
                
            },
            deep: true,
            immediate: true
        },
        paramsDesc: {
            handler(val,oldValue) {
               
                let that = this
                val.forEach(item=>{
                    
                    if(that.metricKey.indexOf(item.name)!=-1) {
                        // 说明存在
                        let string = item.name + ':'+ item.describeCn
                        this.metricDesc[that.metricKey.indexOf(item.name)] = string
                        this.metricDesc.push(string)
                        // this.metricDesc.reverse()
                    }
                    
                })
               
                
         
            },
            deep: true,
            immediate: true
        }
    }
}
</script>
<style lang="scss" scoped>
.box-card{
    height: 260px;
    margin-bottom: 30px;
    border: none;
    padding:30px;
    box-sizing: border-box;
    cursor: pointer;
    ::v-deep .el-card__body{
        padding: 0;
    }
    .item{
        font-size: 20px;
        .main-header{
            position: relative;
            .time{
                font-size: 16px;
                color: #A7A7DB;
                margin-top: 5px;
                margin-bottom: 30px;
            }
            .main-title{
                display: flex;
                align-items: center;
                
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
            .sub-title{
                margin: 10px 0;
                .primary {
                    color: #409eff;
                    background: #ecf5ff;
                    border-color: #b3d8ff;
                }
            }
        }
        .main-middle{
            align-items: center;
            margin-top: 0px;
            .desc{
                margin-top: 20px;
                font-size: 14px;
                // height: 70px;
                
                color: #B8B8B8;
                display: -webkit-box;
                -webkit-box-orient: vertical;
                -webkit-line-clamp: 1;
                overflow: hidden;
            }
            .desc-party{
                font-size: 14px;
                display: -webkit-box;
                -webkit-box-orient: vertical;
                -webkit-line-clamp: 1;
                overflow: hidden;
                // height: 70px;
            }
            
            .data-box{
                font-size: 12px;
                .artil{
                    // width: 97px;
                    padding: 0 10px;
                    height: 33px;
                    line-height: 33px;
                    text-align: center;
                    background: #F6F6FB;
                    border-radius: 8px;
                    margin-right: 10px;
                    display: inline-block;
                }
                .artil:last-child{
                    margin-right: 0;
                }
                
            }
            
        }
    }
}
.one{
    width: 344px;
    height: 260px;
    // background-color: aqua;
    position: relative;
    margin: 0 60px;
    margin-bottom: 30px;
    }
    .one:before{
        content: "";
        width: 0;
        height: 0;
        position: absolute;
        top: 0;
        left: -59px;
        border-top: 130px solid transparent;
        border-bottom: 130px solid transparent;
        border-right: 60px #fff solid;
        border-left:none;
        border-radius: 5px;
    }
    .one:after{
        content: "";
        width: 0;
        height: 0;
        position: absolute;
        top: 0;
        left: 342px;
        border-top: 130px solid transparent;
        border-bottom: 130px solid transparent;
        border-right: none;
        border-left:60px #fff solid;
        border-radius: 5px;
    }
</style>

