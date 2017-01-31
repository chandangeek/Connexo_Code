/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

/**
 * Models the configuration options of the data collection engine.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-01 (13:53)
 */
public interface DataCollectionConfiguration {

    public boolean isConfiguredToCollectRegisterData ();

    public boolean isConfiguredToCollectLoadProfileData ();

    public boolean isConfiguredToRunBasicChecks ();

    public boolean isConfiguredToCheckClock ();

    public boolean isConfiguredToCollectEvents ();

    public boolean isConfiguredToSendMessages ();

    public boolean isConfiguredToReadStatusInformation ();

    public boolean isConfiguredToUpdateTopology ();

}