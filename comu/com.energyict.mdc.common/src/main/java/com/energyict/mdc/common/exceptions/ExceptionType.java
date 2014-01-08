package com.energyict.mdc.common.exceptions;

/**
 * Models the different types of exceptional situations that are
 * encountered in the ComServer components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-26 (16:26)
 */
public enum ExceptionType {

    /**
     * Indicates a data quality issue in existing data.
     * <br>
     * <b>Examples:</b>
     * <ul>
     * <li>A device is returning data in a format that is not recognized.</li>
     * <li>A device is expected to have a certain property but it claims it doesn't.</li>
     * </ul>
     */
    DATA_QUALITY("DQUA"),

    /**
     * Indicates that data does not comply with the business validation rules that apply to it.
     * This type of exception relates closely to DATA_QUALITY but will occur before the data
     * is stored in the system, i.e. at the time the data is captured either through the UI or via external interfaces.
     * <b>Examples:</b>
     * <ul>
     * <li>A value provided for a pluggable property is not of the correct type.</li>
     * <li>A value provided for a pluggable property does not comply with the expected format.</li>
     * </ul>
     */
    VALIDATION("DVAL"),

    /**
     * Indicates a configuration issue.<br>
     * <b>Examples:</b>
     * <ul>
     * <li>A device is expected to have a certain register or load profile but it claims it doesn't.</li>
     * </ul>
     */
    CONFIGURATION("CONF"),

    /**
     * Indicates an issue in sql statements.
     * These types of errors relate mostly to setup or coding problems.
     */
    SQL("SQL"),

    /**
     * Indicates an issue reported by system components.
     * Typical example is IOException.
     */
    SYSTEM("SYST"),

    /**
     * Indicates an error that should have been caught at development time
     * and should in fact never occur in a production environment.<br>
     * <b>Examples:</b>
     * <ul>
     * <li>Subclass does not override a template method that for one reason
     *     or the other could not be marked as abstract
     *     and was therefore not trapped by the compiler.</li>
     * </ul>
     *
     */
    CODING("DEV"),

    /**
     * Indicates an error during any type of <i>Communication</i><br>
     * <b>Examples:</b>
     * <ul>
     *     <li>A Communication timeout</li>
     *     <li>A Connection failure</li>
     *     <li>A Dialing error</li>
     *     <li>A failure to initiate a connection (e.g. wake up)</li>
     * </ul>
     *
     */
    COMMUNICATION("COM");

    ExceptionType (String code) {
        this.code = code;
    }

    public String getCode () {
        return code;
    }

    private String code;

}