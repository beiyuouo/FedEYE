<template>
    <div>
        <el-dialog
            :title="title"
            :visible.sync="creatEditVisible"
            width="1200"
            :before-close='beforeCloseClick'
            :show-close='false'
            center
            >
            <el-form @submit.native.prevent ref="form" label-width="auto" :label-position="labelPosition" :model="form" :rules="rules">
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
                <el-form-item label="模型介绍" prop="describe">
                    <el-input clearable placeholder='请输入模型介绍' v-model="form.describe"></el-input>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button class="save" type="primary"  @click="createTask">确定</el-button>
                <el-button class="cancle"  @click="beforeCloseClick">取消</el-button>
            </div>
        </el-dialog>
    </div>
</template>
<script>
import { getTenantId } from '@/utils/auth.js'
import { modalEdit} from '@/api/index'
export default {
    inject:['reload'],
    props: {
        visible: Boolean,
        explain: String,
        introduce: String,
    
    },
    data() {
        return {
            labelPosition: 'left',
            title:'模型基本信息修改',
            id:'',
            modalId:'',
            form:{
               text:'',
               describe: '',
               
            },
            datatypeFlag: false,
            rules: {
               
                text: [
                    { required: true, message: '请输入使用说明', trigger: 'blur' }
                ],
               
                describe: [
                    { required: true, message: '请输入任务描述', trigger: 'blur' }
                ]
            },
           
        }
    },
    computed: {
        creatEditVisible: {
            get() {
                return this.visible
            },
            set (val) {
                // this.$emit('updateJoinVisible',val)
            }
        }
    },
    watch: {
        explain(newValue,oldValue) {
            this.form.text = newValue
        },
       
        introduce(newValue,oldValue) {
            this.form.describe = newValue
        },
       
        
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
                        // 更新信息
                      this.modalEdit()
                } else {
                    return false;
                }
            });
            
        },
        // 模型更新
        async modalEdit() {
            // let data = new URLSearchParams()
            let data = {
                modelDescribe: this.form.describe,
                id: this.modalId,
                instruction: this.form.text,
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
                this.$message.error(res.data.message)
            })
        }
    }
}
</script>
