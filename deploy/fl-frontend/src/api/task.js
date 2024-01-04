import {request} from "@/utils/request";


// 检测部署状态，创建招募任务前，必须先配置好环境
export function softWareStatus(id) {
    return request({
        url: '/platform/flJobRecruit/softWareStatus',
        method: 'post',
        headers: {
            'tenant_id': id
        },
    })
}
  
  // 数据选择
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

  // 算法选择
export function queryAlgorithm(params) {
    return request({
      url: '/federalml/algorithm/queryAlgorithm',
      method: 'get',
      params
    })
}
  // 保存任务
export function createJob(data) {
    return request({
      url: '/platform/flJobRecruit/createJob',
      method: 'post',
      data
    })
}
//  任务名称、任务状态
export function queryByIdTask(params,id) {
    return request({
      url: '/platform/flJobRecruit/queryById',
      method: 'get',
      params,
      headers: {
        'tenant_id': id
      },
    })
}

// 每个联邦方情况，个数、数据条数需自己汇总
export function queryFlJobRegistByMainId(params,id) {
    return request({
      url: '/platform/flJobRecruit/queryFlJobRegistByMainId',
      method: 'get',
      params,
      headers: {
        'tenant_id': id
      },
    })
}
// 我的加入状态、阶段状态
// export function queryMyFlJobRegistByMainId(params,id) {
//     return request({
//       url: '/platform/flJobRecruit/queryMyFlJobRegistByMainId',
//       method: 'get',
//       params,
//       headers: {
//         'tenant_id': id
//       },
//     })
// }


// 通过邀请码加入任务 
export function joinJob(data,id) {
  return request({
    url: '/platform/flJobRecruit/joinJob',
    method: 'post',
    data,
    headers: {
      'tenant_id': id
    },
  })
}

// 联邦招募任务报名
export function registJob(data,id) {
  return request({
    url: '/platform/flJobRecruit/regist',
    method: 'post',
    data,
    headers: {
      'tenant_id': id,
      'Content-Type' : 'application/x-www-form-urlencoded'
    },
  })
}


// 环境检测

// 软件状态检查
export function appStatus(params) {
  return request({
    url: '/federalml/readyStatus/appStatus',
    method: 'get',
    params,
    // headers: {
    //   'tenant_id': id,
    //   'Content-Type' : 'application/x-www-form-urlencoded'
    // },
  })
}
// 硬件环境检查
export function nodeStatus(params) {
  return request({
    url: '/federalml/readyStatus/nodeStatus',
    method: 'get',
    params,
    // headers: {
    //   'tenant_id': id,
    //   'Content-Type' : 'application/x-www-form-urlencoded'
    // },
  })
}
// 任务详情 任务名称、任务状态 /platform/flJobRecruit/queryById 
// 每个联邦方情况，个数、数据条数需自己汇总 /platform/flJobRecruit/queryFlJobRegistByMainId
//我的加入状态、阶段状态 /platform/flJobRecruit/queryMyFlJobRegistByMainId



// 任务执行完毕 任务执行完的展示信息

// 检查硬件状态
export function hardWareStatus(id) {
  return request({
    url: '/platform/flJobRecruit/hardWareStatus',
    method: 'post',
    headers: {
      'tenant_id': id,
      'Content-Type' : 'application/x-www-form-urlencoded'
    },
  })
}


// 发起方数据格式信息
export function queryFlJobDataInfo(params) {
  return request({
    url: '/platform/flJobRecruit/queryFlJobDataInfo',
    method: 'get',
    headers: {
      'Content-Type' : 'application/x-www-form-urlencoded'
    },
    params
  })
}


// 数据对比
export function summaryCompare(data) {
  return request({
    url: '/federalml/collectFileMeta/summaryCompare',
    method: 'post',
    data
  })
}

// 保存训练数据
export function updateData(data) {
  return request({
    url: '/platform/flJobRecruit/updateData',
    method: 'post',
    headers: {
      'Content-Type' : 'application/x-www-form-urlencoded'
    },
    data
  })
}

// 开始训练模型
export function taskSubmit(data) {
  return request({
    url: '/platform/flJobRecruit/submit',
    method: 'post',
    headers: {
      'Content-Type' : 'application/x-www-form-urlencoded'
    },
    data
  })
}

// 查询任务执行信息
export function jobInfo(data,id) {
  return request({
    url: '/platform/flJobRecruit/jobInfo',
    method: 'post',
    headers: {
      'Content-Type' : 'application/x-www-form-urlencoded',
      'tenant_id': id,
    },
    data
  })
}
// beta 版本查询任务进度

export function jobInfoByRecruitId(data,id) {
  return request({
    url: '/platform/flJobRecruit/jobInfoByRecruitId',
    method: 'post',
    headers: {
      'Content-Type' : 'application/x-www-form-urlencoded',
      'tenant_id': id,
    },
    data
  })
}
// 模型指标
export function metric(data,id) {
  return request({
    url: '/platform/flJobRecruit/metric',
    method: 'post',
    headers: {
      'tenant_id': id,
    },
    data
  })
}

// 任务退出
export function exitRegist(data,id) {
  return request({
    url: '/platform/flJobRecruit/exitRegist',
    method: 'post',
    headers: {
      'tenant_id': id,
    },
    data
  })
}
 
// 生成加入联邦任务的邀请码
export function taskInviteCode(params,id) {
  return request({
    url: '/platform/flJobRecruit/inviteCode',
    method: 'get',
    headers: {
      'tenant_id': id,
    },
    params
  })
}

// 报名审核 
export function checkRegist(data,id) {
  return request({
    url: '/platform/flJobRecruit/checkRegist',
    method: 'post',
    headers: {
      'tenant_id': id,
    },
    data
  })
}

// // 查询任务执行情况
// export function jobInfo(data,id) {
//   return request({
//     url: '/platform/flJobRecruit/jobInfo',
//     method: 'post',
//     headers: {
//       'tenant_id': id,
//     },
//     data
//   })
// }

// 非联邦任务

export function createLocalJob(data,id) {
  return request({
    url: '/platform/flJobRecruit/createLocalJob',
    method: 'post',
    headers: {
      'tenant_id': id,
    },
    data
  })
}

// 通用算法库

export function algorithmCall(taskid,data,id) {
  return request({
    url: '/federalml/algorithm/call?id='+taskid,
    method: 'post',
    headers: {
      'tenant_id': id,
    },
    data
  })
}

// 新建任务数据算法双向选择

export function queryFileMetaAndAlgorithm(params,id) {
  return request({
    url: '/federalml/collectFileMeta/queryFileMetaAndAlgorithm',
    method: 'get',
    headers: {
      'tenant_id': id,
    },
    params
  })
}

// 修改联邦任务的算法和数据
export function update(data,id) {
  return request({
    url: '/platform/flJobRecruit/update',
    method: 'put',
    headers: {
      'tenant_id': id,
    },
    data
  })
}

// 训练任务删除
export function deleteTask(params,id) {
  return request({
    url: '/platform/flJobRecruit/delete',
    method: 'delete',
    headers: {
      'tenant_id': id,
    },
    params
  })
}
// 数据删除
export function deleteData(params,id) {
  return request({
    url: '/federalml/collectFileMeta/delete',
    method: 'delete',
    headers: {
      'tenant_id': id,
    },
    params
  })
}

// 推理任务删除
export function deleteTlTask(params,id) {
  return request({
    url: '/federalml/flModelInference/delete',
    method: 'delete',
    headers: {
      'tenant_id': id,
    },
    params
  })
}
// 模型删除
export function deleteModel(params,id) {
  return request({
    url: '/federalml/flModelInfo/delete',
    method: 'delete',
    headers: {
      'tenant_id': id,
    },
    params
  })
}
// 模型池删除
export function deleteModelPub(params,id) {
  return request({
    url: '/federalml/flModelInfo/delete_public',
    method: 'delete',
    headers: {
      'tenant_id': id,
    },
    params
  })
}
// 热门算法
export function hotAlgorithm(id) {
    return request({
      url: '/federalml/algorithm/hotAlgorithm',
      method: 'get',
      headers: {
        'tenant_id': id,
      }
    })
  }

// 纵向推理任务数据选择
export function hetero(params,id) {
    return request({
        url: '/federalml/flModelInference/hetero',
        method: 'post',
        headers: {
          'tenant_id': id,
        },
        params
    })
}
// 纵向推理任务数据选择
export function heteroDataList(params,id) {
    return request({
        url: '/federalml/flModelInference/heteroDataList',
        method: 'get',
        headers: {
          'tenant_id': id,
        },
        params
    })
}
// 纵向推理任务开始
export function heteroInference(params,id) {
    return request({
        url: '/federalml/flModelInference/heteroInference',
        method: 'post',
        headers: {
          'tenant_id': id,
        },
        params
    })
}
// 纵向推理 数据选择
export function selectList(params,id) {
    return request({
        url: '/federalml/collectFileMeta/selectList',
        method: 'get',
        headers: {
          'tenant_id': id,
        },
        params
    })
}