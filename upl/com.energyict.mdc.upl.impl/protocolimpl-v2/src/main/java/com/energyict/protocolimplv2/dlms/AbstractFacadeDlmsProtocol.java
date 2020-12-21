package com.energyict.protocolimplv2.dlms;

import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedLoadProfileReader;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedLogBookReader;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedRegisterReader;

import java.util.List;

/**
 * This class should be used just to delegate towards specialized implementations making it possible to write specialised classes for certain domains of interest.
 */
public abstract class AbstractFacadeDlmsProtocol extends AbstractDlmsProtocol {

    protected final DeviceInformation deviceInformation;
    protected DeviceMessageSupport messageSupport;

    protected final CollectedRegisterReader registryReader;
    protected final CollectedLogBookReader logBookReader;
    protected final CollectedLoadProfileReader loadProfileReader;

    public AbstractFacadeDlmsProtocol(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, DeviceInformation deviceInformation, DlmsSessionProperties dlmsSessionProperties) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        super.dlmsProperties = dlmsSessionProperties;
        this.deviceInformation = deviceInformation;
        this.messageSupport = getDeviceMessageSupport();
        this.registryReader = getRegistryReader();
        this.logBookReader = getLogBookReader();
        this.loadProfileReader = getLoadProfileReader();
    }

    protected abstract CollectedLogBookReader getLogBookReader();

    protected abstract CollectedRegisterReader getRegistryReader();

    protected abstract CollectedLoadProfileReader getLoadProfileReader();

    /**
     * Initialize of message domain methods class handler. It would have been better to ask it at construct time yet this is
     * not straight forwards (due to classes design) since most of such implementations need the actual protocol (who extends current class) implementation
     * therefore not available at constructor time.
     */
    public abstract DeviceMessageSupport getDeviceMessageSupport();

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return this.messageSupport.executePendingMessages(pendingMessages);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return this.messageSupport.getSupportedMessages();
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return this.messageSupport.updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return this.messageSupport.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return this.deviceInformation.getFunction();
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return this.deviceInformation.getManufacturer();
    }

    @Override
    public String getVersion() {
        return this.deviceInformation.getVersion();
    }

    @Override
    public String getProtocolDescription() {
        return this.deviceInformation.getProtocolDescription();
    }

    @Override
    public DlmsSessionProperties getDlmsSessionProperties() {
        return this.dlmsProperties;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return registryReader.readRegisters(registers);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return logBookReader.getLogBookData(logBooks);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return loadProfileReader.getLoadProfileData(loadProfiles);
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return loadProfileReader.fetchLoadProfileConfiguration(loadProfilesToRead);
    }

}
