package com.energyict.mdc.common.exceptions;

/**
 * Specifies the possible error references for all exceptions
 * defined in the common components of the ComServer module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (09:02)
 */
public enum CommonExceptionReferences implements ExceptionReference<CommonReferenceScope> {

    /**
     * Used when a BusinessException occurs dispite all the effort
     * of the developer to understand and respect the requirements.
     */
    UNEXPECTED_BUSINESS_EXCEPTION(99, 0),
    METHOD_ARGUMENT_CAN_NOT_BE_NULL(100, 3),
    JAVA_REFLECTION_ERROR(101, 2),
    LOGGER_FACTORY_REQUIRES_INTERFACE(102, 1),
    LOGGER_FACTORY_SUPPORTS_ONLY_ONE_THROWABLE_PARAMETER(103, 1),
    VALIDATION_FAILED(104, 2),
    UNRECOGNIZED_ENUM_VALUE(105, 2),
    UNEXPECTED_FACTORY(106, 2),
    ASYNCHRONEOUS_COMMUNICATION_IS_NOT_SUPPORTED(107, 0),
    INDEX_OUT_OF_BOUND_DATA_PARSE_EXCEPTION(108, 1),

    /**
     * Indication of a scenario where the configuration of a device is not accessible
     */
    CONFIG_NOT_ACCESSIBLE(109, 1),
    /**
     * Indication of a scenario where the number of channels is different then expected
     */
    CONFIG_CHANNEL_MISMATCH(110, 3),
    /**
     * Indication of a scenario where the name which is given to a ChannelInfo is not an ObisCode
     */
    CONFIG_CHANNEL_NAME_NOT_OBISCODE(111, 1),
    /**
     * Indication of a scenario where the serialNumber of the device is not correct.
     */
    CONFIG_SERIAL_NUMBER_MISMATCH(112, 2),
    /**
     * Indication of a scenario where the connect of a protocol did not succeed
     */
    PROTOCOL_CONNECT(113, 1),
    /**
     * Indication of a scenario where the disconnect of a protocol did not succeed
     */
    PROTOCOL_DISCONNECT(114, 1),
    /**
     * Indication of a parameter value that was expected to be numeric.
     * Typically this situation is signaled by a NumberFormatException.
     */
    NUMERIC_PARAMETER_EXPECTED(115, 2),
    /**
     * Indication that the content type received by a
     * servlet based inbound discovery protocol is not supported.
     */
    UNSUPPORTED_URL_CONTENT_TYPE(116, 1),
    /**
     * Indication that the content type received by a
     * servlet based inbound discovery protocol is not supported.
     */
    UNSUPPORTED_VERSION(117, 2),
    /**
     * Indication that a device did not send expected
     * data to a servlet based inbound discovery protocol.
     */
    NO_INBOUND_DATE(118, 1),
    /**
     * Indication of an error causing the maximum number of retries being reached (e.g. this can be a timeout error).
     */
    NUMBER_OF_RETRIES_REACHED(119, 2),
    /**
     * Indication of any error related to the ciphering of data (e.g.: errors popping up while encrypting data with the AES algorithm).
     */
    CIPHERING_EXCEPTION(120, 1),
    /**
     * Indication that a ConnectionTask of an expected ConnectionType does not exist in a Device.
     */
    MISSING_CONNECTION_TASK(121, 2),
    /**
     * Indication that some component does not yet support
     * a InboundDeviceProtocol.DiscoverResultType
     * that was recently added to the code base.
     */
    UNSUPPORTED_DISCOVERY_RESULT_TYPE(122, 1),
    /**
     * Indication that the method is not supported for this class.
     */
    UNSUPPORTED_METHOD(123, 2),
    /**
     * Indicates that a Map element was called, which is not included in the map
     */
    NON_EXISTING_MAP_ELEMENT(124, 3),
    /**
     * Generic reflection error, not specific for pluggable classes
     */
    GENERIC_JAVA_REFLECTION_ERROR(125, 1),
    /**
     * ClassNotFound exception indicating that the DeviceSecuritySupport class is not known
     */
    UNKNOWN_DEVICE_SECURITY_SUPPORT_CLASS(126, 1),
    /**
     * PrimaryKey format of DeviceMessageSpec was not in correct format
     */
    INCORRECT_DEVICE_MESSAGE_SPEC_PRIMARY_KEY(127, 1),
    /**
     * Indication a scenario where the class of the messageSpec does not exist 'anymore'
     */
    UNKNOWN_DEVICE_MESSAGE_SPEC_CLASS(128, 1),
    /**
     * PrimaryKey format of DeviceMessageSpec was not in correct format
     */
    INCORRECT_DEVICE_MESSAGE_CATEGORY_PRIMARY_KEY(129, 1),
    /**
     * Indication a scenario where the class of the messageSpec does not exist 'anymore'
     */
    UNKNOWN_DEVICE_MESSAGE_CATEGORY_CLASS(130, 1),
    /**
     * Indicates that a subclass of PartialConnectionTaskShadow is not supported in all areas of the code yet.
     */
    UNKNOWN_PARTIAL_CONNECTION_TASK_SHADOW(131, 1),
    /**
     * ClassNotFound exception indicating that the LegacyMessageConverter class is not known
     */
    UNKNOWN_DEVICE_MESSAGE_CONVERTER_CLASS(132, 1),
    /**
     * Indicates that an object could not be marshalled as a JSon object
     * due to missing or incorrect JAXB annotations.
     */
    UNEXPECTED_XML_JSON_ERROR(133, 1),
    /**
     * Indication of a scenario where the time difference exceeds the maximum allowed time difference
     */
    MAXIMUM_TIME_DIFFERENCE_EXCEEDED(134, 2),
    /**
     * Indicates the exception scenario where the protocol could not correctly parse the data, which should lead to a halt in the Communication
     */
    PROTOCOL_IO_PARSE_ERROR(135, 1),
    /**
     * Indicates the exception scenario where a SecurityPropertySet
     * does not support a property that is provided by name.
     */
    UNSUPPORTED_SECURITY_PROPERTY(136, 2),

    /**
     * Indicates the init method of a device protocol received an unexpected com channel type
     */
    UNEXPECTED_COM_CHANNEL(137, 2),

    /**
     * Indicates a property has a wrong (unexpected) format
     */
    INVALID_PROPERTY_FORMAT(138, 3),
    /**
     * Indicates we have an unexpected number of ComTasks
     */
    INCORRECT_NUMBER_OF_COMTASKS(139,2),
    /**
     * Indicates no pluggable classes could be found for the specified connection type
     */
    NO_PLUGGABLE_CLASSES_FOUND_FOR_CONNECTION_TYPE(140, 1),
    /**
     * Indicates the property spec of a certain property of a connection type could not be found
     */
    CONNECTION_TYPE_PROPERTY_SPEC_NOT_FOUND(141, 2),
    /**
     * Indicates the a device is not ready for inbound communication.
     * One of the following situations may be the cause:
     * <ul>
     * <li>No inbound connection task</li>
     * <li>No ComTask linked to the inbound connection task</li>
     * <li>No access to the security properties</li>
     * </ul>
     */
    NOT_CONFIGURED_FOR_INBOUND_COMMUNICATION(142, 1),

    /**
     * Indicates that an attempt was made to convert a PluggableClass
     * that is not of type PluggableClassType#CONNECTIONTYPE
     * to a ConnectionTypePluggableClass.
     */
    NO_CONNECTION_TYPE_PLUGGABLE_CLASS(143, 1),
    /**
     * Indicates that there is no ComTaskExecutionSessionShadow
     * available while one is expected because the related ComTask
     * has effectively executed.
     */
    SESSION_FOR_COMTASK_MISSING(144, 1),

    /**
     * Indicates that there was a duplicate object found when a unique object was expected
     */
    DUPLICATE_FOUND(145, 2),

    /**
     * E.g. when an exception occurs while parsing some XML
     */
    GENERAL_PARSE_ERROR(146, 1),

    UNKNOWN_LEGACY_VALUEFACTORY_CLASS(147, 1),

    /**
     * Indicates that a legacy protocol was not recognized as one of the known
     * legacy protocol types. Should be MeterProtocol or SmartMeterProtocol.
     */
    UNSUPPORTED_LEGACY_PROTOCOL_TYPE(148, 1),

    /**
     * Indication of a scenario in which, for any reason, the meter does not respond to any requests anymore
     */
    CONNECTION_TIMEOUT(200, 1),
    /**
     * Indication of a scenario in which, for any reason, the meter does not respond to any requests anymore
     */
    CONNECTION_FAILURE(201, 0),
    /**
     * Indication of a scenario in which the setup of the connection failed
     */
    CONNECTION_SETUP_ERROR(202, 0),

    /**
     * Indication of any Security-related issues during a communication session with a Device
     */
    SECURITY(300, 0),


    /**
     * Indication of a scenario where the contents of the first inbound frame are not as expected
     */
    INBOUND_UNEXPECTED_FRAME(401, 2),

    /**
     * Indication of a scenario where the receiving of the inbound frame times out
     */
    INBOUND_TIMEOUT(402, 1),

    /**
     * Indication of an exception caught during the setup of an inbound call
     */
    UNEXPECTED_INBOUND_COMMUNICATION_EXCEPTION(403, 0),

    /**
     * Indication of an IOException in a MeterProtocol or GenericProtocol, caught in one of the protocol adapters
     */
    LEGACY_IO(998, 1),
    UNEXPECTED_IO_EXCEPTION(999, 0);

    public long toNumerical() {
        return code;
    }

    @Override
    public int expectedNumberOfArguments() {
        return this.expectedNumberOfArguments;
    }


    CommonExceptionReferences(long code, int expectedNumberOfArguments) {
        this.code = code;
        this.expectedNumberOfArguments = expectedNumberOfArguments;
    }

    private long code;
    private int expectedNumberOfArguments;

}