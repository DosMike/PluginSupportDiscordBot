package com.itwookie.discordbot.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CommandSpec {

    private List<String> cmdAliases;
    private List<String> argLabels;
    private List<ArgumentScanner> argScanners;
    private CommandExecutor runner;
    private Predicate<CommandContext> permission;

    private CommandSpec(List<String> command, Predicate<CommandContext> permcheck, List<String> names, List<ArgumentScanner> scanners, CommandExecutor executor) {
        cmdAliases = command;
        permission = permcheck;
        argLabels = names;
        argScanners = scanners;
        runner = executor;
    }

    boolean hasPermission(CommandContext user) {
        return permission.test(user);
    }

    boolean matchName(String name) {
        return cmdAliases.contains(name.toLowerCase());
    }

    int args() { return argScanners.size(); }
    int matchArg(String input, int argNo) {
        int result = argScanners.get(argNo).scan(input);
        if (result < 1 && argScanners.get(argNo).required())
            throw new RuntimeException("Argument type did not match for arg "+(argNo+1)+" ("+ argLabels.get(argNo)+")");
        return result;
    }
    String argName(int argNo) {
        return argLabels.get(argNo);
    }

    CommandExecutor getRunner() { return runner; }

    Collection<String> nameConflicts(CommandSpec other) {
        List<String> cpy = new LinkedList<>(cmdAliases);
        cpy.retainAll(other.cmdAliases);
        return cpy;
    }

    //Region builder
    public static class Builder {
        List<String> _names = new LinkedList<>();
        List<ArgumentScanner> _scanners = new LinkedList<>();
        CommandExecutor _executor;
        Predicate<CommandContext> _permission = x->true;
        private Builder() {

        }

        public Builder permission(Predicate<CommandContext> permission) {
            _permission = permission;
            return Builder.this;
        }

        public Builder executor(CommandExecutor executor) {
            _executor = executor;
            return Builder.this;
        }

        public Builder argument(String name, ArgumentScanner type) {
            _names.add(name);
            _scanners.add(type);
            return Builder.this;
        }

        public CommandSpec build(String cmdName, String... aliases) {
            assert(_executor!=null);
            List<String> names = new LinkedList<>();
            names.add(cmdName);
            if (aliases.length>0) names.addAll(Arrays.asList(aliases));
            return new CommandSpec(names.stream().map(String::toLowerCase).collect(Collectors.toList()),
                    _permission, _names, _scanners, _executor);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
    //endregion

}
