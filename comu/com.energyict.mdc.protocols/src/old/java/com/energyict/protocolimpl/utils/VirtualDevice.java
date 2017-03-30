/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.utils;

import com.energyict.mdc.protocol.api.UnsupportedException;

import com.energyict.protocolimpl.utils.communicationdump.CommunicationDumpEntry;
import com.energyict.protocolimpl.utils.communicationdump.CommunicationDumpFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

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

}
