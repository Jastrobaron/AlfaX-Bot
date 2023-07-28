# Alfa-X Bot
---
This is a simple bot for a private Discord server that no longer exists,
so I, as the developer, decided to publish it on GitHub, since the server's owner was fine with it.
This bot features a few simple commands that we were messing around with, and a few other features.
The entire bot is written in Java, using discord4j API.
## Features
- A few simple commands
- Command execution scheduler, if you want to run a certain command at a certain time (Not implemented yet) 
- Simple webserver that allows the developers to use the bot as a webhook
- MQTT client, that allows the bot to send messages to an MQTT broker to, for example, retrieve data from a sensor

## Commands
Note: you can change the prefix in the configuration file or by using using the program argument `--prefix=<prefix>`.
All the commands have to be prefixed with the prefix, otherwise the bot will not respond.
- `help` - Displays a list of commands
- `test` - Sends a test message
- `8ball <question>` - Sends a random answer to a yes/no question
- `pick <option 1> <option 2> [option 3] ...` - Picks a random item from a list
- `today` - Prints out what day it is
- `weather` - Prints out the weather in a certain location (you need to provide the API key in the configuration file)
- `bigtext` - Prints out the text in big letters 
- `mqtt` - Sends a message to an MQTT broker
- `senreg <type> <unit> <min> <max>` - Registers a sensor
- `register` - Registers a user to the permission system
- `usermod <user id> <permission integer>` - Modifies a user's permissions 
- `redeem` - Redeem an admin token, if you're the first admin

## Configuration
You can configure the bot using the input arguments or specify the configuration file using the`--config=<path>` argument.
The configuration file is in `.properties` format. You can generate the default configuration 
file using the `--default-config=<path>` argument.
The configuration file has the following properties:
- `token` - The bot's token
- `prefix` - The bot's prefix
- `weather-api-key` - The OpenWeatherMap API key
- `webserver-port` - The webserver port
- `db-host` - The database host
- `db-user` - The database username
- `db-password` - The database password
- `db-name` - The database name
- `mqtt-uri` - The MQTT broker URI
- `mqtt-username` - The MQTT broker username
- `mqtt-password` - The MQTT broker password

## Database
The bot uses a MySQL database to store the users, their permissions and more. To ensure the best security, you should
create a dedicated user for the database, and only give it the permissions it needs (`SELECT`, `INSERT`, `UPDATE`).
The bot will automatically create the tables it needs, so you don't have to worry about that, 
just please make sure that the database is empty.

## Webserver
To enable the webserver functionality, you must specify the `--webserver-port=<port>` argument.
For now, the webserver can only send messages to certain channels, but in the future, it will be able to do more.
The webserver has the following endpoints (all of them are POST requests in JSON format):
- `/channel_message` - Sends a message to a channel
    - `channel_id` - The channel ID
    - `message` - The message
    - `auth_key` - The authentication key you receive when you use the `register` command
- `/direct_message` - Sends a direct message to a user
    - `user_id` - The user ID
    - `message` - The message
    - `auth_key` - The authentication key
  