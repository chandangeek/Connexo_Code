package com.energyict.protocolimplv2.dlms.idis.am500;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cpo.PropertySpec;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.DataEncryptionException;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.dlms.idis.IDISObjectList;
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

import java.io.IOException;
import java.util.ArrayList;
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
public class AM500 extends AbstractDlmsProtocol implements SerialNumberSupport{

    protected IDISLogBookFactory idisLogBookFactory = null;
    protected IDISMessaging idisMessaging = null;
    protected IDISProfileDataReader idisProfileDataReader = null;
    protected IDISStoredValues storedValues = null;
    private IDISRegisterFactory registerFactory = null;
    private String serialNumber = null;
    private static final ObisCode LOGICAL_DEVICE_NAME_OBIS = ObisCode.fromString("0.0.42.0.0.255");

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
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = getNewInstanceOfConfigurationSupport();
        }
        return dlmsConfigurationSupport;
    }

    protected ConfigurationSupport getNewInstanceOfConfigurationSupport() {
        return new IDISConfigurationSupport();
    }

    public IDISProperties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = getNewInstanceOfProperties();
        }
        return (IDISProperties) dlmsProperties;
    }

    protected IDISProperties getNewInstanceOfProperties() {
        return new IDISProperties();
    }

    /**
     * Connect to the device and check the cached object list.
     */
    @Override
    public void logOn() {
        connectWithRetries(getDlmsSession());
        checkCacheObjects();
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
                dlmsSession.getDLMSConnection().setRetries(0);   //Temporarily disable retries in the connection layer, AARQ retries are handled here
                if (dlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                    dlmsSession.getDlmsV2Connection().connectMAC();
                    dlmsSession.createAssociation();
                }
                return;
            } catch (ProtocolRuntimeException e) {
                if (e.getCause() != null && e.getCause() instanceof DataAccessResultException) {
                    throw e;        //Throw real errors, e.g. unsupported security mechanism, wrong password...
                } else if (e instanceof ConnectionCommunicationException) {
                    throw e;
                } else if (e instanceof DataEncryptionException) {
                    throw e;
                }
                exception = e;
            } finally {
                dlmsSession.getDLMSConnection().setRetries(getDlmsSessionProperties().getRetries());
            }

            //Release and retry the AARQ in case of ACSE exception
            if (++tries > dlmsSession.getProperties().getRetries()) {
                getLogger().severe("Unable to establish association after [" + tries + "/" + (dlmsSession.getProperties().getRetries() + 1) + "] tries.");
                throw CommunicationException.protocolConnectFailed(exception);
            } else {
                getLogger().info("Unable to establish association after [" + tries + "/" + (dlmsSession.getProperties().getRetries() + 1) + "] tries. Sending RLRQ and retry ...");
                try {
                    dlmsSession.getAso().releaseAssociation();
                } catch (ProtocolRuntimeException e) {
                    dlmsSession.getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
                    // Absorb exception: in 99% of the cases we expect an exception here ...
                }
            }
        }
    }

    @Override
    protected DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (dlmsSecuritySupport == null) {
            dlmsSecuritySupport = new AM500SecuritySupport();
        }
        return dlmsSecuritySupport;
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    protected void checkCacheObjects() {
        boolean readCache = getDlmsSessionProperties().isReadCache();
        if ((getDeviceCache().getObjectList() == null) || (readCache)) {
            if (readCache) {
                getLogger().info("ReadCache property is true, reading cache!");
                readObjectList();
                getDeviceCache().saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
            } else {
                getLogger().info("Cache does not exist, using hardcoded copy of object list");
                UniversalObject[] objectList = new IDISObjectList().getObjectList();
                getDeviceCache().saveObjectList(objectList);
            }
        } else {
            getLogger().info("Cache exist, will not be read!");
        }
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDeviceCache().getObjectList());
    }

    @Override
    public DLMSCache getDeviceCache() {
        DLMSCache deviceCache = super.getDeviceCache();
        if (deviceCache == null) {
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
        return "Elster AM500 DLMS (IDIS P1) V2";
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
        return getIDISMessaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getIDISMessaging().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getIDISMessaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getIDISMessaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return "";
    }

    protected IDISMessaging getIDISMessaging() {
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

    /**
     *
     * @return this method returns either serial number or logical device name depending on the value of "UseLogicalDeviceNameAsSerialNumber" property
     */
    @Override
    public String getSerialNumber() {

        if(!getDlmsSessionProperties().useLogicalDeviceNameAsSerialNumber()){
            return getMeterInfo().getSerialNr();
        } else {
            try {
                final Data data = getDlmsSession().getCosemObjectFactory().getData(LOGICAL_DEVICE_NAME_OBIS);
                final OctetString logicalDeviceName = data.getValueAttr(OctetString.class);
                return logicalDeviceName.stringValue();
            } catch (IOException e) {
                throw DLMSIOExceptionHandler.handle(e, getDlmsSessionProperties().getRetries() + 1);
            }
        }
    }

    @Override
    public AbstractMeterTopology getMeterTopology() {
        if (meterTopology == null) {
            meterTopology = new IDISMeterTopology(this);
            meterTopology.searchForSlaveDevices();
        }
        return meterTopology;
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-05-06 09:13:12 +0300 (Fri, 06 May 2016)$";
    }
}