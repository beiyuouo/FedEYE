<template>
    <div>
        <el-dialog
            :visible.sync="sendMsg"
            width="20%"
            top='25vh'
            class="codeSend"
            :show-close='false'
            >
            <div class="diatitter">成功加入任务"XXX"！任务发发出加入请求，任务负责人会尽快回复~</div>
            <div  slot="footer" class="dialog-footer">
                <el-button  type="text" class="confirm" @click="joinCodeClick">现在部署</el-button>
                <el-button type="text" class="cancle" @click="dialogVisible = false">取 消</el-button>
            </div>
        </el-dialog>
    </div>
</template>
<script>
export default {
    props: {
        visible: Boolean,
    },
    methods: {
        // 邀请码的确认
        joinCodeClick() {
            this.joinDialogVisible = false
            this.sendMsg = true
        },
        showClose() {
            this.$emit(
                'handleClickJoinCode',
            )
        },
    },
}
</script>
<style lang="scss" scoped>

</style>
