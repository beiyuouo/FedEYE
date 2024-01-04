<template>
    <div class="joinCodeBox">
        <el-dialog
            :visible.sync="joinDialogVisible"
            width="20%"
            top='25vh'
            class="joincode"
            :before-close='showClose'
            :show-close='false'
            >
            <div class="diatitter">请输入邀请码</div>
            <div>
               <el-input v-model="code" placeholder="邀请码"></el-input>
            </div>
            <div  slot="footer" class="dialog-footer">
                <el-button  type="text" class="confirm" @click="joinCodeClick">确 认</el-button>
                <el-button type="text" class="cancle" @click="showClose">取 消</el-button>
            </div>
        </el-dialog>
    </div>
</template>
<script>
import { joinJob } from '@/api/task'
import { getTenantId } from "@/utils/auth";
export default {
    props: {
        visible: Boolean,
    },
    inject:['reload'],
    data(){
        return {
            code: ''
        }
    },
    methods: {
        // 邀请码的确认
        joinCodeClick() {
            let reg =/^[0-9]*$/
            if(!reg.test(this.code)) {
                this.$message.error('请输入正确数字')
            } else {
                this.joinJob()
                this.joinDialogVisible = false
                this.sendMsg = true
            }
            
        },
        showClose() {
            this.$emit(
                'handleClickJoinCode',
            )
        },
        // 通过邀请码加入任务
        async joinJob() {
            
            let id = getTenantId()
            if(this.code =='') {
                this.$message.error('请输入邀请码')
                return
            } else {
                let data = new URLSearchParams()
                data.append('inviteCode',this.code)
                await joinJob(data,id).then(res=>{
                    if(res.data.success) {
                        this.$message.success(res.data.message)
                        this.$router.push({
                            name: 'MytaskDetail',
                            query: {
                                id: res.data.result.recruitId,
                                role:res.data.result.role,
                                algid: '',

                            }
                        })
                        // this.reload()
                    }
                   
                })
                .catch(res=>{
                    
                })
            }
            
        }
    },
    computed: {
        joinDialogVisible: {
            get() {
                return this.visible
            },
            set (val) {
                this.$emit('updateInfoVisible',val)
            }
        }
    },
}
</script>
<style lang="scss" scoped>
.joincode{
    ::v-deep .el-dialog__header{
        padding: 0;
    }
    ::v-deep .el-dialog__footer{
        padding: 0;
        border-top: 1px solid #dcdcdc;
    }
    .confirm{
        border-right: 1px solid #dcdcdc;
    }
    .cancle{
        color: #000000;
    }
    .diatitter{
        margin-bottom: 20px;
        color: #484848;
        font-size: 18px;
        font-weight: 700;
    }
    .dialog-footer{
        text-align: center;
        
    }
    .el-button{
        width: 50%;
        margin: 0;
        padding: 20px;
    }
}
</style>
