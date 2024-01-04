<template>
    <div 
        class="index-box"
        >
        <div class="header">
            <div class="title">选择哪个联邦身份：</div>
        </div>
       
        
        <div class="item">
            <el-row :gutter="100">
                <el-col
                    v-for="(item) in changePartData"
                    :key="item.id" 
                    :span='8'
                    >
                    <el-card   shadow="hover" class="box-card">
                        <h2 
                            v-loading.fullscreen.lock="fullscreenLoading"
                            element-loading-text="数据加载中"
                            element-loading-spinner="el-icon-loading"
                            element-loading-background="rgba(0, 0, 0, 0.8)"
                            @click="gotoHome(item.id,item.name,item.uploadUrl,item.nameEn)">
                            {{item.name}}
                        </h2>
                    </el-card>     
                </el-col>
            </el-row>
        </div>
         <div :class="[pagination.total<6?'showPage':'','block']">
                <el-pagination
                    background
                    @current-change="handleChangePage"
                    :current-page.sync="pagination.currentPage"
                    :page-size="pagination.limit"
                    layout="prev, pager, next"
                    :total="pagination.total">
                </el-pagination>
            </div>
    </div>
</template>
<script>
import {getFlUserPermission  , showFlUserRole } from '@/api/index.js'
import { getAuth, setAuth,setTenantId,getPartyInfo } from '@/utils/auth.js'
export default {
    data() {
        return {
            name: '',
            partData: [],
            pagination: {
                offset: 0,
                total: 0,
                currentPage: 1,
                limit: 4
            },
            fullscreenLoading: false
        }
    },
    computed: {
        changePartData() {
            
            let start = (this.pagination.currentPage - 1) * this.pagination.limit
            let end = this.pagination.currentPage  * this.pagination.limit
            return this.partData.slice(start, end)
        }
    },
    created() {
        this.queryPartyInfo()
        this.$emit('header',false)
        sessionStorage.setItem('tourist',false)
        // this.$emit('tourist',false)
    },
  
    methods: {
        handleChangePage(current) {
            this.pagination.currentPage = current;     
        },
         // 获取用户所属联邦方
        queryPartyInfo() {
            this.partData = JSON.parse(getPartyInfo())
            this.pagination.total = this.partData.length
            
        },
        // 获取用户权限
        async getFlUserPermission(id,name,uploadUrl,nameEn) {
            await getFlUserPermission(id).then(res=>{
                this.fullscreenLoading = false
                setAuth(JSON.stringify(res.data.result))
                this.Auth =JSON.parse(getAuth())
                sessionStorage.setItem('showname',name)
                sessionStorage.setItem("uploadUrl",uploadUrl);
                sessionStorage.setItem("nameEn", nameEn);
                // sessionStorage.setItem("userName",updateBy);
                this.$router.push({
                    name: 'newhome'
                })
                
            })
            .catch(res=>{
                
            })
        },
         // 获取用户角色及状态
        async showFlUserRole(id) {
            await showFlUserRole(id).then(res=>{
                
            })
            .catch(res=>{
                
            })
        },
        // 跳转至主页
        gotoHome(id,name,uploadUrl,nameEn) {
            this.fullscreenLoading = true;
            setTenantId(id)
            this.$emit('name',name)
            this.$emit('tourist',false)
            this.getFlUserPermission(id,name,uploadUrl,nameEn)
            this.showFlUserRole(id)
        }
    }
}
</script>
<style lang="scss" scoped>
.index-box{
    height: 100%;
    overflow: hidden;
    .header{
        margin: 30px 0 40px 0;
        display: flex;
        justify-content: space-between;
        align-items: center;
        .title{
            // margin: 30px 0 40px 0;
            // text-align: center;
            margin: 40px auto;
            font-size: 40px;
        }
    }
   
   .item{
       margin: 0 95px;
       overflow: hidden;
       height: 100%;
       .box-card{
           text-align: center;
           height: 200px;
           line-height: 200px;
           margin-bottom: 30px;
           h2{
               margin: 0;
           }
           ::v-deep .el-card__body{
                padding: 0 ;
            }
       }
   }
   
}
.block{
    text-align: right;
    margin: 40px 0;
}
.showPage{
    display: none
}
</style>
