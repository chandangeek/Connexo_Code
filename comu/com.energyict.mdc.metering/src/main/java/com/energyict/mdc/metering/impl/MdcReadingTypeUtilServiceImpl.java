/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.ReadingTypeInformation;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Component(name = "com.energyict.mdc.metering.mapping.MdcReadingTypeUtilService", service = MdcReadingTypeUtilService.class,
        property = {"osgi.command.scope=mdc.metering",
                "osgi.command.function=getReadingTypeInformation",
                "osgi.command.function=readingTypeHelp",
                "osgi.command.function=getReadingTypeFrom"}, immediate = true)
public class MdcReadingTypeUtilServiceImpl implements MdcReadingTypeUtilService {

    private volatile MeteringService meteringService;

    Logger logger = Logger.getLogger(MdcReadingTypeUtilServiceImpl.class.getName());

    public MdcReadingTypeUtilServiceImpl() {
    }

    @Inject
    public MdcReadingTypeUtilServiceImpl(MeteringService meteringService) {
        this();
        this.setMeteringService(meteringService);
    }

    @SuppressWarnings("unused")
    public void readingTypeHelp() {
        logger.info("- Use getReadingTypeInformation(readingType) to print the ObisCode/Unit and interval of a given 18 digit ReadingType.");
        logger.info("- The getReadingTypeFrom takes two or three arguments:");
        logger.info("\t- Obiscode and Unit.");
        logger.info("\t- Obiscode, Unit and Interval in seconds.");
    }

    @SuppressWarnings("unused")
    public void getReadingTypeInformation(String readingType) {
        getReadingTypeInformationFrom(readingType).ifPresent(rT -> {
            logger.info("ObisCode : " + rT.getObisCode());
            logger.info("Unit : " + rT.getUnit());
            logger.info("Interval : " + rT.getTimeDuration());
        });
    }

    public void getReadingTypeFrom(String... arguments) {
        try {
            if (arguments.length == 2) {
                logger.info("ReadingType : " + getReadingTypeMridFrom(ObisCode.fromString(arguments[0]), Unit.get(arguments[1])));
            } else if (arguments.length == 3) {
                logger.info("ReadingType : " + getReadingTypeFrom(ObisCode.fromString(arguments[0]), Unit.get(arguments[1]), new TimeDuration(Integer.valueOf(arguments[2]))));
            } else {
                logger.info("Sorry, you provided an incorrect amount of arguments.");
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    @Deprecated
    public ReadingTypeInformation getReadingTypeInformationFor(String readingType) {
        return getReadingTypeInformationFrom(readingType).orElse(null);
    }

    @Override
    @Deprecated
    public ReadingTypeInformation getReadingTypeInformationFor(ReadingType readingType) {
        return getReadingTypeInformationFrom(readingType.getMRID()).orElse(null);
    }


    @Override
    public Optional<ReadingTypeInformation> getReadingTypeInformationFrom(String readingType) {
        return ReadingTypeToObisCodeFactory.from(readingType);
    }

    @Override
    public Optional<ReadingTypeInformation> getReadingTypeInformationFrom(ReadingType readingType) {
        return ReadingTypeToObisCodeFactory.from(readingType.getMRID());
    }

    @Override
    public String getReadingTypeMridFrom(ObisCode obisCode, Unit unit) {
        return ObisCodeToReadingTypeFactory.createMRIDFromObisCodeAndUnit(obisCode, unit);
    }

    @Override
    public ReadingType getReadingTypeFrom(ObisCode obisCode, Unit unit) {
        String readingTypeMridFrom = getReadingTypeMridFrom(obisCode, unit);
        return this.meteringService.getReadingType(readingTypeMridFrom).orElse(null);
    }

    @Override
    public String getReadingTypeFrom(ObisCode obisCode, Unit unit, TimeDuration interval) {
        return ObisCodeToReadingTypeFactory.createMRIDFromObisCodeUnitAndInterval(obisCode, unit, interval);
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public Optional<ReadingType> getIntervalAppliedReadingType(ReadingType readingType, Optional<TimeDuration> interval, ObisCode registerObisCode) {
        ReadingTypeCodeBuilder readingTypeCodeBuilder = getReadingTypeCodeBuilderWithInterval(readingType, interval, registerObisCode);
        return this.meteringService.getReadingType(readingTypeCodeBuilder.code());
    }

    private ReadingTypeCodeBuilder getReadingTypeCodeBuilderWithInterval(ReadingType readingType, Optional<TimeDuration> interval, ObisCode registerObisCode) {
        ReadingTypeCodeBuilder readingTypeCodeBuilder = createReadingTypeCodeBuilderFrom(readingType);

        if (interval.isPresent() && interval.get().equals(TimeDuration.days(1))) {
            readingTypeCodeBuilder.period(MacroPeriod.DAILY);
            readingTypeCodeBuilder.period(TimeAttribute.NOTAPPLICABLE);
        } else if (interval.isPresent() && interval.get().equals(TimeDuration.months(1))) {
            readingTypeCodeBuilder.period(MacroPeriod.MONTHLY);
            readingTypeCodeBuilder.period(TimeAttribute.NOTAPPLICABLE);
        } else if (interval.isPresent() && interval.get().equals(TimeDuration.years(1))) {
            readingTypeCodeBuilder.period(MacroPeriod.YEARLY);
            readingTypeCodeBuilder.period(TimeAttribute.NOTAPPLICABLE);
        } else {
            readingTypeCodeBuilder.period(MeasuringPeriodMapping.getMeasuringPeriodFor(registerObisCode, interval.orElse(null)));
            readingTypeCodeBuilder.period(MacroPeriod.NOTAPPLICABLE);
        }
        return readingTypeCodeBuilder;
    }

    @Override
    public ReadingType getOrCreateIntervalAppliedReadingType(ReadingType readingType, Optional<TimeDuration> interval, ObisCode registerObisCode) {
        ReadingTypeCodeBuilder readingTypeCodeBuilder = getReadingTypeCodeBuilderWithInterval(readingType, interval, registerObisCode);
        return this.meteringService
                .getReadingType(readingTypeCodeBuilder.code())
                .orElseGet(() -> this.meteringService.createReadingType(readingTypeCodeBuilder.code(), readingType.getAliasName()));
    }

    @Override
    public ReadingType findOrCreateReadingType(String mrid, String alias) {
        return meteringService
                .getReadingType(mrid)
                .orElseGet(() -> meteringService.createReadingType(mrid, alias));
    }

    @Override
    public List<Unit> getMdcUnitsFor(String readingTypeStr) {
        return this.meteringService
                .getReadingType(readingTypeStr)
                .map(this::getMdcUnitsFor)
                .orElseGet(() -> Collections.singletonList(Unit.getUndefined()));
    }

    @Override
    public List<Unit> getMdcUnitsFor(ReadingType readingType) {
        return ReadingTypeUnitMapping.getMdcUnitsFor(readingType.getUnit(), readingType.getMultiplier());
    }

    @Override
    public ReadingTypeCodeBuilder createReadingTypeCodeBuilderFrom(ReadingType readingType) {
        return ReadingTypeCodeBuilder.of(readingType.getCommodity())
                .period(readingType.getMacroPeriod())
                .period(readingType.getMeasuringPeriod())
                .accumulate(readingType.getAccumulation())
                .aggregate(readingType.getAggregate())
                .argument(((int) readingType.getArgument().getNumerator()), (int) readingType.getArgument().getDenominator())
                .cpp(readingType.getCpp())
                .currency(readingType.getCurrency())
                .flow(readingType.getFlowDirection())
                .harmonic(((int) readingType.getInterharmonic().getNumerator()), (int) readingType.getInterharmonic().getDenominator())
                .in(readingType.getMultiplier(), readingType.getUnit())
                .measure(readingType.getMeasurementKind())
                .phase(readingType.getPhases())
                .tier(readingType.getConsumptionTier())
                .tou(readingType.getTou());
    }

    @Override
    public Optional<TimeDuration> getReadingTypeInterval(String readingType) {
        return ReadingTypeToObisCodeFactory.getIntervalFrom(readingType);
    }

    @Override
    public String getReadingTypeFilterFrom(ObisCode obisCode) {
        return ObisCodeToReadingTypeFilterFactory.createMRIDFilterFrom(obisCode);
    }
}