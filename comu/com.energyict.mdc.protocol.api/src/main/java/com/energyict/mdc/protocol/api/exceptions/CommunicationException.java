package com.energyict.mdc.protocol.api.exceptions;

import com.energyict.mdc.common.ApplicationComponent;
import com.energyict.mdc.common.exceptions.CommonExceptionReferences;
import com.energyict.mdc.common.exceptions.CommonReferenceScope;
import com.energyict.mdc.common.exceptions.ExceptionCode;
import com.energyict.mdc.common.exceptions.ExceptionType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueFactory;

import java.io.IOException;

/**
 * Models the exceptional situation that occurs when underlying
 * communication mechanisms report an IOException.
 * The design is that these will be caught by an AOP component
 * and dumped in a ComTaskExecutionJournalEntry.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-15 (10:21)
 */
public class CommunicationException extends ComServerRuntimeException {

    public CommunicationException(IOException cause) {
        super(cause, unexpectedIoExceptionCode());
    }

    public CommunicationException(NumberFormatException cause, String parameterName, String value) {
        super(cause, numberFormatExceptionCode(), parameterName, value);
    }

    protected CommunicationException (Throwable cause, ExceptionCode code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

    protected CommunicationException (ExceptionCode code, Object... messageArguments) {
        super(code, messageArguments);
    }

    private CommunicationException(ExceptionCode code, Exception cause) {
        super(cause, code, cause.getMessage());
    }

    private static ExceptionCode unexpectedIoExceptionCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.COMMUNICATION, CommonExceptionReferences.UNEXPECTED_IO_EXCEPTION);
    }

    private static ExceptionCode numberFormatExceptionCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.COMMUNICATION, CommonExceptionReferences.NUMERIC_PARAMETER_EXPECTED);
    }

    public static CommunicationException unsupportedUrlContentType (String value) {
        return new CommunicationException(unsupportedUrlContentTypeCode(), value);
    }

    private static ExceptionCode unsupportedUrlContentTypeCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.COMMUNICATION, CommonExceptionReferences.UNSUPPORTED_URL_CONTENT_TYPE);
    }

    /**
     * Throws a new CommunicationException that indicates that the specified
     * version number is not supported in the specified context.
     * The message that will be generated looks like: <code>Version {0} is not supported for {1}</code>.
     *
     * @param value The unsupported version number
     * @param context The context that does not support the version
     * @return The CommunicationException
     */
    public static CommunicationException unsupportedVersion (String value, String context) {
        return new CommunicationException(unsupportedVersionCode(), value, context);
    }

    /**
     * Throws a new CommunicationException that indicates that the specified
     * version number is not supported in the specified context.
     * The message that will be generated looks like: <code>Version {0} is not supported for {1}</code>.
     *
     * @param value The unsupported version number
     * @param context The context that does not support the version
     * @return The CommunicationException
     */
    public static CommunicationException unsupportedVersion (int value, String context) {
        return new CommunicationException(unsupportedVersionCode(), value, context);
    }

    private static ExceptionCode unsupportedVersionCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.COMMUNICATION, CommonExceptionReferences.UNSUPPORTED_VERSION);
    }

    /**
     * Throws a CommunicationException that indicates that the Device does not have
     * a ConnectionTask of the expected ConnectionType.
     *
     * @param deviceIdentifier The reference to the Device
     * @param connectionTypeClass The class of the ConnectionType that is was expected
     * @return the newly create CommunicationException
     */
    public static <T extends ConnectionType> CommunicationException missingConnectionTask (DeviceIdentifier deviceIdentifier, Class<T> connectionTypeClass) {
        return new CommunicationException(missingConnectionTaskCode(), deviceIdentifier, connectionTypeClass.getName());
    }

    private static ExceptionCode missingConnectionTaskCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.COMMUNICATION, CommonExceptionReferences.MISSING_CONNECTION_TASK);
    }

    /**
     * Throws a CommunicationException that indicates that the Device
     * is not ready for inbound communication.
     * One of the following situations may be the cause:
     * <ul>
     * <li>No inbound connection task</li>
     * <li>No ComTask linked to the inbound connection task</li>
     * <li>No access to the security properties</li>
     * </ul>
     *
     * @param deviceIdentifier The reference to the Device
     * @return the newly create CommunicationException
     */
    public static CommunicationException notConfiguredForInboundCommunication (DeviceIdentifier deviceIdentifier) {
        return new CommunicationException(notConfiguredForInboundCommunicationCode(), deviceIdentifier);
    }

    private static ExceptionCode notConfiguredForInboundCommunicationCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.COMMUNICATION, CommonExceptionReferences.NOT_CONFIGURED_FOR_INBOUND_COMMUNICATION);
    }

    /**
     * Throws a new CommunicationException that indicates that the specified
     * {@link InboundDeviceProtocol.DiscoverResultType} is not supported in the specified context.
     * Most likely, the component has not been updated after the result type
     * was added to the code base.
     *
     * @param resultType The unsupported DiscoverResultType
     * @return The CommunicationException
     */
    public static CommunicationException unsupportedDiscoveryResultType (InboundDeviceProtocol.DiscoverResultType resultType) {
        return new CommunicationException(unsupportedDiscoveryResultTypeCode(), resultType);
    }

    private static ExceptionCode unsupportedDiscoveryResultTypeCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.UNSUPPORTED_DISCOVERY_RESULT_TYPE);
    }

    public static CommunicationException missingInboundData (DeviceIdentifier deviceIdentifier) {
        return new CommunicationException(missingInboundDataCode(), deviceIdentifier.toString());
    }

    private static ExceptionCode missingInboundDataCode () {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.COMMUNICATION, CommonExceptionReferences.NO_INBOUND_DATE);
    }

    /**
     * Creates an exception, indicating that the logical connect to a device failed.</br>
     * <i>(with logical connect we mean a signOn/logOn, the physical connection should already be established)</i>
     *
     * @param cause the exception created by the protocol
     * @return the newly created protocolConnect failed exception
     */
    public static CommunicationException protocolConnectFailed(final Exception cause) {
        return new CommunicationException(generateExceptionCodeByReference(CommonExceptionReferences.PROTOCOL_CONNECT), cause);
    }

    /**
     * Creates an exception, indication that the logical disconnect to a device failed.</br>
     * <i>(with logical disconnect we mean a signOff/logOff, not the disconnect of the physical link)</i>
     *
     * @param cause the exception created by the protocol
     * @return the newly created protocolDisconnect failed exception
     */
    public static CommunicationException protocolDisconnectFailed(final Exception cause) {
        return new CommunicationException(generateExceptionCodeByReference(CommonExceptionReferences.PROTOCOL_DISCONNECT), cause);
    }

    /**
     * Creates an exception, indication that the communication failed due to the number of retries being reached.</br>
     * <i>Note: the root cause of the fail can be any IOException (e.g.: a timeout) and is given as messageArgument </i>
     *
     * @param totalNumberOfAttempts the total number of attempts made to transmit/receive a piece of information from the device
     * @param cause the exception created by the protocol
     * @return the newly created numberOfRetriesReached CommunicationException
     */
    public static CommunicationException numberOfRetriesReached(final Exception cause, final int totalNumberOfAttempts) {
        return new CommunicationException(generateExceptionCodeByReference(CommonExceptionReferences.NUMBER_OF_RETRIES_REACHED), cause, totalNumberOfAttempts);
    }

    /**
     * Creates an exception, indication of any error related to the ciphering of data.<\br>
     * <i>(e.g.: errors popping up while encrypting data with an AES128 algorithm).</i>
     *
     * @param cause the exception created by the protocol
     * @return the newly created numberOfRetriesReached CommunicationException
     */
    public static CommunicationException cipheringException(final Exception cause) {
        return new CommunicationException(generateExceptionCodeByReference(CommonExceptionReferences.CIPHERING_EXCEPTION), cause);
    }

    /**
     * Generate an <code>ExceptionCode</code> based on the given <code>ComServerExecutionExceptionReferences</code>
     *
     * @param reference the {@link ExceptionCode#reference reference} to use in the <code>ExceptionCode</code>
     * @return the newly created <code>ExceptionCode</code>
     */
    private static ExceptionCode generateExceptionCodeByReference(CommonExceptionReferences reference) {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.COMMUNICATION, reference);
    }

    /**
     * Creates an exception, indication that no {@link com.energyict.mdc.common.ApplicationComponent}
     * was found that implements an expected interface.
     *
     * @param moduleInterface The interface that was expected to be implemented by the ApplicationComponent
     * @return the newly created CommunicationException
     */
    public static <T> CommunicationException missingModuleException (Class<T> moduleInterface) {
        return new CommunicationException(missingModuleExceptionExceptionCode(CommonExceptionReferences.MISSING_MODULE), moduleInterface.getName());
    }

    /**
     * Generate an <code>ExceptionCode</code> based on the given <code>ComServerExecutionExceptionReferences</code>
     *
     * @param reference the {@link ExceptionCode#reference reference} to use in the <code>ExceptionCode</code>
     * @return the newly created <code>ExceptionCode</code>
     */
    private static ExceptionCode missingModuleExceptionExceptionCode(CommonExceptionReferences reference) {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, reference);
    }

    /**
     * Indicates that some method is not supported for the specified class.
     *
     * @param clazz      the {@link Class} where the coding error occurred
     * @param methodName the method name of the unsupported method
     * @return a newly created CommunicationException
     */
    public static CommunicationException unsupportedMethod(Class clazz, String methodName) {
        return new CommunicationException(methodNotSupported(), clazz.getName(), methodName);
    }

    private static ExceptionCode methodNotSupported() {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.UNSUPPORTED_METHOD);
    }

    /**
     * Constructs a CommunicationException to represent the scenario where the className of
     * a {@link ValueFactory lecacy value factory} is unknown or no longer supported.
     *
     * @param className the unknown className
     * @return the newly created CommunicationException
     */
    public static CommunicationException unKnownLegacyValueFactoryClass(String className) {
        return new CommunicationException(unKnownLegacyValueFactoryClassExceptionCode(), className);
    }

    private static ExceptionCode unKnownLegacyValueFactoryClassExceptionCode() {
        return new ExceptionCode(new CommonReferenceScope(), ExceptionType.CODING, CommonExceptionReferences.UNKNOWN_LEGACY_VALUEFACTORY_CLASS);
    }

}