package com.energyict.protocolimpl.debug;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.energyict.protocol.ProtocolCollection;
import com.energyict.protocolimpl.base.ProtocolCollectionImpl;

/**
 * @author jme
 *
 */
public class ProtocolList {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) {
		ProtocolCollection collection = new ProtocolCollectionImpl();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < collection.getSize(); i++) {
			sb.append(i).append(" = ");

			try {
				sb.append(collection.getProtocolClassName(i)).append("; ");
			} catch (IOException e) {
				sb.append("???").append("; ");
			}

			try {
				sb.append(collection.getProtocolName(i)).append("; ");
			} catch (IOException e) {
				sb.append("???").append("; ");
			}

			try {
				sb.append(collection.getProtocolRevision(i)).append("; ");
			} catch (IOException e) {
				sb.append("???").append("; ");
			}

			sb.append("\r\n");
		}

		System.out.println(sb.toString());
		if ((args != null) && (args.length != 0) && (args[0] != null) && (args[0].length() != 0)) {
			writeToFile(args[0], sb);
		}

	}

	private static void writeToFile(String fileName, StringBuilder sb) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fileName);
			fos.write(sb.toString().getBytes());
		} catch (FileNotFoundException e) {
			System.out.println("Unable to open or create file [" + fileName + "]: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("Unable to write to file [" + fileName + "]: " + e.getMessage());
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					System.out.println("Unable to close file [" + fileName + "]: " + e.getMessage());
				}
			}
		}

	}

}
