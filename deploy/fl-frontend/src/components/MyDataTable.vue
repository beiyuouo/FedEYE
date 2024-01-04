<template>
    <div>
        <el-table
            border
            :header-cell-style="{color:'#606266',fontWeight:700}"
            :data="tableData"
            style="width: 100%">
                <el-table-column
                    prop="resourceId"
                    label="数据id"
                    >
                </el-table-column>
                <el-table-column
                    prop="eggrollName"
                    label="数据名称"
                    >
                </el-table-column>
                
                <el-table-column
                    prop="createTime"
                    label="采集日期"
                    >
                </el-table-column>
                <el-table-column
                    prop="name"
                    label="文件名称"
                    width="180">
                </el-table-column>
                <el-table-column
                    prop="size"
                    label="文件大小">
                </el-table-column>
                <el-table-column
                    prop="contentType"
                    label="数据集类型">
                </el-table-column>
                <el-table-column
                    prop="rowsNum"
                    label="数据行数">
                </el-table-column>
                <el-table-column
                    label="操作">
                    <template slot-scope="scope">
                        <el-button @click="gotoDataDetail(scope.$index, scope.row)"  type="text" size="small">查看详情</el-button>
                    </template>
                </el-table-column>
        </el-table>
    </div>
       
</template>
<script>
// import imgs from '@/assets/avatar.png'
export default {
    props:{
        tableData: Array
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
         gotoDataDetail(row) {
           
            this.$router.push({
                name: 'DataDetail',
                query: { 
                    partyId: row.partyId,
                    id: row.id
                }
            })
        }
    }
}
</script>
