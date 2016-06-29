package com.energyict.mdc.device.data.tasks.history;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.protocol.api.device.data.ResultType;

import java.util.EnumSet;
import java.util.Set;

/**
 * Models the different outcomes of the execution of tiny commands
 * that are sent to a {@link com.energyict.mdc.device.data.Device}
 * in a communication session.
 * Some outcomes are more serious then others and should
 * be given priority when processing.
 * The {@link #hasPriorityOver(CompletionCode)} and {@link #upgradeTo(CompletionCode)} methods will support that.
 * <p>
 * User: sva
 * Date: 23/04/12
 * Time: 14:30
 */
public enum CompletionCode {
    Ok(EnumSet.of(ResultType.Supported)),
    ConfigurationWarning(EnumSet.of(ResultType.NotSupported, ResultType.ConfigurationMisMatch)),
    NotExecuted(EnumSet.noneOf(ResultType.class)),
    ProtocolError(EnumSet.of(ResultType.DataIncomplete, ResultType.InCompatible)),
    ConfigurationError(EnumSet.of(ResultType.ConfigurationError)),
    IOError(EnumSet.noneOf(ResultType.class)),
    UnexpectedError(EnumSet.of(ResultType.Other)),
    TimeError(EnumSet.noneOf(ResultType.class)),
    InitError(EnumSet.noneOf(ResultType.class)),
    TimeoutError(EnumSet.noneOf(ResultType.class)),
    ConnectionError(EnumSet.noneOf(ResultType.class));

    private Set<ResultType> relatedResultTypes;

    CompletionCode(Set<ResultType> relatedResultTypes) {
        this.relatedResultTypes = relatedResultTypes;
    }

    public static CompletionCode fromOrdinal(int ordinal) {
        return values()[ordinal];
    }

    /**
     * Finds the CompletionCode for the specified {@link ResultType}.
     *
     * @param resultType The ResultType
     * @return The CompletionCode
     */
    public static CompletionCode forResultType(ResultType resultType) {
        for (CompletionCode completionCode : values()) {
            if (completionCode.relatesTo(resultType)) {
                return completionCode;
            }
        }
        throw new ApplicationException("No matching CompletionCode for ResultType: " + resultType);
    }

    public int dbValue() {
        return this.ordinal();
    }

    private boolean relatesTo(ResultType resultType) {
        return this.relatedResultTypes.contains(resultType);
    }

    /**
     * Returns the most serious CompletionCode,
     * i.e. when the other CompletionCode is more serious then
     * this one then the other is returned.
     * As an example, Ok is considered less serious
     * then ConfigurationWarning and ConfigurationWarning
     * is considered less serious then ConfigurationError.
     * Therefore the following should hold true:
     * <pre>
     * <code>
     *     CompletionCode.Ok.upgradeTo(CompletionCode.ConfigurationWaring) == CompletionCode.ConfigurationWarning
     *     CompletionCode.ConfigurationWaring.upgradeTo(CompletionCode.Ok) == CompletionCode.ConfigurationWarning
     *     CompletionCode.ConfigurationWaring.upgradeTo(CompletionCode.ConfigurationError) == CompletionCode.ConfigurationError
     * </code>
     * </pre>
     *
     * @param other The other CompletionCode
     * @return The most serious CompletionCode
     */
    public CompletionCode upgradeTo(CompletionCode other) {
        if (hasPriorityOver(other)) {
            return this;
        } else {
            return other;
        }
    }

    /**
     * Tests if this CompletionCode has priority over the other,
     * i.e. if this CompletionCode is more serious that the other.
     *
     * @param other The other CompletionCode
     * @return A flag that indicates if this CompletionCode is more serious than
     * the other and is therefore considered have a higher priority
     */
    public boolean hasPriorityOver(CompletionCode other) {
        return other == null || this.compareTo(other) > 0;
    }
}