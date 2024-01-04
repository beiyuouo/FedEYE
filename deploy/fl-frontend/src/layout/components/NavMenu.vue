<template>
    <div class="nav-box">
        
        <el-menu
            default-active="2"
            background-color="#545c64"
            text-color="#fff"
            active-text-color="#ffd04b"
            class="el-menu-vertical-demo"
            >
            <el-menu-item index="1">
                <i class="el-icon-menu"></i>
                <span slot="title">主页</span>
            </el-menu-item>
            <el-submenu index="2">
                <template slot="title">
                    <i class="el-icon-location"></i>
                    <span>我的</span>
                </template>
                <el-menu-item-group>
                    <el-menu-item index="1-1">
                         <i class="el-icon-menu"></i>
                        <span slot="title">我的任务</span>
                    </el-menu-item>
                    <el-menu-item index="1-2">
                        <i class="el-icon-menu"></i>
                        <span slot="title">我的数据</span>
                    </el-menu-item>
                </el-menu-item-group>
            </el-submenu>
            <el-menu-item index="3">
                <i class="el-icon-menu"></i>
                <span slot="title">工具池</span>
            </el-menu-item>
            <el-menu-item index="4">
                <i class="el-icon-menu"></i>
                <span slot="title">任务池</span>
            </el-menu-item>
            <el-menu-item index="5">
                <i class="el-icon-menu"></i>
                <span slot="title">数据池</span>
            </el-menu-item>
            <el-menu-item index="6">
                <i class="el-icon-menu"></i>
                <span slot="title">模型池</span>
            </el-menu-item>
        </el-menu>
    </div>
</template>
<script>
export default {
  
}
</script>
<style lang="scss" scoped>
    .nav-box{
        width: 200px;
        height: 100%;
        background-color: #fff;
        .el-menu-vertical-demo{
            margin-top: 200px;
        }
        ::v-deep .el-menu {
            height: 100%;
           
        }
    }
</style>
