package com.itwookie.discordbot.commands;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class CommandContext {

    public final User user;
    public final TextChannel channel;

    public CommandContext(User _user, TextChannel _channel) {
        user = _user;
        channel = _channel;
    }

}
