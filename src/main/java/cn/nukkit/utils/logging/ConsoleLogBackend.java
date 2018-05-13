package cn.nukkit.utils.logging;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import cn.nukkit.Nukkit;
import cn.nukkit.command.Console;
import cn.nukkit.utils.TextFormat;

public class ConsoleLogBackend implements LoggingBackend {

	private static Map<TextFormat, String> replacements = new EnumMap<TextFormat, String>(TextFormat.class) {
		{
			this.put(TextFormat.BLACK, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLACK).boldOff().toString());
			this.put(TextFormat.DARK_BLUE,
					Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLUE).boldOff().toString());
			this.put(TextFormat.DARK_GREEN,
					Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).boldOff().toString());
			this.put(TextFormat.DARK_AQUA,
					Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).boldOff().toString());
			this.put(TextFormat.DARK_RED, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).boldOff().toString());
			this.put(TextFormat.DARK_PURPLE,
					Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.MAGENTA).boldOff().toString());
			this.put(TextFormat.GOLD, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).boldOff().toString());
			this.put(TextFormat.GRAY, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).boldOff().toString());
			this.put(TextFormat.DARK_GRAY, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLACK).bold().toString());
			this.put(TextFormat.BLUE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLUE).bold().toString());
			this.put(TextFormat.GREEN, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).bold().toString());
			this.put(TextFormat.AQUA, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).bold().toString());
			this.put(TextFormat.RED, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).bold().toString());
			this.put(TextFormat.LIGHT_PURPLE,
					Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.MAGENTA).bold().toString());
			this.put(TextFormat.YELLOW, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString());
			this.put(TextFormat.WHITE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).bold().toString());
			this.put(TextFormat.BOLD, Ansi.ansi().a(Ansi.Attribute.UNDERLINE_DOUBLE).toString());
			this.put(TextFormat.STRIKETHROUGH, Ansi.ansi().a(Ansi.Attribute.STRIKETHROUGH_ON).toString());
			this.put(TextFormat.UNDERLINE, Ansi.ansi().a(Ansi.Attribute.UNDERLINE).toString());
			this.put(TextFormat.ITALIC, Ansi.ansi().a(Ansi.Attribute.ITALIC).toString());
			this.put(TextFormat.RESET, Ansi.ansi().a(Ansi.Attribute.RESET).toString());
		}
	};

	private static final TextFormat[] colors = TextFormat.values();

	private final Console console;

	private final SimpleDateFormat tsFormat = new SimpleDateFormat("HH:mm:ss ");

	public ConsoleLogBackend(Console console) {
		this.console = Objects.requireNonNull(console);
		AnsiConsole.systemInstall();
	}

	@Override
	public void log(Date timestamp, String message) {
		try {
			String consoleDateFormat = tsFormat.format(timestamp);
			console.stashLine();
			System.out.println(
					colorize(TextFormat.AQUA + consoleDateFormat + TextFormat.RESET + message + TextFormat.RESET));
			console.unstashLine();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	private String colorize(String string) {
		if (string.indexOf(TextFormat.ESCAPE) < 0) {
			return string;
		} else if (Nukkit.ANSI) {
			for (TextFormat color : colors) {
				if (replacements.containsKey(color)) {
					string = string.replaceAll("(?i)" + color, replacements.get(color));
				} else {
					string = string.replaceAll("(?i)" + color, "");
				}
			}
		} else {
			return TextFormat.clean(string);
		}
		return string + Ansi.ansi().reset();
	}
}
