package com.gmail.necnionch.myplugin.simplejigsaw.common.command.errors;

import org.jetbrains.annotations.NotNull;

public class InternalCommandError extends CommandError {

    private final Throwable exception;

    public InternalCommandError(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public @NotNull String getMessage() {
        return "internal error";
    }

    public Throwable getException() {
        return exception;
    }

}
