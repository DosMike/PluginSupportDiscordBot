package com.itwookie.discordbot;

import com.itwookie.discordbot.commands.CommandManager;
import com.itwookie.discordbot.logscanning.LogManager;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventListener {

    private static final Pattern oreLookup = Pattern.compile("\\bore:`([^`]+)`", Pattern.CASE_INSENSITIVE);

    private static Map<String, String> botChannels = new HashMap<>();

    @SubscribeEvent
    public void onMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getMember()==null) return;
        if (event.getAuthor().isBot()) return; //don't respond to bots
        String botChannel = botChannels.computeIfAbsent(event.getMessage().getGuild().getId(),
                k->event.getMessage().getGuild().getTextChannels().stream()
                        .filter(c-> c.getName().toLowerCase().startsWith("bot"))
                        .map(ISnowflake::getId)
                        .findAny().orElse("") );
        if (!botChannel.isEmpty() && !event.getMessage().getChannel().getId().equals(botChannel)) return; //only respond in bot-chanel if found

        System.out.println("Read message: "+event.getMessage().getContentDisplay());

        // handle ore search queries
        Matcher matcher = oreLookup.matcher(event.getMessage().getContentDisplay());
        while (matcher.find()) {
            Executable.exec.execute(new SearchPlugins(event.getChannel(), matcher.group(1)));
        }

        // try to find logs in the message
        if (LogManager.analyzeMessage(event.getMessage())) return;

        // execute commands, if present
        try {
            CommandManager.parse(event.getMessage());
        } catch (Exception e) {
            System.out.println("Error processing command: "+e.getMessage());
        }
    }

}
