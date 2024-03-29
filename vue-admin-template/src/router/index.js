import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'

/**
 * Note: sub-menu only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 *
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menu
 *                                if not set alwaysShow, when item has more than one children route,
 *                                it will becomes nested mode, otherwise not show the root menu
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
    roles: ['admin','editor']    control the page roles (you can set multiple roles)
    title: 'title'               the name show in sidebar and breadcrumb (recommend set)
    icon: 'svg-name'/'el-icon-x' the icon show in the sidebar
    breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
    activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
  }
 */

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  },

  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true
  },

  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [{
      path: 'dashboard',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/index'),
      meta: { title: 'Dashboard', icon: 'dashboard' }
    }]
  },

  //医院设置
  {
    path: '/yygh/hospset',
    component: Layout,
    redirect: '/yygh/hospset/list',
    name: '医院管理',
    meta: { title: '医院管理', icon: 'el-icon-s-help' },
    children: [
      {
        path: 'list',
        name: 'HospSetList',
        component: () => import('@/views/yygh/hospset/list'),//@可以理解为src目录
        meta: { title: '医院设置列表', icon: 'table' }
      },
      {
        path: 'create',
        name: 'HospSetCreate',
        component: () => import('@/views/yygh/hospset/form'),
        meta: { title: '医院设置添加', icon: 'tree' }
      },
      { //这是修改的请求路由，我们让它和添加共用一个页面 form.vue
        //并将其隐藏起来
        path: 'edit/:id', //该脚手架使用':id'接收请求参数，这里是当前行数据的id(医院设置信息的id)
        name: 'HospSetEdit',
        component: () => import('@/views/yygh/hospset/form'),
        meta: { title: '医院设置修改', icon: 'tree' },
        hidden: true //将该页面隐藏
      },
      //医院管理
      {
        path: '/yygh/hosp/list',
        name: '医院列表',
        component: () => import('@/views/yygh/hosp/list'),
        meta: { title: '医院列表', icon: 'table' }
      },
      //查看医院详情
      {
        path: '/yygh/hosp/show/:id',
        name: '查看',
        component: () => import('@/views/yygh/hosp/show'),
        meta: { title: '查看', noCache: true },
        hidden: true //隐藏页面
      },
      //排班信息
      {
        path: '/yygh/hosp/schedule/:hoscode', //hoscode接收当前医院的医院编号
        name: '排班',
        component: () => import('@/views/yygh/hosp/schedule'),
        meta: { title: '排班', noCache: true },
        hidden: true
      }
    ]
  },

  //js文件格式化快捷键 alt + shift + f
  //用户管理
  {
    path: '/user',
    component: Layout,
    redirect: '/user/userInfo/list',
    name: 'userInfo',
    meta: { title: '用户管理', icon: 'table' },
    alwaysShow: true,
    children: [
      {
        path: 'userInfo/list',
        name: '用户列表',
        component: () => import('@/views/yygh/userInfo/list'),
        meta: { title: '用户列表', icon: 'table' }
      },
      {
        path: 'userInfo/authList',
        name: '认证审批列表',
        component: () => import('@/views/yygh/userInfo/authList'),
        meta: { title: '认证审批列表', icon: 'table' }
      },
      {
        path: 'userInfo/show/:id',
        name: '用户查看',
        component: () => import('@/views/yygh/userInfo/show'),
        meta: { title: '用户查看' },
        hidden: true
      }
    ]
  },

  //数据字典
  {
    path: '/yygh/dict/',
    component: Layout,
    redirect: '/yygh/dict/list',
    name: '数据管理',
    alwaysShow: true,
    meta: { title: '数据管理', icon: 'example' },
    children: [
      {
        path: 'list',
        name: '数据字典',
        component: () => import('@/views/yygh/dict/list'),
        meta: { title: '数据字典', icon: 'table' }
      }
    ]
  },
  {
    path: '/statistics',
    component: Layout,
    redirect: '/statistics/order/index',
    name: 'BasesInfo',
    meta: { title: '统计管理', icon: 'table' },
    alwaysShow: true,
    children: [
      {
        path: 'order/index',
        name: '预约统计',
        component: () => import('@/views/yygh/sta/index'),
        meta: { title: '预约统计' }
      }
    ]
  },





  // {
  //   path: '/form',
  //   component: Layout,
  //   children: [
  //     {
  //       path: 'index',
  //       name: 'Form',
  //       component: () => import('@/views/form/index'),
  //       meta: { title: 'Form', icon: 'form' }
  //     }
  //   ]
  // },

  // {
  //   path: '/nested',
  //   component: Layout,
  //   redirect: '/nested/menu1',
  //   name: 'Nested',
  //   meta: {
  //     title: 'Nested',
  //     icon: 'nested'
  //   },
  //   children: [
  //     {
  //       path: 'menu1',
  //       component: () => import('@/views/nested/menu1/index'), // Parent router-view
  //       name: 'Menu1',
  //       meta: { title: 'Menu1' },
  //       children: [
  //         {
  //           path: 'menu1-1',
  //           component: () => import('@/views/nested/menu1/menu1-1'),
  //           name: 'Menu1-1',
  //           meta: { title: 'Menu1-1' }
  //         },
  //         {
  //           path: 'menu1-2',
  //           component: () => import('@/views/nested/menu1/menu1-2'),
  //           name: 'Menu1-2',
  //           meta: { title: 'Menu1-2' },
  //           children: [
  //             {
  //               path: 'menu1-2-1',
  //               component: () => import('@/views/nested/menu1/menu1-2/menu1-2-1'),
  //               name: 'Menu1-2-1',
  //               meta: { title: 'Menu1-2-1' }
  //             },
  //             {
  //               path: 'menu1-2-2',
  //               component: () => import('@/views/nested/menu1/menu1-2/menu1-2-2'),
  //               name: 'Menu1-2-2',
  //               meta: { title: 'Menu1-2-2' }
  //             }
  //           ]
  //         },
  //         {
  //           path: 'menu1-3',
  //           component: () => import('@/views/nested/menu1/menu1-3'),
  //           name: 'Menu1-3',
  //           meta: { title: 'Menu1-3' }
  //         }
  //       ]
  //     },
  //     {
  //       path: 'menu2',
  //       component: () => import('@/views/nested/menu2/index'),
  //       name: 'Menu2',
  //       meta: { title: 'menu2' }
  //     }
  //   ]
  // },

  // {
  //   path: 'external-link',
  //   component: Layout,
  //   children: [
  //     {
  //       path: 'https://panjiachen.github.io/vue-element-admin-site/#/',
  //       meta: { title: 'External Link', icon: 'link' }
  //     }
  //   ]
  // },

  // 404 page must be placed at the end !!!
  { path: '*', redirect: '/404', hidden: true }
]

const createRouter = () => new Router({
  // mode: 'history', // require service support
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})

const router = createRouter()

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export default router
