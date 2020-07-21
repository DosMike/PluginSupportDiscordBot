package com.itwookie.discordbot.logscanning;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class LogPattern {

    public enum RecordRule { First, Last, All, /** Means, that this is only as flag for reference */Hidden }
    public enum Severity { Info("* [INFO] "), Warn("+ [WARN] "), Error("- [ERR ] ");
        private final String s;
        Severity(String _s) { s=_s; }
        @Override public String toString() { return s; }
    }

    protected List<LogPattern> requires;
    protected Predicate<String> matcher;
    protected UnaryOperator<String> logger;
    protected RecordRule rule;
    protected Severity severity;

    /**
     * @param _rule Describes what occurrences should be recorded
     * @param _severity Is the severity of this log entry
     * @param _matcher Is a predicate to the line, that returns true if this pattern matches the line
     */
    public LogPattern(RecordRule _rule, Severity _severity, Predicate<String> _matcher) {
        matcher = _matcher;
        rule = _rule;
        severity = _severity;
        logger = x->x;
        requires = new LinkedList<>();
    }
    /**
     * @param _rule Describes what occurrences should be recorded
     * @param _severity Is the severity of this log entry
     * @param _matcher Is a predicate to the line, that returns true if this pattern matches the line
     * @param _logger Maps the information in this line into the entry that will be reported back (default x->x)
     */
    public LogPattern(RecordRule _rule, Severity _severity, Predicate<String> _matcher, UnaryOperator<String> _logger) {
        matcher = _matcher;
        rule = _rule;
        severity = _severity;
        logger = _logger;
        requires = new LinkedList<>();
    }
    /**
     * @param _rule Describes what occurrences should be recorded
     * @param _severity Is the severity of this log entry
     * @param _matcher Is a predicate to the line, that returns true if this pattern matches the line
     * @param _logger Maps the information in this line into the entry that will be reported back (default x->x)
     * @param required are LogPatterns that have to have matched before this pattern
     */
    public LogPattern(RecordRule _rule, Severity _severity, Predicate<String> _matcher, UnaryOperator<String> _logger, LogPattern... required) {
        matcher = _matcher;
        rule = _rule;
        severity = _severity;
        logger = _logger;
        requires = Arrays.asList(required);
    }

    public RecordRule getRule() {
        return rule;
    }

    public Severity getSeverity() {
        return severity;
    }

    public Optional<String> map(String line) {
        return matcher.test(line) ? Optional.ofNullable(logger.apply(line)) : Optional.empty();
    }

    public boolean requirementMet(Collection<LogMatches> found, int beforeLine) {
        //required is a list of all patterns that have to have occurred before this pattern
        List<LogPattern> required = new LinkedList<>(requires);
        for (LogMatches m : found) {
            if (required.contains(m.getPattern()) &&
                    m.getMatches().stream().anyMatch(data->data.line<beforeLine))
                //remove pattern if it was matched before
                required.remove(m.getPattern());
        }
        //if all patterns occurred the list will be empty. this means that the requirement is met
        return required.isEmpty();
    }

    @Override
    public String toString() {
        return "LogPattern{ "+severity.name()+" "+rule.name()+" }";
    }
}
