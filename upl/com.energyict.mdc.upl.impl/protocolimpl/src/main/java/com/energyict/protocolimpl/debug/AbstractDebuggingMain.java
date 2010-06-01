package com.energyict.protocolimpl.debug;

import com.energyict.dialer.core.LinkException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.RegisterProtocol;

import java.io.IOException;
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

    abstract P getMeterProtocol();

    abstract void doDebug() throws LinkException, IOException;

    private RegisterProtocol getRegisterProtocol() {
        if (getMeterProtocol() instanceof RegisterProtocol) {
            return (RegisterProtocol) getMeterProtocol();
        } else {
            return null;
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

}
