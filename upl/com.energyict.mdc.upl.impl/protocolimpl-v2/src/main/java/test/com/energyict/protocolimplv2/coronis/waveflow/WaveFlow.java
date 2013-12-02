package test.com.energyict.protocolimplv2.coronis.waveflow;

import com.energyict.concentrator.communication.driver.rf.eictwavenis.WaveModuleLinkAdaptor;
import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisStack;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.channels.ip.socket.WavenisGatewayComChannel;
import com.energyict.mdc.channels.ip.socket.WavenisGatewayConnectionType;
import com.energyict.mdc.channels.serial.rf.WavenisSerialConnectionType;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.*;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.security.*;
import com.energyict.mdc.protocol.tasks.support.*;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.*;
import com.energyict.protocol.*;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.comchannels.WavenisStackUtils;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.security.NoSecuritySupport;
import test.com.energyict.protocolimplv2.coronis.common.WaveFlowConnect;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.CommonObisCodeMapper;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter.ParameterFactory;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter.PulseWeight;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.RadioCommandFactory;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 7/06/13
 * Time: 10:59
 * Author: khe
 */
public abstract class WaveFlow implements DeviceProtocol {

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
        if (comChannel.getClass().isAssignableFrom(WavenisGatewayComChannel.class)) {   //Create a link
            WavenisStack wavenisStack = ((WavenisGatewayComChannel) comChannel).getWavenisStack();
            WaveModuleLinkAdaptor waveModuleLinkAdaptor = WavenisStackUtils.createLink(getWaveFlowProperties().getRFAddress(), wavenisStack);
            SynchroneousComChannel synchroneousComChannel = new SynchroneousComChannel(waveModuleLinkAdaptor.getInputStream(), waveModuleLinkAdaptor.getOutputStream());
            waveFlowConnect = new WaveFlowConnect(synchroneousComChannel, getWaveFlowProperties().getTimeout(), getWaveFlowProperties().getRetries());
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
            waveFlowProperties = new WaveFlowProperties(TypedProperties.empty());
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
    public List<PropertySpec> getRequiredProperties() {
        return getWaveFlowProperties().getRequiredProperties();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return getWaveFlowProperties().getOptionalProperties();
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> connectionTypes = new ArrayList<>();
        connectionTypes.add(new WavenisGatewayConnectionType());
        connectionTypes.add(new WavenisSerialConnectionType());
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
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getSerialNumber");
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        this.deviceCache = deviceProtocolCache;
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return deviceCache;
    }

    @Override
    public void setTime(Date timeToSet) {
        getParameterFactory().writeTimeDateRTC(timeToSet);
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
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return getMessaging().format(propertySpec, messageAttribute);
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
        dialects.add(new NoParamsDeviceProtocolDialect());
        return dialects;
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        getWaveFlowProperties().addProperties(dialectProperties);
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
    public String getSecurityRelationTypeName() {
        return getSecuritySupport().getSecurityRelationTypeName();
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
    public PropertySpec getSecurityPropertySpec(String name) {
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
        CollectedTopology deviceTopology = MdcManager.getCollectedDataFactory().createCollectedTopology(getDeviceIdentifier());
        deviceTopology.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(offlineDevice, "devicetopologynotsupported"));
        return deviceTopology;
    }

    private NoSecuritySupport getSecuritySupport() {
        if (securitySupport == null) {
            securitySupport = new NoSecuritySupport();
        }
        return securitySupport;
    }

    @Override
    public void addProperties(TypedProperties properties) {
        getWaveFlowProperties().addProperties(properties);
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
        return offlineDevice.getAllOfflineLoadProfiles().get(0).getInterval().getSeconds();
    }

    public CommonObisCodeMapper getCommonObisCodeMapper() {
        if (commonObisCodeMapper == null) {
            commonObisCodeMapper = new CommonObisCodeMapper(this);
        }
        return commonObisCodeMapper;
    }
}