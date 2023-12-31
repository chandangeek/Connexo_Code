/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.util.time.StopWatch;
import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.channel.serial.ServerSerialPort;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.events.io.ReadEvent;
import com.energyict.mdc.engine.impl.events.io.WriteEvent;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.upl.properties.TypedProperties;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an implementation for the {@link ComPortRelatedComChannel} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-24 (13:44)
 */
public class ComPortRelatedComChannelImpl implements ComPortRelatedComChannel {

    private static final long NANOS_IN_MILLI = 1000000L;

    private final Clock clock;
    private final HexService hexService;
    private final EventPublisher eventPublisher;
    private final StopWatch talking;
    private final Counters sessionCounters = new Counters();
    private final Counters taskSessionCounters = new Counters();
    private final DeviceMessageService deviceMessageService;
    private final ComChannel comChannel;
    private ComPort comPort;
    private ByteArrayOutputStream bytesReadForLogging;
    private ByteArrayOutputStream bytesWrittenForLogging;
    private final Logger logger = Logger.getLogger("deviceMessageTracing");
    private boolean traced;
    private String deviceName;
    private String comTaskName;

    public ComPortRelatedComChannelImpl(ComChannel comChannel, ComPort comPort, Clock clock, DeviceMessageService deviceMessageService, HexService hexService, EventPublisher eventPublisher) {
        super();
        this.comChannel = comChannel;
        this.clock = clock;
        this.deviceMessageService = deviceMessageService;
        this.hexService = hexService;
        this.eventPublisher = eventPublisher;
        this.talking = new StopWatch(false);  // No cpu required;
        this.talking.stop();
        this.comPort = comPort;
    }

    @Override
    public ComPort getComPort() {
        return comPort;
    }

    @Override
    public void setComPort(ComPort comPort) {
        this.comPort = comPort;
    }

    @Override
    public void updatePortConfiguration(SerialPortConfiguration serialPortConfiguration) {
        if (getComChannelType() == ComChannelType.SerialComChannel || getComChannelType() == ComChannelType.OpticalComChannel) {
            ((SerialPortComChannel) getActualComChannel()).updatePortConfiguration(serialPortConfiguration);
        }
    }

    @Override
    public SerialPortConfiguration getSerialPortConfiguration() {
        if (getComChannelType() == ComChannelType.SerialComChannel || getComChannelType() == ComChannelType.OpticalComChannel) {
            return ((SerialPortComChannel) getActualComChannel()).getSerialPortConfiguration();
        }
        return null;
    }

    @Override
    public ServerSerialPort getSerialPort() {
        if (getComChannelType() == ComChannelType.SerialComChannel || getComChannelType() == ComChannelType.OpticalComChannel) {
            return ((SerialPortComChannel) getActualComChannel()).getSerialPort();
        }
        return null;
    }

    @Override
    public ComChannel getActualComChannel() {
        return comChannel;
    }

    private LogLevel getServerLogLevel() {
        return this.getServerLogLevel(this.comPort);
    }

    private LogLevel getServerLogLevel(ComPort comPort) {
        return this.getServerLogLevel(comPort.getComServer());
    }

    private LogLevel getServerLogLevel(ComServer comServer) {
        return LogLevelMapper.forComServerLogLevel().toLogLevel(comServer.getServerLogLevel());
    }

    @Override
    public boolean startReading() {
        boolean changed = this.comChannel.startReading();
        if (changed) {
            this.logBytesWrittenIfAny();
            this.bytesReadForLogging = null;
        }
        return changed;
    }

    @Override
    public int read() {
        this.talking.start();
        int byteRead = comChannel.read();
        if (byteRead != -1) {
            this.ensureBytesReadForLogging();
            this.bytesReadForLogging.write(byteRead);
            this.afterReading(1);
            return byteRead;
        } else {
            return this.afterReading(byteRead);
        }
    }

    @Override
    public int read(byte[] buffer) {
        this.talking.start();
        int numberOfBytesRead = comChannel.read(buffer);
        if (numberOfBytesRead != -1) {
            this.ensureBytesReadForLogging();
            this.safeWriteTo(buffer, this.bytesReadForLogging);
        }
        return this.afterReading(numberOfBytesRead);
    }

    @Override
    public int read(byte[] buffer, int offset, int length) {
        this.talking.start();
        int numberOfBytesRead = comChannel.read(buffer, offset, length);
        if (numberOfBytesRead != -1) {
            this.ensureBytesReadForLogging();
            this.safeWriteTo(buffer, offset, length, this.bytesReadForLogging);
        }
        return this.afterReading(numberOfBytesRead);
    }

    private void ensureBytesReadForLogging() {
        if (this.bytesReadForLogging == null) {
            this.bytesReadForLogging = new ByteArrayOutputStream();
        }
    }

    private int afterReading(int bytesRead) {
        this.talking.stop();
        if (bytesRead != -1) {
            Counters sessionCounters = this.sessionCounters;
            sessionCounters.bytesRead(bytesRead);
            if (!sessionCounters.isReading()) {
                sessionCounters.reading();
                sessionCounters.packetRead();
            }
            Counters taskSessionCounters = this.taskSessionCounters;
            taskSessionCounters.bytesRead(bytesRead);
            if (!taskSessionCounters.isReading()) {
                taskSessionCounters.reading();
                taskSessionCounters.packetRead();
            }
        }
        return bytesRead;
    }

    private void safeWriteTo(byte[] bytes, ByteArrayOutputStream os) {
        try {
            os.write(bytes);
        } catch (IOException e) {
            // Should not occur since the ByteArrayOutputStream is in memory
            e.printStackTrace(System.err);
        }
    }

    private void safeWriteTo(byte[] bytes, int offset, int length, ByteArrayOutputStream os) {
        os.write(bytes, offset, length);
    }

    @Override
    public int available() {
        return comChannel.available();
    }

    @Override
    public boolean startWriting() {
        boolean changed = this.comChannel.startWriting();
        if (changed) {
            this.logBytesReadIfAny();
            this.bytesWrittenForLogging = null;
        }
        return changed;
    }

    @Override
    public int write(int b) {
        talking.start();
        int numberOfBytesWritten = comChannel.write(b);
        if (numberOfBytesWritten != -1) {
            this.ensureBytesWrittenForLogging();
            this.bytesWrittenForLogging.write(b);
            return this.afterWriting(1);
        } else {
            return this.afterWriting(numberOfBytesWritten);
        }
    }

    @Override
    public int write(byte[] bytes) {
        talking.start();
        int numberOfBytesWritten = comChannel.write(bytes);
        if (numberOfBytesWritten != -1) {
            this.ensureBytesWrittenForLogging();
            this.safeWriteTo(bytes, this.bytesWrittenForLogging);
        }
        return this.afterWriting(numberOfBytesWritten);
    }

    private void ensureBytesWrittenForLogging() {
        if (this.bytesWrittenForLogging == null) {
            this.bytesWrittenForLogging = new ByteArrayOutputStream();
        }
    }

    private int afterWriting(int numberOfBytesWritten) {
        talking.stop();
        if (numberOfBytesWritten != -1) {
            Counters sessionCounters = this.sessionCounters;
            sessionCounters.bytesSent(numberOfBytesWritten);
            if (!sessionCounters.isWriting()) {
                sessionCounters.writing();
                sessionCounters.packetSent();
            }
            Counters taskSessionCounters = this.taskSessionCounters;
            taskSessionCounters.bytesSent(numberOfBytesWritten);
            if (!taskSessionCounters.isWriting()) {
                taskSessionCounters.writing();
                taskSessionCounters.packetSent();
            }
        }
        return numberOfBytesWritten;
    }

    @Override
    public void close() {
        if (this.comChannel != null) {
            this.comChannel.close();
        }
        this.talking.stop();
        this.logRemainingBytes();
    }

    @Override
    public void logRemainingBytes() {
        this.logBytesReadIfAny();
        this.logBytesWrittenIfAny();
    }

    @Override
    public void addProperties(TypedProperties typedProperties) {
        comChannel.addProperties(typedProperties);
    }

    @Override
    public TypedProperties getProperties() {
        return comChannel.getProperties();
    }

    @Override
    public void prepareForDisConnect() {
        comChannel.prepareForDisConnect();
    }

    @Override
    public void setTimeout(long millis) {
        comChannel.setTimeout(millis);
    }

    @Override
    public boolean isVoid() {
        return comChannel.isVoid();
    }

    @Override
    public ComChannelType getComChannelType() {
        return comChannel.getComChannelType();
    }

    @Override
    public void flush() throws IOException {
        comChannel.flush();
    }

    private void logBytesWrittenIfAny() {
        if (this.bytesWrittenForLogging != null) {
            this.logBytesWrittenAndReset();
            this.bytesWrittenForLogging = null;
        }
    }

    private void logBytesWrittenAndReset() {
        byte[] bytesWrittenForLogging = this.bytesWrittenForLogging.toByteArray();
        if (this.logger != null && traced) {
            String hexBytes = this.hexService.toHexString(bytesWrittenForLogging);
            this.logger.log(Level.INFO, "TX " + hexBytes, new Object[]{deviceName, comTaskName});
        }
        this.publish(new WriteEvent(new ComServerEventServiceProvider(), this.comPort, bytesWrittenForLogging));
    }

    private void logBytesReadIfAny() {
        if (this.bytesReadForLogging != null) {
            this.logBytesReadAndReset();
            this.bytesReadForLogging = null;
        }
    }

    private void logBytesReadAndReset() {
        byte[] bytesReadForLogging = this.bytesReadForLogging.toByteArray();
        if (this.logger != null && traced) {
            String hexBytes = this.hexService.toHexString(bytesReadForLogging);
            this.logger.log(Level.INFO, "RX " + hexBytes, new Object[]{deviceName, comTaskName});
        }
        this.publish(new ReadEvent(new ComServerEventServiceProvider(), this.comPort, bytesReadForLogging));
    }

    @Override
    public Duration talkTime() {
        return Duration.ofMillis(this.talking.getElapsed() / NANOS_IN_MILLI);
    }

    @Override
    public Counters getSessionCounters() {
        return this.sessionCounters;
    }

    @Override
    public Counters getTaskSessionCounters() {
        return this.taskSessionCounters;
    }

    private void publish(ComServerEvent event) {
        this.eventPublisher.publish(event);
    }

    private class ComServerEventServiceProvider implements AbstractComServerEventImpl.ServiceProvider {
        @Override
        public Clock clock() {
            return clock;
        }

        @Override
        public DeviceMessageService deviceMessageService() {
            return deviceMessageService;
        }
    }

    @Override
    public void setTraced(boolean traced) {
        this.traced = traced;
    }

    @Override
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public void setComTaskName(String comTaskName) {
        this.comTaskName = comTaskName;
    }
}
