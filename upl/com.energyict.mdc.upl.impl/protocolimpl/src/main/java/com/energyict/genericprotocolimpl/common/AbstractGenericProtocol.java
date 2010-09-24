package com.energyict.genericprotocolimpl.common;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.Link;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.CommunicationScheduler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 5-aug-2010
 * Time: 11:21:58
 */

/**
 * This abstract class should contain all the most used and common methods of a basic generic protocol.
 * Keep this class reusable for other generic protocols. No protocol specific code here.
 */
public abstract class AbstractGenericProtocol implements GenericProtocol {

    private Properties properties = null;
    private long timeDifference = 0;

    private CommunicationScheduler communicationScheduler;
    private Link link;
    private Logger logger;

    /**
     * This is the method where the implementing protocol does all his stuff.
     */
    protected abstract void doExecute();

    public void execute(CommunicationScheduler communicationScheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
        this.communicationScheduler = communicationScheduler;
        this.link = link;
        this.logger = logger;
        doExecute();
    }

    /**
     * Getter for the time difference of the clock in the device
     *
     * @return
     */
    public long getTimeDifference() {
        return timeDifference;
    }

    /**
     * Setter for the time difference of the clock in the device
     *
     * @param timeDifference
     */
    public void setTimeDifference(long timeDifference) {
        this.timeDifference = timeDifference;
    }

    /**
     * Setter for the generic protocol properties
     *
     * @param properties
     */
    public void addProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Lazy getter for the generic protocol properties
     * When properties == null, we will initialize it as a new empty Properties object and return this instead of null;
     *
     * @return
     */
    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    /**
     * Getter for the communicationScheduler field
     *
     * @return
     */
    public CommunicationScheduler getCommunicationScheduler() {
        return communicationScheduler;
    }

    /**
     * Getter for the link field
     *
     * @return
     */
    public Link getLink() {
        return link;
    }

    /**
     * Get the current protocol logger. If the logger == null, initialize it with default logger
     *
     * @return
     */
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    /**
     * Log a message, with a given log level
     *
     * @param level
     * @param message
     */
    protected void log(Level level, String message) {
        getLogger().log(level, message);
    }

    /**
     * Log a message as INFO log level
     * @param message
     */
    protected void log(String message) {
        log(Level.INFO, message);
    }

    public boolean propertyExist(String propertyKey) {
        String value = getProperties().getProperty(propertyKey);
        return (value != null) && (value.length() > 0);
    }

}
