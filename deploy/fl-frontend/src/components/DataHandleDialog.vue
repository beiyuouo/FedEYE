<template>
    <div class="diabox">
        <el-dialog
            title="预处理"
            :visible.sync="changeVisible"
            width="1200"
            :before-close='beforeCloseClick'
            :show-close='false'
            :close-on-click-modal='false'
            center
            >
            <el-form :model="ruleformPro"  ref="form" :inline="true" label-width="110px">
                <div v-for="(item, index) of ruleformPro.preproccessAlgorithmList" :key="index">
                    <el-row>
                        
                        <el-col :span="10">
                            <el-form-item
                                :rules="algorithmNameRules"
                                :prop="formItemProp(index,'algorithmName')"  
                                :label="'预处理算法' + indexNumber(index)">
                                <el-input
                                    :autosize="{ minRows: 4}"
                                    type="textarea" 
                                    v-model="item.algorithmName"  
                                    autocomplete="off"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="10">
                            <el-form-item
                                :rules="algorithmConfRules"
                                :prop="formItemProp(index,'algorithmConf')"
                                :label="'参数'+ indexNumber(index)">
                                <el-input 
                                    :autosize="{ minRows: 4}"
                                    type="textarea"   
                                    v-model="item.algorithmConf" 
                                    autocomplete="off"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="4">
                            <el-button  type="info" icon="el-icon-delete" @click.prevent="handleDel(index)">删除规则</el-button>
                        </el-col>
                    </el-row>
                    
                    
                    
                </div>
               
            </el-form>
            <div class="addRules">
                <el-button type="primary" class="addBtn" icon="el-icon-plus" @click="addDomain">新增规则</el-button>
            </div>
            <div slot="footer" class="dialog-footer">
                
                <el-button @click="confirmClick" class="save" type="primary" >保存</el-button>
                <el-button class="cancle"  @click="beforeCloseClick">取消</el-button>
                
            </div>
        </el-dialog>
    </div>
</template>

<script>
    import { getTenantId } from '@/utils/auth.js'
    import { addPreproccess } from '@/api/index.js'
    export default {
        props: {
            visible: Boolean,
            id: String,
        },
        data() {
            return {
                ruleformPro: {
                    preproccessAlgorithmList: [
                        {
                            algorithmName: "",
                            algorithmConf: "",
                        }
                    ]
                }      
            }
        },
        computed: {
            changeVisible: {
                get() {
                    return this.visible
                },
                set (val) {
                    this.$emit('updateJoinVisible',val)
                }
            },
            algorithmNameRules() {
                return {
                    required: true,
                    message: '预处理算法不能为空',
                    trigger: 'blur'
                }
            },
            algorithmConfRules() {
                return  {
                    required: true,
                    message: '预处理算法不能为空',
                    trigger: 'blur'
                }
            }
        },
        methods: {
            // 处理规则序号
            indexNumber(item) {
                return item+1
            },
            // props处理
            formItemProp(index,name) {
                return  'preproccessAlgorithmList.' + index + '.' + name
            },
            // 保存数据预处理算法
            confirmClick() {
                this.$refs["form"].validate((valid) => {
                    if (valid) {
                        let data = {
                            jobId: this.id,
                            preproccessAlgorithmList: this.ruleformPro.preproccessAlgorithmList
                        }
                        this.addPreproccess(data)
                    } else {
                        return false;
                    }
                });
                
             
            },
            // 数据预处理算法
            async addPreproccess(data) {
                await addPreproccess(data,getTenantId()).then(res=>{
                    if(res.data.code == 200) {
                        this.$message.success(res.data.result)
                        this.$emit('cancleCreateClick')
                        this.ruleformPro.preproccessAlgorithmList= [
                            {
                                algorithmName: "",
                                algorithmConf: "",
                            }
                        ]
                    } else {
                        this.$message.error(res.data.msg)
                    }
                })
                .catch(res=>{

                })
            },
            // 删除
            handleDel(index) {
                if(this.ruleformPro.preproccessAlgorithmList.length<=1) {
                    this.$message.warning('至少保留一条')
                    return
                } else {
                    this.ruleformPro.preproccessAlgorithmList.splice(index, 1);
                }
               
            },
            // 新增
            addDomain() {
                this.ruleformPro.preproccessAlgorithmList.push({
                    algorithmName: "",
                    algorithmConf: "",
                })
            },
            // 关闭dig
            beforeCloseClick() {
    
                this.$emit('cancleCreateClick')
                this.ruleformPro.preproccessAlgorithmList= [
                    {
                        algorithmName: "",
                        algorithmConf: "",
                    }
                ]
                this.$refs.form.clearValidate()
               
            },
        },
        watch: {
        }
    }
</script>

<style lang="scss" scoped>
.diabox{
    ::v-deep .el-dialog{
        // height: 75vh;
        overflow: auto;
    }
    ::v-deep .el-form-item__label-wrap {
        margin-left: 0px !important;
    }
    ::v-deep .el-form-item {
        display: flex;
    }
    ::v-deep .el-form-item__content{
        flex: 1;
    }
    ::v-deep .el-col-4{
        text-align: right;
    }
    .addRules{
        .addBtn{
            margin-left: 30px;
        }
    }
}

</style>