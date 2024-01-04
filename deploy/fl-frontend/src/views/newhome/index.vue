<template>
    <div class="home_box">
        <div class="home_title">
            Hi，{{userName}}
        </div>
        <div class="home_content">
            <div class="home_left">
                <div class="home_l_t">
                    <div class="home_l_t_l">
                        <utils :allAlgorithmObj='allAlgorithmObj'></utils>
                    </div>  
                    <div class="home_l_t_r">
                        <my-data :touristFlag='touristFlag'  :myDataObj='myDataObj'></my-data>
                    </div>    
                </div>
                <div class="home_l_b">
                    <my-task :touristFlag='touristFlag'></my-task>
                </div>
            </div>
            <div class="home_right">
                <hot-task></hot-task>
            </div>
        </div>
    </div>
</template>
<script>
import Utils  from './component/utils'
import { mapGetters } from 'vuex'
import MyData  from './component/myData'
import MyTask from './component/myTask'
import HotTask from './component/hotTask'
import {  mytask  , listCurrent ,queryPartyInfo ,listAllAlgorithm} from '@/api/index'
import { getUserInfo,getAuth, getTenantId , setPartyInfo, setIndex } from '@/utils/auth.js'
import { hotAlgorithm } from '@/api/task.js'
export default {
    components: {
        Utils,
        MyData,
        MyTask,
        HotTask
    },
    data() {
        return {
            touristFlag:false,
            authData: [],
            myDataObj: {},
            userName:'',
            listAllAlgorithmname:'',
            pagination: {
                offset: 0,
                total: 0,
                currentPage: 1,
                limit: 4
            },
            allAlgorithmObj: null
        }
    },
    computed: {
        ...mapGetters([
            'sidebar',
            {
                userInfo:'userInfo'
            }
        ])
      
    },
    created(){
       
        if(sessionStorage.getItem('tourist') == 'true'){
        
           this.$emit('name', '')
            // 游客登录状态
            this.touristFlag = true
            this.$emit('tourist',true)
            this.queryPartyInfo()
            JSON.parse(getAuth()).auth.map(item=>{
                this.authData.push(item.describe)
            })
        } else {
            let resdata =JSON.parse(getUserInfo())
            this.hotAlgorithm()
            this.userName = resdata.username
            this.$emit('name', sessionStorage.getItem('showname'))
            this.$emit('tourist',false)
            this.touristFlag = false
            // 正常登录状态
            setIndex('')
            this.listCurrent()
            this.mytask()
            // this.listAllAlgorithm()
            JSON.parse(getAuth()).auth.map(item=>{
                this.authData.push(item.describe)
            })
            this.$emit('header',true)
        }
        
        
        
    },
    methods: {
        // 查询热门算法
        async hotAlgorithm() {
            
            await hotAlgorithm(getTenantId()).then(res=>{
                this.allAlgorithmObj = res.data.result.slice(0,4)
            })
        },
         // 获取用户所属联邦方
        async queryPartyInfo() {
            await queryPartyInfo().then(res=>{
                if(res.data.success) {
                    // 用户所属联邦方
                    if(res.data.result == null ) {
                       sessionStorage.setItem("showname", '');
                    } else {
                        setPartyInfo(JSON.stringify(res.data.result))
                        if(res.data.result.length==1){
                            this.touristFlag = false
                            this.$emit('tourist',false)
                        }
                    }
                   
                    
                }
            })
            .catch(res=>{
                
            })
        },
        // 我的任务
        async mytask() {
            let params = {
                name: '',
                pageNo:'1',
                pageSize:'10'

            }
            let id = getTenantId()
            await mytask(params,id).then(res=>{
                this.myTaskData = res.data.result
                // 先判断新手任务是否存在，存在的话，我的任务最多显示2个
               
                if(this.newTaskNumber==6) {
                    // 新手任务存在
                    if(res.data.result.total>=1) {
                        this.myTaskNumber = 12
                    } else {
                        this.myTaskNumber = 6
                    }
                } else {
                    // 新手任务不存在
                    if(res.data.result.total>=2) {
                        this.myTaskNumber = 18
                    } else if(res.data.result.total==1) {
                        this.myTaskNumber = 12
                    } else {
                        this.myTaskNumber = 6
                    }
                }

            })
            .catch(res=>{
                
            })
        },
        // 我的数据
        async listCurrent() {
            let params = {
                name: '',
                pageNo:'1',
                pageSize:'10'

            }
            let id = getTenantId()
            await listCurrent(params,id).then(res=>{
                if(res.data.success) {
                    this.myDataObj = res.data.result  
                }
                
            }) 
            .catch(res=>{
                
            })
        },
        // 快捷工具
        async listAllAlgorithm() {
            let params = {
                name: this.listAllAlgorithmname,
                pageNo:this.pagination.currentpage,
                pageSize: this.pagination.limit
            }
            await listAllAlgorithm(params,getTenantId()).then(res=>{
                if(res.data.success) {
                    this.allAlgorithmObj = res.data.result
                }
            })
            .catch(res=>{
                
            })
        },
    }
}
</script>
<style lang="scss" scoped>
    .home_box {
        width: 100%;
        .home_title {
            height: 53px;
            line-height: 53px;
            margin-bottom: 48px;
            font-size: 40px;
        }
        .home_content {
            display: flex;
            .home_left {
                // width: 1125px;
                // flex: 1;
                margin-right: 64px;
                .home_l_t {
                    display: flex;
                    .home_l_t_l {
                        background-color: #fff;
                        border-radius: 5px;
                        width: 730px;
                        margin-right: 27px;
                    }
                    .home_l_t_r {
                        background-color: #fff;
                        border-radius: 5px;
                        width: 370px;
                    }
                }
                .home_l_b {
                    width: 1125px;
                    margin-top: 25px;
                    background-color: #fff;
                    border-radius: 5px;
                }
            }

            .home_right {
                // width: 433px;
                flex: 1;
                background-color: #fff;
                border-radius: 5px;
            }
        }
    }
</style>
