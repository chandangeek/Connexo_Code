package com.energyict.protocolimplv2.nta.dsmr50.elster.am540;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.protocolimplv2.ApplicationServiceObjectV2;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.common.ComServerRuntimeException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOnV2;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.protocolimpl.dlms.idis.AM540ObjectList;
import com.energyict.protocolimplv2.common.MyOwnPrivateRegister;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.g3.common.G3Topology;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.logbooks.Dsmr50LogBookFactory;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages.AM540MessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages.AM540Messaging;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.profiles.AM540LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.dsmr50.registers.Dsmr50RegisterFactory;
import com.energyict.protocolimplv2.securitysupport.DsmrSecuritySupport;
import com.energyict.protocols.impl.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.protocols.impl.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.protocols.mdc.protocoltasks.SerialDeviceProtocolDialect;
import com.energyict.protocols.mdc.protocoltasks.TcpDeviceProtocolDialect;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
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
    private long initialFrameCounter = -1;
    private AM540LoadProfileBuilder loadProfileBuilder;
    private Dsmr50LogBookFactory dsmr50LogBookFactory;
    private Dsmr50RegisterFactory registerFactory;
    private AM540Cache am540Cache;

    @Inject
    public AM540(
            PropertySpecService propertySpecService, SocketService socketService, SerialComponentService serialComponentService,
            IssueService issueService, TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService,
            IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService,
            LoadProfileFactory loadProfileFactory, Clock clock, Thesaurus thesaurus, Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider) {
        super(clock, thesaurus, propertySpecService, socketService, serialComponentService, issueService, topologyService,
                readingTypeUtilService, identificationService, collectedDataFactory, meteringService, loadProfileFactory,
                dsmrSecuritySupportProvider);
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
        if (!getOfflineDevice().getAllSlaveDevices().isEmpty()) {
            getMeterTopology().searchForSlaveDevices();
        }
    }

    @Override
    public AM540Cache getDeviceCache() {
        if (this.am540Cache == null) {
            am540Cache = new AM540Cache(getDlmsSessionProperties().useBeaconMirrorDeviceDialect());
        }
        this.am540Cache.setFrameCounter(getDlmsSession().getAso().getSecurityContext().getFrameCounter() + 1);     //Save this for the next session
        return this.am540Cache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if ((deviceProtocolCache != null) && (deviceProtocolCache instanceof AM540Cache)) {
            am540Cache = (AM540Cache) deviceProtocolCache;
            this.initialFrameCounter = this.am540Cache.getFrameCounter();
        }
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        super.setSecurityPropertySet(deviceProtocolSecurityPropertySet);
        this.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(initialFrameCounter == -1 ? 1 : initialFrameCounter);    //Set the frameCounter from last session (which has been loaded from cache)
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    protected void checkCacheObjects() {
        boolean readCache = getDlmsSessionProperties().isReadCache();
        if ((getDeviceCache().getObjectList() == null) || (readCache)) {
            if (readCache) {
                getLogger().fine("ForcedToReadCache property is true, reading cache!");
                readObjectList();
                getDeviceCache().saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
            } else {
                getLogger().fine("Cache does not exist, using hardcoded copy of object list");
                UniversalObject[] objectList = new AM540ObjectList().getObjectList();
                getDeviceCache().saveObjectList(objectList);
            }
        } else {
            getLogger().fine("Cache exist, will not be read!");
        }
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDeviceCache().getObjectList());
    }

    public DSMR50Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new DSMR50Properties(getPropertySpecService(), this.getThesaurus());
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
                checkIfWeCanSolveWithAReleaseAssociation(e);

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

    private void checkIfWeCanSolveWithAReleaseAssociation(ComServerRuntimeException e) {
        if (e.getMessage() != null &&
                e.getMessage().contains("Application Association Establishment Failed, ACSE_SERVICE_USER, no reason given")) {
            if (getDlmsSession().getAso() != null) {
                try {
                    getDlmsSession().getAso().releaseAssociation();
                } catch (IOException | DLMSConnectionException e1) {
                    // just log it
                    getLogger().fine(e1::getMessage);
                }
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
        result.add(new SioOpticalConnectionType(getSerialComponentService(), this.getThesaurus()));
        result.add(new RxTxOpticalConnectionType(getSerialComponentService(), this.getThesaurus()));
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
            this.loadProfileBuilder = new AM540LoadProfileBuilder(this, getIssueService(), getReadingTypeUtilService(), getDlmsProperties().isBulkRequest(), getCollectedDataFactory());
            loadProfileBuilder.setCumulativeCaptureTimeChannel(getDlmsSessionProperties().isCumulativeCaptureTimeChannel());
        }
        return loadProfileBuilder;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBookReaders) {
        return getDsmr50LogBookFactory().getLogBookData(logBookReaders);
    }

    private Dsmr50LogBookFactory getDsmr50LogBookFactory() {
        if (dsmr50LogBookFactory == null) {
            dsmr50LogBookFactory = new Dsmr50LogBookFactory(getCollectedDataFactory(), getIssueService(), getMeteringService(), this);
        }
        return dsmr50LogBookFactory;
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
        return Arrays.asList(
                new TcpDeviceProtocolDialect(this.getThesaurus(), this.getPropertySpecService()),
                new SerialDeviceProtocolDialect(this.getThesaurus(), this.getPropertySpecService()));
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    protected Dsmr50RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new Dsmr50RegisterFactory(this, getIssueService(), getReadingTypeUtilService(), getDlmsProperties().isBulkRequest(), getCollectedDataFactory(), getClock());
        }
        return registerFactory;
    }

    AM540Messaging getAM540Messaging() {
        if (this.am540Messaging == null) {
            this.am540Messaging = new AM540Messaging(new AM540MessageExecutor(this, getClock(), getTopologyService(), getIssueService(), getReadingTypeUtilService(), getCollectedDataFactory(), getLoadProfileFactory()), getTopologyService());
        }
        return am540Messaging;
    }

    /**
     * Read out the serial number, this can either be of the module (equipment identifier) or of the connected e-meter.
     * Note that reading out this register from the mirror logical device in the Beacon, the obiscode must always be 0.0.96.1.0.255
     */
    @Override
    public String getSerialNumber() {
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect() || !getDlmsSessionProperties().useEquipmentIdentifierAsSerialNumber()) {
            return getMeterInfo().getSerialNr();
        } else {
            return getMeterInfo().getEquipmentIdentifier();
        }
    }

    @Override
    public Date getTime() {
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect()) {
            return new Date();  //Don't read out the clock of the mirror logical device, it does not know the actual meter time.
        } else {
            return super.getTime();
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect()) {
            IOException cause = new IOException("When connected to the mirror logical device, writing of the clock is not allowed.");
            throw new CommunicationException(MessageSeeds.NOT_ALLOWED_TO_DO_SET_TIME, cause);
        } else {
            super.setTime(timeToSet);
        }
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
    public DSMR50Properties getDlmsProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new DSMR50Properties(getPropertySpecService(), this.getThesaurus());
        }
        return (DSMR50Properties) dlmsProperties;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        List<CollectedRegister> collectedRegisters = getRegisterFactory().readRegisters(Collections.singletonList(getFirmwareRegister()));
        CollectedFirmwareVersion firmwareVersionsCollectedData = getCollectedDataFactory().createFirmwareVersionsCollectedData(this.offlineDevice.getDeviceIdentifier());
        if (!collectedRegisters.isEmpty() && collectedRegisters.get(0).getResultType().equals(ResultType.Supported) && collectedRegisters.get(0).getText() != null) {
            firmwareVersionsCollectedData.setActiveMeterFirmwareVersion(collectedRegisters.get(0).getText());
            return firmwareVersionsCollectedData;
        }
        collectedRegisters.stream().forEach(each -> each.getIssues().stream().forEach(issue -> firmwareVersionsCollectedData.setFailureInformation(each.getResultType(), issue)));
        firmwareVersionsCollectedData.setFailureInformation(ResultType.DataIncomplete, createCouldNotReadoutFirmwareVersionIssue(getFirmwareRegister()));
        return firmwareVersionsCollectedData;
    }

    protected Issue createCouldNotReadoutFirmwareVersionIssue(OfflineRegister firmwareRegister) {
        return getIssueService().newProblem(
                firmwareRegister,
                com.energyict.mdc.protocol.api.MessageSeeds.COULD_NOT_READ_FIRMWARE_VERSION,
                firmwareRegister.getObisCode());
    }

    private OfflineRegister getFirmwareRegister() {
        return new MyOwnPrivateRegister(this.getOfflineDevice(), ObisCode.fromString("1.1.0.2.0.255"));
    }

    private OfflineRegister getCalendarRegister(ObisCode obisCode) {
        return new MyOwnPrivateRegister(this.getOfflineDevice(), obisCode);
    }

    @Override
    public boolean supportsCommunicationFirmwareVersion() {
        return true;
    }
}