package cn.nukkit.network.rcon;

import cn.nukkit.Server;
import cn.nukkit.command.RemoteConsoleCommandSender;
import cn.nukkit.event.server.RemoteServerCommandEvent;
import cn.nukkit.utils.TextFormat;

import java.io.IOException;

/**
 * Implementation of Source RCON protocol.
 * https://developer.valvesoftware.com/wiki/Source_RCON_Protocol
 * <p>
 * Wrapper for RCONServer. Handles data.
 *
 * @author Tee7even
 */
public class RCON {

    public static void check(RCONServer rcon, Server server) {
        RCONCommand command;
        while ((command = rcon.receive()) != null) {
            RemoteConsoleCommandSender sender = new RemoteConsoleCommandSender();
            RemoteServerCommandEvent event = new RemoteServerCommandEvent(sender, command.getCommand());
            server.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                server.dispatchCommand(sender, command.getCommand());
            }

            rcon.respond(command.getSender(), command.getId(), TextFormat.clean(sender.getMessages()));
        }
    }
}
