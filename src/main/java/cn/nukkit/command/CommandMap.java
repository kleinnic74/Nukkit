package cn.nukkit.command;

import java.io.IOException;
import java.util.List;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public interface CommandMap {

    void registerAll(String fallbackPrefix, List<? extends Command> commands);

    boolean register(String fallbackPrefix, Command command);

    boolean register(String fallbackPrefix, Command command, String label);

    void registerSimpleCommands(Object object);

    boolean dispatch(CommandSender sender, String cmdLine);

    void clearCommands() throws IOException;

    Command getCommand(String name);

}
