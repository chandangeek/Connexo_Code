package com.elster.jupiter.nls;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.logging.Level;

import com.google.common.collect.ImmutableMap;

public final class NlsMessageFormat {
	private static Map<Level,String> levelIndicators =
			ImmutableMap.of(Level.INFO,"I",Level.CONFIG,"C",Level.WARNING,"W",Level.SEVERE,"S");
	private final int number;
	private final Level level;
	private final NlsString messageFormat;
	
	NlsMessageFormat(int number, NlsString messageFormat, Level level) {
		this.number = number;
		this.messageFormat = messageFormat;
		this.level = level;
	}

	public String format(Object... args) {
		return 
			messageFormat.getComponent() +
			new DecimalFormat("0000").format(number) +
			levelIndicators.get(level) +
			" " +
			MessageFormat.format(messageFormat.toString(), args);
	}
	
}
