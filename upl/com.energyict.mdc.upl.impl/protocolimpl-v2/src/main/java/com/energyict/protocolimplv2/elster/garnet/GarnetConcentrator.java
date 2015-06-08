package com.energyict.protocolimplv2.elster.garnet;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.garnet.common.TopologyMaintainer;
import com.energyict.protocolimplv2.elster.garnet.exception.GarnetException;
import com.energyict.protocolimplv2.elster.garnet.structure.ConcentratorVersionResponseStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author sva
 * @since 23/05/2014 - 9:12
 */
public class GarnetConcentrator implements DeviceProtocol {

    private OfflineDevice offlineDevice;
    private TopologyMaintainer topologyMaintainer;
    private RequestFactory requestFactory;

    private SecuritySupport securitySupport;
    private RegisterFactory registerFactory;
    private LogBookFactory logBookFactory;
    private ConcentratorMessaging messaging;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        setDevice(offlineDevice);
        getRequestFactory().setComChannel(comChannel);
    }

    @Override
    public void terminate() {
        //Nothing to do here...
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return getProperties().getRequiredProperties();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return getProperties().getOptionalProperties();
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundTcpIpConnectionType());
        result.add(new SioSerialConnectionType());
        result.add(new RxTxSerialConnectionType());
        return result;
    }

    @Override
    public void logOn() {
        try {
            getRequestFactory().openSession();
        } catch (GarnetException e) {
            throw MdcManager.getComServerExceptionFactory().createProtocolConnectFailed(e);
        }
    }

    @Override
    public void daisyChainedLogOn() {
        logOn();
    }

    @Override
    public void logOff() {
        // No logoff required
    }

    @Override
    public void daisyChainedLogOff() {
        logOff();
    }

    @Override
    public String getSerialNumber() {
        try {
            ConcentratorVersionResponseStructure concentratorVersion = getRequestFactory().readConcentratorVersion();
            return ProtocolTools.removeLeadingZerosFromString(concentratorVersion.getSerialNumber().getSerialNumber());
        } catch (GarnetException e) {
            throw MdcManager.getComServerExceptionFactory().createUnexpectedResponse(e);
        }
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        List<CollectedLoadProfileConfiguration> collectedLoadProfileConfigurations = new ArrayList<>(loadProfilesToRead.size());
        for (LoadProfileReader loadProfileReader : loadProfilesToRead) {

            CollectedLoadProfileConfiguration configuration = MdcManager.getCollectedDataFactory().createCollectedLoadProfileConfiguration(
                    loadProfileReader.getProfileObisCode(),
                    loadProfileReader.getMeterSerialNumber()
            );
            configuration.setSupportedByMeter(false);
            configuration.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addProblem(loadProfileReader.getProfileObisCode(), "loadProfileXnotsupported", loadProfileReader.getProfileObisCode()));
            collectedLoadProfileConfigurations.add(configuration);
        }
        return collectedLoadProfileConfigurations;
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        throw MdcManager.getComServerExceptionFactory().createUnsupportedMethodException(this.getClass(), "getLoadProfileData");
    }

    @Override
    public Date getTime() {
        return new Date();
    }

    @Override
    public void setTime(Date timeToSet) {
        // Garnet protocol has no formal time set method - time is automatically synced each communication session
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return null;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        // DeviceProtocolCache not used for Garnet protocol
    }

    @Override
    public String getProtocolDescription() {
        return "Elster Concentrator Garnet";
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getLogBookFactory().getLogBookData(logBooks);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getMessaging().getSupportedMessages();
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
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return getMessaging().format(propertySpec, messageAttribute);
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new TcpDeviceProtocolDialect(), new SerialDeviceProtocolDialect());
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        getRequestFactory().getProperties().addProperties(dialectProperties);

    }

    @Override
    public void addProperties(TypedProperties properties) {
        getRequestFactory().getProperties().addProperties(properties);
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        getRequestFactory().getProperties().addProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
        getRequestFactory().getProperties().setSecurityPropertySet(deviceProtocolSecurityPropertySet);
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

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return getTopologyMaintainer().getDeviceTopology();
    }

    /**
     * The version date
     */
    @Override
    public String getVersion() {
        return "$Date$";
    }

    public GarnetProperties getProperties() {
        return getRequestFactory().getProperties();
    }

    public RequestFactory getRequestFactory() {
        if (requestFactory == null) {
            this.requestFactory = new RequestFactory();
        }
        return requestFactory;
    }

    public RegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            this.registerFactory = new RegisterFactory(this);
        }
        return registerFactory;
    }

    public LogBookFactory getLogBookFactory() {
        if (this.logBookFactory == null) {
            this.logBookFactory = new LogBookFactory(this);
        }
        return logBookFactory;
    }

    public ConcentratorMessaging getMessaging() {
        if (this.messaging == null) {
            this.messaging = new ConcentratorMessaging(this);
        }
        return messaging;
    }

    public SecuritySupport getSecuritySupport() {
        if (securitySupport == null) {
            this.securitySupport = new SecuritySupport();
        }
        return securitySupport;
    }

    public TopologyMaintainer getTopologyMaintainer() {
        if (topologyMaintainer == null) {
            this.topologyMaintainer = new TopologyMaintainer(this);
        }
        return topologyMaintainer;
    }

    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }

    private void setDevice(OfflineDevice offlineDevice) {
        this.offlineDevice = offlineDevice;
    }
}