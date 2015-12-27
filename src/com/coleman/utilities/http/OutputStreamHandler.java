package com.coleman.utilities.http;

import java.io.IOException;
import java.io.OutputStream;
/**
 * Handler for writing to outputstreams
 */
public abstract class OutputStreamHandler {
	/**
	 * This method will be called after opening an output stream to
	 * the connection. This method should be overridden to write
	 * your data to it as needed
	 * @param stream the stream for you to write to
	 */
	public abstract void writeTo(OutputStream stream) throws IOException;
}
