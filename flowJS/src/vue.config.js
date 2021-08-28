const path = require('path');

module.export = {
    runtimeCompiler: true,
    configureWebpack: {
        resolve: {
            alias: {
                '@': path.join(__dirname, 'src/'),
                'assets': path.resolve(__dirname, 'src/assets')
            }
        }
    },
    devServer: {
    },
    chainWebpack: config => {
        config.plugins.delete('prefetch')
    }
}