const path = require('path')

module.exports = {
    content: [
        path.resolve(__dirname, './*.html'),
        path.resolve(__dirname, `./**/*.js`),
        path.resolve(__dirname, `./**/*.html`),
    ],
    theme: {
        extend: {},
    },
    plugins: [],
}
