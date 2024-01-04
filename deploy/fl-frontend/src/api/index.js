import {request,request1} from "@/utils/request";

// 生成加入联邦方的邀请码  
export function inviteCode(id) {
  return request({
    url: '/platform/flPartyInfo/inviteCode',
    method: 'get',
    headers: {
      'tenant_id': id
    },
  })
}

// 通过邀请码加入联邦方
export function joinParty(data) {
  return request({
    url: '/platform/flPartyInfo/joinParty',
    method: 'post',
    data
  })
}
// 联邦方信息编辑
export function Partyedit(data) {
  return request({
    url: '/platform/flPartyInfo/edit',
    method: 'put',
    data
  })
}

//查询当前用户拥有的菜单权限和按钮权限
export function getFlUserPermission(id) {
  return request({
    url: '/sys/permission/getFlUserPermission',
    method: 'get',
    headers: {
      'tenant_id': id
    },
  })
}
// 查询用户所属的所有联邦方
export function queryPartyInfo(params) {
  return request({
    url: '/platform/flPartyInfo/queryPartyInfo',
    method: 'get',
    params
  })
}

// 文件上传
export function upload(data) {
  return request({
    headers: {
      // 'Content-Type' : 'application/x-www-form-urlencoded'
      'Content-Type': 'multipart/form-data'
    },
    url: '/sys/common/upload',
    method: 'post',
    data
  })
}

// // 数据集上传
export function uploadData(data,url,fn) {
  return request1({
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    url: url+'/uploadZip',
    // url: url,
    method: 'post',
    data,
    onUploadProgress:fn,
  })
}
// 推理图片任务开启
export function inferenceImage(data,id) {
  return request({
    url: '/federalml/flModelInference/inferenceImages',
    method: 'post',
    headers: {
      'tenant_id': id,
      // 'Content-Type': 'application/json',
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}
// 数据集保存
export function saveUploadData(data,url) {
  return request1({
    url:url+'/save',
    method: 'post',
    data,
  })
}
// 新手任务
export function beginnerJob(id) {
  return request({
    url: '/platform/flJobRecruit/beginnerJob',
    method: 'post',
    headers: {
      'tenant_id': id
    },
    
  })
}
// 我的任务
export function mytask(params,id) {
  return request({
    url: '/platform/flJobRecruit/listMyJob',
    headers: {
      'tenant_id': id
    },
    method: 'get',
    params
  })
}

// 任务池
export function listAllJob(params,id) {
  return request({
    url: '/platform/flJobRecruit/listAllJob',
    method: 'get',
    headers: {
        'tenant_id': id
      },
    params
  })
}

//  数据池
export function listAllData(params,id) {
  return request({
    url: '/federalml/collectFileMeta/listAllData',
    method: 'get',
    headers: {
        'tenant_id': id
      },
    params
  })
}
// 我的数据
export function listCurrent(params,id) {
  return request({
    url: '/federalml/collectFileMeta/listCurrent',
    method: 'get',
    headers: {
      'tenant_id': id
    },
    params
  })
}

// 详细数据查询
export function queryDataId(params) {
  return request({
    url: '/federalml/collectFileMeta/queryById',
    method: 'get',
    params
  })
}

// 当前用户的角色及状态
export function showFlUserRole(id) {
  return request({
    url: '/sys/user/showUserRole',
    method: 'get',
    headers: {
      'tenant_id': id
    }
  })
}

// 算法池

export function listAllAlgorithm(params,id) {
  return request({
    url: '/federalml/algorithm/listAllAlgorithm',
    method: 'get',
    headers: {
      'tenant_id': id
    },
    params
  })
}

// 查询指定数据

export function queryData(params,id) {
  return request({
    url: '/federalml/collectFileMeta/queryData',
    method: 'get',
    headers: {
      'tenant_id': id
    },
    params
  })
}

// 校验数据集名称 

export function searchByName(params,id) {
  return request({
    url: '/federalml/collectFileMeta/searchByName',
    method: 'get',
    headers: {
      'tenant_id': id
    },
    params
  })
}
// 热门任务  /platform/flJobRecruit/hotJob
export function hotJob(id) {
  return request({
    url: '/platform/flJobRecruit/hotJob',
    method: 'get',
    headers: {
      'tenant_id': id
    },
  })
}

// 任务最近更新
export function missionOpLog (id) {  
  return request({
    url: '/federalml/flOpLog/missionOpLog',
    method: 'get',
    headers: {
      'tenant_id': id
    },
  })
}

// 通过id 查询算法
export function queryAlgById (params,id) {  
  return request({
    url: '/federalml/algorithm/queryById',
    method: 'get',
    headers: {
      'tenant_id': id
    },
    params
  })
}
// 算法的最近使用
export function algOpLog (data,id) {  
  return request({
    url: '/federalml/flOpLog/algOpLog',
    method: 'post',
    headers: {
      'tenant_id': id,
      'Content-Type' : 'application/x-www-form-urlencoded'
    },
    data
  })
}

// 算法的基础信息
export function algUseInfo (params,id) {
  return request({
    url: '/federalml/algorithm/algUseInfo',
    method: 'get',
    headers: {
      'tenant_id': id
    },
    params
  })
}


//  我的模型 
export function listMyModel (params,id) {
  return request({
    url: '/federalml/flModelInfo/listMyModel',
    method: 'get',
    headers: {
      'tenant_id': id
    },
    params
  })
}
// 模型池  
export function listPoolModel (params,id) {
  return request({
    url: '/federalml/flModelInfo/listPoolModel',
    method: 'get',
    headers: {
        'tenant_id': id
      },
    params
  })
}

//模型详情 
export function modelDetail (params,id) {
  return request({
    url: '/federalml/flModelInfo/modelDetail',
    method: 'get',
    headers: {
      'tenant_id': id
    },
    params
  })
}

// 模型上传 
export function modalUpload (data,id) {
  return request({
    url: '/federalml/flModelInfo/upload',
    method: 'post',
    headers: {
      'tenant_id': id
    },
    data
  })
}

// 模型下载
export function modalDownload (data,id) {
  return request({
    url: '/federalml/flModelInfo/download',
    method: 'post',
    headers: {
      'tenant_id': id
    },
    data
  })
}
// 模型转换
export function modalChangeDownload (params,id) {
    return request({
      url: 'federalml/flModelInfo/model_download_by_id',
      method: 'get',
      headers: {
        'tenant_id': id,
        "Content-Type":"application/json;charset=UTF-8;"
      },
        params,
        responseType: 'blob',
        // paramsSerializer:params=>{
		// 	return qs.stringify(params)
		// }
        // method: 'get',
    })
  }

// 模型更新
export function modalEdit (data,id) {
  return request({
    url: '/federalml/flModelInfo/edit',
    method: 'put',
    headers: {
      'tenant_id': id
    },
    data
  })
}
// 模型状态查询
export function opStatus (params,id) {
  return request({
    url: '/federalml/flModelInfo/opStatus',
    method: 'get',
    headers: {
      'tenant_id': id
    },
    params
  })
}

// 创建推理任务
export function createJob (data,id) {
  return request({
    url: '/federalml/flModelInference/createJob',
    method: 'post',
    headers: {
      'tenant_id': id
    },
    data
  })
}
// 删除模型推理任务
export function deleteJob (data,id) {
  return request({
    url: '/federalml/flModelInference/delete',
    method: 'delete',
    headers: {
      'tenant_id': id
    },
    data
  })
}
// 开启推理任务
export function inference (data,id) {
  return request({
    url: '/federalml/flModelInference/inference',
    method: 'post',
    headers: {
      'tenant_id': id
    },
    data
  })
}
// 查询推理任务
export function ferenceList (params,id) {
  return request({
    url: '/federalml/flModelInference/list',
    method: 'get',
    headers: {
      'tenant_id': id
    },
    params
  })
}
// 推理任务详情
export function ferenceQueryById(params,id) {
  return request({
    url: '/federalml/flModelInference/queryById',
    method: 'get',
    headers: {
      'tenant_id': id
    },
    params
  })
}

// 模型的最近使用 
export function modelOpLog(data,id) {
  return request({
    url: 'federalml/flOpLog/modelOpLog',
    method: 'post',
    headers: {
      'tenant_id': id
    },
    data
  })
}

// 异步联邦任务是否保留

export function submitNotAllReady(data,id) {
  return request({
    url: 'platform/flJobRecruit/submitNotAllReady',
    method: 'post',
    headers: {
      'tenant_id': id
    },
    data
  })
}

//  数据集推理
export function inferenceByDataCollection(data,id) {
  return request({
    url: 'federalml/flModelInference/inferenceByDataCollection',
    method: 'post',
    headers: {
      'tenant_id': id
    },
    data
  })
}

// 数据集id选择数据 listOfMata?pageNo=1&pageSize=10&tableName=3tihhx
export function listOfMata(params,url,id) {
  return request1({
    url: url+'/listOfMeta',
    method: 'get',
    headers: {
      'tenant_id': id
    },
    params
  })
}
// 更新数据集
export function selectedMeta(data,url,id) {
  return request1({
    url: url+'/selectedMeta',
    method: 'post',
    headers: {
      'tenant_id': id
    },
    data
  })
}

// 登录页面接口查询
export function loginNum(params) {
  return request({
    url: 'federalml/bigScreen/num',
    method: 'get',
    params
  })
}
// 修改用户密码
export function updatePassword(data) {
  return request({
    url: '/sys/user/updatePassword',
    method: 'put',
    data
  })
}


export function phoneVerification(data) {
  return request({
    url: '/sys/user/phoneVerification',
    method: 'post',
    data
  })
}

// 模型参数说明
export function parameterDesc(params) {
  return request({
    url: '/federalml/flModelInfo/parameter',
    method: 'get',
    params
  })
}

// 更新日志
export function journal(params) {
  return request({
    url: '/federalml/flConf/version/list',
    method: 'get',
    params
  })
}
// nlp 模型推理
export function inferenceNlp(data,id) {
  return request({
    url: '/federalml/flModelInference/inference_nlp',
    method: 'post',
    headers: {
      'tenant_id': id,
      'Content-Type': 'application/json',
      // 'Content-Type': 'multipart/form-data'
    },
    data
  })
}
// 图神经模型推理

export function inferenceGraph(data,id) {
    return request({
      url: '/federalml/flModelInference/inference',
      method: 'post',
      headers: {
        'tenant_id': id,
        'Content-Type': 'application/json',
        // 'Content-Type': 'multipart/form-data'
      },
      data
    })
  }
 // 更新日志
export function downFile(url) {
    return request1({
      url: url,
      responseType: 'blob',  
      method: 'get',
    })
  }
// keycloak 登录接口
export function keycloakFn(params) {
    return request1 ({
        url: 'http://192.168.66.44:8081/jeecg-boot/sys/keycloakLogin',
        method: 'get',
        params
    })
}
// 数据预处理算法
export function addPreproccess(data,id) {
    return request({
      url: '/platform/flJobRecruit/addPreproccess',
      method: 'post',
      headers: {
        'tenant_id': id,
      },
      data
    })
  }