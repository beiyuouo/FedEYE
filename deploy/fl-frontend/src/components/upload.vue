<template>
    <el-dialog
        v-loading="loading"
        element-loading-text="数据上传中"
        element-loading-spinner="el-icon-loading"
        element-loading-background="rgba(0, 0, 0, 0.8)"
        :title="title"
        :close-on-click-modal='false'
        :close-on-press-escape = 'false'
        center
        :show-close='false'
        :visible.sync="uploadVisible"
        width="1200"
        :before-close="beforeCloseClick">
        <div class="box">
            <el-upload
                class="upload-demo"
                :show-file-list='false'
                :headers="headers"
                :http-request="upLoadAvatarImg"
                :on-progress="uploadProcess"
                drag
                action='.zip'
                multiple>
                <img src="@/assets/upload.png" >
                <div class="el-upload__text">请点击上传本地数据文件或拖拽文件至从此处，文件需为<em>zip格式</em>且满足对应要求</div>
                <p @click.stop="descClick" class="desc">要求见：数据上传格式说明></p>
            </el-upload>
            <el-progress v-show="imgFlag" :percentage="percent"></el-progress>
        </div>
        
        
        <div class="btn">
            <el-button @click="beforeCloseClick">取消</el-button>
        </div>
    </el-dialog>
</template>
<script>
import { uploadData } from '@/api/index'  
export default {
    props: {
        uploadVisible: Boolean
    },
    data() {
        return {
            title: '添加数据集',
            percent: 0,
            imgFlag: false,
            uploadUrl: '',
            headers: {
                'Content-Type' : 'application/x-www-form-urlencoded'
            },
            loading: false
        }
    },
    created() {
        this.uploadUrl =  sessionStorage.getItem("uploadUrl");
   
        // this.uploadUrl = 'http://localhost:8080/uploadZip'
       
    },
    methods: {
        // 文件上传描述
        descClick() {
            window.open('https://thoughts.teambition.com/share/62dfa659e1ab8d0041d99ff0#title=%E8%81%94%E9%82%A6%E6%95%B0%E6%8D%AE%E4%B8%8A%E4%BC%A0%E6%A0%BC%E5%BC%8F%E5%AE%9A%E4%B9%89',"_blank")
        },
         // 关闭dig
        beforeCloseClick() {
            this.$emit('cancleCreateClick')
            this.imgFlag = false;
            this.percent = 0
        },
        upLoadAvatarImg(file) {
            let fileName = file.file.name.substring(file.file.name.lastIndexOf('.')+1)
            const extension = fileName === 'zip'
            if(!extension) {
                this.$message({
                    message: '上传文件只能是zip格式!',
                    type:'warning' 
                })
                return
            } else {
                this.loading = true
                const formData = new FormData();
                formData.append('file', file.file);
                // let Url = `http://${this.uploadUrl}/uploadZip`
                uploadData(formData,this.uploadUrl,function(progressEvent){
                    file.onProgress({percent: progressEvent.loaded / progressEvent.total *100})     
                }).then(res=>{
                    if(res.data.success) {
                        this.loading = false
                        let obj = JSON.parse(JSON.stringify(res.data.result))
                        this.$emit('uploadSuccess',obj)
                    }
                })
                .catch(res=>{
                    this.imgFlag = false;
                    this.loading = false
                    
                    // this.$message.error('数据上传失败' + res.message)
                })
            }
            
            

        },
        uploadProcess(event, file) {
            this.imgFlag = true;
            this.percent = Math.floor(event.percent);
        },
    },
    watch: {
        uploadVisible(newValue,oldValue) {
            if(!newValue) {
                this.percent = 0
                this.imgFlag = false
            }
        }
    }
}
</script>
<style lang="scss" scoped>
::v-deep .el-dialog__title{
    font-weight: 700;
}
::v-deep .el-dialog__body{
    padding: 30px 60px;
}
::v-deep .el-dialog__header{
    padding: 40px 0 0;
} 
::v-deep .el-progress{
    width: 50%;
    margin-left: 25%;
}  
.box{
    border: 1px solid #9A9A9A;
    height: 374px;
    width: 100%;
    
    .upload-demo{
        text-align: center;
        height: 90%;
        
        ::v-deep .el-upload{
            height: 100%;
            width: 100%;
        }
        ::v-deep .el-upload-dragger{
            height: 100%;
            width: 100%;
            border: none;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-direction: column;
        }
        .el-upload__text{
            margin-top: 30px;
            em{
                color: #0B80EA;
            }
        }
        .desc{
            margin-top: 30px;
            color: #0B80EA;
        }
    }
}
    
    .btn{
        text-align: center;
        margin-top: 40px;
        .el-button{
            width: 200px;
        }
    }
</style>
