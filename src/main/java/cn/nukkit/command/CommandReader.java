package cn.nukkit.command;

import java.io.IOException;
import java.util.Objects;

import cn.nukkit.Server;
import cn.nukkit.event.server.ServerCommandEvent;
import cn.nukkit.utils.concurrent.StoppableRunnable;
import co.aikar.timings.Timings;

/**
 * author: MagicDroidX
 * Nukkit
 */
public class CommandReader implements StoppableRunnable {

    private Console console;

    public CommandReader(Console console) {
    	this.console = Objects.requireNonNull(console);
    }

    public void run() {
        Long lastLine = System.currentTimeMillis();
        String line;

        try {
            while ((line = console.readLine()) != null) {
                if (Server.getInstance().getConsoleSender() == null || Server.getInstance().getPluginManager() == null) {
                    continue;
                }

                if (!line.trim().isEmpty()) {
                    //todo 将即时执行指令改为每tick执行
                    try {
                        Timings.serverCommandTimer.startTiming();
                        ServerCommandEvent event = new ServerCommandEvent(Server.getInstance().getConsoleSender(), line);
                        Server.getInstance().getPluginManager().callEvent(event);
                        if (!event.isCancelled()) {
                            Server.getInstance().getScheduler().scheduleTask(() -> Server.getInstance().dispatchCommand(event.getSender(), event.getCommand()));
                        }
                        Timings.serverCommandTimer.stopTiming();
                    } catch (Exception e) {
                        Server.getInstance().getLogger().logException(e);
                    }

                } else if (System.currentTimeMillis() - lastLine <= 1) {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        Server.getInstance().getLogger().logException(e);
                    }
                }
                lastLine = System.currentTimeMillis();
            }
            console.removePromptLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long shutdown() {
    	console.close();
        return 500;
    }


}
