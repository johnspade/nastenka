const path = require('path')
const colors = require('tailwindcss/colors')
const typography = require('@tailwindcss/typography')
const forms = require('@tailwindcss/forms')

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
    plugins: [typography, forms]
}
