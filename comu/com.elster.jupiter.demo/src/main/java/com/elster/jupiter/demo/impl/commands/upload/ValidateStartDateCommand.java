/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.upload;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.commands.upload.time.IntervalReadingTimeProvider;
import com.elster.jupiter.demo.impl.commands.upload.time.NoneIntervalReadingTimeProvider;
import com.elster.jupiter.demo.impl.commands.upload.time.TimeProvider;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ValidateStartDateCommand extends ReadDataFromFileCommand{
    private final MeteringService meteringService;
    private final IdsService idsService;
    private final Clock clock;

    private Map<ReadingType, Instant> readingTypeInstantMap;
    private Instant vaultMaxValue;

    private TimeProvider intervalTimeProvider;
    private TimeProvider noneIntervalTimeProvider;

    @Inject
    public ValidateStartDateCommand(MeteringService meteringService, IdsService idsService, Clock clock) {
        super(meteringService);
        this.meteringService = meteringService;
        this.idsService = idsService;
        this.clock = clock;

        this.intervalTimeProvider = new IntervalReadingTimeProvider();
        this.noneIntervalTimeProvider = new NoneIntervalReadingTimeProvider();
    }

    @Override
    protected void checkBeforeRun(){
        if (getStart() == null){
            throw new UnableToCreate("Please specify the start time");
        }
    }

    @Override
    // Determine the maximum allowed timestamp for readings
    protected void beforeParse() {
        super.beforeParse();
        readingTypeInstantMap = new HashMap<>();
        vaultMaxValue = getMaxAllowedTimestamp().orElseGet(this::getMaxAllowedTimestampFromNow);
    }

    private Optional<Instant> getMaxAllowedTimestamp(){
        int[] vaultIds = {1, 2, 3};
        Instant current = null;
        for (int vaultId : vaultIds) {
            Optional<Vault> vault = idsService.getVault(MeteringService.COMPONENTNAME, vaultId);
            if (vault.isPresent()){
                Instant candidate = vault.get().getMaxDate();
                if (current == null || (candidate != null && candidate.isBefore(current))){
                    current = candidate;
                }
            }
        }
        return Optional.ofNullable(current);
    }

    private Instant getMaxAllowedTimestampFromNow(){
        return clock.instant().plus(360, ChronoUnit.DAYS);
    }

    @Override
    // calculate maximum reading timestamp for current startDate parameter
    protected void saveRecord(ReadingType readingType, String controlValue, Double value) {
        TimeProvider timeProvider = null;
        if (readingType.getMeasuringPeriod() == TimeAttribute.NOTAPPLICABLE && readingType.getMacroPeriod() != MacroPeriod.NOTAPPLICABLE){
            // it's none interval reading type
            timeProvider = noneIntervalTimeProvider;
        } else {
            // it's interval reading type
            timeProvider = intervalTimeProvider;
        }
        Instant timeForReading = timeProvider.getTimeForReading(readingType, getStart(), controlValue);
        Instant current = readingTypeInstantMap.get(readingType);
        if (current == null || current.isBefore(timeForReading)){
            readingTypeInstantMap.put(readingType, timeForReading);
        }
    }

    @Override
    // Compare max allowed timestamp and max calculated timestamp
    protected void afterParse() {
        super.afterParse();
        Optional<Instant> maxCalculatedReadingTimestamp = readingTypeInstantMap.values().stream().max((a, b) -> a.compareTo(b));
        if (maxCalculatedReadingTimestamp.isPresent() && this.vaultMaxValue.isBefore(maxCalculatedReadingTimestamp.get())){
            // error case
            Instant maxStartInstant = getStart().minusMillis(maxCalculatedReadingTimestamp.get().minusMillis(this.vaultMaxValue.toEpochMilli()).toEpochMilli());
            ZonedDateTime maxStart = ZonedDateTime.ofInstant(maxStartInstant, ZoneId.systemDefault()).withDayOfMonth(1).toLocalDate().atStartOfDay(ZoneId.systemDefault());
            throw new UnableToCreate("Incorrect start date parameter. The maximum allowed startDate is " + maxStart);
        }
    }
}
