<template>
    <div>
        <el-dialog
            title=" 请完善联邦方信息"
            :visible.sync="infoVisible"
            width="30%"
            :before-close='showClose'
            center>
            <div class="right">
                <h3 class="title">
                    邀请其他用户加入“{{firstName}}”联邦方：
                </h3>
                <div class="desc">
                    复制联邦方邀请码，对方可在“绑定联邦方”位置输入加入
                </div>
                <div class="btn">
                    <el-input placeholder="" disabled="" v-model="vcode">
                        <el-button @click="doCopy" type="primary" slot="append" size="mini">复制</el-button>
                    </el-input>
                </div>
                <div class="link">
                    <span @click="doCopyImg">点击复制“操作说明”，发给受邀人</span>
                    <a href=""></a>
                </div>
            </div>
        </el-dialog>
        <img-dialog @handleClickinImg='handleClickinImg'  :imgUrlImg='imgUrlImg' :visible.sync='imgShowVisible'></img-dialog>
    </div>
    
   
</template>
<script>
import { getTenantId ,getPartyInfo } from "@/utils/auth";
import { inviteCode } from '@/api/index'
import  imgDialog  from '@/components/imgDialog'
import  imgUrl  from  '@/assets/bindlbf.png'
export default {
    props: {
        visible: Boolean,
    },
    components: {
        imgDialog
    },
    data() {
        return {
            firstName: '',
            partInfo:[],
            vcode: '',
            imgUrlImg :imgUrl,
            imgShowVisible: false,
        }
    },
    computed: {
        infoVisible: {
            get() {
                return this.visible
            },
            set (val) {
                this.$emit('updateInfoVisible',val)
            }
        }
    },
    created() {
        this.partInfo = JSON.parse(getPartyInfo())
        this.firstName = this.partInfo[0].name
        this.createVode()
    },
    methods: {
        handleClickinImg(){
            this.imgShowVisible = false
        },
        async createVode() {
            let id = getTenantId()
            await inviteCode(id).then(res=>{
                if(res.data.success) {
                    this.vcode = res.data.result
                }
            }) 
        },
        doCopy() {
            this.$copyText(this.vcode).then(res=>{
                this.$message.success('已复制到粘贴板')
                this.$emit(
                    'handleClick',
                )
            })
            .catch(res=>{
                this.$message.error('复制失败') 
            })
        },
        // 复制图片
        doCopyImg() {
            // this.visible = false
            this.$emit(
                'handleClick',
            )
            this.imgShowVisible = true
            // this.$html2canvas(document.getElementById("img")).then(async (canvas)=>{
            //     const data = await fetch(this.imgUrlImg)
            //     const blob =  await data.blob()
            //     console.log(navigator.clpboard)
            //     // if(navigator.clpboard == undefined) {
            //     //     this.$message.error('请在https协议吓操作')
            //     // } else {
            //     //     this.$message.success('复制成功')
            //     // }
            //     await navigator.clipboard.write([
            //         new ClipboardItem({
            //             [blob.type]: blob
            //         })
            //     ])
            // })
        },
        showClose() {
            this.$emit(
                'handleClick',
            )
        },
    }
    
}
</script>
<style lang="scss" scoped>
.right{
    .desc{
        font-weight: 600;
    }
    .btn{
        margin-top: 20px;
        margin-bottom: 20px;
        ::v-deep .el-input-group__append, .el-input-group__prepend{
            // background-color: #409EFF;
            color: #fff;
            font-weight: 600;
        }
        ::v-deep .el-input__inner {
            background-color: #e8edfa;
        }
    }
    .link{
        span {
            text-decoration:underline;
            color: #409EFF;
            cursor: pointer;
            font-weight: 600;
        }
    }
}
</style>
