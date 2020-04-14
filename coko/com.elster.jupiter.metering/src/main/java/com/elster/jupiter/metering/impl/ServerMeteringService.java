/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.time.DayMonthTime;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

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

    AmrSystemImpl createAmrSystem(int id, String name);

    EndDeviceEventTypeImpl createEndDeviceEventType(String mRID);

    void createAllReadingTypes(List<Pair<String, String>> readingTypes);

    ServiceCategoryImpl createServiceCategory(ServiceKind serviceKind, boolean active);

    MultiplierType createMultiplierType(MultiplierType.StandardType standardType);

    EndDeviceControlTypeImpl createEndDeviceControlType(String mRID);

    /**
     * Creates the one and only {@link GasDayOptions}.
     *
     * @param yearStart The start of the gas year
     * @return The GasDayOptions
     * @throws IllegalStateException Thrown when GasDayOptions have already been created before
     */
    GasDayOptions createGasDayOptions(DayMonthTime yearStart);

    boolean isCalendarEffectiveForAnyUsagePoint(Calendar calendar);

    /**
     * @param id "id" of a reading type.
     * @return Reading type by its id. Pls note that reading types are cached by id until restart of Connexo,
     * due to this all editable properties of the reading type may be obsolete (for now it's only {@link ReadingType#isActive()} flag).
     */
    Optional<ReadingType> getReadingTypeById(long id);
}
