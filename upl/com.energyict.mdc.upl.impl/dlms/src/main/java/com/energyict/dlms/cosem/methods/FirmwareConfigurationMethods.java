package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Firmware configuration IC, methods
 * class id = 20024, version = 0, logical name = 0-128:96.130.0.255 (0080608200FF)
 * This manufacturer-defined COSEM IC allows for changing the active firmware configuration.
 * This includes resetting the data partition.
 */
public enum FirmwareConfigurationMethods implements DLMSClassMethods {

    /**
     * Initiates a factory reset. Invoking this method results in a system reboot. All customer data
     * (cached meter data and custom device configuration) are wiped by this action.
     * request_data ::= integer(0)
     * response_data ::= <empty>
     */
    RESET_TO_FACTORY_SETTINGS(1, 0x01),

    /**
     * Overwrites the factory defaults with the current firmware configuration. This effectively means
     * all network configuration, security configuration and application configuration defaults are
     * overwritten with the configuration set by the user. Invoking this method without proper validation
     * first can lead to unrecoverable damage.
     * request_data ::= integer(0)
     * response_data ::= <empty>
     */
    PERSIST_CURRENT_CONFIGURATION(2, 0x02),

    /**
     * Exports the list of system properties currently configured on the device. The system properties
     * allow modifying the runtime behaviour of the device, including its security settings. During
     * normal device operation, this method should not be required. We recommend the customer
     * to disable access to this method if not used.
     * request_data ::= integer(0)
     * response_data ::= array of system_property
     * system_property ::= structure
     * {
     *   name: utf8-string, -- property name
     *   value: utf8-string -- property value
     * }
     */
    EXPORT_PROPERTIES(3, 0x03),

    /**
     * Imports a new set of system properties into the device configuration. The system properties
     * allow modifying the runtime behaviour of the device, including its security settings. During
     * normal device operation, this method should not be required. Direct changes to the system
     * properties is not recommended: entering invalid system properties can prevent firmware components
     * from loading and operating correctly, which may require a factory reset to recover.
     * The properties take effect when the affected firmware component is loaded. Depending on
     * the property, this may require a reboot. We recommend the customer to disable access to this method if not used.
     * request_data ::= array of system_property
     * response_data ::= <empty>
     */
    IMPORT_PROPERTIES(4, 0x04),

    /**
     * Replaces all system properties currently configured with a given set of properties. The system
     * properties allow modifying the runtime behaviour of the device, including its security settings.
     * During normal device operation, this method should not be required. Direct changes
     * to the system properties is not recommended: entering invalid system properties can prevent
     * firmware components from loading and operating correctly, which may require a factory reset
     * to recover. The properties take effect when the affected firmware component is loaded. Depending
     * on the property, this may require a reboot. We recommend the customer to disable
     * access to this method if not used.
     * request_data ::= array of system_property
     * response_data ::= <empty>
     */
    REPLACE_PROPERTIES(5, 0x05),

    /**
     * Exports the full set of firmware customization applied to this device to a single tarball. This
     * includes imported certificates, system properties and external daemon configuration files. The
     * returned octet-string contains a tarball containing all system configuration applicable to the
     * device. During normal device operation, this method should not be required. We recommend
     * the customer to disable access to this method if not used.
     * request_data ::= integer(0)
     * response_data ::= octet-string
     */
    EXPORT_SYSTEM_CONFIGURATION(6, 0x06),

    /**
     * Imports a new set of configuration files from a given tarball. This method can be used to apply
     * a custom configuration on a deployed device. The given octet-string contains a valid GNU/-
     * POSIX compatible tar archive with gzip compression applied, and a valid signature appended.
     * Files contain their correct destination, groupid, userid and permissions. Depending on the imported
     * configuration files, a reboot may be required. Since importing configuration files allows
     * for altering the runtime behaviour of the device including its security configuration, access
     * to this method is restricted. Importing custom files directly can very easily lead to insecure
     * or inconsistent device configuration, and therefore, the use of this method should certainly
     * be limited. During normal device operation, use of this method should not be required; We
     * recommend the customer to disable access to this method if not used.
     * request_data ::= octet-string
     * response_data ::= <empty>
     */
    IMPORT_SYSTEM_CONFIGURATION(7, 0x07);


    /** The method number. */
    private final int methodNumber;

    /** The short address. */
    private final int shortAddress;

    /**
     * Create a new instance.
     *
     * @param 	methodNumber		The method number.
     * @param 	shortAddress		The short address.
     */
    private FirmwareConfigurationMethods(final int methodNumber, final int shortAddress) {
        this.methodNumber = methodNumber;
        this.shortAddress = shortAddress;
    }

    /**
     * {@inheritDoc}
     */
    public final DLMSClassId getDlmsClassId() {
        return DLMSClassId.FIRMWARE_CONFIGURATION_IC;
    }

    /**
     * {@inheritDoc}
     */
    public final int getShortName() {
        return this.shortAddress;
    }

    /**
     * {@inheritDoc}
     */
    public final int getMethodNumber() {
        return this.methodNumber;
    }
}
