package com.energyict.mdc.upl.meterdata;

import com.energyict.mdc.upl.tasks.CompletionCode;

/**
 * Identifies different types of results that a DataCollected-Object can represent.
 */
public enum ResultType {

    /**
     * Identifies that the object is supported and complete according to the requested information.
     */
    Supported(CompletionCode.Ok),

    /**
     * Identifies that the object is not supported by the device, so no additional data is provided.
     */
    NotSupported(CompletionCode.ConfigurationWarning),

    /**
     * Identifies that the object is supported by the device, but the requested information could not be returned
     * because the configuration does not match with the request.
     */
    ConfigurationMisMatch(CompletionCode.ConfigurationWarning),

    /**
     * Type similar to {@link #ConfigurationMisMatch}, but with CompletionCode ConfigurationError instead of ConfigurationWarning.
     */
    ConfigurationError(CompletionCode.ConfigurationError),

    /**
     * Identifies that the object is supported by the device, but the requested information could not be fully fetched.
     * No data is returned, what data could not be fetched should be informed by the Issue object.
     */
    DataIncomplete(CompletionCode.ProtocolError),

    /**
     * Identifies that the object is supported by the device, but the format of the returned data is not as expected.
     * No data is returned.
     */
    InCompatible(CompletionCode.ProtocolError),

    /**
     * Should only be used if none of the other <code>ResultTypes</code> are valid.
     * The <code>Issue Object</code> should contain detailed information so it is perfectly understandable what and why this object is returned.
     */
    Other(CompletionCode.UnexpectedError);

    private final CompletionCode correspondingCompletionCode;

    ResultType(CompletionCode correspondingCompletionCode) {
        this.correspondingCompletionCode = correspondingCompletionCode;
    }

    public CompletionCode getCorrespondingCompletionCode() {
        return correspondingCompletionCode;
    }

}