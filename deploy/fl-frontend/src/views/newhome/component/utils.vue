<template>
    <div class="box">
        <div class="header">
            <div class="title">热门算法</div>

        </div>
        <div class="content">
            <div v-for="(item,index) in allAlgorithmObj" 
                :key="index"
                @click="gotoDataDetail(item.id,item.flType)"
                class="item">
                <img src="@/assets/data1.jpg" alt="">
                <p>{{item.nameCn}}</p>
            </div>
            
        </div>
    </div>
</template>
<script>
export default {
    name: 'Utils',
    props: {
        allAlgorithmObj: Array
    },
    data() {
        return {
            allData: [],
        }
    },
    methods: {
        gotoDataDetail(id,flType) {
            this.$router.push({
                name: 'algorithmDetail',
                query: {
                    id:  id,
                    flType:flType,
                    from: 'Home'
                }
            })
        },
    },
    // watch: {
    //     allAlgorithmObj(newData,oldData) {
    //         this.allData = newData.records
    //     }
    // }
}
</script>
<style lang="scss" scoped>
.box{
    padding: 40px;
   .header{
       margin-bottom: 30px;
       .title{
           font-size: 27px;
           font-weight: 700;
           color: #3A4755;
       }
   }
    .content{
        display: flex;
        .item{
            margin-right: 30px;
            text-align:center;
            cursor: pointer;
            width: 140px;
            img{
                width: 100px;
                height: 100px
            }
            p{
               font-size: 20px;
               font-weight: 700;
                color: #3A4755;
                padding-left: 10px;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
            }
        }
   }
}
</style>
