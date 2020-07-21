package com.itwookie.discordbot.logscanning;

import com.itwookie.discordbot.Executable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class LogManager implements Runnable {

    private static List<LogPattern> patterns = new LinkedList<>();

    public static void register(LogPattern pattern) {
        patterns.add(pattern);
    }

    Map<LogPattern, LogMatches> matches = new HashMap<>();
    NamedInputStream inputStream;
    TextChannel channel;
    private LogManager(TextChannel _channel, NamedInputStream _stream) {
        channel = _channel;
        inputStream = _stream;
    }

    public static boolean analyzeMessage(Message message) {
        Optional<PasteSiteReader> pasteSite = PasteSiteReader.from(message.getContentDisplay());
        if (pasteSite.isPresent()) {
            Executable.queue(new LogManager(message.getTextChannel(), pasteSite.get()));
            return true;
        }
        List<Message.Attachment> attachments = message.getAttachments().stream()
                .filter(a->"txt".equalsIgnoreCase(a.getFileExtension())||"log".equalsIgnoreCase(a.getFileExtension()))
                .collect(Collectors.toList());
        if (!attachments.isEmpty()) {
            for (Message.Attachment a : attachments) {
                Executable.queue(new LogManager(message.getTextChannel(), NamedInputStream.wrap(a.getFileName(),
                        ()-> {try { return a.retrieveInputStream().get(); } catch (Exception ignore) { return null; }}
                )));
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {
        InputStream in = inputStream.get();
        if (in == null) return;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(in));
            String line; int lno = 0;

            while ((line=br.readLine())!=null) {
                process(line, ++lno);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
            Executable.postNotification("Log "+inputStream.getName(), "Analysis failed: "+e.getMessage());
        }
        finally { try { br.close(); } catch (Exception ignore) {} }

        sendResult();
    }

    public void process(String line, int lineNo) {
        for (LogPattern p : patterns) {
            if (p.requirementMet(matches.values(), lineNo))
                matches.computeIfAbsent(p, LogMatches::new).createMatch(line, lineNo);
        }
    }

    int cntInfo=0,cntWarn=0,cntErr=0;
    /** when all lines are processed, this method will collect the log messages mapped
     * through LogPatterns and return them in a presentable way (as diff for discord) */
    private List<String> collect() {
        List<LogMatches.MatchData> flattened = new LinkedList<>();
        for (LogMatches m : matches.values()) {
            List<LogMatches.MatchData> md = m.getEntries();
            if (m.getPattern().getSeverity() == LogPattern.Severity.Warn) cntWarn+=md.size();
            else if (m.getPattern().getSeverity() == LogPattern.Severity.Error) cntErr+=md.size();
            else cntInfo+=md.size();
            flattened.addAll(md);
        }
        flattened.sort(Comparator.comparingInt(a -> a.line));
        return flattened.stream().map(data->data.logMessage).collect(Collectors.toList());
    }

    private void sendResult() {
        List<String> result = collect();
        if (cntInfo+cntWarn+cntErr==0) {
            Executable.postNotification("Log "+inputStream.getName(), "Analysis without Problems");
        } else {
            channel.sendMessage("Analysis results for "+inputStream.getName()+":\n```diff\n"+String.join("\n", result)+"\n```").complete();
            Executable.postNotification("Log "+inputStream.getName(), String.format("Analysis found i%d w%d e%d Problems", cntInfo, cntWarn, cntErr));
        }
    }

}
