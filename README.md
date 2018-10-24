# TeleJenkins [![Build Status](https://ci.jenkins.io/job/Plugins/job/telegram-notifications-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/telegram-notifications-plugin/job/master/)

![TeleJenkins Logo](https://pp.vk.me/c636926/v636926471/193d1/fARBefBcfzs.jpg)

This plugin allows **Jenkins** to send notifications via **telegram** bot.

---
 
## Installation

### Build from source 
1. To build a plugin, run `mvn install`. This will create the file *./target/telegram-notifications.hpi*
2. [Manually install](https://jenkins.io/doc/book/managing/plugins/#advanced-installation) plugin to your Jenkins

### Download an hpi
1. Download _telegram-notifications-*.hpi_ release [here](https://github.com/jenkinsci/telegram-notifications-plugin/releases)
2. [Manually install](https://jenkins.io/doc/book/managing/plugins/#advanced-installation) plugin to your Jenkins

### Plugin Manager
1. Go to *<your_jenkins>/pluginManager/available*
2. Find and install **Telegram Bot**


## Basic usage
### Create bot
1. Find BotFather in Telegram ([@BotFather](https://t.me/@BotFather))
2. Send */newbot* command 
3. Follow the instructions

### Global config
1. Open the Jenkins global config
2. Paste your bot *name* and *token* to according textfields

### Subscribe for Jenkins messages
1. In telegram find your bot and send */start* command
2. Send */sub* command

### Manage your job
1. Add a build step and/or a post build step
2. Fill the message (you can use environment variables and simple Markdown)

### Use in Pipeline
`telegramSend 'Hello World'`

