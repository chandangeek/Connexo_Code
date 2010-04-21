package com.energyict.protocolimpl.utils;

import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.utils.communicationdump.CommunicationDumpEntry;
import com.energyict.protocolimpl.utils.communicationdump.CommunicationDumpFile;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jme
 *
 */
public class VirtualDevice {

	private ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(new byte[0]);
	private final CommunicationDumpFile dumpFile;
	private int seqPointer = -1;
    private boolean showCommunication = false;

    public VirtualDevice(CommunicationDumpFile dumpFile) {
        this.dumpFile = dumpFile;
    }

    public void reset() {
        seqPointer = -1;
    }

    public boolean isShowCommunication() {
        return showCommunication;
    }

    public void setShowCommunication(boolean showCommunication) {
        this.showCommunication = showCommunication;
    }

    private InputStream inputStream = new InputStream() {
		@Override
		public int read() throws IOException {return byteArrayInputStream.read();}
		public int read(byte[] b) throws IOException {return byteArrayInputStream.read(b);};
		public int read(byte[] b, int off, int len) throws IOException {return byteArrayInputStream.read(b, off, len);};
		public int available() throws IOException {return byteArrayInputStream.available();};
	};

	private OutputStream outputStream = new OutputStream() {
		@Override
		public void write(int b) throws IOException {throw new UnsupportedException();}
		public void write(byte[] b) throws IOException {throw new UnsupportedException();};
		public void write(byte[] b, int off, int len) throws IOException {writeToProtocol(getResponseTo(b));}

	};

	public InputStream getInputStream() {
		return inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	private void writeToProtocol(byte[] bytes) {
        if (isShowCommunication()) {
            System.out.println(CommunicationDumpEntry.createRxEntry(bytes));
        }
        byteArrayInputStream = new ByteArrayInputStream(bytes);
	}

    private byte[] getResponseTo(byte[] data) {
        if (isShowCommunication()) {
            System.out.println(CommunicationDumpEntry.createTxEntry(data));
        }
        List<CommunicationDumpEntry> tx = dumpFile.getTxEntries();
        for (CommunicationDumpEntry txEntry : tx) {
            if (seqPointer < txEntry.getSequenceNumber()) {
                if (Arrays.equals(txEntry.getData(), data)) {
                    seqPointer = txEntry.getSequenceNumber();
                    List<CommunicationDumpEntry> rx = dumpFile.getRxEntries();
                    for (CommunicationDumpEntry rxEntry : rx) {
                        if (seqPointer < rxEntry.getSequenceNumber()) {
                            seqPointer = rxEntry.getSequenceNumber();
                            return rxEntry.getData().clone();
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

		for (String string : content) {
			System.out.print(string);
		}

	}

}
