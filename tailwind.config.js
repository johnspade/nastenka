const path = require('path')
const typography = require('@tailwindcss/typography')
const forms = require('@tailwindcss/forms')
const lineClamp = require('@tailwindcss/line-clamp')

module.exports = {
    content: [
        path.resolve(__dirname, './*.html'),
        path.resolve(__dirname, `./**/*.js`),
        path.resolve(__dirname, `./**/*.html`),
    ],
    theme: {
        extend: {},
    },
    corePlugins: {},
    plugins: [path, typography, forms, lineClamp]
}
