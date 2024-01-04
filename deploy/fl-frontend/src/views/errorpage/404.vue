<template>
  <div class="wscn-http404-container">
    <div class="wscn-http404">
      <div class="pic-404">
        <img class="pic-404__parent" src="@/assets/404_images/404.png" alt="404">
      </div>
      
    </div>
  </div>
</template>

<script>

export default {
  name: 'Page404',
  computed: {
    message() {
      return 'The webmaster said that you can not enter this page...'
    }
  }
}
</script>

<style lang="scss" scoped>
.wscn-http404-container{
//   transform: translate(-50%,-50%);
//   position: absolute;
//   top: 40%;
//   left: 50%;
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
}
.wscn-http404 {
//   position: relative;
//   width: 1200px;
//   padding: 0 50px;
//   overflow: hidden;
  .pic-404 {
    // position: relative;
    // float: left;
    // width: 600px;
    // overflow: hidden;
    image{
         width: 600px;
    }
  

  }

}
</style>