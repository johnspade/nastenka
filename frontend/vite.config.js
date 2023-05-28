
import { resolve } from 'path'
import { createHtmlPlugin } from 'vite-plugin-html'

const scalaVersion = '3.2.0'

// https://vitejs.dev/config/
export default ({ mode }) => {
    const mainJS = `./target/scala-${scalaVersion}/nastenka-frontend-${mode === 'production' ? 'opt' : 'fastopt'}/main.js`
    const script = `<script type="module" src="${mainJS}"></script>`

    return {
        plugins: [
            createHtmlPlugin({
                minify: true,
                inject: {
                    data: {
                        script
                    }
                }
            })
        ],
        resolve: {
            alias: {
                'stylesheets': resolve(__dirname, './src/main/static/stylesheets'),
            }
        },
        base: "/app/"
    }
}
