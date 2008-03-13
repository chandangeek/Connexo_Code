package com.energyict.genericprotocolimpl.lgadvantis;

import java.util.*;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.lgadvantis.collector.*;
import com.energyict.genericprotocolimpl.lgadvantis.encoder.*;
import com.energyict.genericprotocolimpl.lgadvantis.parser.*;
import com.energyict.obis.ObisCode;

public class CosemFactory {
    
    public final static int ACTIVE_CALENDAR = 0;
    public final static int PASSIVE_CALENDAR = 1;
    
    private TimeZone timeZone;
    
    private List all = new ArrayList( );
    private Map obisCodes = new HashMap();
    private List allAttributes = new ArrayList();

    private CosemObject clock;
    private CosemObject meterStatus;
    private CosemObject errorCodeRegister;
    
    private CosemObject activityCalendar;
    
    private CosemObject scriptMobilePeakTempo;
    private CosemObject breakerUnit;
    private CosemObject meterIdentification;
    private CosemObject presetDemand;
    private CosemObject ticConfiguration;
    private CosemObject energy;
    private CosemObject dailyEnergyValueProfile; 
    private CosemObject loadProfileEnergy;
    private CosemObject logBook;
    private CosemObject thresholdForSag; 
    private CosemObject thresholdForSwell;  
    private CosemObject timeIntegralForSagMeasurement;
    private CosemObject timeThresholdForLongPowerFailure; 
    private CosemObject timeIntegralForInstantaneousDemand; 
    private CosemObject timeIntegralForSwellMeasurement;
    private CosemObject numberOfLongPowerFailure;
    private CosemObject numberOfShortPowerFailure; 
    private CosemObject numberOfSags;
    private CosemObject numberOfSwells; 
    private CosemObject maximumVoltage; 
    private CosemObject minimumVoltage; 
    private CosemObject instantaneousDemand;
    private CosemObject currentRatio;
    
    private CosemAttribute loadProfileBuffer;
    private CosemAttribute capturePeriod;
    
    private CosemAttribute calendarNameActive;
    
    private CosemAttribute daylyEnergyValueProfileBuffer;
    private CosemAttribute logbookBuffer;

    private CosemAttribute activeEnergySumm;
    private CosemAttribute activeEnergyTou1;
    private CosemAttribute activeEnergyTou2;
    private CosemAttribute activeEnergyTou3;
    private CosemAttribute activeEnergyTou4;
    private CosemAttribute activeEnergyTou5;
    private CosemAttribute activeEnergyTou6;
    
    private CosemAttribute thresholdForSagAttribute;
    private CosemAttribute thresholdForSwellAttribute;
    private CosemAttribute timeIntegralForSagMeasurementAttribute;
    private CosemAttribute timeThresholdForLongPowerFailureAttribute;;
    private CosemAttribute timeIntegralForInstantaneousDemandAttribute;
    private CosemAttribute timeIntegralForSwellMeasurementAttribute;
    
    
    public CosemFactory(TimeZone timeZone) {
        this.timeZone = timeZone;
        init();
    }
    
    public void init( ) {
        
        Unit wh = Unit.get( BaseUnit.WATTHOUR ); 
        
        String obis = "0.0.1.0.0.255";
        clock = 
            new CosemObject(obis, "Clock") 
                .add( new CosemAttribute( 0x2BC8, "time" )
                        .setParser( new DateTimeParser( timeZone ) )
                        .setEncoder( new DateEncoder(timeZone) ) );
        add( clock );        
        
        
        meterStatus = 
            new CosemObject( "0.0.96.5.0.255", "Meter Status" )
                .add( new CosemAttribute( 0xc100,  "value" ) );
        add( meterStatus );
        
        
        errorCodeRegister = 
            new CosemObject( "0.0.97.97.0.255", "Error code register" )
                .add( new CosemAttribute( 0x2FB0, "value" ) );
        add( errorCodeRegister );
        
        activityCalendar = 
            new CosemObject( "0.0.13.0.0.255", "Activity calendar" );
        activityCalendar
            .setCollector(new ActivityCalendarCollector(this) ); 
        activityCalendar
        
                .add( calendarNameActive = new CosemAttribute( 0xc740, "Calendar name active" ))
                .add( new CosemAttribute( 0xc748, "Season profile active" )
                    .setParser( new SeasonProfileParser(ACTIVE_CALENDAR) ) )
                .add( new CosemAttribute( 0xc750, "Week profile active" )
                    .setParser( new WeekProfileTableParser( ACTIVE_CALENDAR ) ) )
                .add( new CosemAttribute( 0xc758, "Day profile table active" ) 
                    .setParser( new DayProfileParser( PASSIVE_CALENDAR ) ) )
                    
                .add( new CosemAttribute( 0xc760, "Calendar name passive" )
                    .setParser( new CalendarNameParser(PASSIVE_CALENDAR) )
                    .setEncoder( new CalendarNameEncoder( ) )  )
                .add( new CosemAttribute( 0xc768, "Season profile passive" )
                    .setParser( new SeasonProfileParser(PASSIVE_CALENDAR) ) 
                    .setEncoder( new SeasonProfileEncoder( ) )  )
                .add( new CosemAttribute( 0xc770, "Week profile table passive" )
                    .setParser( new WeekProfileTableParser( PASSIVE_CALENDAR ) )
                    .setEncoder( new WeekProfileEncoder( ) ) )
                .add( new CosemAttribute( 0xc778, "Day profile table passive" )
                    .setParser( new DayProfileParser( PASSIVE_CALENDAR ) ) 
                    .setEncoder( new DayProfileEncoder( ) ) )
                .add( new CosemAttribute( 0xc780, "Activate passive calendar time" )
                    .setParser( new ActivatePassiveCalendarTimeParser( ) ) 
                    .setEncoder( new ActivatePassiveCalendarEncoder( ) ) );
        add( activityCalendar );
        
        calendarNameActive.setParser( new CalendarNameParser(ACTIVE_CALENDAR) );
        /* :-S The collector is used, but it could/should be defined on 
         * all attributes */
        calendarNameActive.setCollector( new ActivityCalendarCollector(this) );
        
        scriptMobilePeakTempo = 
            new CosemObject( "0.0.10.0.125.255", "Mobile peak tempo" )
                .add( new CosemMethod( 0xFC40, "value" )
                    .setEncoder( new Unsigned16Encoder( ) ) ); 
        add( scriptMobilePeakTempo );
                
        
        breakerUnit = 
            new CosemObject( "0.0.128.30.22.255", "Breaker unit" )
                .add( new CosemAttribute( 0x9238, "value" )
                .setEncoder( new Unsigned8Encoder() )  );
        add( breakerUnit );
        
        
        meterIdentification = 
            new CosemObject( "0.0.96.2.0.255", "Meter identification" )
                .add( new CosemAttribute( 0x4978, "value" )
                    .setParser(new MeterIdentificationParser() ) );
        add( meterIdentification );
        
        
        presetDemand = 
            new CosemObject( "0.0.16.0.1.255", "Preset demand" )
                .add( new CosemAttribute( 0x1DB8, "thresholds")
                        .setParser( new PresetParser( ) )
                        .setEncoder(new PresetEncoder( ) ) );
        add( presetDemand );
        
        
        ticConfiguration = 
            new CosemObject( "0.0.96.3.2.255", "TIC Configuration" )
                .add( new CosemAttribute( 0x5288, "value" )
                .setEncoder( new Unsigned8Encoder() ) );
        add( ticConfiguration );
        
        
        energy =
        new CosemObject( "Summa Active Energy" )
            .add( activeEnergySumm = new CosemAttribute( "1.0.1.8.0.255", 0x1778, "Summa Active Energy", wh ) )
            .add( activeEnergyTou1 = new CosemAttribute( "1.0.1.8.1.255", 0x0328, "Active import energy rate 1", wh ) )
            .add( activeEnergyTou2 = new CosemAttribute( "1.0.1.8.2.255", 0x03F0, "Active import energy rate 2", wh ) )
            .add( activeEnergyTou3 = new CosemAttribute( "1.0.1.8.3.255", 0x04B8, "Active import energy rate 3", wh ) )
            .add( activeEnergyTou4 = new CosemAttribute( "1.0.1.8.4.255", 0x0580, "Active import energy rate 4", wh ) )
            .add( activeEnergyTou5 = new CosemAttribute( "1.0.1.8.5.255", 0x0648, "Active import energy rate 5", wh ) )
            .add( activeEnergyTou6 = new CosemAttribute( "1.0.1.8.6.255", 0x0710, "Active import energy rate 6", wh ) ) ;
        add( energy );

        dailyEnergyValueProfile = 
            new CosemObject( "1.0.98.1.0.255", "Daily energy value profile" )
                .add( daylyEnergyValueProfileBuffer = new CosemAttribute( 0x6408, "buffer" ) );
        daylyEnergyValueProfileBuffer.setParser( new BillingParser(timeZone) );
        daylyEnergyValueProfileBuffer.setCollector(new DailyEnergyValueCollector());
        add( dailyEnergyValueProfile );
        
        
        loadProfileEnergy = 
            new CosemObject( "1.0.99.1.0.255", "Load profile energy" )
                .add( loadProfileBuffer = new CosemAttribute( 0x6278, "buffer" ) )
                .add( capturePeriod = new CosemAttribute( 0x6288, "capture period" ) );
        
        loadProfileBuffer.setParser( new ProfileParser( timeZone ) );
        loadProfileBuffer.setCollector(new ProfileAttributeCollector());
        
        capturePeriod.setEncoder( new Unsigned32Encoder( ) );
        capturePeriod.setParser( new CapturePeriodParser() );
        add( loadProfileEnergy );
        
        
        logBook = 
            new CosemObject( "1.0.99.98.1.255", "Log book" )
                .add( logbookBuffer = new CosemAttribute( 0x60E8, "buffer" ) );
        logbookBuffer.setParser( new LogbookParser( timeZone ) );
        logbookBuffer.setCollector(new ProfileAttributeCollector());
        add( logBook );
        
        
        thresholdForSag = 
            new CosemObject( "1.0.12.31.0.255", "Threshold for sag" )
                .add( thresholdForSagAttribute = new CosemAttribute( 0x8E68, "value" ) );
        
        add( thresholdForSag );
        
        
        thresholdForSwell =  
            new CosemObject( "1.0.12.35.0.255", "Threshold for swell" )
                .add( thresholdForSwellAttribute = new CosemAttribute( 0x8E98, "value" )) ;
        add( thresholdForSwell );
        
        
        timeIntegralForSagMeasurement = 
            new CosemObject( "1.0.12.31.129.255", "Time integral for sag measurement" )
                .add( timeIntegralForSagMeasurementAttribute = new CosemAttribute( 0x8F00, "value" ) );
        add( timeIntegralForSagMeasurement );
        
        
        timeThresholdForLongPowerFailure = 
            new CosemObject( "0.0.96.7.20.255", "Time threshold for long power failure" )
                .add( timeThresholdForLongPowerFailureAttribute = new CosemAttribute( 0x4A58, "value" ) );
        add( timeThresholdForLongPowerFailure );
        
        
        timeIntegralForInstantaneousDemand = 
            new CosemObject( "1.0.0.8.2.255", "Time integral for instantaneous demand" )
                .add( timeIntegralForInstantaneousDemandAttribute = new CosemAttribute( 0x92F8, "value" ) ); 
        add( timeIntegralForInstantaneousDemand );
        
        
        timeIntegralForSwellMeasurement = 
            new CosemObject( "1.0.12.35.129.255", "Time integral for swell measurement" )
                .add( timeIntegralForSwellMeasurementAttribute = new CosemAttribute( 0x8F50, "value") );
        add( timeIntegralForSwellMeasurement );
        
        
        numberOfLongPowerFailure = 
            new CosemObject( "0.0.96.7.5.255", "Number off long power failure" )
                .add( new CosemAttribute( 0x8BE0, "value" ) );
        add( numberOfLongPowerFailure );
        
        
        numberOfShortPowerFailure = 
            new CosemObject( "0.0.96.7.0.255", "Number of short power failure" )
                .add( new CosemAttribute( 0x8CA8, "value" ) );
        add( numberOfShortPowerFailure );
     
        
        numberOfSags = 
            new CosemObject( "1.0.12.32.0.255", "Number of sags" )
                .add( new CosemAttribute( 0x8A50, "value" ) );
        add( numberOfSags );
        
        
        numberOfSwells = 
            new CosemObject( "1.0.12.36.0.255", "Number of swells" )
                .add( new CosemAttribute( 0x8B18, "value" ) );
        add( numberOfSwells );
        
        
        maximumVoltage = 
            new CosemObject( "1.0.12.38.0.255", "Maximum voltage" ) 
                .add( new CosemAttribute( 0x87F8, "value" ) );
        add( maximumVoltage );
        
        
        minimumVoltage = 
            new CosemObject( "1.0.12.34.0.255", "Minimum voltage" )
                .add( new CosemAttribute( 0x88C0, "value" ) );
        add( minimumVoltage );
        
        
        instantaneousDemand = 
            new CosemObject( "1.0.1.7.0.255", "Instantaneous demand" )
                .add( new CosemAttribute( 0x8988, "value" ) );
        add( instantaneousDemand );
        
        
        currentRatio = 
            new CosemObject( "1.1.0.4.2.255", "Current ration" )
                .add( new CosemAttribute( 0x52A0,  "value" ) );
        add( currentRatio );
        
        
        
    }
    
    private CosemObject add( CosemObject register ) {
        
        all.add( register );
        
        if( register.getObisCode() != null )
            obisCodes.put( register.getObisCode(), register );
        
        Iterator ai = register.getAttributes().iterator();
        while( ai.hasNext() ) {
            CosemAttribute attribute = (CosemAttribute) ai.next();
            allAttributes.add( attribute );
        }
        
        return register;
    }
    
    
    Cosem findByShortName( int iShortName ) {
        
        Iterator i = all.iterator();
        while( i.hasNext() ) {
            CosemObject register = (CosemObject)i.next();
            
            if( register.getShortName() == iShortName )
                return register;
            
            Iterator ai = register.getAttributes().iterator();
            while( ai.hasNext() ) {
                CosemAttribute attribute = (CosemAttribute) ai.next();
                if( attribute.getShortName() == iShortName )
                    return attribute;
            }
            
            Iterator mi = register.getMethods().iterator();
            while( ai.hasNext() ) {
                CosemMethod method = (CosemMethod) mi.next();
                if( method.getShortName() == iShortName )
                    return method;
            }
                
        }
        
        return null;
        
    }
    
    CosemAttribute findAttributesByShortName( int shortName ) {
        
        Iterator i = all.iterator();
        while( i.hasNext() ) {
            CosemObject register = (CosemObject)i.next();
            
            Iterator ai = register.getAttributes().iterator();
            while( ai.hasNext() ) {
                CosemAttribute attribute = (CosemAttribute) ai.next();
                if( attribute.getShortName() == shortName )
                    return attribute;
            }
            
        }
        
        return null;
        
    }
    
    Cosem findByObisCode(ObisCode obisCode) {
        
        Iterator i = all.iterator();
        while( i.hasNext() ) {
            CosemObject register = (CosemObject)i.next();
            ObisCode rObis = register.getObisCode();
            
            if( rObis != null && rObis.equals( obisCode ) )
                return register;
         
            
            Iterator ai = register.getAttributes().iterator();
            while( ai.hasNext() ){
                CosemAttribute cosemAttrib = (CosemAttribute)ai.next();
                ObisCode aObis = cosemAttrib.getObisCode();
                
                if( aObis != null && aObis.equals( obisCode ) )
                    return cosemAttrib;
             
            }
        } 
        
        return null;
        
    }
    
    Cosem findByObisCode(String obisCode) {
        return findByObisCode( ObisCode.fromString(obisCode) );
        
    }
    
    Collection allAttributes( ) {
        return allAttributes;
    }
    

    List getAll() {
        return all;
    }


    Map getObisCodes() {
        return obisCodes;
    }


    CosemObject getClock() {
        return clock;
    }


    CosemObject getMeterStatus() {
        return meterStatus;
    }


    CosemObject getErrorCodeRegister() {
        return errorCodeRegister;
    }


    public CosemObject getActivityCalendar() { 
        return activityCalendar;
    }


    CosemObject getScriptMobilePeakTempo() {
        return scriptMobilePeakTempo;
    }


    CosemObject getBreakerUnit() {
        return breakerUnit;
    }


    CosemObject getMeterIdentification() {
        return meterIdentification;
    }


    CosemObject getPresetDemand() {
        return presetDemand;
    }


    CosemObject getTicConfiguration() {
        return ticConfiguration;
    }

    CosemObject getEnergy( ) {
        return energy;
    }

    CosemObject getDailyEnergyValueProfile() {
        return dailyEnergyValueProfile;
    }


    CosemObject getLoadProfileEnergy() {
        return loadProfileEnergy;
    }


    CosemObject getLogBook() {
        return logBook;
    }


    CosemObject getThresholdForSag() {
        return thresholdForSag;
    }


    CosemObject getThresholdForSwell() {
        return thresholdForSwell;
    }


    CosemObject getTimeIntegralForSagMeasurement() {
        return timeIntegralForSagMeasurement;
    }


    CosemObject getTimeThresholdForLongPowerFailure() {
        return timeThresholdForLongPowerFailure;
    }


    CosemObject getTimeIntegralForInstantaneousDemand() {
        return timeIntegralForInstantaneousDemand;
    }


    CosemObject getTimeIntegralForSwellMeasurement() {
        return timeIntegralForSwellMeasurement;
    }


    CosemObject getNumberOfLongPowerFailure() {
        return numberOfLongPowerFailure;
    }


    CosemObject getNumberOfShortPowerFailure() {
        return numberOfShortPowerFailure;
    }


    CosemObject getNumberOfSags() {
        return numberOfSags;
    }


    CosemObject getNumberOfSwells() {
        return numberOfSwells;
    }


    CosemObject getMaximumVoltage() {
        return maximumVoltage;
    }


    CosemObject getMinimumVoltage() {
        return minimumVoltage;
    }


    CosemObject getInstantaneousDemand() {
        return instantaneousDemand;
    }
    
    
    CosemAttribute getDaylyEnergyValueProfileBuffer( ){
        return daylyEnergyValueProfileBuffer;
    }
    
    CosemAttribute getLoadProfileBuffer( ) {
        return loadProfileBuffer;
    }

    CosemAttribute getCapturePeriod( ) {
        return capturePeriod;
    }
    
    CosemAttribute getLogbookBuffer() {
        return logbookBuffer;
    }

    CosemAttribute getActiveEnergySumm() {
        return activeEnergySumm;
    }

    CosemAttribute getActiveEnergyTou1() {
        return activeEnergyTou1;
    }

    CosemAttribute getActiveEnergyTou2() {
        return activeEnergyTou2;
    }

    CosemAttribute getActiveEnergyTou3() {
        return activeEnergyTou3;
    }

    CosemAttribute getActiveEnergyTou4() {
        return activeEnergyTou4;
    }

    CosemAttribute getActiveEnergyTou5() {
        return activeEnergyTou5;
    }

    CosemAttribute getActiveEnergyTou6() {
        return activeEnergyTou6;
    }

    CosemAttribute getThresholdForSagAttribute() {
        return thresholdForSagAttribute;
    }

    CosemAttribute getThresholdForSwellAttribute() {
        return thresholdForSwellAttribute;
    }

    CosemAttribute getTimeIntegralForSagMeasurementAttribute() {
        return timeIntegralForSagMeasurementAttribute;
    }

    CosemAttribute getTimeThresholdForLongPowerFailureAttribute() {
        return timeThresholdForLongPowerFailureAttribute;
    }

    CosemAttribute getTimeIntegralForInstantaneousDemandAttribute() {
        return timeIntegralForInstantaneousDemandAttribute;
    }

    CosemAttribute getTimeIntegralForSwellMeasurementAttribute() {
        return timeIntegralForSwellMeasurementAttribute;
    }
    
}
