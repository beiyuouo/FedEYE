<template>
  <div :class="classObj" class="app-wrapper">
    <sidebar class="sidebar-container" />
    <div class="main-container">
      <navbar :touristFlag='touristFlag'  :showHeader='showHeader' :firstName='firstName' />
      <app-main />
    </div>
  </div>
</template>

<script>
import { Navbar, Sidebar, AppMain } from './components'

export default {
    name: 'Layout',
    data() {
        return {
            showHeader: true,
            touristFlag: JSON.parse(sessionStorage.getItem('tourist')),
            firstName: ''
        }
    },
    components: {
        Navbar,
        Sidebar,
        AppMain
    },
    computed: {
        sidebar() {
        return this.$store.state.app.sidebar
        },
        classObj() {
        return {
            hideSidebar: !this.sidebar.opened,
            openSidebar: this.sidebar.opened,
            withoutAnimation: this.sidebar.withoutAnimation,
            mobile: this.device === 'mobile'
        }
        }
    },
    methods: {
        handleClickOutside() {
        this.$store.dispatch('app/closeSideBar', { withoutAnimation: false })
        }
    }
}
</script>

<style lang="scss" scoped>
    @import "~@/styles/mixin.scss";
    @import "~@/styles/variables.scss";

  .app-wrapper {
    @include clearfix;
    position: relative;
    height: 100%;
    width: 100%;
    display: flex;
    .sidebar-container{
      height: 100%;
      width: 200px;
    }
    .main-container{
      height: 100%;
      overflow: auto;
      flex: 1;
      padding: 0 30px;
      
    }
  }
  .drawer-bg {
    background: #000;
    opacity: 0.3;
    width: 100%;
    top: 0;
    height: 100%;
    position: absolute;
    z-index: 999;
  }
</style>
