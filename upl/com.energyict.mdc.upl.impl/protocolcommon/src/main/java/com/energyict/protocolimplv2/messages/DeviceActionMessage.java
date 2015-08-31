package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Provides a summary of all messages related to general Device Actions
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 11:59
 */
public enum DeviceActionMessage implements DeviceMessageSpec {

    BILLING_RESET(0),
    BILLING_RESET_CONTRACT_1(1),
    BILLING_RESET_CONTRACT_2(2),
    SET_PASSIVE_EOB_DATETIME(3,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.contractAttributeName, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.year),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.month),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.day),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.dayOfWeek),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.hour),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.minute),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.second)
    ),
    GLOBAL_METER_RESET(4),
    DEMAND_RESET(5),
    POWER_OUTAGE_RESET(6),
    POWER_QUALITY_RESET(7),
    ERROR_STATUS_RESET(8),
    REGISTERS_RESET(9),
    LOAD_LOG_RESET(10),
    EVENT_LOG_RESET(11),
    ALARM_REGISTER_RESET(12),
    ERROR_REGISTER_RESET(13),
    REBOOT_DEVICE(14),
    DISABLE_WEBSERVER(15),
    ENABLE_WEBSERVER(16),
    RESTORE_FACTORY_SETTINGS(17),
    SetFTIONReboot(18, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.FTIONReboot)),
    SetFTIONInitialize(19, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.FTIONInitialize)),
    SetFTIONMailLog(20, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.FTIONMailLog)),
    SetFTIONSaveConfig(21, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.FTIONSaveConfig)),
    SetFTIONUpgrade(22, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.FTIONUpgrade)),
    SetFTIONClearMem(23, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.FTIONClearMem)),
    SetFTIONMailConfig(24, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.FTIONMailConfig)),
    SetFTIONModemReset(25, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.FTIONModemReset)),
    SetChangeAdminPassword(26,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.AdminOld),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.AdminNew)
    ),
    SetAnalogOut(27,
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, getPossibleValues()),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.AnalogOutValue)
    ),

    FTIONUpgrade(28),
    RtuPlusServerEnterMaintenanceMode(29),
    RtuPlusServerExitMaintenanceMode(30),
    ForceMessageToFailed(31,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.deviceId),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.trackingId)),
    FTIONUpgradeAndInit(32),
    FTIONUpgradeAndInitWithNewEIServerURL(33, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.servletURL)),
    FTIONUpgradeWithNewEIServerURL(34, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.servletURL)),
    FTIONInitDatabaseKeepConfig(35),
    FTIONReboot(36),
    FTIONRestart(37),
    FTIONScanBus(38),
    SyncMasterdata(39),
    RebootApplication(40),
    DemandResetWithForceClock(41),
    HardResetDevice(42),
    SyncMasterdataForDC(43, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.dcDeviceIDAttributeName)),
    PauseDCScheduler(44),
    ResumeDCScheduler(45),
    SyncDeviceDataForDC(46, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.dcDeviceID2AttributeName)),
    SyncOneConfigurationForDC(47, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.deviceConfigurationIDAttributeName));

    private static final DeviceMessageCategory category = DeviceMessageCategories.DEVICE_ACTIONS;
    private final int id;
    private final List<PropertySpec> deviceMessagePropertySpecs;

    private DeviceActionMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    private static BigDecimal[] getPossibleValues() {
        BigDecimal[] result = new BigDecimal[16];
        for (int index = 17; index <= 32; index++) {
            result[index - 17] = BigDecimal.valueOf(index);
        }
        return result;
    }

    private static String translate(final String key) {
        return UserEnvironment.getDefault().getTranslation(key);
    }

    public int getMessageId() {
        return id;
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
    }

    @Override
    public String getName() {
        return translate(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return DeviceActionMessage.class.getSimpleName() + "." + this.toString();
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
}
