package com.itwookie.discordbot.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Args {

    public static final ArgumentScanner INTEGER = new ArgumentScanner() {
        final Pattern pat = Pattern.compile("^([0-9]+)");
        @Override
        public int scan(String input) {
            Matcher matcher = pat.matcher(input);
            if (matcher.find()) return matcher.end();
            return 0;
        }
    };
    public static final ArgumentScanner FLOAT = new ArgumentScanner() {
        final Pattern pat = Pattern.compile("^((?:[0-9]+|[0-9]*\\.[0-9]+))");
        @Override
        public int scan(String input) {
            Matcher matcher = pat.matcher(input);
            if (matcher.find()) return matcher.end();
            return 0;
        }
    };
    public static final ArgumentScanner STRING = new ArgumentScanner() {
        final Pattern pat = Pattern.compile("^((?:[^\\s]+|\"[^\"]*\"))");
        @Override
        public int scan(String input) {
            Matcher matcher = pat.matcher(input);
            if (matcher.find()) return matcher.end();
            return 0;
        }
    };
    public static final ArgumentScanner REMAINING_STRING = new ArgumentScanner() {
        @Override
        public int scan(String input) {
            return input.length();
        }
    };

    public static class Optional implements ArgumentScanner {
        private ArgumentScanner wrapped;
        Optional(ArgumentScanner wrap) {
            wrap = wrapped;
        }

        @Override
        public int scan(String input) {
            return wrapped.scan(input);
        }

        @Override
        public boolean required() {
            return false;
        }
    }

}
