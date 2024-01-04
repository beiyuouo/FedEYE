<template>
    <div>
        <div  class="content-box">
            <div  class="content-box-top">
                <div v-if="jobType==1" class='left'>
                    <div class="title">
                        发起方
                    </div>
                    <el-card 
                        shadow="never"
                        @click.native='guestClick(items)'
                        class="box-card"
                        v-for="(items,index) in fljob.slice(0,1)"
                        :key="index">
                        <div :class="[items.envStatus==1?'status':'error']"></div>
                        <div class='name'>
                            {{items.partyName}}
                        </div>
                    </el-card>
                </div>
                <div v-if="jobType==1" class='middle'>
                    <div class="title">联邦方</div>
                    <div class='lbbox' v-if="fljob.slice(1).length!=0">
                        <el-card 
                            shadow="never"
                            class="box-card"
                            style="margin-right:10px;"
                            v-for="(items,index) in fljob.slice(1)"
                            @click.native='noGuestClick(items)'
                            :key="index">
                    
                            <div :class="[items.envStatus==1?'status':'error']"></div>
                            <div class='name' >
                                {{items.partyName}}
                            </div>
                        
                            
                        </el-card>
                    </div>
                    
                    <el-card shadow="never" class="box-card" v-else>
                        <div>
                            
                            <div class='name' >
                                暂无参与方
                            </div>
                        </div>
                        
                    </el-card>
                </div>
                <div  class='right' v-if="jobType==1">
                     <div  v-if="showStart" @click="invcodeJoinClick" class='right'>
                        <div  class="title">邀请联邦方</div>
                        <el-card shadow="never" class="box-card">
                        <img :src="img3" alt="">
                        </el-card>
                    </div>
                </div>
               
            </div>
            <div v-if="jobType==1" class="quote">
                <div></div>
            </div>
            <div class='img-box'>
                <span  class=" img-name">
                </span>
                 <div @click="changSfClick" class="suanfa-box">
                    <span v-if="showStart&&suanfaNameCn!=''">
                        <i class="el-icon-edit"></i>
                    </span>
                    <span>
                        {{suanfaNameCn}}
                    </span>
                </div>
                
            </div>
            <div class="icon-box">
                <i class="el-icon-bottom"></i>
            </div>
             <div class='img-box'>
                <div  class=" img-name"></div>
                <div class="name-box">  
                    {{taskname}}模型
                </div>
                <!-- <el-tooltip :content="taskname+'模型'" placement="bottom" effect="light">
                     
                </el-tooltip> -->
               
                
            </div>
        </div>
        <el-dialog
            :visible.sync="invcodeFlag"
            width="20%"
            class="success1"
            top='25vh'
         
            >
            <div class="right">
                <h3 class="title">
                    邀请其他用户加入该任务：
                </h3>
                <div class="desc">
                    复制任务邀请码，对方可在主页选择"加入任务"后输入邀请码加入
                </div>
                <div class="btn1">
                    <el-input placeholder="" disabled="" v-model="vcode">
                        <el-button @click="createVode" type="primary" slot="append" size="mini">复制</el-button>
                        <!-- <el-button type="primary" slot="append">复制</el-button> -->
                    </el-input>
                </div>
                <div class="link">
                    <span @click="copyImg">点击查看“加入任务说明”，保存发给受邀联邦方</span>
                    
                </div>
            </div>
            <div ref="foo">
                <img style="display:none" src="@/assets/jointask.png" alt="">
            </div>
        </el-dialog>
        <img-dialog @handleClickinImg='handleClickinImg'  :imgUrlImg='imgUrlImg' :visible.sync='imgShowVisible'></img-dialog>
        <change-sf :id='id' :algParams='algParams'  @cancleCreateClick='cancleCreateClick' :visible.sync='changeVisible'></change-sf>
    </div>
</template>

<script>
import img3 from '@/assets/newtask_nor.png'
import  imgUrl  from  '@/assets/jointask.png'
import  imgDialog  from '@/components/imgDialog'
import  changeSf from '@/components/changeSf'
import ChangeSf from '../changeSf.vue'
import { queryAlgById} from '@/api/index'
import { getTenantId } from "@/utils/auth";
export default {
    props: {
        guestClick: Function,
        noGuestClick: Function,
        fljob: Array,
        invcodeJoinClick: Function,
        comSuanfaname: String,
        taskname: String,
        showStart: Boolean,
        algorithmId: String,
        id: String,
        dataFalg: Boolean,
        suanfaNameCn: String,
        jobType: Number


    },
    components: {
        imgDialog,
        changeSf,
    },
    data() {
        return {
            img3:img3,
            imgShowVisible: false,
            imgUrlImg :imgUrl,
            invcodeFlag: false,
            vcode: '',
            changeVisible: false,
            algParams: ''
        }
    },
    
    methods: {
         // 算法查询
        async queryAlgById() {
            let id = getTenantId()
            let params = {
                id: this.algorithmId
            }
            await queryAlgById(params,id).then(res=>{
                if(res.data.code==200) {
                    this.algParams =res.data.result.params
                    this.changeVisible = true
                    // this.algObject = res.data.result
                    // let tempObj = JSON.parse(this.algObject.params)
                }
            })
        },
        // 新建任务弹框取消
        cancleCreateClick() {
            this.changeVisible = false
        },
        // 算法调参
        changSfClick() {
            if(this.showStart) {
                if(this.comSuanfaname=='') {
                    return
                } else  {
                    // 弹框 修改算法参数
                    // console.log(this.algorithmId)
                    // console.log(123)
                    this.queryAlgById()
                }
            } else {
                return
            }
            
            
        },
        handleClickinImg(){
          
            this.imgShowVisible = false
        },
         // 创建邀请码
        createVode() {
             this.$copyText(this.vcode).then(res=>{
                this.$message.success('已复制到粘贴板')
                this.invcodeFlag = false
            })
            .catch(res=>{
                this.$message.error('复制失败') 
            })
        },
        // 复制图片
        copyImg() {
            this.invcodeFlag = false
            this.imgShowVisible = true
        },
    }
}
</script>

<style lang="scss" scoped>
    .content-box{
        border: 1px solid #ccc;
        height: 450px;
        padding: 30px 40px;
        .content-box-top{
            display:flex;
            overflow: hidden;
            // height: 150px;
            .title{
                font-weight: 700;
            }
            .box-card{
                width: 104px !important;
                height: 104px !important;
                margin-top: 10px;
                .status{
                    background-color: green;
                    width: 16px;
                    height: 2px;
                }
                .error{
                    width: 16px;
                    height: 2px;
                    background-color: #F38383;
                }
                .name {
                    margin-top: 5px;
                    font-weight: 700;
                    font-style: 14px;
                }
                                
            }
            .left{
                flex-shrink: 0;
                margin-right: 50px;
            }
            .middle{
                flex: 1;
                flex-shrink: 0;
                height: 150px;
                overflow-x: auto;
                overflow-y: hidden;
                // overflow: auto;
                .lbbox{
                    display:flex;
                    overflow: auto;
                    height: 100%;
                    .box-card{
                        flex-shrink: 0;
                    }
                }
                margin-right: 50px;
            }
            .right{
                flex-shrink: 0;
              
                img{
                    width: 100%;
                }
            }
        }
        .img-box{
            text-align: center;
            // display: flex;
            // justify-content: center;
            // align-items: center;
            .name-box{
                     display: -webkit-box;
                    -webkit-box-orient: vertical;
                    -webkit-line-clamp: 1;
                    overflow: hidden;
                    font-weight: 700;
            }
            .suanfa-box{
                // display: flex;
                cursor: pointer;
                 display: -webkit-box;
                    -webkit-box-orient: vertical;
                    -webkit-line-clamp: 1;
                    overflow: hidden;
                font-weight: 700;
            }
            .img-name{
                width: 100px;
                height: 80px;
                display: inline-block;
                justify-content: center;
                align-items: center;
                // line-height: 80px;
                font-size: 12px;
                background-size: 100px 80px;
                cursor: pointer;
                // display: inline-block;
                background-image: url('../../assets/tool_bg_model.svg');
                span{
                   
                }
            }
            .active{
                background-image: url('../../assets/tool_btn_active_model.svg');
            }
            
        }
        .icon-box{
            text-align: center;
            // margin: 20px 0;
            ::v-deep .el-icon-bottom{
                font-size: 20px;
                color:blue;
            }
        }
        .quote {
            position: relative;
            width: 100%; 
            margin-top: 10px;
            height: 40px;
        }
        .quote::before, .quote::after, .quote ::before, .quote ::after {
            content: '';
            display: block;
            position: absolute;
            width: calc(50% - 20px);
            height: 20px;
            border-style: solid;
            border-color: blue;
            border-width: 0;
        }
        .quote ::before, .quote ::after {
            top: 0;
            border-bottom-width: 1px;
        }
        .quote::before, .quote::after {
            top: 20px;
            border-top-width: 1px;
        }
        .quote ::before {
            left: 0;
            border-bottom-left-radius: 20px;
        }
        .quote ::after {
            right: 0;
            border-bottom-right-radius: 20px;
        }
        .quote::before {
            left: 20px;
            border-top-right-radius: 20px;
        }
        .quote::after{
        right: 20px;
            border-top-left-radius: 20px
        }
    }
    .success1{
        ::v-deep .el-dialog__header{
            padding: 0;
        }
        .el-button{
                margin: 0;
            }
        .right{
            
            .desc{
                font-weight: 600;
            }
            .btn1{
                margin-top: 20px;
                margin-bottom: 20px;
                ::v-deep .el-input-group__append, .el-input-group__prepend{
                    // background-color: #9898F1;
                     padding: 0;
                    color: #fff;
                    font-weight: 600;
                }
                ::v-deep .el-input__inner {
                    background-color: #e8edfa;
                }
            }
            .link{
                span {
                    text-decoration:underline;
                    color: #409EFF;
                    cursor: pointer;
                    font-weight: 600;
                }
            }
        }
    }
</style>