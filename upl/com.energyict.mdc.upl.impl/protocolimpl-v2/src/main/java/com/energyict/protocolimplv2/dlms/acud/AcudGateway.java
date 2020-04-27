package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.cim.EndDeviceType;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.meterdata.*;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;

import java.util.Arrays;
import java.util.List;

public class AcudGateway extends Acud {

    private static final EndDeviceType typeMeter = EndDeviceType.GATEWAY;


    public AcudGateway(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, nlsService, converter, messageFileExtractor);
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

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return null;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
                FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE.get(getPropertySpecService(), getNlsService(), getConverter()));
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
        return "$Date: 2020-03-31 13:26:25 +0200 (Tue, 31 Mar 2020) $";
    }
}