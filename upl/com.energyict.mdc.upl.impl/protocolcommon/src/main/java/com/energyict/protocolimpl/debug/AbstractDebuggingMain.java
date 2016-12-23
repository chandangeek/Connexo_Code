package com.energyict.protocolimpl.debug;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.Optical;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocolimpl.base.DebuggingObserver;
import com.energyict.protocolimpl.properties.TypedProperties;

import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 1-jun-2010
 * Time: 8:07:53
 */
public abstract class AbstractDebuggingMain<P extends MeterProtocol> {

    private static final Level LOG_LEVEL = Level.ALL;

    private Logger logger = null;
    private Dialer dialer = null;

    private TimeZone timeZone = TimeZone.getTimeZone("GMT+01");

    private String phoneNumber = null;
    private String commPort = null;
    private String observerFilename = null;
    private String modemInit = "ATM0";

    private int baudRate = 9600;
    private int dataBits = SerialCommunicationChannel.DATABITS_8;
    private int parity = SerialCommunicationChannel.PARITY_NONE;
    private int stopBits = SerialCommunicationChannel.STOPBITS_1;
    private boolean showCommunication = false;
    private boolean asciiMode = false;
    private boolean mode7E1 = false;

    abstract void doDebug() throws LinkException, IOException;

    abstract P getMeterProtocol();

    abstract Properties getProperties();

    protected void set7E1Mode(boolean mode7E1) {
        this.mode7E1 = mode7E1;
    }

    public Dialer getDialer() throws LinkException, IOException {
        if (dialer == null) {
            DebuggingObserver debuggingObserver = new DebuggingObserver(observerFilename, showCommunication, asciiMode, mode7E1);
            if (phoneNumber != null) {
                dialer = DebugUtils.getConnectedModemDialer(phoneNumber, commPort, modemInit, debuggingObserver);
            } else {
                dialer = DebugUtils.getConnectedDirectDialer(commPort, baudRate, dataBits, parity, stopBits, debuggingObserver);
            }
        }
        return dialer;
    }

    private RegisterProtocol getRegisterProtocol() {
        if (getMeterProtocol() instanceof RegisterProtocol) {
            return (RegisterProtocol) getMeterProtocol();
        } else {
            return null;
        }
    }

    public void initAndConnectMeterProtocol() throws LinkException, IOException, ParseException {
        getMeterProtocol().setProperties(TypedProperties.copyOf(getProperties()));
        getMeterProtocol().init(getDialer().getInputStream(), getDialer().getOutputStream(), getTimeZone(), getLogger());
        if (getDialer() instanceof Optical) {
            if (getMeterProtocol() instanceof HHUEnabler) {
                ((HHUEnabler) getMeterProtocol()).enableHHUSignOn(getDialer().getSerialCommunicationChannel());
            }
        }
        getMeterProtocol().connect();
    }

    public void disconnectDialer() {
        log("Closing connections. \n");
        if (dialer != null) {
            try {
                if (getDialer().getStreamConnection().isOpen()) {
                    getDialer().disConnect();
                }
            } catch (Exception e) {
                // Absorb
            } finally {
                dialer = null;
            }
        }
    }

    public void run() {
        try {
            initAndConnectMeterProtocol();
            doDebug();
            getMeterProtocol().disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            log("Error: " + e.getMessage() + ". \n");
        } finally {
            disconnectDialer();
        }
    }

    public void readRegister(String obisCodeAsString) {
        readRegister(ObisCode.fromString(obisCodeAsString));
    }

    public void readRegister(ObisCode obisCode) {
        try {
            log(getRegisterProtocol().readRegister(obisCode));
        } catch (IOException e) {
            log(obisCode.toString() + ", " + e.getMessage());
        }
    }

    public void log(Object objectToLog) {
        getLogger().log(Level.INFO, objectToLog == null ? "null" : objectToLog.toString());
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getCanonicalName());
            logger.setLevel(LOG_LEVEL);
        }
        return logger;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCommPort() {
        return commPort;
    }

    public void setCommPort(String commPort) {
        this.commPort = commPort;
    }

    public String getObserverFilename() {
        return observerFilename;
    }

    public void setObserverFilename(String observerFilename) {
        this.observerFilename = observerFilename;
    }

    public String getModemInit() {
        return modemInit;
    }

    public void setModemInit(String modemInit) {
        this.modemInit = modemInit;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public int getDataBits() {
        return dataBits;
    }

    public void setDataBits(int dataBits) {
        this.dataBits = dataBits;
    }

    public int getParity() {
        return parity;
    }

    public void setParity(int parity) {
        this.parity = parity;
    }

    public int getStopBits() {
        return stopBits;
    }

    public void setStopBits(int stopBits) {
        this.stopBits = stopBits;
    }

    public boolean isShowCommunication() {
        return showCommunication;
    }

    public void setShowCommunication(boolean showCommunication) {
        this.showCommunication = showCommunication;
    }

    public boolean isAsciiMode() {
        return asciiMode;
    }

    public void setAsciiMode(boolean asciiMode) {
        this.asciiMode = asciiMode;
    }
}
