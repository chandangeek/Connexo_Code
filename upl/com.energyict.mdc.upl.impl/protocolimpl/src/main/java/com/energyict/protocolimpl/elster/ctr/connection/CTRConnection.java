package com.energyict.protocolimpl.elster.ctr.connection;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.*;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 11:47:09
 */
public class CTRConnection implements ProtocolConnection {

    private HHUSignOn hhuSignOn = null;
    private final OutputStream outputStream;
    private final InputStream inputStream;

    private int forcedDelay;
    private int timeout;
    private int retries;

    public CTRConnection(InputStream inputStream, OutputStream outputStream, int forcedDelay, int timeout, int retries) throws ConnectionException {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.forcedDelay = forcedDelay;
        this.timeout = timeout;
        this.retries = retries;
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn) {
        this.hhuSignOn = hhuSignOn;
    }

    public HHUSignOn getHhuSignOn() {
        return hhuSignOn;
    }

    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {
        throw new ProtocolConnectionException("Not implemented yet.");
    }

    public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws IOException, ProtocolConnectionException {
        return null;
    }

    public byte[] dataReadout(String strID, String nodeId) throws NestedIOException, ProtocolConnectionException {
        throw new ProtocolConnectionException("Not implemented yet.");
    }

    public void writeRawData(byte[] data) throws IOException {
        if (this.outputStream != null) {
            doForcedDelay();
            this.outputStream.write(data);
        }
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

}
