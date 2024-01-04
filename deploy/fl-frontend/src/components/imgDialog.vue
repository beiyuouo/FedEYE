<template>
    <el-dialog
        :visible.sync="imgShowVisible"
        :before-close='showClose'
        :append-to-body='true'
        width='50%'
      
        center>
        <img width="100%" height="100%" :src="imgUrlImg" alt=""> 
    </el-dialog>
</template>

<script>
export default {
    props: {
        visible: Boolean,
        imgUrlImg: String
    },
    methods: {
        showClose() {
            this.$emit(
                'handleClickinImg',
            )
        },
    },
    computed: {
        imgShowVisible: {
            get() {
                return this.visible
            },
            set (val) {
                // this.$emit('updateImgVisible',val)
            }
        }
    },
}
</script>

<style>

</style>
