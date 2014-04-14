package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceTypeFields;

/**
 * Models the exceptional situation that occurs when an
 * attempt is made to create an entity of this bundle
 * but another one of the same type and name already exists.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (16:10)
 */
public class DuplicateNameException extends LocalizedFieldValidationException {

    /**
     * Creates a new DuplicateNameException that models the exceptional situation
     * that occurs when an attempt is made to create a {@link com.energyict.mdc.device.config.RegisterGroup}
     * but another one with the same name already exists.
     *
     * @param thesaurus The Thesaurus
     * @param name The name of the RegisterGroup that already exists
     * @return The DuplicateNameException
     */
    public static DuplicateNameException registerGroupAlreadyExists (Thesaurus thesaurus, String name) {
        return new DuplicateNameException(thesaurus, MessageSeeds.REGISTER_GROUP_ALREADY_EXISTS, name);
    }

    /**
     * Creates a new DuplicateNameException that models the exceptional situation
     * that occurs when an attempt is made to create a {@link com.energyict.mdc.device.config.RegisterMapping}
     * but another one with the same name already exists.
     *
     * @param thesaurus The Thesaurus
     * @param name The name of the RegisterMapping that already exists
     * @return The DuplicateNameException
     */
    public static DuplicateNameException registerMappingAlreadyExists (Thesaurus thesaurus, String name) {
        return new DuplicateNameException(thesaurus, MessageSeeds.REGISTER_MAPPING_ALREADY_EXISTS, name);
    }

    /**
     * Creates a new DuplicateNameException that models the exceptional situation
     * that occurs when an attempt is made to create a {@link com.energyict.mdc.device.config.LoadProfileType}
     * but another one with the same name already exists.
     *
     * @param thesaurus The Thesaurus
     * @param name The name of the LoadProfileType that already exists
     * @return The DuplicateNameException
     */
    public static DuplicateNameException loadProfileTypeAlreadyExists (Thesaurus thesaurus, String name) {
        return new DuplicateNameException(thesaurus, MessageSeeds.LOAD_PROFILE_TYPE_ALREADY_EXISTS, name);
    }

    /**
     * Creates a new DuplicateNameException that models the exceptional situation
     * that occurs when an attempt is made to create a {@link com.energyict.mdc.common.interval.Phenomenon}
     * but another one with the same name already exists.
     *
     * @param thesaurus The Thesaurus
     * @param name The name of the Phenomenon that already exists
     * @return The DuplicateNameException
     */
    public static DuplicateNameException phenomenonAlreadyExists (Thesaurus thesaurus, String name) {
        return new DuplicateNameException(thesaurus, MessageSeeds.PHENOMENON_NAME_IS_REQUIRED, name);
    }

    /**
     * Creates a new DuplicateNameException that models the exceptional situation
     * that occurs when an attempt is made to create a {@link com.energyict.mdc.device.config.ChannelSpec}
     * but another one with the same name already exists.
     *
     * @param thesaurus The Thesaurus
     * @param name The name of the LogBookType that already exists
     * @return The DuplicateNameException
     */
    public static DuplicateNameException channelSpecAlreadyExists(Thesaurus thesaurus, String name) {
        return new DuplicateNameException(thesaurus, MessageSeeds.CHANNEL_SPEC_ALREADY_EXISTS, name);
    }

    /**
     * Creates a new DuplicateNameException that models the exceptional situation
     * that occurs when an attempt is made to create a {@link com.energyict.mdc.device.config.ChannelSpec}
     * but another one already exists with the same 27 first characters ...
     *
     * @param thesaurus The Thesaurus
     * @param name The name of the LogBookType that already exists
     * @return The DuplicateNameException
     */
    public static DuplicateNameException channelSpecAlreadyExistsFirstChars(Thesaurus thesaurus, String name) {
        return new DuplicateNameException(thesaurus, MessageSeeds.CHANNEL_SPEC_ALREADY_EXISTS_27_CHAR, name);
    }

    /**
     * Creates a new DuplicateNameException that models the exceptional situation
     * that occurs when an attempt is made to create a {@link com.energyict.mdc.device.config.DeviceConfiguration}
     * on a {@link com.energyict.mdc.device.config.DeviceType} but another one with the same
     * name already exists
     *
     * @param thesaurus The Thesaurus
     * @param name The name of the LogBookType that already exists
     * @return The DuplicateNameException
     */
    public static DuplicateNameException deviceConfigurationExists(Thesaurus thesaurus, String name) {
        return new DuplicateNameException(thesaurus, MessageSeeds.DUPLICATE_DEVICE_CONFIGURATION, name);
    }

    /**
     * Creates a new DuplicateNameException that models the exceptional situation
     * that occurs when an attempt is made to create a {@link com.energyict.mdc.device.config.DeviceType}
     * on a {@link com.energyict.mdc.device.config.DeviceType} but another one with the same
     * name already exists
     *
     * @param thesaurus The Thesaurus
     * @param name The name of the LogBookType that already exists
     * @return The DuplicateNameException
     */
    public static DuplicateNameException deviceTypeExists(Thesaurus thesaurus, String name) {
        return new DuplicateNameException(thesaurus, MessageSeeds.DEVICE_TYPE_ALREADY_EXISTS, DeviceTypeFields.NAME.fieldName(), name);
    }

    private DuplicateNameException(Thesaurus thesaurus, MessageSeeds messageSeeds, String name) {
        super(thesaurus, messageSeeds, null, name);
        this.set("name", name);
    }

    private DuplicateNameException(Thesaurus thesaurus, MessageSeeds messageSeeds, String javaFieldName, String name) {
        super(thesaurus, messageSeeds, javaFieldName, name);
        this.set("name", name);
    }

    public static DuplicateNameException protocolConfigurationPropertiesAlreadyExists(Thesaurus thesaurus, String name) {
        return new DuplicateNameException(thesaurus, MessageSeeds.DUPLICATE_PROTOCOL_CONFIGURATION_PROPERTIES, name);
    }

    public static DuplicateNameException partialInboundConnectionTaskExists(Thesaurus thesaurus, String name) {
        return new DuplicateNameException(thesaurus, MessageSeeds.DUPLICATE_PARTIAL_INBOUND_CONNECTION_TYPE, name);
    }

    public static DuplicateNameException partialOutboundConnectionTaskExists(Thesaurus thesaurus, String name) {
        return new DuplicateNameException(thesaurus, MessageSeeds.DUPLICATE_PARTIAL_OUTBOUND_CONNECTION_TYPE, name);
    }

    public static DuplicateNameException partialConnectionInitiationTaskExists(Thesaurus thesaurus, String name) {
        return new DuplicateNameException(thesaurus, MessageSeeds.DUPLICATE_PARTIAL_CONNECTION_INITIATION_TYPE, name);
    }

    public static DuplicateNameException securityPropertySetAlreadyExists(Thesaurus thesaurus, String name) {
        return new DuplicateNameException(thesaurus, MessageSeeds.DUPLICATE_SECURITY_PROPERTY_SET, name);
    }
}