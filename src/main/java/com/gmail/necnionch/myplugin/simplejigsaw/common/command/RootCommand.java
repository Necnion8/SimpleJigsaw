package com.gmail.necnionch.myplugin.simplejigsaw.common.command;

import com.gmail.necnionch.myplugin.simplejigsaw.common.command.errors.CommandError;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.errors.InternalCommandError;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.errors.NotFoundCommandError;
import com.gmail.necnionch.myplugin.simplejigsaw.common.command.errors.PermissionCommandError;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public abstract class RootCommand {
    private final Map<String, Command> commands = new HashMap<>();
    private Command defaultCommand;


    public Command addCommand(@NotNull String name, @Nullable String permission, @NotNull Command.Executor executor, @Nullable Command.TabCompleter completer) {
        Command command = new Command(name, permission, executor, completer);
        commands.put(name.toLowerCase(Locale.ROOT), command);
        return command;
    }

    public Command addCommand(@NotNull String name, @Nullable String permission, @NotNull Command.Executor executor) {
        return addCommand(name, permission, executor, null);
    }

    public Command addCommand(Command command) {
        commands.put(command.getName(), command);
        return command;
    }


    @Nullable
    public Command getCommand(String name) {
        return commands.get(name);
    }

    public Command[] getCommands() {
        return commands.values().toArray(new Command[0]);
    }

    public Map<String, Command> getCommands(CommandSender sender) {
        return commands.values().stream()
                .filter(c -> c.getPermission() == null || sender.hasPermission(c.getPermission()))
                .collect(Collectors.toMap(Command::getName, c -> c));
    }

    public void setDefault(Command command) {
        defaultCommand = command;
    }

    public Command getDefaultCommand() {
        return defaultCommand;
    }


    void execute(CommandSender sender, List<String> args) {
        Command command = null;
        String name;

        try {
            if (args.isEmpty()) {
                command = defaultCommand;
                name = (defaultCommand != null) ? defaultCommand.getName() : null;
            } else {
                name = args.remove(0).toLowerCase(Locale.ROOT);
                command = getCommand(name);
            }

            if (command == null)
                throw new NotFoundCommandError(name);

            if (!sender.hasPermission(command))
                throw new PermissionCommandError();

            command.getExecutor().execute(sender, args);

        } catch (CommandError e) {
            onError(sender, command, e);

        } catch (Throwable e) {
            e.printStackTrace();
            onError(sender, command, new InternalCommandError(e));
        }
    }

    List<String> tabComplete(CommandSender sender, String command, List<String> args) {
        if (args.size() == 1) {
            return generateSuggests(command, getCommands(sender).keySet().toArray(new String[0]));

        } else if (args.size() >= 2) {
            Command cmd = getCommand(command);
            if (cmd != null && sender.hasPermission(cmd) && cmd.getCompleter() != null) {
                args.remove(0);
                return cmd.getCompleter().tabComplete(sender, command, args);
            }
        }

        return Collections.emptyList();
    }



    public static List<String> generateSuggests(String arg, String... arguments) {
        String lowerArg = arg.toLowerCase(Locale.ROOT);
        return Stream.of(arguments)
                .filter(a -> a.toLowerCase(Locale.ROOT).startsWith(lowerArg))
                .collect(Collectors.toList());
    }



    public void onError(@NotNull CommandSender sender, @Nullable Command command, @NotNull CommandError error) {
        sender.sendMessage(new ComponentBuilder("Err: " + error.getMessage()).color(ChatColor.RED).create());
    }


}
