package cn.nukkit.utils;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class SoftwareVersion {
	public static SoftwareVersion get() {
		Properties values = new Properties();
		try(InputStream in = SoftwareVersion.class.getResourceAsStream("/git.properties")) {
			if (in != null) {
			  values.load(in);
			}
		} catch(IOException e) {
			System.err.println("Could not load resource 'git.properties': "+e.getMessage());
		}
		return new SoftwareVersion(values);
	}

	private final Properties properties;

	private SoftwareVersion(Properties properties) {
		this.properties = properties;
	}

	public String getCommitId() {
		return properties.getProperty("git.commit.id");
	}

	public String getGitUrl() {
		return properties.getProperty("git.remote.origin.url");
	}

	public String getBranch() {
		return properties.getProperty("git.branch");
	}
}

