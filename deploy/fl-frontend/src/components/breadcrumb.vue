<template>
    <div class="breadcrumb">
        <el-breadcrumb separator-class="el-icon-arrow-right">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item
                v-for="(item,index) in breadArray"
                :key="index"
                :to="item.url?{path:item.url}:''"
                >
                {{item.name}}
            </el-breadcrumb-item>
            
        </el-breadcrumb>
        <el-button @click='goBack' icon="el-icon-back" type="text">返回</el-button>
        <!-- <el-link type="primary" :underline="false">
            返回
        </el-link> -->
    </div>
</template>
<script>
export default {
    props:{
        breadArray: Array
    },
    created() {

    },
    methods: {
        goBack() {
            this.$router.go(-1)
        }
    }
}
</script>
<style lang="scss" scoped>
.breadcrumb{
    margin: 16px 0;
    display: flex;
    align-items: center;
    justify-content: space-between;
    ::v-deep .el-button {
        margin-right: 20px;
    }
}
</style>
