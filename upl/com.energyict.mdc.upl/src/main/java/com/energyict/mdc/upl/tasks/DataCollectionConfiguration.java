package com.energyict.mdc.upl.tasks;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Models the configuration options of the data collection engine.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-01 (13:53)
 */
public interface DataCollectionConfiguration {

    @XmlAttribute
    boolean isConfiguredToCollectRegisterData();

    @XmlAttribute
    boolean isConfiguredToCollectLoadProfileData();

    @XmlAttribute
    boolean isConfiguredToRunBasicChecks();

    @XmlAttribute
    boolean isConfiguredToCheckClock();

    @XmlAttribute
    boolean isConfiguredToCollectEvents();

    @XmlAttribute
    boolean isConfiguredToSendMessages();

    @XmlAttribute
    boolean isConfiguredToReadStatusInformation();

    @XmlAttribute
    boolean isConfiguredToUpdateTopology();
}