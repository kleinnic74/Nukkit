package cn.nukkit.command;

import java.io.IOException;
import java.util.Objects;

import cn.nukkit.Server;
import cn.nukkit.utils.completers.CommandsCompleter;
import cn.nukkit.utils.completers.PlayersCompleter;
import jline.console.ConsoleReader;
import jline.console.CursorBuffer;
import jline.console.completer.Completer;

public class Console {

	private ConsoleReader reader;

	private CursorBuffer stashed;

	public Console() throws IOException {
		this.reader = new ConsoleReader();
		reader.setPrompt("> ");
		// instance = this;
	}

	public void close()
	{
		this.reader.shutdown();
	}
	
	public void addCompleter(Completer completer) {
		reader.addCompleter(Objects.requireNonNull(completer));
	}

	public String readLine() throws IOException {
		return reader.readLine();
	}

	public synchronized void stashLine() throws IOException {
		this.stashed = reader.getCursorBuffer().copy();
		reader.getOutput().write("\u001b[1G\u001b[K");
		reader.flush();
	}

	public synchronized void unstashLine() throws IOException {
		reader.resetPromptLine("> ", this.stashed.toString(), this.stashed.cursor);
	}

	public void removePromptLine() throws IOException {
		reader.resetPromptLine("", "", 0);
	}

}
