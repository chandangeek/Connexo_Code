package com.energyict.protocolimplv2.dlms.idis.hs3300;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.FrameCounterProvider;
import com.energyict.dlms.cosem.PLCOFDMType2MACSetup;
import com.energyict.dlms.cosem.SixLowPanAdaptationLayerSetup;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.MirrorTcpDeviceProtocolDialect;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.AdvancedDeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.security.RequestSecurityLevel;
import com.energyict.mdc.upl.security.ResponseSecurityLevel;
import com.energyict.mdc.upl.security.SecuritySuite;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.DataEncryptionException;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocol.exception.ProtocolExceptionMessageSeeds;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.hs3300.messages.HS3300Messaging;
import com.energyict.protocolimplv2.dlms.idis.hs3300.profiles.HS3300LoadProfileDataReader;
import com.energyict.protocolimplv2.dlms.idis.hs3300.properties.HS3300ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.hs3300.properties.HS3300Properties;
import com.energyict.protocolimplv2.dlms.idis.hs3300.registers.HS3300RegisterFactory;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.G3NodeState;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;
import com.energyict.protocolimplv2.security.DlmsSecuritySuite1And2Support;
import com.energyict.protocolimplv2.security.SecurityPropertySpecTranslationKeys;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class HS3300 extends AbstractDlmsProtocol implements SerialNumberSupport, AdvancedDeviceProtocolSecurityCapabilities {

    protected static final int MANAGEMENT_CLIENT   = 1;
    protected static final int DATA_READOUT_CLIENT = 2;
    protected static final int PLC_CLIENT          = 4;
    protected static final int PUBLIC_CLIENT       = 16;

    private static final ObisCode FC_MANAGEMENT   = ObisCode.fromString("0.0.43.1.1.255");
    private static final ObisCode FC_DATA_READOUT = ObisCode.fromString("0.0.43.1.2.255");
    private static final ObisCode FC_PLC_CLIENT   = ObisCode.fromString("0.0.43.1.4.255");

    private final TariffCalendarExtractor calendarExtractor;
    private final NlsService nlsService;
    private final Converter converter;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private CertificateWrapperExtractor certificateWrapperExtractor;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;

    protected HS3300Messaging deviceMessaging;
    private HS3300Cache deviceCache;
    private HS3300RegisterFactory registerFactory;
    private HS3300LoadProfileDataReader loadProfileDataReader;
    private PLCOFDMType2MACSetup plcMACSetup;
    private SixLowPanAdaptationLayerSetup sixLowPanSetup;
    private Array neighbourTable;
    private Array adpRoutingTable;

    public HS3300(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                  TariffCalendarExtractor calendarExtractor, NlsService nlsService, Converter converter,
                  DeviceMessageFileExtractor messageFileExtractor, CertificateWrapperExtractor certificateWrapperExtractor,
                  KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        this.calendarExtractor           = calendarExtractor;
        this.nlsService                  = nlsService;
        this.converter                   = converter;
        this.messageFileExtractor        = messageFileExtractor;
        this.certificateWrapperExtractor = certificateWrapperExtractor;
        this.keyAccessorTypeExtractor    = keyAccessorTypeExtractor;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        //getDeviceCache().setConnectionToBeaconMirror(getDlmsSessionProperties().useBeaconMirrorDeviceDialect());
        journal("Start protocol for " + offlineDevice.getSerialNumber());
        journal("Version: " + getVersion());
        handleFC(comChannel);
        initDlmsSession(comChannel);
        journal("Protocol init successful");
    }

    protected void initDlmsSession(ComChannel comChannel) {
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties(), getLogger()));
    }

    @Override
    public String getVersion() {
        return "$Date: 2020-08-26$";
    }

    /**
     * Read out the serial number, this can either be of the module (equipment identifier) or of the connected e-meter.
     * Note that reading out this register from the mirror logical device in the Beacon, the ObisCode must always be 0.0.96.1.0.255
     */
    @Override
    public String getSerialNumber() {
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect() || !getDlmsSessionProperties().useEquipmentIdentifierAsSerialNumber()) {
            return getMeterInfo().getSerialNr();
        } else {
            return getMeterInfo().getEquipmentIdentifier();
        }
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
                journal("Connecting with client "+dlmsSession.getProperties().getClientMacAddress()+" to "+dlmsSession.getProperties().getServerUpperMacAddress());
                dlmsSession.getDLMSConnection().setRetries(0);   //Temporarily disable retries in the connection layer, AARQ retries are handled here
                if (dlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                    dlmsSession.getDlmsV2Connection().connectMAC();
                    dlmsSession.createAssociation((int) getDlmsSessionProperties().getAARQTimeout());
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
            if (++tries > getDlmsSessionProperties().getAARQRetries()) {
                journal(Level.SEVERE, "Unable to establish association after [" + tries + "/" + (getDlmsSessionProperties().getAARQRetries() + 1) + "] tries.");
                throw CommunicationException.protocolConnectFailed(exception);
            } else {
                journal("Unable to establish association after [" + tries + "/" + (getDlmsSessionProperties().getAARQRetries() + 1) + "] tries. Sending RLRQ and retry ...");
                try {
                    dlmsSession.getAso().releaseAssociation();
                } catch (ProtocolRuntimeException e) {
                    // Absorb exception: in 99% of the cases we expect an exception here ...
                }
                dlmsSession.getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
            }
        }
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.NONE;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList(
                new SioOpticalConnectionType(this.getPropertySpecService()),
                new RxTxOpticalConnectionType(this.getPropertySpecService()),
                new OutboundTcpIpConnectionType(this.getPropertySpecService())
        );
    }

    @Override
    public String getProtocolDescription() {
        return "Honeywell HS3300 DLMS Meter";
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.asList(
                new SerialDeviceProtocolDialect(this.getPropertySpecService(), nlsService), // HDLC
                new TcpDeviceProtocolDialect(this.getPropertySpecService(), nlsService),    // Gateway
                new MirrorTcpDeviceProtocolDialect(this.getPropertySpecService(), nlsService)); // Mirror
    }

    @Override
    public HS3300Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new HS3300Properties(this.getPropertySpecService(), nlsService, certificateWrapperExtractor);
        }
        return (HS3300Properties) dlmsProperties;
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    @Override
    protected final void checkCacheObjects() {
        boolean readCache = getDlmsSessionProperties().isReadCache();
        final int clientId = getDlmsSessionProperties().getClientMacAddress();

        if ((getDeviceCache().getObjectList(clientId) == null) || readCache) {
            journal(readCache ? "ReReadCache property is true, reading cache!" : "The cache was empty, reading out the object list!");

            readObjectList();

            final UniversalObject[] objectList = getDlmsSession().getMeterConfig().getInstantiatedObjectList();
            getDeviceCache().setObjectList(clientId, objectList);
        } else {
            journal("Cache exist, will not be read!");
        }

        getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDeviceCache().getObjectList(clientId));
    }

    @Override
    public HS3300Cache getDeviceCache() {
        if (deviceCache == null) {
            deviceCache = new HS3300Cache(getDlmsSessionProperties().useBeaconMirrorDeviceDialect());
        }
        return deviceCache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if (deviceProtocolCache instanceof HS3300Cache) {
            deviceCache = (HS3300Cache) deviceProtocolCache;
        }
    }

    @Override
    public void terminate() {
        // As a last step, update the cache with the last FC
        if (getDlmsSession() != null && getDlmsSession().getAso() != null && getDlmsSession().getAso().getSecurityContext() != null) {
            long frameCounter = getDlmsSession().getAso().getSecurityContext().getFrameCounter();
            int clientMacAddress = getDlmsSessionProperties().getClientMacAddress();
            getDeviceCache().setTXFrameCounter(clientMacAddress, frameCounter);
            journal("Caching frameCounter=" + frameCounter + " for client=" + clientMacAddress);
        }
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        final DeviceIdentifier meterId = new DeviceIdentifierById(offlineDevice.getId());
        CollectedTopology deviceTopology = getCollectedDataFactory().createCollectedTopology(meterId);

        boolean canReadTopology = true;

        if (!getNeighbourTable().isPresent()){
            canReadTopology = false;
            journal(Level.WARNING, "Cannot read topology: Neighbour table is not present.");
        }

        if (!getPLCMacSetup().isPresent()){
            canReadTopology = false;
            journal(Level.WARNING, "Cannot read topology: PLC Mac-Setup table is not present.");
        }

        if (canReadTopology) {
            final Array neighbourArray = getNeighbourTable().get();
            journal(neighbourArray.toString());
            for (final AbstractDataType node : neighbourArray) {
                final Structure neighbourStruct = (Structure) node;

                // build up the topologyNeighbour
                long shortAddress = neighbourStruct.getDataType(0).getUnsigned16().longValue();
                int modulationSchema = neighbourStruct.getDataType(1).getBooleanObject().intValue();
                long toneMap = neighbourStruct.getDataType(2).getBitString().longValue();
                int modulation = neighbourStruct.getDataType(3).getTypeEnum().getValue();
                int txGain = neighbourStruct.getDataType(4).getInteger8().getValue();
                int txRes = neighbourStruct.getDataType(5).getTypeEnum().getValue();
                int txCoeff = neighbourStruct.getDataType(6).getBitString().intValue();
                int lqi = neighbourStruct.getDataType(7).getUnsigned8().intValue();
                int phaseDifferential = neighbourStruct.getDataType(8).getInteger8().intValue();
                int tmrValidTime = neighbourStruct.getDataType(9).getUnsigned8().intValue();
                int neighbourValidTime = neighbourStruct.getDataType(10).getUnsigned8().intValue();

                long macPANId = -1;
                try {
                    macPANId = getPLCMacSetup().get().readPanId().longValue();
                } catch (final NotInObjectListException e) {
                    journal(Level.WARNING, "Could not read PAN ID: NotInObjectListException");
                } catch (final IOException e) {
                    journal(Level.WARNING, "IOException while reading PAN ID");
                }

                /**
                 * The neighborDeviceIdentifier and nodeAddress (full MAC address)
                 * cannot be obtain from the meter. We rely on the shortAddress to find later the device with
                 * the corresponding 'Short address PAN' property equal to this.
                 */
                final DeviceIdentifier neighbourDeviceIdentifier = null;
                final String nodeAddress = null;

                final Date lastUpdate = new Date();
                final Date lastPathRequest = new Date();
                final int state = G3NodeState.UNKNOWN.getValue();
                final long roundTrip = 0;
                int linkCost = 0;

                if (getADPRoutingTable().isPresent()) {
                    final Optional<Structure> neighbourNode = findNeighbourNode(shortAddress);
                    if (neighbourNode.isPresent()) {
                        linkCost = neighbourNode.get().getDataType(2).getUnsigned16().getValue();
                    }
                }

                deviceTopology.addTopologyNeighbour(
                        neighbourDeviceIdentifier, modulationSchema, toneMap, modulation, txGain, txRes,
                        txCoeff, lqi, phaseDifferential, tmrValidTime, neighbourValidTime, macPANId,
                        nodeAddress, Math.toIntExact(shortAddress), lastUpdate, lastPathRequest, state, roundTrip, linkCost
                );

                StringBuilder sb = new StringBuilder();

                sb.append("Topology neighbour: ");
                sb.append("shortAddress: ").append(Math.toIntExact(shortAddress));
                sb.append(", modulationSchema: ").append(modulationSchema);
                sb.append(", toneMap: ").append(toneMap);
                sb.append(", modulation: ").append(modulation);
                sb.append(", txGain: ").append(txGain);
                sb.append(", txRes: ").append(txRes);
                sb.append(", txCoeff: ").append(txCoeff);
                sb.append(", lqi: ").append(lqi);
                sb.append(", phaseDifferential: ").append(phaseDifferential);
                sb.append(", tmrValidTime: ").append(tmrValidTime);
                sb.append(", macPANId: ").append(macPANId);
                sb.append(", state: ").append(state);
                sb.append(", roundTrip: ").append(roundTrip);
                sb.append(", linkCost: ").append(linkCost);
                sb.append(", lastUpdate: ").append(lastUpdate);
                sb.append(", lastPathRequest: ").append(lastPathRequest);
                sb.append(", neighbourValidTime: ").append(neighbourValidTime);

                journal(sb.toString());
            }
        }

        return deviceTopology;
    }

    private Optional<Structure> findNeighbourNode(long shortAddress) {
        if (getADPRoutingTable().isPresent()) {
            for (final AbstractDataType item : getADPRoutingTable().get()) {
                final Structure structure = item.getStructure();
                if (structure.getDataType(0).isUnsigned16() &&
                    structure.getDataType(0).getUnsigned16().longValue() == shortAddress) {
                    return Optional.of( structure );
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Array> getNeighbourTable() {
        try {
            if (neighbourTable == null) {
                if (getPLCMacSetup().isPresent()) {
                    neighbourTable = (Array) getPLCMacSetup().get().readNeighbourTable();
                    return Optional.of(neighbourTable);
                } else {
                    journal(Level.WARNING, "Could not read neighbour table: could not get PLCOFDMType2MACSetup");
                }
            } else {
                return Optional.of(neighbourTable);
            }
        } catch (final IOException e) {
            journal(Level.WARNING, "Could not read neighbour table: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * @return Optional of Array of routing_table: struct
     * {
     *    destination_address: long_unsigned
     *    next_hop_address: long_unsigned
     *    route_cost: long_unsigned
     *    hop_count: unsigned
     *    weak_link_count: unsigned
     *    valid time: long_unsigned
     * } if available, Optional.empty() otherwise
     */
    private Optional<Array> getADPRoutingTable() {
        try {
            if (adpRoutingTable == null) {
                if (getSixLowPanSetup().isPresent()) {
                    adpRoutingTable = getSixLowPanSetup().get().readAdpRoutingTable();
                    return Optional.of(adpRoutingTable);
                } else {
                    journal(Level.WARNING, "Could not read ADP routing table: could not get SixLowPanAdaptationLayerSetup");
                }
            } else {
                return Optional.of(adpRoutingTable);
            }
        } catch (final IOException e) {
            journal(Level.WARNING, "Could not read ADP routing table: " + e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<PLCOFDMType2MACSetup> getPLCMacSetup() {
        if (plcMACSetup == null) {
            try {
                plcMACSetup = getDlmsSession().getCosemObjectFactory().getPLCOFDMType2MACSetup();
                return Optional.of(plcMACSetup);
            } catch (final NotInObjectListException e) {
                journal(Level.WARNING, "Could not get PLCOFDMType2MACSetup: NotInObjectListException");
            }
        } else {
            return Optional.of(plcMACSetup);
        }
        return Optional.empty();
    }

    private Optional<SixLowPanAdaptationLayerSetup> getSixLowPanSetup() {
        if (sixLowPanSetup == null) {
            try {
                sixLowPanSetup = getDlmsSession().getCosemObjectFactory().getSixLowPanAdaptationLayerSetup();
                return Optional.of(sixLowPanSetup);
            } catch (final NotInObjectListException e) {
                journal(Level.WARNING, "Could not get SixLowPanAdaptationLayerSetup: NotInObjectListException");
            }
        } else {
            return Optional.of(sixLowPanSetup);
        }
        return Optional.empty();
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getLoadProfileDataReader().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getLoadProfileDataReader().getLoadProfileData(loadProfiles);
    }

    private HS3300LoadProfileDataReader getLoadProfileDataReader() {
        if (this.loadProfileDataReader == null) {
            this.loadProfileDataReader = new HS3300LoadProfileDataReader(this, getCollectedDataFactory(), getIssueFactory());
        }
        return this.loadProfileDataReader;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return null;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getDeviceMessaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getDeviceMessaging().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getDeviceMessaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getDeviceMessaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return getDeviceMessaging().prepareMessageContext(device, offlineDevice, deviceMessage);
    }

    protected HS3300Messaging getDeviceMessaging() {
        if (this.deviceMessaging == null) {
            this.deviceMessaging = new HS3300Messaging(this, getCollectedDataFactory(), getIssueFactory(),
                    getPropertySpecService(), nlsService, converter, calendarExtractor, certificateWrapperExtractor,
                    messageFileExtractor, keyAccessorTypeExtractor);
        }
        return this.deviceMessaging;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    protected HS3300RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new HS3300RegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerFactory;
    }

    /**
     * First read out the frame counter for the management client, using the public client.
     * Unless of course the whole session is done with the public client, then there's no need to read out the FC.
     */
    private void handleFC(ComChannel comChannel) {
        if (getDlmsSessionProperties().getAuthenticationSecurityLevel() < 5) {
            journal("Skipping FC handling due to lower security level.");
            return; // no need to handle any FC
        }

        if (!getDlmsSessionProperties().usesPublicClient()) {
            final int clientId = getDlmsSessionProperties().getClientMacAddress();

            boolean weHaveValidCachedFrameCounter = false;
            if (getDlmsSessionProperties().useCachedFrameCounter()) {
                weHaveValidCachedFrameCounter = getCachedFrameCounter(comChannel, clientId);
            }

            if (!weHaveValidCachedFrameCounter) {
                readFrameCounterSecure(comChannel);
            }
        }
    }

    /**
     * Read frame counter by calling a custom method in the Beacon
     */
    protected void readFrameCounterSecure(ComChannel comChannel) {
        journal("Reading frame counter using secure method");

        byte[] authenticationKey = getDlmsSessionProperties().getSecurityProvider().getAuthenticationKey();
        if (authenticationKey.length != 16) {
            throw DeviceConfigurationException.unsupportedPropertyValueLengthWithReason(SecurityPropertySpecTranslationKeys.AUTHENTICATION_KEY.toString(), String.valueOf(authenticationKey.length * 2), "Need a plain text AuthenticationKey (32 hex chars) to read out the frame counter securely using HMAC");
        }

        // construct a temporary session with 0:0 security and clientId=16 (public)
        final TypedProperties publicProperties = TypedProperties.copyOf(getDlmsSessionProperties().getProperties());
        publicProperties.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, BigDecimal.valueOf(PUBLIC_CLIENT));
        final HS3300Properties publicClientProperties = new HS3300Properties(this.getPropertySpecService(), nlsService, certificateWrapperExtractor);
        publicClientProperties.addProperties(publicProperties);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(BigDecimal.valueOf(PUBLIC_CLIENT), 0, 0, 0, 0, 0, publicProperties));    //SecurityLevel 0:0

        final DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties, getDlmsSessionProperties().getSerialNumber());
        final ObisCode frameCounterObisCode = getFrameCounterForClient(getDlmsSessionProperties().getClientMacAddress());
        final long frameCounter;

        publicDlmsSession.getDlmsV2Connection().connectMAC();
        publicDlmsSession.createAssociation((int) getDlmsSessionProperties().getAARQTimeout());

        try {

            FrameCounterProvider frameCounterProvider = publicDlmsSession.getCosemObjectFactory().getFrameCounterProvider(frameCounterObisCode);
            frameCounterProvider.setSkipValidation(getDlmsSessionProperties().skipFramecounterAuthenticationTag());

            frameCounter = frameCounterProvider.getFrameCounter(authenticationKey);

            journal("The read-out frame-counter is: " + frameCounter);

        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, publicDlmsSession.getProperties().getRetries() + 1);
        } catch (Exception e) {
            final ProtocolException protocolException = new ProtocolException(e, "Error while reading out the secure frame counter, cannot continue! " + e.getMessage());
            throw ConnectionCommunicationException.unExpectedProtocolError(protocolException);
        } finally {
            publicDlmsSession.disconnect();
        }

        setTXFrameCounter(frameCounter + 1);
    }

    /**
     * Get the frame counter from the cache, for the given clientId.
     * If no frame counter is available in the cache (value -1), use the configured InitialFC property.
     * <p/>
     * Additionally, the FC value can be validated with ValidateCachedFrameCounterAndFallback
     */
    private boolean getCachedFrameCounter(ComChannel comChannel, int clientId) {
        journal("Will try to use a cached frame counter");
        boolean weHaveAFrameCounter = false;
        long cachedFrameCounter = getDeviceCache().getTXFrameCounter(clientId);
        long initialFrameCounter = getDlmsSessionProperties().getInitialFrameCounter();

        if (initialFrameCounter > cachedFrameCounter) { //Note that this is also the case when the cachedFrameCounter is unavailable (value -1).
            journal("Using initial frame counter: " + initialFrameCounter + " because it has a higher value than the cached frame counter: " + cachedFrameCounter);
            setTXFrameCounter(initialFrameCounter);
            weHaveAFrameCounter = true;
        } else if (cachedFrameCounter > 0) {
            journal("Using cached frame counter: " + cachedFrameCounter);
            setTXFrameCounter(cachedFrameCounter + 1);
            weHaveAFrameCounter = true;
        }

        if (weHaveAFrameCounter) {
            if (getDlmsSessionProperties().validateCachedFrameCounter()) {
                return testConnectionAndRetryWithFrameCounterIncrements(comChannel);
            } else {
                journal(Level.WARNING, " - cached frame counter will not be validated - if the communication fails please set the cache property back to {No}, so a fresh one will be read-out");
                // do not validate, just use it and hope for the best
                return true;
            }
        }

        return false;
    }

    private void setTXFrameCounter(long frameCounter) {
        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }

    private boolean testConnectionAndRetryWithFrameCounterIncrements(ComChannel comChannel) {
        DlmsSession testDlmsSession = getDlmsSessionForFCTesting(comChannel);
        int retries = getDlmsSessionProperties().getFrameCounterRecoveryRetries();
        int step = getDlmsSessionProperties().getFrameCounterRecoveryStep();
        boolean releaseOnce = true;

        journal("Will test the frameCounter. Recovery mechanism: retries=" + retries + ", step=" + step);
        if (retries <= 0) {
            retries = 0;
            step = 0;
        }

        do {
            try {
                testDlmsSession.getDlmsV2Connection().connectMAC();
                testDlmsSession.createAssociation();
                if (testDlmsSession.getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_CONNECTED) {
                    testDlmsSession.disconnect();
                    journal("Cached FrameCounter is valid!");
                    setTXFrameCounter(testDlmsSession.getAso().getSecurityContext().getFrameCounter());
                    return true;
                }
            } catch (CommunicationException ex) {
                if (isAssociationFailed(ex)) {
                    long frameCounter = testDlmsSession.getAso().getSecurityContext().getFrameCounter();
                    journal(Level.WARNING, "Current frame counter [" + frameCounter + "] is not valid, received exception " + ex.getMessage() + ", increasing frame counter by " + step);
                    frameCounter += step;
                    setTXFrameCounter(frameCounter);
                    testDlmsSession.getAso().getSecurityContext().setFrameCounter(frameCounter);

                    if (releaseOnce) {
                        releaseOnce = false;
                        //Try to release that association once, it may be that it was still open from a previous session, causing troubles to create the new association.
                        try {
                            testDlmsSession.getAso().releaseAssociation();
                        } catch (ProtocolRuntimeException e) {
                            testDlmsSession.getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
                            // Absorb exception: in 99% of the cases we expect an exception here ...
                        }
                    }
                } else {
                    throw ex;       //Propagate any other exception
                }
            }
            retries--;
        } while (retries > 0);

        testDlmsSession.disconnect();
        journal(Level.WARNING, "Could not validate the frame counter, seems that it's out-of sync with the device. You'll have to read a fresh one.");
        return false;
    }

    private boolean isAssociationFailed(CommunicationException ex) {
        return ex.getMessageSeed() == ProtocolExceptionMessageSeeds.UNEXPECTED_RESPONSE
                || ex.getMessageSeed() == ProtocolExceptionMessageSeeds.UNEXPECTED_PROTOCOL_ERROR
                || ex.getMessageSeed() == ProtocolExceptionMessageSeeds.PROTOCOL_CONNECT;
    }

    /**
     * Sub classes (for example the crypto-protocol) can override
     */
    protected DlmsSession getDlmsSessionForFCTesting(ComChannel comChannel) {
        return new DlmsSession(comChannel, getDlmsSessionProperties(), getLogger());
    }

    protected ObisCode getFrameCounterForClient(int clientId) {
        switch (clientId) {
            case MANAGEMENT_CLIENT:
                return FC_MANAGEMENT;
            case DATA_READOUT_CLIENT:
                return FC_DATA_READOUT;
            case PLC_CLIENT:
                return FC_PLC_CLIENT;
            case PUBLIC_CLIENT:
            default:
                return FC_MANAGEMENT;
        }
    }

    /**
     * A collection of general AM500 properties.
     * These properties are not related to the security or the protocol dialects.
     */
    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = getNewInstanceOfConfigurationSupport();
        }
        return dlmsConfigurationSupport;
    }

    private HasDynamicProperties getNewInstanceOfConfigurationSupport() {
        return new HS3300ConfigurationSupport(this.getPropertySpecService());
    }

    @Override
    protected AdvancedDeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (dlmsSecuritySupport == null) {
            dlmsSecuritySupport = new DlmsSecuritySuite1And2Support(this.getPropertySpecService());
        }
        return (AdvancedDeviceProtocolSecurityCapabilities) dlmsSecuritySupport;
    }

    @Override
    public List<SecuritySuite> getSecuritySuites() {
        return getSecuritySupport().getSecuritySuites();
    }

    @Override
    public List<RequestSecurityLevel> getRequestSecurityLevels() {
        return getSecuritySupport().getRequestSecurityLevels();
    }

    @Override
    public List<ResponseSecurityLevel> getResponseSecurityLevels() {
        return getSecuritySupport().getResponseSecurityLevels();
    }

    protected NlsService getNlsService() {
        return nlsService;
    }

    protected Converter getConverter() {
        return converter;
    }

    protected TariffCalendarExtractor getCalendarExtractor() {
        return calendarExtractor;
    }

    protected DeviceMessageFileExtractor getMessageFileExtractor() {
        return messageFileExtractor;
    }

    protected KeyAccessorTypeExtractor getKeyAccessorTypeExtractor() {
        return keyAccessorTypeExtractor;
    }

    protected CertificateWrapperExtractor getCertificateWrapperExtractor() {
        return certificateWrapperExtractor;
    }
}
