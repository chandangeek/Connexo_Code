package com.energyict.protocols.mdc.services.impl;

/**
 * Provides environmental properties that are used by
 * legacy protocols to change their behavior.
 * These properties used to be provided by the
 * EIServer Environment class through
 * the various get and getProperty methods.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-30 (08:44)
 */
public interface EnvironmentPropertyService {

    public long getOpenSerialAndFlush();

    public long getSetParityAndFlush();

    public long getSetParamsAndFlush();

    public long getSetBaudrateAndFlush();

    public boolean isRs485SoftwareDriven();

    public String getDcdComPortsToIgnore();

    public int getDatagramInputStreamBufferSize();

}