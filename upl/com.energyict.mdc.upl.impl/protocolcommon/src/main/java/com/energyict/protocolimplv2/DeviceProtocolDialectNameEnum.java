package com.energyict.protocolimplv2;

/**
 * Specifies the possible DeviceProtocolDialect names.
 *
 * <p>
 * Each name should be unique, as it is used in RelationTypes! <BR>
 * The length of the name is limited to 24 characters!
 * </p>
 *
 * @author: sva
 * @since: 17/10/12 (10:23)
 */
public enum DeviceProtocolDialectNameEnum {

    CTR_DEVICE_PROTOCOL_DIALECT_NAME("CtrDialect"),
    DSMR23_DEVICE_PROTOCOL_DIALECT_NAME("Dsmr23Dialect"),
    SDK_SAMPLE_STANDARD_DEVICE_PROTOCOL_DIALECT_NAME("SDKStandardDialect"),
    SDK_SAMPLE_LOAD_PROFILE_DEVICE_PROTOCOL_DIALECT_NAME("SDKLoadProfileDialect"),
    SDK_SAMPLE_TIME_DEVICE_PROTOCOL_DIALECT_NAME("SDKTimeDialect"),
    SDK_SAMPLE_TOPOLOGY_DIALECT_NAME("SDKTopologyDialect"),
    ACE4000_DEVICE_PROTOCOL_DIALECT_NAME("ACE4000GprsDialect")
    ;

    DeviceProtocolDialectNameEnum(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public String getName() {
        return uniqueName;
    }

    private String uniqueName;
}
