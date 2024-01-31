FROM node:20-alpine3.19

ARG NPM_AUTH_USERNAME
ENV NPM_AUTH_USERNAME $NPM_AUTH_USERNAME
ARG NPM_AUTH_TOKEN
ENV NPM_AUTH_TOKEN $NPM_AUTH_TOKEN

WORKDIR /app

COPY package.json .
COPY package-lock.json .
COPY .npmrc .
COPY public ./public
COPY src ./src
COPY tsconfig.json .

RUN npm install

RUN npm run build

RUN npm install -g serve

EXPOSE 3000

CMD ["serve", "-s", "build" , "-l" ,"3000"]