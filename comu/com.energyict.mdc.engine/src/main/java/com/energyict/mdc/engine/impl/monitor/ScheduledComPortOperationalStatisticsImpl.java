/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.monitor;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.impl.core.ScheduledComPort;
import com.energyict.mdc.engine.monitor.ScheduledComPortOperationalStatistics;

import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.time.Clock;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link ScheduledComPortOperationalStatistics} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-17 (10:13)
 */
public class ScheduledComPortOperationalStatisticsImpl extends OperationalStatisticsImpl implements ServerScheduledComPortOperationalStatistics {

    public static final String LAST_CHECK_FOR_WORK_ITEM_NAME = "lastCheckForWorkTimestamp";
    private static final String LAST_CHECK_FOR_WORK_ITEM_DESCRIPTION = "last check for work timestamp";

    private final ScheduledComPort comPort;
    private Date lastCheckForWorkTimestamp;

    public ScheduledComPortOperationalStatisticsImpl(ScheduledComPort comPort, Clock clock, Thesaurus thesaurus) {
        super(clock, thesaurus, comPort.getComPort().getComServer().getChangesInterPollDelay());
        this.comPort = comPort;
    }

    @Override
    public TimeDuration getSchedulingInterPollDelay() {
        return this.comPort.getComPort().getComServer().getSchedulingInterPollDelay();
    }

    @Override
    public Optional<Date> getLastCheckForWorkTimestamp() {
        return Optional.ofNullable(this.lastCheckForWorkTimestamp);
    }

    @Override
    public void setLastCheckForWorkTimestamp(Date lastCheckForWorkTimestamp) {
        this.lastCheckForWorkTimestamp = lastCheckForWorkTimestamp;
    }

    @Override
    protected void addItemNames (List<String> itemNames) {
        super.addItemNames(itemNames);
        itemNames.add(LAST_CHECK_FOR_WORK_ITEM_NAME);
    }

    @Override
    protected void addItemDescriptions (List<String> itemDescriptions) {
        super.addItemDescriptions(itemDescriptions);
        itemDescriptions.add(LAST_CHECK_FOR_WORK_ITEM_DESCRIPTION);
    }

    @Override
    protected void addItemTypes (List<OpenType> itemTypes) {
        super.addItemTypes(itemTypes);
        itemTypes.add(SimpleType.STRING);
    }

    @Override
    protected void initializeAccessors (List<CompositeDataItemAccessor> accessors) {
        super.initializeAccessors(accessors);
        accessors.add(
                new CompositeDataItemAccessor(
                        LAST_CHECK_FOR_WORK_ITEM_NAME,
                        () -> getLastCheckForWorkTimestamp()
                                    .map(Date::toString)
                                    .orElse("")));
    }

}