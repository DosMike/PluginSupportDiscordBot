package com.itwookie.discordbot.logscanning;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LogMatches {

    private LogPattern pattern;

    public static class MatchData {
        int line;
        String logMessage;

        MatchData(String _message, int _line) {
            logMessage = _message;
            line = _line;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MatchData matchData = (MatchData) o;

            if (line != matchData.line) return false;
            return logMessage.equals(matchData.logMessage);
        }

        @Override
        public int hashCode() {
            int result = line;
            result = 31 * result + logMessage.hashCode();
            return result;
        }
    }

    public LogMatches(LogPattern ofPatter) {
        pattern = ofPatter;
    }

    private List<MatchData> matches = new LinkedList<>();

    public List<MatchData> getMatches() {
        return Collections.unmodifiableList(matches);
    }
    public void addMatch(MatchData match) {
        matches.add(match);
    }

    public void createMatch(String line, int lineNo) {
        pattern.map(line).ifPresent(logentry->matches.add(new MatchData(String.format("%sL%d %s",pattern.getSeverity().toString(),lineNo,logentry), lineNo)));
    }

    public LogPattern getPattern() {
        return pattern;
    }

    /**
     * This method filters matches according to the LogPattern::RecordRule.
     * This means that results might be hidden by this method.
     * If you need all matches unfiltered, use getMatches()
     * @return entries for logging
     */
    public List<MatchData> getEntries() {
        if (pattern.getRule() == LogPattern.RecordRule.Hidden) return Collections.emptyList();
        else if (pattern.getRule() == LogPattern.RecordRule.First) return matches.isEmpty() ? matches : Collections.singletonList(matches.get(0));
        else if (pattern.getRule() == LogPattern.RecordRule.Last) return matches.isEmpty() ? matches : Collections.singletonList(matches.get(matches.size()-1));
        else return matches;
    }

}
