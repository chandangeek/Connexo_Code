package com.energyict.protocolimplv2.nta.dsmr50.elster.am540;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.protocolimplv2.ApplicationServiceObjectV2;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.ComChannelType;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.SerialComChannel;
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
import com.energyict.mdc.protocol.api.device.data.*;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOnV2;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocolimpl.dlms.idis.AM540ObjectList;
import com.energyict.protocolimplv2.elster.garnet.SerialDeviceProtocolDialect;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages.AM540MessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages.AM540Messaging;
import com.energyict.protocolimplv2.nta.dsmr50.registers.Dsmr50RegisterFactory;
import com.energyict.protocols.mdc.protocoltasks.TcpDeviceProtocolDialect;
import com.energyict.protocolimplv2.g3.common.G3Topology;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.dsmr40.landisgyr.profiles.LGLoadProfileBuilder;
import com.energyict.protocols.impl.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.protocols.impl.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.protocols.impl.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.AM540Cache;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * V2 version of the AM540 protocol.
 * This version adds breaker & relais support and other IDIS features.
 *
 * @author khe
 * @since 17/12/2014 - 14:30
 */
public class AM540 extends AbstractDlmsProtocol {

    private G3Topology g3Topology;
    private AM540Messaging am540Messaging;

    @Inject
    public AM540(PropertySpecService propertySpecService, SocketService socketService, SerialComponentService serialComponentService, IssueService issueService, TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService, LoadProfileFactory loadProfileFactory, Clock clock) {
        super(clock, propertySpecService, socketService, serialComponentService, issueService, topologyService, readingTypeUtilService, identificationService, collectedDataFactory, meteringService, loadProfileFactory);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsProperties().setSerialNumber(offlineDevice.getSerialNumber());

        HHUSignOnV2 hhuSignOn = null;
        if (ComChannelType.SERIAL_COM_CHANNEL.is(comChannel) || ComChannelType.OPTICAL_COM_CHANNEL.is(comChannel)) {
            hhuSignOn = getHHUSignOn((SerialComChannel) comChannel);
        }
        setDlmsSession(new DlmsSession(comChannel, getDlmsProperties(), hhuSignOn, "P07210"));
    }

    private HHUSignOnV2 getHHUSignOn(SerialComChannel serialPortComChannel) {
        HHUSignOnV2 hhuSignOn = new IEC1107HHUSignOn(serialPortComChannel, getDlmsProperties());
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(false);
        return hhuSignOn;
    }

    /**
     * Connect to the device and check the cached object list.
     */
    @Override
    public void logOn() {
        connectWithRetries();
        checkCacheObjects();
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        DeviceProtocolCache deviceCache = super.getDeviceCache();
        if (deviceCache == null || !(deviceCache instanceof AM540Cache)) {
            deviceCache = new AM540Cache();
        }
        ((AM540Cache) deviceCache).setFrameCounter(getDlmsSession().getAso().getSecurityContext().getFrameCounter() + 1);     //Save this for the next session
        setDeviceCache(deviceCache);
        return deviceCache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if ((deviceProtocolCache != null) && (deviceProtocolCache instanceof AM540Cache)) {
            AM540Cache am540Cache = (AM540Cache) deviceProtocolCache;
            super.setDeviceCache(am540Cache);
            long initialFrameCounter = (am540Cache).getFrameCounter();
            this.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(initialFrameCounter == -1 ? 1 : initialFrameCounter);    //Get this from the last session
        }
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    protected void checkCacheObjects() {
        boolean readCache = getDlmsSessionProperties().isReadCache();
        if ((((DLMSCache) getDeviceCache()).getObjectList() == null) || (readCache)) {
            if (readCache) {
                getLogger().fine("ForcedToReadCache property is true, reading cache!");
                readObjectList();
                ((DLMSCache) getDeviceCache()).saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
            } else {
                getLogger().fine("Cache does not exist, using hardcoded copy of object list");
                UniversalObject[] objectList = new AM540ObjectList().getObjectList();
                ((DLMSCache) getDeviceCache()).saveObjectList(objectList);
            }
        } else {
            getLogger().fine("Cache exist, will not be read!");
        }
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(((DLMSCache) getDeviceCache()).getObjectList());
    }

    public DSMR50Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new DSMR50Properties(getPropertySpecService());
        }
        return (DSMR50Properties) dlmsProperties;
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
                getDlmsSession().getDLMSConnection().setRetries(0);   //AARQ retries are handled here
                getDlmsSession().createAssociation((int) getDlmsSessionProperties().getAARQTimeout());
                return;
            } catch (ComServerRuntimeException e) {
                if (e.getCause() != null && e.getCause() instanceof DataAccessResultException) {
                    throw e;        //Throw real errors, e.g. unsupported security mechanism, wrong password...
                }

                exception = e;
            } finally {
                getDlmsSession().getDLMSConnection().setRetries(getDlmsSessionProperties().getRetries());
            }

            //Release and retry the AARQ in case of ACSE exception
            if (++tries > getDlmsSessionProperties().getAARQRetries()) {
                if (getLogger().isLoggable(Level.SEVERE)) {
                    getLogger().severe("Unable to establish association after [" + tries + "/" + (getDlmsSessionProperties().getAARQRetries() + 1) + "] tries.");
                }
                throw new CommunicationException(MessageSeeds.NUMBER_OF_RETRIES_REACHED, tries);
            } else {
                if (exception instanceof CommunicationException) {
                    if (getLogger().isLoggable(Level.INFO)) {
                        getLogger().info("Unable to establish association after [" + tries + "/" + (getDlmsSessionProperties().getAARQRetries() + 1) + "] tries, due to timeout. Retrying.");
                    }
                } else {
                    if (getLogger().isLoggable(Level.INFO)) {
                        getLogger().info("Unable to establish association after [" + tries + "/" + (getDlmsSessionProperties().getAARQRetries() + 1) + "] tries. Sending RLRQ and retry ...");
                    }
                    try {
                        ((ApplicationServiceObjectV2) getDlmsSession().getAso()).releaseAssociation();
                    } catch (ComServerRuntimeException e) {
                        // Absorb exception: in 99% of the cases we expect an exception here ...
                    }
                }
                getDlmsSession().getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
            }
        }
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundTcpIpConnectionType(getPropertySpecService(), getSocketService()));
        result.add(new SioOpticalConnectionType(getSerialComponentService()));
        result.add(new RxTxOpticalConnectionType(getSerialComponentService()));
        return result;
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AM540 DLMS (NTA DSMR5.0) V2";
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfileReaders);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfileReaders) {
        return getLoadProfileBuilder().getLoadProfileData(loadProfileReaders);
    }

    protected LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new LGLoadProfileBuilder(this, getIssueService(), getReadingTypeUtilService(), getDlmsProperties().isBulkRequest(), getCollectedDataFactory());
            ((LGLoadProfileBuilder) loadProfileBuilder).setCumulativeCaptureTimeChannel(getDlmsSessionProperties().isCumulativeCaptureTimeChannel());
        }
        return loadProfileBuilder;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBookReaders) {
        return Collections.emptyList();
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return getAM540Messaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> list) {
        return getAM540Messaging().executePendingMessages(list);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> list) {
        return getAM540Messaging().updateSentMessages(list);
    }

    @Override
    public String format(PropertySpec propertySpec, Object o) {
        return getAM540Messaging().format(propertySpec, o);
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new SerialDeviceProtocolDialect(getPropertySpecService()), new TcpDeviceProtocolDialect(getPropertySpecService()));
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    protected Dsmr50RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new Dsmr50RegisterFactory(this, getIssueService(), getReadingTypeUtilService(), getDlmsProperties().isBulkRequest(), getCollectedDataFactory());
        }
        return (Dsmr50RegisterFactory) registerFactory;
    }

    AM540Messaging getAM540Messaging() {
        if (this.am540Messaging == null) {
            this.am540Messaging = new AM540Messaging(new AM540MessageExecutor(this, getClock(), getTopologyService(), getIssueService(), getReadingTypeUtilService(), getCollectedDataFactory(), getLoadProfileFactory()), getTopologyService());
        }
        return am540Messaging;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return getG3Topology().collectTopology();
    }

    public G3Topology getG3Topology() {
        if (g3Topology == null) {
            g3Topology = new AM540Topology(this.offlineDevice.getDeviceIdentifier(), getIdentificationService(), getIssueService(), getPropertySpecService(), getDlmsSession(), getDlmsProperties(), getCollectedDataFactory());
        }
        return g3Topology;
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-12-19 17:02:12 +0100 (Fri, 19 Dec 2014) $";
    }

    /**
     * Class that holds all DLMS device properties (general, dialect & security related)
     */
    @Override
    protected DSMR50Properties getDlmsProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new DSMR50Properties(getPropertySpecService());
        }
        return (DSMR50Properties) dlmsProperties;
    }


    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        CollectedFirmwareVersion firmwareVersionsCollectedData = getCollectedDataFactory().createFirmwareVersionsCollectedData(this.offlineDevice.getDeviceIdentifier());
        List<CollectedRegister> collectedRegisters = getRegisterFactory().readRegisters(Collections.singletonList(getFirmwareRegister()));
        firmwareVersionsCollectedData.setActiveMeterFirmwareVersion(collectedRegisters.get(0).getText());
        return firmwareVersionsCollectedData;
    }

    private OfflineRegister getFirmwareRegister() {
        return new OfflineRegister() {

            // Module Version identification
            private ObisCode firmwareObisCode = ObisCode.fromString("1.1.0.2.0.255");

            @Override
            public long getRegisterId() {
                return 0;
            }

            @Override
            public ObisCode getObisCode() {
                return firmwareObisCode;
            }

            @Override
            public boolean inGroup(long registerGroupId) {
                return false;
            }

            @Override
            public boolean inAtLeastOneGroup(Collection<Long> registerGroupIds) {
                return false;
            }

            @Override
            public Unit getUnit() {
                return Unit.getUndefined();
            }

            @Override
            public String getDeviceMRID() {
                return null;
            }

            @Override
            public String getDeviceSerialNumber() {
                return AM540.this.getOfflineDevice().getSerialNumber();
            }

            @Override
            public ObisCode getAmrRegisterObisCode() {
                return firmwareObisCode;
            }

            @Override
            public DeviceIdentifier<?> getDeviceIdentifier() {
                return AM540.this.getOfflineDevice().getDeviceIdentifier();
            }

            @Override
            public ReadingType getReadingType() {
                return null;
            }

            @Override
            public BigDecimal getOverFlowValue() {
                return null;
            }

            @Override
            public boolean isText() {
                return true;
            }
        };
    }

    @Override
    public boolean supportsCommunicationFirmwareVersion() {
        return true;
    }
}