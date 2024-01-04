<template>
    <div>
        <div class="block">
            <el-timeline>
                <el-timeline-item
                    v-for="(item) in resultData"
                    :key="(item.id)"
                    :timestamp="item.date" placement="top">
                    <el-card>
                    
                        <p class="text" v-html="item.comment.replace(/\n/g,'<br>')"></p>
                    </el-card>
                </el-timeline-item>
            </el-timeline>
            
        </div>
    </div>
</template>

<script>
    import {journal } from '@/api/index'
    export default {
        data() {
            return { 
                resultData: []
            }
        },
        created() {
            this.journal()
        },
        methods: {
            async journal() {
                await journal ().then(res=>{
                    if(res.data.code == 200) {
                        this.resultData = res.data.result
                    }
                    
                })
                .catch(err=>{

                })
            }
        }
    }
</script>

<style lang="scss" scoped>
.block{
    .text{
        font-size: 18px;
        line-height:1.5;
    }
}
</style>