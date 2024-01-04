<template>
    <div>
        <iframe id="iframe" :src="iframeUrl" width= '100%' style="height: calc(100vh - 50px)" frameborder="0"></iframe>
    </div>
</template>
<script>

export default {
    data() {
        return {
            iframeUrl: 'http://joinus.life:10105/#/' //'http://7rxcoe4x.dongtaiyuming.net/ '
        }
    },
    mounted() {
        this.$nextTick(() =>{
            const _iframe = window.frames['iframe']
            // _iframe.addEventListener('load',true)
        })
       
    }
}
</script>
<style lang="scss" scoped>

</style>
