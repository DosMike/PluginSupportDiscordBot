package com.itwookie.discordbot.logscanning;

import java.io.InputStream;
import java.util.function.Supplier;

public abstract class NamedInputStream implements Supplier<InputStream> {

    String name;

    public NamedInputStream(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static NamedInputStream wrap(String name, Supplier<InputStream> is) {
        return new NamedInputStream(name) {
            @Override
            public InputStream get() {
                return is.get();
            }
        };
    }

}
