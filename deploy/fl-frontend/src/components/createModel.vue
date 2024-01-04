<template>
    <div>
        <el-dialog
            :title="title"
            :visible.sync="creatVisible"
            width="1200"
            :before-close='beforeCloseClick'
            :show-close='false'
            center
            >
            <el-form @submit.native.prevent ref="form" label-width="auto" :label-position="labelPosition" :model="form" :rules="rules">
                <el-form-item label="模型名称" prop="name">
                    <el-input clearable placeholder='请输入模型名称' v-model="form.name"></el-input>
                </el-form-item>
                <!-- <el-form-item label="是否公开" prop="resource">
                    <el-radio-group v-model="form.resource">
                        <el-radio label="2">公开</el-radio>
                        <el-radio label="1">秘密</el-radio>
                    </el-radio-group>
                </el-form-item> -->
                <el-form-item label="使用说明" prop="text">
                    <el-input
                        type="textarea"
                        placeholder="请输入使用说明"
                        v-model="form.text"
                        maxlength="300"
                        :rows="4"
                        show-word-limit
                        >
                    </el-input>
                </el-form-item>
                <el-form-item label="适用情况" prop="application">
                    <el-input clearable placeholder='请输入模型描述' v-model="form.application"></el-input>
                </el-form-item>
                <el-form-item label="模型介绍" prop="describe">
                    <el-input clearable placeholder='请输入模型介绍' v-model="form.describe"></el-input>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button class="save" type="primary"  @click="createTask">{{buttonText}}</el-button>
                <el-button class="cancle"  @click="beforeCloseClick">取消</el-button>
            </div>
        </el-dialog>
    </div>
</template>
<script>
import { getTenantId } from '@/utils/auth.js'
import { modalUpload , modalEdit} from '@/api/index'
export default {
    inject:['reload'],
    props: {
        visible: Boolean,
        explain: String,
        modalName: String,
        introduce: String,
        digType: String,
        application: String
    },
    data() {
        return {
            buttonText: '上传',
            labelPosition: 'left',
            title:'模型上传',
            id:'',
            modalId:'',
            form:{
               name:'',
               resource:'',
               text:'',
               describe: '',
               application:''
            },
            datatypeFlag: false,
            rules: {
                name: [
                    { required: true, message: '请输入任务名称', trigger: 'blur' },
                ],
                text: [
                    { required: true, message: '请输入使用说明', trigger: 'blur' }
                ],
                resource: [
                    { required: true, message: '请选择是否公开', trigger: 'change' }
                ],
                describe: [
                    { required: true, message: '请输入任务描述', trigger: 'blur' }
                ],
                application: [
                    { required: true, message: '请输入适用情况', trigger: 'blur' }
                ]
            },
           
        }
    },
    computed: {
        creatVisible: {
            get() {
                return this.visible
            },
            set (val) {
                this.$emit('updateJoinVisible',val)
            }
        }
    },
    watch: {
        digType(newValue) {
            if(newValue == 0) {
                this.title = '上传模型'
                this.buttonText = '上传'
            } else if(newValue == 1) {
                this.buttonText = '确定'
                this.title = '模型基本信息修改'

            }
        },
        explain(newValue) {
            this.form.text = newValue
        },
        modalName(newValue) {
            this.form.name = newValue
        },
        introduce(newValue) {
          
            this.form.describe = newValue
        },
        application(newValue) {
            this.form.application = newValue
        }
       
        
    },
    created() {
        this.modalId = this.$attrs.id
        this.id = getTenantId()
    },
    methods: {
         // 关闭dig
        beforeCloseClick() {
             this.$refs["form"].resetFields();
             this.$emit('cancleCreateClick')
             this.datatypeFlag = false
        },
         // 新建任务btn
        createTask() {
            this.$refs["form"].validate((valid) => {
                if (valid) {
                    if(this.digType == 0) {
                        // 上传
                        this.modalUpload()
                    } else if(this.digType == 1) {
                        // 更新信息
                        this.modalEdit()
                    }
                   
                } else {
                    return false;
                }
            });
            
        },
        // 上传模型
        async modalUpload() {
            let data = new URLSearchParams()
            data.append('describe',this.form.describe)
            data.append('id',this.modalId)
            data.append('instruction',this.form.text)
            data.append('name',this.form.name)
            data.append('permission','2')
            data.append('application',this.form.application)
            await modalUpload(data,this.id).then(res=>{
                if(res.data.code == 200) {
                    this.$message.success('上传模型成功')
                    this.reload()
                } else {
                    this.$emit('cancleCreateClick')
                    this.$message.error(res.data.msg)
                }
            })
            .catch(res=>{
                // console.log(res)
                // this.$emit('cancleCreateClick')
                // this.$message.error(res.msg)
            })
        },
        // 模型更新
        async modalEdit() {
            // let data = new URLSearchParams()
            let data = {
                modelDescribe: this.form.describe,
                id: this.modalId,
                instruction: this.form.text,
                modelName: this.form.name,
                
                application:this.form.application  // 模型适用情况
            }
            await modalEdit(data,this.id).then(res=>{
                if(res.data.code == 200) {
                    this.$message.success(res.data.result)
                    this.reload()
                } else {
                    this.$message.error(res.data.message)
                }
            })
            .catch(res=>{
                console.log(res)
                // this.$emit('cancleCreateClick')
                // this.$message.error(res.msg)
            })
        }
    }
}
</script>
