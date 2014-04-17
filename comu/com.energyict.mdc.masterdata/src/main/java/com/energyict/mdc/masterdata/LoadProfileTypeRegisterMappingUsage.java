package com.energyict.mdc.masterdata;

/**
 * Models the fact that a {@link LoadProfileType} uses a {@link RegisterMapping}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-15 (17:55)
 */
public interface LoadProfileTypeRegisterMappingUsage {

    public LoadProfileType getLoadProfileType();

    public RegisterMapping getRegisterMapping();

}