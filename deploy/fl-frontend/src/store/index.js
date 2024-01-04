import Vue from 'vue'
import Vuex from 'vuex'
import getters from "./getter";
import user from "./modules/user"
import app from './modules/app'
Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    token:''
  },
  mutations: {
    set_token(state, token) {
      state.token = token;
      sessionStorage.token = token;
    },
    del_token(state) {
      state.token = "";
      sessionStorage.removeItem("token");
    }
  },
  actions: {
  },
  modules: {
    user,
    app
  },
  getters
})
