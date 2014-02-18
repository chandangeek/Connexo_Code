package com.energyict.mdc.metering.impl;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

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

    @Activate
    public void activate(BundleContext context) {
    }

    @Deactivate
    public void deactivate() {
    }

    public void readingTypeHelp(){
        System.out.println("- Use getReadingTypeInformation(readingType) to print the ObisCode/Unit and interval of a given 18 digit ReadingType.");
        System.out.println("- The getReadingTypeFrom takes two or three arguments:");
        System.out.println("\t- Obiscode and Unit.");
        System.out.println("\t- Obiscode, Unit and Interval in seconds.");
    }

    public void getReadingTypeInformation(String readingType){
        ReadingTypeInformation readingTypeInformation = getReadingTypeInformationFor(readingType);
        System.out.println("ObisCode : " + readingTypeInformation.getObisCode());
        System.out.println("Unit : " + readingTypeInformation.getUnit());
        System.out.println("Interval : " + readingTypeInformation.getTimeDuration());
    }

    public void getReadingTypeFrom(String... arguments){
        try {
            if(arguments.length == 2){
                System.out.println("ReadingType : " + getReadingTypeFrom(ObisCode.fromString(arguments[0]), Unit.get(arguments[1])));
            } else if (arguments.length == 3){
                System.out.println("ReadingType : " + getReadingTypeFrom(ObisCode.fromString(arguments[0]), Unit.get(arguments[1]), new TimeDuration(Integer.valueOf(arguments[2]))));
            } else {
                System.out.println("Sorry, you provided an incorrect amount of arguments.");
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
}
