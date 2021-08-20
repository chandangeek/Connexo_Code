package com.energyict.protocolimplv2.umi.connection;

import com.energyict.dlms.DLMSUtils;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.protocolimplv2.umi.properties.CommunicationSessionProperties;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UmiTcpIpConnection implements UmiConnection {
    private final ComChannel comChannel;
    private long forcedDelay;
    private long timeout;
    private static final Logger LOGGER = Logger.getLogger(UmiTcpIpConnection.class.getName());

    public UmiTcpIpConnection(ComChannel comChannel, CommunicationSessionProperties properties) {
        this.comChannel = comChannel;
        this.forcedDelay = properties.getForcedDelay();
        setTimeout(properties.getTimeout());
    }

    @Override
    public void send(byte[] data) throws IOException {
        comChannel.startWriting();
        comChannel.write(data);
        //TODO: replace with debug level
        StringBuffer toLog = new StringBuffer("Sent to device: ");
        for (int i = 0; i < data.length; i++) {
            toLog.append(String.format("0x%02X", data[i])).append(" ");
        }
        LOGGER.log(Level.FINEST, toLog.toString());
    }

    @Override
    public byte[] receive() throws IOException {
        comChannel.startReading();
        DLMSUtils.delay(1000);
        byte[] response = new byte[comChannel.available()];
        comChannel.read(response);
        //TODO: replace with debug level
        StringBuffer toLog = new StringBuffer("Received from device: ");
        for (int i = 0; i < response.length; i++) {
            toLog.append(String.format("0x%02X", response[i])).append(" ");
        }
        LOGGER.log(Level.FINEST, toLog.toString());
        return response;
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
        comChannel.setTimeout(timeout);
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public ComChannel getComChannel() {
        return comChannel;
    }
}
