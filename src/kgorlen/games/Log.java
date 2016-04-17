package kgorlen.games;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Log {
	static class LogFormatter extends Formatter {

	    @Override
	    public String format(LogRecord record) {
	        return record.getMessage();
	    }
	}	

	public static final Logger LOGGER;
	public static final ConsoleHandler CONSOLE_HANDLER;
	public static final FileHandler FILE_HANDLER;
	static {
		String packageName = TreeSearch.class.getPackage().getName();
		LOGGER = Logger.getLogger(packageName);
		CONSOLE_HANDLER = new ConsoleHandler();
		CONSOLE_HANDLER.setLevel(Level.ALL);
		CONSOLE_HANDLER.setFormatter(new LogFormatter());
		try {
			FILE_HANDLER = new FileHandler("%h/" + packageName + "%u.log");
		} catch(IOException e){
			throw new ExceptionInInitializerError(e);
		}
		FILE_HANDLER.setLevel(Level.ALL);
		FILE_HANDLER.setFormatter(new LogFormatter());
		LOGGER.addHandler(CONSOLE_HANDLER);
		LOGGER.addHandler(FILE_HANDLER);
		LOGGER.setLevel(Level.SEVERE);
		LOGGER.setUseParentHandlers(false);
	}

}
