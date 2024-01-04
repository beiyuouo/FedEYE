<template>
    <el-card shadow="hover" class="box-card">
        <div class="img-box">
            <img src="@/assets/binding.png" class="img">
            <div>
                绑定联邦方后激活
            </div>
        </div> 
    </el-card>    
</template>
<style lang="scss" scoped>

        .box-card{
            margin-bottom: 30px;
            ::v-deep .el-card__body{
                padding: 0;
            }
            .img-box{
                // height: 236px;
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
</style>
