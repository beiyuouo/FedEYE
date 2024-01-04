import Vue from 'vue'
import Router from 'vue-router'
import LoginLayout from '@/layout/LoginMain'
import IndexMain from '@/layout/IndexMain'
import HomeMain from '@/layout/HomeMain'
import { getToken } from "@/utils/auth"
Vue.use(Router)
const whiteRouter = ['/forgetPassWord','/register','/bind','/bind-success','/bind-join','/info','/callback']
const constantRoutes = [
//  {
//     path: '/',
//     // hidden: true,
//     component: HomeMain,
//     children: [
//       {
//         path: '/',
//         name: 'newhome',
//         meta: { title: '首页', icon: 'shouye', fixed: true ,keepAlive: false},
//         component: () => import('@/views/newhome/index')
//       }
//     ]
//   },
  {
    path: '/',
    // hidden: true,
    component: HomeMain,
    children: [
      {
        path: '/',
        name: 'newhome',
        meta: { title: '首页', icon: 'shouye', fixed: true ,keepAlive: false},
        component: () => import('@/views/newhome/index')
      }
    ]
  },
  // {
  //   path: '/shortcut',
  //   // hidden: true,
  //   component: HomeMain,
  //   children: [
  //     {
  //       path: '/shortcut',
  //       name: 'shortcut',
  //       meta: { title: '快捷通道', icon: 'shouye', fixed: true ,keepAlive: false},
  //       component: () => import('@/views/shortcut/index')
  //     }
  //   ]
  // },
  {
    path: '/login',
    hidden: true,
    component: LoginLayout,
    children: [
      {
        path: '/login',
        name: 'Login',
        meta: { title: '登录', fixed: true,keepAlive: false },
        component: () => import('@/views/login/index')
      }
    ]
  },
  {
    path: '/callback',
    hidden: true,
    // component: LoginLayout,
    component: () => import('@/views/loading/index'),
    children: [
      {
        path: '/callback',
        name: 'CallBack',
        meta: { title: 'loading',  fixed: true,keepAlive: false },
        component: () => import('@/views/loading/index')
      }
    ]
  },
  {
    path: '/tuili',
    hidden: true,
    component: HomeMain,
    children: [
      {
        path: '/tuili',
        name: 'Tuili',
        meta: { title: '推理', fixed: true ,keepAlive: false},
        component: () => import('@/views/tuili/index')
      }
    ]
  },
  {
    path: '/register',
    hidden: true,
    component: LoginLayout,
    children: [
      {
        path: '/register',
        name: 'Register',
        meta: { title: '注册', fixed: true ,keepAlive: false},
        component: () => import('@/views/register/index')
      }
    ]
  },
  {
    path: '/forgetPassWord',
    hidden: true,
    component: LoginLayout,
    children: [
      {
        path: '/forgetPassWord',
        name: 'ForgetPassWord',
        meta: { title: '密码找回', fixed: true ,keepAlive: false},
        component: () => import('@/views/forgetPsd/index')
      }
    ]
  },
  {
    path: '/mytask/mytaskDetail',
    hidden: true,
    component: HomeMain,
    children: [
      {
        path: '/mytask/mytaskDetail',
        name: 'MytaskDetail',
        meta: { title: '任务详情', fixed: true ,keepAlive: false },
        component: () => import('@/views/taskdetail/index')
      }
    ]
  },
  
  {
    path: '/my',
    component: HomeMain,
    meta: { title: '我的', icon: 'wode', fixed: true },
    children: [
      {
        path: '/mytask',
        name: 'Mytask',
        meta: { 
          title: '我的任务', 
          icon: 'renwu1', 
          fixed: true ,
          isUserCache: false,
          keepAlive: true 
        },
        component: () => import('@/views/mytask/index')
      },
      {
        path: '/mydata',
        name: 'Mydata',
        meta: { 
          title: '我的数据', 
          icon: 'shuju1', 
          fixed: true ,
          isUserCache: false,
          keepAlive: true},
        component: () => import('@/views/moredata/index')
      },
      {
        path: '/mymodal',
        name: 'Mymodal',
        meta: { 
          title: '我的模型', 
          icon: 'moxing2', 
          fixed: true ,
          isUserCache: false,
          keepAlive: true},
        component: () => import('@/views/mymodel/index')
      }
    ]
  },
  {
    path: '/bind',
    hidden: true,
    component: LoginLayout,
    children: [
      {
        path: '/bind',
        name: 'Bind',
        meta: { title: '绑定', fixed: true ,keepAlive: false},
        component: () => import('@/views/bind/index')
      }
    ]
  },
  {
    path: '/algorithm',
    // hidden: true,
    component: HomeMain,
    children: [
      {
        path: '/algorithm',
        name: 'algorithm',
        meta: { title: '算法池', icon: 'suanfaku', fixed: true ,keepAlive: false},
        component: () => import('@/views/algorithm/index')   // @/views/mytask/index   @/views/algorithm/index
      }
    ]
  },
  {
    path: '/algorithm/algorithmDetail',
    hidden: true,
    component: HomeMain,
    children: [
      {
        path: '/algorithm/algorithmDetail',
        name: 'algorithmDetail',
        meta: { title: '算法详情', fixed: true ,keepAlive: false},
        component: () => import('@/views/algorithmDetail/index')
      }
    ]
  },
 
  {
    path: '/task',
    component: HomeMain,
    children: [
      {
        path: '/task',
        name: 'task',
        meta: { title: '任务池', icon: 'huore', fixed: true ,keepAlive: false},
        component: () => import('@/views/task/index')
      }
    ]
  },
  {
    path: '/data',
    component: HomeMain,
    children: [
      {
        path: '/data',
        name: 'data',
        meta: { title: '数据池', icon: 'shuju1', fixed: true ,keepAlive: false},
        component: () => import('@/views/data/index')
      }
    ]
  },
  {
    path: '/modal',
    component: HomeMain,
    children: [
      {
        path: '/modal',
        name: 'modal',
        meta: { title: '模型池', icon: 'moxing2', fixed: true ,keepAlive: false},
        component: () => import('@/views/model/index')
      }
    ]
  },
  {
    path: '/modal/modalDetail',
    hidden: true,
    component: HomeMain,
    children: [
      {
        path: '/modal/modalDetail',
        name: 'modalDetail',
        meta: { title: '模型详情', fixed: true ,keepAlive: false},
        component: () => import('@/views/modeldetail/index')
      }
    ]
  },
  {
    path: '/bind-success',
    hidden: true,
    component: LoginLayout,
    children: [
      {
        path: '/bind-success',
        name: 'BindSuccess',
        meta: { title: '绑定成功', fixed: true ,keepAlive: false},
        component: () => import('@/views/bind/success')
      }
    ]
  },
  {
    path: '/bind-join',
    hidden: true,
    component: LoginLayout,
    children: [
      {
        path: '/bind-join',
        name: 'BindJoin',
        meta: { title: '成功加入', fixed: true ,keepAlive: false},
        component: () => import('@/views/bind/join')
      }
    ]
  },
  {
    path: '/info',
    hidden: true,
    component: LoginLayout,
    children: [
      {
        path: '/info',
        name: 'Info',
        meta: { title: '完善信息', fixed: true,keepAlive: false },
        component: () => import('@/views/info/index')
      }
    ]
  },
  {
    path: '/index',
    hidden: true,
    component: IndexMain,
    children: [
      {
        path: '/index',
        name: 'Index',
        meta: { title: '主页', fixed: true ,keepAlive: false},
        component: () => import('@/views/index/index')
      }
    ]
  },
  {
    path: '/404',
    hidden: true,
    component: HomeMain,
    children: [
      {
        path: '/404',
        name: 'error',
        meta: { title: '404',  fixed: true,keepAlive: false },
        component: () => import('@/views/errorpage/404.vue')
      }
    ]
  },
  {
    path: '/environment',
    hidden: true,
    component: HomeMain,
    children: [
      {
        path: '/environment',
        name: 'Environment',
        meta: { title: '环境部署',  fixed: true,keepAlive: false },
        component: () => import('@/views/environment/index')
      }
    ]
  },
  {
    path: '/journal',
    hidden: true,
    component: HomeMain,
    children: [
      {
        path: '/journal',
        name: 'journal',
        meta: { title: '更新日志',  fixed: true,keepAlive: false },
        component: () => import('@/views/journal/index')
      }
    ]
  },
  {
    path: '/data-detail',
    hidden: true,
    component: HomeMain,
    children: [
      {
        path: '/data-detail',
        name: 'DataDetail',
        meta: { title: '数据详情',  fixed: true,keepAlive: false },
        component: () => import('@/views/datadetail/index')
      }
    ]
  },
 
]

const router = new Router({
  mode: 'history',
  base: process.env.BASE_URL,
  routes: constantRoutes
})
const originalPush = Router.prototype.push
Router.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}
router.beforeEach((to, from, next) => {
  // 存在token时
  if (getToken()) {
    if (to.path === "/login") {
   
      next({ path: "/" });
    } else if(to.path==='/index') {
      sessionStorage.setItem("HideHeader", true);
      next()
    } else {
        sessionStorage.setItem("HideHeader", false);
        next();
    }
  } else {
    if (to.path === "/login") {
      next();
    } else {
      if(whiteRouter.indexOf(to.path) != -1) {
        // 白名单
        next()
      } else {
        // 重定向到login
        next('/login')
      }
      
      // next(`/login?redirect=${to.path}`);
    }
    
  }
});
export default router
