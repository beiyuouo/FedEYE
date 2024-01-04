<template>
    <div class="ssl-box">
        <el-dialog
            title="证书安装说明"
            center
            :visible.sync="sslDialogVisible"
            :width="width||'30%'"
            v-if="sslDialogVisible"
            :before-close="closeClick">
            <!-- 自定义中间内容 -->
            <div class="desc-box">
                <div class="header">
                    <span>请下载对应电脑系统的证书软件</span>
                </div>
                <div class="content">
                    windows系统请下载.exe文件，Mac系统请下载.crt文件
                </div>
                <div class="footer">
                    <div class="left">
                        <img @click="downFile('aierssl.exe')" :src="ssl" alt="">
                    </div>
                    <div class="right">
                          <img @click="downFile('aier-ca.crt')" :src="crt" alt="">
                    </div>
                </div>
            </div>
            
        </el-dialog>

    </div>
</template>

<script>
import ssl from '@/assets/ssl.png'
import crt from '@/assets/crt.png'
import {downFile} from '@/api/index'
export default {
    // name: 'FlAierfederalAiErSSL',
    props: {
        visible: Boolean,
        title: String,
        width: String,
        noFooter: Boolean,

    },
    data() {
        return {
            ssl: ssl,
            crt: crt
        };
    },
    computed: {
        sslDialogVisible: {
            get() {
                return this.visible
            },
            set (val) {
                this.$emit('updateJoinVisible',val)
            }
        }
    },
    methods: {
        async downFile(url) {
            const a = document.createElement('a');
            a.href = './file/' + url;
            a.download = url;

            // 障眼法藏起来a标签
            a.style.display = 'none';
            // 将a标签追加到文档对象中
            document.body.appendChild(a);
            // 模拟点击了<a>标签,会触发<a>标签的href的读取,浏览器就会自动下载了
            a.click();
            // 一次性的,用完就删除a标签
            // a.remove();
        },
        closeClick() {
            this.$emit('closeClick')
        },
        cancelClick() {
            this.$emit('cancelClick')
        },
        saveClick() {
            this.$emit('saveClick')
        }
    },
};
</script>

<style lang="scss" scoped>
.ssl-box{
    .desc-box{
        .header{
            font-size: 16px;
            font-weight: 700;
        }   
        .content{
            font-size: 16px;
            font-weight: 700;
            margin-top: 10px;
        }
        .footer{
            display: flex;
            margin-top: 30px;
            justify-content: space-around;
            .left{
                img{
                    cursor: pointer;
                }
            }
            .right{
                img{
                    cursor: pointer;
                }
            }
        }
    }
}
</style>