const { configure } = require("quasar/wrappers");

module.exports = configure(function (/* ctx */) {
  return {
    boot: ["axios"],

    css: ["app.scss"],

    extras: ["roboto-font", "material-icons"],

    build: {
      target: {
        browser: ["es2019", "edge88", "firefox78", "chrome87", "safari13.1"],
        node: "node20",
      },

      vueRouterMode: "history",

      vitePlugins: [
        [
          "vite-plugin-checker",
          {
            eslint: {
              lintCommand: 'eslint "./**/*.{js,mjs,cjs,vue}"',
            },
          },
          { server: false },
        ],
      ],
    },

    htmlVariables: {
      productName: "LocadoraPablo"
    },

    devServer: {
      open: true,
    },

    framework: {
      config: {},
      plugins: ["Notify"],
    },

    animations: [],

    ssr: {
      pwa: false,
      prodPort: 3000,
      middlewares: ["render"],
    },

    pwa: {
      workboxMode: "generateSW",
      injectPwaMetaTags: true,
      swFilename: "sw.js",
      manifestFilename: "manifest.json",
      useCredentialsForManifestTag: false,
    },

    cordova: {},

    capacitor: {
      hideSplashscreen: true,
    },

    electron: {
      inspectPort: 5858,
      bundler: "packager",
      packager: {},
      builder: {
        appId: "locadora-pablo",
        extendWebpack(cfg) {
          cfg.module.rules.push({
            test: /\.(png|jpe?g|gif|svg)(\?.*)?$/,
            loader: "url-loader",
            options: {
              limit: 10000,
              name: "img/[name].[hash:7].[ext]",
            },
          });
        },
      },
    },

    bex: {
      contentScripts: ["my-content-script"],
    },
  };
});
