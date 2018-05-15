package cn.nukkit;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Preconditions;

import cn.nukkit.FileLayout.DataStore;
import cn.nukkit.block.Block;
import cn.nukkit.block.GlobalBlockPalette;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityBeacon;
import cn.nukkit.blockentity.BlockEntityBed;
import cn.nukkit.blockentity.BlockEntityBrewingStand;
import cn.nukkit.blockentity.BlockEntityCauldron;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.blockentity.BlockEntityComparator;
import cn.nukkit.blockentity.BlockEntityEnchantTable;
import cn.nukkit.blockentity.BlockEntityEnderChest;
import cn.nukkit.blockentity.BlockEntityFlowerPot;
import cn.nukkit.blockentity.BlockEntityFurnace;
import cn.nukkit.blockentity.BlockEntityHopper;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.blockentity.BlockEntityJukebox;
import cn.nukkit.blockentity.BlockEntityPistonArm;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.blockentity.BlockEntitySkull;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandReader;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.Console;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.nukkit.command.SimpleCommandMap;
import cn.nukkit.entity.Attribute;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.entity.item.EntityBoat;
import cn.nukkit.entity.item.EntityExpBottle;
import cn.nukkit.entity.item.EntityFallingBlock;
import cn.nukkit.entity.item.EntityFirework;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.entity.item.EntityMinecartEmpty;
import cn.nukkit.entity.item.EntityPainting;
import cn.nukkit.entity.item.EntityPotion;
import cn.nukkit.entity.item.EntityPrimedTNT;
import cn.nukkit.entity.item.EntityXPOrb;
import cn.nukkit.entity.mob.EntityBlaze;
import cn.nukkit.entity.mob.EntityCaveSpider;
import cn.nukkit.entity.mob.EntityCreeper;
import cn.nukkit.entity.mob.EntityElderGuardian;
import cn.nukkit.entity.mob.EntityEnderDragon;
import cn.nukkit.entity.mob.EntityEnderman;
import cn.nukkit.entity.mob.EntityEndermite;
import cn.nukkit.entity.mob.EntityEvoker;
import cn.nukkit.entity.mob.EntityGhast;
import cn.nukkit.entity.mob.EntityGuardian;
import cn.nukkit.entity.mob.EntityHusk;
import cn.nukkit.entity.mob.EntityMagmaCube;
import cn.nukkit.entity.mob.EntityShulker;
import cn.nukkit.entity.mob.EntitySilverfish;
import cn.nukkit.entity.mob.EntitySkeleton;
import cn.nukkit.entity.mob.EntitySlime;
import cn.nukkit.entity.mob.EntitySpider;
import cn.nukkit.entity.mob.EntityStray;
import cn.nukkit.entity.mob.EntityVex;
import cn.nukkit.entity.mob.EntityVindicator;
import cn.nukkit.entity.mob.EntityWitch;
import cn.nukkit.entity.mob.EntityWither;
import cn.nukkit.entity.mob.EntityWitherSkeleton;
import cn.nukkit.entity.mob.EntityZombie;
import cn.nukkit.entity.mob.EntityZombiePigman;
import cn.nukkit.entity.mob.EntityZombieVillager;
import cn.nukkit.entity.passive.EntityBat;
import cn.nukkit.entity.passive.EntityChicken;
import cn.nukkit.entity.passive.EntityCow;
import cn.nukkit.entity.passive.EntityDonkey;
import cn.nukkit.entity.passive.EntityHorse;
import cn.nukkit.entity.passive.EntityLlama;
import cn.nukkit.entity.passive.EntityMooshroom;
import cn.nukkit.entity.passive.EntityMule;
import cn.nukkit.entity.passive.EntityOcelot;
import cn.nukkit.entity.passive.EntityPig;
import cn.nukkit.entity.passive.EntityPolarBear;
import cn.nukkit.entity.passive.EntityRabbit;
import cn.nukkit.entity.passive.EntitySheep;
import cn.nukkit.entity.passive.EntitySkeletonHorse;
import cn.nukkit.entity.passive.EntitySquid;
import cn.nukkit.entity.passive.EntityVillager;
import cn.nukkit.entity.passive.EntityWolf;
import cn.nukkit.entity.projectile.EntityArrow;
import cn.nukkit.entity.projectile.EntityEgg;
import cn.nukkit.entity.projectile.EntityEnderPearl;
import cn.nukkit.entity.projectile.EntitySnowball;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.level.LevelInitEvent;
import cn.nukkit.event.level.LevelLoadEvent;
import cn.nukkit.event.server.QueryRegenerateEvent;
import cn.nukkit.inventory.CraftingManager;
import cn.nukkit.inventory.Recipe;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.lang.BaseLang;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.level.EnumLevel;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.biome.EnumBiome;
import cn.nukkit.level.format.LevelProvider;
import cn.nukkit.level.format.LevelProviderManager;
import cn.nukkit.level.format.anvil.Anvil;
import cn.nukkit.level.format.leveldb.LevelDB;
import cn.nukkit.level.format.mcregion.McRegion;
import cn.nukkit.level.generator.Flat;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.level.generator.Nether;
import cn.nukkit.level.generator.Normal;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.metadata.EntityMetadataStore;
import cn.nukkit.metadata.LevelMetadataStore;
import cn.nukkit.metadata.PlayerMetadataStore;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.CompressBatchedTask;
import cn.nukkit.network.Network;
import cn.nukkit.network.RakNetInterface;
import cn.nukkit.network.SourceInterface;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.PlayerListPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.query.QueryHandler;
import cn.nukkit.network.rcon.RCON;
import cn.nukkit.network.rcon.RCONServer;
import cn.nukkit.permission.BanEntry;
import cn.nukkit.permission.BanList;
import cn.nukkit.permission.DefaultPermissions;
import cn.nukkit.permission.Permissible;
import cn.nukkit.plugin.JavaPluginLoader;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginLoadOrder;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.plugin.service.NKServiceManager;
import cn.nukkit.plugin.service.ServiceManager;
import cn.nukkit.potion.Effect;
import cn.nukkit.potion.Potion;
import cn.nukkit.resourcepacks.ResourcePackManager;
import cn.nukkit.scheduler.FileWriteTask;
import cn.nukkit.scheduler.ServerScheduler;
import cn.nukkit.utils.Binary;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.LevelException;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.ServerException;
import cn.nukkit.utils.ServerKiller;
import cn.nukkit.utils.SoftwareVersion;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.utils.Utils;
import cn.nukkit.utils.Watchdog;
import cn.nukkit.utils.Zlib;
import cn.nukkit.utils.bugreport.ExceptionHandler;
import cn.nukkit.utils.completers.CommandsCompleter;
import cn.nukkit.utils.completers.PlayersCompleter;
import cn.nukkit.utils.concurrent.NamedExecutorService;
import co.aikar.timings.Timings;

/**
 * @author MagicDroidX
 * @author Box
 */
public class Server {

	public static final String BROADCAST_CHANNEL_ADMINISTRATIVE = "nukkit.broadcast.admin";
	public static final String BROADCAST_CHANNEL_USERS = "nukkit.broadcast.user";

	private static Server instance = null;

	private BanList banByName = null;

	private BanList banByIP = null;

	private Config operators = null;

	private Config whitelist = null;

	private boolean isRunning = true;

	private boolean hasStopped = false;

	private PluginManager pluginManager = null;

	private final int profilingTickrate = 20;

	private ServerScheduler scheduler = null;

	private int tickCounter;

	private long nextTick;

	private final float[] tickAverage = { 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
			20 };

	private final float[] useAverage = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	private float maxTick = 20;

	private float maxUse = 0;

	private int sendUsageTicker = 0;

	private final boolean dispatchSignals = false;

	private final MainLogger logger;

	private final CommandReader consoleCommandReader;

	private SimpleCommandMap commandMap;

	private CraftingManager craftingManager;

	private ResourcePackManager resourcePackManager;

	private ConsoleCommandSender consoleSender;

	private int maxPlayers;

	private boolean autoSave;

	private RCONServer rconServer;

	private EntityMetadataStore entityMetadata;

	private PlayerMetadataStore playerMetadata;

	private LevelMetadataStore levelMetadata;

	private Network network;

	private boolean networkCompressionAsync = true;
	public int networkCompressionLevel = 7;
	private int networkZlibProvider = 0;

	private boolean autoTickRate = true;
	private int autoTickRateLimit = 20;
	private boolean alwaysTickPlayers = false;
	private int baseTickRate = 1;
	private Boolean getAllowFlight = null;
	private int difficulty = Integer.MAX_VALUE;
	private int defaultGamemode = Integer.MAX_VALUE;

	private int autoSaveTicker = 0;
	private int autoSaveTicks = 6000;

	private BaseLang baseLang;

	private boolean forceLanguage = false;

	private UUID serverID;

	private final Set<UUID> uniquePlayers = new HashSet<>();

	private QueryHandler queryHandler;

	private QueryRegenerateEvent queryRegenerateEvent;

	private Config properties;
	private Config config;

	private final Map<String, Player> players = new HashMap<>();

	private final Map<UUID, Player> playerList = new HashMap<>();

	private final Map<Integer, String> identifier = new HashMap<>();

	private final Map<Integer, Level> levels = new HashMap<Integer, Level>() {
		@Override
		public Level put(final Integer key, final Level value) {
			final Level result = super.put(key, value);
			levelArray = levels.values().toArray(new Level[levels.size()]);
			return result;
		}

		@Override
		public boolean remove(final Object key, final Object value) {
			final boolean result = super.remove(key, value);
			levelArray = levels.values().toArray(new Level[levels.size()]);
			return result;
		}

		@Override
		public Level remove(final Object key) {
			final Level result = super.remove(key);
			levelArray = levels.values().toArray(new Level[levels.size()]);
			return result;
		}
	};

	private Level[] levelArray = new Level[0];

	private final ServiceManager serviceManager = new NKServiceManager();

	private Level defaultLevel = null;

	private final Thread currentThread;

	private NamedExecutorService executor;
	private FileLayout fs;
	private DataStore worldsDb;
	private DataStore playersDb;

	Server(final MainLogger logger, final Console console, final FileLayout fs, final NamedExecutorService executor) throws IOException {
		Preconditions.checkState(instance == null, "Already initialized!");
		currentThread = Thread.currentThread(); // Saves the current thread instance as a reference, used in
												// Server#isPrimaryThread()
		instance = this;
		this.logger = logger;
		this.executor = Objects.requireNonNull(executor);
		this.fs = Objects.requireNonNull(fs);
		worldsDb = fs.data().subStore("worlds");
		playersDb = fs.data().subStore("players");

		console.addCompleter(new PlayersCompleter()); // Add player TAB completer
		console.addCompleter(new CommandsCompleter()); // Add command TAB completer
		this.consoleCommandReader = new CommandReader(console);
		// todo: VersionString 现在不必要

		final SoftwareVersion sw = SoftwareVersion.get();
		this.getLogger().info(TextFormat.GREEN + "Nukkit " + sw.getCommitId() + "@" + sw.getGitUrl() + " [branch:"
				+ sw.getBranch() + "]");
		final File configFile = fs.config().file("nukkit.yml");
		if (!configFile.exists()) {
			this.getLogger().info(TextFormat.GREEN + "Welcome! Please choose a language first!");
			try {
				final InputStream languageList = this.getClass().getClassLoader().getResourceAsStream("lang/language.list");
				if (languageList == null) {
					throw new RuntimeException(
							"lang/language.list is missing. If you are running a development version, make sure you have run 'git submodule update --init'.");
				}
				final String[] lines = Utils.readFile(languageList).split("\n");
				for (final String line : lines) {
					this.getLogger().info(line);
				}
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}

			final String fallback = BaseLang.FALLBACK_LANGUAGE;
			String language = null;
			while (language == null) {
				final String lang = console.readLine();
				final InputStream conf = this.getClass().getClassLoader().getResourceAsStream("lang/" + lang + "/lang.ini");
				if (conf != null) {
					language = lang;
				}
			}

			InputStream advacedConf = this.getClass().getClassLoader()
					.getResourceAsStream("lang/" + language + "/nukkit.yml");
			if (advacedConf == null) {
				advacedConf = this.getClass().getClassLoader().getResourceAsStream("lang/" + fallback + "/nukkit.yml");
			}

			try {
				Utils.writeFile(configFile.getAbsolutePath(), advacedConf);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}

		}

		this.executor.launch("Console", consoleCommandReader);

		this.logger.info("Loading " + TextFormat.GREEN + configFile.getAbsolutePath() + TextFormat.WHITE + "...");
		this.config = new Config(configFile.getAbsolutePath(), Config.YAML);

		final File propertiesFile = fs.config().file("server.properties");
		this.logger.info("Loading " + TextFormat.GREEN + propertiesFile.getAbsolutePath() + TextFormat.WHITE + "...");
		this.properties = new Config(propertiesFile.getAbsolutePath(), Config.PROPERTIES, new ConfigSection() {
			{
				put("motd", "Nukkit Server For Minecraft: PE");
				put("sub-motd", "Powered by Nukkit");
				put("server-port", 19132);
				put("server-ip", "0.0.0.0");
				put("view-distance", 10);
				put("white-list", false);
				put("achievements", true);
				put("announce-player-achievements", true);
				put("spawn-protection", 16);
				put("max-players", 20);
				put("allow-flight", false);
				put("spawn-animals", true);
				put("spawn-mobs", true);
				put("gamemode", 0);
				put("force-gamemode", false);
				put("hardcore", false);
				put("pvp", true);
				put("difficulty", 1);
				put("generator-settings", "");
				put("level-name", "world");
				put("level-seed", "");
				put("level-type", "DEFAULT");
				put("enable-query", true);
				put("enable-rcon", false);
				put("rcon.password", Base64.getEncoder()
						.encodeToString(UUID.randomUUID().toString().replace("-", "").getBytes()).substring(3, 13));
				put("auto-save", true);
				put("force-resources", false);
				put("bug-report", true);
				put("xbox-auth", true);
			}
		});

		this.forceLanguage = (Boolean) this.getConfig("settings.force-language", false);
		this.baseLang = new BaseLang((String) this.getConfig("settings.language", BaseLang.FALLBACK_LANGUAGE));
		this.logger.info(this.getLanguage().translateString("language.selected",
				new String[] { getLanguage().getName(), getLanguage().getLang() }));
		this.logger.info(getLanguage().translateString("nukkit.server.start",
				TextFormat.AQUA + this.getVersion() + TextFormat.WHITE));

		Object poolSize = this.getConfig("settings.async-workers", "auto");
		if (!(poolSize instanceof Integer)) {
			try {
				poolSize = Integer.valueOf((String) poolSize);
			} catch (final Exception e) {
				poolSize = Math.max(Runtime.getRuntime().availableProcessors() + 1, 4);
			}
		}

		ServerScheduler.WORKERS = (int) poolSize;

		this.networkZlibProvider = (int) this.getConfig("network.zlib-provider", 2);
		Zlib.setProvider(this.networkZlibProvider);

		this.networkCompressionLevel = (int) this.getConfig("network.compression-level", 7);
		this.networkCompressionAsync = (boolean) this.getConfig("network.async-compression", true);

		this.autoTickRate = (boolean) this.getConfig("level-settings.auto-tick-rate", true);
		this.autoTickRateLimit = (int) this.getConfig("level-settings.auto-tick-rate-limit", 20);
		this.alwaysTickPlayers = (boolean) this.getConfig("level-settings.always-tick-players", false);
		this.baseTickRate = (int) this.getConfig("level-settings.base-tick-rate", 1);

		this.scheduler = new ServerScheduler();

		if (this.getPropertyBoolean("enable-rcon", false)) {
			try {
				this.rconServer = new RCONServer(getLanguage(), getLogger(),
						(!this.getIp().equals("")) ? this.getIp() : "0.0.0.0",
						this.getPropertyInt("rcon.port", this.getPort()), this.getPropertyString("rcon.password", ""));
				executor.launch("RCON", rconServer);
			} catch (final IOException exception) {
				getLogger().critical(
						getLanguage().translateString("nukkit.server.rcon.startupError", exception.getMessage()));
				return;
			}
		}

		this.entityMetadata = new EntityMetadataStore();
		this.playerMetadata = new PlayerMetadataStore();
		this.levelMetadata = new LevelMetadataStore();

		this.operators = new Config(fs.data().file("ops.txt"), Config.ENUM);
		this.whitelist = new Config(fs.data().file("white-list.txt"), Config.ENUM);
		this.banByName = new BanList(fs.data().file("banned-players.json").getAbsolutePath());
		this.banByName.load();
		this.banByIP = new BanList(fs.data().file("banned-ips.json").getAbsolutePath());
		this.banByIP.load();

		this.maxPlayers = this.getPropertyInt("max-players", 20);
		this.setAutoSave(this.getPropertyBoolean("auto-save", true));

		if (this.getPropertyBoolean("hardcore", false) && this.getDifficulty() < 3) {
			this.setPropertyInt("difficulty", 3);
		}

		Nukkit.DEBUG = (int) this.getConfig("debug.level", 1);
		if (this.logger instanceof MainLogger) {
			this.logger.setLogDebug(Nukkit.DEBUG > 1);
		}

		if (this.getConfig().getBoolean("bug-report", true)) {
			ExceptionHandler.registerExceptionHandler();
		}

		this.logger.info(this.getLanguage().translateString("nukkit.server.networkStart",
				new String[] { this.getIp().equals("") ? "*" : this.getIp(), String.valueOf(this.getPort()) }));
		this.serverID = UUID.randomUUID();

		this.network = new Network(this);
		this.network.setName(this.getMotd());
		this.network.setSubName(this.getSubMotd());

		this.logger.info(this.getLanguage().translateString("nukkit.server.info", this.getName(),
				TextFormat.YELLOW + this.getNukkitVersion() + TextFormat.WHITE,
				TextFormat.AQUA + this.getCodename() + TextFormat.WHITE, this.getApiVersion()));
		this.logger.info(this.getLanguage().translateString("nukkit.server.license", this.getName()));

		this.consoleSender = new ConsoleCommandSender();
		this.commandMap = new SimpleCommandMap(this, fs);

		this.registerEntities();
		this.registerBlockEntities();

		Block.init();
		Enchantment.init();
		Item.init(fs.data());
		EnumBiome.values(); // load class, this also registers biomes
		Effect.init();
		Potion.init();
		Attribute.init();
		GlobalBlockPalette.getOrCreateRuntimeId(0, 0); // Force it to load

		this.craftingManager = new CraftingManager(fs.data());
		this.resourcePackManager = new ResourcePackManager(new File(Nukkit.DATA_PATH, "resource_packs"));

		this.pluginManager = new PluginManager(this, this.commandMap);
		this.pluginManager.subscribeToPermission(Server.BROADCAST_CHANNEL_ADMINISTRATIVE, this.consoleSender);

		this.pluginManager.registerInterface(JavaPluginLoader.class);

		this.queryRegenerateEvent = new QueryRegenerateEvent(this, 5);

		final RakNetInterface raknet = new RakNetInterface(this);
		this.network.registerInterface(raknet);
		executor.launch("RakNet", raknet);
		
		this.pluginManager.loadPlugins(fs.plugins().basedir());

		this.enablePlugins(PluginLoadOrder.STARTUP);

		LevelProviderManager.addProvider(this, Anvil.class);
		LevelProviderManager.addProvider(this, McRegion.class);
		LevelProviderManager.addProvider(this, LevelDB.class);

		Generator.addGenerator(Flat.class, "flat", Generator.TYPE_FLAT);
		Generator.addGenerator(Normal.class, "normal", Generator.TYPE_INFINITE);
		Generator.addGenerator(Normal.class, "default", Generator.TYPE_INFINITE);
		Generator.addGenerator(Nether.class, "nether", Generator.TYPE_NETHER);
		// todo: add old generator and hell generator

		for (final String name : ((Map<String, Object>) this.getConfig("worlds", new HashMap<>())).keySet()) {
			if (!this.loadLevel(name)) {
				long seed;
				try {
					seed = ((Integer) this.getConfig("worlds." + name + ".seed")).longValue();
				} catch (final Exception e) {
					seed = System.currentTimeMillis();
				}

				final Map<String, Object> options = new HashMap<>();
				final String[] opts = ((String) this.getConfig("worlds." + name + ".generator",
						Generator.getGenerator("default").getSimpleName())).split(":");
				final Class<? extends Generator> generator = Generator.getGenerator(opts[0]);
				if (opts.length > 1) {
					String preset = "";
					for (int i = 1; i < opts.length; i++) {
						preset += opts[i] + ":";
					}
					preset = preset.substring(0, preset.length() - 1);

					options.put("preset", preset);
				}

				this.generateLevel(name, seed, generator, options);
			}
		}

		if (this.getDefaultLevel() == null) {
			String defaultName = this.getPropertyString("level-name", "world");
			if (defaultName == null || defaultName.trim().isEmpty()) {
				this.getLogger().warning("level-name cannot be null, using default");
				defaultName = "world";
				this.setPropertyString("level-name", defaultName);
			}

			if (!this.loadLevel(defaultName)) {
				long seed;
				final String seedString = String.valueOf(this.getProperty("level-seed", System.currentTimeMillis()));
				try {
					seed = Long.valueOf(seedString);
				} catch (final NumberFormatException e) {
					seed = seedString.hashCode();
				}
				this.generateLevel(defaultName, seed == 0 ? System.currentTimeMillis() : seed);
			}

			this.setDefaultLevel(this.getLevelByName(defaultName));
		}

		this.properties.save(true);

		if (this.getDefaultLevel() == null) {
			this.getLogger().emergency(this.getLanguage().translateString("nukkit.level.defaultError"));
			this.forceShutdown();

			return;
		}

		EnumLevel.initLevels();

		if ((int) this.getConfig("ticks-per.autosave", 6000) > 0) {
			this.autoSaveTicks = (int) this.getConfig("ticks-per.autosave", 6000);
		}

		this.enablePlugins(PluginLoadOrder.POSTWORLD);

		executor.launch("Watchdog", new Watchdog(this, 60000));

		this.start();
	}

	public int broadcastMessage(final String message) {
		return this.broadcast(message, BROADCAST_CHANNEL_USERS);
	}

	public int broadcastMessage(final TextContainer message) {
		return this.broadcast(message, BROADCAST_CHANNEL_USERS);
	}

	public int broadcastMessage(final String message, final CommandSender[] recipients) {
		for (final CommandSender recipient : recipients) {
			recipient.sendMessage(message);
		}

		return recipients.length;
	}

	public int broadcastMessage(final String message, final Collection<CommandSender> recipients) {
		for (final CommandSender recipient : recipients) {
			recipient.sendMessage(message);
		}

		return recipients.size();
	}

	public int broadcastMessage(final TextContainer message, final Collection<CommandSender> recipients) {
		for (final CommandSender recipient : recipients) {
			recipient.sendMessage(message);
		}

		return recipients.size();
	}

	public int broadcast(final String message, final String permissions) {
		final Set<CommandSender> recipients = new HashSet<>();

		for (final String permission : permissions.split(";")) {
			for (final Permissible permissible : this.pluginManager.getPermissionSubscriptions(permission)) {
				if (permissible instanceof CommandSender && permissible.hasPermission(permission)) {
					recipients.add((CommandSender) permissible);
				}
			}
		}

		for (final CommandSender recipient : recipients) {
			recipient.sendMessage(message);
		}

		return recipients.size();
	}

	public int broadcast(final TextContainer message, final String permissions) {
		final Set<CommandSender> recipients = new HashSet<>();

		for (final String permission : permissions.split(";")) {
			for (final Permissible permissible : this.pluginManager.getPermissionSubscriptions(permission)) {
				if (permissible instanceof CommandSender && permissible.hasPermission(permission)) {
					recipients.add((CommandSender) permissible);
				}
			}
		}

		for (final CommandSender recipient : recipients) {
			recipient.sendMessage(message);
		}

		return recipients.size();
	}

	public static void broadcastPacket(final Collection<Player> players, final DataPacket packet) {
		broadcastPacket(players.stream().toArray(Player[]::new), packet);
	}

	public static void broadcastPacket(final Player[] players, final DataPacket packet) {
		packet.encode();
		packet.isEncoded = true;

		if (packet.pid() == ProtocolInfo.BATCH_PACKET) {
			for (final Player player : players) {
				player.dataPacket(packet);
			}
		} else {
			getInstance().batchPackets(players, new DataPacket[] { packet }, true);
		}

		if (packet.encapsulatedPacket != null) {
			packet.encapsulatedPacket = null;
		}
	}

	public void batchPackets(final Player[] players, final DataPacket[] packets) {
		this.batchPackets(players, packets, false);
	}

	public void batchPackets(final Player[] players, final DataPacket[] packets, final boolean forceSync) {
		if (players == null || packets == null || players.length == 0 || packets.length == 0) {
			return;
		}

		Timings.playerNetworkSendTimer.startTiming();
		final byte[][] payload = new byte[packets.length * 2][];
		int size = 0;
		for (int i = 0; i < packets.length; i++) {
			final DataPacket p = packets[i];
			if (!p.isEncoded) {
				p.encode();
			}
			final byte[] buf = p.getBuffer();
			payload[i * 2] = Binary.writeUnsignedVarInt(buf.length);
			payload[i * 2 + 1] = buf;
			packets[i] = null;
			size += payload[i * 2].length;
			size += payload[i * 2 + 1].length;
		}

		final List<String> targets = new ArrayList<>();
		for (final Player p : players) {
			if (p.isConnected()) {
				targets.add(this.identifier.get(p.rawHashCode()));
			}
		}

		if (!forceSync && this.networkCompressionAsync) {
			this.getScheduler()
					.scheduleAsyncTask(new CompressBatchedTask(payload, targets, this.networkCompressionLevel));
		} else {
			try {
				final byte[] data = Binary.appendBytes(payload);
				this.broadcastPacketsCallback(Zlib.deflate(data, this.networkCompressionLevel), targets);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}
		Timings.playerNetworkSendTimer.stopTiming();
	}

	public void broadcastPacketsCallback(final byte[] data, final List<String> identifiers) {
		final BatchPacket pk = new BatchPacket();
		pk.payload = data;

		for (final String i : identifiers) {
			if (this.players.containsKey(i)) {
				this.players.get(i).dataPacket(pk);
			}
		}
	}

	public void enablePlugins(final PluginLoadOrder type) {
		for (final Plugin plugin : new ArrayList<>(this.pluginManager.getPlugins().values())) {
			if (!plugin.isEnabled() && type == plugin.getDescription().getOrder()) {
				this.enablePlugin(plugin);
			}
		}

		if (type == PluginLoadOrder.POSTWORLD) {
			this.commandMap.registerServerAliases();
			DefaultPermissions.registerCorePermissions();
		}
	}

	public void enablePlugin(final Plugin plugin) {
		this.pluginManager.enablePlugin(plugin);
	}

	public void disablePlugins() {
		this.pluginManager.disablePlugins();
	}

	public boolean dispatchCommand(final CommandSender sender, final String commandLine) throws ServerException {
		// First we need to check if this command is on the main thread or not, if not,
		// warn the user
		if (!this.isPrimaryThread()) {
			getLogger().warning("Command Dispatched Async: " + commandLine);
			getLogger().warning("Please notify author of plugin causing this execution to fix this bug!",
					new Throwable());
			// TODO: We should sync the command to the main thread too!
		}
		if (sender == null) {
			throw new ServerException("CommandSender is not valid");
		}

		if (this.commandMap.dispatch(sender, commandLine)) {
			return true;
		}

		sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.generic.unknown", commandLine));

		return false;
	}

	// todo: use ticker to check console
	public ConsoleCommandSender getConsoleSender() {
		return consoleSender;
	}

	public void reload() {
		this.logger.info("Reloading...");

		try {
			this.logger.info("Saving levels...");

			for (final Level level : this.levelArray) {
				level.save();
			}

			this.pluginManager.disablePlugins();
			this.pluginManager.clearPlugins();
			this.commandMap.clearCommands();

			this.logger.info("Reloading properties...");
			this.properties.reload();
			this.maxPlayers = this.getPropertyInt("max-players", 20);

			if (this.getPropertyBoolean("hardcore", false) && this.getDifficulty() < 3) {
				this.setPropertyInt("difficulty", difficulty = 3);
			}

			this.banByIP.load();
			this.banByName.load();
			this.reloadWhitelist();
			this.operators.reload();

			for (final BanEntry entry : this.getIPBans().getEntires().values()) {
				this.getNetwork().blockAddress(entry.getName(), -1);
			}

			this.pluginManager.registerInterface(JavaPluginLoader.class);
			this.pluginManager.loadPlugins(fs.plugins().basedir());
			this.enablePlugins(PluginLoadOrder.STARTUP);
			this.enablePlugins(PluginLoadOrder.POSTWORLD);
			Timings.reset();
		} catch(final IOException e) {
			MainLogger.getLogger().logException(e);
		}
	}

	public void shutdown() {
		if (this.isRunning) {
			final ServerKiller killer = new ServerKiller(90);
			killer.start();
		}
		this.isRunning = false;
	}

	public void forceShutdown() {
		if (this.hasStopped) {
			return;
		}

		try {
			if (!this.isRunning) {
				// todo sendUsage
			}

			executor.shutdown();

			this.hasStopped = true;

			this.shutdown();

			this.getLogger().debug("Disabling all plugins");
			this.pluginManager.disablePlugins();

			for (final Player player : new ArrayList<>(this.players.values())) {
				player.close(player.getLeaveMessage(),
						(String) this.getConfig("settings.shutdown-message", "Server closed"));
			}

			this.getLogger().debug("Removing event handlers");
			HandlerList.unregisterAll();

			this.getLogger().debug("Stopping all tasks");
			this.scheduler.cancelAllTasks();
			this.scheduler.mainThreadHeartbeat(Integer.MAX_VALUE);

			this.getLogger().debug("Unloading all levels");
			for (final Level level : this.levelArray) {
				this.unloadLevel(level, true);
			}

			this.getLogger().debug("Closing console");
			this.consoleCommandReader.shutdown();

			this.getLogger().debug("Stopping network interfaces");
			for (final SourceInterface interfaz : this.network.getInterfaces()) {
				this.network.unregisterInterface(interfaz);
			}

			this.getLogger().debug("Disabling timings");
			Timings.stopServer();
			// todo other things
		} catch (final Exception e) {
			this.logger.logException(e); // todo remove this?
			this.logger.emergency("Exception happened while shutting down, exit the process");
			System.exit(1);
		}
	}

	public void start() {
		if (this.getPropertyBoolean("enable-query", true)) {
			this.queryHandler = new QueryHandler();
		}

		for (final BanEntry entry : this.getIPBans().getEntires().values()) {
			this.network.blockAddress(entry.getName(), -1);
		}

		// todo send usage setting
		this.tickCounter = 0;

		this.logger.info(this.getLanguage().translateString("nukkit.server.defaultGameMode",
				getGamemodeString(this.getGamemode())));

		this.logger.info(this.getLanguage().translateString("nukkit.server.startFinished",
				String.valueOf((double) (System.currentTimeMillis() - Nukkit.START_TIME) / 1000)));

		this.tickProcessor();
		this.forceShutdown();
	}

	public void handlePacket(final String address, final int port, final byte[] payload) {
		try {
			if (payload.length > 2
					&& Arrays.equals(Binary.subBytes(payload, 0, 2), new byte[] { (byte) 0xfe, (byte) 0xfd })
					&& this.queryHandler != null) {
				this.queryHandler.handle(address, port, payload);
			}
		} catch (final Exception e) {
			this.logger.logException(e);

			this.getNetwork().blockAddress(address, 600);
		}
	}

	private int lastLevelGC;

	public void tickProcessor() {
		this.nextTick = System.currentTimeMillis();
		try {
			while (this.isRunning) {
				try {
					this.tick();

					final long next = this.nextTick;
					final long current = System.currentTimeMillis();

					if (next - 0.1 > current) {
						long allocated = next - current - 1;

						{ // Instead of wasting time, do something potentially useful
							int offset = 0;
							for (int i = 0; i < levelArray.length; i++) {
								offset = (i + lastLevelGC) % levelArray.length;
								final Level level = levelArray[offset];
								level.doGarbageCollection(allocated - 1);
								allocated = next - System.currentTimeMillis();
								if (allocated <= 0) {
									break;
								}
							}
							lastLevelGC = offset + 1;
						}

						if (allocated > 0) {
							Thread.sleep(allocated, 900000);
						}
					}
				} catch (final RuntimeException e) {
					this.getLogger().logException(e);
				}
			}
		} catch (final Throwable e) {
			this.logger.emergency("Exception happened while ticking server");
			this.logger.alert(Utils.getExceptionMessage(e));
			this.logger.alert(Utils.getAllThreadDumps());
		}
	}

	public void onPlayerCompleteLoginSequence(final Player player) {
		this.sendFullPlayerListData(player);
	}

	public void onPlayerLogin(final Player player) {
		if (this.sendUsageTicker > 0) {
			this.uniquePlayers.add(player.getUniqueId());
		}
	}

	public void addPlayer(final String identifier, final Player player) {
		this.players.put(identifier, player);
		this.identifier.put(player.rawHashCode(), identifier);
	}

	public void addOnlinePlayer(final Player player) {
		this.playerList.put(player.getUniqueId(), player);
		this.updatePlayerListData(player.getUniqueId(), player.getId(), player.getDisplayName(), player.getSkin(),
				player.getLoginChainData().getXUID());
	}

	public void removeOnlinePlayer(final Player player) {
		if (this.playerList.containsKey(player.getUniqueId())) {
			this.playerList.remove(player.getUniqueId());

			final PlayerListPacket pk = new PlayerListPacket();
			pk.type = PlayerListPacket.TYPE_REMOVE;
			pk.entries = new PlayerListPacket.Entry[] { new PlayerListPacket.Entry(player.getUniqueId()) };

			Server.broadcastPacket(this.playerList.values(), pk);
		}
	}

	public void updatePlayerListData(final UUID uuid, final long entityId, final String name, final Skin skin) {
		this.updatePlayerListData(uuid, entityId, name, skin, "", this.playerList.values());
	}

	public void updatePlayerListData(final UUID uuid, final long entityId, final String name, final Skin skin, final String xboxUserId) {
		this.updatePlayerListData(uuid, entityId, name, skin, xboxUserId, this.playerList.values());
	}

	public void updatePlayerListData(final UUID uuid, final long entityId, final String name, final Skin skin, final Player[] players) {
		this.updatePlayerListData(uuid, entityId, name, skin, "", players);
	}

	public void updatePlayerListData(final UUID uuid, final long entityId, final String name, final Skin skin, final String xboxUserId,
			final Player[] players) {
		final PlayerListPacket pk = new PlayerListPacket();
		pk.type = PlayerListPacket.TYPE_ADD;
		pk.entries = new PlayerListPacket.Entry[] {
				new PlayerListPacket.Entry(uuid, entityId, name, skin, xboxUserId) };
		Server.broadcastPacket(players, pk);
	}

	public void updatePlayerListData(final UUID uuid, final long entityId, final String name, final Skin skin, final String xboxUserId,
			final Collection<Player> players) {
		this.updatePlayerListData(uuid, entityId, name, skin, xboxUserId,
				players.stream().filter(p -> !p.getUniqueId().equals(uuid)).toArray(Player[]::new));
	}

	public void removePlayerListData(final UUID uuid) {
		this.removePlayerListData(uuid, this.playerList.values());
	}

	public void removePlayerListData(final UUID uuid, final Player[] players) {
		final PlayerListPacket pk = new PlayerListPacket();
		pk.type = PlayerListPacket.TYPE_REMOVE;
		pk.entries = new PlayerListPacket.Entry[] { new PlayerListPacket.Entry(uuid) };
		Server.broadcastPacket(players, pk);
	}

	public void removePlayerListData(final UUID uuid, final Collection<Player> players) {
		this.removePlayerListData(uuid, players.stream().toArray(Player[]::new));
	}

	public void sendFullPlayerListData(final Player player) {
		final PlayerListPacket pk = new PlayerListPacket();
		pk.type = PlayerListPacket.TYPE_ADD;
		pk.entries = this.playerList.values().stream().map(p -> new PlayerListPacket.Entry(p.getUniqueId(), p.getId(),
				p.getDisplayName(), p.getSkin(), p.getLoginChainData().getXUID()))
				.toArray(PlayerListPacket.Entry[]::new);

		player.dataPacket(pk);
	}

	public void sendRecipeList(final Player player) {
		player.dataPacket(CraftingManager.packet);
	}

	private void checkTickUpdates(final int currentTick, final long tickTime) {
		for (final Player p : new ArrayList<>(this.players.values())) {
			/*
			 * if (!p.loggedIn && (tickTime - p.creationTime) >= 10000 &&
			 * p.kick(PlayerKickEvent.Reason.LOGIN_TIMEOUT, "Login timeout")) { continue; }
			 * 
			 * client freezes when applying resource packs todo: fix
			 */

			if (this.alwaysTickPlayers) {
				p.onUpdate(currentTick);
			}
		}

		// Do level ticks
		for (final Level level : this.levelArray) {
			if (level.getTickRate() > this.baseTickRate && --level.tickRateCounter > 0) {
				continue;
			}

			try {
				final long levelTime = System.currentTimeMillis();
				level.doTick(currentTick);
				final int tickMs = (int) (System.currentTimeMillis() - levelTime);
				level.tickRateTime = tickMs;

				if (this.autoTickRate) {
					if (tickMs < 50 && level.getTickRate() > this.baseTickRate) {
						int r;
						level.setTickRate(r = level.getTickRate() - 1);
						if (r > this.baseTickRate) {
							level.tickRateCounter = level.getTickRate();
						}
						this.getLogger().debug("Raising level \"" + level.getName() + "\" tick rate to "
								+ level.getTickRate() + " ticks");
					} else if (tickMs >= 50) {
						if (level.getTickRate() == this.baseTickRate) {
							level.setTickRate((int) Math.max(this.baseTickRate + 1,
									Math.min(this.autoTickRateLimit, Math.floor(tickMs / 50))));
							this.getLogger()
									.debug("Level \"" + level.getName() + "\" took " + NukkitMath.round(tickMs, 2)
											+ "ms, setting tick rate to " + level.getTickRate() + " ticks");
						} else if ((tickMs / level.getTickRate()) >= 50
								&& level.getTickRate() < this.autoTickRateLimit) {
							level.setTickRate(level.getTickRate() + 1);
							this.getLogger()
									.debug("Level \"" + level.getName() + "\" took " + NukkitMath.round(tickMs, 2)
											+ "ms, setting tick rate to " + level.getTickRate() + " ticks");
						}
						level.tickRateCounter = level.getTickRate();
					}
				}
			} catch (final Exception e) {
				if (Nukkit.DEBUG > 1 && this.logger != null) {
					this.logger.logException(e);
				}

				this.logger.critical(this.getLanguage().translateString("nukkit.level.tickError",
						new String[] { level.getName(), e.toString() }));
				this.logger.logException(e);
			}
		}
	}

	public void doAutoSave() {
		if (this.getAutoSave()) {
			Timings.levelSaveTimer.startTiming();
			for (final Player player : new ArrayList<>(this.players.values())) {
				if (player.isOnline()) {
					player.save(true);
				} else if (!player.isConnected()) {
					this.removePlayer(player);
				}
			}

			for (final Level level : this.levelArray) {
				level.save();
			}
			Timings.levelSaveTimer.stopTiming();
		}
	}

	private boolean tick() {
		final long tickTime = System.currentTimeMillis();

		// TODO
		final long sleepTime = tickTime - this.nextTick;
		if (sleepTime < -25) {
			try {
				Thread.sleep(Math.max(5, -sleepTime - 25));
			} catch (final InterruptedException e) {
				Server.getInstance().getLogger().logException(e);
			}
		}

		final long tickTimeNano = System.nanoTime();
		if ((tickTime - this.nextTick) < -25) {
			return false;
		}

		Timings.fullServerTickTimer.startTiming();

		++this.tickCounter;

		Timings.connectionTimer.startTiming();
		this.network.processInterfaces();

		if (this.rconServer != null) {
			RCON.check(rconServer, this);
		}
		Timings.connectionTimer.stopTiming();

		Timings.schedulerTimer.startTiming();
		this.scheduler.mainThreadHeartbeat(this.tickCounter);
		Timings.schedulerTimer.stopTiming();

		this.checkTickUpdates(this.tickCounter, tickTime);

		for (final Player player : new ArrayList<>(this.players.values())) {
			player.checkNetwork();
		}

		if ((this.tickCounter & 0b1111) == 0) {
			this.titleTick();
			this.network.resetStatistics();
			this.maxTick = 20;
			this.maxUse = 0;

			if ((this.tickCounter & 0b111111111) == 0) {
				try {
					this.getPluginManager().callEvent(this.queryRegenerateEvent = new QueryRegenerateEvent(this, 5));
					if (this.queryHandler != null) {
						this.queryHandler.regenerateInfo();
					}
				} catch (final Exception e) {
					this.logger.logException(e);
				}
			}

			this.getNetwork().updateName();
		}

		if (this.autoSave && ++this.autoSaveTicker >= this.autoSaveTicks) {
			this.autoSaveTicker = 0;
			this.doAutoSave();
		}

		if (this.sendUsageTicker > 0 && --this.sendUsageTicker == 0) {
			this.sendUsageTicker = 6000;
			// todo sendUsage
		}

		if (this.tickCounter % 100 == 0) {
			for (final Level level : this.levelArray) {
				level.doChunkGarbageCollection();
			}
		}

		Timings.fullServerTickTimer.stopTiming();
		// long now = System.currentTimeMillis();
		final long nowNano = System.nanoTime();
		// float tick = Math.min(20, 1000 / Math.max(1, now - tickTime));
		// float use = Math.min(1, (now - tickTime) / 50);

		final float tick = (float) Math.min(20, 1000000000 / Math.max(1000000, ((double) nowNano - tickTimeNano)));
		final float use = (float) Math.min(1, ((double) (nowNano - tickTimeNano)) / 50000000);

		if (this.maxTick > tick) {
			this.maxTick = tick;
		}

		if (this.maxUse < use) {
			this.maxUse = use;
		}

		System.arraycopy(this.tickAverage, 1, this.tickAverage, 0, this.tickAverage.length - 1);
		this.tickAverage[this.tickAverage.length - 1] = tick;

		System.arraycopy(this.useAverage, 1, this.useAverage, 0, this.useAverage.length - 1);
		this.useAverage[this.useAverage.length - 1] = use;

		if ((this.nextTick - tickTime) < -1000) {
			this.nextTick = tickTime;
		} else {
			this.nextTick += 50;
		}

		return true;
	}

	public long getNextTick() {
		return nextTick;
	}

	// TODO: Fix title tick
	public void titleTick() {
		if (!Nukkit.ANSI) {
			return;
		}

		final Runtime runtime = Runtime.getRuntime();
		final double used = NukkitMath.round((double) (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024, 2);
		final double max = NukkitMath.round(((double) runtime.maxMemory()) / 1024 / 1024, 2);
		final String usage = Math.round(used / max * 100) + "%";
		String title = (char) 0x1b + "]0;" + this.getName() + " " + this.getNukkitVersion() + " | Online "
				+ this.players.size() + "/" + this.getMaxPlayers() + " | Memory " + usage;
		if (!Nukkit.shortTitle) {
			title += " | U " + NukkitMath.round((this.network.getUpload() / 1024 * 1000), 2) + " D "
					+ NukkitMath.round((this.network.getDownload() / 1024 * 1000), 2) + " kB/s";
		}
		title += " | TPS " + this.getTicksPerSecond() + " | Load " + this.getTickUsage() + "%" + (char) 0x07;

		System.out.print(title);
	}

	public QueryRegenerateEvent getQueryInformation() {
		return this.queryRegenerateEvent;
	}

	public String getName() {
		return "Nukkit";
	}

	public boolean isRunning() {
		return isRunning;
	}

	public String getNukkitVersion() {
		return Nukkit.VERSION;
	}

	public String getCodename() {
		return Nukkit.CODENAME;
	}

	public String getVersion() {
		return ProtocolInfo.MINECRAFT_VERSION;
	}

	public String getApiVersion() {
		return Nukkit.API_VERSION;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public int getPort() {
		return this.getPropertyInt("server-port", 19132);
	}

	public int getViewDistance() {
		return this.getPropertyInt("view-distance", 10);
	}

	public String getIp() {
		return this.getPropertyString("server-ip", "0.0.0.0");
	}

	public UUID getServerUniqueId() {
		return this.serverID;
	}

	public boolean getAutoSave() {
		return this.autoSave;
	}

	public void setAutoSave(final boolean autoSave) {
		this.autoSave = autoSave;
		for (final Level level : this.levelArray) {
			level.setAutoSave(this.autoSave);
		}
	}

	public String getLevelType() {
		return this.getPropertyString("level-type", "DEFAULT");
	}

	public boolean getGenerateStructures() {
		return this.getPropertyBoolean("generate-structures", true);
	}

	public int getGamemode() {
		return this.getPropertyInt("gamemode", 0) & 0b11;
	}

	public boolean getForceGamemode() {
		return this.getPropertyBoolean("force-gamemode", false);
	}

	public static String getGamemodeString(final int mode) {
		return getGamemodeString(mode, false);
	}

	public static String getGamemodeString(final int mode, final boolean direct) {
		switch (mode) {
		case Player.SURVIVAL:
			return direct ? "Survival" : "%gameMode.survival";
		case Player.CREATIVE:
			return direct ? "Creative" : "%gameMode.creative";
		case Player.ADVENTURE:
			return direct ? "Adventure" : "%gameMode.adventure";
		case Player.SPECTATOR:
			return direct ? "Spectator" : "%gameMode.spectator";
		}
		return "UNKNOWN";
	}

	public static int getGamemodeFromString(final String str) {
		switch (str.trim().toLowerCase()) {
		case "0":
		case "survival":
		case "s":
			return Player.SURVIVAL;

		case "1":
		case "creative":
		case "c":
			return Player.CREATIVE;

		case "2":
		case "adventure":
		case "a":
			return Player.ADVENTURE;

		case "3":
		case "spectator":
		case "spc":
		case "view":
		case "v":
			return Player.SPECTATOR;
		}
		return -1;
	}

	public static int getDifficultyFromString(final String str) {
		switch (str.trim().toLowerCase()) {
		case "0":
		case "peaceful":
		case "p":
			return 0;

		case "1":
		case "easy":
		case "e":
			return 1;

		case "2":
		case "normal":
		case "n":
			return 2;

		case "3":
		case "hard":
		case "h":
			return 3;
		}
		return -1;
	}

	public int getDifficulty() {
		if (this.difficulty == Integer.MAX_VALUE) {
			this.difficulty = this.getPropertyInt("difficulty", 1);
		}
		return this.difficulty;
	}

	public boolean hasWhitelist() {
		return this.getPropertyBoolean("white-list", false);
	}

	public int getSpawnRadius() {
		return this.getPropertyInt("spawn-protection", 16);
	}

	public boolean getAllowFlight() {
		if (getAllowFlight == null) {
			getAllowFlight = this.getPropertyBoolean("allow-flight", false);
		}
		return getAllowFlight;
	}

	public boolean isHardcore() {
		return this.getPropertyBoolean("hardcore", false);
	}

	public int getDefaultGamemode() {
		if (this.defaultGamemode == Integer.MAX_VALUE) {
			this.defaultGamemode = this.getPropertyInt("gamemode", 0);
		}
		return this.defaultGamemode;
	}

	public String getMotd() {
		return this.getPropertyString("motd", "Nukkit Server For Minecraft: PE");
	}

	public String getSubMotd() {
		return this.getPropertyString("sub-motd", "Powered by Nukkit");
	}

	public boolean getForceResources() {
		return this.getPropertyBoolean("force-resources", false);
	}

	public MainLogger getLogger() {
		return this.logger;
	}

	public EntityMetadataStore getEntityMetadata() {
		return entityMetadata;
	}

	public PlayerMetadataStore getPlayerMetadata() {
		return playerMetadata;
	}

	public LevelMetadataStore getLevelMetadata() {
		return levelMetadata;
	}

	public PluginManager getPluginManager() {
		return this.pluginManager;
	}

	public CraftingManager getCraftingManager() {
		return craftingManager;
	}

	public ResourcePackManager getResourcePackManager() {
		return resourcePackManager;
	}

	public ServerScheduler getScheduler() {
		return scheduler;
	}

	public int getTick() {
		return tickCounter;
	}

	public float getTicksPerSecond() {
		return ((float) Math.round(this.maxTick * 100)) / 100;
	}

	public float getTicksPerSecondAverage() {
		float sum = 0;
		final int count = this.tickAverage.length;
		for (final float aTickAverage : this.tickAverage) {
			sum += aTickAverage;
		}
		return (float) NukkitMath.round(sum / count, 2);
	}

	public float getTickUsage() {
		return (float) NukkitMath.round(this.maxUse * 100, 2);
	}

	public float getTickUsageAverage() {
		float sum = 0;
		final int count = this.useAverage.length;
		for (final float aUseAverage : this.useAverage) {
			sum += aUseAverage;
		}
		return ((float) Math.round(sum / count * 100)) / 100;
	}

	public SimpleCommandMap getCommandMap() {
		return commandMap;
	}

	public Map<UUID, Player> getOnlinePlayers() {
		return new HashMap<>(playerList);
	}

	public void addRecipe(final Recipe recipe) {
		this.craftingManager.registerRecipe(recipe);
	}

	public IPlayer getOfflinePlayer(final String name) {
		final IPlayer result = this.getPlayerExact(name.toLowerCase());
		if (result == null) {
			return new OfflinePlayer(this, name, loadOfflinePlayerData(name));
		}

		return result;
	}

//	public CompoundTag getOfflinePlayerData(String name) {
		
	public CompoundTag loadOfflinePlayerData(String name) {
		name = name.toLowerCase();
		final File file = playersDb.file(name + ".dat");

		if (this.shouldSavePlayerData() && file.exists()) {
			try {
				return NBTIO.readCompressed(new FileInputStream(file));
			} catch (final Exception e) {
				file.renameTo(new File(file.getAbsolutePath() + ".bak"));
				this.logger.notice(this.getLanguage().translateString("nukkit.data.playerCorrupted", name));
				return null;
			}
		} else {
			this.logger.notice(this.getLanguage().translateString("nukkit.data.playerNotFound", name));
			return null;
		}
	}
	
	private CompoundTag createOfflinePlayerData(final String name) {

		final Position spawn = this.getDefaultLevel().getSafeSpawn();
		final CompoundTag nbt = new CompoundTag().putLong("firstPlayed", System.currentTimeMillis() / 1000)
				.putLong("lastPlayed", System.currentTimeMillis() / 1000)
				.putList(new ListTag<DoubleTag>("Pos").add(new DoubleTag("0", spawn.x)).add(new DoubleTag("1", spawn.y))
						.add(new DoubleTag("2", spawn.z)))
				.putString("Level", this.getDefaultLevel().getName()).putList(new ListTag<>("Inventory"))
				.putCompound("Achievements", new CompoundTag()).putInt("playerGameType", this.getGamemode())
				.putList(new ListTag<DoubleTag>("Motion").add(new DoubleTag("0", 0)).add(new DoubleTag("1", 0))
						.add(new DoubleTag("2", 0)))
				.putList(new ListTag<FloatTag>("Rotation").add(new FloatTag("0", 0)).add(new FloatTag("1", 0)))
				.putFloat("FallDistance", 0).putShort("Fire", 0).putShort("Air", 300).putBoolean("OnGround", true)
				.putBoolean("Invulnerable", false).putString("NameTag", name);

		this.saveOfflinePlayerData(name, nbt);
		return nbt;
	}

	public CompoundTag loadOrCreateOfflinePlayerData(final String name) {
		final CompoundTag data = loadOfflinePlayerData(name);
		if (data == null) {
			return createOfflinePlayerData(name);
		} else {
			return data;
		}
	}
	
	public void saveOfflinePlayerData(final String name, final CompoundTag tag) {
		this.saveOfflinePlayerData(name, tag, false);
	}

	public void saveOfflinePlayerData(final String name, final CompoundTag tag, final boolean async) {
		if (this.shouldSavePlayerData()) {
			try {
				final File playerDat = playersDb.file(name.toLowerCase() + ".dat");
				if (async) {
					this.getScheduler().scheduleAsyncTask(
							new FileWriteTask(playerDat, NBTIO.writeGZIPCompressed(tag, ByteOrder.BIG_ENDIAN)));
				} else {
					Utils.writeFile(playerDat,
							new ByteArrayInputStream(NBTIO.writeGZIPCompressed(tag, ByteOrder.BIG_ENDIAN)));
				}
			} catch (final Exception e) {
				this.logger.critical(this.getLanguage().translateString("nukkit.data.saveError",
						new String[] { name, e.getMessage() }));
				if (Nukkit.DEBUG > 1) {
					this.logger.logException(e);
				}
			}
		}
	}

	public Player getPlayer(String name) {
		Player found = null;
		name = name.toLowerCase();
		int delta = Integer.MAX_VALUE;
		for (final Player player : this.getOnlinePlayers().values()) {
			if (player.getName().toLowerCase().startsWith(name)) {
				final int curDelta = player.getName().length() - name.length();
				if (curDelta < delta) {
					found = player;
					delta = curDelta;
				}
				if (curDelta == 0) {
					break;
				}
			}
		}

		return found;
	}

	public Player getPlayerExact(String name) {
		name = name.toLowerCase();
		for (final Player player : this.getOnlinePlayers().values()) {
			if (player.getName().toLowerCase().equals(name)) {
				return player;
			}
		}

		return null;
	}

	public Player[] matchPlayer(String partialName) {
		partialName = partialName.toLowerCase();
		final List<Player> matchedPlayer = new ArrayList<>();
		for (final Player player : this.getOnlinePlayers().values()) {
			if (player.getName().toLowerCase().equals(partialName)) {
				return new Player[] { player };
			} else if (player.getName().toLowerCase().contains(partialName)) {
				matchedPlayer.add(player);
			}
		}

		return matchedPlayer.toArray(new Player[matchedPlayer.size()]);
	}

	public void removePlayer(final Player player) {
		if (this.identifier.containsKey(player.rawHashCode())) {
			final String identifier = this.identifier.get(player.rawHashCode());
			this.players.remove(identifier);
			this.identifier.remove(player.rawHashCode());
			return;
		}

		for (final String identifier : new ArrayList<>(this.players.keySet())) {
			final Player p = this.players.get(identifier);
			if (player == p) {
				this.players.remove(identifier);
				this.identifier.remove(player.rawHashCode());
				break;
			}
		}
	}

	public Map<Integer, Level> getLevels() {
		return levels;
	}

	public Level getDefaultLevel() {
		return defaultLevel;
	}

	public void setDefaultLevel(final Level defaultLevel) {
		if (defaultLevel == null
				|| (this.isLevelLoaded(defaultLevel.getFolderName()) && defaultLevel != this.defaultLevel)) {
			this.defaultLevel = defaultLevel;
		}
	}

	public boolean isLevelLoaded(final String name) {
		return this.getLevelByName(name) != null;
	}

	public Level getLevel(final int levelId) {
		if (this.levels.containsKey(levelId)) {
			return this.levels.get(levelId);
		}
		return null;
	}

	public Level getLevelByName(final String name) {
		for (final Level level : this.levelArray) {
			if (level.getFolderName().equals(name)) {
				return level;
			}
		}

		return null;
	}

	public boolean unloadLevel(final Level level) {
		return this.unloadLevel(level, false);
	}

	public boolean unloadLevel(final Level level, final boolean forceUnload) {
		if (level == this.getDefaultLevel() && !forceUnload) {
			throw new IllegalStateException(
					"The default level cannot be unloaded while running, please switch levels.");
		}

		return level.unload(forceUnload);

	}

	public boolean loadLevel(final String name) throws IOException {
		if (Objects.equals(name.trim(), "")) {
			throw new LevelException("Invalid empty level name");
		}
		if (this.isLevelLoaded(name)) {
			return true;
		} else if (!this.isLevelGenerated(name)) {
			this.logger.notice(this.getLanguage().translateString("nukkit.level.notFound", name));

			return false;
		}

		try {
			final DataStore levelStore = worldsDb.subStore(name);
			final String path = levelStore.basepath().toAbsolutePath().toString();

			final Class<? extends LevelProvider> provider = LevelProviderManager.getProvider(path);

			if (provider == null) {
				this.logger.error(this.getLanguage().translateString("nukkit.level.loadError",
						new String[] { name, "Unknown provider" }));

				return false;
			}

			final Level level = new Level(this, name, path, provider);
			this.levels.put(level.getId(), level);

			level.initLevel();

			this.getPluginManager().callEvent(new LevelLoadEvent(level));

			level.setTickRate(this.baseTickRate);

			return true;
		} catch (final Exception e) {
			this.logger.error(this.getLanguage().translateString("nukkit.level.loadError",
					new String[] { name, e.getMessage() }));
			this.logger.logException(e);
			return false;
		}

	}

	public boolean generateLevel(final String name) throws IOException {
		return this.generateLevel(name, new java.util.Random().nextLong());
	}

	public boolean generateLevel(final String name, final long seed) throws IOException {
		return this.generateLevel(name, seed, null);
	}

	public boolean generateLevel(final String name, final long seed, final Class<? extends Generator> generator) throws IOException {
		return this.generateLevel(name, seed, generator, new HashMap<>());
	}

	public boolean generateLevel(final String name, final long seed, final Class<? extends Generator> generator,
			final Map<String, Object> options) throws IOException {
		return generateLevel(name, seed, generator, options, null);
	}

	public boolean generateLevel(final String name, final long seed, Class<? extends Generator> generator,
			final Map<String, Object> options, Class<? extends LevelProvider> provider) throws IOException {
		if (Objects.equals(name.trim(), "") || this.isLevelGenerated(name)) {
			return false;
		}

		if (!options.containsKey("preset")) {
			options.put("preset", this.getPropertyString("generator-settings", ""));
		}

		if (generator == null) {
			generator = Generator.getGenerator(this.getLevelType());
		}

		if (provider == null) {
			if ((provider = LevelProviderManager
					.getProviderByName((String) this.getConfig("level-settings.default-format", "anvil"))) == null) {
				provider = LevelProviderManager.getProviderByName("anvil");
			}
		}

		try {
			final DataStore levelStore = worldsDb.subStore(name);
			final String path = levelStore.basepath().toAbsolutePath().toString();
			provider.getMethod("generate", String.class, String.class, long.class, Class.class, Map.class).invoke(null,
					path, name, seed, generator, options);

			final Level level = new Level(this, name, path, provider);
			this.levels.put(level.getId(), level);

			level.initLevel();
			level.setTickRate(this.baseTickRate);
			this.getPluginManager().callEvent(new LevelInitEvent(level));
			
			this.getPluginManager().callEvent(new LevelLoadEvent(level));
		} catch (final Exception e) {
			this.logger.error(this.getLanguage().translateString("nukkit.level.generationError",
					new String[] { name, e.getMessage() }));
			this.logger.logException(e);
			return false;
		}


		/*
		 * this.getLogger().notice(this.getLanguage().translateString(
		 * "nukkit.level.backgroundGeneration", name));
		 * 
		 * int centerX = (int) level.getSpawnLocation().getX() >> 4; int centerZ = (int)
		 * level.getSpawnLocation().getZ() >> 4;
		 * 
		 * TreeMap<String, Integer> order = new TreeMap<>();
		 * 
		 * for (int X = -3; X <= 3; ++X) { for (int Z = -3; Z <= 3; ++Z) { int distance
		 * = X * X + Z * Z; int chunkX = X + centerX; int chunkZ = Z + centerZ;
		 * order.put(Level.chunkHash(chunkX, chunkZ), distance); } }
		 * 
		 * List<Map.Entry<String, Integer>> sortList = new
		 * ArrayList<>(order.entrySet());
		 * 
		 * Collections.sort(sortList, new Comparator<Map.Entry<String, Integer>>() {
		 * 
		 * @Override public int compare(Map.Entry<String, Integer> o1, Map.Entry<String,
		 * Integer> o2) { return o2.getValue() - o1.getValue(); } });
		 * 
		 * for (String index : order.keySet()) { Chunk.Entry entry =
		 * Level.getChunkXZ(index); level.populateChunk(entry.chunkX, entry.chunkZ,
		 * true); }
		 */
		return true;
	}

	public boolean isLevelGenerated(final String name) throws IOException {
		if (Objects.equals(name.trim(), "")) {
			return false;
		}

		if (this.getLevelByName(name) == null) {

			final DataStore levelStore = worldsDb.subStore(name);
			if (LevelProviderManager.getProvider(levelStore.basepath().toAbsolutePath().toString()) == null) {
				return false;
			}
		}

		return true;
	}

	public BaseLang getLanguage() {
		return baseLang;
	}

	public boolean isLanguageForced() {
		return forceLanguage;
	}

	public Network getNetwork() {
		return network;
	}

	// Revising later...
	public Config getConfig() {
		return this.config;
	}

	public Object getConfig(final String variable) {
		return this.getConfig(variable, null);
	}

	public Object getConfig(final String variable, final Object defaultValue) {
		final Object value = this.config.get(variable);
		return value == null ? defaultValue : value;
	}

	public Config getProperties() {
		return this.properties;
	}

	public Object getProperty(final String variable) {
		return this.getProperty(variable, null);
	}

	public Object getProperty(final String variable, final Object defaultValue) {
		return this.properties.exists(variable) ? this.properties.get(variable) : defaultValue;
	}

	public void setPropertyString(final String variable, final String value) {
		this.properties.set(variable, value);
		this.properties.save();
	}

	public String getPropertyString(final String variable) {
		return this.getPropertyString(variable, null);
	}

	public String getPropertyString(final String variable, final String defaultValue) {
		return this.properties.exists(variable) ? (String) this.properties.get(variable) : defaultValue;
	}

	public int getPropertyInt(final String variable) {
		return this.getPropertyInt(variable, null);
	}

	public int getPropertyInt(final String variable, final Integer defaultValue) {
		return this.properties.exists(variable) ? (!this.properties.get(variable).equals("")
				? Integer.parseInt(String.valueOf(this.properties.get(variable)))
				: defaultValue) : defaultValue;
	}

	public void setPropertyInt(final String variable, final int value) {
		this.properties.set(variable, value);
		this.properties.save();
	}

	public boolean getPropertyBoolean(final String variable) {
		return this.getPropertyBoolean(variable, null);
	}

	public boolean getPropertyBoolean(final String variable, final Object defaultValue) {
		final Object value = this.properties.exists(variable) ? this.properties.get(variable) : defaultValue;
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		switch (String.valueOf(value)) {
		case "on":
		case "true":
		case "1":
		case "yes":
			return true;
		}
		return false;
	}

	public void setPropertyBoolean(final String variable, final boolean value) {
		this.properties.set(variable, value ? "1" : "0");
		this.properties.save();
	}

	public PluginIdentifiableCommand getPluginCommand(final String name) {
		final Command command = this.commandMap.getCommand(name);
		if (command instanceof PluginIdentifiableCommand) {
			return (PluginIdentifiableCommand) command;
		} else {
			return null;
		}
	}

	public BanList getNameBans() {
		return this.banByName;
	}

	public BanList getIPBans() {
		return this.banByIP;
	}

	public void addOp(final String name) {
		this.operators.set(name.toLowerCase(), true);
		final Player player = this.getPlayerExact(name);
		if (player != null) {
			player.recalculatePermissions();
		}
		this.operators.save(true);
	}

	public void removeOp(final String name) {
		this.operators.remove(name.toLowerCase());
		final Player player = this.getPlayerExact(name);
		if (player != null) {
			player.recalculatePermissions();
		}
		this.operators.save();
	}

	public void addWhitelist(final String name) {
		this.whitelist.set(name.toLowerCase(), true);
		this.whitelist.save(true);
	}

	public void removeWhitelist(final String name) {
		this.whitelist.remove(name.toLowerCase());
		this.whitelist.save(true);
	}

	public boolean isWhitelisted(final String name) {
		return !this.hasWhitelist() || this.operators.exists(name, true) || this.whitelist.exists(name, true);
	}

	public boolean isOp(final String name) {
		return this.operators.exists(name, true);
	}

	public Config getWhitelist() {
		return whitelist;
	}

	public Config getOps() {
		return operators;
	}

	public void reloadWhitelist() {
		this.whitelist.reload();
	}

	public ServiceManager getServiceManager() {
		return serviceManager;
	}

	public Map<String, List<String>> getCommandAliases() {
		final Object section = this.getConfig("aliases");
		final Map<String, List<String>> result = new LinkedHashMap<>();
		if (section instanceof Map) {
			for (final Map.Entry entry : (Set<Map.Entry>) ((Map) section).entrySet()) {
				final List<String> commands = new ArrayList<>();
				final String key = (String) entry.getKey();
				final Object value = entry.getValue();
				if (value instanceof List) {
					for (final String string : (List<String>) value) {
						commands.add(string);
					}
				} else {
					commands.add((String) value);
				}

				result.put(key, commands);
			}
		}

		return result;

	}

	public boolean shouldSavePlayerData() {
		return (Boolean) this.getConfig("player.save-player-data", true);
	}

	/**
	 * Checks the current thread against the expected primary thread for the server.
	 * <p>
	 * <b>Note:</b> this method should not be used to indicate the current
	 * synchronized state of the runtime. A current thread matching the main thread
	 * indicates that it is synchronized, but a mismatch does not preclude the same
	 * assumption.
	 *
	 * @return true if the current thread matches the expected primary thread, false
	 *         otherwise
	 */
	public final boolean isPrimaryThread() {
		return (Thread.currentThread() == currentThread);
	}

	public Thread getPrimaryThread() {
		return currentThread;
	}

	private void registerEntities() {
		Entity.registerEntity("Arrow", EntityArrow.class);
		Entity.registerEntity("Item", EntityItem.class);
		Entity.registerEntity("FallingSand", EntityFallingBlock.class);
		Entity.registerEntity("PrimedTnt", EntityPrimedTNT.class);
		Entity.registerEntity("Snowball", EntitySnowball.class);
		Entity.registerEntity("EnderPearl", EntityEnderPearl.class);
		Entity.registerEntity("Painting", EntityPainting.class);
		// Monsters
		Entity.registerEntity("Creeper", EntityCreeper.class);
		Entity.registerEntity("Blaze", EntityBlaze.class);
		Entity.registerEntity("CaveSpider", EntityCaveSpider.class);
		Entity.registerEntity("ElderGuardian", EntityElderGuardian.class);
		Entity.registerEntity("EnderDragon", EntityEnderDragon.class);
		Entity.registerEntity("Enderman", EntityEnderman.class);
		Entity.registerEntity("Endermite", EntityEndermite.class);
		Entity.registerEntity("Evoker", EntityEvoker.class);
		Entity.registerEntity("Firework", EntityFirework.class);
		Entity.registerEntity("Ghast", EntityGhast.class);
		Entity.registerEntity("Guardian", EntityGuardian.class);
		Entity.registerEntity("Husk", EntityHusk.class);
		Entity.registerEntity("MagmaCube", EntityMagmaCube.class);
		Entity.registerEntity("Shulker", EntityShulker.class);
		Entity.registerEntity("Silverfish", EntitySilverfish.class);
		Entity.registerEntity("Skeleton", EntitySkeleton.class);
		Entity.registerEntity("SkeletonHorse", EntitySkeletonHorse.class);
		Entity.registerEntity("Slime", EntitySlime.class);
		Entity.registerEntity("Spider", EntitySpider.class);
		Entity.registerEntity("Stray", EntityStray.class);
		Entity.registerEntity("Vindicator", EntityVindicator.class);
		Entity.registerEntity("Vex", EntityVex.class);
		Entity.registerEntity("WitherSkeleton", EntityWitherSkeleton.class);
		Entity.registerEntity("Wither", EntityWither.class);
		Entity.registerEntity("Witch", EntityWitch.class);
		Entity.registerEntity("ZombiePigman", EntityZombiePigman.class);
		Entity.registerEntity("ZombieVillager", EntityZombieVillager.class);
		Entity.registerEntity("Zombie", EntityZombie.class);

		// Passive
		Entity.registerEntity("Bat", EntityBat.class);
		Entity.registerEntity("Chicken", EntityChicken.class);
		Entity.registerEntity("Cow", EntityCow.class);
		Entity.registerEntity("Donkey", EntityDonkey.class);
		Entity.registerEntity("Horse", EntityHorse.class);
		Entity.registerEntity("Llama", EntityLlama.class);
		Entity.registerEntity("Mooshroom", EntityMooshroom.class);
		Entity.registerEntity("Mule", EntityMule.class);
		Entity.registerEntity("PolarBear", EntityPolarBear.class);
		Entity.registerEntity("Pig", EntityPig.class);
		Entity.registerEntity("Rabbit", EntityRabbit.class);
		Entity.registerEntity("Sheep", EntitySheep.class);
		Entity.registerEntity("Squid", EntitySquid.class);
		Entity.registerEntity("Wolf", EntityWolf.class);
		Entity.registerEntity("Ocelot", EntityOcelot.class);
		Entity.registerEntity("Villager", EntityVillager.class);

		Entity.registerEntity("ThrownExpBottle", EntityExpBottle.class);
		Entity.registerEntity("XpOrb", EntityXPOrb.class);
		Entity.registerEntity("ThrownPotion", EntityPotion.class);
		Entity.registerEntity("Egg", EntityEgg.class);

		Entity.registerEntity("Human", EntityHuman.class, true);

		Entity.registerEntity("MinecartRideable", EntityMinecartEmpty.class);
		// TODO: 2016/1/30 all finds of minecart
		Entity.registerEntity("Boat", EntityBoat.class);

		// Entity.registerEntity("Lightning", EntityLightning.class); lightning
		// shouldn't be saved as entity
	}

	private void registerBlockEntities() {
		BlockEntity.registerBlockEntity(BlockEntity.FURNACE, BlockEntityFurnace.class);
		BlockEntity.registerBlockEntity(BlockEntity.CHEST, BlockEntityChest.class);
		BlockEntity.registerBlockEntity(BlockEntity.SIGN, BlockEntitySign.class);
		BlockEntity.registerBlockEntity(BlockEntity.ENCHANT_TABLE, BlockEntityEnchantTable.class);
		BlockEntity.registerBlockEntity(BlockEntity.SKULL, BlockEntitySkull.class);
		BlockEntity.registerBlockEntity(BlockEntity.FLOWER_POT, BlockEntityFlowerPot.class);
		BlockEntity.registerBlockEntity(BlockEntity.BREWING_STAND, BlockEntityBrewingStand.class);
		BlockEntity.registerBlockEntity(BlockEntity.ITEM_FRAME, BlockEntityItemFrame.class);
		BlockEntity.registerBlockEntity(BlockEntity.CAULDRON, BlockEntityCauldron.class);
		BlockEntity.registerBlockEntity(BlockEntity.ENDER_CHEST, BlockEntityEnderChest.class);
		BlockEntity.registerBlockEntity(BlockEntity.BEACON, BlockEntityBeacon.class);
		BlockEntity.registerBlockEntity(BlockEntity.PISTON_ARM, BlockEntityPistonArm.class);
		BlockEntity.registerBlockEntity(BlockEntity.COMPARATOR, BlockEntityComparator.class);
		BlockEntity.registerBlockEntity(BlockEntity.HOPPER, BlockEntityHopper.class);
		BlockEntity.registerBlockEntity(BlockEntity.BED, BlockEntityBed.class);
		BlockEntity.registerBlockEntity(BlockEntity.JUKEBOX, BlockEntityJukebox.class);
	}

	public static Server getInstance() {
		return instance;
	}

}
