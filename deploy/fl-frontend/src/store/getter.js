const getters = {
    token: state => state.user.token,
    userInfo: state => state.user.userInfo,
    sidebar: state => state.app.sidebar,
    refreshToken: state => state.user.refreshToken
};
export default getters;