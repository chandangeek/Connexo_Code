package com.energyict.mdc.upl.tasks;

/**
 * Models the configuration options of the data collection engine.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-01 (13:53)
 */
public interface DataCollectionConfiguration {

    boolean isConfiguredToCollectRegisterData();

    boolean isConfiguredToCollectLoadProfileData();

    boolean isConfiguredToRunBasicChecks();

    boolean isConfiguredToCheckClock();

    boolean isConfiguredToCollectEvents();

    boolean isConfiguredToSendMessages();

    boolean isConfiguredToReadStatusInformation();

    boolean isConfiguredToUpdateTopology();

}