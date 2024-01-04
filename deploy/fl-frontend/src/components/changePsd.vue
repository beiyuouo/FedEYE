<template>
    <div>
        <el-dialog
            title="修改密码"
            :visible.sync="passwordVisible"
            width="1200"
            :before-close='beforeCloseClick'
            :show-close='true'
            center
            >
            <el-form @submit.native.prevent ref="form" :model="form" :rules="rules" label-width="auto">
                <el-form-item label="旧密码" prop="oldPass">
                    <el-input  type="password" autocomplete="new-password" show-word-limit  clearable placeholder='请输入旧密码' v-model="form.oldPass"></el-input>
                </el-form-item>
                <el-form-item label="新密码" prop="pass">
                    <el-input  type="password" autocomplete="new-password" show-word-limit  clearable placeholder='请输入新密码' v-model="form.pass"></el-input>
                </el-form-item>
                <el-form-item label="确认新密码" prop="checkPass">
                    <el-input  type="password" autocomplete="new-password" show-word-limit  clearable placeholder='请再次输入新密码' v-model="form.checkPass"></el-input>
                </el-form-item>

            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button class="save" type="primary"  @click="changePsd">确定</el-button>
                <el-button class="cancle"  @click="beforeCloseClick">取消</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import { getTenantId ,} from '@/utils/auth.js'
import { updatePassword } from '@/api/index.js'
export default {
    inject:['reload'],
    props: {
        visible: Boolean,
        name: String,
    },
    data() {
        var validatePass = (rule, value, callback) => {
            if (value === '') {
                callback(new Error('请输入密码'));
            } else {
                if (this.form.checkPass!== '') {
                    this.$refs.form.validateField('checkPass');
                }
                callback();
                }
            }
        var validatePass2 = (rule, value, callback) => {
            if (value === '') {
                callback(new Error('请再次输入密码'));
            } else if (value !== this.form.pass) {
              
                callback(new Error('两次输入密码不一致!'));
            } else {
                callback();
            }
        }
        return {
            form:{
               oldPass:'',
               pass: '',
               checkPass: ''
            },
            rules: {
                oldPass: [
                    { required: true,trigger: 'blur', message: '密码不能为空'}
                   
                    
                ],
                pass: [
                    { validator: validatePass, trigger: 'blur' },
                    { required: true,trigger: 'blur'},
                    { pattern: /^(?=.*[0-9])(?=.*[A-Za-z])(?=.*\W)(?!.* ).{8,16}$/ ,message: '至少8位字符，数字、大小写和特殊字符'}
                ],
                checkPass: [
                    { validator: validatePass2, trigger: 'blur' },
                    { required: true,trigger: 'blur'},
                    { pattern: /^(?=.*[0-9])(?=.*[A-Za-z])(?=.*\W)(?!.* ).{8,16}$/ ,message: '至少8位字符，数字、大小写和特殊字符'}
                ]
            },
        }
    },
    created() {
      
    },
    computed: {
        passwordVisible: {
            get() {
                return this.visible
            },
        }
    },
    methods: {
        // 修改密码
        async updatePassword() {
            let params = {
                username: this.name,
                oldpassword: this.form.oldPass,
                password: this.form.pass,
                confirmpassword: this.form.checkPass
            }
            await updatePassword(params).then(res=>{
                if(res.data.code == 200) {
                    this.$emit('cancleCreateClick')
                    this.$message.success('修改密码成功')
                }
            })
            .catch(err=>{

            })
        },
        //修改密码
        changePsd() {
            this.updatePassword()
        },
        handleClick() {

        },
        //
         // 关闭dig
        beforeCloseClick() {
            this.$refs["form"].resetFields();
            this.$emit('cancleCreateClick')
        },
    }
    
}
</script>

<style lang="scss" scoped>
</style>
