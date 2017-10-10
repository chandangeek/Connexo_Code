/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol.exception;

import com.energyict.mdc.upl.nls.MessageSeed;

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
public class CommunicationException extends com.energyict.protocol.exceptions.CommunicationException {

    public CommunicationException(MessageSeed messageSeed, Object... messageArguments) {
        super(messageSeed, messageArguments);
    }

    public CommunicationException(Throwable cause, MessageSeed messageSeed, Object... messageArguments) {
        super(cause, messageSeed, messageArguments);
    }

    public static CommunicationException numberFormatException(NumberFormatException cause, String parameterName, String value) {
        return new CommunicationException(cause, ProtocolExceptionMessageSeeds.NUMERIC_PARAMETER_EXPECTED, parameterName, value);
    }

    /**
     * The protocol received an unexpected response from the meter.
     * However, it is still possible to further communicate with the device.
     */
    public static CommunicationException unexpectedResponse(IOException e) {
        return new CommunicationException(e, ProtocolExceptionMessageSeeds.UNEXPECTED_RESPONSE, e.getMessage());
    }

    public static CommunicationException unsupportedUrlContentType(String value) {
        return new CommunicationException(ProtocolExceptionMessageSeeds.UNSUPPORTED_URL_CONTENT_TYPE, value);
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
    public static CommunicationException unsupportedVersion(String value, String context) {
        return new CommunicationException(ProtocolExceptionMessageSeeds.UNSUPPORTED_VERSION, value, context);
    }

    /**
     * Throws a new CommunicationException that indicates that the specified
     * version number is not supported in the specified context.
     * The message that will be generated looks like: <code>Version {0} is not supported for {1}</code>.
     *
     * @param value The unsupported version number                     Thesaurus thesaurus
     * @param context The context that does not support the version
     * @return The CommunicationException
     */
    public static CommunicationException unsupportedVersion(int value, String context) {
        return new CommunicationException(ProtocolExceptionMessageSeeds.UNSUPPORTED_VERSION, value, context);
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
        return new CommunicationException(ProtocolExceptionMessageSeeds.NOT_CONFIGURED_FOR_INBOUND_COMMUNICATION, deviceIdentifier);
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
        return new CommunicationException(ProtocolExceptionMessageSeeds.UNSUPPORTED_DISCOVERY_RESULT_TYPE, resultType);
    }

    public static CommunicationException missingInboundData(Object deviceIdentifier) {
        return new CommunicationException(ProtocolExceptionMessageSeeds.NO_INBOUND_DATA, deviceIdentifier);
    }

    /**
     * Creates an exception, indicating that the logical connect to a device failed.</br>
     * <i>(with logical connect we mean a signOn/logOn, the physical connection should already be established)</i>
     * <p>
     * After this exception, communication to the device is no longer possible.
     * The ComServer won't execute the remaining (communication related) ComCommands.
     *
     * @param cause the exception created by the protocol
     * @return the newly created protocolConnect failed exception
     */
    public static CommunicationException protocolConnectFailed(final Exception cause) {
        return new CommunicationException(cause, ProtocolExceptionMessageSeeds.PROTOCOL_CONNECT);
    }

    /**
     * Creates an exception, indication that the logical disconnect to a device failed.</br>
     * <i>(with logical disconnect we mean a signOff/logOff, not the disconnect of the physical link)</i>
     *
     * @param cause the exception created by the protocol
     * @return the newly created protocolDisconnect failed exception
     */
    public static CommunicationException protocolDisconnectFailed(final Exception cause) {
        return new CommunicationException(cause, ProtocolExceptionMessageSeeds.PROTOCOL_DISCONNECT_FAILED);
    }

    /**
     * Creates an exception, indication that the serial number of the device cannot be read.</br>
     *
     * @return the newly created serialNumberNotSupported  exception
     */
    public static CommunicationException serialNumberNotSupported() {
        return new CommunicationException(ProtocolExceptionMessageSeeds.SERIAL_NUMBER_NOT_SUPPORTED);
    }
}