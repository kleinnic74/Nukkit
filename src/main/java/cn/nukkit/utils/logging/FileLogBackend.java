package cn.nukkit.utils.logging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import cn.nukkit.utils.TextFormat;

public class FileLogBackend implements LoggingBackend {

	private final Path target;
	private final SimpleDateFormat tsFormat = new SimpleDateFormat("Y-M-d HH:mm:ss ");

	public FileLogBackend(Path out, Path oldLogs) throws IOException {
		Objects.requireNonNull(out);
        if (!Files.exists(out)) {
        	this.target = Files.createFile(out);
        } else {
            FileTime date = Files.getLastModifiedTime(out);
            String newName = new SimpleDateFormat("Y-M-d HH.mm.ss").format(new Date(date.toMillis())) + ".log";
            if (!Files.exists(oldLogs)) {
            	Files.createDirectories(oldLogs);
            }
            Files.move(out, oldLogs.resolve(newName));
            if (!Files.exists(out)) {
            	this.target = Files.createFile(out);
            } else {
            	this.target = out;
            }
        }
	}

	@Override
	public void log(Date timestamp, String message) {
		String fileDateFormat = tsFormat.format(timestamp);
		try (BufferedWriter writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
			writer.write(fileDateFormat);
			writer.write(TextFormat.clean(message));
			writer.write("\r\n");
		} catch (IOException e) {
			System.err.println("Failed to append to log file: "+e.getMessage());
		}
	}

}
