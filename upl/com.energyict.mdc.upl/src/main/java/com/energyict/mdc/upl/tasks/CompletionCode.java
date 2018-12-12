package com.energyict.mdc.upl.tasks;

/**
 * Models the different outcomes of the execution of tiny commands
 * that are sent to a Device in a communication session.
 * Some outcomes are more serious then others and should
 * be given priority when processing.
 * The {@link #hasPriorityOver(CompletionCode)} and {@link #upgradeTo(CompletionCode)} methods will support that.
 *
 * User: sva
 * Date: 23/04/12
 * Time: 14:30
 */
public enum CompletionCode {
    Ok,
    ConfigurationWarning,
    NotExecuted,
    Rescheduled,
    ProtocolError,
    ConfigurationError,
    IOError,
    UnexpectedError,
    TimeError,
    InitError,
    TimeoutError,
    ConnectionError;

    public int dbValue() {
        return this.ordinal();
    }

    public static CompletionCode valueFromDb(int dbValue) {
        for (CompletionCode completionCode : values()) {
            if (completionCode.dbValue() == dbValue) {
                return completionCode;
            }
        }
        throw new IllegalArgumentException("unknown dbValue: " + dbValue);
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
    public CompletionCode upgradeTo (CompletionCode other) {
        if (hasPriorityOver(other)) {
            return this;
        }
        else {
            return other;
        }
    }

    /**
     * Tests if this CompletionCode has priority over the other,
     * i.e. if this CompletionCode is more serious that the other.
     *
     * @param other The other CompletionCode
     * @return A flag that indicates if this CompletionCode is more serious than
     *         the other and is therefore considered have a higher priority
     */
    public boolean hasPriorityOver (CompletionCode other) {
        return this.compareTo(other) > 0;
    }

}