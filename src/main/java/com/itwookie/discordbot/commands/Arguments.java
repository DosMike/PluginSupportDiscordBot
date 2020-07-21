package com.itwookie.discordbot.commands;

import net.dv8tion.jda.api.entities.Message;

import java.util.HashMap;
import java.util.Map;

public class Arguments {

    private Message original;
    private String rawArgs;
    private Map<String, String> namedArguments = new HashMap<>();

    public Message getOriginal() {
        return original;
    }
    public String getRawArgs() {
        return rawArgs;
    }
    public boolean hasArg(String name) {
        return namedArguments.containsKey(name);
    }
    public String getArg(String name) {
        return namedArguments.get(name);
    }

    Arguments(Message message, String argString, CommandSpec specs) {
        rawArgs = argString;
        int offset;
        argString = ltrim(argString);
        for (int i=0; i<specs.args(); i++) {
            offset = specs.matchArg(argString, i);
            if (offset>0) {
                namedArguments.put(specs.argName(i), argString.substring(0,offset));
                argString = ltrim(argString.substring(offset));
            }
        }
        if (!argString.trim().isEmpty())
            throw new RuntimeException("Too many arguments");
        original = message;
    }
    private String ltrim(String input) {
        int o=0;
        while (o<input.length() && Character.isWhitespace(input.charAt(o))) o++;
        return input.substring(o);
    }

}
