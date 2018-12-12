package com.elster.protocolimpl.dsfg.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
* @version  1.0 (5/12/2010)
* @author   Gunter Heuckeroth
*
* <P>
* <B>Description :</B><BR>
* utility class to parse a dsfg data block
* 
* <B>Changes :</B><BR>
*/ 
public class DsfgBlockInputStream extends ByteArrayInputStream {

	/**
	 * constructor of dsfg block parser utility
	 * 
	 * @param data - the data bytes of a dsfg data block (including block separators)
	 *  
	 */
	public DsfgBlockInputStream(byte[] data) {
		super(data);
	}

	/**
	 * private function to check if read char is a defined delimiter
	 * 
	 * @param ch
	 *            is the char to check
	 * @param delim
	 *            is an array of allowed delimiter
	 * @return true if ch is in delim, otherwise false
	 */
	private boolean isDelim(char ch, byte[] delim) {
		for (int i = 0; i < delim.length; i++) {
			if (delim[i] == ch) {
				return true;
			}
		}
		return false;
	}

	/**
	 * reads a char from stream
	 * 
	 * @param delim
	 *            - array of allowed delimiter
	 * @return the read char
	 * @throws IOException
	 *             if stream is empty or the byte after the char is not a valid
	 *             delimiter
	 */
	public char readChar(byte[] delim) throws IOException {

		char result = (char) this.read();
		char ch = (char) this.read();

		if ((result < 0) || (ch < 0)) {
			throw new IOException("Input past end of data");
		}

		if (!isDelim(ch, delim))
			throw new IOException("Data block delimiter error");

		return result;
	}

	/**
	 * reads a string from stream
	 * 
	 * @param delim
	 *            - array of allowed delimiter
	 * @return read String
	 * @throws IOException
	 *             if stream is empty
	 */
	public String readString(byte[] delim) throws IOException {
		String result = "";

		char ch;

		do {
			ch = (char) this.read();
			if (ch < 0) {
				throw new IOException("Input past end of data");
			}
			if (isDelim(ch, delim))
				break;
			result = result + ch;
		} while (true);

		return result;
	}

	/**
	 * reads an int from stream
	 * 
	 * @param delim
	 *            - array of allowed delimiter
	 * @return read int
	 * @throws IOException
	 *             if stream is empty
	 */
	public int readInt(byte[] delim) throws IOException {
		String s = readString(delim);
		return Integer.parseInt(s);
	}
}
