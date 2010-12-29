package com.energyict.protocolimpl.debug;

import com.energyict.genericprotocolimpl.common.GenericProtocolCollectionImpl;
import com.energyict.protocol.ProtocolCollection;
import com.energyict.protocolimpl.base.ProtocolCollectionImpl;

import java.io.*;

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

        StringBuilder sb = new StringBuilder();
        sb.append("Standard protocols: \r\n").append(getProtocolInfo(new ProtocolCollectionImpl()));
        sb.append("Generic protocols: \r\n").append(getProtocolInfo(new GenericProtocolCollectionImpl()));

		System.out.println(sb.toString());
		if ((args != null) && (args.length != 0) && (args[0] != null) && (args[0].length() != 0)) {
			writeToFile(args[0], sb);
		}

	}

    private static String getProtocolInfo(ProtocolCollection collection) {
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

        sb.append("\r\n");
        
        return sb.toString();
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
