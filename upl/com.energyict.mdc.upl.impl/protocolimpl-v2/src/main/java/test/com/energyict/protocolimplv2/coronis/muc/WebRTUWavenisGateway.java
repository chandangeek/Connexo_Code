package test.com.energyict.protocolimplv2.coronis.muc;

import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisStack;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.channels.ip.socket.ServerWavenisGatewayComChannel;
import com.energyict.mdc.channels.ip.socket.WavenisGatewayComChannel;
import com.energyict.mdc.channels.ip.socket.WavenisGatewayConnectionType;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.comchannels.WavenisStackUtils;
import com.energyict.protocolimplv2.common.AbstractGateway;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.security.NoSecuritySupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Place holder protocol for the transparent MUC Wavenis gateway.
 * End nodes (Waveflow modules) are read out over this gateway
 * <p/>
 * Copyrights EnergyICT
 * Date: 29/05/13
 * Time: 17:02
 * Author: khe
 */
public class WebRTUWavenisGateway extends AbstractGateway {

    private WavenisStack wavenisStack;
    private OfflineDevice offlineDevice;
    private NoSecuritySupport securitySupport;
    private RegisterReader registerReader;
    private DeviceIdentifier deviceIdentifier;
    private Messaging messaging;
    private DeviceProtocolCache deviceCache;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        if (comChannel instanceof ServerWavenisGatewayComChannel) {
            this.wavenisStack = ((ServerWavenisGatewayComChannel) comChannel).getWavenisStack();
        } else {
            throw MdcManager.getComServerExceptionFactory().createUnexpectedComChannel(WavenisGatewayComChannel.class.getSimpleName(), comChannel.getClass().getSimpleName());
        }
        this.offlineDevice = offlineDevice;
    }

    public WavenisStack getWavenisStack() {
        return wavenisStack;
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT WebRTU MUC Wavenis Gateway";
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public void addProperties(TypedProperties properties) {
        //Do nothing
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> connectionTypes = new ArrayList<>();
        connectionTypes.add(new WavenisGatewayConnectionType());
        return connectionTypes;
    }

    @Override
    public Date getTime() {
        try {
            return WavenisStackUtils.readClock(wavenisStack);
        } catch (IOException e) {
            throw MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(e, 1);
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        try {
            WavenisStackUtils.syncClock(wavenisStack);
        } catch (IOException e) {
            throw MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(e, 1);
        }
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
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
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        List<DeviceProtocolDialect> dialects = new ArrayList<>();
        dialects.add(new NoParamsDeviceProtocolDialect());
        return dialects;
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        //Do nothing
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        //Do nothing
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return getMessaging().format(propertySpec, messageAttribute);
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

    private Messaging getMessaging() {
        if (messaging == null) {
            messaging = new Messaging(this);
        }
        return messaging;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        List<CollectedRegister> result = new ArrayList<>();
        for (OfflineRegister register : registers) {
            try {
                result.add(getRegisterReader().readRegister(register));
            } catch (IOException e) {
                throw MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(e, 1);
            }
        }
        return result;
    }

    private RegisterReader getRegisterReader() {
        if (registerReader == null) {
            registerReader = new RegisterReader(wavenisStack);
        }
        return registerReader;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        CollectedTopology collectedTopology = MdcManager.getCollectedDataFactory().createCollectedTopology(getDeviceIdentifier());
        collectedTopology.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning("Cannot read out the slave devices from the MUC Wavecell"));
        return collectedTopology;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        if (deviceIdentifier == null) {
            deviceIdentifier = new DeviceIdentifierById(offlineDevice.getId());
        }
        return deviceIdentifier;
    }

    private NoSecuritySupport getSecuritySupport() {
        if (securitySupport == null) {
            securitySupport = new NoSecuritySupport();
        }
        return securitySupport;
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
}