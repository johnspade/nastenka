FROM node:18

WORKDIR /usr/src/app

COPY package.json yarn.lock ./

RUN yarn install --immutable-cache --check-cache

COPY . .

RUN yarn build

FROM nginx:1.25.0

COPY --from=0 /usr/src/app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
