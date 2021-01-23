package com.energyict.protocolimplv2.dlms;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.mdc.protocol.journal.ProtocolJournal;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.*;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.*;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.DataEncryptionException;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.common.composedobjects.ComposedMeterInfo;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;
import com.energyict.protocolimplv2.nta.dsmr23.topology.MeterTopology;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Common functionality that is shared between the smart V2 DLMS protocols
 * <p>
 * Copyrights EnergyICT
 * Date: 18/10/13
 * Time: 13:30
 * Author: khe
 */
public abstract class AbstractDlmsProtocol implements DeviceProtocol, SerialNumberSupport {

    protected DlmsSessionProperties dlmsProperties;
    protected AbstractMeterTopology meterTopology;
    protected OfflineDevice offlineDevice;
    protected HasDynamicProperties dlmsConfigurationSupport;
    protected DLMSCache dlmsCache;
    protected DeviceProtocolSecurityCapabilities dlmsSecuritySupport;
    private ComposedMeterInfo meterInfo;
    private DlmsSession dlmsSession;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final PropertySpecService propertySpecService;

    /**
     * Indicating if the meter has a breaker.
     * This implies whether or not we can control the breaker and read the control logbook.
     * This will be set to false in the cryptoserver protocols, because these meters don't have a breaker anymore.
     */
    private boolean hasBreaker = true;
    private Logger logger;
    private ProtocolJournal protocolJournal;

    public AbstractDlmsProtocol(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.propertySpecService = propertySpecService;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    public CollectedDataFactory getCollectedDataFactory() {
        return collectedDataFactory;
    }

    public IssueFactory getIssueFactory() {
        return issueFactory;
    }

    /**
     * Connect to the device, check the cached object lost and discover its MBus slaves.
     */
    @Override
    public void logOn() {
        getDlmsSession().connect();
        checkCacheObjects();
    }

    @Override
    public Date getTime() {
        return getMeterInfo().getClock();
    }

    @Override
    public void setTime(Date timeToSet) {
        journal( "Set new date: " + timeToSet.toString() + "." );
        try {
            getDlmsSession().getCosemObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(timeToSet, getTimeZone()));
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSessionProperties().getRetries() + 1);
        }
    }

    @Override
    public String getSerialNumber() {
        return getMeterInfo().getSerialNr();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        this.getDlmsSessionProperties().addProperties(properties);
    }

    /**
     * Dialect properties, add them to the DLMS session properties
     *
     * @param dialectProperties the DeviceProtocolDialectProperties to add to the DeviceProtocol
     */
    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        getDlmsSessionProperties().addProperties(dialectProperties);
    }

    /**
     * Security related properties, add them to the DLMS session properties
     *
     * @param deviceProtocolSecurityPropertySet the {@link DeviceProtocolSecurityPropertySet}to set
     */
    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        getDlmsSessionProperties().addProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
        getDlmsSessionProperties().setSecurityPropertySet(deviceProtocolSecurityPropertySet);
    }

    protected DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (dlmsSecuritySupport == null) {
            dlmsSecuritySupport = new DsmrSecuritySupport(this.propertySpecService);
        }
        return dlmsSecuritySupport;
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return getSecuritySupport().getSecurityProperties();
    }

    @Override
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return getSecuritySupport().getClientSecurityPropertySpec();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return getSecuritySupport().getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return getSecuritySupport().getEncryptionAccessLevels();
    }

    @Override
    public Optional<PropertySpec> getSecurityPropertySpec(String name) {
        return getSecuritySupport().getSecurityPropertySpec(name);
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return getMeterTopology().getDeviceTopology();
    }

    public AbstractMeterTopology getMeterTopology() {
        if (this.meterTopology == null) {
            this.meterTopology = new MeterTopology(this, collectedDataFactory);
            meterTopology.searchForSlaveDevices();
        }
        return meterTopology;
    }

    @Override
    public DLMSCache getDeviceCache() {
        return dlmsCache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        this.dlmsCache = (DLMSCache) deviceProtocolCache;
    }

    /**
     * Request Association buffer list out of the meter.
     */
    protected void readObjectList() {
        try {
            if (getDlmsSession().getReference() == ProtocolLink.LN_REFERENCE) {
                getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDlmsSession().getCosemObjectFactory().getAssociationLN().getBuffer());
            } else if (getDlmsSession().getReference() == ProtocolLink.SN_REFERENCE) {
                getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDlmsSession().getCosemObjectFactory().getAssociationSN().getBuffer());
            } else {
                throw new IllegalArgumentException("Invalid reference method, only 0 and 1 are allowed.");
            }
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSessionProperties().getRetries() + 1);
        }
    }

    /**
     * Add extra retries to the association request.
     * If the request was rejected because by the meter the previous association was still open, this retry mechanism will solve the problem.
     *
     * @param dlmsSession
     */
    protected void connectWithRetries(DlmsSession dlmsSession) {
        int tries = 0;
        while (true) {
            ProtocolRuntimeException exception;
            try {
                dlmsSession.getDLMSConnection().setRetries(0); // Temporarily disable retries in the connection layer, AARQ retries are handled here
                if (dlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                    dlmsSession.getDlmsV2Connection().connectMAC();
                    dlmsSession.createAssociation();
                }
                return;
            } catch (ProtocolRuntimeException e) {
                journal(Level.WARNING, e.getMessage(), e);
                if (e.getCause() != null && e.getCause() instanceof DataAccessResultException) {
                    throw e;    // Throw real errors, e.g. unsupported security mechanism, wrong password...
                } else if (e instanceof ConnectionCommunicationException) {
                    throw e;
                } else if (e instanceof DataEncryptionException) {
                    throw e;
                }
                exception = e;
            } finally {
                dlmsSession.getDLMSConnection().setRetries(getDlmsSessionProperties().getRetries());
            }

            // Release and retry the AARQ in case of ACSE exception
            if (++tries > dlmsSession.getProperties().getRetries()) {
                journal(Level.SEVERE, "Unable to establish association after [" + tries + "/" + (dlmsSession.getProperties().getRetries() + 1) + "] tries.");
                throw CommunicationException.protocolConnectFailed(exception);
            } else {
                journal("Unable to establish association after [" + tries + "/" + (dlmsSession.getProperties().getRetries() + 1) + "] tries. Sending RLRQ and retry ...");
                try {
                    dlmsSession.getAso().releaseAssociation();
                } catch (ProtocolRuntimeException e) {
                    dlmsSession.getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
                    // Absorb exception: in 99% of the cases we expect an exception here ...
                }
            }
        }
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    protected void checkCacheObjects() {
        int configNumber = -1;
        boolean changed = false;
        try {
            if (this.dlmsCache != null && this.dlmsCache.getObjectList() != null) { // the dlmsCache exists
                getDlmsSession().getMeterConfig().setInstantiatedObjectList(this.dlmsCache.getObjectList());

                journal("Checking the configuration parameters.");
                configNumber = getMeterInfo().getConfigurationChanges();

                if (this.dlmsCache.getConfProgChange() != configNumber) {
                    journal("Meter configuration has changed, configuration is forced to be read.");
                    readObjectList();
                    changed = true;
                }

            } else { // cache does not exist
                this.dlmsCache = new DLMSCache();
                journal("Cache does not exist, configuration is forced to be read.");
                readObjectList();
                configNumber = getMeterInfo().getConfigurationChanges();
                changed = true;
            }
        } finally {
            if (changed) {
                this.dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
                this.dlmsCache.setConfProgChange(configNumber);
            }
        }
    }

    @Override
    public void logOff() {
        if (getDlmsSession() != null) {
            getDlmsSession().disconnect();
        }
    }

    public DlmsSession getDlmsSession() {
        return dlmsSession;
    }

    public void setDlmsSession(DlmsSession dlmsSession) {
        this.dlmsSession = dlmsSession;
    }

    /**
     * 'Lazy' getter for the {@link #meterInfo}
     *
     * @return the {@link #meterInfo}
     */
    protected ComposedMeterInfo getMeterInfo() {
        if (meterInfo == null) {
            meterInfo = new ComposedMeterInfo(getDlmsSession(),
                    getDlmsSessionProperties().isBulkRequest(),
                    getDlmsSessionProperties().getRoundTripCorrection(),
                    getDlmsSessionProperties().getRetries()
            );
        }
        return meterInfo;
    }

    public DlmsSessionProperties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new DlmsProperties();
        }
        return dlmsProperties;
    }

    public TimeZone getTimeZone() {
        return getDlmsSessionProperties().getTimeZone();
    }

    /**
     * Return a B-Field corrected ObisCode.
     *
     * @param obisCode     the ObisCode to correct
     * @param serialNumber the serialNumber of the device for which this ObisCode must be corrected
     * @return the corrected ObisCode
     */
    public ObisCode getPhysicalAddressCorrectedObisCode(ObisCode obisCode, String serialNumber) {
        if (obisCode.anyChannel()) {
            return getMeterTopology().getPhysicalAddressCorrectedObisCode(obisCode, serialNumber);
        } else {
            return obisCode;
        }
    }

    /**
     * Return the serialNumber of the meter which corresponds with the B-Field of the given ObisCode
     *
     * @param obisCode the ObisCode
     * @return the serialNumber
     */
    public String getSerialNumberFromCorrectObisCode(ObisCode obisCode) {
        return getMeterTopology().getSerialNumber(obisCode);
    }

    /**
     * Search for the physicalAddress of the meter with the given serialNumber
     *
     * @param serialNumber the serialNumber of the meter
     * @return the requested physical address or -1 when it could not be found
     */
    public int getPhysicalAddressFromSerialNumber(String serialNumber) {
        return getMeterTopology().getPhysicalAddress(serialNumber);
    }

    @Override
    public void terminate() {
        //Nothing to do here...
    }

    @Override
    public void daisyChainedLogOn() {
        logOn();
    }

    @Override
    public void daisyChainedLogOff() {
        logOff();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return getDlmsConfigurationSupport().getUPLPropertySpecs();
    }

    public boolean hasBreaker() {
        return hasBreaker;
    }

    /**
     * Setter is only called from the cryptoserver protocols to remove the breaker functionality
     */
    public void setHasBreaker(boolean hasBreaker) {
        this.hasBreaker = hasBreaker;
    }

    /**
     * A collection of general DLMS properties.
     * These properties are not related to the security or the protocol dialects.
     */
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new DlmsConfigurationSupport(this.propertySpecService);
        }
        return dlmsConfigurationSupport;
    }

     /**
     * In Connexo logging is not visible on the web-gui
     *
     * @deprecated use {@link #journal(String)}  instead.
     */
    @Deprecated
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getName());
        }
        return logger;
    }

    @Override
    public void setProtocolJournaling(ProtocolJournal protocolJournal){
        this.protocolJournal = protocolJournal;
    }

    @Override
    public void journal(String message){
        if (protocolJournal!=null){
            try {
                protocolJournal.addToJournal(message);
            } catch(Exception x){
                //swallow
            }
        }
        try {
            Logger.getLogger(this.getClass().getName()).fine(message);
        } catch (Exception x){
            //swallow
        }
    }

    /**
     * Internal wrapper for non-info log messages
     * @param level
     * @param message
     */
    public void journal(Level level, String message) {
        getLogger().log(level, message);
        journal("["+level.getLocalizedName()+"]: "+message);
    }


    public void journal(Level level, String message, Throwable e) {
        try {
            journal(level, message + "\n" + e.getStackTrace().toString());
        } catch (Throwable ex){
            journal(level, message);
        }
    }
    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }

    public boolean useDsmr4SelectiveAccessFormat() {
        return false;
    }

    /**
     * Default DLMS activity calendar. Subclasses can override.
     */
    @Override
    public CollectedCalendar getCollectedCalendar() {
        CollectedCalendar result = this.collectedDataFactory.createCalendarCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
        try {
            ActivityCalendar activityCalendar = getDlmsSession().getCosemObjectFactory().getActivityCalendar(DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE);

            journal("Reading active calendar name from "+DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE);
            String nameActive = activityCalendar.readCalendarNameActive().stringValue();
            journal("Active calendar name is "+nameActive);
            result.setActiveCalendar(nameActive);

            journal("Reading passive calendar name from "+DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE);
            String namePassive = activityCalendar.readCalendarNamePassive().stringValue();
            journal("Passive calendar name is "+namePassive);
            result.setPassiveCalendar(namePassive);

        } catch (IOException e) {
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getDlmsSessionProperties().getRetries())) {
                Issue problem = this.issueFactory.createProblem(
                        DLMSActivityCalendarController.ACTIVITY_CALENDAR_OBISCODE,
                        "issue.protocol.readingOfCalendarFailed",
                        e.toString());
                result.setFailureInformation(ResultType.InCompatible, problem);
                journal(Level.WARNING, "Error while reading calendar information: "+e.getLocalizedMessage());
            } //Else, a communication timeout is thrown
        }
        return result;
    }

    /**
     * Returns the value of register 1.0.0.2.0.255 as text. Subclasses can override.
     */
    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        CollectedFirmwareVersion firmwareVersionsCollectedData = this.collectedDataFactory.createFirmwareVersionsCollectedData(new DeviceIdentifierById(this.offlineDevice.getId()));
        firmwareVersionsCollectedData.setActiveMeterFirmwareVersion(getMeterInfo().getFirmwareVersion());
        return firmwareVersionsCollectedData;
    }

    /**
     * Return empty status by default, subclasses can override.
     */
    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return this.collectedDataFactory.createBreakerStatusCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
    }
}
