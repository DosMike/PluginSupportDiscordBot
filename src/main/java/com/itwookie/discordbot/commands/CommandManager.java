package com.itwookie.discordbot.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.LinkedList;
import java.util.List;

public class CommandManager {

    private static final String commandPrefix = "$";
    private static List<CommandSpec> commands = new LinkedList<>();

    /**
     * @return true if the message appeared to be a command, false otherwise
     * @throws RuntimeException if command parsing failed
     */
    public static boolean parse(Message message) {
        if (message.getMember() == null) return false;
        User from = message.getAuthor();
        CommandContext ctx = new CommandContext(from, message.getTextChannel());

        String input = message.getContentDisplay();
        if (!input.startsWith(commandPrefix)) return false;
        input = input.substring(commandPrefix.length());

        int offset = input.indexOf(' ');
        String name;
        if (offset > 0) {
            name = input.substring(0,offset);
            input = input.substring(offset+1);
        } else {
            name = input;
            input = "";
        }

        for (CommandSpec candidate : commands) {
            if (!candidate.matchName(name)) continue;
            if (!candidate.hasPermission(ctx)) throw new RuntimeException("User is not permitted to call command");

            Arguments args = new Arguments(message, input, candidate);

            candidate.getRunner().handle(ctx, args);

            return true;
        }
        throw new RuntimeException("Arguments could not be applied to any command spec");
    }

    public static void register(CommandSpec... specs) {
        for (CommandSpec spec : specs) {
            if (commands.stream().map(c -> c.nameConflicts(spec)).anyMatch(a -> !a.isEmpty()))
                throw new RuntimeException("Command name conflict"); //could be more descriptive but i'm lazy
            commands.add(spec);
        }
    }

}
