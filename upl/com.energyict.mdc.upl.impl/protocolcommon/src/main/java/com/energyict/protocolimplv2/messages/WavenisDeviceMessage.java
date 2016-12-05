package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum WavenisDeviceMessage implements DeviceMessageSpecSupplier {

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
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.hexStringSpec(service, DeviceMessageConstants.rfCommand, DeviceMessageConstants.rfCommandDefaultTranslation));
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

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec hexStringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .hexStringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    private String getNameResourceKey() {
        return WavenisDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    protected List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        if (this.deviceMessageConstantKey == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(
                    this.stringSpec(propertySpecService, this.deviceMessageConstantKey, this.deviceMessageConstantDefaultTranslation));
        }
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                this.id,
                new EnumBasedDeviceMessageSpecPrimaryKey(this, name()),
                new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.WAVENIS_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService);
    }

}