package test.com.energyict.protocolimplv2.coronis.muc;

import com.energyict.mdc.channels.ip.socket.ServerWavenisGatewayComChannel;
import com.energyict.mdc.channels.ip.socket.WavenisGatewayComChannel;
import com.energyict.mdc.channels.ip.socket.WavenisGatewayConnectionType;
import com.energyict.mdc.io.ConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisStack;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
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
import java.util.Optional;

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
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;

    public WebRTUWavenisGateway(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        if (comChannel instanceof ServerWavenisGatewayComChannel) {
            this.wavenisStack = ((ServerWavenisGatewayComChannel) comChannel).getWavenisStack();
        } else {
            throw DeviceConfigurationException.unexpectedComChannel(WavenisGatewayComChannel.class.getSimpleName(), comChannel.getClass().getSimpleName());
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
        return "$Date: 2015-11-13 15:14:02 +0100 (Fri, 13 Nov 2015) $";
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        //Do nothing
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> connectionTypes = new ArrayList<>();
        connectionTypes.add(new WavenisGatewayConnectionType(this.propertySpecService));
        return connectionTypes;
    }

    @Override
    public Date getTime() {
        try {
            return WavenisStackUtils.readClock(wavenisStack);
        } catch (IOException e) {
            throw ConnectionCommunicationException.numberOfRetriesReached(e, 1);
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        try {
            WavenisStackUtils.syncClock(wavenisStack);
        } catch (IOException e) {
            throw ConnectionCommunicationException.numberOfRetriesReached(e, 1);
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
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
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getMessaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
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
            messaging = new Messaging(this, collectedDataFactory, issueFactory, this.propertySpecService, this.nlsService, this.converter);
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
                throw ConnectionCommunicationException.numberOfRetriesReached(e, 1);
            }
        }
        return result;
    }

    private RegisterReader getRegisterReader() {
        if (registerReader == null) {
            registerReader = new RegisterReader(wavenisStack, collectedDataFactory, issueFactory);
        }
        return registerReader;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        CollectedTopology collectedTopology = this.collectedDataFactory.createCollectedTopology(getDeviceIdentifier());
        collectedTopology.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning("Cannot read out the slave devices from the MUC Wavecell"));
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

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.NONE;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return null;
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return null;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        return null;
    }
}