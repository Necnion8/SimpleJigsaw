package com.gmail.necnionch.myplugin.simplejigsaw.common.command.errors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NotFoundCommandError extends CommandError {
    private final @Nullable String name;

    public NotFoundCommandError(@Nullable String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getMessage() {
        return "command not found";
    }


    public @Nullable String getName() {
        return name;
    }

}
