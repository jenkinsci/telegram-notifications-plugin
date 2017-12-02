# TeleJenkins

![TeleJenkins Japan Style](https://pp.vk.me/c636926/v636926471/193d1/fARBefBcfzs.jpg)

This plugin allows **Jenkins** to send notifications via **telegram** bot.

---
 
#### Installation
1. Download latest release 
2. [Manually install](https://jenkins.io/doc/book/managing/plugins/#advanced-installation) *.hpi* plugin to your jenkins

#### Basic usage
##### Create bot
1. Find BotFather in Telegram ([@BotFather](https://t.me/@BotFather))
2. Send */newbot* command 
3. Enter bot name and bot username

##### Global config
1. Open the Jenkins global config
2. Paste your bot name and username to according textfields
3. In filed Usernames fill names of users who can get Jenkins messages (separated by spaces) 
4. Save 

##### Subscribe for Jenkins messages
1. In telegram find your bot and send */start* command
2. Send */sub* command

##### Manage your job
1. Add build-step (or post build-step)
2. Fill the message (you can use environment variables and simple Markdown)
3. Save your job
