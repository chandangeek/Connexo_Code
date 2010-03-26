package com.energyict.protocolimpl.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.communicationdump.CommunicationDumpEntry;
import com.energyict.protocolimpl.utils.communicationdump.CommunicationDumpFile;

/**
 * @author jme
 *
 */
public class VirtualDevice {

	private ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
	private CommunicationDumpFile dumpFile = new CommunicationDumpFile("c:\\dump_edited.log");
	private int seqPointer = -1;

	private InputStream inputStream = new InputStream() {

		@Override
		public int read() throws IOException {return bais.read();}
		public int read(byte[] b) throws IOException {return bais.read(b);};
		public int read(byte[] b, int off, int len) throws IOException {return bais.read(b, off, len);};
		public int available() throws IOException {return bais.available();};

	};

	private OutputStream outputStream = new OutputStream() {

		@Override
		public void write(int b) throws IOException {
			throw new UnsupportedException();
		}

		public void write(byte[] b) throws IOException {
			throw new UnsupportedException();
		};

		public void write(byte[] b, int off, int len) throws IOException {
			//System.out.println(ProtocolTools.getHexStringFromBytes(b));
			writeToProtocol(getResponseTo(b));
		}

	};

	public InputStream getInputStream() {
		return inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	private void writeToProtocol(byte[] bytes) {
		//System.out.println("TX: " + ProtocolTools.getHexStringFromBytes(bytes));
		bais = new ByteArrayInputStream(bytes);
	}

	private byte[] getResponseTo(byte[] b) {
		List<CommunicationDumpEntry> tx = dumpFile.getTxEntries();
		for (CommunicationDumpEntry txEntry : tx) {
			if (seqPointer < txEntry.getSequenceNumber()) {
				if (Arrays.equals(txEntry.getData(), b)) {
					seqPointer = txEntry.getSequenceNumber();
					List<CommunicationDumpEntry> rx = dumpFile.getRxEntries();
					for (CommunicationDumpEntry rxEntry : rx) {
						if (seqPointer < rxEntry.getSequenceNumber()) {
							seqPointer = rxEntry.getSequenceNumber();
							return rxEntry.getData();
						}
					}
				}
			}
		}
		return new byte[0];
	};

	public static void main(String[] args) {
		String dateFormat = "dd/MM/yy HH:mm:ss.SSS";

		String[] content = new String(ProtocolTools.readBytesFromFile("c:\\dump.log")).split("\n");
		for (int i = 0; i < content.length; i++) {
			content[i] = content[i].replace(" <= ", " RX ");
			content[i] = content[i].replace(" => ", " TX ");
			content[i] = content[i].replace("RX[", "");
			content[i] = content[i].replace("TX[", "");
			content[i] = content[i].replace("]", "");

			String timeAsString = content[i].substring(0, 13);
			DateFormat sdf = new SimpleDateFormat(dateFormat);
			String dateFormatted = sdf.format(new Date(Long.valueOf(timeAsString).longValue()));

			content[i] = content[i].replace(timeAsString, dateFormatted);

		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (String string : content) {
			try {
				baos.write(string.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		//ProtocolTools.writeBytesToFile("c:\\dump_edited.log", baos.toByteArray(), false);

		for (String string : content) {
			System.out.print(string);
		}

	}

}
