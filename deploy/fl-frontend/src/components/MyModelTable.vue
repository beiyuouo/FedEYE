<template>
    <div>
        <el-table
                :data="allData"
                border
                 :header-cell-style="{color:'#606266',fontWeight:700}"
                style="width: 100%">
                <el-table-column
                    prop="resourceId"
                    label="模型id"
                    >
                </el-table-column>
                <el-table-column
                    prop="modelId"
                    label="模型名称"
                    >
                </el-table-column>
                <el-table-column
                    prop="partyName"
                    label="模型提供方"
                    >
                </el-table-column>
                <el-table-column
                    prop="modelDescribe"
                    label="模型描述"
                    >
                </el-table-column>
                <el-table-column
                    prop="metric"
                    label="模型性能"
                    >
                </el-table-column>
               
                <el-table-column
                    label="操作">
                    <template slot-scope="scope">
                        <el-button @click="gotoDetil(scope.row.id)"  type="text" size="small">查看详情</el-button>
                    </template>
                </el-table-column>
        </el-table>
    </div>
       
</template>
<script>
// import imgs from '@/assets/avatar.png'
export default {
    props:{
        allData: Array
    },
    data() {
        return {
            // imgs:imgs,
            // status: 3,
          
        }
    },
    created () {
    
    },
    methods: {
        gotoDetil(id) {
            this.$router.push({
                name:'modalDetail',
                query: {
                    id:  id,
                    from: 'myModal'
                }
            })
        }
    }
}
</script>
