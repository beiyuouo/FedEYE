/**
 * @param {string} path
 * @returns {Boolean}
 */
 export function isExternal(path) {
    return /^(https?:|mailto:|tel:)/.test(path)
  }
  
  /**
   * @param {string} str
   * @returns {Boolean}
   */
  export function validUsername(str) {
    const valid_map = ['admin', 'editor']
    return valid_map.indexOf(str.trim()) >= 0
  }
  export function formatTime(time) {
    let secondTime = parseInt(time) // 秒
    let minuteTime = 0 //分钟
    let hourTime = 0  // 小时
    if(secondTime > 60)  {
      // 如果大于60秒
      // 获取分钟
      minuteTime = parseInt( secondTime / 60)
      // 获取秒
      secondTime = parseInt( secondTime % 60 )
      // 如果分钟大于60 将转换成小时
      if(minuteTime > 60) {
        // 获取小时
        hourTime = parseInt(minuteTime / 60)
        // 获取分钟
        minuteTime = parseInt( minuteTime % 60)
      }
    }
    // let result =  minuteTime + 'm' + secondTime  + 's'
    if(hourTime<10) {
      hourTime = '0' + hourTime 
    } 
    if(minuteTime<10) {
      minuteTime = '0' + minuteTime  
    } 
 
    if(secondTime<10) {
      secondTime = '0' + secondTime  
    } 
    let result = hourTime +':' + minuteTime  +':' + secondTime  
    return result
  }
  