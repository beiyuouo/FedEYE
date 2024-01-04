import {request} from "@/utils/request";
// 任务提醒

export function sendJobNotic(params,id) {
    return request({
        url: '/sys/annountCement/sendJobNotic',
        headers: {
            'Content-Type' : 'application/x-www-form-urlencoded',
            'tenant_id': id
        },
        method: 'get',
        params,
    })
}
// 发送消息
export function sendSysAnnouncement(id) {
    return request({
        url: '/sys/api/sendSysAnnouncement',
        method: 'post',
        headers: {
            'Content-Type' : 'application/x-www-form-urlencoded'
        },
    })
}

// 更改状态为已读
export function editByAnntIdAndUserId(data,id) {
    return request({
        url: '/sys/sysAnnouncementSend/editByAnntIdAndUserId',
        method: 'put',
        headers: {
            'tenant_id': id
        },
        data
    })
}

// 所有消息列表
export function listByUserAndParty(params,id) {
    return request({
        url: '/sys/annountCement/listByUserAndParty',
        method: 'get',
        headers: {
            'tenant_id': id,
            'Content-Type' : 'application/x-www-form-urlencoded'
        },
        params
    })
}

// 联邦方基础信息查询
export function queryById(params,id) {
    return request({
        url: '/platform/flPartyInfo/queryById',
        method: 'get',
        headers: {
            'Content-Type' : 'application/x-www-form-urlencoded'
        },
        params
    })
}