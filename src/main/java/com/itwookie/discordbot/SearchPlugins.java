package com.itwookie.discordbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.Instant;

public class SearchPlugins extends BotContext {

    private String pluginQuery;
    SearchPlugins(TextChannel channel, String pluginQuery) {
        super(channel);
        this.pluginQuery = pluginQuery;
    }

    @Override
    public void run() {
        Message message = channel().sendMessage("Searching Plugin `"+pluginQuery+"`...").complete();
        channel().sendTyping().complete();
        Executable.ore.projectSearch(pluginQuery,null)
                .filter(plugin->plugin.getPagination().getResultCount()>0)
                .map(plugin->plugin.getResult()[0])
                .map(plugin-> {
                            EmbedBuilder builder = new EmbedBuilder()
                                    .setTitle(plugin.getName(), "https://ore.spongepowered.org/" + plugin.getNamespace().toString())
                                    .setDescription(plugin.getDescription())
                                    .appendDescription("\n:star:" + plugin.getStars() +
                                            " :inbox_tray:" + plugin.getDownloads() +
                                            " :three_button_mouse:" + plugin.getViews() +
                                            " :label:" + (plugin.getPromotedVersions().length > 0 ? plugin.getPromotedVersions()[0].getVersion() : "?")
                                    ).setColor(Integer.parseInt("f6cf17", 16))
                                    .setThumbnail(plugin.getUrlIcon())
                                    .setTimestamp(Instant.ofEpochMilli(plugin.getCreatedAt()))
                                    .setAuthor(plugin.getNamespace().getOwner());
                            return builder.build();
                        }
                ).map(embed-> {
                    message.delete().reason("Display first result for search: `"+pluginQuery+"`").queue();
                    return channel().sendMessage(embed).complete();
                })
                .orElseGet(()->
                    message.editMessage("No results for `"+pluginQuery+"`.").complete()
                );
    }

}
