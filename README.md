# Nastenka

## Selling pitch (by ChatGPT)

>Are you tired of juggling multiple to-do lists, notes, and documents? Nastenka is here to help!
>
>Nastenka is a self-hosted system that allows you to import and pin all of your important email messages, Telegram messages, files, notes, bookmarks and other content to boards, which we call Investigations. With Nastenka, you can easily keep track of everything you need in one convenient location, making it easy to find what you're looking for when you need it.
>
>You can use Investigations to remember things, plan trips, make to-do lists, and so much more. Whether you're a busy professional, a student, or just looking for a way to stay organized, Nastenka has you covered.
>
>But the best part about Nastenka is that it's completely self-hosted, which means you have full control over your data. You can rest easy knowing that your information is safe and secure, and that you have full control over who has access to it.
>
>So why wait? Get organized and simplify your life with Nastenka today!

## Current features

![](/images/screenshot1.png)
![](/images/screenshot2.png)

- Import emails and Telegram messages to Nastenka by forwarding them, with support for additional content types in the future.
- Skip browser extensions or mobile apps, just forward messages to a Telegram bot or special email address.
- Organize any Pins into Investigations, which are like boards or folders.
- Move Pins around in Investigations to set their importance by dragging them.
- Keep your content private by using Nastenka on your own server and connecting it with your own Telegram bot and email account.

## Setup

- Create a new Telegram bot by messaging [@BotFather](https://t.me/BotFather) on Telegram.
- Create a new email account or alias that you will use to forward emails to Nastenka. Your email provider must support [plus addressing](https://www.fastmail.help/hc/en-us/articles/360060591053-Plus-addressing-and-subdomain-addressing).
- Create a new folder in your email account for Nastenka, and set up rules for automatically moving relevant emails into it. Nastenka uses separate email addresses for each Investigation and they are all in the format `alias+UUID@example.com` where `UUID` is the ID of the Investigation.
- Setup Nastenka on your server using Docker Compose. See the [example docker-compose.yml](/example-docker-compose.yml) for an example configuration. You will need to set up a Postgresql database, a Minio S3-compatible object storage service, and a reverse proxy (e.g. Caddy or Traefik) to handle routing to the frontend and backend servers.
- Create a new bucket in Minio for Nastenka, and set up a policy to allow public access to the bucket.
- Provide the necessary environment variables to the backend server.

**Environment variables:**

- `DATABASE_URL=postgresql://postgres:postgres@localhost:5432/nastenka` - The URL for connecting to the Postgresql database.
- `PORT=8080` - The internal port on which the backend server will run.
- `BOT_USER_ID` - Your Telegram user ID.
- `BOT_TOKEN` - The token for your personal Telegram bot.
- `EMAIL_USER` - The username associated with your email account.
- `EMAIL_PASSWORD` - The password (or application password) associated with your email account.
- `EMAIL_FOLDER=Nastenka` - Name of the folder in your email account where you should move emails from Nastenka. You should create this folder and set up rules for automatically moving relevant emails into it.
- `EMAIL_NASTENKA_ALIAS=nastenka@example.com` - The email address or alias that you will use to forward emails to Nastenka.
- `EMAIL_URL=imap://imap.fastmail.com:993` - The IMAP URL for accessing your email account.
- `AWS_REGION=us-east-1` - This should always be set to `us-east-1` for Minio.
- `AWS_ACCESS_KEY_ID` - Your AWS (Minio) access key ID.
- `AWS_SECRET_ACCESS_KEY` - Your AWS (Minio) secret access key.
- `S3_BUCKET_NAME=nastenka` - The name of the S3 bucket where Nastenka files will be stored.
- `S3_ENDPOINT_URL=http://127.0.0.1:9000` - The endpoint URL for the S3-compatible API provided by the self-hosted Minio service.
- `S3_PUBLIC_BUCKET_URL=http://localhost:9000/nastenka` - The public URL for the Nastenka S3 bucket. The bucket should be configured to allow public access.

## Development

Open three terminal windows or panes and run the following commands:

**Pane 1**
*Installs frontend dependencies and runs the development server*
```shell
cd frontend
yarn install
yarn exec vite
```

**Pane 2**
*Compiles and runs the http server*
```shell
<env-variables> sbt
~ backend/reStart
```

**Pane 3**
*Compiles the frontend JavaScript*
```shell
sbt
~ frontend/fastLinkJS
```

Then open the browser and navigate to [localhost:3000/app/](http://localhost:3000/app/).
