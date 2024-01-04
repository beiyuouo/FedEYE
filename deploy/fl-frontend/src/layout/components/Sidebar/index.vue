<template>
  <div>
        <div class="img-box">
            <img style="width:100%;"   v-if="classObj.openSidebar"   src="@/assets/lt-logo-big.png" alt="">
            <img style="width:100%;"   v-if="classObj.hideSidebar"    src="@/assets/lt-logo-s.png" alt="">
          <!-- <img style="width:50%;" src="@/assets/lt-logo.png" alt=""> -->
        </div>
        <el-menu
            :default-active="activeMenu"
            :collapse="isCollapse"
            :unique-opened="false"
            :collapse-transition="false"
           
            active-text-color="#e01622"
            mode="vertical"
        >
            <sidebar-item v-for="route in routes" :key="route.path" :item="route" :base-path="route.path" />
        </el-menu>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Logo from './Logo'
import SidebarItem from './SidebarItem'
import variables from '@/styles/variables.scss'

export default {
  components: { SidebarItem, Logo },
  computed: {
    ...mapGetters([
      'sidebar'
    ]),
    routes() {
      return this.$router.options.routes
    },
    classObj() {
        return {
            hideSidebar: !this.sidebar.opened,
            openSidebar: this.sidebar.opened,
        }
    },
    activeMenu() {
      const route = this.$route
      const { meta, path } = route
      // if set path, the sidebar will highlight the path you set
      if (meta.activeMenu) {
        return meta.activeMenu
      }
      return path
    },
  
    variables() {
      return variables
    },
    isCollapse() {
      return !this.sidebar.opened
    }
  }
}
</script>

<style lang="scss" scoped>
.img-box{
  text-align: center;
  background-color: #fff;
  padding: 0 15px;
}
::v-deep .el-menu {
    height: 100%;
    border-right: none;
    
}
::v-deep .el-submenu.is-active .el-submenu__title{
//    color: #fff !important;
}
::v-deep .el-menu-item.is-active{
  color: #e01622 !important;
}
::v-deep .el-menu.is-active{
  ::v-deep .el-submenu__title{
   
  }
    // height: 100%;
    // border-right: none;
    
}
</style>
