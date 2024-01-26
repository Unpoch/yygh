import Vue from 'vue'
import Router from 'vue-router'
import { interopDefault } from './utils'
import scrollBehavior from './router.scrollBehavior.js'

const _0bba32b1 = () => interopDefault(import('..\\pages\\order\\index.vue' /* webpackChunkName: "pages/order/index" */))
const _00bc855a = () => interopDefault(import('..\\pages\\patient\\index.vue' /* webpackChunkName: "pages/patient/index" */))
const _7a96406c = () => interopDefault(import('..\\pages\\user\\index.vue' /* webpackChunkName: "pages/user/index" */))
const _50a10842 = () => interopDefault(import('..\\pages\\hospital\\booking.vue' /* webpackChunkName: "pages/hospital/booking" */))
const _cb698c04 = () => interopDefault(import('..\\pages\\hospital\\schedule.vue' /* webpackChunkName: "pages/hospital/schedule" */))
const _b94ddc24 = () => interopDefault(import('..\\pages\\order\\show.vue' /* webpackChunkName: "pages/order/show" */))
const _51c451a9 = () => interopDefault(import('..\\pages\\patient\\add.vue' /* webpackChunkName: "pages/patient/add" */))
const _1edd4625 = () => interopDefault(import('..\\pages\\patient\\show.vue' /* webpackChunkName: "pages/patient/show" */))
const _cac73fd8 = () => interopDefault(import('..\\pages\\weixin\\callback.vue' /* webpackChunkName: "pages/weixin/callback" */))
const _935b381c = () => interopDefault(import('..\\pages\\hospital\\detail\\_hoscode.vue' /* webpackChunkName: "pages/hospital/detail/_hoscode" */))
const _db9f2b8e = () => interopDefault(import('..\\pages\\hospital\\notice\\_hoscode.vue' /* webpackChunkName: "pages/hospital/notice/_hoscode" */))
const _0096be3e = () => interopDefault(import('..\\pages\\hospital\\_hoscode.vue' /* webpackChunkName: "pages/hospital/_hoscode" */))
const _1dbf2f96 = () => interopDefault(import('..\\pages\\index.vue' /* webpackChunkName: "pages/index" */))

// TODO: remove in Nuxt 3
const emptyFn = () => {}
const originalPush = Router.prototype.push
Router.prototype.push = function push (location, onComplete = emptyFn, onAbort) {
  return originalPush.call(this, location, onComplete, onAbort)
}

Vue.use(Router)

export const routerOptions = {
  mode: 'history',
  base: decodeURI('/'),
  linkActiveClass: 'nuxt-link-active',
  linkExactActiveClass: 'nuxt-link-exact-active',
  scrollBehavior,

  routes: [{
    path: "/order",
    component: _0bba32b1,
    name: "order"
  }, {
    path: "/patient",
    component: _00bc855a,
    name: "patient"
  }, {
    path: "/user",
    component: _7a96406c,
    name: "user"
  }, {
    path: "/hospital/booking",
    component: _50a10842,
    name: "hospital-booking"
  }, {
    path: "/hospital/schedule",
    component: _cb698c04,
    name: "hospital-schedule"
  }, {
    path: "/order/show",
    component: _b94ddc24,
    name: "order-show"
  }, {
    path: "/patient/add",
    component: _51c451a9,
    name: "patient-add"
  }, {
    path: "/patient/show",
    component: _1edd4625,
    name: "patient-show"
  }, {
    path: "/weixin/callback",
    component: _cac73fd8,
    name: "weixin-callback"
  }, {
    path: "/hospital/detail/:hoscode?",
    component: _935b381c,
    name: "hospital-detail-hoscode"
  }, {
    path: "/hospital/notice/:hoscode?",
    component: _db9f2b8e,
    name: "hospital-notice-hoscode"
  }, {
    path: "/hospital/:hoscode?",
    component: _0096be3e,
    name: "hospital-hoscode"
  }, {
    path: "/",
    component: _1dbf2f96,
    name: "index"
  }],

  fallback: false
}

export function createRouter () {
  return new Router(routerOptions)
}
