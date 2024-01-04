<template>
  <div id="app">
    <router-view v-if="isRouterAlive" />
  </div>
</template>

<script>
export default {
  name: 'App',
  provide() {
    return {
      reload: this.reload
    }
  },
  data() {
    return {
      isRouterAlive : true
    }
  },
  
  methods: {
    reload() {
      this.isRouterAlive = false;
      this.$nextTick(function(){
        this.isRouterAlive = true
      })
    }
  }
}
</script>


<style lang="scss">
.inve-popover{
    padding: 0 !important;
}
   html{
     height: 100%;
   }
  body {
    margin: 0;
    padding: 0;
    height: 100%;
  }
  #app {
    font-family: Avenir, Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    // text-align: center;
    color: #2c3e50;
    // background: #f3f8fd;
    background-color: #f2f2f7;
    height: 100%;
  }
  .box-card {
    cursor: pointer;
    // height: 236px;
    ::v-deep .el-card__body {
      padding: 0;
    }
  }
  .el-form-item__error:before {
    display: inline-table !important;
    width: 16px !important;
    vertical-align: middle !important;
    content: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAAXNSR0IArs4c6QAAAVpJREFUOE+NkjFLAmEYgJ9vUQcXnRoUBMupH6Gbp4GFQo21uDSEIOgkKEEKwi21WD8gqEAHvUn0P9RkCIEONekgyEVwcfd1pwdK3vTed+/zfu+97yNYe4xUag+4Ao4wjH3gByE+gDa6fiv6/S87XdiBoSineL0PKIqfeBzCYflpMoHhEDRtwff3uej1XsxjC7SgQOCRWg2i0fUmVvF4DJUKzOc5ExZWex7PO6rqJxJZJZbLMq7X3XCxaN58YII3ZDJl8nn3Tem0fO923eetFnQ61yb4iqoeEovtBo5GUCi8CUNRlrTbPjye3cDlErLZxXawVJKFGg13QQfc1urm2YLT6rbhbJqqWWxtOHIdzabftcNNrZq7tNfxJ0COYPCJavV/AWazE6Fp7ZVyyeQZPt+9pVwiAaGQ/MvpFAYDqZyuXwhNe3aUc3yVkl8CxxiG1MiWHO5Er/dp5/4Cf2iUZRZaSIcAAAAASUVORK5CYII=) !important;
  }
  #app::-webkit-scrollbar,
  .digbox::-webkit-scrollbar,
  .el-dialog__body::-webkit-scrollbar,
  .form-scroll::-webkit-scrollbar,
  .el-table--scrollable-y::-webkit-scrollbar {
    width: 3px;
    height: 0px;
  }
  #app::-webkit-scrollbar-track,
  .digbox::-webkit-scrollbar-track,
  .el-dialog__body::-webkit-scrollbar-track,
  .form-scroll::-webkit-scrollbar-track,
  .el-table--scrollable-y::-webkit-scrollbar-track {
    background-color: transparent;
  }
  #app::-webkit-scrollbar-thumb,
  .digbox::-webkit-scrollbar-thumb,
  .el-dialog__body::-webkit-scrollbar-thumb,
  .form-scroll::-webkit-scrollbar-thumb,
  .header::-webkit-scrollbar-thumb,
  .el-table--scrollable-y::-webkit-scrollbar-thumb {
    background-color: rgba(102, 103, 104, 0.5);
  }
  #app::-webkit-scrollbar-button,
  .digbox::-webkit-scrollbar-button,
  .el-dialog__body::-webkit-scrollbar-button,
  .form-scroll::-webkit-scrollbar-button,
  .header::-webkit-scrollbar-button,
  .el-table--scrollable-y::-webkit-scrollbar-button {
    background-color: transparent;
    height: 3px;
  }
  #app::-webkit-scrollbar-corner,
  .el-dialog__body::-webkit-scrollbar-corner,
  .form-scroll::-webkit-scrollbar-corner,
  .header::-webkit-scrollbar-corner,
  .el-table--scrollable-y::-webkit-scrollbar-corner {
    background-color: transparent;
  }
  ::-webkit-scrollbar{
    width: 3px;
    height: 3px;
  }
  ::-webkit-scrollbar-thumb {
    background-color: rgba(102, 103, 104, 0.5);
  }
</style>
