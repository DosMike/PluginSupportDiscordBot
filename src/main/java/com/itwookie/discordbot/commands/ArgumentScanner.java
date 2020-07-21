package com.itwookie.discordbot.commands;

public interface ArgumentScanner {

    /**
     * Tries to find an argument of this type at the start of the input sequence.
     * @return the length of the value found or <1
     */
    int scan(String input);

    default boolean required() { return true; }

}
