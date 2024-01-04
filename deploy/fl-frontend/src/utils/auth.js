export function getToken() {
  return sessionStorage.getItem("token"); 
}
  
export function setToken(token) {
  return sessionStorage.setItem("token", token); 
}
  
export function removeToken() {
  return sessionStorage.removeItem("token");
}
export function getRefreshToken() {
    return sessionStorage.getItem("refreshToken"); 
}

export function setRefreshToken(refreshToken) {
    return sessionStorage.setItem("refreshToken", refreshToken); 
}

export function removeRefreshToken() {
    return sessionStorage.removeItem("refreshToken");
}
export function getUserInfo() {
  return sessionStorage.getItem("userInfo"); ;
}
  
export function setUserInfo(userInfo) {
  return sessionStorage.setItem("userInfo", userInfo);
}
  
export function removeUserInfo() {
  return sessionStorage.removeItem("userInfo");
}
export function getAuth() {
  return sessionStorage.getItem("Auth");
}
  
export function setAuth(Auth) {
  return sessionStorage.setItem("Auth", Auth);
}
  
export function removeAuth() {
  return sessionStorage.removeItem("Auth");
}
export function getPartyInfo() {
  return sessionStorage.getItem("PartyInfo");
}
export function setPartyInfo(PartyInfo) {
  return sessionStorage.setItem("PartyInfo", PartyInfo);
}
export function getTenantId() {
  return sessionStorage.getItem("TenantId");
}
  
export function setTenantId(TenantId) {
  return sessionStorage.setItem("TenantId", TenantId);
}

export function getIndex() {
  return sessionStorage.getItem("Index");
}
  
export function setIndex(Index) {
  return sessionStorage.setItem("Index", Index);
}

export function setUserRole(UserRole) {
  return sessionStorage.setItem("UserRole", UserRole);
}

export function getUserRole() {
  return sessionStorage.getItem("UserRole");
}
  