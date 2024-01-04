<template>
    <div class="navbar">
        <hamburger :is-active="sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />
        <div class="right-menu">
            <span class="question">
                <el-link href="https://m2e380twcw.feishu.cn/share/base/form/shrcnWPEJABokO375J2ZlbWu6ld" target="_blank" :underline="false">问题/需求反馈</el-link>
            </span>
            
            <span class="info">
                <el-popover
                    placement="bottom-end"
                    trigger="click">
                    <el-badge slot="reference"  :hidden='badgeFlag' :value="total"  class="notice item">
                        <i class="el-icon-bell"></i>
                    </el-badge>
                    
                    <div>
                        <div class="header">
                            <h3>全部通知</h3>
                            <!-- <div class="read" @click="readClick">一键已读</div> -->
                        </div>
                        <div  class="content-info">
                            <div class="item"
                                @click="readInfoClick(item.id,item.msgCategory,item.busType)"
                                v-for="(item,index) in infoNotice"
                                :key="index"
                                >
                                <div class="header-info">
                                    {{item.msgContent}}
                                </div>
                                <div v-if="item.msgCategory=='3'&&item.busType=='regist'" class="btn-box">
                                    <el-button size="medium" @click.stop="approveClick(1,item.busId,item.id)" round>同意</el-button>
                                    <el-button size="medium" @click.stop="notapproveClick(2,item.busId,item.id)" round>不同意</el-button>
                                </div>
                                <div class="info-time">
                                    {{item.sendTime}}
                                </div>
                            </div>
                            
                        </div>
                    </div>
                </el-popover>
            </span>
            <span @click="gotoDetail" class="journal">
                <el-badge  slot="reference"  :hidden='badgeFlag'  type="primary" class="notice item">
                        <svg-icon icon-class="help" />
                    </el-badge>
                <!-- <el-link @click="gotoDetail" :underline="false">更新日志</el-link> -->
            </span>
            <div class="center">
                <span class="title">{{showname}}</span>
                <span class="federal_box">
                    <el-popover
                        placement="bottom"
                        popper-class='inve-popover'
                        trigger="click"
                        v-model="visible1">
                        <div class="content_box">
                            <div class="one">
                                <h3>切换联邦方：</h3>
                                <div :class="[showScroll?'showHight':'','content-item']">
                                     <div avatar
                                        v-for="item in partInfo" 
                                        :key="item.id" 
                                        @click="choseitem(item)" 
                                        class="hospital-box">
                                        <img style="width:25px;height:25px;padding:5px 0;" :src="item.avatar==null||item.avatar==''?defaultImg:item.avatar.indexOf('http')!=-1?item.avatar:`${$url}`+item.avatar" alt="">
                                        <span>{{item.name}}</span>  
                                    </div>
                                </div>
                                   
                            </div>  
                            <div class="two">
                                <h3 @click="bandingClick">+ 绑定新联邦方</h3>
                            </div>
                        </div>
                        <div slot="reference">
                            <span class="hospital el-dropdown-link">
                                <span class="toogle">切换</span>
                            </span>
                        </div>
                    </el-popover>
                </span>
                <span class="federal_box invitation">
                    <el-popover
                        placement="bottom-end"
                        popper-class='inve-popover'
                        trigger="click"
                        @show='showPopover'
                        v-model="visible2">
                        <div class="content_box">
                            <div v-show="!touristFlag" v-if="showsuperuser" class="right">
                                <h3 class="title">
                                    邀请其他用户加入“{{showname}}”联邦方：
                                </h3>
                                <div class="desc">
                                    复制联邦方邀请码，对方可在“绑定联邦方”位置输入加入
                                </div>
                                <div class="btn">
                                    <el-input placeholder="" disabled="" v-model="vcode">
                                        <el-button @click="doCopy" type="primary" slot="append" >复制</el-button>
                                        <!-- <el-button type="primary" slot="append">复制</el-button> -->
                                    </el-input>
                                </div>
                                <div class="link">
                                    <span @click="doCopyImg">点击复制“操作说明”，发给受邀人</span>
                                    <a href=""></a>
                                </div>
                            </div>
                        </div>
                        <div slot="reference">
                            <span class="hospital el-dropdown-link">
                                <span class="toogle">邀请</span>
                            </span>
                        </div>
                    </el-popover>
                </span>
            </div>
            <el-dropdown placement='bottom-end' class="avatar-container" trigger="click">
                <el-avatar class="avatar" :src="imgs"></el-avatar>
                <el-dropdown-menu slot="dropdown" class="user-dropdown">
                    <el-dropdown-item  @click.native="logout">
                        <span >退出</span>
                        
                    </el-dropdown-item>
                    <el-dropdown-item  @click.native="changePsd">
                        <span >修改密码</span>
                        
                    </el-dropdown-item>
                </el-dropdown-menu>
            </el-dropdown>
           
        </div>
        <img-dialog @handleClickinImg='handleClickinImg'  :imgUrlImg='imgUrlImg' :visible.sync='imgShowVisible'></img-dialog>
        <dialog-lb @handleClickbd='handleClickbd' @joincode='joincode' @partyCheck='partyCheck' :visible.sync='bindlianbangVisible'></dialog-lb>
        <info-dialog @registerSuccess='registerSuccess' @handleClickinfo='handleClickinfo' :dialogName='dialogName' :visible.sync='infoVisible'></info-dialog>
        <join-dialog @gotoHome='gotoHome' @handleClickinjoin='handleClickinjoin' @continueClick='continueClick'  :partyInfoName='partyInfoName' :registerSuccessName='registerSuccessName' :registerSuccessFlag='registerSuccessFlag' :visible.sync='joinVisible'></join-dialog>
        <change-pass-word :name='name' @cancleCreateClick='cancleCreateClick' :visible.sync='passwordVisible'></change-pass-word>
    </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getUserInfo ,  getIndex, getTenantId ,getPartyInfo , getAuth, setAuth, setIndex,setTenantId, removeAuth , setPartyInfo ,setUserRole } from "@/utils/auth";
import {getFlUserPermission , queryPartyInfo ,showFlUserRole ,updatePassword} from '@/api/index.js'
import { inviteCode } from '@/api/index'
import { listByUserAndParty , editByAnntIdAndUserId } from '@/api/info'
import {checkRegist} from '@/api/task'
import  dialogLb from '@/components/dialogLb'
import  infoDialog from '@/components/infoDialog'
import  joinDialog  from '@/components/joinDialog'
import  imgDialog  from '@/components/imgDialog'
import changePassWord from '@/components/changePsd'
import  imgUrl  from  '@/assets/bindlbf.png'
import defaultImg from '@/assets/building.png'
import store from '@/store/index'
import Hamburger from '@/components/Hamburger'
import imgs  from '@/assets/person.png'
export default {
    data() {
        return {
            imgs:imgs,
            defaultImg:defaultImg,
            imgShowVisible: false,
            passwordVisible: false,
            imgUrlImg :imgUrl,
            notshowImg:false,
            name:"Mia",
            input2:"",
            visible1:false,
            visible2:false,
            showsuperuser: true,
            partInfo:[],
            showname:'',
            authData: [],
            showlbf: true,
            vcode: '',
            bindlianbangVisible: false,
            infoVisible: false,
            joinVisible: false,
            dialogName:'',
            registerSuccessName: '',
            registerSuccessFlag: false,
            infoNotice: [],
            total: 0,
            badgeFlag: true,
            partyInfoName: '',
            websock: null,
            lockReconnect:false,
            userId:'',
            showScroll: false,
        }
    },
    name: 'HeaderBar',
    props: {
        showHeader: Boolean,
        firstName: String,
        touristFlag: Boolean
    },
    inject:['reload'],
    components: {
        dialogLb,
        infoDialog,
        joinDialog,
        imgDialog,
        Hamburger,
        changePassWord
    },
    computed: {
        ...mapGetters([
            'sidebar',
            {
                userInfo:'userInfo'
            }
        ])
      
    },
    created() {
        this.partInfo = JSON.parse(getPartyInfo())
        if(this.partInfo.length>10) {
            this.showScroll = true
        }
       
        let resdata =JSON.parse(getUserInfo())
        
        this.name = resdata.username
        this.userId = resdata.id
        
        if(this.showname=='') {
            this.showname = sessionStorage.getItem('showname')
        }
        if(this.showHeader) {
            if(this.touristFlag){
               
            } else {
                
                this.listByUserAndParty()
            }
            
        }
    },
    mounted(){
        // this.initWebSocket()
    },
    methods: {
        gotoDetail() {
            this.$router.push({
                name: 'journal'
            })
        },
        cancleCreateClick() {
            this.passwordVisible = false
        },
        // 修改密码
        changePsd() {
            this.passwordVisible = true
        },
        toggleSideBar() {
            this.$store.dispatch('app/toggleSideBar')
        },
        // async logout() {
        //     await this.$store.dispatch("user/logout").then(() => {
        //         // var sevice = "http://"+window.location.host+"/";
        //         // var serviceUrl = encodeURIComponent(sevice);
        //         // window.location.href = window._CONFIG['casPrefixUrl']+"/logout?service="+serviceUrl;
              
        //         // location.reload();
        //         this.$router.push(`/login`)
        //     });;
            
        // },
        bandingClick() {
            this.visible1 = false
            this.bindlianbangVisible = true
            
        },
        reconnect() {
            var that = this;
            if(that.lockReconnect) return;
            that.lockReconnect = true;
            //没连接上会一直重连，设置延迟避免请求过多
            setTimeout(function () {
                console.info("尝试重连...");
                that.initWebSocket();
                that.lockReconnect = false;
            }, 5000);
        },
        initWebSocket: function () {
            // WebSocket与普通的请求所用协议有所不同，ws等同于http，wss等同于https
            var userId = this.userId;
            
            var url = process.env.VUE_APP_BASE_API.replace("https://","wss://").replace("http://","ws://")+"/websocket/"+userId;
            this.websock = new WebSocket(url);
            this.websock.onopen = this.websocketOnopen;
            this.websock.onerror = this.websocketOnerror;
            this.websock.onmessage = this.websocketOnmessage;
            this.websock.onclose = this.websocketOnclose;
        },
        websocketOnopen: function () {
    
            //心跳检测重置
            //this.heartCheck.reset().start();
        },
        websocketOnerror: function (e) {
          
            this.reconnect();
        },
        websocketOnmessage: function (e) {
            this.listByUserAndParty()
        
        },
        websocketOnclose: function (e) {
            console.log("connection closed (" + e + ")");
            if(e){
                console.log("connection closed (" + e.code + ")");
            }
            this.reconnect();
        },
        websocketSend(text) { // 数据发送
            try {
                this.websock.send(text);
            } catch (err) {
                console.log("send failed (" + err.code + ")");
            }
        },
        // 审核报名状态
        async checkRegist(id,registId,infoId) {
            let data = new URLSearchParams()
            data.append('checkStatus', id)
            data.append('registId', registId)
            let tenId = getTenantId()
            await checkRegist(data,tenId).then(res=>{
                if(res.data.success) {
                    this.editByAnntIdAndUserId(infoId)
                }
            })
            .catch(res=>{

            })
        },
        // 同意
        approveClick(id,registId,infoId){
            this.checkRegist(id,registId,infoId)
        },
        // 不同意
        notapproveClick(id,registId,infoId){
            this.checkRegist(id,registId,infoId)
        },
        // 复制图片
        doCopyImg() {
            this.visible2 = false
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
        //
        doCopy() {
            this.$copyText(this.vcode).then(res=>{
                this.$message.success('已复制到粘贴板')
            })
            .catch(res=>{
                this.$message.error('复制失败') 
            })
        },
        showPopover() {
            this.createVode()
        },
        async createVode() {
            let id = getTenantId()
            await inviteCode(id).then(res=>{
                if(res.data.success) {
                    this.vcode = res.data.result
                }
            }) 
        },
        // 系统消息
        async listByUserAndParty() {
            let params = {
                pageNo: '',
                pageSize: '',
                readStatus: 0
            }
            let id = getTenantId()
            await listByUserAndParty(params,id).then(res=>{
                if(res.data.success) {
                    this.infoNotice = res.data.result.records
                    this.total = res.data.result.total
                    if(this.total == 0 ){
                        this.badgeFlag = true
                    } else {
                        this.badgeFlag = false
                    }
                }
                
            })
            .catch(res=>{

            })
        },
        //信息点击
        readInfoClick(id,msgCategory,busType) {
            if(msgCategory=='3'&&busType=='regist') {
                return 
            } else {
                this.editByAnntIdAndUserId(id)
            }
            
        },
        // 更改消息已读
        async editByAnntIdAndUserId(id) {
            let params = {
                anntId: id
            }
            let tenId = getTenantId()
            await editByAnntIdAndUserId(params,tenId).then(res=>{
                if(res.data.success) {
                    this.listByUserAndParty()
                }
                
            })
        },
        choseitem(item) {
            setTenantId(item.id)
            sessionStorage.setItem("showname", item.name);
            sessionStorage.setItem("uploadUrl", item.uploadUrl);
            sessionStorage.setItem("nameEn", item.nameEn);
            // sessionStorage.setItem("userName",item.updateBy);
            this.getFlUserPermission(item.id)
            this.listByUserAndParty()
            this.showFlUserRole(item.id)
            this.$router.push({
                name: 'newhome',
                query: {
                    name:  item.name
                }
            })
            this.showname = item.name 
        },
            // 获取用户权限
        async getFlUserPermission(id) {
            await getFlUserPermission(id).then(res=>{
                this.visible1 = false
                this.visible2 = false
                setAuth(JSON.stringify(res.data.result))
                this.Auth =JSON.parse(getAuth()) 
                this.reload()
            })
            .catch(res=>{
                
            })
        },
            // 获取用户角色及状态
        async showFlUserRole(id) {
            
            await showFlUserRole(id).then(res=>{
                if(res.data.success) {
                    // 用户角色
                    setUserRole(JSON.stringify(res.data.result))
                }
            })
            .catch(res=>{
                
            })
        },

        handleClickbd() {
            this.bindlianbangVisible = false
        },
        handleClickinfo() {
            this.infoVisible = false
        },
        handleClickinjoin() {
            this.joinVisible = false
        },
        handleClickinImg(){
            this.imgShowVisible = false
        },
        bandingClick() {
            this.visible = false
            this.bindlianbangVisible = true
            
        },
        partyCheck(name) {
            this.bindlianbangVisible = false
            this.infoVisible = true
            this.dialogName = name
        },
        joincode(name) {
            this.bindlianbangVisible = false
            this.joinVisible = true
            this.registerSuccessFlag = false
            this.partyInfoName = name
        },
        registerSuccess(name) {
            this.infoVisible = false,
            this.joinVisible = true
            this.registerSuccessFlag = true
            this.registerSuccessName = name
        },
        // 继续绑定
        continueClick() {
            this.visible = false
            this.joinVisible = false
            this.infoVisible = false
            this.registerSuccessFlag = false
            this.bindlianbangVisible = true
        },
        //进入主页
        gotoHome(){
            this.visible = false
            this.joinVisible = false
            this.infoVisible = false
            this.registerSuccessFlag = false
            this.bindlianbangVisible = false
        },
        async logout() {
            await this.$store.dispatch('user/logout')
            this.$router.push(`/login`)
        }
    },  
    watch: {
        firstName(newValue,oldValue){
            this.showname = newValue
        },
        touristFlag(newValue,oldValue){
            
            if(!newValue) this.listByUserAndParty()
            immediate: true
        }
    },
}
</script>

<style lang="scss" scoped>
.el-popover{
    height: 50%;
    .content_box{
        // display: flex;
        // background: #F4F6F8;
        height: 50%;
        padding: 30px;
        box-sizing: border-box;
        .one{
            height: 50%;
            display: flex;
            text-align:left;
            flex-direction: column;
            overflow: hidden;
            height: 50%;
            h3{
                margin-top: 0;
                // height: 40px;
            }
            .hospital-box{
                margin-bottom: 20px;
                display: flex;
                align-items: center;
            }
            .hospital-box:hover{
                cursor: pointer;
                background-color: #DAE8F7;
            }
            div{
                // display: flex;
                // align-items: center;
                // height: 30px;
                // line-height:30px;
                span{
                    display: inline-block;
                    padding: 0 10px;
                }
            }
            .showHight{
                height: 300px;
            }
            .content-item{
                display: flex;
                text-align:left;
                flex-direction: column;
                flex-grow: 1; 
                overflow-y: auto;
            }
            ::v-deep .el-link{
                // justify-content: flex-start;
                .el-link--inner{
                    display: flex;
                    align-items: center;
                }
            }
        }
        .two{
            color: #409EFF;
            cursor: pointer;
            font-weight: 700;
            h3{
                margin: 0;
                color: #409EFF;
            }
        }
        
        .right{
            flex: 2;
            padding: 30px;
            box-sizing: border-box;
            .title{
                margin: 0;
            }
            .desc{
                // font-weight: 600;
                margin-top: 30px;
            }
            .btn{
                margin-top: 30px;
                margin-bottom: 30px;
                ::v-deep .el-input-group__append, .el-input-group__prepend{
                    // background-color: #409EFF;
                    color: #fff;
                    font-weight: 600;
                }
                ::v-deep .el-input__inner {
                    background-color: #e8edfa;
                    color: #000;
                }
            }
            .link{
                
                span {
                text-decoration:underline;
                // color: #409EFF;
                cursor: pointer;
                font-weight: 600;
                }
            }
        }
    }
}
.content-info{
    max-height: 500px;
    overflow: auto;
    .item{
        padding: 16px 12px 12px;
        cursor: pointer;
        white-space: normal;
        .header-info{
            font-size: 14px;
            line-height: 22px;
            color: #1f2329;
        }
        .btn-box{
            margin-top: 12px;
        }
        .info-time{
            margin-top: 12px;
            font-size: 12px;
            line-height: 20px;
            color: #646a73;
        }
    }
    .item:hover{
        cursor: pointer;
        background-color: #DAE8F7;
        .fr {
            .num-box{
                .num{
                    background-color: #F2F6FC;
                }
            }
        }
    }
}
.navbar {
  height: 60px;
  overflow: hidden;
  position: relative;
  margin-top: 20px;
//   box-shadow: 0 1px 4px rgba(0,21,41,.08);

  .right-menu {
    // float: right;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    height: 100%;
    line-height: 50px;
    .question {
        cursor: pointer;
        pointer-events:auto;
        ::v-deep .el-link.el-link--default{
            font-size: 16px;
            color: #000;
            cursor: pointer;
        }
    }
    .journal{
        margin-left: 20px;
        background-color: #fff;
        line-height: 40px;
        background-color: #fff;
        width: 50px;
        text-align: center;
        margin-right: 20px;
        border-radius: 10px;
        font-size: 20px;
        cursor: pointer;
    }
    .info{
        margin-left: 20px;
        background-color: #fff;
        line-height: 40px;
        background-color: #fff;
        width: 50px;
        text-align: center;
        margin-right: 20px;
        border-radius: 10px;
        font-size: 20px;
    }
    .center{
        height: 40px;
        line-height: 40px;
        background-color: #fff;
        border-radius: 10px;
        // width: 200px;
        margin-right: 20px;
        display: flex;
        // text-align: center;
        .title{
            margin-left: 20px;
            margin-right: 20px;
            font-size: 16px;
            font-weight: 700;
        }
        .federal_box{
           display: flex;
            font-weight: 700;
            font-size: 14px;
            cursor: pointer;
            .toogle{
                text-decoration:underline;
            }
        
        }
        .invitation{
            margin-left: 10px;
            margin-right: 10px;
        }
    }
    .avatar-container{
        display: flex;
        .avatar{
            margin-right: 10px;
            background-color: #fff;
        }
    }
    
    &:focus {
      outline: none;
    }

    
  }
  .hamburger-container {
    line-height: 46px;
    height: 100%;
    float: left;
    cursor: pointer;
    transition: background .3s;
    -webkit-tap-highlight-color:transparent;

    &:hover {
      background: rgba(0, 0, 0, .025)
    }
  }

  .breadcrumb-container {
    float: left;
  }
    
}
</style>
