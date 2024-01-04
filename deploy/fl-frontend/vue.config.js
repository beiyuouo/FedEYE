"use strict";
const path = require("path");

function resolve(dir) {
  return path.join(__dirname, dir);
}
const name = ""; // page title  = defaultSettings.title ||

module.exports = {
    publicPath: "/",
    productionSourceMap: false,
    configureWebpack: {
      name: name,
      resolve: {
        alias: {
          "@": resolve("src")
        }
      }
    },
    css: {
      loaderOptions: {
          css: {},
          postcss: {
              plugins: [
                  require('postcss-px2rem')({
                      remUnit: 192 //设计图宽度大小/10之后的数值
                  })
              ]
          }
      }
    },
    chainWebpack(config) {
      config.module
          .rule('svg')
          .exclude.add(resolve('src/icons'))
          .end()
        config.module
          .rule('icons')
          .test(/\.svg$/)
          .include.add(resolve('src/icons'))
          .end()
          .use('svg-sprite-loader')
          .loader('svg-sprite-loader')
          .options({
            symbolId: 'icon-[name]'
          })
          .end()
      config.when(process.env.NODE_ENV !== "development", config => {
        // set svg-sprite-loader
        config
          .plugin("ScriptExtHtmlWebpackPlugin")
          .after("html")
          .use("script-ext-html-webpack-plugin", [
            {
              // `runtime` must same as runtimeChunk name. default is `runtime`
              inline: /runtime\..*\.js$/
            }
          ])
          .end();
        config.optimization.minimizer('terser').tap((args) => {
            args[0].terserOptions.compress.drop_console = true
            return args
        })
      });
    },
    devServer: {
      port:8888,
      // 配置代理
      proxy: {
        "/jeecg-boot": {
            target: process.env.PROXY_TARGET,
            ws: false,
            changeOrigin: true, // 设置同源  默认false，是否需要改变原始主机头为目标URL,
            // pathRewrite: {
            //   "^/jeecg-boot": "/" //这里理解成用‘/api’代替target里面的地址，后面组件中我们掉接口时直接用api代替 比如我要调用'http://m.kugou.com/rank/info/?rankid=23784&page=1&json=true'，直接写‘/api/rank/info/?rankid=23784&page=1&json=true’即可
            // }
        },

      },
    }
  };