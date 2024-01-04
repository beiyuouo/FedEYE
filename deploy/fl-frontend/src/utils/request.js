
import axios from 'axios'
import { Message } from 'element-ui'

import store from '@/store'
import { getToken , getTenantId ,getRefreshToken} from '@/utils/auth'

// 创建一个axios实例
const request = axios.create({
    baseURL: process.env.VUE_APP_BASE_API,
    timeout: 60*1000*120
})
const request1 = axios.create({
    timeout: 60*1000*120
})
const  $message = Message
// 请求拦截器
// 请求拦截
request.interceptors.request.use(
    
    config => {
        // // 登录时用的token
        if (store.getters.token) {
            // 登录后用的token
            config.headers["X-Access-Token"] = getToken();
        } else {
            config.headers["X-Access-Token"] = "";
        }
        if (store.getters.refreshToken) {
            // 登录后用的token
            config.headers["refreshToken"] = getRefreshToken();
        } else {
            config.headers["refreshToken"] = "";
        }
        // if (getTenantId()) {
        //     config.headers["tenant_id"] = getTenantId();
        // }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// 响应拦截
request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.token) {
            return response;
        }  else if(res.code==500) {
            $message({
                message: res.message,
                type: "error",
                duration: 5 * 1000
            });
            return Promise.reject(res);
        }
        else {
            return response;
        }
    },
    error => {
       
        if (error.response) {
            
            switch (error.response.status) {
                case 401:
                    if (store.getters.token) {
                        $message({
                            message: "会话已超时，请重新登录",
                            type: "error",
                            duration: 1000,
                            // offset:50,
                            onClose() {
                                store.dispatch("user/logout").then(() => {
                                    location.reload();
                                });
                            }
                        });
                    } else {
                        router.push("/login");
                    }
                    break;
                default:
                    $message({
                        message: error.message,
                        type: "error",
                        duration: 5 * 1000
                    });
                    return Promise.reject(error);
            }
        }
    }
);
request1.interceptors.request.use(
    
    config => {
        // // 登录时用的token
        if (store.getters.token) {
            // 登录后用的token
            config.headers["X-Access-Token"] = getToken();
        } else {
            config.headers["X-Access-Token"] = "";
        }
        // if (getTenantId()) {
        //     config.headers["tenant_id"] = getTenantId();
        // }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// 响应拦截
request1.interceptors.response.use(
    response => {
        const res = response.data
        if (res.token) {
            return response;
        } 
        else if(res.code==500) {
            $message({
                message: res.message,
                type: "error",
                duration: 5 * 1000
            });
            return Promise.reject(res);
        }
        else {
            return response;
        }
    },
    error => {
        if (error.response) {
            
            switch (error.response.status) {
                case 401:
                    if (store.getters.token) {
                        $message({
                            message: "会话已超时，请重新登陆",
                            type: "error",
                            duration: 1000,
                            // offset:50,
                            onClose() {
                                store.dispatch("user/logout").then(() => {
                                    location.reload();
                                });
                            }
                        });
                    } else {
                        router.push("/login");
                    }
                    break;
                default:
                    $message({
                        message: error.message,
                        type: "error",
                        duration: 5 * 1000
                    });
                    return Promise.reject(error);
            }
        }
    }
);
export {request,request1}