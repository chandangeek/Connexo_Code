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

    WavenisAddAddressGetNetworkId(38001, "Add Wavenis module", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisAddAddressWithNetworkId(38002, "Add Wavenis module", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisBranchMove(38003, "Branch move a slave to a master", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisChangeMasterAddress(38004, "Change master address", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisCompareRepaireDatabases(38005, "Compare the Wavenis root database with the RTU local database, and repair if necessary"),
    WavenisDeleteAddress(38005, "Delete Wavenis module", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisInitBubbleUpSlotDatabase(38007, "Initialize the bubble up slot database"),
    WavenisInitDatabases(38008, "Initialize the Wavenis root database and RTU local database"),
    WavenisProgramRadioAddress(38009, "Program the Wavecard radio address", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisRemoveBubbleUpSlot(38010, "Remove a bubble up slot", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisRequestBubbleUpSlot(38011, "Request a bubble up slot", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisRequestModuleStatus(38012, "Request the state of a Wavenis module", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WaveCardRadioAddress(38013, "Request the Wavecard spec"),
    WavenisRestoreDatabasesUsingEIServerMasterdata(38014, "Restore local (route & bubble up) and root database using EIServer master data"),
    WavenisRestoreBubbleUpDatabase(38015, "Restore bubble up database"),
    WavenisRestoreLocalFromEIServer(38016, "Restore the RTU+Server local route database using the EIServer master data"),
    WavenisRestoreRootDatabaseFromLocal(38017, "Restore the Wavecard root database using the local route database"),
    WavenisResynchronizeModule(38018, "Resynchronize Wavenis module", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisFreeRequestResponse(38019, "Send a Wavenis RF request") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.hexStringSpec(service, DeviceMessageConstants.rfCommand, DeviceMessageConstants.rfCommandDefaultTranslation));
        }
    },
    WavenisSetRunLevelIdle(38020, "Set the run level to idle"),
    WavenisSetRunLevelInit(38021, "Set the run level to init"),
    WavenisSetRunLevelRun(38022, "Set the run level to run"),
    WavenisSetFriendlyName(38023, "Set friendly name", DeviceMessageConstants.friendlyName, DeviceMessageConstants.friendlyNameDefaultTranslation),
    WavenisSetL1PreferredList(38024, "Set the L1 preferred list", DeviceMessageConstants.preferredL1NodeList, DeviceMessageConstants.preferredL1NodeListDefaultTranslation),
    WavenisSynchronizeModule(38025, "Synchronize a Wavenis module on the network", DeviceMessageConstants.rfAddress, DeviceMessageConstants.rfAddressDefaultTranslation),
    WavenisUpdateEIServerMasterdataUsingLocalDatabases(38026, "Update EIServer master data using the local (route & bubble up) databases"),
    WavenisEnableDisable(38027, "Enable or disable Wavenis", DeviceMessageConstants.enableWavenis, DeviceMessageConstants.enableWavenisDefaultTranslation);

    private final long id;
    private final String deviceMessageConstantKey;
    private final String deviceMessageConstantDefaultTranslation;
    private final String defaultNameTranslation;

    WavenisDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
        this.deviceMessageConstantKey = null;
        this.deviceMessageConstantDefaultTranslation = null;
    }

    WavenisDeviceMessage(long id, String defaultNameTranslation, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
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
                .markRequired()
                .finish();
    }

    protected PropertySpec hexStringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .hexStringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
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
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.WAVENIS_CONFIGURATION,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

}