package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.metering.ReadingTypeInformation;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 17/02/14
 * Time: 13:58
 */
@Component(name = "com.energyict.mdc.metering.mapping.MdcReadingTypeUtilService", service = MdcReadingTypeUtilService.class,
        property = {"osgi.command.scope=mdc.metering",
                "osgi.command.function=getReadingTypeInformation",
                "osgi.command.function=readingTypeHelp",
                "osgi.command.function=getReadingTypeFrom"},  immediate = true)
public class MdcReadingTypeUtilServiceImpl implements MdcReadingTypeUtilService {

    private volatile MeteringService meteringService;

    Logger logger = Logger.getLogger(MdcReadingTypeUtilServiceImpl.class.getName());

    public MdcReadingTypeUtilServiceImpl() {
    }

    @Inject
    public MdcReadingTypeUtilServiceImpl(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Activate
    public void activate(BundleContext context) {
    }

    @Deactivate
    public void deactivate() {
    }

    public void readingTypeHelp(){
        logger.info("- Use getReadingTypeInformation(readingType) to print the ObisCode/Unit and interval of a given 18 digit ReadingType.");
        logger.info("- The getReadingTypeFrom takes two or three arguments:");
        logger.info("\t- Obiscode and Unit.");
        logger.info("\t- Obiscode, Unit and Interval in seconds.");
    }

    public void getReadingTypeInformation(String readingType){
        ReadingTypeInformation readingTypeInformation = getReadingTypeInformationFor(readingType);
        logger.info("ObisCode : " + readingTypeInformation.getObisCode());
        logger.info("Unit : " + readingTypeInformation.getUnit());
        logger.info("Interval : " + readingTypeInformation.getTimeDuration());
    }

    public void getReadingTypeFrom(String... arguments){
        try {
            if(arguments.length == 2){
                logger.info("ReadingType : " + getReadingTypeFrom(ObisCode.fromString(arguments[0]), Unit.get(arguments[1])));
            } else if (arguments.length == 3){
                logger.info("ReadingType : " + getReadingTypeFrom(ObisCode.fromString(arguments[0]), Unit.get(arguments[1]), new TimeDuration(Integer.valueOf(arguments[2]))));
            } else {
                logger.info("Sorry, you provided an incorrect amount of arguments.");
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    @Override
    public ReadingTypeInformation getReadingTypeInformationFor(String readingType){
        return ReadingTypeToObisCodeFactory.from(readingType);
    }

    @Override
    public ReadingTypeInformation getReadingTypeInformationFor(ReadingType readingType){
        return ReadingTypeToObisCodeFactory.from(readingType.getMRID());
    }

    @Override
    public String getReadingTypeFrom(ObisCode obisCode, Unit unit){
        return ObisCodeToReadingTypeFactory.createMRIDFromObisCodeAndUnit(obisCode, unit);
    }

    @Override
    public String getReadingTypeFrom(ObisCode obisCode, Unit unit, TimeDuration interval){
        return ObisCodeToReadingTypeFactory.createMRIDFromObisCodeUnitAndInterval(obisCode, unit, interval);
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public ReadingType getIntervalAppliedReadingType(ReadingType readingType, TimeDuration interval, ObisCode registerObisCode) {
        ReadingTypeCodeBuilder readingTypeCodeBuilder = copyReadingTypeFields(readingType);

        readingTypeCodeBuilder.period(MeasuringPeriodMapping.getMeasuringPeriodFor(registerObisCode, interval));
        readingTypeCodeBuilder.period(MacroPeriodMapping.getMacroPeriodFor(registerObisCode, interval));

        return this.meteringService.getReadingType(readingTypeCodeBuilder.code()).orNull();
    }

    private ReadingTypeCodeBuilder copyReadingTypeFields(ReadingType readingType) {
        return ReadingTypeCodeBuilder.of(readingType.getCommodity())
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
    public Unit getMdcUnitFor(ReadingType readingType) {
        return ReadingTypeUnitMapping.getMdcUnitFor(readingType.getUnit(), readingType.getMultiplier());
    }
}
