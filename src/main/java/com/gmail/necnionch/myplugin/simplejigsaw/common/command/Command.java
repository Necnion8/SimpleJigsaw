package com.gmail.necnionch.myplugin.simplejigsaw.common.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Command {

    private final @NotNull String name;
    private final @Nullable String permission;
    private @NotNull Command.Executor executor;
    private @Nullable Command.TabCompleter completer;


    public Command(@NotNull String name, @Nullable String permission, @NotNull Command.Executor executor, @Nullable Command.TabCompleter completer) {
        this.name = name;
        this.permission = permission;
        this.executor = executor;
        this.completer = completer;
    }


    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public String getPermission() {
        return permission;
    }

    @NotNull
    public Command.Executor getExecutor() {
        return executor;
    }

    @Nullable
    public Command.TabCompleter getCompleter() {
        return completer;
    }


    public Command executor(@NotNull Command.Executor executor) {
        this.executor = executor;
        return this;
    }

    public Command completer(@NotNull Command.TabCompleter completer) {
        this.completer = completer;
        return this;
    }



    public interface Executor {
        void execute(CommandSender sender, List<String> args);
    }

    public interface TabCompleter {
        @NotNull List<String> tabComplete(CommandSender sender, String command, List<String> args);
    }

}
