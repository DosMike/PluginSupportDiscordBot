package com.itwookie.discordbot.logscanning;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasteSiteReader extends NamedInputStream implements AutoCloseable {

    InputStream in = null;
    HttpsURLConnection connection = null;
    public PasteSiteReader(String documentName, String targetUrl) {
        super(documentName);
        try {
            connection = (HttpsURLConnection) new URL(targetUrl).openConnection();
            connection.setRequestProperty("User-Agent", "DosMike's Discord Bot/1.0 (Analyzing logs automatically)");
            connection.setRequestProperty("Accept-Encoding", "identity");
            connection.setDoInput(true);
            in = connection.getInputStream();
        } catch (Exception ignore) {}
    }

    @Override
    public InputStream get() {
        return in;
    }

    @Override
    public void close() throws Exception {
        if (in!=null) try { in.close(); } catch (IOException ignore) {}
        if (connection!=null) connection.disconnect();
    }

    private static final Map<Pattern, Function<String, PasteSiteReader>> pasteSites = new HashMap<>();
    static {
        pasteSites.put(Pattern.compile("\\bhttps://pastebin\\.com/(\\w+)\\b", Pattern.CASE_INSENSITIVE),
                (document)->new PasteSiteReader(document,"https://pastebin.com/raw/" + document));
        pasteSites.put(Pattern.compile("\\bhttps://hastebin\\.com/(\\w+)\\.\\w+\\b", Pattern.CASE_INSENSITIVE),
                (document)->new PasteSiteReader(document,"https://hastebin.com/raw/" + document));
        pasteSites.put(Pattern.compile("\\bhttps://p\\.teknik\\.io/(\\w+)\\b", Pattern.CASE_INSENSITIVE),
                (document)->new PasteSiteReader(document,"https://p.teknik.io/Raw/" + document));
        pasteSites.put(Pattern.compile("\\bhttps://paste.gg/p/anonymous/(\\w+)\\b", Pattern.CASE_INSENSITIVE),
                (document)->new PasteSiteReader(document,"https://paste.gg/p/anonymous/" + document + "/files/" + document + "/raw"));
    }
    public static Optional<PasteSiteReader> from(String message) {
        for (Map.Entry<Pattern, Function<String, PasteSiteReader>> entry : pasteSites.entrySet()) {
            Matcher matcher = entry.getKey().matcher(message);
            if (matcher.find()) {
                return Optional.of(entry.getValue().apply(matcher.group(1)));
            }
        }
        return Optional.empty();
    }

}
