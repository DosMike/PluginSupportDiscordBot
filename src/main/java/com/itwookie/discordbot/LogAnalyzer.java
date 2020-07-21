package com.itwookie.discordbot;

import com.itwookie.discordbot.logscanning.LogManager;
import com.itwookie.discordbot.logscanning.LogPattern;
import com.itwookie.discordbot.logscanning.LogRegexPattern;

import java.util.regex.Pattern;

public class LogAnalyzer {

    public static void register() {
        // Version info
        LogManager.register(new LogPattern(
                LogPattern.RecordRule.First,
                LogPattern.Severity.Warn,
                (line)->line.contains("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!") ||
                        line.contains("To change this, set \"online-mode\" to \"true\" in the server.properties file."),
                x->"The server is running in offline/insecure mode!"
        ));
        LogManager.register(new LogRegexPattern(
                LogPattern.RecordRule.First,
                LogPattern.Severity.Info,
                Pattern.compile("\\[FML]: Forge Mod Loader version (.*)"),
                m->"FML version "+m.group(1)
        ));

        // General warnings
        LogManager.register(new LogPattern(
                LogPattern.RecordRule.First,
                LogPattern.Severity.Warn,
                (line)->line.contains("The coremod OTGCorePlugin"),
                x->"Found OTG: this might cause problems"
        ));
        LogManager.register(new LogPattern(
                LogPattern.RecordRule.First,
                LogPattern.Severity.Warn,
                (line)->line.contains("at org.spongepowered.asm.mixin.transformer."),
                x->"Found stacktrace about sponge mixin! Is SpongeForge renamed?"
        ));

        // DosMike plugins issues
        LogManager.register(new LogPattern( //LangSwitch probably can't load a "default" locale
                LogPattern.RecordRule.All,
                LogPattern.Severity.Info,
                (line)-> {
                    int index;
                    return ((index=indexAfterIfContains(line, "[langswitch]: Setting default locale to "))>=0 &&
                            !nextWord(line, index).equalsIgnoreCase("en_US"));
                },
                x->"Default Locale in LangSwitch is not en_US"
        ));
        LogManager.register(new LogPattern( //VillagerShops can't read currency by name
                LogPattern.RecordRule.First,
                LogPattern.Severity.Error,
                line->line.contains("at de.dosmike.sponge.vshop.Utilities.CurrencyByName(Utilities.java:"),
                m->"Could not get CurrencyByName - Economy plugin is not properly configured"
        ));
        LogManager.register(new LogPattern( //Translation files for LangSwitch are missing / outdated
                LogPattern.RecordRule.All,
                LogPattern.Severity.Warn,
                line->line.contains("[langswitch]: Missing translation"),
                x->"Translations are missing, is this for a non-default locale or are these files outdate?"
        ));

        // Generic Plugin issues
        LogManager.register(new LogPattern( //Mods are missing
                LogPattern.RecordRule.All,
                LogPattern.Severity.Error,
                (line)->line.contains("MissingModsException: "),
                x->"Missing Mods: "+x.substring(indexAfterIfContains(x,"MissingModsException: "))
        ));
        LogManager.register(new LogPattern( //Plugins are missing
                LogPattern.RecordRule.All,
                LogPattern.Severity.Error,
                (line)->line.contains("[Sponge]: Cannot load plugin "),
                x->"Missing Plugins: "+x.substring(indexAfterIfContains(x," missing the required dependencies "))
        ));
    }

    private static int indexAfterIfContains(String haystack, String needle) {
        int index = haystack.indexOf(needle);
        if (index >= 0) return index+needle.length();
        return -1;
    }
    /**
     * Read the next word. this means skip until a \p{L} is read, then accumulate characters until a \s is read.
     * End of Line will terminate early.
     * @return a word, or empty string if nothing was found
     */
    private static String nextWord(String line, int from) {
        int first=from, last=-1;
        while (!Character.isLetter(line.charAt(from))) if (++first >= line.length()) return "";
        last=first;
        while (!Character.isWhitespace(last)) if (++last >= line.length()) break;
        if (last < first) return "";
        return line.substring(from, last);
    }

}
