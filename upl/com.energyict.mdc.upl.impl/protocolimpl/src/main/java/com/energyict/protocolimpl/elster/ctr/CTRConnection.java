package com.energyict.protocolimpl.elster.ctr;

import com.energyict.protocolimpl.elster.ctr.packets.CTRPacket;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 11:47:09
 */
public class CTRConnection {

    private final InputStream inputStream;
    private final OutputStream outputStream;
    private Logger logger;

    private int forcedDelay;
    private int timeout;
    private int retries;

    private String password;
    private String encryptionKey;

    public CTRConnection(InputStream inputStream, OutputStream outputStream, ProtocolProperties properties, Logger logger) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.logger = logger;

        this.forcedDelay = properties.getForcedDelay();
        this.timeout = properties.getTimeout();
        this.retries = properties.getRetries();
        this.password = properties.getPassword();
        this.encryptionKey = properties.getEncryptionKey();

    }

    public void writeRawData(byte[] data) throws IOException {
        doForcedDelay();
        this.outputStream.write(data);
    }

    private void doForcedDelay() {
        if (forcedDelay > 0) {
            ProtocolTools.delay(forcedDelay);
        }
    }

    public byte[] readRawData() throws IOException {
        waitForData();
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        while (inputStream.available() > 0) {
            byte[] buffer = new byte[4096];
            int len = inputStream.read(buffer);
            if (len == -1) {
                throw new IOException("Unable to read.");
            }
            response.write(buffer, 0, len);
            ProtocolTools.delay(1);
        }
        return response.toByteArray();
    }

    private void waitForData() throws IOException {
        long timeOutTime = System.currentTimeMillis() + timeout;
        while (inputStream.available() <= 0) {
            if (timeOutTime <= System.currentTimeMillis()) {
                throw new IOException("timeOut while waiting for data");
            }
            ProtocolTools.delay(1);
        }
    }

    public CTRPacket sendRequestGetResonse(CTRPacket packet) throws IOException {
        int attempts = 0;
        while (attempts++ < retries) {
/*
            try {
                writeRawData(packet.getBytes());
                byte[] rawData = readRawData();

            } catch (IOException e) {

            }
*/
        }
        return null;
    }

    ;

}
