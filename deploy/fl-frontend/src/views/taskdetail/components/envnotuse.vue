<template>
    <div class="env-box">
        <div class="box">
            <div class="box-card">
                <div class="header">
                    <div class="img">
                        <img src="@/assets/3.png" alt="">
                        <span class="sub-title">部署方法说明:</span>
                    </div>
                    <div class="text">
                        需要您联系IT人员或直接与我们IT人员联系，会帮您完成部署，联系电话：xxxxxxxxxxxx
                        如果您是IT人员：<span @click="gotoLink" class="link">请按文档步骤部署</span>,请按下列环境检测结果排查问题，待检测项全部绿色，即部署完毕。
                    </div>
                </div>
                <div class="title">
                    <div class="img">
                        <img src="@/assets/3.png" alt="">
                        <span class="sub-title">当前环境检测:</span>
                        <span class="red">{{titleFlag==true?'':'尚未部署成功，请排查标红项'}}</span>
                        <span class="btn">
                            <el-button @click="reloadClick" type="primary">重新检测</el-button>
                        </span>
                    </div>
                    
                </div>
                <div
                    class="itembox">
                    <div class="item">
                        <ul class="timeline">
                            <li class="timeline-item oneitem">
                                <div class="timeline-item__tail"></div>
                                <div class="timeline-item__node timeline-item__node--normal timeline-item__node--">
                                    <img src="@/assets/yingjian.png" alt="">
                                </div>
                                <div class="timeline-item__wrapper">
                                    <div  class="title-item">
                                        <span class="name">硬件：</span>
                                        <span class="status">{{titleFlag==true?'准备完成':'准备未完成'}}</span>
                                    </div>
                                </div>
                            </li>
                            <li v-for="(item,index) in hardWareData" :key="index" class="timeline-item oneitem">
                                <div class="timeline-item__tail"></div>
                                <div class="timeline-item__node timeline-item__node--normal timeline-item__node--">
                                    <img :src="item.success==0?img1:img2" alt="">
                                </div>
                                <div class="timeline-item__wrapper">
                                    <div class="content-item">
                                        {{item.name}}
                                    </div>
                                </div>
                            </li>
                        </ul>
                    </div>
                    <div class="item">
                        <ul class="timeline">
                            <li class="timeline-item oneitem">
                                <div class="timeline-item__tail"></div>
                                <div class="timeline-item__node timeline-item__node--normal timeline-item__node--">
                                    <img src="@/assets/ruanjian.png" alt="">
                                </div>
                                <div class="timeline-item__wrapper">
                                    <div  class="title-item">
                                        <span class="name">软件：</span>
                                        <span class="status">{{titleFlag==true?'准备完成':'准备未完成'}}</span>
                                    </div>
                                </div>
                            </li>
                            <li v-for="(item,index) in softWareData" :key="index" class="timeline-item oneitem">
                                <div class="timeline-item__tail"></div>
                                <div class="timeline-item__node timeline-item__node--normal timeline-item__node--">
                                     <img :src="item.success==0?img1:img2" alt="">
                                </div>
                                <div class="timeline-item__wrapper">
                                    <div class="content-item">
                                        {{item.name}}
                                    </div>
                                </div>
                            </li>
                            
                        </ul>
                    </div>
                </div>
                
            </div>
        </div>
    </div>
   
</template>
<script>
import { hardWareStatus , softWareStatus } from  '@/api/task'
import {  getTenantId } from "@/utils/auth";
import img1  from '@/assets/0.png'
import img2  from '@/assets/1.png'
export default {
    data() {
        return {
            breadArray: [{
                name: '环境部署'
            }],
            loading: true,
            partyName: '',
            hardWareData:[
            ],
            softWareData: [],
            img1: img1,
            img2: img2,
            title: '尚未部署成功，请排查标红项',
            titleFlag: true
        }
    },
    created() {
        this.hardWareStatus()
        this.softWareStatus()
    },
    methods: {
        // 部署文档页面
        gotoLink() {
            window.open("https://thoughts.teambition.com/share/614598f6cb08550041b6b48a#title=用户部署文档")
        },
        // 
        reloadClick() {
            this.hardWareStatus()
            this.softWareStatus()
        },
        // 硬件检测
        async hardWareStatus() {
            let id = getTenantId()
            this.hardWareData = []
            await hardWareStatus(id).then(res=>{
                if(res.data.result.length==0)this.titleFlag = true
                else this.titleFlag = false
                //  if(res.data.result.indexOf('Ready')!=-1) {
                //     this.hardWareData.push(
                //         {
                //             name: '网络检测',
                //             success: 1
                //         },
                //         {
                //             name: '可用内存检测',
                //             success: 1
                //         },
                //         {
                //             name: '磁盘空间检测',
                //             success: 1
                //         },
                //         {
                //             name: '进程压力检测',
                //             success: 1
                //         }
                //     )
                // } else {
                //     this.hardWareData.push(
                //         {
                //             name: '网络检测',
                //             success: 0
                //         },
                //         {
                //             name: '可用内存检测',
                //             success: 0
                //         },
                //         {
                //             name: '磁盘空间检测',
                //             success: 0
                //         },
                //         {
                //             name: '进程压力检测',
                //             success: 0
                //         }
                //     )
                // }
                if(res.data.result.indexOf('NetworkUnavailable')!=-1) {
                    this.hardWareData.push(
                        {
                            name: '网络检测',
                            success: 1
                        }
                    )
                }else{
                    this.hardWareData.push(
                        {
                            name: '网络检测',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('MemoryPressure')!=-1) {
                    this.hardWareData.push(
                        {
                            name: '可用内存检测',
                            success: 1
                        }
                    )
                }else{
                    this.hardWareData.push(
                        {
                            name: '可用内存检测',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('DiskPressure')!=-1) {
                    this.hardWareData.push(
                        {
                            name: '磁盘空间检测',
                            success: 1
                        }
                    )
                }else{
                    this.hardWareData.push(
                        {
                            name: '磁盘空间检测',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('PIDPressure')!=-1) {
                    this.hardWareData.push(
                        {
                            name: '进程压力检测',
                            success: 1
                        }
                    )
                }else{
                    this.hardWareData.push(
                        {
                            name: '进程压力检测',
                            success: 0
                        }
                    )
                }
            })
            .catch(res=>{
                
            })
        },
        // 软件检测
        async softWareStatus() {
            let id = getTenantId()
            this.softWareData = []
            await softWareStatus(id).then(res=>{
                if(res.data.result.length==0)this.titleFlag = true
                else this.titleFlag = false
                if(res.data.result.indexOf('python')!=-1) {
                    this.softWareData.push(
                        {
                            name: '联邦训练组件',
                            success: 1
                        }
                    )
                }else{
                    this.softWareData.push(
                        {
                            name: '联邦训练组件',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('board ')!=-1) {
                    this.softWareData.push(
                        {
                            name: '训练可视化组件',
                            success: 1
                        }
                    )
                }else{
                    this.softWareData.push(
                        {
                            name: '训练可视化组件',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('rollsite')!=-1) {
                    this.softWareData.push(
                        {
                            name: '联邦通信组件',
                            success: 1
                        }
                    )
                }else{
                    this.softWareData.push(
                        {
                            name: '联邦通信组件',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('clustermanager')!=-1) {
                    this.softWareData.push(
                        {
                            name: '集群管理组件',
                            success: 1
                        }
                    )
                }else{
                    this.softWareData.push(
                        {
                            name: '集群管理组件',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('nodemanager')!=-1) {
                    this.softWareData.push(
                        {
                            name: '节点管理组件',
                            success: 1
                        }
                    )
                }else{
                    this.softWareData.push(
                        {
                            name: '节点管理组件',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('minio')!=-1) {
                    this.softWareData.push(
                        {
                            name: '存储组件',
                            success: 1
                        }
                    )
                }else{
                    this.softWareData.push(
                        {
                            name: '存储组件',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('mysql')!=-1) {
                    this.softWareData.push(
                        {
                            name: '数据库组件',
                            success: 1
                        }
                    )
                }else{
                    this.softWareData.push(
                        {
                            name: '数据库组件',
                            success: 0
                        }
                    )
                }
                if(res.data.result.indexOf('fledge')!=-1) {
                    this.softWareData.push(
                        {
                            name: '边缘分析组件',
                            success: 1
                        }
                    )
                }else{
                    this.softWareData.push(
                        {
                            name: '边缘分析组件',
                            success: 0
                        }
                    )
                }
            })
            .catch(res=>{
                
            })
        },
        
    }
}
</script>
<style lang="scss" scoped>
.box{
    width: 100%;
    height: 100vh;
    .sub-title{
        color: #484848;
        padding: 20px;
        font-weight: 700;
    }
    .box-card{
        padding: 30px;
        height: 100%;
        .img{
            display: flex;
            align-items: center;
        }
        .text {
            line-height: 1.5;
        }
        .itembox{
            display: flex;
            margin-top: 40px;
            .item{
                // border: 1px solid #D2E4F5;
                flex: 1;
                padding: 20px;
                .title-item{
                    margin-left: 16px;
                    display: flex;
                    align-items: center;
                    img{
                        margin-right: 15px;
                    }
                    span{
                        font-size: 20px;
                        font-weight: 700;
                    }
                    
                }
            }
            .item:first-child{
                margin-right: 30px;
            }
        }
        .title{
            .red{
                padding: 0 10px;
                color: #FF4A4A;
                font-size: 16px;
            }
        }
        
    }
}
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
::v-deep .el-card__body{
    margin: 0 30%;
}
</style>
