/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.impl.parser;

/**
 * @author grhodes
 * @since 10 Dec 2012 10:58:44
 */
class Options {

    private boolean throwExceptionOnParseError;
    private CasingTypeEnum casingType;
    private boolean verbose;
    private boolean zeroBasedDayOfWeek;

    public Options() {
        this.throwExceptionOnParseError = true;
        this.casingType = CasingTypeEnum.Sentence;
        this.verbose = false;
        // By default CRON expressions are zero-based
        this.zeroBasedDayOfWeek = true;
    }

    /**
     * @return the throwExceptionOnParseError
     */
    public boolean isThrowExceptionOnParseError() {
        return throwExceptionOnParseError;
    }

    /**
     * @param throwExceptionOnParseError the throwExceptionOnParseError to set
     */
    public void setThrowExceptionOnParseError(boolean throwExceptionOnParseError) {
        this.throwExceptionOnParseError = throwExceptionOnParseError;
    }

    /**
     * @return the casingType
     */
    public CasingTypeEnum getCasingType() {
        return casingType;
    }

    /**
     * @param casingType the casingType to set
     */
    public void setCasingType(CasingTypeEnum casingType) {
        this.casingType = casingType;
    }

    /**
     * @return the verbose
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * @param verbose the verbose to set
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * If we are zero-based
     * <ul>
     * <li>day of week (0 - 7) (0 to 6 are Sunday to Saturday, or use names; 7 is Sunday, the same as 0)</li>
     * </ul>
     * If we are not zero based
     * <ul>
     * <li>day of week (1 - 7) (1 to 6 are Sunday to Saturday, or use names; 7 is Saturday)</li>
     * </ul>
     * @param zeroBasedDayOfWeek if we are zero-based or not
     */
    public void setZeroBasedDayOfWeek(boolean zeroBasedDayOfWeek) {
        this.zeroBasedDayOfWeek = zeroBasedDayOfWeek;
    }

    /**
     * If we are zero-based
     * <ul>
     * <li>day of week (0 - 7) (0 to 6 are Sunday to Saturday, or use names; 7 is Sunday, the same as 0)</li>
     * </ul>
     * If we are not zero based
     * <ul>
     * <li>day of week (1 - 7) (1 to 7 are Sunday to Saturday, or use names)</li>
     * </ul>
     * @return if we are zero-based or not
     */
    public boolean isZeroBasedDayOfWeek() {
        return zeroBasedDayOfWeek;
    }

}
