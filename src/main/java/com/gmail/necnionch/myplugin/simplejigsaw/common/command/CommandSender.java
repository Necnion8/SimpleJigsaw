package com.gmail.necnionch.myplugin.simplejigsaw.common.command;

import net.md_5.bungee.api.chat.BaseComponent;
import org.jetbrains.annotations.Nullable;

public interface CommandSender {
    void sendMessage(BaseComponent[] components);

    default void sendMessage(BaseComponent component) {
        sendMessage(new BaseComponent[] {component});
    }

    Object getSender();

    boolean hasPermission(String permission);

    boolean hasPermission(Command command);

    @Nullable String getLocale();

}
