package com.energyict.protocolimplv2.dlms;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.dsmr23.ComposedMeterInfo;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;
import com.energyict.protocolimplv2.nta.dsmr23.topology.MeterTopology;
import com.energyict.protocolimplv2.security.DlmsSecuritySupport;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Common functionality that is shared between the smart V2 DLMS protocols
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/10/13
 * Time: 13:30
 * Author: khe
 */
public abstract class AbstractDlmsProtocol implements DeviceProtocol {

    protected DlmsProperties dlmsProperties;
    protected AbstractMeterTopology meterTopology;
    protected OfflineDevice offlineDevice;
    protected ConfigurationSupport dlmsConfigurationSupport;
    protected DLMSCache dlmsCache;
    protected DlmsSecuritySupport dlmsSecuritySupport;
    private ComposedMeterInfo meterInfo;
    private DlmsSession dlmsSession;
    /**
     * Indicating if the meter has a breaker.
     * This implies whether or not we can control the breaker and read the control logbook.
     * This will be set to false in the cryptoserver protocols, because these meters don't have a breaker anymore.
     */
    private boolean hasBreaker = true;
    private Logger logger;

    /**
     * Connect to the device, check the cached object lost and discover its MBus slaves.
     */
    @Override
    public void logOn() {
        getDlmsSession().connect();
        checkCacheObjects();
        if (!getOfflineDevice().getAllSlaveDevices().isEmpty()) {
            getMeterTopology().searchForSlaveDevices();
        }
    }

    @Override
    public Date getTime() {
        try {
            return getDlmsSession().getCosemObjectFactory().getClock().getDateTime();
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        try {
            getDlmsSession().getCosemObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(timeToSet, getTimeZone()));
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
    }

    @Override
    public String getSerialNumber() {
        return getMeterInfo().getSerialNr();
    }

    /**
     * General device properties, add them to the DLMS session properties
     *
     * @param properties properties to add
     */
    @Override
    public void addProperties(TypedProperties properties) {
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
            dlmsSecuritySupport = new DsmrSecuritySupport();
        }
        return dlmsSecuritySupport;
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return getSecuritySupport().getSecurityProperties();
    }

    @Override
    public String getSecurityRelationTypeName() {
        return getSecuritySupport().getSecurityRelationTypeName();
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
    public PropertySpec getSecurityPropertySpec(String name) {
        return getSecuritySupport().getSecurityPropertySpec(name);
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return getMeterTopology().getDeviceTopology();
    }

    public AbstractMeterTopology getMeterTopology() {
        if (this.meterTopology == null) {
            this.meterTopology = new MeterTopology(this);
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
            throw IOExceptionHandler.handle(e, getDlmsSession());
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

                getLogger().info("Checking the configuration parameters.");
                configNumber = getMeterInfo().getConfigurationChanges();

                if (this.dlmsCache.getConfProgChange() != configNumber) {
                    getLogger().info("Meter configuration has changed, configuration is forced to be read.");
                    readObjectList();
                    changed = true;
                }

            } else { // cache does not exist
                this.dlmsCache = new DLMSCache();
                getLogger().info("Cache does not exist, configuration is forced to be read.");
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

    protected void setDlmsSession(DlmsSession dlmsSession) {
        this.dlmsSession = dlmsSession;
    }

    /**
     * 'Lazy' getter for the {@link #meterInfo}
     *
     * @return the {@link #meterInfo}
     */
    protected ComposedMeterInfo getMeterInfo() {
        if (meterInfo == null) {
            meterInfo = new ComposedMeterInfo(getDlmsSession(), getDlmsSessionProperties().isBulkRequest());
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
    public List<PropertySpec> getRequiredProperties() {
        return getDlmsConfigurationSupport().getRequiredProperties();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return getDlmsConfigurationSupport().getOptionalProperties();
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
    protected ConfigurationSupport getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new DlmsConfigurationSupport();
        }
        return dlmsConfigurationSupport;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getName());
        }
        return logger;
    }

    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }
}
