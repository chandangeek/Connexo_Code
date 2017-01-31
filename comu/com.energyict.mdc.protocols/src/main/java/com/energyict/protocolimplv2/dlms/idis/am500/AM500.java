/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.idis.am500;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.exceptions.DataEncryptionException;
import com.energyict.mdc.protocol.api.legacy.dynamic.ConfigurationSupport;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.api.tasks.support.DeviceBasicSupport;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
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
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class AM500 extends AbstractDlmsProtocol implements DeviceBasicSupport {

    protected IDISLogBookFactory idisLogBookFactory = null;
    protected IDISMessaging idisMessaging = null;
    protected IDISProfileDataReader idisProfileDataReader = null;
    protected IDISStoredValues storedValues = null;
    private IDISRegisterFactory registerFactory = null;
    private String serialNumber = null;
    private static final ObisCode LOGICAL_DEVICE_NAME_OBIS = ObisCode.fromString("0.0.42.0.0.255");

    @Inject
    public AM500(Clock clock, Thesaurus thesaurus, PropertySpecService propertySpecService, SocketService socketService, SerialComponentService serialComponentService, IssueService issueService, TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService, LoadProfileFactory loadProfileFactory, Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider) {
        super(clock, thesaurus, propertySpecService, socketService, serialComponentService, issueService, topologyService, readingTypeUtilService, identificationService, collectedDataFactory, meteringService, loadProfileFactory, dsmrSecuritySupportProvider);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsProperties().setSerialNumber(offlineDevice.getSerialNumber());
        setDlmsSession(new DlmsSession(comChannel, getDlmsProperties()));
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
        return new IDISConfigurationSupport(propertySpecService);
    }

    public IDISProperties getDlmsProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = getNewInstanceOfProperties();
        }
        return (IDISProperties) dlmsProperties;
    }

    protected IDISProperties getNewInstanceOfProperties() {
        return new IDISProperties(propertySpecService, thesaurus);
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
            ComServerRuntimeException exception;
            try {
                dlmsSession.getDLMSConnection().setRetries(0);   //Temporarily disable retries in the connection layer, AARQ retries are handled here
                if (dlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                    dlmsSession.getDlmsV2Connection().connectMAC();
                    dlmsSession.createAssociation();
                }
                return;
            } catch (ComServerRuntimeException e) {
                getLogger().log(Level.WARNING, e.getMessage(), e);
                if (e.getCause() != null && e.getCause() instanceof DataAccessResultException) {
                    throw e;        //Throw real errors, e.g. unsupported security mechanism, wrong password...
                } else if (e instanceof ConnectionCommunicationException) {
                    throw e;
                } else if (e instanceof DataEncryptionException) {
                    throw e;
                }
                exception = e;
            } finally {
                dlmsSession.getDLMSConnection().setRetries(getDlmsProperties().getRetries());
            }

            //Release and retry the AARQ in case of ACSE exception
            if (++tries > dlmsSession.getProperties().getRetries()) {
                getLogger().severe("Unable to establish association after [" + tries + "/" + (dlmsSession.getProperties().getRetries() + 1) + "] tries.");
                throw new CommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, exception);
            } else {
                getLogger().info("Unable to establish association after [" + tries + "/" + (dlmsSession.getProperties().getRetries() + 1) + "] tries. Sending RLRQ and retry ...");
                try {
                    dlmsSession.getAso().releaseAssociation();
                } catch (ComServerRuntimeException e) {
                    dlmsSession.getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
                    // Absorb exception: in 99% of the cases we expect an exception here ...
                }
            }
        }
    }

    @Override
    protected DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (dlmsSecuritySupport == null) {
            dlmsSecuritySupport = new AM500SecuritySupport(propertySpecService, thesaurus);
        }
        return dlmsSecuritySupport;
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    protected void checkCacheObjects() {
        boolean readCache = getDlmsProperties().isReadCache();
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
        DLMSCache deviceCache = (DLMSCache) super.getDeviceCache();
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
            idisProfileDataReader = new IDISProfileDataReader(this, getDlmsProperties().getLimitMaxNrOfDays(), getCollectedDataFactory(), getIssueService());
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
    public Set<DeviceMessageId> getSupportedMessages() {
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
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return getIDISMessaging().format(propertySpec, messageAttribute);
    }

    protected IDISMessaging getIDISMessaging() {
        if (idisMessaging == null) {
            idisMessaging = new IDISMessaging(this);
        }
        return idisMessaging;
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.<DeviceProtocolDialect>singletonList(new NoParamsDeviceProtocolDialect(thesaurus, propertySpecService));
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

        if(!getDlmsProperties().useLogicalDeviceNameAsSerialNumber()){
            return getMeterInfo().getSerialNr();
        } else {
            try {
                final Data data = getDlmsSession().getCosemObjectFactory().getData(LOGICAL_DEVICE_NAME_OBIS);
                final OctetString logicalDeviceName = data.getValueAttr(OctetString.class);
                return logicalDeviceName.stringValue();
            } catch (IOException e) {
                throw DLMSIOExceptionHandler.handle(e, getDlmsProperties().getRetries() + 1);
            }
        }
    }

    @Override
    public void setTime(Date newMeterTime) {
        try {
            AXDRDateTime dateTime = new AXDRDateTime(newMeterTime, getTimeZone());
            dateTime.useUnspecifiedAsDeviation(getDlmsProperties().useUndefinedAsTimeDeviation());
            getDlmsSession().getCosemObjectFactory().getClock().setAXDRDateTimeAttr(dateTime);
        } catch (IOException e) {
            getLogger().log(Level.FINEST, e.getMessage());
            throw DLMSIOExceptionHandler.handle(e, getDlmsProperties().getRetries() + 1);
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
        return "$Date: 2016-05-09 15:56:50 +0300 (Mon, 09 May 2016)$";
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return null;
    }
}