package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageSpecPrimaryKey;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum WavenisDeviceMessage implements DeviceMessageSpec {

    WavenisAddAddressGetNetworkId(0, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.rfAddress)),
    WavenisAddAddressWithNetworkId(1, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.rfAddress)),
    WavenisBranchMove(2, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.rfAddress)),
    WavenisChangeMasterAddress(3, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.rfAddress)),
    WavenisCompareRepaireDatabases(4),
    WavenisDeleteAddress(5, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.rfAddress)),
    WavenisInitBubbleUpSlotDatabase(6),
    WavenisInitDatabases(7),
    WavenisProgramRadioAddress(8, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.rfAddress)),
    WavenisRemoveBubbleUpSlot(9, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.rfAddress)),
    WavenisRequestBubbleUpSlot(10, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.rfAddress)),
    WavenisRequestModuleStatus(11, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.rfAddress)),
    WaveCardRadioAddress(12),
    WavenisRestoreDatabasesUsingEIServerMasterdata(13),
    WavenisRestoreBubbleUpDatabase(14),
    WavenisRestoreLocalFromEIServer(15),
    WavenisRestoreRootDatabaseFromLocal(16),
    WavenisResynchronizeModule(17, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.rfAddress)),
    WavenisFreeRequestResponse(18, PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.rfCommand)),
    WavenisSetRunLevelIdle(19),
    WavenisSetRunLevelInit(20),
    WavenisSetRunLevelRun(21),
    WavenisSetFriendlyName(22, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.friendlyName)),
    WavenisSetL1PreferredList(23, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.preferredL1NodeList)),
    WavenisSynchronizeModule(24, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.rfAddress)),
    WavenisUpdateEIServerMasterdataUsingLocalDatabases(25),
    WavenisEnableDisable(26, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.enableWavenis));

    private static final DeviceMessageCategory category = DeviceMessageCategories.WAVENIS_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private WavenisDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
    }

    @Override
    public String getName() {
        return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return WavenisDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.deviceMessagePropertySpecs;
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
    public int getMessageId() {
        return id;
    }
}