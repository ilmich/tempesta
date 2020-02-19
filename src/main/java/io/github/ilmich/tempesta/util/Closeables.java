package io.github.ilmich.tempesta.util;

import java.io.Closeable;
import java.io.IOException;

public class Closeables {

	public static void closeQuietly(Closeable channel) {
		try {
			if (channel != null)
				channel.close();
		} catch (IOException e) {

		}
	}

}
