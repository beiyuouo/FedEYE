<template>
    <div class="diabox">
        <el-dialog
            title="修改算法参数"
            :visible.sync="changeVisible"
            width="1200"
            :before-close='beforeCloseClick'
            :show-close='false'
            :close-on-click-modal='false'
            center
            >
            <div class="radio-box">
                <el-radio-group v-model="radio">
                    <el-tooltip class="item" effect="dark" content="修改学习率和迭代率" placement="top-start">
                       <el-radio :label="0">简单模式</el-radio>
                    </el-tooltip>
                    <el-tooltip class="item" effect="dark" content="JSON编辑器进行JSON编辑" placement="top-start">
                        <el-radio :label="1">复杂模式</el-radio>
                    </el-tooltip>
                    
                </el-radio-group>
            </div>
            
            <el-form v-if="radio==0" @submit.native.prevent :label-position="labelPosition" ref="form" label-width="auto">
                <!--     原始版本 -----start -->
                <!-- <el-form-item 
                    v-for="(item,index) in form"
                    :key="index" 
                    
                    >
                    <span slot='label'>
                        <span>{{item.label}} </span>
                        <el-tooltip v-if="item.desc!=''" class="item" effect="dark" :content="item.desc" placement="right">
                            <i class="el-icon-question"></i>
                        </el-tooltip>
                    </span>
                   
                    <el-input clearable  v-model="item.value" ></el-input>
                </el-form-item> -->
                <!--    原始版本 -----end -->


                <!--     只有学习率和迭代次数的版本 -----start -->
                <el-form-item 
                    >
                    <span slot='label'>
                        <span>learning_rate</span>
                        <el-tooltip  class="item" effect="dark" content="学习率" placement="right">
                            <i class="el-icon-question"></i>
                        </el-tooltip>
                    </span>
                    <el-slider
                        v-model="useObj.learning_rate"
                        :step="0.0001"
                        show-stops
                        :marks='marks'
                        :max="0.01"
                        show-input>
                    </el-slider>
                </el-form-item>
                <el-form-item 
                    >
                    <span slot='label'>
                        <span>max_iter</span>
                        <el-tooltip  class="item" effect="dark" content="迭代次数" placement="right">
                            <i class="el-icon-question"></i>
                        </el-tooltip>
                    </span>
                    <el-slider
                            v-model="useObj.max_iter"
                            :min="2"
                            :max="10"
                            :marks = 'marks1'
                            :show-stops = true
                            show-input>
                    </el-slider>
                  
                </el-form-item>
                
                <!--     只有学习率和迭代次数的版本 -----end -->
                
                <!-- <span
                    v-for="(item,index) in form"
                    :key="index" >
                        <el-form-item v-if="item.value">
                            <span slot='label'>
                                <span>{{item.label}} </span>
                                <el-tooltip v-if="item.desc!=''" class="item" effect="dark" :content="item.desc" placement="right">
                                    <i class="el-icon-question"></i>
                                </el-tooltip>
                            </span>

                            <el-input type="textarea" clearable  v-model="item.value" ></el-input>
                        </el-form-item>
                        <el-collapse v-if='item.obj' >
                            <el-collapse-item :title="item.label" name="1">
                                <el-form-item
                                    v-for="(item1,index) in item.obj"
                                    :key="index">
                                    <span slot='label'>
                                        <span>{{item1.label}}</span>
                                        <el-tooltip v-if="item1.desc!=''" class="item" effect="dark" :content="item.desc" placement="right">
                                            <i class="el-icon-question"></i>
                                        </el-tooltip>
                                     </span>
                                     <el-input type="textarea" clearable  v-model="item1.value" ></el-input>
                                </el-form-item>
                            </el-collapse-item>
                        </el-collapse>
                </span> -->
                
            </el-form>
            <el-form  :rules="ruleValidate" v-if="radio==1" ref="form" :model="jsonObj" label-width="0px">
                <el-form-item label="">
                   <v-jsoneditor ref="editor" v-model="jsonObj.value" :options='options' :plus="true"  height='400px' ></v-jsoneditor>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button class="save" type="primary"  @click="createSfClick">确定</el-button>
                <el-button class="cancle"  @click="beforeCloseClick">取消</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
    import { getTenantId } from '@/utils/auth.js'
    import {update} from '@/api/task' 
    import VJsoneditor from 'v-jsoneditor'
    export default {
        props: {
            visible: Boolean,
            slectData: Array,
            typeList: Array,
            flType: String,
            algId: String,
            algParams: String,
            id: String,
        },
        components: {
            VJsoneditor
        },
        data() {
            const checkObj=(rule, value, callback)=>{
                try {
                if(JSON.parse(value.trim())){
                    callback()
                }   
                }catch (e) { 
                    callback('不是标准json')
                }
            } 
            return {
                radio: 0,
                ruleValidate: {
                    tempSource: [{ required: true, message: '必填项', trigger: 'blur' },{ validator:checkObj, trigger: 'blur' }]
                },
                options: {
                    mode: 'code'
                },
                form: [],
                algObject: {},
                labelPosition: 'left',
                dataAlgObj: {},
                describe: {},
                useObj: {
                    learning_rate: '',   // 学习率
                    max_iter: ''
                },
                marks:{
                    0.0001: "0.0001",
                    // 0.00025: "0.00025",
                    // 0.0005: "0.0005",
                    // 0.00075: "0.00075",
                    0.001: "0.001",
                    0.0025: "0.0025",
                    0.005: "0.005",
                    0.0075: "0.0075",
                    // 0.01: "0.01",
                },
                marks1:{
                    2: "2",
                    // 0.00025: "0.00025",
                    // 0.0005: "0.0005",
                    // 0.00075: "0.00075",
                    3: "3",
                    5: "5",
                    7: "7",
                    9: "9",
                    // 0.01: "0.01",
                },
                jsonObj: {
                    value: ''
                }
            }
        },
        computed: {
            changeVisible: {
                get() {
                    return this.visible
                },
                set (val) {
                    this.$emit('updateJoinVisible',val)
                }
            }
        },
        methods: {
           // 更新算法参数
            async update(jsonString) {
                let id = getTenantId()
                let data = {
                    parameters: JSON.stringify(jsonString),
                    id: this.id    // 任务id
                }
                await update(data,id).then(res=>{
                    if(res.data.code == 200 ) {
                        // this.$store.dispatch('app/toggleSideBar')
                        this.$message.success(res.data.message)
                        this.$emit('cancleCreateClick')
                        this.radio = 0
                        // this.reload()
                    }
                    
                })
                .catch(err=>{

                })
            },
            // 关闭dig
            beforeCloseClick() {
                // this.$refs["form"].resetFields();
                this.radio = 0
                this.$emit('cancleCreateClick')
               
            },
            // 修改算法参数
            createSfClick() {
                //判断哪个模式
                if(this.radio == 0) {
                    // 简单模式
                    this.form.forEach((item,index)=>{
                        // if(item.value) {
                        //     this.dataAlgObj[item.label] = JSON.parse(item.value) 
                        // } else if(item.obj) {
                        //     let copyObj = {}
                        //     item.obj.forEach(item1=>{
                        //         copyObj[item1.label] = JSON.parse(item1.value) 
                        //     })
                        //     this.dataAlgObj[item.label] = copyObj
                        // }
                        
                        this.dataAlgObj[item.label] = JSON.parse(item.value) 

                        
                    })
                    //   只有学习率和迭代次数的版本 -----start
                    this.dataAlgObj.max_iter = this.useObj.max_iter
                    if(this.dataAlgObj.hasOwnProperty('learning_rate')) {
                        this.dataAlgObj.learning_rate = this.useObj.learning_rate
                    } else if(typeof(this.dataAlgObj.optimizer)!='string') {
                    
                        this.dataAlgObj.optimizer['lr'] = this.useObj.learning_rate
                    }
        
                    //   只有学习率和迭代次数的版本 -----start
                
                    this.update(this.dataAlgObj)
                } else {
                    // 复杂模式
                    this.$refs.editor.editor.validate().then(res=>{
                        if(res.length== 0) {
                           this.update(this.jsonObj.value)
                        } else {
                            // 校验没通过
                            this.$message.error('请检查数据格式')
                        }
                    })
                 
                  
                }
              
            }
        },
        watch: {
            algParams: {
                // handler(newvalue) {
                //     this.form= []
                //     this.describe = {}
                //     this.algObject=JSON.parse(JSON.parse(JSON.stringify(newvalue))) 
                //     console.log(this.algObject)
                //     let object =JSON.parse(JSON.stringify(this.algObject))
                //     if(this.algObject.hasOwnProperty("describe")){
                //        // // 含有说明
                //         this.describe = object.describe
                //         Reflect.deleteProperty(object,'describe')
                //     }
                //     for(let item  in object) {
                        
                    
                //         if(typeof(object[item])=='object'){
                //             if(object[item] == null) {
                //                 if(this.describe.hasOwnProperty(item)) {
                //                     this.form.push({
                //                         'label':item,
                //                         'value':  JSON.stringify(object[item]),
                //                         'desc': JSON.stringify(this.describe[item])
                //                     }) 
                //                 } else {
                //                     this.form.push({
                //                         'label':item,
                //                         'value':  JSON.stringify(object[item]),
                //                         'desc': ''
                //                     }) 
                //                 }
                //             } else {
                //                 let array = []
                //                 for(let item1 in object[item]) {
                //                     array.push({
                //                         'label':item1,
                //                         'value':  JSON.stringify(object[item][item1])
                                    
                //                     })
                //                 }
                //                 if(this.describe.hasOwnProperty(item)) {
                                    
                //                     this.form.push(
                //                         item = {
                //                             'label':item,
                //                             'obj':  array,
                //                             'desc': JSON.stringify(this.describe[item])
                //                         }) 
                //                 } else {
                //                     this.form.push(
                //                         item = {
                //                             'label':item,
                //                             'obj':  array,
                //                             'desc': ''
                //                         }) 
                //                 }
                //             }
                            
                //         }  else {
                //             if(this.describe.hasOwnProperty(item)) {
                //                 this.form.push({
                //                     'label':item,
                //                     'value':  JSON.stringify(object[item]),
                //                     'desc': JSON.stringify(this.describe[item])
                //                 }) 
                //             } else {
                //                 this.form.push({
                //                     'label':item,
                //                     'value':  JSON.stringify(object[item]),
                //                     'desc': ''
                //                 }) 
                //             }
                //         }
                        
                        
                        
                //     }

                // },
                handler(newvalue) {
                    //   只有学习率和迭代次数的版本 -----start
                    if(JSON.parse(newvalue).hasOwnProperty('learning_rate')) {
                        // 含有学习率
                        this.useObj.learning_rate =JSON.parse(newvalue).learning_rate 
                        this.useObj.max_iter = JSON.parse(newvalue).max_iter
                        this.nums = JSON.parse(newvalue).learning_rate * 1
                    } else {
                        // 不含有学习率
                        if(typeof(JSON.parse(newvalue).optimizer)!='string') {
                            // debugger
                            this.nums = JSON.parse(newvalue).optimizer.lr * 1
                            this.useObj.learning_rate =JSON.parse(newvalue).optimizer.lr  
                            this.useObj.max_iter = JSON.parse(newvalue).max_iter
                        } else {
                            this.nums = 0.001
                            this.useObj.learning_rate = 0.001  
                            this.useObj.max_iter = JSON.parse(newvalue).max_iter
                        }
                    }
                     //   只有学习率和迭代次数的版本 -----end
                    this.form= []
                    this.describe = {}
                    this.algObject=JSON.parse(JSON.parse(JSON.stringify(newvalue))) 
                    let object = JSON.parse(JSON.stringify(this.algObject))
                    let copyObj = JSON.parse(JSON.stringify(this.algObject))
                    
                    if(this.algObject.hasOwnProperty("describe")){
                        // 含有说明
                        this.describe = object.describe
                        Reflect.deleteProperty(object,'describe')
                        Reflect.deleteProperty(copyObj,'describe')
                    } 
                    this.jsonObj.value = copyObj
                    for(let item  in object) {
                        if(this.describe.hasOwnProperty(item)) {
                            this.form.push({
                                'label':item,
                                'value':  JSON.stringify(object[item]),
                                'desc': JSON.stringify(this.describe[item])
                            }) 
                        } else {
                            this.form.push({
                                'label':item,
                                'value':  JSON.stringify(object[item]),
                                'desc': ''
                            }) 
                        }
                        
                        
                    }
                },
                // immediate: true,
                deep: true
            }
        }
    }
</script>

<style lang="scss" scoped>
.diabox{
    ::v-deep .el-dialog{
        // height: 75vh;
        overflow: auto;
    }
    .radio-box{
        text-align: center;
        margin-bottom: 30px;
    }
    ::v-deep.jsoneditor-menu{
        display: none;
    }
    ::v-deep .el-form-item__label-wrap {
        margin-left: 0px !important;
    }
    ::v-deep .max-btn{
        display: none !important;
    }
    ::v-deep .jsoneditor-statusbar{
        display: none;
    }
    .el-icon-question{
            color: #409EFF;
    }
    ::v-deep .jsoneditor-outer.has-main-menu-bar{
        padding: 0;
        margin: 0;
    }
}

</style>