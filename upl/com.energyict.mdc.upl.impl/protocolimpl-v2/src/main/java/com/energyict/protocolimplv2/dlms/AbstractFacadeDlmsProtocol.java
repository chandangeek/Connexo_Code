package com.energyict.protocolimplv2.dlms;

import com.energyict.dlms.DLMSCache;
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
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.dlms.common.dlms.PublicClientDlmsSessionProvider;
import com.energyict.protocolimplv2.dlms.common.framecounter.FrameCounter;
import com.energyict.protocolimplv2.dlms.common.framecounter.FrameCounterBuilder;
import com.energyict.protocolimplv2.dlms.common.framecounter.FrameCounterCacheBuilder;
import com.energyict.protocolimplv2.dlms.common.framecounter.FrameCounterHandler;
import com.energyict.protocolimplv2.dlms.common.properties.DlmsPropertiesFrameCounterSupport;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedLoadProfileReader;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedLogBookReader;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedRegisterReader;

import java.util.List;

/**
 * This class should be used just to delegate towards specialized implementations making it possible to write specialised classes for certain domains of interest.
 */
public abstract class AbstractFacadeDlmsProtocol<T extends DLMSCache> extends AbstractDlmsProtocol {

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

    public void handleFrameCounter(PublicClientDlmsSessionProvider publicClientDlmsSessionProvider) {
        DlmsPropertiesFrameCounterSupport dlmsSessionProperties = getDlmsSessionProperties();
        FrameCounter frameCounter = FrameCounterBuilder.build(dlmsSessionProperties.getAuthenticationSecurityLevel(),
                new FrameCounterCacheBuilder(getDeviceCache(), dlmsSessionProperties.useCachedFrameCounter()),
                publicClientDlmsSessionProvider, dlmsSessionProperties.frameCounterObisCode(), dlmsSessionProperties);

        new FrameCounterHandler(frameCounter.get()).handle(dlmsSessionProperties);
    }

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
    public abstract DlmsPropertiesFrameCounterSupport getDlmsSessionProperties();

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
        return loadProfileReader.readData(loadProfiles);
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return loadProfileReader.readConfiguration(loadProfilesToRead);
    }

    /**
     * This is here only because of: https://acsjira.honeywell.com/browse/COMMUNICATION-3602
     * Needed only for creating proper instance of cache (not DLMSCache)
     */
    protected void checkCacheObjects() {
        int configNumber = -1;
        boolean changed = false;
        try {
            if (super.dlmsCache != null && super.dlmsCache.getObjectList() != null) { // the dlmsCache exists
                getDlmsSession().getMeterConfig().setInstantiatedObjectList(super.dlmsCache.getObjectList());

                journal("Checking the configuration parameters.");
                configNumber = getMeterInfo().getConfigurationChanges();

                if (super.dlmsCache.getConfProgChange() != configNumber) {
                    journal("Meter configuration has changed, configuration is forced to be read.");
                    readObjectList();
                    changed = true;
                }

            } else { // cache does not exist
                super.dlmsCache = buildNewDLMSCache();
                journal("Cache does not exist, configuration is forced to be read.");
                readObjectList();
                configNumber = getMeterInfo().getConfigurationChanges();
                changed = true;
            }
        } finally {
            if (changed) {
                super.dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
                super.dlmsCache.setConfProgChange(configNumber);
            }
        }
    }

    protected abstract T buildNewDLMSCache();

}
