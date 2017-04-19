const path = require('path');
const webpack = require('webpack');
const CopyWebpackPlugin = require('copy-webpack-plugin');
// const pkg = require('./package.json');

// const packages = Object.keys(pkg.dependencies);
// [
// 'classnames',
// 'core-js',
// 'jsoninput',
// 'lodash',
// 'material-ui',
// 'react',
// 'react-dom',
// 'react-tinymce',
// 'recast'
// ];

module.exports = {
    devtool: 'source-map',
    entry: {
        bundle: './src/index.js'
    },
    output: {
        path: path.join(__dirname, 'dist'),
        filename: '[name].js',
        publicPath: 'wegas-react-form/dist/'
    },
    resolve: {
        mainFields: ['module', 'jsnext:main', 'browser', 'main']
    },
    plugins: [
        new webpack.DefinePlugin({
            'process.env': {
                NODE_ENV: JSON.stringify('production')
            }
        }),
        new webpack.optimize.CommonsChunkPlugin({
            name: 'vendor',
            // children: true,
            // async: true,
            minChunks: function minChunks(module) {
                return (
                    module.context &&
                    module.context.indexOf('node_modules') > -1
                );
            }
        }),
        new webpack.optimize.CommonsChunkPlugin({
            name: 'manifest',
            minChunks: Infinity
        }),
        new CopyWebpackPlugin([
            {
                from: 'node_modules/monaco-editor/min/vs',
                to: 'vs'
            }
        ]),
        new webpack.optimize.UglifyJsPlugin({
            compressor: {
                warnings: false
            },
            sourceMap: true,
            comments: false
        })
    ],
    module: {
        rules: [
            {
                test: /\.jsx?$/,
                loaders: ['babel-loader'],
                exclude: /node_modules/
                // include: [
                //     path.join(__dirname, 'src')
                // ]
            },
            {
                test: /\.css$/,
                use: [
                    'style-loader',
                    {
                        loader: 'css-loader',
                        options: {
                            modules: true,
                            importLoaders: 1
                        }
                    },
                    'postcss-loader'
                ]
            }
        ]
    }
};
