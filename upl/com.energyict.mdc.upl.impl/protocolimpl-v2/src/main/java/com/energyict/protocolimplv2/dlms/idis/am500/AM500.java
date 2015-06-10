package com.energyict.protocolimplv2.dlms.idis.am500;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cpo.PropertySpec;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.exceptions.ComServerExecutionException;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
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
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.AbstractMeterTopology;
import com.energyict.protocolimplv2.dlms.idis.am500.events.IDISLogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.protocolimplv2.dlms.idis.am500.profiledata.IDISProfileDataReader;
import com.energyict.protocolimplv2.dlms.idis.am500.properties.IDISConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am500.properties.IDISProperties;
import com.energyict.protocolimplv2.dlms.idis.am500.registers.IDISRegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.registers.IDISStoredValues;
import com.energyict.protocolimplv2.dlms.idis.topology.IDISMeterTopology;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This V2 protocol is a port from the old V1 IDIS protocol.
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/12/2014 - 10:42
 */
public class AM500 extends AbstractDlmsProtocol {

    protected ConfigurationSupport idisConfigurationSupport;
    protected IDISLogBookFactory idisLogBookFactory = null;
    protected IDISMessaging idisMessaging = null;
    private IDISRegisterFactory registerFactory = null;
    private IDISProfileDataReader idisProfileDataReader = null;
    private IDISStoredValues storedValues = null;
    private String serialNumber = null;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties()));
    }

    /**
     * A collection of general AM500 properties.
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
        connectWithRetries(getDlmsSession(), getDlmsSessionProperties());
        checkCacheObjects();
        getMeterTopology().searchForSlaveDevices();
    }

    /**
     * Add extra retries to the association request.
     * If the request was rejected because by the meter the previous association was still open, this retry mechanism will solve the problem.
     *
     * @param dlmsSession
     */
    protected void connectWithRetries(DlmsSession dlmsSession, IDISProperties dlmsSessionProperties) {
        int tries = 0;
        while (true) {
            ComServerExecutionException exception;
            try {
                if (dlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                    dlmsSession.createAssociation();
                }
                return;
            } catch (ComServerExecutionException e) {
                if (e.getCause() != null && e.getCause() instanceof DataAccessResultException) {
                    throw e;        //Throw real errors, e.g. unsupported security mechanism, wrong password...
                } else if (MdcManager.getComServerExceptionFactory().isConnectionCommunicationException(e)) {
                    throw e;
                } else if (MdcManager.getComServerExceptionFactory().isDataEncryptionException(e)) {
                    throw e;
                }
                exception = e;
            }

            //Release and retry the AARQ in case of ACSE exception
            if (++tries > dlmsSessionProperties.getRetries()) {
                getLogger().severe("Unable to establish association after [" + tries + "/" + (dlmsSessionProperties.getRetries() + 1) + "] tries.");
                throw MdcManager.getComServerExceptionFactory().createProtocolConnectFailed(exception);
            } else {
                getLogger().info("Unable to establish association after [" + tries + "/" + (dlmsSessionProperties.getRetries() + 1) + "] tries. Sending RLRQ and retry ...");
                try {
                    dlmsSession.getAso().releaseAssociation();
                } catch (ComServerExecutionException e) {
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
        boolean readCache = getDlmsSessionProperties().isReadCache();
        if ((((DLMSCache) getDeviceCache()).getObjectList() == null) || (readCache)) {
            if (readCache) {
                getLogger().info("ReadCache property is true, reading cache!");
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
            idisProfileDataReader = new IDISProfileDataReader(this, getDlmsSessionProperties().getLimitMaxNrOfDays());
        }
        return idisProfileDataReader;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getIDISLogBookFactory().getLogBookData(logBooks);
    }

    protected IDISLogBookFactory getIDISLogBookFactory() {
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

    protected IDISMessaging getIdisMessaging() {
        if (idisMessaging == null) {
            idisMessaging = new IDISMessaging(this);
        }
        return idisMessaging;
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new NoParamsDeviceProtocolDialect());
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
    public AbstractMeterTopology getMeterTopology() {
        if (meterTopology == null) {
            meterTopology = new IDISMeterTopology(this);
        }
        return meterTopology;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }
}