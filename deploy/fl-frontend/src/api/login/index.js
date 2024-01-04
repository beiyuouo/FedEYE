import {request} from "@/utils/request";


// 获取图形验证码
export function getCaptcha(key) {
  return request({
    url: '/sys/randomImage/'+ key,
    // url:'/sys/randomImage/',
    method:"get"
  })
}
// 获取手机号验证码
export function getPhoneCode(data) {
  return request({
    url: '/sys/sms',
    method: 'post',
    data
  })
}
// 用户注册接口
export function register(data) {
  return request({
    url: '/sys/user/register',
    method: 'post',
    data
  })
}
// 校验用户账号是否唯一
export function checkOnlyUser(params) {
  return request({
    url: '/sys/user/checkOnlyUser',
    method: 'get',
    params
  })
}
// 用户密码登录接口
export function login(data) {
  return request({
    url: '/sys/login',
    method: 'post',
    data
  })
}
//检查联邦方是否已存在 
export function checkOnlyParty(data) {
  return request({
    url: '/platform/flPartyInfo/checkOnlyParty',
    method: 'post',
    data
  })
}
// 联邦方注册
export function lbregister(data) {
  return request({
    url: '/platform/flPartyInfo/register',
    method: 'post',
    data
  })
}
// 用户手机号验证
export function phoneVerification(data) {
  return request({
    url: '/sys/user/phoneVerification',
    method: 'post',
    data
  })
}
// 用户忘记密码
export function passwordChange(params) {
  return request({
    url: '/sys/user/passwordChange',
    method: 'get',
    params
  })
}