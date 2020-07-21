package com.itwookie.discordbot;

import com.itwookie.discordbot.commands.Args;
import com.itwookie.discordbot.commands.CommandManager;
import com.itwookie.discordbot.commands.CommandSpec;
import com.itwookie.discordbot.logscanning.LogManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;

import java.awt.*;
import java.util.Random;

public class Commands {

    private static Random RNG = new Random();

    static void register() {

        CommandManager.register(CommandSpec.builder()
                .permission(ctx->!ctx.channel.getGuild().getName().equalsIgnoreCase("SpongePowered"))
                .executor(((src, args) -> src.channel.sendMessage(new MessageBuilder(new EmbedBuilder()
                                .setTitle("How-to Logs")
                                .setColor(Color.YELLOW)
                                .setDescription("Please go to the `logs` folder and upload the `debug.log` file to one of these sites, and then give us the link.")
                                .addField("Paste Sites", "https://paste.gg/\nhttps://paste.feed-the-beast.com/\nhttps://hastebin.com/\nhttps://gist.github.com/", false)
                                .setFooter("Copied from the Sponge Discord :slight_smile:")
                                .build()).build()).submit() ))
                .build("logs"));

        CommandManager.register(CommandSpec.builder()
                .argument("Question", Args.REMAINING_STRING)
                .executor(((src, args) -> {
                    if (!args.hasArg("Question")) {
                        src.channel.sendMessage("Come on, you have to ask a question.").submit();
                    } else {
                        int val = RNG.nextInt(9);
                        String message = "> "+args.getArg("Question")+"\n";
                        if (val == 8) {
                            String[] kek = new String[]{"maybe", "perhaps", "( ͡° ͜ʖ ͡°)", "mayhaps"};
                            message+=kek[RNG.nextInt(kek.length)];
                        } else message+=(val < 4 ? "yes" : "no");
                        src.channel.sendMessage(message).submit();
                    }
                }))
                .build("8ball"));

        CommandManager.register(CommandSpec.builder()
                .permission(ctx->!ctx.channel.getGuild().getName().equalsIgnoreCase("SpongePowered"))
                .executor((src, args) -> src.channel.sendMessage(
                                "Make a test server, and install only half of your mods in it, plus SpongeForge.\n" +
                                        "Does the problem persist? If so, the problem mod is in that half; if not, it's in the other half.\n" +
                                        "Now you know what half it's in. Clear the mods folder, and put half of that half in the folder, plus SpongeForge.\n" +
                                        "Same procedure, and now you know what quarter of your mods the problem mod is in.\n" +
                                        "Do it again to know which eighth, and so forth until there's only one mod aside from SpongeForge.\n" +
                                        "Through this process you can efficiently narrow down what mod is likely causing your problem.\n" +
                                        "(Copied from the Sponge Discord :slight_smile:)").submit() )
                .build("binarysearch"));

        CommandManager.register(CommandSpec.builder()
                .argument("MessageID", Args.INTEGER)
                .executor((src, args) -> {
                    try {
                        long mno = Long.parseLong(args.getArg("MessageID"));
                        src.channel.retrieveMessageById(mno).submit().thenAccept((message)->{
                            if (!LogManager.analyzeMessage(message)) {
                                message.getTextChannel().sendMessage("No logs found").submit();
                            }
                        });
                    } catch (NumberFormatException ignore) {
                        src.channel.sendMessage("Please specify a message id in this channel").submit();
                    }
                })
                .build("analyze", "scan"));

    }



}
