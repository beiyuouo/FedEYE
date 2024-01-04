<template>
    <div class="detail-container">
        <bread-crumb :breadArray='breadArray'></bread-crumb>
        <my-modal-detail :canConvertFlag="canConvertFlag" :id='id' v-if="myModalFlag" :usedNums='usedNums' :recentData='recentData' :usedUserList='usedUserList' :partyNums='partyNums' :flModelInfoObj='flModelInfoObj' :partyNameList='partyNameList' :rowNums='rowNums'></my-modal-detail>
        <modal-detail :canDeleteFlag='canDeleteFlag' :id='id' v-if="!myModalFlag" :recentData='recentData' :usedNums='usedNums' :usedUserList='usedUserList' :partyNums='partyNums' :flModelInfoObj='flModelInfoObj' :partyNameList='partyNameList' :rowNums='rowNums'></modal-detail>
    </div>
</template>
<script>
import BreadCrumb from '@/components/breadcrumb.vue'
import { getTenantId } from "@/utils/auth";
import { modelDetail , modelOpLog} from '@/api/index'
import MyModalDetail from './components/myDetail'
import modalDetail from './components/modelDetail'

export default {
    components:{
        BreadCrumb,
        MyModalDetail,
        modalDetail
    },
    inject:['reload'],
    data() {
        return {
           
         
            breadArray: [
            ],
            id: '',
            flModelInfoObj: {},
            partyNameList: [],
            rowNums:0,
            myModalFlag:false,
            usedUserList: [],
            usedNums: 0,
            partyNums: 0,
            canDeleteFlag: false,
            recentData: {},
            canConvertFlag: false
        }
    },
    created() {
       
        this.id = this.$route.query.id
        this.from = this.$route.query.from
        if(this.from == 'myModal') {
            this.myModalFlag = true
            this.breadArray = [
                {
                    name: '我的模型',
                    url: '/mymodal'
                },
                {
                    name: '模型详情'
                }
            ]
        } else {
            this.myModalFlag = false
            this.breadArray = [
                {
                    name: '模型池',
                    url: '/modal'
                },
                {
                    name: '模型详情'
                }
            ]
        }
      
        this.modelDetail()
        this.modelOpLog()
    },
    methods: {
        // 模型详情接口
        async modelDetail() {
            let id = getTenantId()
            let params = {
                id : this.id
            }
            await modelDetail(params,id).then(res=>{
                if(res.data.code == 200) {
                    this.flModelInfoObj = res.data.result.flModelInfo
                    this.partyNameList = res.data.result.partyNameList
                    this.rowNums = res.data.result.rowNums
                    this.canDeleteFlag = res.data.result.canDelete
                    this.canConvertFlag = res.data.result.canConvert
                    if(res.data.result.partyNums) {
                         this.partyNums = res.data.result.partyNums
                    }
                    if(res.data.result.usedNums) {
                          this.usedNums = res.data.result.usedNums

                    }
                    if(res.data.result.usedUserList) {
                          this.usedUserList = res.data.result.usedUserList
                    }

                    
               
                }
            })
        },
        // 模型的最近使用
        async modelOpLog() {
            let id = getTenantId()
            let data = new URLSearchParams()
            data.append('id',this.id)
            await modelOpLog(data,id).then(res=>{
                if(res.data.code == 200 ) {
                    this.recentData = res.data.result
                   
                }
            })
            .catch(err=>{

            })
        },
    }
}
</script>
<style lang="scss" scoped>
</style>
