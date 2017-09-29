package test.com.energyict.protocolimplv2.coronis.waveflow;

import com.energyict.mdc.channels.ip.socket.ServerWavenisGatewayComChannel;
import com.energyict.mdc.channels.ip.socket.WavenisGatewayConnectionType;
import com.energyict.mdc.channels.serial.rf.WavenisSerialConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;

import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisStack;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolcommon.exceptions.CodingException;
import com.energyict.protocolimpl.properties.Temporals;
import com.energyict.protocolimplv2.comchannels.WavenisStackUtils;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.security.NoSecuritySupport;
import test.com.energyict.protocolimplv2.coronis.common.WaveFlowConnect;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.CommonObisCodeMapper;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter.ParameterFactory;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter.PulseWeight;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.RadioCommandFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 7/06/13
 * Time: 10:59
 * Author: khe
 */
public abstract class WaveFlow implements DeviceProtocol, SerialNumberSupport {

    protected boolean isV1;
    protected boolean isV210;

    private OfflineDevice offlineDevice;
    private DeviceIdentifierById deviceIdentifier;
    private WaveFlowProperties waveFlowProperties;
    private NoSecuritySupport securitySupport;
    private DeviceProtocolCache deviceCache;
    private PulseWeight[] pulseWeights = new PulseWeight[4];

    private ParameterFactory parameterFactory;
    private test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.RadioCommandFactory radioCommandFactory;
    private WaveFlowConnect waveFlowConnect;
    private CommonObisCodeMapper commonObisCodeMapper;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;

    protected WaveFlow(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService) {
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
    }

    protected CollectedDataFactory getCollectedDataFactory() {
        return collectedDataFactory;
    }

    protected IssueFactory getIssueFactory() {
        return issueFactory;
    }

    /**
     * If the ComChannel is of type WavenisGatewayComChannel, this means the RF module is being read out over a transparent gateway (MUC Wavecell).
     * In this case, the comchannel provides a fully started Wavenis stack that allows us to create a link to the module, based on the RF address.
     * <p/>
     * Any other type of ComChannel (e.g. WavenisSerialComChannel) should already contain a direct link to the RF module.
     *
     * @param offlineDevice contains the complete definition/configuration of a Device
     * @param comChannel    the used ComChannel where all read/write actions are going to be performed
     */
    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getWaveFlowProperties().addProperties(offlineDevice.getAllProperties());
        if (comChannel instanceof ServerWavenisGatewayComChannel) {   //Create a link
            WavenisStack wavenisStack = ((ServerWavenisGatewayComChannel) comChannel).getWavenisStack();
            WavenisStackUtils.WavenisInputOutStreams inOutStreams = WavenisStackUtils.createInputOutStreams(getWaveFlowProperties().getRFAddress(), wavenisStack);
            //SynchroneousComChannel synchroneousComChannel = new SynchroneousComChannel(inOutStreams.inputStream, inOutStreams.outputStream);
            //waveFlowConnect = new WaveFlowConnect(synchroneousComChannel, getWaveFlowProperties().getTimeout(), getWaveFlowProperties().getRetries());
        } else {                                                                        //Use the given link
            waveFlowConnect = new WaveFlowConnect(comChannel, getWaveFlowProperties().getTimeout(), getWaveFlowProperties().getRetries());
        }
        radioCommandFactory = new RadioCommandFactory(this);
    }

    /**
     * Each sub protocol has its own register mapper
     */
    protected abstract DeviceRegisterSupport getObisCodeMapper();

    /**
     * Each sub protocol has its own messaging class
     */
    protected abstract DeviceMessageSupport getMessaging();

    /**
     * Each sub protocol has its own profile data reader
     */
    protected abstract DeviceLoadProfileSupport getProfileDataReader();

    /**
     * Each sub protocol has its own event reader
     */
    protected abstract DeviceLogBookSupport getEventReader();

    public WaveFlowProperties getWaveFlowProperties() {
        if (waveFlowProperties == null) {
            waveFlowProperties = new WaveFlowProperties(TypedProperties.empty(), this.propertySpecService);
        }
        return waveFlowProperties;
    }

    public WaveFlowConnect getWaveFlowConnect() {
        return waveFlowConnect;
    }

    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }

    public boolean isV1() {
        return isV1;
    }

    public boolean isV210() {
        return isV210;
    }

    /**
     * Getter for the pulse weight for a specific port
     *
     * @param port port number, zero based!
     */
    public PulseWeight getPulseWeight(int port, boolean requestsAllowed) {
        if (pulseWeights[port] == null) {
            if (requestsAllowed) {
                pulseWeights[port] = getParameterFactory().readPulseWeight(port + 1);
            } else {
                pulseWeights[port] = new PulseWeight(this, 0, 1, port + 1);
            }
        }
        return pulseWeights[port];
    }

    public void setPulseWeights(PulseWeight[] pulseWeights) {
        this.pulseWeights = pulseWeights;
    }

    public PulseWeight getPulseWeight(int port) {
        return getPulseWeight(port, true);
    }

    @Override
    public void terminate() {
        //Nothing to do here
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        List<DeviceProtocolCapabilities> capabilities = new ArrayList<>();
        capabilities.add(DeviceProtocolCapabilities.PROTOCOL_SESSION);   //Read out directly
        capabilities.add(DeviceProtocolCapabilities.PROTOCOL_SLAVE);     //Read out over a gateway
        return capabilities;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return this.getWaveFlowProperties().getPropertySpecs();
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> connectionTypes = new ArrayList<>();
        connectionTypes.add(new WavenisGatewayConnectionType(this.propertySpecService));
        connectionTypes.add(new WavenisSerialConnectionType(this.propertySpecService));
        return connectionTypes;
    }

    @Override
    public void logOn() {
        //Nothing to do here
    }

    @Override
    public void daisyChainedLogOn() {
        //Nothing to do here
    }

    @Override
    public void logOff() {
        //Nothing to do here
    }

    @Override
    public void daisyChainedLogOff() {
        //Nothing to do here
    }

    /**
     * Cannot read out the serial number of the WaveFlow device
     */
    @Override
    public String getSerialNumber() {
        throw CodingException.unsupportedMethod(this.getClass(), "getSerialNumber");
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return deviceCache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        this.deviceCache = deviceProtocolCache;
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getProfileDataReader().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getProfileDataReader().getLoadProfileData(loadProfiles);
    }

    @Override
    public Date getTime() {
        return getParameterFactory().readTimeDateRTC();
    }

    @Override
    public void setTime(Date timeToSet) {
        getParameterFactory().writeTimeDateRTC(timeToSet);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getEventReader().getLogBookData(logBooks);
    }

    public ParameterFactory getParameterFactory() {
        if (parameterFactory == null) {
            parameterFactory = new ParameterFactory(this);
        }
        return parameterFactory;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getObisCodeMapper().readRegisters(registers);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getMessaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    public RadioCommandFactory getRadioCommandFactory() {
        if (radioCommandFactory == null) {
            radioCommandFactory = new RadioCommandFactory(this);
        }
        return radioCommandFactory;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessaging().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessaging().updateSentMessages(sentMessages);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getMessaging().getSupportedMessages();
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        List<DeviceProtocolDialect> dialects = new ArrayList<>();
        dialects.add(new NoParamsDeviceProtocolDialect(nlsService));
        return dialects;
    }

    @Override
    public void addDeviceProtocolDialectProperties(com.energyict.mdc.upl.properties.TypedProperties dialectProperties) {
        this.getWaveFlowProperties().addProperties(dialectProperties);
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        //Do nothing
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

    public DeviceIdentifier getDeviceIdentifier() {
        if (deviceIdentifier == null) {
            deviceIdentifier = new DeviceIdentifierById(offlineDevice.getId());
        }
        return deviceIdentifier;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        CollectedTopology deviceTopology = this.collectedDataFactory.createCollectedTopology(getDeviceIdentifier());
        deviceTopology.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(offlineDevice, "devicetopologynotsupported"));
        return deviceTopology;
    }

    private NoSecuritySupport getSecuritySupport() {
        if (securitySupport == null) {
            securitySupport = new NoSecuritySupport();
        }
        return securitySupport;
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {
        this.getWaveFlowProperties().addProperties(TypedProperties.copyOf(properties));
    }

    public int getNumberOfChannels() {
        return getParameterFactory().readOperatingMode().getNumberOfInputsUsed();
    }

    /**
     * Returns the configured device timezone
     */
    public TimeZone getTimeZone() {
        return offlineDevice.getTimeZone();
    }

    public int getProfileInterval() {
        return (int) Temporals.toSeconds(offlineDevice.getAllOfflineLoadProfiles().get(0).interval());
    }

    public CommonObisCodeMapper getCommonObisCodeMapper() {
        if (commonObisCodeMapper == null) {
            commonObisCodeMapper = new CommonObisCodeMapper(this, collectedDataFactory, issueFactory);
        }
        return commonObisCodeMapper;
    }

}