package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.cim.EndDeviceType;
import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundWebServiceConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AcudGateway extends Acud {

    private static final EndDeviceType typeMeter = EndDeviceType.GATEWAY;
    private String hostAddress;
    private int portNumber;

    public AcudGateway(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, nlsService, converter, calendarExtractor, messageFileExtractor);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        this.hostAddress = comChannel.getProperties().getTypedProperty("host");
        this.portNumber = comChannel.getProperties().getTypedProperty("portNumber", BigDecimal.valueOf(80)).intValue();
    }

    @Override
    public void logOn() {
        // nothing to de here
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.singletonList(new NoParamsDeviceProtocolDialect(getNlsService())
        );
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return null;
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return null;
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return null;
    }

    protected AcudLogBookFactory createLogBookFactory() {
        return null;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
                FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER.get(getPropertySpecService(), getNlsService(), getConverter()),
                ConfigurationChangeDeviceMessage.WRITE_CONFIGURATION_TEXT.get(getPropertySpecService(), getNlsService(), getConverter()));
    }

    public EndDeviceType getTypeMeter() {
        return typeMeter;
    }

    @Override
    public String getProtocolDescription() {
        return "ACUD Gateway";
    }

    @Override
    public String getVersion() {
        return "$Date: 2023-02-23 $";
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList(
                new SioOpticalConnectionType(getPropertySpecService()),
                new RxTxOpticalConnectionType(getPropertySpecService()),
                new OutboundTcpIpConnectionType(getPropertySpecService()),
                new OutboundWebServiceConnectionType(getPropertySpecService()),
                new InboundIpConnectionType());
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public int getPortNumber() {
        return portNumber;
    }
}