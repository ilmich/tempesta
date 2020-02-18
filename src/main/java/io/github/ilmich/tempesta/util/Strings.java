package io.github.ilmich.tempesta.util;

public class Strings {
	
	public static boolean isNullOrEmpty(String arg) {
		return (arg == null) || (arg.equals(""));
	}

	public static String nullToEmpty(String arg) {
		return isNullOrEmpty(arg) ? "" : arg;
	}
	
	public static String emptyToNull(String arg) {
		return isNullOrEmpty(arg) ? null : arg;
	}
	
}
