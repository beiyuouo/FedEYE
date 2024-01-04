<template>
    <div class="common_box">
        <div class="main">
            <div class="titile">
               中国联通研究院-跨域可信联邦协同建模平台
            </div>
            <div class="desc">
               跨域可信联邦协同建模平台是一款面向联通场景中联邦训练的产品，解决了不同地区在数据不互通的情况下，共建模型的需求。目前已有北京、湖南、湖北等地的多家联通公司入驻使用。
            </div>
            <div class="img">
                <!-- <img src="@/assets/login-item.png" alt=""> -->
                <img src="@/assets/banner1.png" alt="">
            </div>
        </div>
        <div class="line">
        </div>
        <div class="footer">
            <div class="heade_titile">平台目前已有:</div>
            <div class="item">
                <el-row :gutter="25">
                    <el-col :span="8">
                        <el-card shadow="hover" class="box-card">
                            <div class="header">
                                <div class="img">
                                    <img src="@/assets/item.jpg" alt="">
                                </div>
                                <div>
                                    <div class="number">{{partyNum}}个</div>
                                    <div class="typename">联邦方</div>
                                </div>
                            </div>
                            <!-- <div class="desc-text">
                                关于联邦方的文字介绍文字描述及其他关于联邦方的文字介绍文字描述及其他，关于联邦方的文字介绍文字描述及其他文字描述关于。 
                            </div> -->
                        </el-card>
                    </el-col>
                    <el-col :span="8">
                        <el-card shadow="hover" class="box-card">
                            <div class="header">
                                <div class="img">
                                    <img src="@/assets/task1.jpg" alt="">
                                </div>
                                <div>
                                    <div class="number">{{jobNum}}个</div>
                                    <div class="typename">任务</div>
                                </div>
                            </div>
                            <!-- <div class="desc-text">
                                关于联邦方的文字介绍文字描述及其他关于联邦方的文字介绍文字描述及其他，关于联邦方的文字介绍文字描述及其他文字描述关于。 
                            </div> -->
                        </el-card>
                    </el-col>
                    <el-col :span="8">
                        <el-card shadow="hover" class="box-card">
                            <div class="header">
                                <div class="img">
                                    <img src="@/assets/data1.jpg" alt="">
                                </div>
                                <div>
                                    <div class="number">{{metaNum}}个</div>
                                    <div class="typename">数据集</div>
                                </div>
                            </div>
                            <!-- <div class="desc-text">
                                关于联邦方的文字介绍文字描述及其他关于联邦方的文字介绍文字描述及其他，关于联邦方的文字介绍文字描述及其他文字描述关于。 
                            </div> -->
                        </el-card>
                    </el-col>
                </el-row>
            </div>
        </div>
    </div>
</template>
<script>
import { loginNum } from '@/api/index.js'
export default {
    data() {
        return {
            jobNum: 0,
            metaNum: 0,
            partyNum: 0
        }
    },
    created() {
        this.loginNum()
    },
    methods: {
         //登录页接口查询
        async loginNum() {
            await loginNum().then(res=>{
                if(res.data.code == 200) {
                    this.jobNum = res.data.result.jobNum,
                    this.metaNum = res.data.result.metaNum ,
                    this.partyNum =  res.data.result.partyNum
                }
                
            })
            .catch(err=>{
             
            })
        }
    }
}
</script>
<style lang="scss" scoped>
// @media screen  and (min-width:1800px){
    .common_box{
        background-color: #f4f6f8;
        height: 100%;
        .main{
            margin: 70px 70px 0;
            border-radius: 10px;
            .titile{
                text-align: left;
                color: #484848;
                font-size: 50px;
                font-weight: 700;
                margin-bottom: 50px;
            }
            .desc{
                // text-indent:2em;
                line-height: 2;
                margin-bottom: 17px;
            }
            .img{
                text-align: center;
            }
            img{
                width: 100%;
            }
            
        }
        .line{
            border: 1px dashed  #CACACA;
            margin: 20px 60px 20px;
            // margin-bottom: 50px;
        }
        .footer{
            margin: 0px 60px 0;
            border-radius: 10px;
            .el-card{
                background: transparent;
                border: 2px solid #E8EEF4;
            }
            .heade_titile{
                text-align: center;
                font-size: 24px;
                font-weight: 700;
                color: #484848;
            }
            .item{
                margin-top: 40px;
                .box-card{
                    ::v-deep .el-card__body{
                        padding: 0 30px;
                    }
                    height:160px;
                    border-radius: 10px;
                    .header{
                        display: flex;
                        // align-items: center;
                        margin-top: 40px;
                        .img{
                            width: 80px;
                            height: 80px;
                            margin-right: 30px;
                            img{
                                width: 100%;
                            }
                        }
                        div{
                            .number{
                                font-size: 34px;
                                font-weight: bold;
                                color: #484848;
                            }
                            .typename{
                                font-size: 18px;
                                font-weight: bold;
                                color: #8E8E8E;
                                margin-top: 20px;
                            }
                        }
                        
                    }
                    .desc-text{
                        margin-top: 28px;
                        text-indent: 2em;
                    }
                }
            }
        }
    }
// }

// @media screen  and (max-width:1799px){
//     .common_box{
//         background-color: #f4f6f8;
//         height: 100%;
//         .main{
//             margin: 70px 70px 0;
//             border-radius: 10px;
//             .titile{
//                 text-align: left;
//                 color: #484848;
//                 font-size: 40px;
//                 font-weight: 700;
//                 margin-bottom: 40px;
//             }
//             .desc{
//                 // text-indent:2em;
//                 line-height: 2;
//                 margin-bottom: 17px;
//             }
//             .img{
//                 text-align: center;
//             }
//             img{
//                 width: 100%;
//             }
            
//         }
//         .line{
//             border: 1px dashed  #CACACA;
//             margin: 0px 60px 50px;
//             // margin-bottom: 50px;
//         }
//         .footer{
//             margin: 0px 60px 0;
//             border-radius: 10px;
//             .el-card{
//                 background: transparent;
//                 border: 2px solid #E8EEF4;
//             }
//             .heade_titile{
//                 text-align: center;
//                 font-size: 24px;
//                 font-weight: 700;
//                 color: #484848;
//             }
//             .item{
//                 margin-top: 40px;
//                 .box-card{
//                     ::v-deep .el-card__body{
//                         padding: 0 30px;
//                     }
//                     height:160px;
//                     border-radius: 10px;
//                     .header{
//                         display: flex;
//                         // align-items: center;
//                         margin-top: 40px;
//                         .img{
//                             width: 60px;
//                             height: 60px;
//                             // width: 100%;
//                             margin-right: 20px;
//                             img{
//                                 width: 100%;
//                             }
                        
//                         }
//                         div{
//                             .number{
//                                 font-size: 24px;
//                                 font-weight: bold;
//                                 color: #484848;
//                             }
//                             .typename{
//                                 font-size: 18px;
//                                 font-weight: bold;
//                                 color: #8E8E8E;
//                                 margin-top: 14px;
//                             }
//                         }
                        
//                     }
//                     .desc-text{
//                         margin-top: 28px;
//                         text-indent: 2em;
//                     }
//                 }
//             }
//         }
//     }   
// }
</style>
