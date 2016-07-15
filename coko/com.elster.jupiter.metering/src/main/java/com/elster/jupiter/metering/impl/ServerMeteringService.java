package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.DayMonthTime;

import java.time.Clock;

/**
 * Adds behavior to {@link MeteringService} that is specific
 * to server-side component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-02 (14:56)
 */
public interface ServerMeteringService extends MeteringService {

    void addHeadEndInterface(HeadEndInterface headEndInterface);

    void removeHeadEndInterface(HeadEndInterface headEndInterface);

    DataModel getDataModel();

    Thesaurus getThesaurus();

    Clock getClock();

    EndDeviceEventTypeImpl createEndDeviceEventType(String mRID);

    EndDeviceControlTypeImpl createEndDeviceControlType(String mRID);

    /**
     * Creates the one and only {@link GasDayOptions}.
     *
     * @param yearStart The start of the gas year
     * @return The GasDayOptions
     * @throws IllegalStateException Thrown when GasDayOptions have already been created before
     */
    GasDayOptions createGasDayOptions(DayMonthTime yearStart);

    /**
     * Gets the GasDayOptions that were created at system installation time.
     *
     * @return The GasDayOptions
     */
    GasDayOptions getGasDayOptions();

}