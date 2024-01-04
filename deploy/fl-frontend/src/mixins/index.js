export const utils = {
  methods: {
    // 深拷贝
    deepCopy(obj) {
      let newObj = null
      let vm = this
      if (typeof obj === 'object' && obj !== null) {
          newObj = obj instanceof Array ? [] : {}
          for (let i in obj) {
              newObj[i] = typeof obj[i] === 'object' ? vm.deepCopy(obj[i]) : obj[i]
          }
      } else {
          newObj = obj
      }
      return newObj
    }
  }
}
