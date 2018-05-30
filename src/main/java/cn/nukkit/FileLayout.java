package cn.nukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileLayout {

	public static interface DataStore {
		public File basedir();

		public Path basepath();

		public File file(String name);

		public Path filePath(String name);

		public DataStore subStore(String... paths) throws IOException;
	}

	private static class SubdirDataStore implements DataStore {
		private Path root;

		public SubdirDataStore(Path root) {
			this.root = root;
		}

		public File basedir() {
			return root.toFile();
		}

		public Path basepath() {
			return root;
		}

		@Override
		public File file(String name) {
			return root.resolve(name).toFile();
		}

		@Override
		public Path filePath(String name) {
			return root.resolve(name);
		}

		@Override
		public DataStore subStore(String... paths) throws IOException {
			Path result = root;
			for (String p : paths) {
				result = result.resolve(p);
			}
			if (!Files.exists(result)) {
				Files.createDirectories(result);
			}
			return new SubdirDataStore(result);
		}
	}

	public static FileLayout build(String basedir) throws IOException {
		Path root = Paths.get(basedir);
		if (!Files.exists(root)) {
			throw new IOException(String.format("Cannot use %s as basedir", basedir));
		}
		Path data = ensureDirectory(root, "data");
		Path plugins = ensureDirectory(root, "plugins");
		return new FileLayout(root, data, plugins);
	}

	private static Path ensureDirectory(Path root, String name) throws IOException {
		Path dir = root.resolve(name);
		if (!Files.exists(dir)) {
			dir = Files.createDirectories(dir);
		}
		if (!Files.isDirectory(dir)) {
			throw new IOException(String.format("Not a directory %s", dir));
		}
		return dir;
	}

	private final SubdirDataStore data;
	private final SubdirDataStore plugins;
	private final SubdirDataStore config;

	private FileLayout(Path configDir, Path datadir, Path pluginDir) {
		this.config = new SubdirDataStore(configDir);
		this.data = new SubdirDataStore(datadir);
		this.plugins = new SubdirDataStore(pluginDir);
	}

	public DataStore config() {
		return config;
	}

	public DataStore data() {
		return data;
	}

	public DataStore plugins() {
		return plugins;
	}
}
