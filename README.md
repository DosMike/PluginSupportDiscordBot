# DiscordBot

This Bot Helps on my Support Discord for my Minecraft Plugins.
It is meant for local hosting but should also run on a server.

## Features:

* Command registration in a similar fashion to the SpongeAPI
* Ore Search using the API Implementation from OreGet when mentioning ``ore:`search` `` in a message
* Automatic Log Analyzer looking through uploads and paste-sites, scanning for common issues.

If you want to add commands, you can look at [this class](https://github.com/DosMike/PluginSupportDiscordBot/blob/master/src/main/java/com/itwookie/discordbot/Commands.java);
If you want to add log patterns, you can look [here](https://github.com/DosMike/PluginSupportDiscordBot/blob/master/src/main/java/com/itwookie/discordbot/LogAnalyzer.java).

To host this bot yourself, you'll have to add your Discord Token into `bot.properties` (That file should auto generate)
