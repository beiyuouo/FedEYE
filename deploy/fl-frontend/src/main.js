import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import '@/styles/index.scss'
import '@/icons'
import Axios from "axios"
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import "normalize.css/normalize.css"; // A modern alternative to CSS resets
import VueClipboard from 'vue-clipboard2'
import html2canvas from 'html2canvas'
import  '@/utils/flexible.js'
import SSO from '@/cas/sso.js'
Vue.config.productionTip = false
Vue.prototype.$axios = Axios
Vue.prototype.$html2canvas = html2canvas
Vue.prototype.$url = '/jeecg-boot/sys/common/viewpic?path='  //'http://112.230.202.198:30088/jeecg-boot'  // 'http://112.230.202.198:31825/'  // 10.201.18.253 http://47.104.248.182:31825   // http://112.230.202.198:31825/
import 'default-passive-events'
import '@/theme/index.css'
Vue.use(ElementUI);
Vue.use(VueClipboard)
import MODEL_DESC from '@/utils/modeldesc.js'
Vue.prototype.$MODEL_DESC = MODEL_DESC 
// new Vue({
//     router,
//     store,
//     render: h => h(App)
// }).$mount('#app')

// const keycloak = Keycloak({
//     url: 'http://192.168.66.1:8080',
//     realm: 'fids',
//     clientId: 'fl-platform'
// });
// Vue.prototype.$keycloak = keycloak;
// // Vue.use(html2canvas)
// // new Vue({
// //   router,
// //   store,
// //   render: h => h(App)
// // }).$mount('#app')
SSO.init(() => {
  main();
});
function main() {
  new Vue({
    router,
    store,
    render: h => h(App)
  }).$mount('#app')
}