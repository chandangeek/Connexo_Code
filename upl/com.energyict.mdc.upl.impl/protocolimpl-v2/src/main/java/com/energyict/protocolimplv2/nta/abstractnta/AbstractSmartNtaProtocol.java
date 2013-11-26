package com.energyict.protocolimplv2.nta.abstractnta;

import com.energyict.cpo.PropertySpec;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.dsmr23.*;
import com.energyict.protocolimplv2.nta.dsmr23.logbooks.Dsmr23LogBookFactory;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23Messaging;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.dsmr23.registers.Dsmr23RegisterFactory;
import com.energyict.protocolimplv2.nta.dsmr23.topology.MeterTopology;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Common functionality that is shared between the smart V2 DSMR protocols
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/10/13
 * Time: 13:30
 * Author: khe
 */
public abstract class AbstractSmartNtaProtocol implements DeviceProtocol, NtaProtocol {

    private Dsmr23RegisterFactory registerFactory = null;
    private ComposedMeterInfo meterInfo;
    private DlmsProperties dlmsProperties;
    private DlmsConfigurationSupport dlmsConfigurationSupport;
    private DlmsSession dlmsSession;
    private LoadProfileBuilder loadProfileBuilder;
    private DLMSCache dlmsCache;
    private MeterTopology meterTopology;
    private Dsmr23LogBookFactory logBookFactory;
    private Dsmr23Messaging dsmr23Messaging;

    /**
     * Connect to the device, check the cached object lost and discover its MBus slaves.
     */
    @Override
    public void logOn() {
        getDlmsSession().connect();
        checkCacheObjects();
        getMeterTopology().searchForSlaveDevices();
    }

    public Dsmr23LogBookFactory getDeviceLogBookFactory() {
        if (logBookFactory == null) {
            logBookFactory = new Dsmr23LogBookFactory(this);
        }
        return logBookFactory;
    }

    protected Dsmr23Messaging getDsmr23Messaging() {
        if (dsmr23Messaging == null) {
            dsmr23Messaging = new Dsmr23Messaging(this);
        }
        return dsmr23Messaging;
    }

    public MeterTopology getMeterTopology() {
        if (this.meterTopology == null) {
            this.meterTopology = new MeterTopology(this);
        }
        return meterTopology;
    }

    protected Dsmr23RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new Dsmr23RegisterFactory(this, getDlmsSessionProperties().isBulkRequest());
        }
        return registerFactory;
    }

    protected LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new LoadProfileBuilder(this, getDlmsSessionProperties().isBulkRequest());
        }
        return loadProfileBuilder;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        this.dlmsCache = (DLMSCache) deviceProtocolCache;
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return dlmsCache;
    }

    @Override
    public Logger getLogger() {
        return Logger.getLogger(this.getClass().getName());
    }

    /**
     * Request Association buffer list out of the meter.
     */
    private void requestConfiguration() {
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
                    requestConfiguration();
                    changed = true;
                }

            } else { // cache does not exist
                this.dlmsCache = new DLMSCache();
                getLogger().info("Cache does not exist, configuration is forced to be read.");
                requestConfiguration();
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

    protected void setDlmsSession(DlmsSession dlmsSession) {
        this.dlmsSession = dlmsSession;
    }

    @Override
    public DlmsSession getDlmsSession() {
        return dlmsSession;
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

    protected DlmsSessionProperties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new DlmsProperties();
        }
        return dlmsProperties;
    }

    @Override
    public void terminate() {
        //Nothing to do here...
    }

    @Override
    public void daisyChainedLogOn() {
        //Nope
    }

    @Override
    public void daisyChainedLogOff() {
        //Nope
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return getDlmsConfigurationSupport().getRequiredProperties();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return getDlmsConfigurationSupport().getOptionalProperties();
    }

    /**
     * A collection of general DLMS properties.
     * These properties are not related to the security or the protocol dialects.
     */
    private DlmsConfigurationSupport getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new DlmsConfigurationSupport();
        }
        return dlmsConfigurationSupport;
    }
}
