/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.edmi.mk10.streamfilters;

import com.energyict.protocols.util.OutputStreamDecorator;

import com.energyict.protocolimpl.edmi.mk10.parsers.MK10OutputStreamParser;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MK10PushOutputStream extends OutputStreamDecorator {

	private static final int DEBUG 			= 0;
	private ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
    private final Logger logger;

    public MK10PushOutputStream(OutputStream stream, Logger logger) {
		super(stream);
        this.logger = logger;
	}

	public void write(byte[] b, int off, int len) throws IOException {
		bufferOut.write(b, off, len);
		updateBuffer();
	}

	public void write(byte[] b) throws IOException {
		bufferOut.write(b);
		updateBuffer();
	}

	public void write(int b) throws IOException {
		System.out.println("Wrote single byte: " + b);
        bufferOut.write(b);
		updateBuffer();
	}

	private void updateBuffer() throws IOException {
		byte[] packet = null;
        MK10OutputStreamParser outputParser = new MK10OutputStreamParser();
		bufferOut.flush();
		outputParser.parse(bufferOut.toByteArray());

		if (outputParser.isValidPacket()) {
            packet = outputParser.getValidPacket();
            getStream().write(packet);
			 getStream().flush();
		}

        logTX(bufferOut.toByteArray(), packet);

        if (outputParser.isValidPacket()) {
            bufferOut.reset();
        }

    }

    private void logTX(byte[] rawPacket, byte[] pushPacket) {
        if (this.logger != null) {
            String raw = rawPacket != null ? ProtocolTools.getHexStringFromBytes(rawPacket) : "null";
            String push = pushPacket != null ? ProtocolTools.getHexStringFromBytes(pushPacket) : "null";
            String currentMillis = "[" + System.currentTimeMillis() + "]  ";
            this.logger.log(Level.INFO, currentMillis + "TX RAW = " + raw + ", PUSH = " + push);
        }
    }

}
