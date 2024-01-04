<template>
    <div class="my-task-box">
        <div class="header"  slot='title'>
            <div class="title">
                <h3  @click="moData" class="sub-title">
                    我的数据
                </h3>
            </div>
            <div v-if="!touristFlag" v-show="!showMoreFlag" @click="moData" class="more">
                更多
            </div>
        </div>
        <div class="data-box" v-if="data&&!touristFlag">
                <div v-if="data.length != 0"> 
                    <div 
                        class="item"
                        v-for="item in data"
                        :key="item.id"
                        @click="gotoDataDetail(item.id,item.partyId)"
                        >
                        <div class="title">
                            <img align="absmiddle" class="img" src="@/assets/dataset.jpg" alt="">
                            {{item.name}}
                        </div>
                        <div class="fr">
                            
                            <div class="num-box">
                                <span class="num">{{item.rowsNum}}</span>
                                <span >数据</span>
                            </div>
                        </div>
                    </div>
                </div>    
                    
                <div v-if="data.length == 0&&!touristFlag" class="img-box"  >
                    <img src="@/assets/nodataset.png" class="img">
                        <div>
                            暂无数据
                        </div>
                </div>
                
            </div>
            <div v-if="touristFlag"  class="img-box">
                <img src="@/assets/binding.png" class="img">
                <div>
                    绑定联邦方后激活
                </div>
            </div>    
        <upload @uploadSuccess='uploadSuccess' @cancleCreateClick='cancleCreateClick' :uploadVisible='uploadVisible'></upload>
        <upload-step :successObj='successObj' @cancleCreateClickStep='cancleCreateClickStep' :uploadStepVisible='uploadStepVisible'></upload-step>
    </div>
</template>
<script>
import upload from '@/components/upload'
import uploadStep from '@/components/uploadDialog'
export default {
    props:{
        myDataObj: Object,
        touristFlag: Boolean
    },
    components: {
        upload,
        uploadStep
    },
    created () {
    },
    data() {
        return {
            showMoreFlag: false,
            data: [],
            uploadVisible: false,
            uploadStepVisible: false,
            successObj: null
        }
    },
    methods: {
        moData() {
            if(this.showMoreFlag){
                return 
            } else {
                if(!this.touristFlag){
                    this.$router.push({
                        name: 'Mydata'
                    })
                }
            }
            
            
        },
        cancleCreateClick(){
            this.uploadVisible = false;
        },
        cancleCreateClickStep() {
            this.uploadStepVisible = false
        },
        dataClick() {
            this.uploadVisible =  true
        },
        gotoDataDetail(id,partyId) {
            this.$router.push({
                name: 'DataDetail',
                query: {
                    id: id,
                    partyId:partyId,
                    from: 'Home'
                }
            })
        },
        uploadSuccess(obj) {
            this.uploadVisible = false
            this.uploadStepVisible = true
            this.successObj = obj
        }
    },
    watch: {
        myDataObj(newData,oldData) {
           
            if(newData.records.length>=4){
                this.data = newData.records.slice(0,4)
                this.showMoreFlag = false
            } else if (newData.records.length==0) {
                this.showMoreFlag = true
            } else {
                this.data = newData.records
                this.showMoreFlag = false
            }
            
        }
    }
}
</script>
<style lang="scss" scoped>
.my-task-box{
    padding: 30px;
    .header{
        cursor: pointer;
        display: flex;
        align-items: baseline;
        margin-bottom: 20px;
        .title{
            flex:2;
            color: #484848;
            font-size: 24px;
            font-weight:700;
            display: flex;
            align-items: center;
       
            h3{

                margin: 0;
            }
            ::v-deep .el-icon-plus{
                margin-left: 30px;
                font-weight: 700;
            }
            div{
                margin-right: 30px;
            }
            
        }
        .more{
            float: right;
            font-size: 16px;
            color: #B3B3BC;
            font-weight: 700;
        }
    }
    .img-box{
            // height: 236px;
        padding: 12px;
        display: flex;
        justify-content: center;
        flex-direction: column;
        align-items: center;
        .img{
            height: 100px;
        }
        div{
            color: #9a9a9a;
            font-size: 20px;
            font-weight: 700;
            margin-top: 14px;
        }
    }
    .sub-title{
        color: #484848;
        font-size: 24px;
        font-weight:700;
    }
    .item:hover{
        cursor: pointer;
        background-color: #DAE8F7;
    }
    
    .item{
        padding: 12px 0;
        display: flex;
        border-radius: 6px;
        .title{
            // float: left;
            flex: 1;
            font-size: 18px;
            font-family: Microsoft YaHei;
            font-weight: 600;
            color: #484848;
             overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
            .img{
                margin-right: 12px;
            }
        }
        .fr{
            // float: right;
            display: flex;
            .num-box {
                .num {
                    display: inline-block;
                    width: 50px;
                    height: 26px;
                    line-height: 26px;
                    // background: #E6E7FF;
                    border-radius: 40px;
                    text-align: center;
                    font-size: 16px;
                    font-family: Microsoft YaHei;
                    color: #B3B3BC;
                    font-size: 16px;
                    font-weight: bold;
                    margin: 0 5px;
                }
                span{
                    color: #B3B3BC;
                    font-size: 16px;
                    font-weight: bold;
                }
                
            }
            
        }
        
    }
    .hui{
        background: #FCFCFC;
        border: 1px solid #EEF4FE;
        .title{
            font-size: 18px;
            font-weight: normal;
            color: #9A9A9A;
        }
        
    }
    .nodata-box{
        display: flex;
        padding-top: 20px;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        div{
            margin-top: 10px;
            color: #9A9A9A;
        }
    }
    .box-card{
        height: 236px;
        padding: 18px;
        box-sizing: border-box;
        ::v-deep .el-card__body{
            padding: 0;
        }
        .data-box{
            // margin: 30px 30px;

            .img-box{
                // height: 236px;
                padding: 12px;
                display: flex;
                justify-content: center;
                flex-direction: column;
                align-items: center;
                .img{
                    height: 100px;
                }
                div{
                    color: #9a9a9a;
                    font-size: 20px;
                    font-weight: 700;
                    margin-top: 14px;
                }
            }
        }
        .img-box{
            // height: 236px;
            padding: 12px;
            display: flex;
            justify-content: center;
            flex-direction: column;
            align-items: center;
            .img{
                height: 100px;
            }
            div{
                color: #9a9a9a;
                font-size: 20px;
                font-weight: 700;
                margin-top: 14px;
            }
        }
        .add-and-join{
            display: flex;
            justify-content: center;
            .img-box{
                width: 50%;
            }
            .img-box:first-child{
                border-right: 1px solid #E7F1F9;
            }
        }
    }
    
}


</style>
