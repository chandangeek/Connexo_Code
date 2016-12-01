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

    WavenisAddAddressGetNetworkId(0, "Add Wavenis module", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisAddAddressWithNetworkId(1, "Add Wavenis module", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisBranchMove(2, "Branch move a slave to a master", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisChangeMasterAddress(3, "Change master address", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisCompareRepaireDatabases(4, "Compare the Wavenis root database with the RTU local database, and repair if necessary"),
    WavenisDeleteAddress(5, "Delete Wavenis module", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisInitBubbleUpSlotDatabase(6, "Initialize the bubble up slot database"),
    WavenisInitDatabases(7, "Initialize the Wavenis root database and RTU local database"),
    WavenisProgramRadioAddress(8, "Program the Wavecard radio address", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisRemoveBubbleUpSlot(9, "Remove a bubble up slot", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisRequestBubbleUpSlot(10, "Request a bubble up slot", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisRequestModuleStatus(11, "Request the state of a Wavenis module", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WaveCardRadioAddress(12, "Request the Wavecard spec"),
    WavenisRestoreDatabasesUsingEIServerMasterdata(13, "Restore local (route & bubble up) and root database using EIServer master data"),
    WavenisRestoreBubbleUpDatabase(14, "Restore bubble up database"),
    WavenisRestoreLocalFromEIServer(15, "Restore the RTU+Server local route database using the EIServer master data"),
    WavenisRestoreRootDatabaseFromLocal(16, "Restore the Wavecard root database using the local route database"),
    WavenisResynchronizeModule(17, "Resynchronize Wavenis module", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisFreeRequestResponse(18, "Send a Wavenis RF request") {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.hexStringSpec(DeviceMessageConstants.rfCommand, DeviceMessageConstants.rfCommandDefaultTranslation));
        }
    },
    WavenisSetRunLevelIdle(19, "Set the run level to idle"),
    WavenisSetRunLevelInit(20, "Set the run level to init"),
    WavenisSetRunLevelRun(21, "Set the run level to run"),
    WavenisSetFriendlyName(22, "Set friendly name", DeviceMessageConstants.friendlyName, DeviceMessageConstants.friendlyNameDefaultTranslation),
    WavenisSetL1PreferredList(23, "Set the L1 preferred list", DeviceMessageConstants.preferredL1NodeList, DeviceMessageConstants.preferredL1NodeListDefaultTranslation),
    WavenisSynchronizeModule(24, "Synchronize a Wavenis module on the network", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisUpdateEIServerMasterdataUsingLocalDatabases(25, "Update EIServer master data using the local (route & bubble up) databases"),
    WavenisEnableDisable(26, "Enable or disable Wavenis", DeviceMessageConstants.enableWavenis, DeviceMessageConstants.enableWavenisDefaultTranslation);

    private final long id;
    private final String deviceMessageConstantKey;
    private final String deviceMessageConstantDefaultTranslation;
    private final String defaultNameTranslation;

    WavenisDeviceMessage(int id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
        this.deviceMessageConstantKey = null;
        this.deviceMessageConstantDefaultTranslation = null;
    }

    WavenisDeviceMessage(int id, String defaultNameTranslation, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
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

    private String getNameResourceKey() {
        return WavenisDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public TranslationKeyImpl getNameTranslationKey() {
        return new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation);
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