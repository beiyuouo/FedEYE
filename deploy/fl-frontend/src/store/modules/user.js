import { login } from "@/api/login/index";
import { getToken, setToken, removeToken , 
  getUserInfo, setUserInfo, removeUserInfo,getRefreshToken,setRefreshToken,removeRefreshToken
} from "@/utils/auth";

const state = {
  token: getToken(),
  userInfo: JSON.parse(getUserInfo()) ,
  refreshToken: getRefreshToken()
};

const mutations = {
  SET_TOKEN: (state, token) => {
    state.token = token;
  },
  SET_USERINFO: (state,userinfo) => {
    state.userInfo = userinfo
  },
  SET_REFRESHTOKEN: (state, refreshToken) => {
    state.refreshToken = refreshToken;
  },
};

const actions = {
  // CAS验证登录
  ValidateLogin({ commit }, userInfo) {
    return new Promise((resolve, reject) => {
      getAction("/sys/cas/client/validateLogin",userInfo).then(response => {
        console.log("----cas 登录--------",response);
        if(response.success){
          const result = response.result
          const userInfo = result.userInfo
          commit("SET_TOKEN", data.token);
          commit("SET_USERINFO", data.userInfo);
          setToken(data.token);
          setUserInfo(JSON.stringify(data.userInfo))
          // Vue.ls.set(ACCESS_TOKEN, result.token, 7 * 24 * 60 * 60 * 1000)
          // Vue.ls.set(USER_NAME, userInfo.username, 7 * 24 * 60 * 60 * 1000)
          // Vue.ls.set(USER_INFO, userInfo, 7 * 24 * 60 * 60 * 1000)
          // commit('SET_TOKEN', result.token)
          // commit('SET_INFO', userInfo)
          // commit('SET_NAME', { username: userInfo.username,realname: userInfo.realname, welcome: welcome() })
          // commit('SET_AVATAR', userInfo.avatar)
          resolve(response)
        }else{
          resolve(response)
        }
      }).catch(error => {
        reject(error)
      })
    })
  },
  // set userinfo 
  setuserinfo({commit},userInfo) {
    return new Promise((resolve) => {
      commit("SET_USERINFO",userInfo);
      setUserInfo(userInfo)
      resolve();
    });
  },
  //  set refreshToken
  seturefreshToken({commit},refreshToken) {
    return new Promise((resolve) => {
      commit("SET_REFRESHTOKEN",refreshToken);
      setRefreshToken(refreshToken)
      resolve();
    });
  },
  // user login
  login({ commit },params) {
    return new Promise((resolve, reject) => {
      login(params)
        .then(response => {
          const data  = response.data.result
          commit("SET_TOKEN", data.token);
          commit("SET_USERINFO", data.userInfo);
          setToken(data.token);
          setUserInfo(JSON.stringify(data.userInfo))
          resolve(response);
        })
        .catch(res => {
            reject(res);
        });
    });
  },
  ssoLogin({commit},obj) {
    return new Promise(resolve => {
        console.log(resolve())
        commit("SET_TOKEN", obj.token);
        commit("SET_USERINFO",obj.userInfo);
        commit("SET_REFRESHTOKEN", obj.refreshToken);
        setToken(obj.token);
        setUserInfo(JSON.stringify(obj.userInfo))
        seturefreshToken(obj.refreshToken)
        resolve();
    });
  },
  // user logout
  logout({ commit }) {
    return new Promise(resolve => {
      commit("SET_TOKEN", "");
      commit("SET_USERINFO", "");
      commit("SET_REFRESHTOKEN", "");
      removeToken();
      removeUserInfo()
      removeRefreshToken()
      resolve();
    });
  },
  // remove token
  resetToken({ commit }) {
    return new Promise(resolve => {
      commit("SET_TOKEN", "");
      commit("SET_USERINFO", "");
      removeToken();
      removeUserInfo()
      resolve();
    });
  },
  // remove    RefreshToken
  resetRefreshToken({ commit }) {
    return new Promise(resolve => {
      commit("SET_TOKEN", "");
      commit("SET_USERINFO", "");
      commit("SET_REFRESHTOKEN", "");
      removeToken();
      removeUserInfo()
      removeRefreshToken()
      resolve();
    });
  },
};

export default {
  namespaced: true,
  state,
  mutations,
  actions
};
