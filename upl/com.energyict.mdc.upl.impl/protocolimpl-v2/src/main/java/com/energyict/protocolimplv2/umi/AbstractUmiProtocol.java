package com.energyict.protocolimplv2.umi;


import com.energyict.mdc.protocol.journal.ProtocolJournal;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.energyict.dlms.DLMSCache;
import com.energyict.protocolimplv2.umi.ei4.EI4UmiSecuritySupport;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_TIMEZONE;

public abstract class AbstractUmiProtocol implements DeviceProtocol, SerialNumberSupport {
    private final PropertySpecService propertySpecService;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    UmiConfigurationSupport umiConfigurationSupport;
    EI4UmiSecuritySupport umiSecuritySupport;

    private Logger logger;
    private ProtocolJournal protocolJournal;

    public AbstractUmiProtocol(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.propertySpecService = propertySpecService;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    @Override
    public void logOn() {

    }

    @Override
    public Date getTime() {
        return null;
    }

    @Override
    public void setTime(Date timeToSet) {

    }

    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(DEFAULT_TIMEZONE);
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        umiConfigurationSupport.setUPLProperties(properties);
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {

    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {

    }

    protected DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (umiSecuritySupport == null) {
            umiSecuritySupport = new EI4UmiSecuritySupport(this.propertySpecService);
        }
        return umiSecuritySupport;
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
        return null;
    }

    @Override
    public DLMSCache getDeviceCache() {
        return null;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
    }

    @Override
    public void logOff() {
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
        return getUmiConfigurationSupport().getUPLPropertySpecs();
    }

    @Override
    public void setProtocolJournaling(ProtocolJournal protocolJournal) {
        this.protocolJournal = protocolJournal;
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
    public void journal(String message) {
        if (protocolJournal != null) {
            try {
                protocolJournal.addToJournal(message);
            } catch (Exception x) {
                //swallow
            }
        }
        try {
            Logger.getLogger(this.getClass().getName()).fine(message);
        } catch (Exception x) {
            //swallow
        }
    }

    /**
     * Internal wrapper for non-info log messages
     *
     * @param level
     * @param message
     */
    public void journal(Level level, String message) {
        getLogger().log(level, message);
        journal("[" + level.getLocalizedName() + "]: " + message);
    }


    public void journal(Level level, String message, Throwable e) {
        try {
            journal(level, message + "\n" + e.getStackTrace().toString());
        } catch (Throwable ex) {
            journal(level, message);
        }
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return null;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        return null;
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return null;
    }

    public CollectedDataFactory getCollectedDataFactory() {
        return collectedDataFactory;
    }

    public IssueFactory getIssueFactory() {
        return issueFactory;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected HasDynamicProperties getUmiConfigurationSupport() {
        if (umiConfigurationSupport == null) {
            umiConfigurationSupport = new UmiConfigurationSupport(this.propertySpecService);
        }
        return umiConfigurationSupport;
    }
}
