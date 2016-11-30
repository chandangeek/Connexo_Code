package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocolimplv2.messages.nls.Thesaurus;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum WavenisDeviceMessage implements DeviceMessageSpec {

    WavenisAddAddressGetNetworkId(0, DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisAddAddressWithNetworkId(1, DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisBranchMove(2, DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisChangeMasterAddress(3, DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisCompareRepaireDatabases(4),
    WavenisDeleteAddress(5, DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisInitBubbleUpSlotDatabase(6),
    WavenisInitDatabases(7),
    WavenisProgramRadioAddress(8, DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisRemoveBubbleUpSlot(9, DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisRequestBubbleUpSlot(10, DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisRequestModuleStatus(11, DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WaveCardRadioAddress(12),
    WavenisRestoreDatabasesUsingEIServerMasterdata(13),
    WavenisRestoreBubbleUpDatabase(14),
    WavenisRestoreLocalFromEIServer(15),
    WavenisRestoreRootDatabaseFromLocal(16),
    WavenisResynchronizeModule(17, DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisFreeRequestResponse(18) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.hexStringSpec(DeviceMessageConstants.rfCommand, DeviceMessageConstants.rfCommandDefaultTranslation));
        }
    },
    WavenisSetRunLevelIdle(19),
    WavenisSetRunLevelInit(20),
    WavenisSetRunLevelRun(21),
    WavenisSetFriendlyName(22, DeviceMessageConstants.friendlyName, DeviceMessageConstants.friendlyNameDefaultTranslation),
    WavenisSetL1PreferredList(23, DeviceMessageConstants.preferredL1NodeList, DeviceMessageConstants.preferredL1NodeListDefaultTranslation),
    WavenisSynchronizeModule(24, DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisUpdateEIServerMasterdataUsingLocalDatabases(25),
    WavenisEnableDisable(26, DeviceMessageConstants.enableWavenis, DeviceMessageConstants.enableWavenisDefaultTranslation);

    private final long id;
    private final String deviceMessageConstantKey;
    private final String deviceMessageConstantDefaultTranslation;

    WavenisDeviceMessage(int id) {
        this.id = id;
        this.deviceMessageConstantKey = null;
        this.deviceMessageConstantDefaultTranslation = null;
    }

    WavenisDeviceMessage(int id, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        this.id = id;
        this.deviceMessageConstantKey = deviceMessageConstantKey;
        this.deviceMessageConstantDefaultTranslation = deviceMessageConstantDefaultTranslation;
    }


    protected PropertySpec stringSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec hexStringSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .hexStringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return DeviceMessageCategories.WAVENIS_CONFIGURATION;
    }

    @Override
    public String getName() {
        return Services
                .nlsService()
                .getThesaurus(Thesaurus.ID.toString())
                .getFormat(this.getNameTranslationKey())
                .format();
    }

    @Override
    public String getNameResourceKey() {
        return WavenisDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    private TranslationKeyImpl getNameTranslationKey() {
        return new TranslationKeyImpl(this.getNameResourceKey(), "MR" + this.getNameResourceKey());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        if (this.deviceMessageConstantKey == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(this.stringSpec(this.deviceMessageConstantKey, this.deviceMessageConstantDefaultTranslation));
        }
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }

    @Override
    public long getMessageId() {
        return id;
    }

}