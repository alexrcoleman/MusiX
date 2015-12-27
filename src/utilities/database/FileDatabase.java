package utilities.database;

import java.io.*;

public class FileDatabase<K, V> extends Database<K, V> {
	private static final long serialVersionUID = 8933809499256120159L;

	private File file;

	private transient OutputStream output;
	private transient InputStream input;

	public FileDatabase(File file) {
		this(file, false, false);
		readFromFile();
	}

	public FileDatabase(File file, boolean writeLock, boolean readLock) {
		this.file = file;
		// boolean madeDirs = file.mkdirs();
		// System.out.println("Made dirs for \"" + file + "\": " +
		// madeDirs);
		// System.out.println(file.getAbsolutePath());
		boolean writeDefault = false;// file.createNewFile();
		System.out.println(writeDefault);
		if (readLock)
			this.input = getInput();
		if (writeLock)
			this.output = getOutput();
		if (writeDefault)
			writeToFile();
	}

	public void readFromFile() {
		try {
			boolean lockFile = input != null;
			input = getInput();
			super.readFrom(input);
			if (!lockFile) {
				input.close();
				input = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeToFile() {
		try {
			boolean lockFile = output != null;
			output = getOutput();
			super.writeTo(output);
			if (!lockFile) {
				output.close();
				output = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error saving database!");
		}
	}

	private InputStream getInput() {
		if (input != null)
			return input;
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private OutputStream getOutput() {
		if (output != null)
			return output;
		try {
			return new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
