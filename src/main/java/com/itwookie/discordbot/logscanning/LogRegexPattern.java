package com.itwookie.discordbot.logscanning;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogRegexPattern extends LogPattern {

    protected Pattern matcher;
    protected Function<Matcher, String> logger;

    /**
     * @param _rule Describes what occurrences should be recorded
     * @param _severity Is the severity of this log entry
     * @param _pattern Is the regex pattern to compare the line against
     */
    public LogRegexPattern(RecordRule _rule, Severity _severity, Pattern _pattern) {
        super(_rule, _severity, null, null);
        matcher = _pattern;
        logger = Matcher::group;
    }
    /**
     * @param _rule Describes what occurrences should be recorded
     * @param _severity Is the severity of this log entry
     * @param _pattern Is the regex pattern to compare the line against
     * @param _mapper converts the matcher into a string to log
     */
    public LogRegexPattern(RecordRule _rule, Severity _severity, Pattern _pattern, Function<Matcher,String> _mapper) {
        super(_rule, _severity, null, null);
        matcher = _pattern;
        logger = _mapper;
    }
    /**
     * @param _rule Describes what occurrences should be recorded
     * @param _severity Is the severity of this log entry
     * @param _pattern Is the regex pattern to compare the line against
     * @param _mapper converts the matcher into a string to log
     * @param required are LogPatterns that have to have matched before this pattern
     */
    public LogRegexPattern(RecordRule _rule, Severity _severity, Pattern _pattern, Function<Matcher,String> _mapper, LogRegexPattern... required) {
        super(_rule, _severity, null, null, required);
        matcher = _pattern;
        logger = _mapper;
    }

    public Optional<String> map(String line) {
        Matcher match = matcher.matcher(line);
        if (match.find()) {
            return Optional.ofNullable(logger.apply(match));
        }
        return Optional.empty();
    }

}
