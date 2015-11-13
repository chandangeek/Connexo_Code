package com.energyict.protocol.exceptions;

import java.io.IOException;

/**
 * Models the exceptional situation that occurs when underlying
 * communication mechanisms report an IOException.
 * The design is that these will be caught by an AOP component
 * and dumped in a com.energyict.mdc.journal.ComTaskExecutionJournalEntry.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-15 (10:21)
 */
public class CommunicationException extends ProtocolRuntimeException {

    protected CommunicationException(Throwable cause, ProtocolExceptionReference code, Object... messageArguments) {
        super(cause, code, messageArguments);
    }

    protected CommunicationException(ProtocolExceptionReference reference, Object... messageArguments) {
        super(reference, messageArguments);
    }

    private CommunicationException(ProtocolExceptionReference reference, Exception cause) {
        super(cause, reference, cause.getMessage());
    }

    public static CommunicationException numberFormatException(NumberFormatException cause, String parameterName, String value) {
        return new CommunicationException(cause, ProtocolExceptionReference.NUMERIC_PARAMETER_EXPECTED, parameterName, value);
    }

    /**
     * The protocol received an unexpected response from the meter.
     * However, it is still possible to further communicate with the device.
     */
    public static CommunicationException unexpectedResponse(IOException e) {
        return new CommunicationException(e, ProtocolExceptionReference.UNEXPECTED_RESPONSE, e.getMessage());
    }

    public static CommunicationException unsupportedUrlContentType(String value) {
        return new CommunicationException(ProtocolExceptionReference.UNSUPPORTED_URL_CONTENT_TYPE, value);
    }

    /**
     * Throws a new CommunicationException that indicates that the specified
     * version number is not supported in the specified context.
     * The message that will be generated looks like: <code>Version {0} is not supported for {1}</code>.
     *
     * @param value   The unsupported version number
     * @param context The context that does not support the version
     * @return The CommunicationException
     */
    public static CommunicationException unsupportedVersion(String value, String context) {
        return new CommunicationException(ProtocolExceptionReference.UNSUPPORTED_VERSION, value, context);
    }

    /**
     * Throws a new CommunicationException that indicates that the specified
     * version number is not supported in the specified context.
     * The message that will be generated looks like: <code>Version {0} is not supported for {1}</code>.
     *
     * @param value   The unsupported version number
     * @param context The context that does not support the version
     * @return The CommunicationException
     */
    public static CommunicationException unsupportedVersion(int value, String context) {
        return new CommunicationException(ProtocolExceptionReference.UNSUPPORTED_VERSION, value, context);
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
    public static CommunicationException notConfiguredForInboundCommunication(Object deviceIdentifier) {
        return new CommunicationException(ProtocolExceptionReference.NOT_CONFIGURED_FOR_INBOUND_COMMUNICATION, deviceIdentifier);
    }

    /**
     * Throws a new CommunicationException that indicates that the specified
     * DiscoverResultType is not supported in the specified context.
     * Most likely, the component has not been updated after the result type
     * was added to the code base.
     *
     * @param resultType The unsupported DiscoverResultType
     * @return The CommunicationException
     */
    public static CommunicationException unsupportedDiscoveryResultType(String resultType) {
        return new CommunicationException(ProtocolExceptionReference.UNSUPPORTED_DISCOVERY_RESULT_TYPE, resultType);
    }

    public static CommunicationException missingInboundData(Object deviceIdentifier) {
        return new CommunicationException(ProtocolExceptionReference.NO_INBOUND_DATE, deviceIdentifier.toString());
    }

    /**
     * Creates an exception, indication that the logical disconnect to a device failed.</br>
     * <i>(with logical disconnect we mean a signOff/logOff, not the disconnect of the physical link)</i>
     *
     * @param cause the exception created by the protocol
     * @return the newly created protocolDisconnect failed exception
     */
    public static CommunicationException protocolDisconnectFailed(final Exception cause) {
        return new CommunicationException(ProtocolExceptionReference.PROTOCOL_DISCONNECT, cause);
    }
}