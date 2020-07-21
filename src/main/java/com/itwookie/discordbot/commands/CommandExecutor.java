package com.itwookie.discordbot.commands;

public interface CommandExecutor {

    void handle(CommandContext src, Arguments args);

}
