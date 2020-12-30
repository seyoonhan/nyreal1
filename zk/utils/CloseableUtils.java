package com.han.startup.zk.utils;

import com.google.common.io.Closeables;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by shan2 on 7/7/2017.
 */
@Slf4j
public class CloseableUtils {
	public static void closeQuietly(Closeable closeable) {
		try {
			Closeables.close(closeable, true);
		} catch (IOException e) {
			log.error("IOException should not have been thrown.", e);
		}
	}
}
