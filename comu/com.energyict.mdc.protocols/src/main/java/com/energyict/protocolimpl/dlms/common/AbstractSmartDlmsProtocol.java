/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.common;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.protocols.mdc.services.impl.OrmClient;
import com.energyict.protocols.util.CacheMechanism;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.protocolimpl.base.ProtocolProperties;
import com.energyict.protocolimpl.dlms.RtuDLMS;
import com.energyict.protocolimpl.dlms.RtuDLMSCache;
import com.energyict.smartmeterprotocolimpl.common.AbstractSmartMeterProtocol;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Abstract Implementation of a DLMS SmartMeterProtocol.
 */
public abstract class AbstractSmartDlmsProtocol extends AbstractSmartMeterProtocol implements CacheMechanism, HHUEnabler {

    /**
     * The used {@link com.energyict.dlms.DlmsSession}
     */
    protected DlmsSession dlmsSession;
    private final OrmClient ormClient;

    protected AbstractSmartDlmsProtocol(PropertySpecService propertySpecService, OrmClient ormClient) {
        super(propertySpecService);
        this.ormClient = ormClient;
    }

    protected OrmClient getOrmClient() {
        return ormClient;
    }

    /**
     * <p></p>
     *
     * @return the current device time
     * @throws java.io.IOException <br>
     */
    public Date getTime() throws IOException {
        try {
            getLogger().info("Reading CLOCK");
            return getDlmsSession().getCosemObjectFactory().getClock().getDateTime();
        } catch (IOException e) {
            getLogger().log(Level.FINEST, e.getMessage());
            throw new IOException("Could not retrieve the Clock object." + e);
        }
    }

    /**
     * Used by sub protocols that implement ProtocolLink
     */
    public ApplicationServiceObject getAso() {
        return getDlmsSession().getAso();
    }

    /**
     * <p>
     * sets the device time to the current system time.
     * </p>
     *
     * @param newMeterTime the time to set in the meter
     * @throws java.io.IOException Thrown in case of an exception
     */
    public void setTime(Date newMeterTime) throws IOException {
        try {
            this.dlmsSession.getCosemObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(newMeterTime));
        } catch (IOException e) {
            getLogger().log(Level.FINEST, e.getMessage());
            throw new IOException("Could not set the Clock object." + e);
        }
    }

    /**
     * The {@link DLMSCache} of the current RTU
     */
    protected DLMSCache dlmsCache;

    /**
     * Getter for the {@link DlmsProtocolProperties}
     *
     * @return the requested Properties
     */
    protected abstract DlmsProtocolProperties getProperties();

    /**
     * Initialization method right after we are connected to the physical device.
     */
    protected abstract void initAfterConnect() throws ConnectionException;

    @Override
    protected ProtocolProperties getProtocolProperties() {
        return getProperties();
    }

    public DlmsSession getDlmsSession() {
        if (dlmsSession == null) {
            dlmsSession = new DlmsSession(getInputStream(), getOutputStream(), getLogger(), getProperties(), getTimeZone());
        }
        return dlmsSession;
    }

    /**
     * Make a connection to the physical device.
     * Setup the association and check the objectList
     *
     * @throws java.io.IOException if errors occurred during data fetching
     */
    public void connect() throws IOException {
        getDlmsSession().connect();
        checkCacheObjects();
        initAfterConnect();
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    protected void checkCacheObjects() throws IOException {
        int configNumber = -1;
        boolean changed = false;
        try {
            if (this.dlmsCache != null && this.dlmsCache.getObjectList() != null) { // the dlmsCache exists
                getDlmsSession().getMeterConfig().setInstantiatedObjectList(this.dlmsCache.getObjectList());

                getLogger().info("Checking the configuration parameters.");
                configNumber = requestConfigurationChanges();

                if (this.dlmsCache.getConfProgChange() != configNumber) {
                    getLogger().info("Meter configuration has changed, configuration is forced to be read.");
                    requestConfiguration();
                    changed = true;
                }

            } else { // cache does not exist
                this.dlmsCache = new DLMSCache();
                getLogger().info("Cache does not exist, configuration is forced to be read.");
                requestConfiguration();
                configNumber = requestConfigurationChanges();
                changed = true;
            }
        } finally {
            if (changed) {
                this.dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
                this.dlmsCache.setConfProgChange(configNumber);
            }
        }
    }

    /**
     * Read the number of configuration changes in the meter
     * The number should increase if something in the configuration or firmware changed. This can cause the objectlist to change.
     * <br>
     * <i>This method may be overridden to fetch the version in a getWithListRequest</i>
     *
     * @return the number of configuration changes.
     * @throws IOException
     */
    public int requestConfigurationChanges() throws IOException {
        try {
            return (int) getDlmsSession().getCosemObjectFactory().getCosemObject(getDlmsSession().getMeterConfig().getConfigObject().getObisCode()).getValue();
        } catch (IOException e) {
            getLogger().log(Level.FINEST, e.getMessage());
            throw new IOException("Could not retrieve the configuration change parameter" + e);
        }
    }

    /**
     * Request Association buffer list out of the meter.
     *
     * @throws IOException if something fails during the request or the parsing of the buffer
     */
    protected void requestConfiguration() throws IOException {

        try {
            if (getDlmsSession().getReference() == ProtocolLink.LN_REFERENCE) {
                getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDlmsSession().getCosemObjectFactory().getAssociationLN().getBuffer());
            } else if (getDlmsSession().getReference() == ProtocolLink.SN_REFERENCE) {
                getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDlmsSession().getCosemObjectFactory().getAssociationSN().getBuffer());
            } else {
                throw new IllegalArgumentException("Invalid reference method, only 0 and 1 are allowed.");
            }
        } catch (IOException e) {
            getLogger().log(Level.FINEST, e.getMessage());
            throw new IOException("Requesting configuration failed." + e);
        }
    }


    /**
     * Disconnect from the physical device.
     * Close the association and check if we need to close the underlying connection
     */
    public void disconnect() throws IOException {
        getDlmsSession().disconnect();
    }

    @Override
    public void setCache(Object cacheObject) {
        this.dlmsCache = (DLMSCache) cacheObject;
    }

    @Override
    public Object getCache() {
        return dlmsCache;
    }

    /**
     * The name under which the file will be save in the OperatingSystem.
     *
     * @return the expected fileName of the cacheFile.
     */
    public String getFileName() {
        final Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_" + getProperties().getSerialNumber() + "_SimpleDLMS.cache";
    }

    /**
     * Fetch the DLMS cache from the database
     *
     * @param rtuid - the RTU database id
     * @return a DLMS cache object
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public Object fetchCache(final int rtuid) throws SQLException {
        if (rtuid != 0) {
            RtuDLMSCache rtuCache = new RtuDLMSCache(rtuid, this.ormClient);
            RtuDLMS rtu = new RtuDLMS(rtuid, ormClient);
            try {
                return new DLMSCache(rtuCache.getObjectList(), rtu.getConfProgChange());
            } catch (NotFoundException e) {
                return new DLMSCache(null, -1);
            }
        } else {
            throw new IllegalArgumentException("invalid RtuId!");
        }
    }

    /**
     * Write the DLMSCache back to the database
     *
     * @param rtuid       - the RTU database id
     * @param cacheObject - the DLMSCache
     * @throws java.sql.SQLException if a database access error occurs
     */
    @Override
    public void updateCache(final int rtuid, final Object cacheObject) throws SQLException {
        if (rtuid != 0) {
            DLMSCache dc = (DLMSCache) cacheObject;
            if (dc.isDirty()) {
                new RtuDLMS(rtuid, ormClient).saveObjectList(dc.getConfProgChange(), dc.getObjectList());
            }
        } else {
            throw new IllegalArgumentException("invalid RtuId!");
        }
    }

    /**
     * Tests if the Device wants to use the bulkRequests
     *
     * @return true if the Device wants to use BulkRequests, false otherwise
     */
    public boolean supportsBulkRequests() {
        return getProperties().isBulkRequest();
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    /**
     * This is an empty implementation, and you should override this method if you're using the HHUSignon
     *
     * @param commChannel       The SerialCommunicationChannel
     * @param enableDataReadout enable or disable the data readout
     * @throws ConnectionException
     */
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException {

    }

    /**
     * Override this method to use the HHUData readout
     *
     * @return empty byte array (new byte[0])
     */
    public byte[] getHHUDataReadout() {
        return new byte[0];
    }

    @Override
    public Optional<String> getActiveCalendarName() throws IOException {
        ActivityCalendar activityCalendar = this.getDlmsSession().getCosemObjectFactory().getActivityCalendar(DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE);
        String calendarName = activityCalendar.readCalendarNameActive().stringValue();
        if (calendarName != null && !calendarName.isEmpty()) {
            return Optional.of(calendarName);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getPassiveCalendarName() throws IOException {
        ActivityCalendar activityCalendar = this.getDlmsSession().getCosemObjectFactory().getActivityCalendar(DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE);
        String calendarName = activityCalendar.readCalendarNamePassive().stringValue();
        if (calendarName != null && !calendarName.isEmpty()) {
            return Optional.of(calendarName);
        } else {
            return Optional.empty();
        }
    }

}
