package com.energyict.protocolimplv2.dlms.idis.am500;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.comserver.exceptions.ComServerRuntimeException;
import com.energyict.cpo.PropertySpec;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.exceptions.CommunicationException;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimpl.dlms.idis.IDISObjectList;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.protocolimplv2.dlms.idis.am500.events.IDISLogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.dlms.idis.am500.profiledata.IDISProfileDataReader;
import com.energyict.protocolimplv2.dlms.idis.am500.properties.IDISConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am500.properties.IDISProperties;
import com.energyict.protocolimplv2.dlms.idis.am500.registers.IDISRegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.registers.IDISStoredValues;
import com.energyict.protocolimplv2.dlms.idis.am500.topology.IDISMeterTopology;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractDlmsProtocol;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * This V2 protocol is a port from the old V1 IDIS protocol.
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/12/2014 - 10:42
 */
public class AM500 extends AbstractDlmsProtocol {

    private IDISConfigurationSupport idisConfigurationSupport;
    private IDISRegisterFactory registerFactory = null;
    private IDISLogBookFactory idisLogBookFactory = null;
    private IDISProfileDataReader idisProfileDataReader = null;
    private IDISMessaging idisMessaging = null;
    private IDISStoredValues storedValues = null;
    private IDISMeterTopology idisMeterTopology = null;
    private String serialNumber = null;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties()));
    }

    /**
     * A collection of general DSMR50 properties.
     * These properties are not related to the security or the protocol dialects.
     */
    protected ConfigurationSupport getDlmsConfigurationSupport() {
        if (idisConfigurationSupport == null) {
            idisConfigurationSupport = new IDISConfigurationSupport();
        }
        return idisConfigurationSupport;
    }

    public IDISProperties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new IDISProperties();
        }
        return (IDISProperties) dlmsProperties;
    }

    /**
     * Connect to the device and check the cached object list.
     */
    @Override
    public void logOn() {
        connectWithRetries();
        checkCacheObjects();
    }

    /**
     * Add extra retries to the association request.
     * If the request was rejected because by the meter the previous association was still open, this retry mechanism will solve the problem.
     */
    private void connectWithRetries() {
        int tries = 0;

        while (true) {
            ComServerRuntimeException exception;
            try {
                if (getDlmsSession().getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                    getDlmsSession().createAssociation();
                }
                return;
            } catch (ComServerRuntimeException e) {
                if (e instanceof CommunicationException) {
                    throw e;        //Meter cannot be reached, stop
                }
                exception = e;
            }

            //Release and retry the AARQ in case of ACSE exception
            if (++tries > getDlmsSessionProperties().getRetries()) {
                getLogger().severe("Unable to establish association after [" + tries + "/" + (getDlmsSessionProperties().getRetries() + 1) + "] tries.");
                throw MdcManager.getComServerExceptionFactory().createProtocolConnectFailed(exception);
            } else {
                if (getLogger().isLoggable(Level.INFO)) {
                    getLogger().info("Unable to establish association after [" + tries + "/" + (getDlmsSessionProperties().getRetries() + 1) + "] tries. Sending RLRQ and retry ...");
                }
                try {
                    getDlmsSession().getAso().releaseAssociation();
                } catch (ComServerRuntimeException e) {
                    getDlmsSession().getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
                    // Absorb exception: in 99% of the cases we expect an exception here ...
                }
            }
        }
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    protected void checkCacheObjects() {
        boolean readCache = getDlmsSessionProperties().isReadCache();
        if ((((DLMSCache) getDeviceCache()).getObjectList() == null) || (readCache)) {
            if (readCache) {
                getLogger().info("ForcedToReadCache property is true, reading cache!");
                readObjectList();
                ((DLMSCache) getDeviceCache()).saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
            } else {
                getLogger().info("Cache does not exist, using hardcoded copy of object list");
                UniversalObject[] objectList = new IDISObjectList().getObjectList();
                ((DLMSCache) getDeviceCache()).saveObjectList(objectList);
            }
        } else {
            getLogger().info("Cache exist, will not be read!");
        }
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(((DLMSCache) getDeviceCache()).getObjectList());
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        DeviceProtocolCache deviceCache = super.getDeviceCache();
        if (deviceCache == null || !(deviceCache instanceof DLMSCache)) {
            deviceCache = new DLMSCache();
        }
        setDeviceCache(deviceCache);
        return deviceCache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if ((deviceProtocolCache != null) && (deviceProtocolCache instanceof DLMSCache)) {
            DLMSCache dlmsCache = (DLMSCache) deviceProtocolCache;
            super.setDeviceCache(dlmsCache);
        }
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Collections.emptyList();     //This protocol does not manage the connections, it's a physical slave. It must be used in combination with the IDIS gateway (RTU+Server).
    }

    @Override
    public String getProtocolDescription() {
        return "AM500 DLMS (IDIS P1) V2";
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getIDISProfileDataReader().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getIDISProfileDataReader().getLoadProfileData(loadProfiles);
    }

    public IDISProfileDataReader getIDISProfileDataReader() {
        if (idisProfileDataReader == null) {
            idisProfileDataReader = new IDISProfileDataReader(this);
        }
        return idisProfileDataReader;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getIDISLogBookFactory().getLogBookData(logBooks);
    }

    private IDISLogBookFactory getIDISLogBookFactory() {
        if (idisLogBookFactory == null) {
            idisLogBookFactory = new IDISLogBookFactory(this);
        }
        return idisLogBookFactory;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getIdisMessaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getIdisMessaging().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getIdisMessaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return getIdisMessaging().format(propertySpec, messageAttribute);
    }

    private IDISMessaging getIdisMessaging() {
        if (idisMessaging == null) {
            idisMessaging = new IDISMessaging(this);
        }
        return idisMessaging;
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new NoParamsDeviceProtocolDialect());   //No dialect properties, only general properties, since only 1 type of connection is supported: using the IDIS gateway (TCP).
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getIDISRegisterFactory().readRegisters(registers);
    }

    private IDISRegisterFactory getIDISRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new IDISRegisterFactory(this);
        }
        return registerFactory;
    }

    public IDISStoredValues getStoredValues() {
        if (storedValues == null) {
            storedValues = new IDISStoredValues(this);
        }
        return storedValues;
    }

    @Override
    public String getSerialNumber() {
        if (serialNumber == null) {
            serialNumber = super.getSerialNumber();
        }
        return serialNumber;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return getIDISMeterTopology().discoverMBusDevices();
    }

    public IDISMeterTopology getIDISMeterTopology() {
        if (idisMeterTopology == null) {
            idisMeterTopology = new IDISMeterTopology(this);
        }
        return idisMeterTopology;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }
}