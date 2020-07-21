package com.itwookie.discordbot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public abstract class BotContext implements Runnable {

    private final TextChannel channel;

    protected BotContext(TextChannel channel) {
        this.channel = channel;
    }

    public Guild guild() {
        return channel.getGuild();
    }

    public TextChannel channel() {
        return channel;
    }

}
