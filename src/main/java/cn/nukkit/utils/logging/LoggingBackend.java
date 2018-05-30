package cn.nukkit.utils.logging;

import java.util.Date;

public interface LoggingBackend {
	void log(Date timestamp, String message);
}
