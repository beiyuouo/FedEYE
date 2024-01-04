<template>
    <div>
        <el-dialog
            
            width="1200"
            :before-close='beforeCloseClick'
            :show-close='false'
            center
            title="新建推理任务"
            :visible.sync="tuiVisible"
            >
            <el-form @submit.native.prevent ref="form" :model="form" :rules="rules" label-width="auto">
                <el-form-item label="选择模型" prop="value">
                   <el-select v-model="form.value" placeholder="请选择模型">
                        <div 
                            class="minbox"
                            v-if="allData" 
                            v-infinite-scroll='loadMore'
                            infinite-scroll-delay='200'
                            >
                            <el-option v-for="(item,index) in allData "
                                @click.native="slectDataItem(item)"
                                :key="index"
                                :label="item.modelName"
                                :value="item.id"
                                >
                            </el-option>
                        </div>
                    </el-select>
                </el-form-item>
                <el-form-item label="任务名称" prop="name">
                    <el-input clearable placeholder='请输入任务名称' v-model="form.name"></el-input>
                </el-form-item>
                <el-form-item label="备注">
                    <el-input
                        type="textarea"
                        placeholder="请输入内容"
                        v-model="form.text"
                        maxlength="300"
                        :rows="4"
                        show-word-limit
                        >
                    </el-input>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button class="save" type="primary" :loading="loadingFlag"  @click="createTlTask">确定</el-button>
                <el-button class="cancle"  @click="beforeCloseClick">取消</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
    import { createJob } from '@/api/index'
    import {  getTenantId } from "@/utils/auth"
    import { ferenceQueryById } from '@/api/index'
    export default {
        props: {
            visible: Boolean,
            modalId: String,
            allData: Array
        },
        data() {
            return { 
                form: {
                    name: '',
                    text: '',
                    value: '',
                },
                rules: {
                    name: [
                        { required: true, message: '请输入任务名称', trigger: 'blur' },
                    ],
                    text: [
                        { required: true, message: '请输入任务描述', trigger: 'blur' }
                    ],
                    value: [
                        { required: true, message: '请选择模型', trigger: 'blur' }
                    ]
                },
                genId: '',
                loadingFlag: false
            }
        },
        created() {
            this.genId = getTenantId()
        },
        methods: {
            // 加载更多
            loadMore() {
                this.$emit('loadMoreData')
            },
             // 推理任务详情查询
            async ferenceQueryById() {
                let params = {
                    id: this.taskId
                }
                await ferenceQueryById(params, this.genId).then(res=>{
                    if(res.data.code == 200) {
                        if(res.data.result.flJobInference.status == 'CREATING') {
                            setTimeout(() => {
                                this.ferenceQueryById()
                            }, 1000);
                            
                        } else {
                            this.$router.push({
                                name: 'Tuili',
                                query: {
                                    id: this.taskId,
                                    modalId: this.modalId
                                }
                                
                            })
                        }
                        //INFERENCEING 推理中 COMPLETED推理完成  未开始 CREATED
                        // if(res.data.result.flJobInference.status == 'CREATED') {
                            
                        // } else if(res.data.result.flJobInference.status == 'INFERENCEING') {
                            
                        // } else {

                        // }
                    }
                })
                .catch(err=>{

                })
            },
            // 创建推理任务
            async createJob() {
                // let data = new URLSearchParams()
                // data.append('name',this.form.name)
                // data.append('id',this.modalId)
                let data = {
                    name: this.form.name,
                    id: this.form.value,
                    content: this.form.text
                }
                await createJob(data, this.genId).then(res=>{
                    if(res.data.code == 200) {
                        this.loadingFlag = false
                        this.taskId = res.data.result
                        this.$router.push({
                            name: 'Tuili',
                            query: {
                                id: this.taskId,
                                modalId: this.modalId
                            }
                            
                        })
                        // this.ferenceQueryById()
                        
                    } else {
                        this.$message.error(res.data.msg)
                        this.loadingFlag = false
                    }
                })
                .catch(err=>{
                    this.loadingFlag = false
                })
            },
            createTlTask() {
                this.$refs['form'].validate((valid) => {
                    if(valid) {
                        this.loadingFlag = true
                        this.createJob()
                    }
                })
                
            },
            beforeCloseClick() {
                this.$refs["form"].resetFields();
                this.$emit('cancleCreateTlClick')
            },
            // 选择模型
            slectDataItem(item) {
                
            }
        },
        computed: {
            tuiVisible: {
                get() {
                    return this.visible
                },
                set (val) {
                    // this.$emit('updateJoinVisible',val)
                }
            }
        },
    }
</script>

<style lang="scss" scoped>
    .dialog-footer { 
        .save {
            background: #9898F1;
        }
        .cancle {

        }
    }
    .minbox{
        height: 200px;
    }
</style>