package cn.nukkit.command;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.nukkit.FileLayout;
import cn.nukkit.Server;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.command.defaults.BanCommand;
import cn.nukkit.command.defaults.BanIpCommand;
import cn.nukkit.command.defaults.BanListCommand;
import cn.nukkit.command.defaults.DebugPasteCommand;
import cn.nukkit.command.defaults.DefaultGamemodeCommand;
import cn.nukkit.command.defaults.DeopCommand;
import cn.nukkit.command.defaults.DifficultyCommand;
import cn.nukkit.command.defaults.EffectCommand;
import cn.nukkit.command.defaults.EnchantCommand;
import cn.nukkit.command.defaults.GamemodeCommand;
import cn.nukkit.command.defaults.GameruleCommand;
import cn.nukkit.command.defaults.GarbageCollectorCommand;
import cn.nukkit.command.defaults.GiveCommand;
import cn.nukkit.command.defaults.HelpCommand;
import cn.nukkit.command.defaults.KickCommand;
import cn.nukkit.command.defaults.KillCommand;
import cn.nukkit.command.defaults.ListCommand;
import cn.nukkit.command.defaults.MeCommand;
import cn.nukkit.command.defaults.OpCommand;
import cn.nukkit.command.defaults.PardonCommand;
import cn.nukkit.command.defaults.PardonIpCommand;
import cn.nukkit.command.defaults.ParticleCommand;
import cn.nukkit.command.defaults.PluginsCommand;
import cn.nukkit.command.defaults.ReloadCommand;
import cn.nukkit.command.defaults.SaveCommand;
import cn.nukkit.command.defaults.SaveOffCommand;
import cn.nukkit.command.defaults.SaveOnCommand;
import cn.nukkit.command.defaults.SayCommand;
import cn.nukkit.command.defaults.SeedCommand;
import cn.nukkit.command.defaults.SetWorldSpawnCommand;
import cn.nukkit.command.defaults.SpawnpointCommand;
import cn.nukkit.command.defaults.StatusCommand;
import cn.nukkit.command.defaults.StopCommand;
import cn.nukkit.command.defaults.TeleportCommand;
import cn.nukkit.command.defaults.TellCommand;
import cn.nukkit.command.defaults.TimeCommand;
import cn.nukkit.command.defaults.TimingsCommand;
import cn.nukkit.command.defaults.TitleCommand;
import cn.nukkit.command.defaults.VanillaCommand;
import cn.nukkit.command.defaults.VersionCommand;
import cn.nukkit.command.defaults.WeatherCommand;
import cn.nukkit.command.defaults.WhitelistCommand;
import cn.nukkit.command.defaults.XpCommand;
import cn.nukkit.command.simple.Arguments;
import cn.nukkit.command.simple.CommandParameters;
import cn.nukkit.command.simple.CommandPermission;
import cn.nukkit.command.simple.ForbidConsole;
import cn.nukkit.command.simple.Parameters;
import cn.nukkit.command.simple.SimpleCommand;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.utils.Utils;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class SimpleCommandMap implements CommandMap {
    protected final Map<String, Command> knownCommands = new HashMap<>();

    private final Server server;

	private final FileLayout fs;

    public SimpleCommandMap(final Server server, final FileLayout fs) throws IOException {
        this.server = server;
        this.fs = Objects.requireNonNull(fs);
        this.setDefaultCommands();
    }

    private void setDefaultCommands() throws IOException {
        this.register("nukkit", new VersionCommand("version"));
        this.register("nukkit", new PluginsCommand("plugins"));
        this.register("nukkit", new SeedCommand("seed"));
        this.register("nukkit", new HelpCommand("help"));
        this.register("nukkit", new StopCommand("stop"));
        this.register("nukkit", new TellCommand("tell"));
        this.register("nukkit", new DefaultGamemodeCommand("defaultgamemode"));
        this.register("nukkit", new BanCommand("ban"));
        this.register("nukkit", new BanIpCommand("ban-ip", fs.data().subStore("players")));
        this.register("nukkit", new BanListCommand("banlist"));
        this.register("nukkit", new PardonCommand("pardon"));
        this.register("nukkit", new PardonIpCommand("pardon-ip"));
        this.register("nukkit", new SayCommand("say"));
        this.register("nukkit", new MeCommand("me"));
        this.register("nukkit", new ListCommand("list"));
        this.register("nukkit", new DifficultyCommand("difficulty"));
        this.register("nukkit", new KickCommand("kick"));
        this.register("nukkit", new OpCommand("op"));
        this.register("nukkit", new DeopCommand("deop"));
        this.register("nukkit", new WhitelistCommand("whitelist"));
        this.register("nukkit", new SaveOnCommand("save-on"));
        this.register("nukkit", new SaveOffCommand("save-off"));
        this.register("nukkit", new SaveCommand("save-all"));
        this.register("nukkit", new GiveCommand("give"));
        this.register("nukkit", new EffectCommand("effect"));
        this.register("nukkit", new EnchantCommand("enchant"));
        this.register("nukkit", new ParticleCommand("particle"));
        this.register("nukkit", new GamemodeCommand("gamemode"));
        this.register("nukkit", new GameruleCommand("gamerule"));
        this.register("nukkit", new KillCommand("kill"));
        this.register("nukkit", new SpawnpointCommand("spawnpoint"));
        this.register("nukkit", new SetWorldSpawnCommand("setworldspawn"));
        this.register("nukkit", new TeleportCommand("tp"));
        this.register("nukkit", new TimeCommand("time"));
        this.register("nukkit", new TitleCommand("title"));
        this.register("nukkit", new ReloadCommand("reload"));
        this.register("nukkit", new WeatherCommand("weather"));
        this.register("nukkit", new XpCommand("xp"));

//        if ((boolean) this.server.getConfig("debug.commands", false)) {
        this.register("nukkit", new StatusCommand("status"));
        this.register("nukkit", new GarbageCollectorCommand("gc"));
        this.register("nukkit", new TimingsCommand("timings", fs.data().subStore("timings")));
        this.register("nukkit", new DebugPasteCommand("debugpaste", fs.config(), fs.data()));
        //this.register("nukkit", new DumpMemoryCommand("dumpmemory"));
//        }
    }

    @Override
    public void registerAll(final String fallbackPrefix, final List<? extends Command> commands) {
        for (final Command command : commands) {
            this.register(fallbackPrefix, command);
        }
    }

    @Override
    public boolean register(final String fallbackPrefix, final Command command) {
        return this.register(fallbackPrefix, command, null);
    }

    @Override
    public boolean register(String fallbackPrefix, final Command command, String label) {
        if (label == null) {
            label = command.getName();
        }
        label = label.trim().toLowerCase();
        fallbackPrefix = fallbackPrefix.trim().toLowerCase();

        final boolean registered = this.registerAlias(command, false, fallbackPrefix, label);

        final List<String> aliases = new ArrayList<>(Arrays.asList(command.getAliases()));

        for (final Iterator<String> iterator = aliases.iterator(); iterator.hasNext(); ) {
            final String alias = iterator.next();
            if (!this.registerAlias(command, true, fallbackPrefix, alias)) {
                iterator.remove();
            }
        }
        command.setAliases(aliases.stream().toArray(String[]::new));

        if (!registered) {
            command.setLabel(fallbackPrefix + ":" + label);
        }

        command.register(this);

        return registered;
    }

    @Override
    public void registerSimpleCommands(final Object object) {
        for (final Method method : object.getClass().getDeclaredMethods()) {
            final cn.nukkit.command.simple.Command def = method.getAnnotation(cn.nukkit.command.simple.Command.class);
            if (def != null) {
                final SimpleCommand sc = new SimpleCommand(object, method, def.name(), def.description(), def.usageMessage(), def.aliases());

                final Arguments args = method.getAnnotation(Arguments.class);
                if (args != null) {
                    sc.setMaxArgs(args.max());
                    sc.setMinArgs(args.min());
                }

                final CommandPermission perm = method.getAnnotation(CommandPermission.class);
                if (perm != null) {
                    sc.setPermission(perm.value());
                }

                if (method.isAnnotationPresent(ForbidConsole.class)) {
                    sc.setForbidConsole(true);
                }

                final CommandParameters commandParameters = method.getAnnotation(CommandParameters.class);
                if (commandParameters != null) {
                    final Map<String, CommandParameter[]> map = Arrays.stream(commandParameters.parameters())
                            .collect(Collectors.toMap(Parameters::name, parameters -> Arrays.stream(parameters.parameters())
                                    .map(parameter -> new CommandParameter(parameter.name(), parameter.type(), parameter.optional()))
                                    .distinct()
                                    .toArray(CommandParameter[]::new)));

                    sc.commandParameters.putAll(map);
                }

                this.register(def.name(), sc);
            }
        }
    }

    private boolean registerAlias(final Command command, final boolean isAlias, final String fallbackPrefix, final String label) {
        this.knownCommands.put(fallbackPrefix + ":" + label, command);

        //if you're registering a command alias that is already registered, then return false
        final boolean alreadyRegistered = this.knownCommands.containsKey(label);
        final Command existingCommand = this.knownCommands.get(label);
        final boolean existingCommandIsNotVanilla = alreadyRegistered && !(existingCommand instanceof VanillaCommand);
        //basically, if we're an alias and it's already registered, or we're a vanilla command, then we can't override it
        if ((command instanceof VanillaCommand || isAlias) && alreadyRegistered && existingCommandIsNotVanilla) {
            return false;
        }

        //if you're registering a name (alias or label) which is identical to another command who's primary name is the same
        //so basically we can't override the main name of a command, but we can override aliases if we're not an alias

        //added the last statement which will allow us to override a VanillaCommand unconditionally
        if (alreadyRegistered && existingCommand.getLabel() != null && existingCommand.getLabel().equals(label) && existingCommandIsNotVanilla) {
            return false;
        }

        //you can now assume that the command is either uniquely named, or overriding another command's alias (and is not itself, an alias)

        if (!isAlias) {
            command.setLabel(label);
        }

        // Then we need to check if there isn't any command conflicts with vanilla commands
        final ArrayList<String> toRemove = new ArrayList<String>();

        for (final Entry<String, Command> entry : knownCommands.entrySet()) {
            final Command cmd = entry.getValue();
            if (cmd.getLabel().equalsIgnoreCase(command.getLabel()) && !cmd.equals(command)) { // If the new command conflicts... (But if it isn't the same command)
                if (cmd instanceof VanillaCommand) { // And if the old command is a vanilla command...
                    // Remove it!
                    toRemove.add(entry.getKey());
                }
            }
        }

        // Now we loop the toRemove list to remove the command conflicts from the knownCommands map
        for (final String cmd : toRemove) {
            knownCommands.remove(cmd);
        }

        this.knownCommands.put(label, command);

        return true;
    }

    private ArrayList<String> parseArguments(final String cmdLine) {
        final StringBuilder sb = new StringBuilder(cmdLine);
        final ArrayList<String> args = new ArrayList<>();
        boolean notQuoted = true;
        int start = 0;

        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '\\') {
                sb.deleteCharAt(i);
                continue;
            }

            if (sb.charAt(i) == ' ' && notQuoted) {
                final String arg = sb.substring(start, i);
                if (!arg.isEmpty()) {
                    args.add(arg);
                }
                start = i + 1;
            } else if (sb.charAt(i) == '"') {
                sb.deleteCharAt(i);
                --i;
                notQuoted = !notQuoted;
            }
        }

        final String arg = sb.substring(start);
        if (!arg.isEmpty()) {
            args.add(arg);
        }
        return args;
    }

    @Override
    public boolean dispatch(final CommandSender sender, final String cmdLine) {
        final ArrayList<String> parsed = parseArguments(cmdLine);
        MainLogger.getLogger().debug("Dispatching command "+cmdLine);
        if (parsed.size() == 0) {
            return false;
        }

        final String sentCommandLabel = parsed.remove(0).toLowerCase();
        final String[] args = parsed.toArray(new String[parsed.size()]);
        final Command target = this.getCommand(sentCommandLabel);

        if (target == null) {
        	MainLogger.getLogger().debug(String.format("No target for command %s", sentCommandLabel));
            return false;
        }

        target.timing.startTiming();
        try {
            target.execute(sender, sentCommandLabel, args);
        } catch (final Exception e) {
            sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.exception"));
            this.server.getLogger().critical(this.server.getLanguage().translateString("nukkit.command.exception", cmdLine, target.toString(), Utils.getExceptionMessage(e)));
            final MainLogger logger = sender.getServer().getLogger();
            if (logger != null) {
                logger.logException(e);
            }
        }
        target.timing.stopTiming();

        return true;
    }

    @Override
    public void clearCommands() throws IOException {
        for (final Command command : this.knownCommands.values()) {
            command.unregister(this);
        }
        this.knownCommands.clear();
        this.setDefaultCommands();
    }

    @Override
    public Command getCommand(final String name) {
        if (this.knownCommands.containsKey(name)) {
            return this.knownCommands.get(name);
        }
        return null;
    }

    public Map<String, Command> getCommands() {
        return knownCommands;
    }

    public void registerServerAliases() {
        final Map<String, List<String>> values = this.server.getCommandAliases();
        for (final Map.Entry<String, List<String>> entry : values.entrySet()) {
            final String alias = entry.getKey();
            final List<String> commandStrings = entry.getValue();
            if (alias.contains(" ") || alias.contains(":")) {
                this.server.getLogger().warning(this.server.getLanguage().translateString("nukkit.command.alias.illegal", alias));
                continue;
            }
            final List<String> targets = new ArrayList<>();

            String bad = "";

            for (final String commandString : commandStrings) {
                final String[] args = commandString.split(" ");
                final Command command = this.getCommand(args[0]);

                if (command == null) {
                    if (bad.length() > 0) {
                        bad += ", ";
                    }
                    bad += commandString;
                } else {
                    targets.add(commandString);
                }
            }

            if (bad.length() > 0) {
                this.server.getLogger().warning(this.server.getLanguage().translateString("nukkit.command.alias.notFound", new String[]{alias, bad}));
                continue;
            }

            if (!targets.isEmpty()) {
                this.knownCommands.put(alias.toLowerCase(), new FormattedCommandAlias(alias.toLowerCase(), targets));
            } else {
                this.knownCommands.remove(alias.toLowerCase());
            }
        }
    }
}