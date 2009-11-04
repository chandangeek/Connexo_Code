package com.energyict.protocolimpl.powermeasurement.ion;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IonHandle {

    private int key;
    private ByteArray byteArray;
    private String description;
    private IonHandle module;
    
    private static Map knownHandles = new HashMap( );
    
    /** Create an IonHandle for an id.  This does not have to be a handle 
     * that is know by the protocol.  
     * @param id 
     * @return ionHandle object
     */
    static IonHandle create( int id ){
        return new IonHandle( id );
    }
    
    /** private way to create a Handle and add it to the known handles */
    private static IonHandle create( int id, String description ){
        IonHandle h = new IonHandle( id );
        h.description = description;
        knownHandles.put( new Integer(h.key), h );
        return h;
    }

    /** private way to create a Handle and add it to the known handles */
    private static IonHandle create( int id, String description, IonHandle module ){
        IonHandle h = new IonHandle( id );
        h.description = description;
        h.module = module;
        knownHandles.put( new Integer(h.key), h );
        return h;
    }
    
    private IonHandle( int key ) {
        this.key = key;
        byte b1 = (byte)(( key & 0x0000ff00 ) >> 8 );
        byte b2 = (byte)( key & 0x000000ff );
        this.byteArray = new ByteArray( new byte [] { b1, b2 } ); 
    }
    
    static IonHandle get( int key ) {
        return (IonHandle)knownHandles.get( new Integer(key) );
    }
    
    IonHandle getModule( ){
        return module;
    }
    
    ByteArray toByteArray( ){
        return byteArray;
    }
    
    static Collection getMax( ){
        return max;
    }
    
    static Collection getMin( ){
        return min;
    }
    
    static Collection getInt( ){
        return integrator;
    }
    
    public String toString( ){
        return new StringBuffer( )
        .append( "IonHandle [ "  )
        .append( byteArray.toHexaString( true ) )
        .append( ", " )
        .append( description )
        .append( "]" ).toString();
    }
    
    static final IonHandle FEATURE_MANAGER =
        create( 0x0002, "Feature manager" );
    
    static final IonHandle CLK_1 = 
        create( 0x1580, "CLK #1" );
    static final IonHandle CLK_1_UNIVERSAL_TIME_NVR = 
        create( 0x5B7C, "Universal Time (NVR)", CLK_1 );
    static final IonHandle CLK_1_DST_FLAG_BVR = 
        create( 0x6113, "DST Flag (BVR)", CLK_1 );
    static final IonHandle CLK_1_YEAR_NVR = 
        create( 0x3E97, "Year (NVR)", CLK_1 );
    static final IonHandle CLK_1_DAY_NVR = 
        create( 0x3E99, "Day (NVR)", CLK_1 );
    static final IonHandle CLK_1_MINUTE_NVR = 
        create( 0x3E9B, "Minute (NVR)", CLK_1 );
    static final IonHandle CLK_1_DAYOFWEEK_NVR = 
        create( 0x3E9D, "DayofWeek (NVR)", CLK_1 );
    static final IonHandle CLK_1_DST_START_NBR = 
        create( 0x722F, "DST Start (NBR)", CLK_1 );
    static final IonHandle CLK_1_DST_OFFSET_NBR = 
        create( 0x7231, "DST Offset (NBR)", CLK_1 );
    static final IonHandle CLK_1_TIMESYNCTYPE_ENR = 
        create( 0x7BB8, "TimeSyncType (ENR)", CLK_1 );
    static final IonHandle CLK_1_NEW_YEAR_PR = 
        create( 0x6FF6, "New Year (PR)", CLK_1 );
    static final IonHandle CLK_1_NEW_DAY_PR = 
        create( 0x6FF8, "New Day (PR)", CLK_1 );
    static final IonHandle CLK_1_NEW_MINUTE_PR = 
        create( 0x6FFA, "New Minute (PR)", CLK_1 );
    static final IonHandle  TOU_1 = 
        create( 0x1F00, "TOU 1" );
    static final IonHandle TOU_1_RATE_A_STATUS_BR = 
        create( 0x6234, "Rate A Status (BR)", TOU_1 );
    static final IonHandle TOU_1_RATE_C_STATUS_BR = 
        create( 0x6244, "Rate C Status (BR)", TOU_1 );
    static final IonHandle TOU_1_RATE_CHANGE_PR = 
        create( 0x6F26, "Rate Change (PR)", TOU_1 );
    static final IonHandle TOU_1_SEASON_2_STATUS_BR = 
        create( 0x625C, "Season 2 Status (BR)", TOU_1 );
    static final IonHandle TOU_1_SEASON_4_STATUS_BR = 
        create( 0x626C, "Season 4 Status (BR)", TOU_1 );
    static final IonHandle TOU_1_WEEKDAY_STATUS_BR = 
        create( 0x62AE, "Weekday Status (BR)", TOU_1 );
    static final IonHandle TOU_1_ALTERNATIVE_1_STATUS_BR = 
        create( 0x62BE, "Alternative 1 Status (BR)", TOU_1 );
    static final IonHandle TOU_1_HOLIDAY_STATUS_BR = 
        create( 0x62CE, "Holiday Status (BR)", TOU_1 );
    static final IonHandle TOU_1_MONDAY_STATUS_BR = 
        create( 0x62D6, "Monday Status (BR)", TOU_1 );
    static final IonHandle TOU_1_WEDNESDAY_STATUS_BR = 
        create( 0x62E6, "Wednesday Status (BR)", TOU_1 );
    static final IonHandle TOU_1_FRIDAY_STATUS_BR = 
        create( 0x62F6, "Friday Status (BR)", TOU_1 );
    static final IonHandle TOU_1_SUNDAY_STATUS_BR = 
        create( 0x6306, "Sunday Status (BR)", TOU_1 );
    static final IonHandle TOU_1_SELF_READ_PR = 
        create( 0x6DAE, "Self Read (PR)", TOU_1 );
    static final IonHandle TOU_1_SEASON_1_SR = 
        create( 0x50AF, "Season 1 (SR)", TOU_1 );
    static final IonHandle TOU_1_SEASON_1_WEEKEND_SR = 
        create( 0x50D7, "Season 1 Weekend (SR)", TOU_1 );
    static final IonHandle TOU_1_SEASON_1_ALTERNATIVE_2_SR = 
        create( 0x50E7, "Season 1 Alternative 2 (SR)", TOU_1 );
    static final IonHandle TOU_1_SEASON_2_SR = 
        create( 0x50B7, "Season 2 (SR)", TOU_1 );
    static final IonHandle TOU_1_SEASON_2_WEEKEND_SR = 
        create( 0x50FF, "Season 2 Weekend (SR)", TOU_1 );
    static final IonHandle TOU_1_SEASON_2_ALTERNATIVE_2_SR = 
        create( 0x510F, "Season 2 Alternative 2 (SR)", TOU_1 );
    static final IonHandle TOU_1_SEASON_3_SR = 
        create( 0x50BF, "Season 3 (SR)", TOU_1 );
    static final IonHandle TOU_1_SEASON_3_WEEKEND_SR = 
        create( 0x5127, "Season 3 Weekend (SR)", TOU_1 );
    static final IonHandle TOU_1_SEASON_3_ALTERNATIVE_2_SR = 
        create( 0x5137, "Season 3 Alternative 2 (SR)", TOU_1 );
    static final IonHandle TOU_1_SEASON_4_SR = 
        create( 0x50C7, "Season 4 (SR)", TOU_1 );
    static final IonHandle TOU_1_SEASON_4_WEEKEND_SR = 
        create( 0x514F, "Season 4 Weekend (SR)", TOU_1 );
    static final IonHandle TOU_1_SEASON_4_ALTERNATIVE_2_SR = 
        create( 0x515F, "Season 4 Alternative 2 (SR)", TOU_1 );
    static final IonHandle TOU_1_WEEKDAY_DAYS_SR = 
        create( 0x516F, "Weekday Days (SR)", TOU_1 );
    static final IonHandle TOU_1_ALTERNATIVE_1_DATES_SR = 
        create( 0x517F, "Alternative 1 Dates (SR)", TOU_1 );
    static final IonHandle TOU_1_HOLIDAY_DATES_SR = 
        create( 0x518F, "Holiday Dates (SR)", TOU_1 );
    static final IonHandle  TOU_2 = 
        create( 0x1F01, "TOU 2" );
    static final IonHandle TOU_2_RATE_A_STATUS_BR = 
        create( 0x6235, "Rate A Status (BR)", TOU_2 );
    static final IonHandle TOU_2_RATE_C_STATUS_BR = 
        create( 0x6245, "Rate C Status (BR)", TOU_2 );
    static final IonHandle TOU_2_RATE_CHANGE_PR = 
        create( 0x6F27, "Rate Change (PR)", TOU_2 );
    static final IonHandle TOU_2_SEASON_2_STATUS_BR = 
        create( 0x625D, "Season 2 Status (BR)", TOU_2 );
    static final IonHandle TOU_2_SEASON_4_STATUS_BR = 
        create( 0x626D, "Season 4 Status (BR)", TOU_2 );
    static final IonHandle TOU_2_WEEKDAY_STATUS_BR = 
        create( 0x62AF, "Weekday Status (BR)", TOU_2 );
    static final IonHandle TOU_2_ALTERNATIVE_1_STATUS_BR = 
        create( 0x62BF, "Alternative 1 Status (BR)", TOU_2 );
    static final IonHandle TOU_2_HOLIDAY_STATUS_BR = 
        create( 0x62CF, "Holiday Status (BR)", TOU_2 );
    static final IonHandle TOU_2_MONDAY_STATUS_BR = 
        create( 0x62D7, "Monday Status (BR)", TOU_2 );
    static final IonHandle TOU_2_WEDNESDAY_STATUS_BR = 
        create( 0x62E7, "Wednesday Status (BR)", TOU_2 );
    static final IonHandle TOU_2_FRIDAY_STATUS_BR = 
        create( 0x62F7, "Friday Status (BR)", TOU_2 );
    static final IonHandle TOU_2_SUNDAY_STATUS_BR = 
        create( 0x6307, "Sunday Status (BR)", TOU_2 );
    static final IonHandle TOU_2_SELF_READ_PR = 
        create( 0x6DAF, "Self Read (PR)", TOU_2 );
    static final IonHandle TOU_2_SEASON_1_SR = 
        create( 0x50B0, "Season 1 (SR)", TOU_2 );
    static final IonHandle TOU_2_SEASON_1_WEEKEND_SR = 
        create( 0x50D8, "Season 1 Weekend (SR)", TOU_2 );
    static final IonHandle TOU_2_SEASON_1_ALTERNATIVE_2_SR = 
        create( 0x50E8, "Season 1 Alternative 2 (SR)", TOU_2 );
    static final IonHandle TOU_2_SEASON_2_SR = 
        create( 0x50B8, "Season 2 (SR)", TOU_2 );
    static final IonHandle TOU_2_SEASON_2_WEEKEND_SR = 
        create( 0x5100, "Season 2 Weekend (SR)", TOU_2 );
    static final IonHandle TOU_2_SEASON_2_ALTERNATIVE_2_SR = 
        create( 0x5110, "Season 2 Alternative 2 (SR)", TOU_2 );
    static final IonHandle TOU_2_SEASON_3_SR = 
        create( 0x50C0, "Season 3 (SR)", TOU_2 );
    static final IonHandle TOU_2_SEASON_3_WEEKEND_SR = 
        create( 0x5128, "Season 3 Weekend (SR)", TOU_2 );
    static final IonHandle TOU_2_SEASON_3_ALTERNATIVE_2_SR = 
        create( 0x5138, "Season 3 Alternative 2 (SR)", TOU_2 );
    static final IonHandle TOU_2_SEASON_4_SR = 
        create( 0x50C8, "Season 4 (SR)", TOU_2 );
    static final IonHandle TOU_2_SEASON_4_WEEKEND_SR = 
        create( 0x5150, "Season 4 Weekend (SR)", TOU_2 );
    static final IonHandle TOU_2_SEASON_4_ALTERNATIVE_2_SR = 
        create( 0x5160, "Season 4 Alternative 2 (SR)", TOU_2 );
    static final IonHandle TOU_2_WEEKDAY_DAYS_SR = 
        create( 0x5170, "Weekday Days (SR)", TOU_2 );
    static final IonHandle TOU_2_ALTERNATIVE_1_DATES_SR = 
        create( 0x5180, "Alternative 1 Dates (SR)", TOU_2 );
    static final IonHandle TOU_2_HOLIDAY_DATES_SR = 
        create( 0x5190, "Holiday Dates (SR)", TOU_2 );
    static final IonHandle  TOU_3 = 
        create( 0x1F02, "TOU 3" );
    static final IonHandle TOU_3_RATE_A_STATUS_BR = 
        create( 0x6236, "Rate A Status (BR)", TOU_3 );
    static final IonHandle TOU_3_RATE_C_STATUS_BR = 
        create( 0x6246, "Rate C Status (BR)", TOU_3 );
    static final IonHandle TOU_3_RATE_CHANGE_PR = 
        create( 0x6F28, "Rate Change (PR)", TOU_3 );
    static final IonHandle TOU_3_SEASON_2_STATUS_BR = 
        create( 0x625E, "Season 2 Status (BR)", TOU_3 );
    static final IonHandle TOU_3_SEASON_4_STATUS_BR = 
        create( 0x626E, "Season 4 Status (BR)", TOU_3 );
    static final IonHandle TOU_3_WEEKDAY_STATUS_BR = 
        create( 0x62B0, "Weekday Status (BR)", TOU_3 );
    static final IonHandle TOU_3_ALTERNATIVE_1_STATUS_BR = 
        create( 0x62C0, "Alternative 1 Status (BR)", TOU_3 );
    static final IonHandle TOU_3_HOLIDAY_STATUS_BR = 
        create( 0x62D0, "Holiday Status (BR)", TOU_3 );
    static final IonHandle TOU_3_MONDAY_STATUS_BR = 
        create( 0x62D8, "Monday Status (BR)", TOU_3 );
    static final IonHandle TOU_3_WEDNESDAY_STATUS_BR = 
        create( 0x62E8, "Wednesday Status (BR)", TOU_3 );
    static final IonHandle TOU_3_FRIDAY_STATUS_BR = 
        create( 0x62F8, "Friday Status (BR)", TOU_3 );
    static final IonHandle TOU_3_SUNDAY_STATUS_BR = 
        create( 0x6308, "Sunday Status (BR)", TOU_3 );
    static final IonHandle TOU_3_SELF_READ_PR = 
        create( 0x6DB0, "Self Read (PR)", TOU_3 );
    static final IonHandle TOU_3_SEASON_1_SR = 
        create( 0x50B1, "Season 1 (SR)", TOU_3 );
    static final IonHandle TOU_3_SEASON_1_WEEKEND_SR = 
        create( 0x50D9, "Season 1 Weekend (SR)", TOU_3 );
    static final IonHandle TOU_3_SEASON_1_ALTERNATIVE_2_SR = 
        create( 0x50E9, "Season 1 Alternative 2 (SR)", TOU_3 );
    static final IonHandle TOU_3_SEASON_2_SR = 
        create( 0x50B9, "Season 2 (SR)", TOU_3 );
    static final IonHandle TOU_3_SEASON_2_WEEKEND_SR = 
        create( 0x5101, "Season 2 Weekend (SR)", TOU_3 );
    static final IonHandle TOU_3_SEASON_2_ALTERNATIVE_2_SR = 
        create( 0x5111, "Season 2 Alternative 2 (SR)", TOU_3 );
    static final IonHandle TOU_3_SEASON_3_SR = 
        create( 0x50C1, "Season 3 (SR)", TOU_3 );
    static final IonHandle TOU_3_SEASON_3_WEEKEND_SR = 
        create( 0x5129, "Season 3 Weekend (SR)", TOU_3 );
    static final IonHandle TOU_3_SEASON_3_ALTERNATIVE_2_SR = 
        create( 0x5139, "Season 3 Alternative 2 (SR)", TOU_3 );
    static final IonHandle TOU_3_SEASON_4_SR = 
        create( 0x50C9, "Season 4 (SR)", TOU_3 );
    static final IonHandle TOU_3_SEASON_4_WEEKEND_SR = 
        create( 0x5151, "Season 4 Weekend (SR)", TOU_3 );
    static final IonHandle TOU_3_SEASON_4_ALTERNATIVE_2_SR = 
        create( 0x5161, "Season 4 Alternative 2 (SR)", TOU_3 );
    static final IonHandle TOU_3_WEEKDAY_DAYS_SR = 
        create( 0x5171, "Weekday Days (SR)", TOU_3 );
    static final IonHandle TOU_3_ALTERNATIVE_1_DATES_SR = 
        create( 0x5181, "Alternative 1 Dates (SR)", TOU_3 );
    static final IonHandle TOU_3_HOLIDAY_DATES_SR = 
        create( 0x5191, "Holiday Dates (SR)", TOU_3 );
    static final IonHandle  TOU_4 = 
        create( 0x1F03, "TOU 4" );
    static final IonHandle TOU_4_RATE_A_STATUS_BR = 
        create( 0x6237, "Rate A Status (BR)", TOU_4 );
    static final IonHandle TOU_4_RATE_C_STATUS_BR = 
        create( 0x6247, "Rate C Status (BR)", TOU_4 );
    static final IonHandle TOU_4_RATE_CHANGE_PR = 
        create( 0x6F29, "Rate Change (PR)", TOU_4 );
    static final IonHandle TOU_4_SEASON_2_STATUS_BR = 
        create( 0x625F, "Season 2 Status (BR)", TOU_4 );
    static final IonHandle TOU_4_SEASON_4_STATUS_BR = 
        create( 0x626F, "Season 4 Status (BR)", TOU_4 );
    static final IonHandle TOU_4_WEEKDAY_STATUS_BR = 
        create( 0x62B1, "Weekday Status (BR)", TOU_4 );
    static final IonHandle TOU_4_ALTERNATIVE_1_STATUS_BR = 
        create( 0x62C1, "Alternative 1 Status (BR)", TOU_4 );
    static final IonHandle TOU_4_HOLIDAY_STATUS_BR = 
        create( 0x62D1, "Holiday Status (BR)", TOU_4 );
    static final IonHandle TOU_4_MONDAY_STATUS_BR = 
        create( 0x62D9, "Monday Status (BR)", TOU_4 );
    static final IonHandle TOU_4_WEDNESDAY_STATUS_BR = 
        create( 0x62E9, "Wednesday Status (BR)", TOU_4 );
    static final IonHandle TOU_4_FRIDAY_STATUS_BR = 
        create( 0x62F9, "Friday Status (BR)", TOU_4 );
    static final IonHandle TOU_4_SUNDAY_STATUS_BR = 
        create( 0x6309, "Sunday Status (BR)", TOU_4 );
    static final IonHandle TOU_4_SELF_READ_PR = 
        create( 0x6DB1, "Self Read (PR)", TOU_4 );
    static final IonHandle TOU_4_SEASON_1_SR = 
        create( 0x50B2, "Season 1 (SR)", TOU_4 );
    static final IonHandle TOU_4_SEASON_1_WEEKEND_SR = 
        create( 0x50DA, "Season 1 Weekend (SR)", TOU_4 );
    static final IonHandle TOU_4_SEASON_1_ALTERNATIVE_2_SR = 
        create( 0x50EA, "Season 1 Alternative 2 (SR)", TOU_4 );
    static final IonHandle TOU_4_SEASON_2_SR = 
        create( 0x50BA, "Season 2 (SR)", TOU_4 );
    static final IonHandle TOU_4_SEASON_2_WEEKEND_SR = 
        create( 0x5102, "Season 2 Weekend (SR)", TOU_4 );
    static final IonHandle TOU_4_SEASON_2_ALTERNATIVE_2_SR = 
        create( 0x5112, "Season 2 Alternative 2 (SR)", TOU_4 );
    static final IonHandle TOU_4_SEASON_3_SR = 
        create( 0x50C2, "Season 3 (SR)", TOU_4 );
    static final IonHandle TOU_4_SEASON_3_WEEKEND_SR = 
        create( 0x512A, "Season 3 Weekend (SR)", TOU_4 );
    static final IonHandle TOU_4_SEASON_3_ALTERNATIVE_2_SR = 
        create( 0x513A, "Season 3 Alternative 2 (SR)", TOU_4 );
    static final IonHandle TOU_4_SEASON_4_SR = 
        create( 0x50CA, "Season 4 (SR)", TOU_4 );
    static final IonHandle TOU_4_SEASON_4_WEEKEND_SR = 
        create( 0x5152, "Season 4 Weekend (SR)", TOU_4 );
    static final IonHandle TOU_4_SEASON_4_ALTERNATIVE_2_SR = 
        create( 0x5162, "Season 4 Alternative 2 (SR)", TOU_4 );
    static final IonHandle TOU_4_WEEKDAY_DAYS_SR = 
        create( 0x5172, "Weekday Days (SR)", TOU_4 );
    static final IonHandle TOU_4_ALTERNATIVE_1_DATES_SR = 
        create( 0x5182, "Alternative 1 Dates (SR)", TOU_4 );
    static final IonHandle TOU_4_HOLIDAY_DATES_SR = 
        create( 0x5192, "Holiday Dates (SR)", TOU_4 );
    static final IonHandle  TOU_5 = 
        create( 0x1F04, "TOU 5" );
    static final IonHandle TOU_5_RATE_A_STATUS_BR = 
        create( 0x6238, "Rate A Status (BR)", TOU_5 );
    static final IonHandle TOU_5_RATE_C_STATUS_BR = 
        create( 0x6248, "Rate C Status (BR)", TOU_5 );
    static final IonHandle TOU_5_RATE_CHANGE_PR = 
        create( 0x6F2A, "Rate Change (PR)", TOU_5 );
    static final IonHandle TOU_5_SEASON_2_STATUS_BR = 
        create( 0x6260, "Season 2 Status (BR)", TOU_5 );
    static final IonHandle TOU_5_SEASON_4_STATUS_BR = 
        create( 0x6270, "Season 4 Status (BR)", TOU_5 );
    static final IonHandle TOU_5_WEEKDAY_STATUS_BR = 
        create( 0x62B2, "Weekday Status (BR)", TOU_5 );
    static final IonHandle TOU_5_ALTERNATIVE_1_STATUS_BR = 
        create( 0x62C2, "Alternative 1 Status (BR)", TOU_5 );
    static final IonHandle TOU_5_HOLIDAY_STATUS_BR = 
        create( 0x62D2, "Holiday Status (BR)", TOU_5 );
    static final IonHandle TOU_5_MONDAY_STATUS_BR = 
        create( 0x62DA, "Monday Status (BR)", TOU_5 );
    static final IonHandle TOU_5_WEDNESDAY_STATUS_BR = 
        create( 0x62EA, "Wednesday Status (BR)", TOU_5 );
    static final IonHandle TOU_5_FRIDAY_STATUS_BR = 
        create( 0x62FA, "Friday Status (BR)", TOU_5 );
    static final IonHandle TOU_5_SUNDAY_STATUS_BR = 
        create( 0x630A, "Sunday Status (BR)", TOU_5 );
    static final IonHandle TOU_5_SELF_READ_PR = 
        create( 0x6DB2, "Self Read (PR)", TOU_5 );
    static final IonHandle TOU_5_SEASON_1_SR = 
        create( 0x50B3, "Season 1 (SR)", TOU_5 );
    static final IonHandle TOU_5_SEASON_1_WEEKEND_SR = 
        create( 0x50DB, "Season 1 Weekend (SR)", TOU_5 );
    static final IonHandle TOU_5_SEASON_1_ALTERNATIVE_2_SR = 
        create( 0x50EB, "Season 1 Alternative 2 (SR)", TOU_5 );
    static final IonHandle TOU_5_SEASON_2_SR = 
        create( 0x50BB, "Season 2 (SR)", TOU_5 );
    static final IonHandle TOU_5_SEASON_2_WEEKEND_SR = 
        create( 0x5103, "Season 2 Weekend (SR)", TOU_5 );
    static final IonHandle TOU_5_SEASON_2_ALTERNATIVE_2_SR = 
        create( 0x5113, "Season 2 Alternative 2 (SR)", TOU_5 );
    static final IonHandle TOU_5_SEASON_3_SR = 
        create( 0x50C3, "Season 3 (SR)", TOU_5 );
    static final IonHandle TOU_5_SEASON_3_WEEKEND_SR = 
        create( 0x512B, "Season 3 Weekend (SR)", TOU_5 );
    static final IonHandle TOU_5_SEASON_3_ALTERNATIVE_2_SR = 
        create( 0x513B, "Season 3 Alternative 2 (SR)", TOU_5 );
    static final IonHandle TOU_5_SEASON_4_SR = 
        create( 0x50CB, "Season 4 (SR)", TOU_5 );
    static final IonHandle TOU_5_SEASON_4_WEEKEND_SR = 
        create( 0x5153, "Season 4 Weekend (SR)", TOU_5 );
    static final IonHandle TOU_5_SEASON_4_ALTERNATIVE_2_SR = 
        create( 0x5163, "Season 4 Alternative 2 (SR)", TOU_5 );
    static final IonHandle TOU_5_WEEKDAY_DAYS_SR = 
        create( 0x5173, "Weekday Days (SR)", TOU_5 );
    static final IonHandle TOU_5_ALTERNATIVE_1_DATES_SR = 
        create( 0x5183, "Alternative 1 Dates (SR)", TOU_5 );
    static final IonHandle TOU_5_HOLIDAY_DATES_SR = 
        create( 0x5193, "Holiday Dates (SR)", TOU_5 );
    static final IonHandle  TOU_6 = 
        create( 0x1F05, "TOU 6" );
    static final IonHandle TOU_6_RATE_A_STATUS_BR = 
        create( 0x6239, "Rate A Status (BR)", TOU_6 );
    static final IonHandle TOU_6_RATE_C_STATUS_BR = 
        create( 0x6249, "Rate C Status (BR)", TOU_6 );
    static final IonHandle TOU_6_RATE_CHANGE_PR = 
        create( 0x6F2B, "Rate Change (PR)", TOU_6 );
    static final IonHandle TOU_6_SEASON_2_STATUS_BR = 
        create( 0x6261, "Season 2 Status (BR)", TOU_6 );
    static final IonHandle TOU_6_SEASON_4_STATUS_BR = 
        create( 0x6271, "Season 4 Status (BR)", TOU_6 );
    static final IonHandle TOU_6_WEEKDAY_STATUS_BR = 
        create( 0x62B3, "Weekday Status (BR)", TOU_6 );
    static final IonHandle TOU_6_ALTERNATIVE_1_STATUS_BR = 
        create( 0x62C3, "Alternative 1 Status (BR)", TOU_6 );
    static final IonHandle TOU_6_HOLIDAY_STATUS_BR = 
        create( 0x62D3, "Holiday Status (BR)", TOU_6 );
    static final IonHandle TOU_6_MONDAY_STATUS_BR = 
        create( 0x62DB, "Monday Status (BR)", TOU_6 );
    static final IonHandle TOU_6_WEDNESDAY_STATUS_BR = 
        create( 0x62EB, "Wednesday Status (BR)", TOU_6 );
    static final IonHandle TOU_6_FRIDAY_STATUS_BR = 
        create( 0x62FB, "Friday Status (BR)", TOU_6 );
    static final IonHandle TOU_6_SUNDAY_STATUS_BR = 
        create( 0x630B, "Sunday Status (BR)", TOU_6 );
    static final IonHandle TOU_6_SELF_READ_PR = 
        create( 0x6DB3, "Self Read (PR)", TOU_6 );
    static final IonHandle TOU_6_SEASON_1_SR = 
        create( 0x50B4, "Season 1 (SR)", TOU_6 );
    static final IonHandle TOU_6_SEASON_1_WEEKEND_SR = 
        create( 0x50DC, "Season 1 Weekend (SR)", TOU_6 );
    static final IonHandle TOU_6_SEASON_1_ALTERNATIVE_2_SR = 
        create( 0x50EC, "Season 1 Alternative 2 (SR)", TOU_6 );
    static final IonHandle TOU_6_SEASON_2_SR = 
        create( 0x50BC, "Season 2 (SR)", TOU_6 );
    static final IonHandle TOU_6_SEASON_2_WEEKEND_SR = 
        create( 0x5104, "Season 2 Weekend (SR)", TOU_6 );
    static final IonHandle TOU_6_SEASON_2_ALTERNATIVE_2_SR = 
        create( 0x5114, "Season 2 Alternative 2 (SR)", TOU_6 );
    static final IonHandle TOU_6_SEASON_3_SR = 
        create( 0x50C4, "Season 3 (SR)", TOU_6 );
    static final IonHandle TOU_6_SEASON_3_WEEKEND_SR = 
        create( 0x512C, "Season 3 Weekend (SR)", TOU_6 );
    static final IonHandle TOU_6_SEASON_3_ALTERNATIVE_2_SR = 
        create( 0x513C, "Season 3 Alternative 2 (SR)", TOU_6 );
    static final IonHandle TOU_6_SEASON_4_SR = 
        create( 0x50CC, "Season 4 (SR)", TOU_6 );
    static final IonHandle TOU_6_SEASON_4_WEEKEND_SR = 
        create( 0x5154, "Season 4 Weekend (SR)", TOU_6 );
    static final IonHandle TOU_6_SEASON_4_ALTERNATIVE_2_SR = 
        create( 0x5164, "Season 4 Alternative 2 (SR)", TOU_6 );
    static final IonHandle TOU_6_WEEKDAY_DAYS_SR = 
        create( 0x5174, "Weekday Days (SR)", TOU_6 );
    static final IonHandle TOU_6_ALTERNATIVE_1_DATES_SR = 
        create( 0x5184, "Alternative 1 Dates (SR)", TOU_6 );
    static final IonHandle TOU_6_HOLIDAY_DATES_SR = 
        create( 0x5194, "Holiday Dates (SR)", TOU_6 );
    static final IonHandle  TOU_7 = 
        create( 0x1F06, "TOU 7" );
    static final IonHandle TOU_7_RATE_A_STATUS_BR = 
        create( 0x623A, "Rate A Status (BR)", TOU_7 );
    static final IonHandle TOU_7_RATE_C_STATUS_BR = 
        create( 0x624A, "Rate C Status (BR)", TOU_7 );
    static final IonHandle TOU_7_RATE_CHANGE_PR = 
        create( 0x6F2C, "Rate Change (PR)", TOU_7 );
    static final IonHandle TOU_7_SEASON_2_STATUS_BR = 
        create( 0x6262, "Season 2 Status (BR)", TOU_7 );
    static final IonHandle TOU_7_SEASON_4_STATUS_BR = 
        create( 0x6272, "Season 4 Status (BR)", TOU_7 );
    static final IonHandle TOU_7_WEEKDAY_STATUS_BR = 
        create( 0x62B4, "Weekday Status (BR)", TOU_7 );
    static final IonHandle TOU_7_ALTERNATIVE_1_STATUS_BR = 
        create( 0x62C4, "Alternative 1 Status (BR)", TOU_7 );
    static final IonHandle TOU_7_HOLIDAY_STATUS_BR = 
        create( 0x62D4, "Holiday Status (BR)", TOU_7 );
    static final IonHandle TOU_7_MONDAY_STATUS_BR = 
        create( 0x62DC, "Monday Status (BR)", TOU_7 );
    static final IonHandle TOU_7_WEDNESDAY_STATUS_BR = 
        create( 0x62EC, "Wednesday Status (BR)", TOU_7 );
    static final IonHandle TOU_7_FRIDAY_STATUS_BR = 
        create( 0x62FC, "Friday Status (BR)", TOU_7 );
    static final IonHandle TOU_7_SUNDAY_STATUS_BR = 
        create( 0x630C, "Sunday Status (BR)", TOU_7 );
    static final IonHandle TOU_7_SELF_READ_PR = 
        create( 0x6DB4, "Self Read (PR)", TOU_7 );
    static final IonHandle TOU_7_SEASON_1_SR = 
        create( 0x50B5, "Season 1 (SR)", TOU_7 );
    static final IonHandle TOU_7_SEASON_1_WEEKEND_SR = 
        create( 0x50DD, "Season 1 Weekend (SR)", TOU_7 );
    static final IonHandle TOU_7_SEASON_1_ALTERNATIVE_2_SR = 
        create( 0x50ED, "Season 1 Alternative 2 (SR)", TOU_7 );
    static final IonHandle TOU_7_SEASON_2_SR = 
        create( 0x50BD, "Season 2 (SR)", TOU_7 );
    static final IonHandle TOU_7_SEASON_2_WEEKEND_SR = 
        create( 0x5105, "Season 2 Weekend (SR)", TOU_7 );
    static final IonHandle TOU_7_SEASON_2_ALTERNATIVE_2_SR = 
        create( 0x5115, "Season 2 Alternative 2 (SR)", TOU_7 );
    static final IonHandle TOU_7_SEASON_3_SR = 
        create( 0x50C5, "Season 3 (SR)", TOU_7 );
    static final IonHandle TOU_7_SEASON_3_WEEKEND_SR = 
        create( 0x512D, "Season 3 Weekend (SR)", TOU_7 );
    static final IonHandle TOU_7_SEASON_3_ALTERNATIVE_2_SR = 
        create( 0x513D, "Season 3 Alternative 2 (SR)", TOU_7 );
    static final IonHandle TOU_7_SEASON_4_SR = 
        create( 0x50CD, "Season 4 (SR)", TOU_7 );
    static final IonHandle TOU_7_SEASON_4_WEEKEND_SR = 
        create( 0x5155, "Season 4 Weekend (SR)", TOU_7 );
    static final IonHandle TOU_7_SEASON_4_ALTERNATIVE_2_SR = 
        create( 0x5165, "Season 4 Alternative 2 (SR)", TOU_7 );
    static final IonHandle TOU_7_WEEKDAY_DAYS_SR = 
        create( 0x5175, "Weekday Days (SR)", TOU_7 );
    static final IonHandle TOU_7_ALTERNATIVE_1_DATES_SR = 
        create( 0x5185, "Alternative 1 Dates (SR)", TOU_7 );
    static final IonHandle TOU_7_HOLIDAY_DATES_SR = 
        create( 0x5195, "Holiday Dates (SR)", TOU_7 );
    static final IonHandle  TOU_8 = 
        create( 0x1F07, "TOU 8" );
    static final IonHandle TOU_8_RATE_A_STATUS_BR = 
        create( 0x623B, "Rate A Status (BR)", TOU_8 );
    static final IonHandle TOU_8_RATE_C_STATUS_BR = 
        create( 0x624B, "Rate C Status (BR)", TOU_8 );
    static final IonHandle TOU_8_RATE_CHANGE_PR = 
        create( 0x6F2D, "Rate Change (PR)", TOU_8 );
    static final IonHandle TOU_8_SEASON_2_STATUS_BR = 
        create( 0x6263, "Season 2 Status (BR)", TOU_8 );
    static final IonHandle TOU_8_SEASON_4_STATUS_BR = 
        create( 0x6273, "Season 4 Status (BR)", TOU_8 );
    static final IonHandle TOU_8_WEEKDAY_STATUS_BR = 
        create( 0x62B5, "Weekday Status (BR)", TOU_8 );
    static final IonHandle TOU_8_ALTERNATIVE_1_STATUS_BR = 
        create( 0x62C5, "Alternative 1 Status (BR)", TOU_8 );
    static final IonHandle TOU_8_HOLIDAY_STATUS_BR = 
        create( 0x62D5, "Holiday Status (BR)", TOU_8 );
    static final IonHandle TOU_8_MONDAY_STATUS_BR = 
        create( 0x62DD, "Monday Status (BR)", TOU_8 );
    static final IonHandle TOU_8_WEDNESDAY_STATUS_BR = 
        create( 0x62ED, "Wednesday Status (BR)", TOU_8 );
    static final IonHandle TOU_8_FRIDAY_STATUS_BR = 
        create( 0x62FD, "Friday Status (BR)", TOU_8 );
    static final IonHandle TOU_8_SUNDAY_STATUS_BR = 
        create( 0x630D, "Sunday Status (BR)", TOU_8 );
    static final IonHandle TOU_8_SELF_READ_PR = 
        create( 0x6DB5, "Self Read (PR)", TOU_8 );
    static final IonHandle TOU_8_SEASON_1_SR = 
        create( 0x50B6, "Season 1 (SR)", TOU_8 );
    static final IonHandle TOU_8_SEASON_1_WEEKEND_SR = 
        create( 0x50DE, "Season 1 Weekend (SR)", TOU_8 );
    static final IonHandle TOU_8_SEASON_1_ALTERNATIVE_2_SR = 
        create( 0x50EE, "Season 1 Alternative 2 (SR)", TOU_8 );
    static final IonHandle TOU_8_SEASON_2_SR = 
        create( 0x50BE, "Season 2 (SR)", TOU_8 );
    static final IonHandle TOU_8_SEASON_2_WEEKEND_SR = 
        create( 0x5106, "Season 2 Weekend (SR)", TOU_8 );
    static final IonHandle TOU_8_SEASON_2_ALTERNATIVE_2_SR = 
        create( 0x5116, "Season 2 Alternative 2 (SR)", TOU_8 );
    static final IonHandle TOU_8_SEASON_3_SR = 
        create( 0x50C6, "Season 3 (SR)", TOU_8 );
    static final IonHandle TOU_8_SEASON_3_WEEKEND_SR = 
        create( 0x512E, "Season 3 Weekend (SR)", TOU_8 );
    static final IonHandle TOU_8_SEASON_3_ALTERNATIVE_2_SR = 
        create( 0x513E, "Season 3 Alternative 2 (SR)", TOU_8 );
    static final IonHandle TOU_8_SEASON_4_SR = 
        create( 0x50CE, "Season 4 (SR)", TOU_8 );
    static final IonHandle TOU_8_SEASON_4_WEEKEND_SR = 
        create( 0x5156, "Season 4 Weekend (SR)", TOU_8 );
    static final IonHandle TOU_8_SEASON_4_ALTERNATIVE_2_SR = 
        create( 0x5166, "Season 4 Alternative 2 (SR)", TOU_8 );
    static final IonHandle TOU_8_WEEKDAY_DAYS_SR = 
        create( 0x5176, "Weekday Days (SR)", TOU_8 );
    static final IonHandle TOU_8_ALTERNATIVE_1_DATES_SR = 
        create( 0x5186, "Alternative 1 Dates (SR)", TOU_8 );
    static final IonHandle TOU_8_HOLIDAY_DATES_SR = 
        create( 0x5196, "Holiday Dates (SR)", TOU_8 );
    static final IonHandle  INT_1 = 
        create( 0x500, "INT #1" );
    static final IonHandle INT_1_RESULT_NVR = 
        create( 0x58B0, "Result (NVR)", INT_1 );
    static final IonHandle INT_1_TRIGGER_DR = 
        create( 0x6826, "Trigger (DR)", INT_1 );
    static final IonHandle INT_1_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_1 );
    static final IonHandle INT_1_INT_MODEENR = 
        create( 0x78D8, "Int Mode(ENR)", INT_1 );
    static final IonHandle INT_1_ROLLVALUE_NBR = 
        create( 0x721E, "RollValue (NBR)", INT_1 );
    static final IonHandle  INT_2 = 
        create( 0x0501, "INT #2" );
    static final IonHandle INT_2_RESULT_NVR = 
        create( 0x58B1, "Result (NVR)", INT_2 );
    static final IonHandle INT_2_TRIGGER_DR = 
        create( 0x6827, "Trigger (DR)", INT_2 );
    static final IonHandle INT_2_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_2 );
    static final IonHandle INT_2_INT_MODEENR = 
        create( 0x78D9, "Int Mode(ENR)", INT_2 );
    static final IonHandle INT_2_ROLLVALUE_NBR = 
        create( 0x721F, "RollValue (NBR)", INT_2 );
    static final IonHandle  INT_3 = 
        create( 0x0502, "INT #3" );
    static final IonHandle INT_3_RESULT_NVR = 
        create( 0x58B2, "Result (NVR)", INT_3 );
    static final IonHandle INT_3_TRIGGER_DR = 
        create( 0x6828, "Trigger (DR)", INT_3 );
    static final IonHandle INT_3_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_3 );
    static final IonHandle INT_3_INT_MODEENR = 
        create( 0x78DA, "Int Mode(ENR)", INT_3 );
    static final IonHandle INT_3_ROLLVALUE_NBR = 
        create( 0x7220, "RollValue (NBR)", INT_3 );
    static final IonHandle  INT_4 = 
        create( 0x0503, "INT #4" );
    static final IonHandle INT_4_RESULT_NVR = 
        create( 0x58B3, "Result (NVR)", INT_4 );
    static final IonHandle INT_4_TRIGGER_DR = 
        create( 0x6829, "Trigger (DR)", INT_4 );
    static final IonHandle INT_4_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_4 );
    static final IonHandle INT_4_INT_MODEENR = 
        create( 0x78DB, "Int Mode(ENR)", INT_4 );
    static final IonHandle INT_4_ROLLVALUE_NBR = 
        create( 0x7221, "RollValue (NBR)", INT_4 );
    static final IonHandle  INT_5 = 
        create( 0x0504, "INT #5" );
    static final IonHandle INT_5_RESULT_NVR = 
        create( 0x58B4, "Result (NVR)", INT_5 );
    static final IonHandle INT_5_TRIGGER_DR = 
        create( 0x682A, "Trigger (DR)", INT_5 );
    static final IonHandle INT_5_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_5 );
    static final IonHandle INT_5_INT_MODEENR = 
        create( 0x78DC, "Int Mode(ENR)", INT_5 );
    static final IonHandle INT_5_ROLLVALUE_NBR = 
        create( 0x7222, "RollValue (NBR)", INT_5 );
    static final IonHandle  INT_6 = 
        create( 0x0505, "INT #6" );
    static final IonHandle INT_6_RESULT_NVR = 
        create( 0x58B5, "Result (NVR)", INT_6 );
    static final IonHandle INT_6_TRIGGER_DR = 
        create( 0x682B, "Trigger (DR)", INT_6 );
    static final IonHandle INT_6_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_6 );
    static final IonHandle INT_6_INT_MODEENR = 
        create( 0x78DD, "Int Mode(ENR)", INT_6 );
    static final IonHandle INT_6_ROLLVALUE_NBR = 
        create( 0x7223, "RollValue (NBR)", INT_6 );
    static final IonHandle  INT_7 = 
        create( 0x0506, "INT #7" );
    static final IonHandle INT_7_RESULT_NVR = 
        create( 0x58B6, "Result (NVR)", INT_7 );
    static final IonHandle INT_7_TRIGGER_DR = 
        create( 0x682C, "Trigger (DR)", INT_7 );
    static final IonHandle INT_7_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_7 );
    static final IonHandle INT_7_INT_MODEENR = 
        create( 0x78DE, "Int Mode(ENR)", INT_7 );
    static final IonHandle INT_7_ROLLVALUE_NBR = 
        create( 0x7224, "RollValue (NBR)", INT_7 );
    static final IonHandle  INT_8 = 
        create( 0x0507, "INT #8" );
    static final IonHandle INT_8_RESULT_NVR = 
        create( 0x58B7, "Result (NVR)", INT_8 );
    static final IonHandle INT_8_TRIGGER_DR = 
        create( 0x682D, "Trigger (DR)", INT_8 );
    static final IonHandle INT_8_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_8 );
    static final IonHandle INT_8_INT_MODEENR = 
        create( 0x78DF, "Int Mode(ENR)", INT_8 );
    static final IonHandle INT_8_ROLLVALUE_NBR = 
        create( 0x7225, "RollValue (NBR)", INT_8 );
    static final IonHandle  INT_9 = 
        create( 0x0508, "INT #9" );
    static final IonHandle INT_9_RESULT_NVR = 
        create( 0x58B8, "Result (NVR)", INT_9 );
    static final IonHandle INT_9_TRIGGER_DR = 
        create( 0x682E, "Trigger (DR)", INT_9 );
    static final IonHandle INT_9_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_9 );
    static final IonHandle INT_9_INT_MODEENR = 
        create( 0x78E0, "Int Mode(ENR)", INT_9 );
    static final IonHandle INT_9_ROLLVALUE_NBR = 
        create( 0x7226, "RollValue (NBR)", INT_9 );
    static final IonHandle  INT_10 = 
        create( 0x0509, "INT #10" );
    static final IonHandle INT_10_RESULT_NVR = 
        create( 0x58B9, "Result (NVR)", INT_10 );
    static final IonHandle INT_10_TRIGGER_DR = 
        create( 0x682F, "Trigger (DR)", INT_10 );
    static final IonHandle INT_10_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_10 );
    static final IonHandle INT_10_INT_MODEENR = 
        create( 0x78E1, "Int Mode(ENR)", INT_10 );
    static final IonHandle INT_10_ROLLVALUE_NBR = 
        create( 0x7227, "RollValue (NBR)", INT_10 );
    static final IonHandle  INT_11 = 
        create( 0x050A, "INT #11" );
    static final IonHandle INT_11_RESULT_NVR = 
        create( 0x58BA, "Result (NVR)", INT_11 );
    static final IonHandle INT_11_TRIGGER_DR = 
        create( 0x6830, "Trigger (DR)", INT_11 );
    static final IonHandle INT_11_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_11 );
    static final IonHandle INT_11_INT_MODEENR = 
        create( 0x78E2, "Int Mode(ENR)", INT_11 );
    static final IonHandle INT_11_ROLLVALUE_NBR = 
        create( 0x7228, "RollValue (NBR)", INT_11 );
    static final IonHandle  INT_12 = 
        create( 0x050B, "INT #12" );
    static final IonHandle INT_12_RESULT_NVR = 
        create( 0x58BB, "Result (NVR)", INT_12 );
    static final IonHandle INT_12_TRIGGER_DR = 
        create( 0x6831, "Trigger (DR)", INT_12 );
    static final IonHandle INT_12_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_12 );
    static final IonHandle INT_12_INT_MODEENR = 
        create( 0x78E3, "Int Mode(ENR)", INT_12 );
    static final IonHandle INT_12_ROLLVALUE_NBR = 
        create( 0x7229, "RollValue (NBR)", INT_12 );
    static final IonHandle  INT_13 = 
        create( 0x050C, "INT #13" );
    static final IonHandle INT_13_RESULT_NVR = 
        create( 0x58BC, "Result (NVR)", INT_13 );
    static final IonHandle INT_13_TRIGGER_DR = 
        create( 0x6832, "Trigger (DR)", INT_13 );
    static final IonHandle INT_13_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_13 );
    static final IonHandle INT_13_INT_MODEENR = 
        create( 0x78E4, "Int Mode(ENR)", INT_13 );
    static final IonHandle INT_13_ROLLVALUE_NBR = 
        create( 0x722A, "RollValue (NBR)", INT_13 );
    static final IonHandle  INT_14 = 
        create( 0x050D, "INT #14" );
    static final IonHandle INT_14_RESULT_NVR = 
        create( 0x58BD, "Result (NVR)", INT_14 );
    static final IonHandle INT_14_TRIGGER_DR = 
        create( 0x6833, "Trigger (DR)", INT_14 );
    static final IonHandle INT_14_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_14 );
    static final IonHandle INT_14_INT_MODEENR = 
        create( 0x78E5, "Int Mode(ENR)", INT_14 );
    static final IonHandle INT_14_ROLLVALUE_NBR = 
        create( 0x722B, "RollValue (NBR)", INT_14 );
    static final IonHandle  INT_15 = 
        create( 0x050E, "INT #15" );
    static final IonHandle INT_15_RESULT_NVR = 
        create( 0x58BE, "Result (NVR)", INT_15 );
    static final IonHandle INT_15_TRIGGER_DR = 
        create( 0x6834, "Trigger (DR)", INT_15 );
    static final IonHandle INT_15_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_15 );
    static final IonHandle INT_15_INT_MODEENR = 
        create( 0x78E6, "Int Mode(ENR)", INT_15 );
    static final IonHandle INT_15_ROLLVALUE_NBR = 
        create( 0x722C, "RollValue (NBR)", INT_15 );
    static final IonHandle  INT_16 = 
        create( 0x050F, "INT #16" );
    static final IonHandle INT_16_RESULT_NVR = 
        create( 0x58BF, "Result (NVR)", INT_16 );
    static final IonHandle INT_16_TRIGGER_DR = 
        create( 0x6835, "Trigger (DR)", INT_16 );
    static final IonHandle INT_16_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_16 );
    static final IonHandle INT_16_INT_MODEENR = 
        create( 0x78E7, "Int Mode(ENR)", INT_16 );
    static final IonHandle INT_16_ROLLVALUE_NBR = 
        create( 0x722D, "RollValue (NBR)", INT_16 );
    static final IonHandle  INT_17 = 
        create( 0x0510, "INT #17" );
    static final IonHandle INT_17_RESULT_NVR = 
        create( 0x5C3B, "Result (NVR)", INT_17 );
    static final IonHandle INT_17_TRIGGER_DR = 
        create( 0x69E4, "Trigger (DR)", INT_17 );
    static final IonHandle INT_17_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_17 );
    static final IonHandle INT_17_INT_MODEENR = 
        create( 0x7AD7, "Int Mode(ENR)", INT_17 );
    static final IonHandle INT_17_ROLLVALUE_NBR = 
        create( 0x7322, "RollValue (NBR)", INT_17 );
    static final IonHandle  INT_18 = 
        create( 0x0511, "INT #18" );
    static final IonHandle INT_18_RESULT_NVR = 
        create( 0x5C3C, "Result (NVR)", INT_18 );
    static final IonHandle INT_18_TRIGGER_DR = 
        create( 0x69E5, "Trigger (DR)", INT_18 );
    static final IonHandle INT_18_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_18 );
    static final IonHandle INT_18_INT_MODEENR = 
        create( 0x7AD8, "Int Mode(ENR)", INT_18 );
    static final IonHandle INT_18_ROLLVALUE_NBR = 
        create( 0x7323, "RollValue (NBR)", INT_18 );
    static final IonHandle  INT_19 = 
        create( 0x0512, "INT #19" );
    static final IonHandle INT_19_RESULT_NVR = 
        create( 0x5C3D, "Result (NVR)", INT_19 );
    static final IonHandle INT_19_TRIGGER_DR = 
        create( 0x69E6, "Trigger (DR)", INT_19 );
    static final IonHandle INT_19_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_19 );
    static final IonHandle INT_19_INT_MODEENR = 
        create( 0x7AD9, "Int Mode(ENR)", INT_19 );
    static final IonHandle INT_19_ROLLVALUE_NBR = 
        create( 0x7324, "RollValue (NBR)", INT_19 );
    static final IonHandle  INT_20 = 
        create( 0x0513, "INT #20" );
    static final IonHandle INT_20_RESULT_NVR = 
        create( 0x5C3E, "Result (NVR)", INT_20 );
    static final IonHandle INT_20_TRIGGER_DR = 
        create( 0x69E7, "Trigger (DR)", INT_20 );
    static final IonHandle INT_20_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_20 );
    static final IonHandle INT_20_INT_MODEENR = 
        create( 0x7ADA, "Int Mode(ENR)", INT_20 );
    static final IonHandle INT_20_ROLLVALUE_NBR = 
        create( 0x7325, "RollValue (NBR)", INT_20 );
    static final IonHandle  INT_21 = 
        create( 0x0514, "INT #21" );
    static final IonHandle INT_21_RESULT_NVR = 
        create( 0x5C3F, "Result (NVR)", INT_21 );
    static final IonHandle INT_21_TRIGGER_DR = 
        create( 0x69E8, "Trigger (DR)", INT_21 );
    static final IonHandle INT_21_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_21 );
    static final IonHandle INT_21_INT_MODEENR = 
        create( 0x7ADB, "Int Mode(ENR)", INT_21 );
    static final IonHandle INT_21_ROLLVALUE_NBR = 
        create( 0x7326, "RollValue (NBR)", INT_21 );
    static final IonHandle  INT_22 = 
        create( 0x0515, "INT #22" );
    static final IonHandle INT_22_RESULT_NVR = 
        create( 0x5C40, "Result (NVR)", INT_22 );
    static final IonHandle INT_22_TRIGGER_DR = 
        create( 0x69E9, "Trigger (DR)", INT_22 );
    static final IonHandle INT_22_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_22 );
    static final IonHandle INT_22_INT_MODEENR = 
        create( 0x7ADC, "Int Mode(ENR)", INT_22 );
    static final IonHandle INT_22_ROLLVALUE_NBR = 
        create( 0x7327, "RollValue (NBR)", INT_22 );
    static final IonHandle  INT_23 = 
        create( 0x0516, "INT #23" );
    static final IonHandle INT_23_RESULT_NVR = 
        create( 0x5C41, "Result (NVR)", INT_23 );
    static final IonHandle INT_23_TRIGGER_DR = 
        create( 0x69EA, "Trigger (DR)", INT_23 );
    static final IonHandle INT_23_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_23 );
    static final IonHandle INT_23_INT_MODEENR = 
        create( 0x7ADD, "Int Mode(ENR)", INT_23 );
    static final IonHandle INT_23_ROLLVALUE_NBR = 
        create( 0x7328, "RollValue (NBR)", INT_23 );
    static final IonHandle  INT_24 = 
        create( 0x0517, "INT #24" );
    static final IonHandle INT_24_RESULT_NVR = 
        create( 0x5C42, "Result (NVR)", INT_24 );
    static final IonHandle INT_24_TRIGGER_DR = 
        create( 0x69EB, "Trigger (DR)", INT_24 );
    static final IonHandle INT_24_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_24 );
    static final IonHandle INT_24_INT_MODEENR = 
        create( 0x7ADE, "Int Mode(ENR)", INT_24 );
    static final IonHandle INT_24_ROLLVALUE_NBR = 
        create( 0x7329, "RollValue (NBR)", INT_24 );
    static final IonHandle  INT_25 = 
        create( 0x0518, "INT #25" );
    static final IonHandle INT_25_RESULT_NVR = 
        create( 0x5C43, "Result (NVR)", INT_25 );
    static final IonHandle INT_25_TRIGGER_DR = 
        create( 0x69EC, "Trigger (DR)", INT_25 );
    static final IonHandle INT_25_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_25 );
    static final IonHandle INT_25_INT_MODEENR = 
        create( 0x7ADF, "Int Mode(ENR)", INT_25 );
    static final IonHandle INT_25_ROLLVALUE_NBR = 
        create( 0x732A, "RollValue (NBR)", INT_25 );
    static final IonHandle  INT_26 = 
        create( 0x0519, "INT #26" );
    static final IonHandle INT_26_RESULT_NVR = 
        create( 0x5C44, "Result (NVR)", INT_26 );
    static final IonHandle INT_26_TRIGGER_DR = 
        create( 0x69ED, "Trigger (DR)", INT_26 );
    static final IonHandle INT_26_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_26 );
    static final IonHandle INT_26_INT_MODEENR = 
        create( 0x7AE0, "Int Mode(ENR)", INT_26 );
    static final IonHandle INT_26_ROLLVALUE_NBR = 
        create( 0x732B, "RollValue (NBR)", INT_26 );
    static final IonHandle  INT_27 = 
        create( 0x051A, "INT #27" );
    static final IonHandle INT_27_RESULT_NVR = 
        create( 0x5C45, "Result (NVR)", INT_27 );
    static final IonHandle INT_27_TRIGGER_DR = 
        create( 0x69EE, "Trigger (DR)", INT_27 );
    static final IonHandle INT_27_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_27 );
    static final IonHandle INT_27_INT_MODEENR = 
        create( 0x7AE1, "Int Mode(ENR)", INT_27 );
    static final IonHandle INT_27_ROLLVALUE_NBR = 
        create( 0x732C, "RollValue (NBR)", INT_27 );
    static final IonHandle  INT_28 = 
        create( 0x051B, "INT #28" );
    static final IonHandle INT_28_RESULT_NVR = 
        create( 0x5C46, "Result (NVR)", INT_28 );
    static final IonHandle INT_28_TRIGGER_DR = 
        create( 0x69EF, "Trigger (DR)", INT_28 );
    static final IonHandle INT_28_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_28 );
    static final IonHandle INT_28_INT_MODEENR = 
        create( 0x7AE2, "Int Mode(ENR)", INT_28 );
    static final IonHandle INT_28_ROLLVALUE_NBR = 
        create( 0x732D, "RollValue (NBR)", INT_28 );
    static final IonHandle  INT_29 = 
        create( 0x051C, "INT #29" );
    static final IonHandle INT_29_RESULT_NVR = 
        create( 0x5C47, "Result (NVR)", INT_29 );
    static final IonHandle INT_29_TRIGGER_DR = 
        create( 0x69F0, "Trigger (DR)", INT_29 );
    static final IonHandle INT_29_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_29 );
    static final IonHandle INT_29_INT_MODEENR = 
        create( 0x7AE3, "Int Mode(ENR)", INT_29 );
    static final IonHandle INT_29_ROLLVALUE_NBR = 
        create( 0x732E, "RollValue (NBR)", INT_29 );
    static final IonHandle  INT_30 = 
        create( 0x051D, "INT #30" );
    static final IonHandle INT_30_RESULT_NVR = 
        create( 0x5C48, "Result (NVR)", INT_30 );
    static final IonHandle INT_30_TRIGGER_DR = 
        create( 0x69F1, "Trigger (DR)", INT_30 );
    static final IonHandle INT_30_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_30 );
    static final IonHandle INT_30_INT_MODEENR = 
        create( 0x7AE4, "Int Mode(ENR)", INT_30 );
    static final IonHandle INT_30_ROLLVALUE_NBR = 
        create( 0x732F, "RollValue (NBR)", INT_30 );
    static final IonHandle  INT_31 = 
        create( 0x051E, "INT #31" );
    static final IonHandle INT_31_RESULT_NVR = 
        create( 0x5F9F, "Result (NVR)", INT_31 );
    static final IonHandle INT_31_TRIGGER_DR = 
        create( 0x6CD6, "Trigger (DR)", INT_31 );
    static final IonHandle INT_31_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_31 );
    static final IonHandle INT_31_INT_MODEENR = 
        create( 0x7C25, "Int Mode(ENR)", INT_31 );
    static final IonHandle INT_31_ROLLVALUE_NBR = 
        create( 0x76C7, "RollValue (NBR)", INT_31 );
    static final IonHandle  INT_32 = 
        create( 0x051F, "INT #32" );
    static final IonHandle INT_32_RESULT_NVR = 
        create( 0x5FA0, "Result (NVR)", INT_32 );
    static final IonHandle INT_32_TRIGGER_DR = 
        create( 0x6CD7, "Trigger (DR)", INT_32 );
    static final IonHandle INT_32_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_32 );
    static final IonHandle INT_32_INT_MODEENR = 
        create( 0x7C26, "Int Mode(ENR)", INT_32 );
    static final IonHandle INT_32_ROLLVALUE_NBR = 
        create( 0x76C8, "RollValue (NBR)", INT_32 );
    static final IonHandle  INT_33 = 
        create( 0x0520, "INT #33" );
    static final IonHandle INT_33_RESULT_NVR = 
        create( 0x5FA1, "Result (NVR)", INT_33 );
    static final IonHandle INT_33_TRIGGER_DR = 
        create( 0x6CD8, "Trigger (DR)", INT_33 );
    static final IonHandle INT_33_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_33 );
    static final IonHandle INT_33_INT_MODEENR = 
        create( 0x7C27, "Int Mode(ENR)", INT_33 );
    static final IonHandle INT_33_ROLLVALUE_NBR = 
        create( 0x76C9, "RollValue (NBR)", INT_33 );
    static final IonHandle  INT_34 = 
        create( 0x0521, "INT #34" );
    static final IonHandle INT_34_RESULT_NVR = 
        create( 0x5FA2, "Result (NVR)", INT_34 );
    static final IonHandle INT_34_TRIGGER_DR = 
        create( 0x6CD9, "Trigger (DR)", INT_34 );
    static final IonHandle INT_34_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_34 );
    static final IonHandle INT_34_INT_MODEENR = 
        create( 0x7C28, "Int Mode(ENR)", INT_34 );
    static final IonHandle INT_34_ROLLVALUE_NBR = 
        create( 0x76CA, "RollValue (NBR)", INT_34 );
    static final IonHandle  INT_35 = 
        create( 0x0522, "INT #35" );
    static final IonHandle INT_35_RESULT_NVR = 
        create( 0x5FA3, "Result (NVR)", INT_35 );
    static final IonHandle INT_35_TRIGGER_DR = 
        create( 0x6CDA, "Trigger (DR)", INT_35 );
    static final IonHandle INT_35_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_35 );
    static final IonHandle INT_35_INT_MODEENR = 
        create( 0x7C29, "Int Mode(ENR)", INT_35 );
    static final IonHandle INT_35_ROLLVALUE_NBR = 
        create( 0x76CB, "RollValue (NBR)", INT_35 );
    static final IonHandle  INT_36 = 
        create( 0x0523, "INT #36" );
    static final IonHandle INT_36_RESULT_NVR = 
        create( 0x5FA4, "Result (NVR)", INT_36 );
    static final IonHandle INT_36_TRIGGER_DR = 
        create( 0x6CDB, "Trigger (DR)", INT_36 );
    static final IonHandle INT_36_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_36 );
    static final IonHandle INT_36_INT_MODEENR = 
        create( 0x7C2A, "Int Mode(ENR)", INT_36 );
    static final IonHandle INT_36_ROLLVALUE_NBR = 
        create( 0x76CC, "RollValue (NBR)", INT_36 );
    static final IonHandle  INT_37 = 
        create( 0x0524, "INT #37" );
    static final IonHandle INT_37_RESULT_NVR = 
        create( 0x5FA5, "Result (NVR)", INT_37 );
    static final IonHandle INT_37_TRIGGER_DR = 
        create( 0x6CDC, "Trigger (DR)", INT_37 );
    static final IonHandle INT_37_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_37 );
    static final IonHandle INT_37_INT_MODEENR = 
        create( 0x7C2B, "Int Mode(ENR)", INT_37 );
    static final IonHandle INT_37_ROLLVALUE_NBR = 
        create( 0x76CD, "RollValue (NBR)", INT_37 );
    static final IonHandle  INT_38 = 
        create( 0x0525, "INT #38" );
    static final IonHandle INT_38_RESULT_NVR = 
        create( 0x5FA6, "Result (NVR)", INT_38 );
    static final IonHandle INT_38_TRIGGER_DR = 
        create( 0x6CDD, "Trigger (DR)", INT_38 );
    static final IonHandle INT_38_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_38 );
    static final IonHandle INT_38_INT_MODEENR = 
        create( 0x7C2C, "Int Mode(ENR)", INT_38 );
    static final IonHandle INT_38_ROLLVALUE_NBR = 
        create( 0x76CE, "RollValue (NBR)", INT_38 );
    static final IonHandle  INT_39 = 
        create( 0x0526, "INT #39" );
    static final IonHandle INT_39_RESULT_NVR = 
        create( 0x5FA7, "Result (NVR)", INT_39 );
    static final IonHandle INT_39_TRIGGER_DR = 
        create( 0x6CDE, "Trigger (DR)", INT_39 );
    static final IonHandle INT_39_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_39 );
    static final IonHandle INT_39_INT_MODEENR = 
        create( 0x7C2D, "Int Mode(ENR)", INT_39 );
    static final IonHandle INT_39_ROLLVALUE_NBR = 
        create( 0x76CF, "RollValue (NBR)", INT_39 );
    static final IonHandle  INT_40 = 
        create( 0x0527, "INT #40" );
    static final IonHandle INT_40_RESULT_NVR = 
        create( 0x5FA8, "Result (NVR)", INT_40 );
    static final IonHandle INT_40_TRIGGER_DR = 
        create( 0x6CDF, "Trigger (DR)", INT_40 );
    static final IonHandle INT_40_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_40 );
    static final IonHandle INT_40_INT_MODEENR = 
        create( 0x7C2E, "Int Mode(ENR)", INT_40 );
    static final IonHandle INT_40_ROLLVALUE_NBR = 
        create( 0x76D0, "RollValue (NBR)", INT_40 );
    static final IonHandle  INT_41 = 
        create( 0x0528, "INT #41" );
    static final IonHandle INT_41_RESULT_NVR = 
        create( 0x5FA9, "Result (NVR)", INT_41 );
    static final IonHandle INT_41_TRIGGER_DR = 
        create( 0x6CE0, "Trigger (DR)", INT_41 );
    static final IonHandle INT_41_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_41 );
    static final IonHandle INT_41_INT_MODEENR = 
        create( 0x7C2F, "Int Mode(ENR)", INT_41 );
    static final IonHandle INT_41_ROLLVALUE_NBR = 
        create( 0x76D1, "RollValue (NBR)", INT_41 );
    static final IonHandle  INT_42 = 
        create( 0x0529, "INT #42" );
    static final IonHandle INT_42_RESULT_NVR = 
        create( 0x5FAA, "Result (NVR)", INT_42 );
    static final IonHandle INT_42_TRIGGER_DR = 
        create( 0x6CE1, "Trigger (DR)", INT_42 );
    static final IonHandle INT_42_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_42 );
    static final IonHandle INT_42_INT_MODEENR = 
        create( 0x7C30, "Int Mode(ENR)", INT_42 );
    static final IonHandle INT_42_ROLLVALUE_NBR = 
        create( 0x76D2, "RollValue (NBR)", INT_42 );
    static final IonHandle  INT_43 = 
        create( 0x052A, "INT #43" );
    static final IonHandle INT_43_RESULT_NVR = 
        create( 0x5FAB, "Result (NVR)", INT_43 );
    static final IonHandle INT_43_TRIGGER_DR = 
        create( 0x6CE2, "Trigger (DR)", INT_43 );
    static final IonHandle INT_43_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_43 );
    static final IonHandle INT_43_INT_MODEENR = 
        create( 0x7C31, "Int Mode(ENR)", INT_43 );
    static final IonHandle INT_43_ROLLVALUE_NBR = 
        create( 0x76D3, "RollValue (NBR)", INT_43 );
    static final IonHandle  INT_44 = 
        create( 0x052B, "INT #44" );
    static final IonHandle INT_44_RESULT_NVR = 
        create( 0x5FAC, "Result (NVR)", INT_44 );
    static final IonHandle INT_44_TRIGGER_DR = 
        create( 0x6CE3, "Trigger (DR)", INT_44 );
    static final IonHandle INT_44_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_44 );
    static final IonHandle INT_44_INT_MODEENR = 
        create( 0x7C32, "Int Mode(ENR)", INT_44 );
    static final IonHandle INT_44_ROLLVALUE_NBR = 
        create( 0x76D4, "RollValue (NBR)", INT_44 );
    static final IonHandle  INT_45 = 
        create( 0x052C, "INT #45" );
    static final IonHandle INT_45_RESULT_NVR = 
        create( 0x5FAD, "Result (NVR)", INT_45 );
    static final IonHandle INT_45_TRIGGER_DR = 
        create( 0x6CE4, "Trigger (DR)", INT_45 );
    static final IonHandle INT_45_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_45 );
    static final IonHandle INT_45_INT_MODEENR = 
        create( 0x7C33, "Int Mode(ENR)", INT_45 );
    static final IonHandle INT_45_ROLLVALUE_NBR = 
        create( 0x76D5, "RollValue (NBR)", INT_45 );
    static final IonHandle  INT_46 = 
        create( 0x052D, "INT #46" );
    static final IonHandle INT_46_RESULT_NVR = 
        create( 0x5FAE, "Result (NVR)", INT_46 );
    static final IonHandle INT_46_TRIGGER_DR = 
        create( 0x6CE5, "Trigger (DR)", INT_46 );
    static final IonHandle INT_46_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_46 );
    static final IonHandle INT_46_INT_MODEENR = 
        create( 0x7C34, "Int Mode(ENR)", INT_46 );
    static final IonHandle INT_46_ROLLVALUE_NBR = 
        create( 0x76D6, "RollValue (NBR)", INT_46 );
    static final IonHandle  INT_47 = 
        create( 0x052E, "INT #47" );
    static final IonHandle INT_47_RESULT_NVR = 
        create( 0x5FAF, "Result (NVR)", INT_47 );
    static final IonHandle INT_47_TRIGGER_DR = 
        create( 0x6CE6, "Trigger (DR)", INT_47 );
    static final IonHandle INT_47_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_47 );
    static final IonHandle INT_47_INT_MODEENR = 
        create( 0x7C35, "Int Mode(ENR)", INT_47 );
    static final IonHandle INT_47_ROLLVALUE_NBR = 
        create( 0x76D7, "RollValue (NBR)", INT_47 );
    static final IonHandle  INT_48 = 
        create( 0x052F, "INT #48" );
    static final IonHandle INT_48_RESULT_NVR = 
        create( 0x5FB0, "Result (NVR)", INT_48 );
    static final IonHandle INT_48_TRIGGER_DR = 
        create( 0x6CE7, "Trigger (DR)", INT_48 );
    static final IonHandle INT_48_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_48 );
    static final IonHandle INT_48_INT_MODEENR = 
        create( 0x7C36, "Int Mode(ENR)", INT_48 );
    static final IonHandle INT_48_ROLLVALUE_NBR = 
        create( 0x76D8, "RollValue (NBR)", INT_48 );
    static final IonHandle  INT_49 = 
        create( 0x0530, "INT #49" );
    static final IonHandle INT_49_RESULT_NVR = 
        create( 0x5FB1, "Result (NVR)", INT_49 );
    static final IonHandle INT_49_TRIGGER_DR = 
        create( 0x6CE8, "Trigger (DR)", INT_49 );
    static final IonHandle INT_49_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_49 );
    static final IonHandle INT_49_INT_MODEENR = 
        create( 0x7C37, "Int Mode(ENR)", INT_49 );
    static final IonHandle INT_49_ROLLVALUE_NBR = 
        create( 0x76D9, "RollValue (NBR)", INT_49 );
    static final IonHandle  INT_50 = 
        create( 0x0531, "INT #50" );
    static final IonHandle INT_50_RESULT_NVR = 
        create( 0x5FB2, "Result (NVR)", INT_50 );
    static final IonHandle INT_50_TRIGGER_DR = 
        create( 0x6CE9, "Trigger (DR)", INT_50 );
    static final IonHandle INT_50_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_50 );
    static final IonHandle INT_50_INT_MODEENR = 
        create( 0x7C38, "Int Mode(ENR)", INT_50 );
    static final IonHandle INT_50_ROLLVALUE_NBR = 
        create( 0x76DA, "RollValue (NBR)", INT_50 );
    static final IonHandle  INT_51 = 
        create( 0x0532, "INT #51" );
    static final IonHandle INT_51_RESULT_NVR = 
        create( 0x5FB3, "Result (NVR)", INT_51 );
    static final IonHandle INT_51_TRIGGER_DR = 
        create( 0x6CEA, "Trigger (DR)", INT_51 );
    static final IonHandle INT_51_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_51 );
    static final IonHandle INT_51_INT_MODEENR = 
        create( 0x7C39, "Int Mode(ENR)", INT_51 );
    static final IonHandle INT_51_ROLLVALUE_NBR = 
        create( 0x76DB, "RollValue (NBR)", INT_51 );
    static final IonHandle  INT_52 = 
        create( 0x0533, "INT #52" );
    static final IonHandle INT_52_RESULT_NVR = 
        create( 0x5FB4, "Result (NVR)", INT_52 );
    static final IonHandle INT_52_TRIGGER_DR = 
        create( 0x6CEB, "Trigger (DR)", INT_52 );
    static final IonHandle INT_52_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_52 );
    static final IonHandle INT_52_INT_MODEENR = 
        create( 0x7C3A, "Int Mode(ENR)", INT_52 );
    static final IonHandle INT_52_ROLLVALUE_NBR = 
        create( 0x76DC, "RollValue (NBR)", INT_52 );
    static final IonHandle  INT_53 = 
        create( 0x0534, "INT #53" );
    static final IonHandle INT_53_RESULT_NVR = 
        create( 0x5FB5, "Result (NVR)", INT_53 );
    static final IonHandle INT_53_TRIGGER_DR = 
        create( 0x6CEC, "Trigger (DR)", INT_53 );
    static final IonHandle INT_53_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_53 );
    static final IonHandle INT_53_INT_MODEENR = 
        create( 0x7C3B, "Int Mode(ENR)", INT_53 );
    static final IonHandle INT_53_ROLLVALUE_NBR = 
        create( 0x76DD, "RollValue (NBR)", INT_53 );
    static final IonHandle  INT_54 = 
        create( 0x0535, "INT #54" );
    static final IonHandle INT_54_RESULT_NVR = 
        create( 0x5FB6, "Result (NVR)", INT_54 );
    static final IonHandle INT_54_TRIGGER_DR = 
        create( 0x6CED, "Trigger (DR)", INT_54 );
    static final IonHandle INT_54_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_54 );
    static final IonHandle INT_54_INT_MODEENR = 
        create( 0x7C3C, "Int Mode(ENR)", INT_54 );
    static final IonHandle INT_54_ROLLVALUE_NBR = 
        create( 0x76DE, "RollValue (NBR)", INT_54 );
    static final IonHandle  INT_55 = 
        create( 0x0536, "INT #55" );
    static final IonHandle INT_55_RESULT_NVR = 
        create( 0x5FB7, "Result (NVR)", INT_55 );
    static final IonHandle INT_55_TRIGGER_DR = 
        create( 0x6CEE, "Trigger (DR)", INT_55 );
    static final IonHandle INT_55_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_55 );
    static final IonHandle INT_55_INT_MODEENR = 
        create( 0x7C3D, "Int Mode(ENR)", INT_55 );
    static final IonHandle INT_55_ROLLVALUE_NBR = 
        create( 0x76DF, "RollValue (NBR)", INT_55 );
    static final IonHandle  INT_56 = 
        create( 0x0537, "INT #56" );
    static final IonHandle INT_56_RESULT_NVR = 
        create( 0x5FB8, "Result (NVR)", INT_56 );
    static final IonHandle INT_56_TRIGGER_DR = 
        create( 0x6CEF, "Trigger (DR)", INT_56 );
    static final IonHandle INT_56_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_56 );
    static final IonHandle INT_56_INT_MODEENR = 
        create( 0x7C3E, "Int Mode(ENR)", INT_56 );
    static final IonHandle INT_56_ROLLVALUE_NBR = 
        create( 0x76E0, "RollValue (NBR)", INT_56 );
    static final IonHandle  INT_57 = 
        create( 0x0538, "INT #57" );
    static final IonHandle INT_57_RESULT_NVR = 
        create( 0x5FB9, "Result (NVR)", INT_57 );
    static final IonHandle INT_57_TRIGGER_DR = 
        create( 0x6CF0, "Trigger (DR)", INT_57 );
    static final IonHandle INT_57_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_57 );
    static final IonHandle INT_57_INT_MODEENR = 
        create( 0x7C3F, "Int Mode(ENR)", INT_57 );
    static final IonHandle INT_57_ROLLVALUE_NBR = 
        create( 0x76E1, "RollValue (NBR)", INT_57 );
    static final IonHandle  INT_58 = 
        create( 0x0539, "INT #58" );
    static final IonHandle INT_58_RESULT_NVR = 
        create( 0x5FBA, "Result (NVR)", INT_58 );
    static final IonHandle INT_58_TRIGGER_DR = 
        create( 0x6CF1, "Trigger (DR)", INT_58 );
    static final IonHandle INT_58_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_58 );
    static final IonHandle INT_58_INT_MODEENR = 
        create( 0x7C40, "Int Mode(ENR)", INT_58 );
    static final IonHandle INT_58_ROLLVALUE_NBR = 
        create( 0x76E2, "RollValue (NBR)", INT_58 );
    static final IonHandle  INT_59 = 
        create( 0x053A, "INT #59" );
    static final IonHandle INT_59_RESULT_NVR = 
        create( 0x5FBB, "Result (NVR)", INT_59 );
    static final IonHandle INT_59_TRIGGER_DR = 
        create( 0x6CF2, "Trigger (DR)", INT_59 );
    static final IonHandle INT_59_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_59 );
    static final IonHandle INT_59_INT_MODEENR = 
        create( 0x7C41, "Int Mode(ENR)", INT_59 );
    static final IonHandle INT_59_ROLLVALUE_NBR = 
        create( 0x76E3, "RollValue (NBR)", INT_59 );
    static final IonHandle  INT_60 = 
        create( 0x053B, "INT #60" );
    static final IonHandle INT_60_RESULT_NVR = 
        create( 0x5FBC, "Result (NVR)", INT_60 );
    static final IonHandle INT_60_TRIGGER_DR = 
        create( 0x6CF3, "Trigger (DR)", INT_60 );
    static final IonHandle INT_60_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_60 );
    static final IonHandle INT_60_INT_MODEENR = 
        create( 0x7C42, "Int Mode(ENR)", INT_60 );
    static final IonHandle INT_60_ROLLVALUE_NBR = 
        create( 0x76E4, "RollValue (NBR)", INT_60 );
    static final IonHandle  INT_61 = 
        create( 0x053C, "INT #61" );
    static final IonHandle INT_61_RESULT_NVR = 
        create( 0x5FBD, "Result (NVR)", INT_61 );
    static final IonHandle INT_61_TRIGGER_DR = 
        create( 0x6CF4, "Trigger (DR)", INT_61 );
    static final IonHandle INT_61_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_61 );
    static final IonHandle INT_61_INT_MODEENR = 
        create( 0x7C43, "Int Mode(ENR)", INT_61 );
    static final IonHandle INT_61_ROLLVALUE_NBR = 
        create( 0x76E5, "RollValue (NBR)", INT_61 );
    static final IonHandle  INT_62 = 
        create( 0x053D, "INT #62" );
    static final IonHandle INT_62_RESULT_NVR = 
        create( 0x5FBE, "Result (NVR)", INT_62 );
    static final IonHandle INT_62_TRIGGER_DR = 
        create( 0x6CF5, "Trigger (DR)", INT_62 );
    static final IonHandle INT_62_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_62 );
    static final IonHandle INT_62_INT_MODEENR = 
        create( 0x7C44, "Int Mode(ENR)", INT_62 );
    static final IonHandle INT_62_ROLLVALUE_NBR = 
        create( 0x76E6, "RollValue (NBR)", INT_62 );
    static final IonHandle  INT_63 = 
        create( 0x053E, "INT #63" );
    static final IonHandle INT_63_RESULT_NVR = 
        create( 0x5FBF, "Result (NVR)", INT_63 );
    static final IonHandle INT_63_TRIGGER_DR = 
        create( 0x6CF6, "Trigger (DR)", INT_63 );
    static final IonHandle INT_63_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_63 );
    static final IonHandle INT_63_INT_MODEENR = 
        create( 0x7C45, "Int Mode(ENR)", INT_63 );
    static final IonHandle INT_63_ROLLVALUE_NBR = 
        create( 0x76E7, "RollValue (NBR)", INT_63 );
    static final IonHandle  INT_64 = 
        create( 0x053F, "INT #64" );
    static final IonHandle INT_64_RESULT_NVR = 
        create( 0x5FC0, "Result (NVR)", INT_64 );
    static final IonHandle INT_64_TRIGGER_DR = 
        create( 0x6CF7, "Trigger (DR)", INT_64 );
    static final IonHandle INT_64_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_64 );
    static final IonHandle INT_64_INT_MODEENR = 
        create( 0x7C46, "Int Mode(ENR)", INT_64 );
    static final IonHandle INT_64_ROLLVALUE_NBR = 
        create( 0x76E8, "RollValue (NBR)", INT_64 );
    static final IonHandle  INT_65 = 
        create( 0x0540, "INT #65" );
    static final IonHandle INT_65_RESULT_NVR = 
        create( 0x5FC1, "Result (NVR)", INT_65 );
    static final IonHandle INT_65_TRIGGER_DR = 
        create( 0x6CF8, "Trigger (DR)", INT_65 );
    static final IonHandle INT_65_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_65 );
    static final IonHandle INT_65_INT_MODEENR = 
        create( 0x7C47, "Int Mode(ENR)", INT_65 );
    static final IonHandle INT_65_ROLLVALUE_NBR = 
        create( 0x76E9, "RollValue (NBR)", INT_65 );
    static final IonHandle  INT_66 = 
        create( 0x0541, "INT #66" );
    static final IonHandle INT_66_RESULT_NVR = 
        create( 0x5FC2, "Result (NVR)", INT_66 );
    static final IonHandle INT_66_TRIGGER_DR = 
        create( 0x6CF9, "Trigger (DR)", INT_66 );
    static final IonHandle INT_66_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_66 );
    static final IonHandle INT_66_INT_MODEENR = 
        create( 0x7C48, "Int Mode(ENR)", INT_66 );
    static final IonHandle INT_66_ROLLVALUE_NBR = 
        create( 0x76EA, "RollValue (NBR)", INT_66 );
    static final IonHandle  INT_67 = 
        create( 0x0542, "INT #67" );
    static final IonHandle INT_67_RESULT_NVR = 
        create( 0x5FC3, "Result (NVR)", INT_67 );
    static final IonHandle INT_67_TRIGGER_DR = 
        create( 0x6CFA, "Trigger (DR)", INT_67 );
    static final IonHandle INT_67_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_67 );
    static final IonHandle INT_67_INT_MODEENR = 
        create( 0x7C49, "Int Mode(ENR)", INT_67 );
    static final IonHandle INT_67_ROLLVALUE_NBR = 
        create( 0x76EB, "RollValue (NBR)", INT_67 );
    static final IonHandle  INT_68 = 
        create( 0x0543, "INT #68" );
    static final IonHandle INT_68_RESULT_NVR = 
        create( 0x5FC4, "Result (NVR)", INT_68 );
    static final IonHandle INT_68_TRIGGER_DR = 
        create( 0x6CFB, "Trigger (DR)", INT_68 );
    static final IonHandle INT_68_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_68 );
    static final IonHandle INT_68_INT_MODEENR = 
        create( 0x7C4A, "Int Mode(ENR)", INT_68 );
    static final IonHandle INT_68_ROLLVALUE_NBR = 
        create( 0x76EC, "RollValue (NBR)", INT_68 );
    static final IonHandle  INT_69 = 
        create( 0x0544, "INT #69" );
    static final IonHandle INT_69_RESULT_NVR = 
        create( 0x5FC5, "Result (NVR)", INT_69 );
    static final IonHandle INT_69_TRIGGER_DR = 
        create( 0x6CFC, "Trigger (DR)", INT_69 );
    static final IonHandle INT_69_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_69 );
    static final IonHandle INT_69_INT_MODEENR = 
        create( 0x7C4B, "Int Mode(ENR)", INT_69 );
    static final IonHandle INT_69_ROLLVALUE_NBR = 
        create( 0x76ED, "RollValue (NBR)", INT_69 );
    static final IonHandle  INT_70 = 
        create( 0x0545, "INT #70" );
    static final IonHandle INT_70_RESULT_NVR = 
        create( 0x5FC6, "Result (NVR)", INT_70 );
    static final IonHandle INT_70_TRIGGER_DR = 
        create( 0x6CFD, "Trigger (DR)", INT_70 );
    static final IonHandle INT_70_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_70 );
    static final IonHandle INT_70_INT_MODEENR = 
        create( 0x7C4C, "Int Mode(ENR)", INT_70 );
    static final IonHandle INT_70_ROLLVALUE_NBR = 
        create( 0x76EE, "RollValue (NBR)", INT_70 );
    static final IonHandle  INT_71 = 
        create( 0x0546, "INT #71" );
    static final IonHandle INT_71_RESULT_NVR = 
        create( 0x5FC7, "Result (NVR)", INT_71 );
    static final IonHandle INT_71_TRIGGER_DR = 
        create( 0x6CFE, "Trigger (DR)", INT_71 );
    static final IonHandle INT_71_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_71 );
    static final IonHandle INT_71_INT_MODEENR = 
        create( 0x7C5B, "Int Mode(ENR)", INT_71 );
    static final IonHandle INT_71_ROLLVALUE_NBR = 
        create( 0x76EF, "RollValue (NBR)", INT_71 );
    static final IonHandle  INT_72 = 
        create( 0x0547, "INT #72" );
    static final IonHandle INT_72_RESULT_NVR = 
        create( 0x5FC8, "Result (NVR)", INT_72 );
    static final IonHandle INT_72_TRIGGER_DR = 
        create( 0x6CFF, "Trigger (DR)", INT_72 );
    static final IonHandle INT_72_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_72 );
    static final IonHandle INT_72_INT_MODEENR = 
        create( 0x7C5C, "Int Mode(ENR)", INT_72 );
    static final IonHandle INT_72_ROLLVALUE_NBR = 
        create( 0x76F0, "RollValue (NBR)", INT_72 );
    static final IonHandle  INT_73 = 
        create( 0x0548, "INT #73" );
    static final IonHandle INT_73_RESULT_NVR = 
        create( 0x5FC9, "Result (NVR)", INT_73 );
    static final IonHandle INT_73_TRIGGER_DR = 
        create( 0x6D00, "Trigger (DR)", INT_73 );
    static final IonHandle INT_73_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_73 );
    static final IonHandle INT_73_INT_MODEENR = 
        create( 0x7C5D, "Int Mode(ENR)", INT_73 );
    static final IonHandle INT_73_ROLLVALUE_NBR = 
        create( 0x76F1, "RollValue (NBR)", INT_73 );
    static final IonHandle  INT_74 = 
        create( 0x0549, "INT #74" );
    static final IonHandle INT_74_RESULT_NVR = 
        create( 0x5FCA, "Result (NVR)", INT_74 );
    static final IonHandle INT_74_TRIGGER_DR = 
        create( 0x6D01, "Trigger (DR)", INT_74 );
    static final IonHandle INT_74_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_74 );
    static final IonHandle INT_74_INT_MODEENR = 
        create( 0x7C5E, "Int Mode(ENR)", INT_74 );
    static final IonHandle INT_74_ROLLVALUE_NBR = 
        create( 0x76F2, "RollValue (NBR)", INT_74 );
    static final IonHandle  INT_75 = 
        create( 0x054A, "INT #75" );
    static final IonHandle INT_75_RESULT_NVR = 
        create( 0x5FCB, "Result (NVR)", INT_75 );
    static final IonHandle INT_75_TRIGGER_DR = 
        create( 0x6D02, "Trigger (DR)", INT_75 );
    static final IonHandle INT_75_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_75 );
    static final IonHandle INT_75_INT_MODEENR = 
        create( 0x7C5F, "Int Mode(ENR)", INT_75 );
    static final IonHandle INT_75_ROLLVALUE_NBR = 
        create( 0x76F3, "RollValue (NBR)", INT_75 );
    static final IonHandle  INT_76 = 
        create( 0x054B, "INT #76" );
    static final IonHandle INT_76_RESULT_NVR = 
        create( 0x5FCC, "Result (NVR)", INT_76 );
    static final IonHandle INT_76_TRIGGER_DR = 
        create( 0x6D03, "Trigger (DR)", INT_76 );
    static final IonHandle INT_76_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_76 );
    static final IonHandle INT_76_INT_MODEENR = 
        create( 0x7C60, "Int Mode(ENR)", INT_76 );
    static final IonHandle INT_76_ROLLVALUE_NBR = 
        create( 0x76F4, "RollValue (NBR)", INT_76 );
    static final IonHandle  INT_77 = 
        create( 0x054C, "INT #77" );
    static final IonHandle INT_77_RESULT_NVR = 
        create( 0x5FCD, "Result (NVR)", INT_77 );
    static final IonHandle INT_77_TRIGGER_DR = 
        create( 0x6D04, "Trigger (DR)", INT_77 );
    static final IonHandle INT_77_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_77 );
    static final IonHandle INT_77_INT_MODEENR = 
        create( 0x7C61, "Int Mode(ENR)", INT_77 );
    static final IonHandle INT_77_ROLLVALUE_NBR = 
        create( 0x76F5, "RollValue (NBR)", INT_77 );
    static final IonHandle  INT_78 = 
        create( 0x054D, "INT #78" );
    static final IonHandle INT_78_RESULT_NVR = 
        create( 0x5FCE, "Result (NVR)", INT_78 );
    static final IonHandle INT_78_TRIGGER_DR = 
        create( 0x6D05, "Trigger (DR)", INT_78 );
    static final IonHandle INT_78_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_78 );
    static final IonHandle INT_78_INT_MODEENR = 
        create( 0x7C62, "Int Mode(ENR)", INT_78 );
    static final IonHandle INT_78_ROLLVALUE_NBR = 
        create( 0x76F6, "RollValue (NBR)", INT_78 );
    static final IonHandle  INT_79 = 
        create( 0x054E, "INT #79" );
    static final IonHandle INT_79_RESULT_NVR = 
        create( 0x5FCF, "Result (NVR)", INT_79 );
    static final IonHandle INT_79_TRIGGER_DR = 
        create( 0x6D06, "Trigger (DR)", INT_79 );
    static final IonHandle INT_79_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_79 );
    static final IonHandle INT_79_INT_MODEENR = 
        create( 0x7C63, "Int Mode(ENR)", INT_79 );
    static final IonHandle INT_79_ROLLVALUE_NBR = 
        create( 0x76F7, "RollValue (NBR)", INT_79 );
    static final IonHandle  INT_80 = 
        create( 0x054F, "INT #80" );
    static final IonHandle INT_80_RESULT_NVR = 
        create( 0x5FD0, "Result (NVR)", INT_80 );
    static final IonHandle INT_80_TRIGGER_DR = 
        create( 0x6D07, "Trigger (DR)", INT_80 );
    static final IonHandle INT_80_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_80 );
    static final IonHandle INT_80_INT_MODEENR = 
        create( 0x7C64, "Int Mode(ENR)", INT_80 );
    static final IonHandle INT_80_ROLLVALUE_NBR = 
        create( 0x76F8, "RollValue (NBR)", INT_80 );
    static final IonHandle  INT_81 = 
        create( 0x0550, "INT #81" );
    static final IonHandle INT_81_RESULT_NVR = 
        create( 0x5FD1, "Result (NVR)", INT_81 );
    static final IonHandle INT_81_TRIGGER_DR = 
        create( 0x6D08, "Trigger (DR)", INT_81 );
    static final IonHandle INT_81_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_81 );
    static final IonHandle INT_81_INT_MODEENR = 
        create( 0x7C65, "Int Mode(ENR)", INT_81 );
    static final IonHandle INT_81_ROLLVALUE_NBR = 
        create( 0x76F9, "RollValue (NBR)", INT_81 );
    static final IonHandle  INT_82 = 
        create( 0x0551, "INT #82" );
    static final IonHandle INT_82_RESULT_NVR = 
        create( 0x5FD2, "Result (NVR)", INT_82 );
    static final IonHandle INT_82_TRIGGER_DR = 
        create( 0x6D09, "Trigger (DR)", INT_82 );
    static final IonHandle INT_82_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_82 );
    static final IonHandle INT_82_INT_MODEENR = 
        create( 0x7C66, "Int Mode(ENR)", INT_82 );
    static final IonHandle INT_82_ROLLVALUE_NBR = 
        create( 0x76FA, "RollValue (NBR)", INT_82 );
    static final IonHandle  INT_83 = 
        create( 0x0552, "INT #83" );
    static final IonHandle INT_83_RESULT_NVR = 
        create( 0x5FD3, "Result (NVR)", INT_83 );
    static final IonHandle INT_83_TRIGGER_DR = 
        create( 0x6D0A, "Trigger (DR)", INT_83 );
    static final IonHandle INT_83_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_83 );
    static final IonHandle INT_83_INT_MODEENR = 
        create( 0x7C67, "Int Mode(ENR)", INT_83 );
    static final IonHandle INT_83_ROLLVALUE_NBR = 
        create( 0x76FB, "RollValue (NBR)", INT_83 );
    static final IonHandle  INT_84 = 
        create( 0x0553, "INT #84" );
    static final IonHandle INT_84_RESULT_NVR = 
        create( 0x5FD4, "Result (NVR)", INT_84 );
    static final IonHandle INT_84_TRIGGER_DR = 
        create( 0x6D0B, "Trigger (DR)", INT_84 );
    static final IonHandle INT_84_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_84 );
    static final IonHandle INT_84_INT_MODEENR = 
        create( 0x7C68, "Int Mode(ENR)", INT_84 );
    static final IonHandle INT_84_ROLLVALUE_NBR = 
        create( 0x76FC, "RollValue (NBR)", INT_84 );
    static final IonHandle  INT_85 = 
        create( 0x0554, "INT #85" );
    static final IonHandle INT_85_RESULT_NVR = 
        create( 0x5FD5, "Result (NVR)", INT_85 );
    static final IonHandle INT_85_TRIGGER_DR = 
        create( 0x6D0C, "Trigger (DR)", INT_85 );
    static final IonHandle INT_85_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_85 );
    static final IonHandle INT_85_INT_MODEENR = 
        create( 0x7C69, "Int Mode(ENR)", INT_85 );
    static final IonHandle INT_85_ROLLVALUE_NBR = 
        create( 0x76FD, "RollValue (NBR)", INT_85 );
    static final IonHandle  INT_86 = 
        create( 0x0555, "INT #86" );
    static final IonHandle INT_86_RESULT_NVR = 
        create( 0x5FD6, "Result (NVR)", INT_86 );
    static final IonHandle INT_86_TRIGGER_DR = 
        create( 0x6D0D, "Trigger (DR)", INT_86 );
    static final IonHandle INT_86_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_86 );
    static final IonHandle INT_86_INT_MODEENR = 
        create( 0x7C6A, "Int Mode(ENR)", INT_86 );
    static final IonHandle INT_86_ROLLVALUE_NBR = 
        create( 0x76FE, "RollValue (NBR)", INT_86 );
    static final IonHandle  INT_87 = 
        create( 0x0556, "INT #87" );
    static final IonHandle INT_87_RESULT_NVR = 
        create( 0x5FD7, "Result (NVR)", INT_87 );
    static final IonHandle INT_87_TRIGGER_DR = 
        create( 0x6D0E, "Trigger (DR)", INT_87 );
    static final IonHandle INT_87_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_87 );
    static final IonHandle INT_87_INT_MODEENR = 
        create( 0x7C6B, "Int Mode(ENR)", INT_87 );
    static final IonHandle INT_87_ROLLVALUE_NBR = 
        create( 0x76FF, "RollValue (NBR)", INT_87 );
    static final IonHandle  INT_88 = 
        create( 0x0557, "INT #88" );
    static final IonHandle INT_88_RESULT_NVR = 
        create( 0x5FD8, "Result (NVR)", INT_88 );
    static final IonHandle INT_88_TRIGGER_DR = 
        create( 0x6D0F, "Trigger (DR)", INT_88 );
    static final IonHandle INT_88_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_88 );
    static final IonHandle INT_88_INT_MODEENR = 
        create( 0x7C6C, "Int Mode(ENR)", INT_88 );
    static final IonHandle INT_88_ROLLVALUE_NBR = 
        create( 0x7700, "RollValue (NBR)", INT_88 );
    static final IonHandle  INT_89 = 
        create( 0x0558, "INT #89" );
    static final IonHandle INT_89_RESULT_NVR = 
        create( 0x5FD9, "Result (NVR)", INT_89 );
    static final IonHandle INT_89_TRIGGER_DR = 
        create( 0x6D10, "Trigger (DR)", INT_89 );
    static final IonHandle INT_89_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_89 );
    static final IonHandle INT_89_INT_MODEENR = 
        create( 0x7C6D, "Int Mode(ENR)", INT_89 );
    static final IonHandle INT_89_ROLLVALUE_NBR = 
        create( 0x7701, "RollValue (NBR)", INT_89 );
    static final IonHandle  INT_90 = 
        create( 0x0559, "INT #90" );
    static final IonHandle INT_90_RESULT_NVR = 
        create( 0x5FDA, "Result (NVR)", INT_90 );
    static final IonHandle INT_90_TRIGGER_DR = 
        create( 0x6D11, "Trigger (DR)", INT_90 );
    static final IonHandle INT_90_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_90 );
    static final IonHandle INT_90_INT_MODEENR = 
        create( 0x7C6E, "Int Mode(ENR)", INT_90 );
    static final IonHandle INT_90_ROLLVALUE_NBR = 
        create( 0x7702, "RollValue (NBR)", INT_90 );
    static final IonHandle  INT_91 = 
        create( 0x055A, "INT #91" );
    static final IonHandle INT_91_RESULT_NVR = 
        create( 0x4B66, "Result (NVR)", INT_91 );
    static final IonHandle INT_91_TRIGGER_DR = 
        create( 0x6EEA, "Trigger (DR)", INT_91 );
    static final IonHandle INT_91_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_91 );
    static final IonHandle INT_91_INT_MODEENR = 
        create( 0x7D11, "Int Mode(ENR)", INT_91 );
    static final IonHandle INT_91_ROLLVALUE_NBR = 
        create( 0x408C, "RollValue (NBR)", INT_91 );
    static final IonHandle  INT_92 = 
        create( 0x055B, "INT #92" );
    static final IonHandle INT_92_RESULT_NVR = 
        create( 0x4B67, "Result (NVR)", INT_92 );
    static final IonHandle INT_92_TRIGGER_DR = 
        create( 0x6EEB, "Trigger (DR)", INT_92 );
    static final IonHandle INT_92_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_92 );
    static final IonHandle INT_92_INT_MODEENR = 
        create( 0x7D12, "Int Mode(ENR)", INT_92 );
    static final IonHandle INT_92_ROLLVALUE_NBR = 
        create( 0x408D, "RollValue (NBR)", INT_92 );
    static final IonHandle  INT_93 = 
        create( 0x055C, "INT #93" );
    static final IonHandle INT_93_RESULT_NVR = 
        create( 0x4B68, "Result (NVR)", INT_93 );
    static final IonHandle INT_93_TRIGGER_DR = 
        create( 0x6EEC, "Trigger (DR)", INT_93 );
    static final IonHandle INT_93_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_93 );
    static final IonHandle INT_93_INT_MODEENR = 
        create( 0x7D13, "Int Mode(ENR)", INT_93 );
    static final IonHandle INT_93_ROLLVALUE_NBR = 
        create( 0x408E, "RollValue (NBR)", INT_93 );
    static final IonHandle  INT_94 = 
        create( 0x055D, "INT #94" );
    static final IonHandle INT_94_RESULT_NVR = 
        create( 0x4B69, "Result (NVR)", INT_94 );
    static final IonHandle INT_94_TRIGGER_DR = 
        create( 0x6EED, "Trigger (DR)", INT_94 );
    static final IonHandle INT_94_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_94 );
    static final IonHandle INT_94_INT_MODEENR = 
        create( 0x7D14, "Int Mode(ENR)", INT_94 );
    static final IonHandle INT_94_ROLLVALUE_NBR = 
        create( 0x408F, "RollValue (NBR)", INT_94 );
    static final IonHandle  INT_95 = 
        create( 0x055E, "INT #95" );
    static final IonHandle INT_95_RESULT_NVR = 
        create( 0x4B6A, "Result (NVR)", INT_95 );
    static final IonHandle INT_95_TRIGGER_DR = 
        create( 0x6EEE, "Trigger (DR)", INT_95 );
    static final IonHandle INT_95_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_95 );
    static final IonHandle INT_95_INT_MODEENR = 
        create( 0x7D15, "Int Mode(ENR)", INT_95 );
    static final IonHandle INT_95_ROLLVALUE_NBR = 
        create( 0x4090, "RollValue (NBR)", INT_95 );
    static final IonHandle  INT_96 = 
        create( 0x055F, "INT #96" );
    static final IonHandle INT_96_RESULT_NVR = 
        create( 0x4B6B, "Result (NVR)", INT_96 );
    static final IonHandle INT_96_TRIGGER_DR = 
        create( 0x6EEF, "Trigger (DR)", INT_96 );
    static final IonHandle INT_96_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_96 );
    static final IonHandle INT_96_INT_MODEENR = 
        create( 0x7D16, "Int Mode(ENR)", INT_96 );
    static final IonHandle INT_96_ROLLVALUE_NBR = 
        create( 0x4091, "RollValue (NBR)", INT_96 );
    static final IonHandle  INT_97 = 
        create( 0x0560, "INT #97" );
    static final IonHandle INT_97_RESULT_NVR = 
        create( 0x4B6C, "Result (NVR)", INT_97 );
    static final IonHandle INT_97_TRIGGER_DR = 
        create( 0x6EF0, "Trigger (DR)", INT_97 );
    static final IonHandle INT_97_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_97 );
    static final IonHandle INT_97_INT_MODEENR = 
        create( 0x7D17, "Int Mode(ENR)", INT_97 );
    static final IonHandle INT_97_ROLLVALUE_NBR = 
        create( 0x4092, "RollValue (NBR)", INT_97 );
    static final IonHandle  INT_98 = 
        create( 0x0561, "INT #98" );
    static final IonHandle INT_98_RESULT_NVR = 
        create( 0x4B6D, "Result (NVR)", INT_98 );
    static final IonHandle INT_98_TRIGGER_DR = 
        create( 0x6EF1, "Trigger (DR)", INT_98 );
    static final IonHandle INT_98_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_98 );
    static final IonHandle INT_98_INT_MODEENR = 
        create( 0x7D18, "Int Mode(ENR)", INT_98 );
    static final IonHandle INT_98_ROLLVALUE_NBR = 
        create( 0x4093, "RollValue (NBR)", INT_98 );
    static final IonHandle  INT_99 = 
        create( 0x0562, "INT #99" );
    static final IonHandle INT_99_RESULT_NVR = 
        create( 0x4B6E, "Result (NVR)", INT_99 );
    static final IonHandle INT_99_TRIGGER_DR = 
        create( 0x6EF2, "Trigger (DR)", INT_99 );
    static final IonHandle INT_99_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_99 );
    static final IonHandle INT_99_INT_MODEENR = 
        create( 0x7D19, "Int Mode(ENR)", INT_99 );
    static final IonHandle INT_99_ROLLVALUE_NBR = 
        create( 0x4094, "RollValue (NBR)", INT_99 );
    static final IonHandle  INT_100 = 
        create( 0x0563, "INT #100" );
    static final IonHandle INT_100_RESULT_NVR = 
        create( 0x4B6F, "Result (NVR)", INT_100 );
    static final IonHandle INT_100_TRIGGER_DR = 
        create( 0x6EF3, "Trigger (DR)", INT_100 );
    static final IonHandle INT_100_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_100 );
    static final IonHandle INT_100_INT_MODEENR = 
        create( 0x7D1A, "Int Mode(ENR)", INT_100 );
    static final IonHandle INT_100_ROLLVALUE_NBR = 
        create( 0x4095, "RollValue (NBR)", INT_100 );
    static final IonHandle  INT_101 = 
        create( 0x0564, "INT #101" );
    static final IonHandle INT_101_RESULT_NVR = 
        create( 0x4B70, "Result (NVR)", INT_101 );
    static final IonHandle INT_101_TRIGGER_DR = 
        create( 0x6EF4, "Trigger (DR)", INT_101 );
    static final IonHandle INT_101_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_101 );
    static final IonHandle INT_101_INT_MODEENR = 
        create( 0x7D1B, "Int Mode(ENR)", INT_101 );
    static final IonHandle INT_101_ROLLVALUE_NBR = 
        create( 0x4096, "RollValue (NBR)", INT_101 );
    static final IonHandle  INT_102 = 
        create( 0x0565, "INT #102" );
    static final IonHandle INT_102_RESULT_NVR = 
        create( 0x4B71, "Result (NVR)", INT_102 );
    static final IonHandle INT_102_TRIGGER_DR = 
        create( 0x6EF5, "Trigger (DR)", INT_102 );
    static final IonHandle INT_102_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_102 );
    static final IonHandle INT_102_INT_MODEENR = 
        create( 0x7D1C, "Int Mode(ENR)", INT_102 );
    static final IonHandle INT_102_ROLLVALUE_NBR = 
        create( 0x4097, "RollValue (NBR)", INT_102 );
    static final IonHandle  INT_103 = 
        create( 0x0566, "INT #103" );
    static final IonHandle INT_103_RESULT_NVR = 
        create( 0x4B72, "Result (NVR)", INT_103 );
    static final IonHandle INT_103_TRIGGER_DR = 
        create( 0x6EF6, "Trigger (DR)", INT_103 );
    static final IonHandle INT_103_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_103 );
    static final IonHandle INT_103_INT_MODEENR = 
        create( 0x7D1D, "Int Mode(ENR)", INT_103 );
    static final IonHandle INT_103_ROLLVALUE_NBR = 
        create( 0x4098, "RollValue (NBR)", INT_103 );
    static final IonHandle  INT_104 = 
        create( 0x0567, "INT #104" );
    static final IonHandle INT_104_RESULT_NVR = 
        create( 0x4B73, "Result (NVR)", INT_104 );
    static final IonHandle INT_104_TRIGGER_DR = 
        create( 0x6EF7, "Trigger (DR)", INT_104 );
    static final IonHandle INT_104_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_104 );
    static final IonHandle INT_104_INT_MODEENR = 
        create( 0x7D1E, "Int Mode(ENR)", INT_104 );
    static final IonHandle INT_104_ROLLVALUE_NBR = 
        create( 0x4099, "RollValue (NBR)", INT_104 );
    static final IonHandle  INT_105 = 
        create( 0x0568, "INT #105" );
    static final IonHandle INT_105_RESULT_NVR = 
        create( 0x4B74, "Result (NVR)", INT_105 );
    static final IonHandle INT_105_TRIGGER_DR = 
        create( 0x6EF8, "Trigger (DR)", INT_105 );
    static final IonHandle INT_105_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_105 );
    static final IonHandle INT_105_INT_MODEENR = 
        create( 0x7D1F, "Int Mode(ENR)", INT_105 );
    static final IonHandle INT_105_ROLLVALUE_NBR = 
        create( 0x409A, "RollValue (NBR)", INT_105 );
    static final IonHandle  INT_106 = 
        create( 0x0569, "INT #106" );
    static final IonHandle INT_106_RESULT_NVR = 
        create( 0x4B75, "Result (NVR)", INT_106 );
    static final IonHandle INT_106_TRIGGER_DR = 
        create( 0x6EF9, "Trigger (DR)", INT_106 );
    static final IonHandle INT_106_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_106 );
    static final IonHandle INT_106_INT_MODEENR = 
        create( 0x7D20, "Int Mode(ENR)", INT_106 );
    static final IonHandle INT_106_ROLLVALUE_NBR = 
        create( 0x409B, "RollValue (NBR)", INT_106 );
    static final IonHandle  INT_107 = 
        create( 0x056A, "INT #107" );
    static final IonHandle INT_107_RESULT_NVR = 
        create( 0x4B76, "Result (NVR)", INT_107 );
    static final IonHandle INT_107_TRIGGER_DR = 
        create( 0x6EFA, "Trigger (DR)", INT_107 );
    static final IonHandle INT_107_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_107 );
    static final IonHandle INT_107_INT_MODEENR = 
        create( 0x7D21, "Int Mode(ENR)", INT_107 );
    static final IonHandle INT_107_ROLLVALUE_NBR = 
        create( 0x409C, "RollValue (NBR)", INT_107 );
    static final IonHandle  INT_108 = 
        create( 0x056B, "INT #108" );
    static final IonHandle INT_108_RESULT_NVR = 
        create( 0x4B77, "Result (NVR)", INT_108 );
    static final IonHandle INT_108_TRIGGER_DR = 
        create( 0x6EFB, "Trigger (DR)", INT_108 );
    static final IonHandle INT_108_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_108 );
    static final IonHandle INT_108_INT_MODEENR = 
        create( 0x7D22, "Int Mode(ENR)", INT_108 );
    static final IonHandle INT_108_ROLLVALUE_NBR = 
        create( 0x409D, "RollValue (NBR)", INT_108 );
    static final IonHandle  INT_109 = 
        create( 0x056C, "INT #109" );
    static final IonHandle INT_109_RESULT_NVR = 
        create( 0x4B78, "Result (NVR)", INT_109 );
    static final IonHandle INT_109_TRIGGER_DR = 
        create( 0x6EFC, "Trigger (DR)", INT_109 );
    static final IonHandle INT_109_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_109 );
    static final IonHandle INT_109_INT_MODEENR = 
        create( 0x7D23, "Int Mode(ENR)", INT_109 );
    static final IonHandle INT_109_ROLLVALUE_NBR = 
        create( 0x409E, "RollValue (NBR)", INT_109 );
    static final IonHandle  INT_110 = 
        create( 0x056D, "INT #110" );
    static final IonHandle INT_110_RESULT_NVR = 
        create( 0x4B79, "Result (NVR)", INT_110 );
    static final IonHandle INT_110_TRIGGER_DR = 
        create( 0x6EFD, "Trigger (DR)", INT_110 );
    static final IonHandle INT_110_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_110 );
    static final IonHandle INT_110_INT_MODEENR = 
        create( 0x7D24, "Int Mode(ENR)", INT_110 );
    static final IonHandle INT_110_ROLLVALUE_NBR = 
        create( 0x409F, "RollValue (NBR)", INT_110 );
    static final IonHandle  INT_111 = 
        create( 0x056E, "INT #111" );
    static final IonHandle INT_111_RESULT_NVR = 
        create( 0x3F32, "Result (NVR)", INT_111 );
    static final IonHandle INT_111_TRIGGER_DR = 
        create( 0x3114, "Trigger (DR)", INT_111 );
    static final IonHandle INT_111_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_111 );
    static final IonHandle INT_111_INT_MODEENR = 
        create( 0x7FC4, "Int Mode(ENR)", INT_111 );
    static final IonHandle INT_111_ROLLVALUE_NBR = 
        create( 0x4645, "RollValue (NBR)", INT_111 );
    static final IonHandle  INT_112 = 
        create( 0x056F, "INT #112" );
    static final IonHandle INT_112_RESULT_NVR = 
        create( 0x3F33, "Result (NVR)", INT_112 );
    static final IonHandle INT_112_TRIGGER_DR = 
        create( 0x3115, "Trigger (DR)", INT_112 );
    static final IonHandle INT_112_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_112 );
    static final IonHandle INT_112_INT_MODEENR = 
        create( 0x7FC5, "Int Mode(ENR)", INT_112 );
    static final IonHandle INT_112_ROLLVALUE_NBR = 
        create( 0x4646, "RollValue (NBR)", INT_112 );
    static final IonHandle  INT_113 = 
        create( 0x0570, "INT #113" );
    static final IonHandle INT_113_RESULT_NVR = 
        create( 0x3F34, "Result (NVR)", INT_113 );
    static final IonHandle INT_113_TRIGGER_DR = 
        create( 0x3116, "Trigger (DR)", INT_113 );
    static final IonHandle INT_113_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_113 );
    static final IonHandle INT_113_INT_MODEENR = 
        create( 0x7FC6, "Int Mode(ENR)", INT_113 );
    static final IonHandle INT_113_ROLLVALUE_NBR = 
        create( 0x4647, "RollValue (NBR)", INT_113 );
    static final IonHandle  INT_114 = 
        create( 0x0571, "INT #114" );
    static final IonHandle INT_114_RESULT_NVR = 
        create( 0x3F35, "Result (NVR)", INT_114 );
    static final IonHandle INT_114_TRIGGER_DR = 
        create( 0x3117, "Trigger (DR)", INT_114 );
    static final IonHandle INT_114_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_114 );
    static final IonHandle INT_114_INT_MODEENR = 
        create( 0x7FC7, "Int Mode(ENR)", INT_114 );
    static final IonHandle INT_114_ROLLVALUE_NBR = 
        create( 0x4648, "RollValue (NBR)", INT_114 );
    static final IonHandle  INT_115 = 
        create( 0x0572, "INT #115" );
    static final IonHandle INT_115_RESULT_NVR = 
        create( 0x3F36, "Result (NVR)", INT_115 );
    static final IonHandle INT_115_TRIGGER_DR = 
        create( 0x3118, "Trigger (DR)", INT_115 );
    static final IonHandle INT_115_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_115 );
    static final IonHandle INT_115_INT_MODEENR = 
        create( 0x7FC8, "Int Mode(ENR)", INT_115 );
    static final IonHandle INT_115_ROLLVALUE_NBR = 
        create( 0x4649, "RollValue (NBR)", INT_115 );
    static final IonHandle  INT_116 = 
        create( 0x0573, "INT #116" );
    static final IonHandle INT_116_RESULT_NVR = 
        create( 0x3F37, "Result (NVR)", INT_116 );
    static final IonHandle INT_116_TRIGGER_DR = 
        create( 0x3119, "Trigger (DR)", INT_116 );
    static final IonHandle INT_116_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_116 );
    static final IonHandle INT_116_INT_MODEENR = 
        create( 0x7FC9, "Int Mode(ENR)", INT_116 );
    static final IonHandle INT_116_ROLLVALUE_NBR = 
        create( 0x464A, "RollValue (NBR)", INT_116 );
    static final IonHandle  INT_117 = 
        create( 0x0574, "INT #117" );
    static final IonHandle INT_117_RESULT_NVR = 
        create( 0x3F38, "Result (NVR)", INT_117 );
    static final IonHandle INT_117_TRIGGER_DR = 
        create( 0x311A, "Trigger (DR)", INT_117 );
    static final IonHandle INT_117_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_117 );
    static final IonHandle INT_117_INT_MODEENR = 
        create( 0x7FCA, "Int Mode(ENR)", INT_117 );
    static final IonHandle INT_117_ROLLVALUE_NBR = 
        create( 0x464B, "RollValue (NBR)", INT_117 );
    static final IonHandle  INT_118 = 
        create( 0x0575, "INT #118" );
    static final IonHandle INT_118_RESULT_NVR = 
        create( 0x3F39, "Result (NVR)", INT_118 );
    static final IonHandle INT_118_TRIGGER_DR = 
        create( 0x311B, "Trigger (DR)", INT_118 );
    static final IonHandle INT_118_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_118 );
    static final IonHandle INT_118_INT_MODEENR = 
        create( 0x7FCB, "Int Mode(ENR)", INT_118 );
    static final IonHandle INT_118_ROLLVALUE_NBR = 
        create( 0x464C, "RollValue (NBR)", INT_118 );
    static final IonHandle  INT_119 = 
        create( 0x0576, "INT #119" );
    static final IonHandle INT_119_RESULT_NVR = 
        create( 0x3F3A, "Result (NVR)", INT_119 );
    static final IonHandle INT_119_TRIGGER_DR = 
        create( 0x311C, "Trigger (DR)", INT_119 );
    static final IonHandle INT_119_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_119 );
    static final IonHandle INT_119_INT_MODEENR = 
        create( 0x7FCC, "Int Mode(ENR)", INT_119 );
    static final IonHandle INT_119_ROLLVALUE_NBR = 
        create( 0x464D, "RollValue (NBR)", INT_119 );
    static final IonHandle  INT_120 = 
        create( 0x0577, "INT #120" );
    static final IonHandle INT_120_RESULT_NVR = 
        create( 0x3F3B, "Result (NVR)", INT_120 );
    static final IonHandle INT_120_TRIGGER_DR = 
        create( 0x311D, "Trigger (DR)", INT_120 );
    static final IonHandle INT_120_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_120 );
    static final IonHandle INT_120_INT_MODEENR = 
        create( 0x7FCD, "Int Mode(ENR)", INT_120 );
    static final IonHandle INT_120_ROLLVALUE_NBR = 
        create( 0x464E, "RollValue (NBR)", INT_120 );
    static final IonHandle  INT_121 = 
        create( 0x0578, "INT #121" );
    static final IonHandle INT_121_RESULT_NVR = 
        create( 0x3F3C, "Result (NVR)", INT_121 );
    static final IonHandle INT_121_TRIGGER_DR = 
        create( 0x311E, "Trigger (DR)", INT_121 );
    static final IonHandle INT_121_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_121 );
    static final IonHandle INT_121_INT_MODEENR = 
        create( 0x7FCE, "Int Mode(ENR)", INT_121 );
    static final IonHandle INT_121_ROLLVALUE_NBR = 
        create( 0x464F, "RollValue (NBR)", INT_121 );
    static final IonHandle  INT_122 = 
        create( 0x0579, "INT #122" );
    static final IonHandle INT_122_RESULT_NVR = 
        create( 0x3F3D, "Result (NVR)", INT_122 );
    static final IonHandle INT_122_TRIGGER_DR = 
        create( 0x311F, "Trigger (DR)", INT_122 );
    static final IonHandle INT_122_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_122 );
    static final IonHandle INT_122_INT_MODEENR = 
        create( 0x7FCF, "Int Mode(ENR)", INT_122 );
    static final IonHandle INT_122_ROLLVALUE_NBR = 
        create( 0x4650, "RollValue (NBR)", INT_122 );
    static final IonHandle  INT_123 = 
        create( 0x057A, "INT #123" );
    static final IonHandle INT_123_RESULT_NVR = 
        create( 0x3F3E, "Result (NVR)", INT_123 );
    static final IonHandle INT_123_TRIGGER_DR = 
        create( 0x3120, "Trigger (DR)", INT_123 );
    static final IonHandle INT_123_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_123 );
    static final IonHandle INT_123_INT_MODEENR = 
        create( 0x7FD0, "Int Mode(ENR)", INT_123 );
    static final IonHandle INT_123_ROLLVALUE_NBR = 
        create( 0x4651, "RollValue (NBR)", INT_123 );
    static final IonHandle  INT_124 = 
        create( 0x057B, "INT #124" );
    static final IonHandle INT_124_RESULT_NVR = 
        create( 0x3F3F, "Result (NVR)", INT_124 );
    static final IonHandle INT_124_TRIGGER_DR = 
        create( 0x3121, "Trigger (DR)", INT_124 );
    static final IonHandle INT_124_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_124 );
    static final IonHandle INT_124_INT_MODEENR = 
        create( 0x7FD1, "Int Mode(ENR)", INT_124 );
    static final IonHandle INT_124_ROLLVALUE_NBR = 
        create( 0x4652, "RollValue (NBR)", INT_124 );
    static final IonHandle  INT_125 = 
        create( 0x057C, "INT #125" );
    static final IonHandle INT_125_RESULT_NVR = 
        create( 0x3F40, "Result (NVR)", INT_125 );
    static final IonHandle INT_125_TRIGGER_DR = 
        create( 0x3122, "Trigger (DR)", INT_125 );
    static final IonHandle INT_125_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_125 );
    static final IonHandle INT_125_INT_MODEENR = 
        create( 0x7FD2, "Int Mode(ENR)", INT_125 );
    static final IonHandle INT_125_ROLLVALUE_NBR = 
        create( 0x4653, "RollValue (NBR)", INT_125 );
    static final IonHandle  INT_126 = 
        create( 0x057D, "INT #126" );
    static final IonHandle INT_126_RESULT_NVR = 
        create( 0x3F41, "Result (NVR)", INT_126 );
    static final IonHandle INT_126_TRIGGER_DR = 
        create( 0x3123, "Trigger (DR)", INT_126 );
    static final IonHandle INT_126_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_126 );
    static final IonHandle INT_126_INT_MODEENR = 
        create( 0x7FD3, "Int Mode(ENR)", INT_126 );
    static final IonHandle INT_126_ROLLVALUE_NBR = 
        create( 0x4654, "RollValue (NBR)", INT_126 );
    static final IonHandle  INT_127 = 
        create( 0x057E, "INT #127" );
    static final IonHandle INT_127_RESULT_NVR = 
        create( 0x3F42, "Result (NVR)", INT_127 );
    static final IonHandle INT_127_TRIGGER_DR = 
        create( 0x3124, "Trigger (DR)", INT_127 );
    static final IonHandle INT_127_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_127 );
    static final IonHandle INT_127_INT_MODEENR = 
        create( 0x7FD4, "Int Mode(ENR)", INT_127 );
    static final IonHandle INT_127_ROLLVALUE_NBR = 
        create( 0x4655, "RollValue (NBR)", INT_127 );
    static final IonHandle  INT_128 = 
        create( 0x057F, "INT #128" );
    static final IonHandle INT_128_RESULT_NVR = 
        create( 0x3F43, "Result (NVR)", INT_128 );
    static final IonHandle INT_128_TRIGGER_DR = 
        create( 0x3125, "Trigger (DR)", INT_128 );
    static final IonHandle INT_128_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", INT_128 );
    static final IonHandle INT_128_INT_MODEENR = 
        create( 0x7FD5, "Int Mode(ENR)", INT_128 );
    static final IonHandle INT_128_ROLLVALUE_NBR = 
        create( 0x4656, "RollValue (NBR)", INT_128 );
    static final IonHandle  REC_1 = 
        create( 0x800, "REC #1" );
    static final IonHandle REC_1_RECORDER_LOG_LR = 
        create( 0x0F80, "Recorder Log (LR)", REC_1 );
    static final IonHandle REC_1_RECORD_LEFT_NVR = 
        create( 0x5D7B, "Record Left (NVR)", REC_1 );
    static final IonHandle REC_1_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_1 );
    static final IonHandle REC_1_RECORDER_MODE_ENR = 
        create( 0x7918, "Recorder Mode (ENR)", REC_1 );
    static final IonHandle  REC_2 = 
        create( 0x0801, "REC #2" );
    static final IonHandle REC_2_RECORDER_LOG_LR = 
        create( 0x0F81, "Recorder Log (LR)", REC_2 );
    static final IonHandle REC_2_RECORD_LEFT_NVR = 
        create( 0x5D7C, "Record Left (NVR)", REC_2 );
    static final IonHandle REC_2_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_2 );
    static final IonHandle REC_2_RECORDER_MODE_ENR = 
        create( 0x7919, "Recorder Mode (ENR)", REC_2 );
    static final IonHandle  REC_3 = 
        create( 0x0802, "REC #3" );
    static final IonHandle REC_3_RECORDER_LOG_LR = 
        create( 0x0F82, "Recorder Log (LR)", REC_3 );
    static final IonHandle REC_3_RECORD_LEFT_NVR = 
        create( 0x5D7D, "Record Left (NVR)", REC_3 );
    static final IonHandle REC_3_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_3 );
    static final IonHandle REC_3_RECORDER_MODE_ENR = 
        create( 0x791A, "Recorder Mode (ENR)", REC_3 );
    static final IonHandle  REC_4 = 
        create( 0x0803, "REC #4" );
    static final IonHandle REC_4_RECORDER_LOG_LR = 
        create( 0x0F83, "Recorder Log (LR)", REC_4 );
    static final IonHandle REC_4_RECORD_LEFT_NVR = 
        create( 0x5D7E, "Record Left (NVR)", REC_4 );
    static final IonHandle REC_4_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_4 );
    static final IonHandle REC_4_RECORDER_MODE_ENR = 
        create( 0x791B, "Recorder Mode (ENR)", REC_4 );
    static final IonHandle  REC_5 = 
        create( 0x0804, "REC #5" );
    static final IonHandle REC_5_RECORDER_LOG_LR = 
        create( 0x0F84, "Recorder Log (LR)", REC_5 );
    static final IonHandle REC_5_RECORD_LEFT_NVR = 
        create( 0x5D7F, "Record Left (NVR)", REC_5 );
    static final IonHandle REC_5_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_5 );
    static final IonHandle REC_5_RECORDER_MODE_ENR = 
        create( 0x791C, "Recorder Mode (ENR)", REC_5 );
    static final IonHandle  REC_6 = 
        create( 0x0805, "REC #6" );
    static final IonHandle REC_6_RECORDER_LOG_LR = 
        create( 0x0F85, "Recorder Log (LR)", REC_6 );
    static final IonHandle REC_6_RECORD_LEFT_NVR = 
        create( 0x5D80, "Record Left (NVR)", REC_6 );
    static final IonHandle REC_6_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_6 );
    static final IonHandle REC_6_RECORDER_MODE_ENR = 
        create( 0x791D, "Recorder Mode (ENR)", REC_6 );
    static final IonHandle  REC_7 = 
        create( 0x0806, "REC #7" );
    static final IonHandle REC_7_RECORDER_LOG_LR = 
        create( 0x0F86, "Recorder Log (LR)", REC_7 );
    static final IonHandle REC_7_RECORD_LEFT_NVR = 
        create( 0x5D81, "Record Left (NVR)", REC_7 );
    static final IonHandle REC_7_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_7 );
    static final IonHandle REC_7_RECORDER_MODE_ENR = 
        create( 0x791E, "Recorder Mode (ENR)", REC_7 );
    static final IonHandle  REC_8 = 
        create( 0x0807, "REC #8" );
    static final IonHandle REC_8_RECORDER_LOG_LR = 
        create( 0x0F87, "Recorder Log (LR)", REC_8 );
    static final IonHandle REC_8_RECORD_LEFT_NVR = 
        create( 0x5D82, "Record Left (NVR)", REC_8 );
    static final IonHandle REC_8_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_8 );
    static final IonHandle REC_8_RECORDER_MODE_ENR = 
        create( 0x791F, "Recorder Mode (ENR)", REC_8 );
    static final IonHandle  REC_9 = 
        create( 0x0808, "REC #9" );
    static final IonHandle REC_9_RECORDER_LOG_LR = 
        create( 0x0F88, "Recorder Log (LR)", REC_9 );
    static final IonHandle REC_9_RECORD_LEFT_NVR = 
        create( 0x5D83, "Record Left (NVR)", REC_9 );
    static final IonHandle REC_9_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_9 );
    static final IonHandle REC_9_RECORDER_MODE_ENR = 
        create( 0x7920, "Recorder Mode (ENR)", REC_9 );
    static final IonHandle  REC_10 = 
        create( 0x0809, "REC #10" );
    static final IonHandle REC_10_RECORDER_LOG_LR = 
        create( 0x0F89, "Recorder Log (LR)", REC_10 );
    static final IonHandle REC_10_RECORD_LEFT_NVR = 
        create( 0x5D84, "Record Left (NVR)", REC_10 );
    static final IonHandle REC_10_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_10 );
    static final IonHandle REC_10_RECORDER_MODE_ENR = 
        create( 0x7921, "Recorder Mode (ENR)", REC_10 );
    static final IonHandle  REC_11 = 
        create( 0x080A, "REC #11" );
    static final IonHandle REC_11_RECORDER_LOG_LR = 
        create( 0x0F8A, "Recorder Log (LR)", REC_11 );
    static final IonHandle REC_11_RECORD_LEFT_NVR = 
        create( 0x5D85, "Record Left (NVR)", REC_11 );
    static final IonHandle REC_11_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_11 );
    static final IonHandle REC_11_RECORDER_MODE_ENR = 
        create( 0x7922, "Recorder Mode (ENR)", REC_11 );
    static final IonHandle  REC_12 = 
        create( 0x080B, "REC #12" );
    static final IonHandle REC_12_RECORDER_LOG_LR = 
        create( 0x0F8B, "Recorder Log (LR)", REC_12 );
    static final IonHandle REC_12_RECORD_LEFT_NVR = 
        create( 0x5D86, "Record Left (NVR)", REC_12 );
    static final IonHandle REC_12_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_12 );
    static final IonHandle REC_12_RECORDER_MODE_ENR = 
        create( 0x7923, "Recorder Mode (ENR)", REC_12 );
    static final IonHandle  REC_13 = 
        create( 0x080C, "REC #13" );
    static final IonHandle REC_13_RECORDER_LOG_LR = 
        create( 0x0F8C, "Recorder Log (LR)", REC_13 );
    static final IonHandle REC_13_RECORD_LEFT_NVR = 
        create( 0x5D87, "Record Left (NVR)", REC_13 );
    static final IonHandle REC_13_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_13 );
    static final IonHandle REC_13_RECORDER_MODE_ENR = 
        create( 0x7924, "Recorder Mode (ENR)", REC_13 );
    static final IonHandle  REC_14 = 
        create( 0x080D, "REC #14" );
    static final IonHandle REC_14_RECORDER_LOG_LR = 
        create( 0x0F8D, "Recorder Log (LR)", REC_14 );
    static final IonHandle REC_14_RECORD_LEFT_NVR = 
        create( 0x5D88, "Record Left (NVR)", REC_14 );
    static final IonHandle REC_14_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_14 );
    static final IonHandle REC_14_RECORDER_MODE_ENR = 
        create( 0x7925, "Recorder Mode (ENR)", REC_14 );
    static final IonHandle  REC_15 = 
        create( 0x080E, "REC #15" );
    static final IonHandle REC_15_RECORDER_LOG_LR = 
        create( 0x0F8E, "Recorder Log (LR)", REC_15 );
    static final IonHandle REC_15_RECORD_LEFT_NVR = 
        create( 0x5D89, "Record Left (NVR)", REC_15 );
    static final IonHandle REC_15_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_15 );
    static final IonHandle REC_15_RECORDER_MODE_ENR = 
        create( 0x7926, "Recorder Mode (ENR)", REC_15 );
    static final IonHandle  REC_16 = 
        create( 0x080F, "REC #16" );
    static final IonHandle REC_16_RECORDER_LOG_LR = 
        create( 0x0F8F, "Recorder Log (LR)", REC_16 );
    static final IonHandle REC_16_RECORD_LEFT_NVR = 
        create( 0x5D8A, "Record Left (NVR)", REC_16 );
    static final IonHandle REC_16_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_16 );
    static final IonHandle REC_16_RECORDER_MODE_ENR = 
        create( 0x7927, "Recorder Mode (ENR)", REC_16 );
    static final IonHandle  REC_17 = 
        create( 0x0810, "REC #17" );
    static final IonHandle REC_17_RECORDER_LOG_LR = 
        create( 0x0F90, "Recorder Log (LR)", REC_17 );
    static final IonHandle REC_17_RECORD_LEFT_NVR = 
        create( 0x5D8B, "Record Left (NVR)", REC_17 );
    static final IonHandle REC_17_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_17 );
    static final IonHandle REC_17_RECORDER_MODE_ENR = 
        create( 0x7928, "Recorder Mode (ENR)", REC_17 );
    static final IonHandle  REC_18 = 
        create( 0x0811, "REC #18" );
    static final IonHandle REC_18_RECORDER_LOG_LR = 
        create( 0x0F91, "Recorder Log (LR)", REC_18 );
    static final IonHandle REC_18_RECORD_LEFT_NVR = 
        create( 0x5D8C, "Record Left (NVR)", REC_18 );
    static final IonHandle REC_18_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_18 );
    static final IonHandle REC_18_RECORDER_MODE_ENR = 
        create( 0x7929, "Recorder Mode (ENR)", REC_18 );
    static final IonHandle  REC_19 = 
        create( 0x0812, "REC #19" );
    static final IonHandle REC_19_RECORDER_LOG_LR = 
        create( 0x0F92, "Recorder Log (LR)", REC_19 );
    static final IonHandle REC_19_RECORD_LEFT_NVR = 
        create( 0x5D8D, "Record Left (NVR)", REC_19 );
    static final IonHandle REC_19_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_19 );
    static final IonHandle REC_19_RECORDER_MODE_ENR = 
        create( 0x792A, "Recorder Mode (ENR)", REC_19 );
    static final IonHandle  REC_20 = 
        create( 0x0813, "REC #20" );
    static final IonHandle REC_20_RECORDER_LOG_LR = 
        create( 0x0F93, "Recorder Log (LR)", REC_20 );
    static final IonHandle REC_20_RECORD_LEFT_NVR = 
        create( 0x5D8E, "Record Left (NVR)", REC_20 );
    static final IonHandle REC_20_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_20 );
    static final IonHandle REC_20_RECORDER_MODE_ENR = 
        create( 0x792B, "Recorder Mode (ENR)", REC_20 );
    static final IonHandle  REC_21 = 
        create( 0x0814, "REC #21" );
    static final IonHandle REC_21_RECORDER_LOG_LR = 
        create( 0x0FA2, "Recorder Log (LR)", REC_21 );
    static final IonHandle REC_21_RECORD_LEFT_NVR = 
        create( 0x4F8C, "Record Left (NVR)", REC_21 );
    static final IonHandle REC_21_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_21 );
    static final IonHandle REC_21_RECORDER_MODE_ENR = 
        create( 0x7E1F, "Recorder Mode (ENR)", REC_21 );
    static final IonHandle  REC_22 = 
        create( 0x0815, "REC #22" );
    static final IonHandle REC_22_RECORDER_LOG_LR = 
        create( 0x0FA3, "Recorder Log (LR)", REC_22 );
    static final IonHandle REC_22_RECORD_LEFT_NVR = 
        create( 0x4F8D, "Record Left (NVR)", REC_22 );
    static final IonHandle REC_22_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_22 );
    static final IonHandle REC_22_RECORDER_MODE_ENR = 
        create( 0x7E20, "Recorder Mode (ENR)", REC_22 );
    static final IonHandle  REC_23 = 
        create( 0x0816, "REC #23" );
    static final IonHandle REC_23_RECORDER_LOG_LR = 
        create( 0x0FA4, "Recorder Log (LR)", REC_23 );
    static final IonHandle REC_23_RECORD_LEFT_NVR = 
        create( 0x4F8E, "Record Left (NVR)", REC_23 );
    static final IonHandle REC_23_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_23 );
    static final IonHandle REC_23_RECORDER_MODE_ENR = 
        create( 0x7E21, "Recorder Mode (ENR)", REC_23 );
    static final IonHandle  REC_24 = 
        create( 0x0817, "REC #24" );
    static final IonHandle REC_24_RECORDER_LOG_LR = 
        create( 0x0FA5, "Recorder Log (LR)", REC_24 );
    static final IonHandle REC_24_RECORD_LEFT_NVR = 
        create( 0x4F8F, "Record Left (NVR)", REC_24 );
    static final IonHandle REC_24_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_24 );
    static final IonHandle REC_24_RECORDER_MODE_ENR = 
        create( 0x7E22, "Recorder Mode (ENR)", REC_24 );
    static final IonHandle  REC_25 = 
        create( 0x0818, "REC #25" );
    static final IonHandle REC_25_RECORDER_LOG_LR = 
        create( 0x0FA6, "Recorder Log (LR)", REC_25 );
    static final IonHandle REC_25_RECORD_LEFT_NVR = 
        create( 0x4F90, "Record Left (NVR)", REC_25 );
    static final IonHandle REC_25_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_25 );
    static final IonHandle REC_25_RECORDER_MODE_ENR = 
        create( 0x7E23, "Recorder Mode (ENR)", REC_25 );
    static final IonHandle  REC_26 = 
        create( 0x0819, "REC #26" );
    static final IonHandle REC_26_RECORDER_LOG_LR = 
        create( 0x0FA7, "Recorder Log (LR)", REC_26 );
    static final IonHandle REC_26_RECORD_LEFT_NVR = 
        create( 0x4F91, "Record Left (NVR)", REC_26 );
    static final IonHandle REC_26_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_26 );
    static final IonHandle REC_26_RECORDER_MODE_ENR = 
        create( 0x7E24, "Recorder Mode (ENR)", REC_26 );
    static final IonHandle  REC_27 = 
        create( 0x081A, "REC #27" );
    static final IonHandle REC_27_RECORDER_LOG_LR = 
        create( 0x0FA8, "Recorder Log (LR)", REC_27 );
    static final IonHandle REC_27_RECORD_LEFT_NVR = 
        create( 0x4F92, "Record Left (NVR)", REC_27 );
    static final IonHandle REC_27_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_27 );
    static final IonHandle REC_27_RECORDER_MODE_ENR = 
        create( 0x7E25, "Recorder Mode (ENR)", REC_27 );
    static final IonHandle  REC_28 = 
        create( 0x081B, "REC #28" );
    static final IonHandle REC_28_RECORDER_LOG_LR = 
        create( 0x0FA9, "Recorder Log (LR)", REC_28 );
    static final IonHandle REC_28_RECORD_LEFT_NVR = 
        create( 0x4F93, "Record Left (NVR)", REC_28 );
    static final IonHandle REC_28_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_28 );
    static final IonHandle REC_28_RECORDER_MODE_ENR = 
        create( 0x7E26, "Recorder Mode (ENR)", REC_28 );
    static final IonHandle  REC_29 = 
        create( 0x081C, "REC #29" );
    static final IonHandle REC_29_RECORDER_LOG_LR = 
        create( 0x0FAA, "Recorder Log (LR)", REC_29 );
    static final IonHandle REC_29_RECORD_LEFT_NVR = 
        create( 0x4F94, "Record Left (NVR)", REC_29 );
    static final IonHandle REC_29_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_29 );
    static final IonHandle REC_29_RECORDER_MODE_ENR = 
        create( 0x7E27, "Recorder Mode (ENR)", REC_29 );
    static final IonHandle  REC_30 = 
        create( 0x081D, "REC #30" );
    static final IonHandle REC_30_RECORDER_LOG_LR = 
        create( 0x0FAB, "Recorder Log (LR)", REC_30 );
    static final IonHandle REC_30_RECORD_LEFT_NVR = 
        create( 0x4F95, "Record Left (NVR)", REC_30 );
    static final IonHandle REC_30_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_30 );
    static final IonHandle REC_30_RECORDER_MODE_ENR = 
        create( 0x7E28, "Recorder Mode (ENR)", REC_30 );
    static final IonHandle  REC_31 = 
        create( 0x081E, "REC #31" );
    static final IonHandle REC_31_RECORDER_LOG_LR = 
        create( 0x0FAC, "Recorder Log (LR)", REC_31 );
    static final IonHandle REC_31_RECORD_LEFT_NVR = 
        create( 0x4F96, "Record Left (NVR)", REC_31 );
    static final IonHandle REC_31_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_31 );
    static final IonHandle REC_31_RECORDER_MODE_ENR = 
        create( 0x7E29, "Recorder Mode (ENR)", REC_31 );
    static final IonHandle  REC_32 = 
        create( 0x081F, "REC #32" );
    static final IonHandle REC_32_RECORDER_LOG_LR = 
        create( 0x0FAD, "Recorder Log (LR)", REC_32 );
    static final IonHandle REC_32_RECORD_LEFT_NVR = 
        create( 0x4F97, "Record Left (NVR)", REC_32 );
    static final IonHandle REC_32_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_32 );
    static final IonHandle REC_32_RECORDER_MODE_ENR = 
        create( 0x7E2A, "Recorder Mode (ENR)", REC_32 );
    static final IonHandle  REC_33 = 
        create( 0x0820, "REC #33" );
    static final IonHandle REC_33_RECORDER_LOG_LR = 
        create( 0x0FAE, "Recorder Log (LR)", REC_33 );
    static final IonHandle REC_33_RECORD_LEFT_NVR = 
        create( 0x4F98, "Record Left (NVR)", REC_33 );
    static final IonHandle REC_33_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_33 );
    static final IonHandle REC_33_RECORDER_MODE_ENR = 
        create( 0x7E2B, "Recorder Mode (ENR)", REC_33 );
    static final IonHandle  REC_34 = 
        create( 0x0821, "REC #34" );
    static final IonHandle REC_34_RECORDER_LOG_LR = 
        create( 0x0FAF, "Recorder Log (LR)", REC_34 );
    static final IonHandle REC_34_RECORD_LEFT_NVR = 
        create( 0x4F99, "Record Left (NVR)", REC_34 );
    static final IonHandle REC_34_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_34 );
    static final IonHandle REC_34_RECORDER_MODE_ENR = 
        create( 0x7E2C, "Recorder Mode (ENR)", REC_34 );
    static final IonHandle  REC_35 = 
        create( 0x0822, "REC #35" );
    static final IonHandle REC_35_RECORDER_LOG_LR = 
        create( 0x0FB0, "Recorder Log (LR)", REC_35 );
    static final IonHandle REC_35_RECORD_LEFT_NVR = 
        create( 0x4F9A, "Record Left (NVR)", REC_35 );
    static final IonHandle REC_35_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_35 );
    static final IonHandle REC_35_RECORDER_MODE_ENR = 
        create( 0x7E2D, "Recorder Mode (ENR)", REC_35 );
    static final IonHandle  REC_36 = 
        create( 0x0823, "REC #36" );
    static final IonHandle REC_36_RECORDER_LOG_LR = 
        create( 0x0FB1, "Recorder Log (LR)", REC_36 );
    static final IonHandle REC_36_RECORD_LEFT_NVR = 
        create( 0x4F9B, "Record Left (NVR)", REC_36 );
    static final IonHandle REC_36_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_36 );
    static final IonHandle REC_36_RECORDER_MODE_ENR = 
        create( 0x7E2E, "Recorder Mode (ENR)", REC_36 );
    static final IonHandle  REC_37 = 
        create( 0x0824, "REC #37" );
    static final IonHandle REC_37_RECORDER_LOG_LR = 
        create( 0x0FB2, "Recorder Log (LR)", REC_37 );
    static final IonHandle REC_37_RECORD_LEFT_NVR = 
        create( 0x4F9C, "Record Left (NVR)", REC_37 );
    static final IonHandle REC_37_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_37 );
    static final IonHandle REC_37_RECORDER_MODE_ENR = 
        create( 0x7E2F, "Recorder Mode (ENR)", REC_37 );
    static final IonHandle  REC_38 = 
        create( 0x0825, "REC #38" );
    static final IonHandle REC_38_RECORDER_LOG_LR = 
        create( 0x0FB3, "Recorder Log (LR)", REC_38 );
    static final IonHandle REC_38_RECORD_LEFT_NVR = 
        create( 0x4F9D, "Record Left (NVR)", REC_38 );
    static final IonHandle REC_38_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_38 );
    static final IonHandle REC_38_RECORDER_MODE_ENR = 
        create( 0x7E30, "Recorder Mode (ENR)", REC_38 );
    static final IonHandle  REC_39 = 
        create( 0x0826, "REC #39" );
    static final IonHandle REC_39_RECORDER_LOG_LR = 
        create( 0x0FB4, "Recorder Log (LR)", REC_39 );
    static final IonHandle REC_39_RECORD_LEFT_NVR = 
        create( 0x4F9E, "Record Left (NVR)", REC_39 );
    static final IonHandle REC_39_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_39 );
    static final IonHandle REC_39_RECORDER_MODE_ENR = 
        create( 0x7E31, "Recorder Mode (ENR)", REC_39 );
    static final IonHandle  REC_40 = 
        create( 0x0827, "REC #40" );
    static final IonHandle REC_40_RECORDER_LOG_LR = 
        create( 0x0FB5, "Recorder Log (LR)", REC_40 );
    static final IonHandle REC_40_RECORD_LEFT_NVR = 
        create( 0x4F9F, "Record Left (NVR)", REC_40 );
    static final IonHandle REC_40_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_40 );
    static final IonHandle REC_40_RECORDER_MODE_ENR = 
        create( 0x7E32, "Recorder Mode (ENR)", REC_40 );
    static final IonHandle  REC_41 = 
        create( 0x0825, "REC #41" );
    static final IonHandle REC_41_RECORDER_LOG_LR = 
        create( 0x0FB6, "Recorder Log (LR)", REC_41 );
    static final IonHandle REC_41_RECORD_LEFT_NVR = 
        create( 0x4FA0, "Record Left (NVR)", REC_41 );
    static final IonHandle REC_41_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_41 );
    static final IonHandle REC_41_RECORDER_MODE_ENR = 
        create( 0x7E33, "Recorder Mode (ENR)", REC_41 );
    static final IonHandle  REC_42 = 
        create( 0x0826, "REC #42" );
    static final IonHandle REC_42_RECORDER_LOG_LR = 
        create( 0x0FB7, "Recorder Log (LR)", REC_42 );
    static final IonHandle REC_42_RECORD_LEFT_NVR = 
        create( 0x4FA1, "Record Left (NVR)", REC_42 );
    static final IonHandle REC_42_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_42 );
    static final IonHandle REC_42_RECORDER_MODE_ENR = 
        create( 0x7E34, "Recorder Mode (ENR)", REC_42 );
    static final IonHandle  REC_43 = 
        create( 0x0827, "REC #43" );
    static final IonHandle REC_43_RECORDER_LOG_LR = 
        create( 0x0FB8, "Recorder Log (LR)", REC_43 );
    static final IonHandle REC_43_RECORD_LEFT_NVR = 
        create( 0x4FA2, "Record Left (NVR)", REC_43 );
    static final IonHandle REC_43_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_43 );
    static final IonHandle REC_43_RECORDER_MODE_ENR = 
        create( 0x7E35, "Recorder Mode (ENR)", REC_43 );
    static final IonHandle  REC_44 = 
        create( 0x0828, "REC #44" );
    static final IonHandle REC_44_RECORDER_LOG_LR = 
        create( 0x0FB9, "Recorder Log (LR)", REC_44 );
    static final IonHandle REC_44_RECORD_LEFT_NVR = 
        create( 0x4FA3, "Record Left (NVR)", REC_44 );
    static final IonHandle REC_44_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_44 );
    static final IonHandle REC_44_RECORDER_MODE_ENR = 
        create( 0x7E36, "Recorder Mode (ENR)", REC_44 );
    static final IonHandle  REC_45 = 
        create( 0x0829, "REC #45" );
    static final IonHandle REC_45_RECORDER_LOG_LR = 
        create( 0x0FBA, "Recorder Log (LR)", REC_45 );
    static final IonHandle REC_45_RECORD_LEFT_NVR = 
        create( 0x4FA4, "Record Left (NVR)", REC_45 );
    static final IonHandle REC_45_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_45 );
    static final IonHandle REC_45_RECORDER_MODE_ENR = 
        create( 0x7E37, "Recorder Mode (ENR)", REC_45 );
    static final IonHandle  REC_46 = 
        create( 0x082A, "REC #46" );
    static final IonHandle REC_46_RECORDER_LOG_LR = 
        create( 0x0FBB, "Recorder Log (LR)", REC_46 );
    static final IonHandle REC_46_RECORD_LEFT_NVR = 
        create( 0x4FA5, "Record Left (NVR)", REC_46 );
    static final IonHandle REC_46_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_46 );
    static final IonHandle REC_46_RECORDER_MODE_ENR = 
        create( 0x7E38, "Recorder Mode (ENR)", REC_46 );
    static final IonHandle  REC_47 = 
        create( 0x082B, "REC #47" );
    static final IonHandle REC_47_RECORDER_LOG_LR = 
        create( 0x0FBC, "Recorder Log (LR)", REC_47 );
    static final IonHandle REC_47_RECORD_LEFT_NVR = 
        create( 0x4FA6, "Record Left (NVR)", REC_47 );
    static final IonHandle REC_47_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_47 );
    static final IonHandle REC_47_RECORDER_MODE_ENR = 
        create( 0x7E39, "Recorder Mode (ENR)", REC_47 );
    static final IonHandle  REC_48 = 
        create( 0x082C, "REC #48" );
    static final IonHandle REC_48_RECORDER_LOG_LR = 
        create( 0x0FBD, "Recorder Log (LR)", REC_48 );
    static final IonHandle REC_48_RECORD_LEFT_NVR = 
        create( 0x4FA7, "Record Left (NVR)", REC_48 );
    static final IonHandle REC_48_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_48 );
    static final IonHandle REC_48_RECORDER_MODE_ENR = 
        create( 0x7E3A, "Recorder Mode (ENR)", REC_48 );
    static final IonHandle  REC_49 = 
        create( 0x082D, "REC #49" );
    static final IonHandle REC_49_RECORDER_LOG_LR = 
        create( 0x0FBE, "Recorder Log (LR)", REC_49 );
    static final IonHandle REC_49_RECORD_LEFT_NVR = 
        create( 0x4FA8, "Record Left (NVR)", REC_49 );
    static final IonHandle REC_49_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_49 );
    static final IonHandle REC_49_RECORDER_MODE_ENR = 
        create( 0x7E3B, "Recorder Mode (ENR)", REC_49 );
    static final IonHandle  REC_50 = 
        create( 0x082E, "REC #50" );
    static final IonHandle REC_50_RECORDER_LOG_LR = 
        create( 0x0FBF, "Recorder Log (LR)", REC_50 );
    static final IonHandle REC_50_RECORD_LEFT_NVR = 
        create( 0x4FA9, "Record Left (NVR)", REC_50 );
    static final IonHandle REC_50_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", REC_50 );
    static final IonHandle REC_50_RECORDER_MODE_ENR = 
        create( 0x7E3C, "Recorder Mode (ENR)", REC_50 );
    static final IonHandle  MAX_1 = 
        create( 0x600, "MAX #1" );
    static final IonHandle MAX_1_MAX_NVR = 
        create( 0x58E0, "Max (NVR)", MAX_1 );
    static final IonHandle MAX_1_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_1 );
    static final IonHandle  MAX_2 = 
        create( 0x0601, "MAX #2" );
    static final IonHandle MAX_2_MAX_NVR = 
        create( 0x58E1, "Max (NVR)", MAX_2 );
    static final IonHandle MAX_2_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_2 );
    static final IonHandle  MAX_3 = 
        create( 0x0602, "MAX #3" );
    static final IonHandle MAX_3_MAX_NVR = 
        create( 0x58E2, "Max (NVR)", MAX_3 );
    static final IonHandle MAX_3_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_3 );
    static final IonHandle  MAX_4 = 
        create( 0x0603, "MAX #4" );
    static final IonHandle MAX_4_MAX_NVR = 
        create( 0x58E3, "Max (NVR)", MAX_4 );
    static final IonHandle MAX_4_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_4 );
    static final IonHandle  MAX_5 = 
        create( 0x0604, "MAX #5" );
    static final IonHandle MAX_5_MAX_NVR = 
        create( 0x58E4, "Max (NVR)", MAX_5 );
    static final IonHandle MAX_5_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_5 );
    static final IonHandle  MAX_6 = 
        create( 0x0605, "MAX #6" );
    static final IonHandle MAX_6_MAX_NVR = 
        create( 0x58E5, "Max (NVR)", MAX_6 );
    static final IonHandle MAX_6_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_6 );
    static final IonHandle  MAX_7 = 
        create( 0x0606, "MAX #7" );
    static final IonHandle MAX_7_MAX_NVR = 
        create( 0x58E6, "Max (NVR)", MAX_7 );
    static final IonHandle MAX_7_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_7 );
    static final IonHandle  MAX_8 = 
        create( 0x0607, "MAX #8" );
    static final IonHandle MAX_8_MAX_NVR = 
        create( 0x58E7, "Max (NVR)", MAX_8 );
    static final IonHandle MAX_8_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_8 );
    static final IonHandle  MAX_9 = 
        create( 0x0608, "MAX #9" );
    static final IonHandle MAX_9_MAX_NVR = 
        create( 0x58E8, "Max (NVR)", MAX_9 );
    static final IonHandle MAX_9_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_9 );
    static final IonHandle  MAX_10 = 
        create( 0x0609, "MAX #10" );
    static final IonHandle MAX_10_MAX_NVR = 
        create( 0x58E9, "Max (NVR)", MAX_10 );
    static final IonHandle MAX_10_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_10 );
    static final IonHandle  MAX_11 = 
        create( 0x060A, "MAX #11" );
    static final IonHandle MAX_11_MAX_NVR = 
        create( 0x58EA, "Max (NVR)", MAX_11 );
    static final IonHandle MAX_11_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_11 );
    static final IonHandle  MAX_12 = 
        create( 0x060B, "MAX #12" );
    static final IonHandle MAX_12_MAX_NVR = 
        create( 0x58EB, "Max (NVR)", MAX_12 );
    static final IonHandle MAX_12_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_12 );
    static final IonHandle  MAX_13 = 
        create( 0x060C, "MAX #13" );
    static final IonHandle MAX_13_MAX_NVR = 
        create( 0x58EC, "Max (NVR)", MAX_13 );
    static final IonHandle MAX_13_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_13 );
    static final IonHandle  MAX_14 = 
        create( 0x060D, "MAX #14" );
    static final IonHandle MAX_14_MAX_NVR = 
        create( 0x58ED, "Max (NVR)", MAX_14 );
    static final IonHandle MAX_14_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_14 );
    static final IonHandle  MAX_15 = 
        create( 0x060E, "MAX #15" );
    static final IonHandle MAX_15_MAX_NVR = 
        create( 0x58EE, "Max (NVR)", MAX_15 );
    static final IonHandle MAX_15_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_15 );
    static final IonHandle  MAX_16 = 
        create( 0x060F, "MAX #16" );
    static final IonHandle MAX_16_MAX_NVR = 
        create( 0x58EF, "Max (NVR)", MAX_16 );
    static final IonHandle MAX_16_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_16 );
    static final IonHandle  MAX_17 = 
        create( 0x0610, "MAX #17" );
    static final IonHandle MAX_17_MAX_NVR = 
        create( 0x58F0, "Max (NVR)", MAX_17 );
    static final IonHandle MAX_17_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_17 );
    static final IonHandle  MAX_18 = 
        create( 0x0611, "MAX #18" );
    static final IonHandle MAX_18_MAX_NVR = 
        create( 0x58F1, "Max (NVR)", MAX_18 );
    static final IonHandle MAX_18_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_18 );
    static final IonHandle  MAX_19 = 
        create( 0x0612, "MAX #19" );
    static final IonHandle MAX_19_MAX_NVR = 
        create( 0x58F2, "Max (NVR)", MAX_19 );
    static final IonHandle MAX_19_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_19 );
    static final IonHandle  MAX_20 = 
        create( 0x0613, "MAX #20" );
    static final IonHandle MAX_20_MAX_NVR = 
        create( 0x58F3, "Max (NVR)", MAX_20 );
    static final IonHandle MAX_20_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_20 );
    static final IonHandle  MAX_21 = 
        create( 0x0614, "MAX #21" );
    static final IonHandle MAX_21_MAX_NVR = 
        create( 0x58F4, "Max (NVR)", MAX_21 );
    static final IonHandle MAX_21_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_21 );
    static final IonHandle  MAX_22 = 
        create( 0x0615, "MAX #22" );
    static final IonHandle MAX_22_MAX_NVR = 
        create( 0x58F5, "Max (NVR)", MAX_22 );
    static final IonHandle MAX_22_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_22 );
    static final IonHandle  MAX_23 = 
        create( 0x0616, "MAX #23" );
    static final IonHandle MAX_23_MAX_NVR = 
        create( 0x58F6, "Max (NVR)", MAX_23 );
    static final IonHandle MAX_23_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_23 );
    static final IonHandle  MAX_24 = 
        create( 0x0617, "MAX #24" );
    static final IonHandle MAX_24_MAX_NVR = 
        create( 0x58F7, "Max (NVR)", MAX_24 );
    static final IonHandle MAX_24_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_24 );
    static final IonHandle  MAX_25 = 
        create( 0x0618, "MAX #25" );
    static final IonHandle MAX_25_MAX_NVR = 
        create( 0x58F8, "Max (NVR)", MAX_25 );
    static final IonHandle MAX_25_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_25 );
    static final IonHandle  MAX_26 = 
        create( 0x0619, "MAX #26" );
    static final IonHandle MAX_26_MAX_NVR = 
        create( 0x58F9, "Max (NVR)", MAX_26 );
    static final IonHandle MAX_26_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_26 );
    static final IonHandle  MAX_27 = 
        create( 0x061A, "MAX #27" );
    static final IonHandle MAX_27_MAX_NVR = 
        create( 0x58FA, "Max (NVR)", MAX_27 );
    static final IonHandle MAX_27_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_27 );
    static final IonHandle  MAX_28 = 
        create( 0x061B, "MAX #28" );
    static final IonHandle MAX_28_MAX_NVR = 
        create( 0x58FB, "Max (NVR)", MAX_28 );
    static final IonHandle MAX_28_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_28 );
    static final IonHandle  MAX_29 = 
        create( 0x061C, "MAX #29" );
    static final IonHandle MAX_29_MAX_NVR = 
        create( 0x58FC, "Max (NVR)", MAX_29 );
    static final IonHandle MAX_29_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_29 );
    static final IonHandle  MAX_30 = 
        create( 0x061D, "MAX #30" );
    static final IonHandle MAX_30_MAX_NVR = 
        create( 0x58FD, "Max (NVR)", MAX_30 );
    static final IonHandle MAX_30_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_30 );
    static final IonHandle  MAX_31 = 
        create( 0x061E, "MAX #31" );
    static final IonHandle MAX_31_MAX_NVR = 
        create( 0x58FE, "Max (NVR)", MAX_31 );
    static final IonHandle MAX_31_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_31 );
    static final IonHandle  MAX_32 = 
        create( 0x061F, "MAX #32" );
    static final IonHandle MAX_32_MAX_NVR = 
        create( 0x58FF, "Max (NVR)", MAX_32 );
    static final IonHandle MAX_32_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_32 );
    static final IonHandle  MAX_33 = 
        create( 0x0620, "MAX #33" );
    static final IonHandle MAX_33_MAX_NVR = 
        create( 0x5E26, "Max (NVR)", MAX_33 );
    static final IonHandle MAX_33_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_33 );
    static final IonHandle  MAX_34 = 
        create( 0x0621, "MAX #34" );
    static final IonHandle MAX_34_MAX_NVR = 
        create( 0x5E27, "Max (NVR)", MAX_34 );
    static final IonHandle MAX_34_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_34 );
    static final IonHandle  MAX_35 = 
        create( 0x0622, "MAX #35" );
    static final IonHandle MAX_35_MAX_NVR = 
        create( 0x5E28, "Max (NVR)", MAX_35 );
    static final IonHandle MAX_35_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_35 );
    static final IonHandle  MAX_36 = 
        create( 0x0623, "MAX #36" );
    static final IonHandle MAX_36_MAX_NVR = 
        create( 0x5E29, "Max (NVR)", MAX_36 );
    static final IonHandle MAX_36_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_36 );
    static final IonHandle  MAX_37 = 
        create( 0x0624, "MAX #37" );
    static final IonHandle MAX_37_MAX_NVR = 
        create( 0x5E2A, "Max (NVR)", MAX_37 );
    static final IonHandle MAX_37_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_37 );
    static final IonHandle  MAX_38 = 
        create( 0x0625, "MAX #38" );
    static final IonHandle MAX_38_MAX_NVR = 
        create( 0x5E2B, "Max (NVR)", MAX_38 );
    static final IonHandle MAX_38_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_38 );
    static final IonHandle  MAX_39 = 
        create( 0x0626, "MAX #39" );
    static final IonHandle MAX_39_MAX_NVR = 
        create( 0x5E2C, "Max (NVR)", MAX_39 );
    static final IonHandle MAX_39_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_39 );
    static final IonHandle  MAX_40 = 
        create( 0x0627, "MAX #40" );
    static final IonHandle MAX_40_MAX_NVR = 
        create( 0x5E2D, "Max (NVR)", MAX_40 );
    static final IonHandle MAX_40_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_40 );
    static final IonHandle  MAX_41 = 
        create( 0x0628, "MAX #41" );
    static final IonHandle MAX_41_MAX_NVR = 
        create( 0x5E2E, "Max (NVR)", MAX_41 );
    static final IonHandle MAX_41_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_41 );
    static final IonHandle  MAX_42 = 
        create( 0x0629, "MAX #42" );
    static final IonHandle MAX_42_MAX_NVR = 
        create( 0x5E2F, "Max (NVR)", MAX_42 );
    static final IonHandle MAX_42_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_42 );
    static final IonHandle  MAX_43 = 
        create( 0x062A, "MAX #43" );
    static final IonHandle MAX_43_MAX_NVR = 
        create( 0x5E30, "Max (NVR)", MAX_43 );
    static final IonHandle MAX_43_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_43 );
    static final IonHandle  MAX_44 = 
        create( 0x062B, "MAX #44" );
    static final IonHandle MAX_44_MAX_NVR = 
        create( 0x5E31, "Max (NVR)", MAX_44 );
    static final IonHandle MAX_44_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_44 );
    static final IonHandle  MAX_45 = 
        create( 0x062C, "MAX #45" );
    static final IonHandle MAX_45_MAX_NVR = 
        create( 0x5E32, "Max (NVR)", MAX_45 );
    static final IonHandle MAX_45_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_45 );
    static final IonHandle  MAX_46 = 
        create( 0x062D, "MAX #46" );
    static final IonHandle MAX_46_MAX_NVR = 
        create( 0x5E33, "Max (NVR)", MAX_46 );
    static final IonHandle MAX_46_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_46 );
    static final IonHandle  MAX_47 = 
        create( 0x062E, "MAX #47" );
    static final IonHandle MAX_47_MAX_NVR = 
        create( 0x5E34, "Max (NVR)", MAX_47 );
    static final IonHandle MAX_47_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_47 );
    static final IonHandle  MAX_48 = 
        create( 0x062F, "MAX #48" );
    static final IonHandle MAX_48_MAX_NVR = 
        create( 0x5E35, "Max (NVR)", MAX_48 );
    static final IonHandle MAX_48_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_48 );
    static final IonHandle  MAX_49 = 
        create( 0x0630, "MAX #49" );
    static final IonHandle MAX_49_MAX_NVR = 
        create( 0x5E36, "Max (NVR)", MAX_49 );
    static final IonHandle MAX_49_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_49 );
    static final IonHandle  MAX_50 = 
        create( 0x0631, "MAX #50" );
    static final IonHandle MAX_50_MAX_NVR = 
        create( 0x5E37, "Max (NVR)", MAX_50 );
    static final IonHandle MAX_50_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_50 );
    static final IonHandle  MAX_51 = 
        create( 0x0632, "MAX #51" );
    static final IonHandle MAX_51_MAX_NVR = 
        create( 0x5E38, "Max (NVR)", MAX_51 );
    static final IonHandle MAX_51_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_51 );
    static final IonHandle  MAX_52 = 
        create( 0x0633, "MAX #52" );
    static final IonHandle MAX_52_MAX_NVR = 
        create( 0x5E39, "Max (NVR)", MAX_52 );
    static final IonHandle MAX_52_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_52 );
    static final IonHandle  MAX_53 = 
        create( 0x0634, "MAX #53" );
    static final IonHandle MAX_53_MAX_NVR = 
        create( 0x5E3A, "Max (NVR)", MAX_53 );
    static final IonHandle MAX_53_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_53 );
    static final IonHandle  MAX_54 = 
        create( 0x0635, "MAX #54" );
    static final IonHandle MAX_54_MAX_NVR = 
        create( 0x5E3B, "Max (NVR)", MAX_54 );
    static final IonHandle MAX_54_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_54 );
    static final IonHandle  MAX_55 = 
        create( 0x0636, "MAX #55" );
    static final IonHandle MAX_55_MAX_NVR = 
        create( 0x5E3C, "Max (NVR)", MAX_55 );
    static final IonHandle MAX_55_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_55 );
    static final IonHandle  MAX_56 = 
        create( 0x0637, "MAX #56" );
    static final IonHandle MAX_56_MAX_NVR = 
        create( 0x5E3D, "Max (NVR)", MAX_56 );
    static final IonHandle MAX_56_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_56 );
    static final IonHandle  MAX_57 = 
        create( 0x0638, "MAX #57" );
    static final IonHandle MAX_57_MAX_NVR = 
        create( 0x5E3E, "Max (NVR)", MAX_57 );
    static final IonHandle MAX_57_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_57 );
    static final IonHandle  MAX_58 = 
        create( 0x0639, "MAX #58" );
    static final IonHandle MAX_58_MAX_NVR = 
        create( 0x5E3F, "Max (NVR)", MAX_58 );
    static final IonHandle MAX_58_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_58 );
    static final IonHandle  MAX_59 = 
        create( 0x063A, "MAX #59" );
    static final IonHandle MAX_59_MAX_NVR = 
        create( 0x5E40, "Max (NVR)", MAX_59 );
    static final IonHandle MAX_59_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_59 );
    static final IonHandle  MAX_60 = 
        create( 0x063B, "MAX #60" );
    static final IonHandle MAX_60_MAX_NVR = 
        create( 0x5E41, "Max (NVR)", MAX_60 );
    static final IonHandle MAX_60_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_60 );
    static final IonHandle  MAX_61 = 
        create( 0x063C, "MAX #61" );
    static final IonHandle MAX_61_MAX_NVR = 
        create( 0x4800, "Max (NVR)", MAX_61 );
    static final IonHandle MAX_61_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_61 );
    static final IonHandle  MAX_62 = 
        create( 0x063D, "MAX #62" );
    static final IonHandle MAX_62_MAX_NVR = 
        create( 0x4801, "Max (NVR)", MAX_62 );
    static final IonHandle MAX_62_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_62 );
    static final IonHandle  MAX_63 = 
        create( 0x063E, "MAX #63" );
    static final IonHandle MAX_63_MAX_NVR = 
        create( 0x4802, "Max (NVR)", MAX_63 );
    static final IonHandle MAX_63_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_63 );
    static final IonHandle  MAX_64 = 
        create( 0x063F, "MAX #64" );
    static final IonHandle MAX_64_MAX_NVR = 
        create( 0x4803, "Max (NVR)", MAX_64 );
    static final IonHandle MAX_64_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_64 );
    static final IonHandle  MAX_65 = 
        create( 0x0640, "MAX #65" );
    static final IonHandle MAX_65_MAX_NVR = 
        create( 0x4804, "Max (NVR)", MAX_65 );
    static final IonHandle MAX_65_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_65 );
    static final IonHandle  MAX_66 = 
        create( 0x0641, "MAX #66" );
    static final IonHandle MAX_66_MAX_NVR = 
        create( 0x4805, "Max (NVR)", MAX_66 );
    static final IonHandle MAX_66_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_66 );
    static final IonHandle  MAX_67 = 
        create( 0x0642, "MAX #67" );
    static final IonHandle MAX_67_MAX_NVR = 
        create( 0x4806, "Max (NVR)", MAX_67 );
    static final IonHandle MAX_67_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_67 );
    static final IonHandle  MAX_68 = 
        create( 0x0643, "MAX #68" );
    static final IonHandle MAX_68_MAX_NVR = 
        create( 0x4807, "Max (NVR)", MAX_68 );
    static final IonHandle MAX_68_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_68 );
    static final IonHandle  MAX_69 = 
        create( 0x0644, "MAX #69" );
    static final IonHandle MAX_69_MAX_NVR = 
        create( 0x4808, "Max (NVR)", MAX_69 );
    static final IonHandle MAX_69_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_69 );
    static final IonHandle  MAX_70 = 
        create( 0x0645, "MAX #70" );
    static final IonHandle MAX_70_MAX_NVR = 
        create( 0x4809, "Max (NVR)", MAX_70 );
    static final IonHandle MAX_70_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_70 );
    static final IonHandle  MAX_71 = 
        create( 0x0646, "MAX #71" );
    static final IonHandle MAX_71_MAX_NVR = 
        create( 0x480A, "Max (NVR)", MAX_71 );
    static final IonHandle MAX_71_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_71 );
    static final IonHandle  MAX_72 = 
        create( 0x0647, "MAX #72" );
    static final IonHandle MAX_72_MAX_NVR = 
        create( 0x480B, "Max (NVR)", MAX_72 );
    static final IonHandle MAX_72_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_72 );
    static final IonHandle  MAX_73 = 
        create( 0x0648, "MAX #73" );
    static final IonHandle MAX_73_MAX_NVR = 
        create( 0x480C, "Max (NVR)", MAX_73 );
    static final IonHandle MAX_73_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_73 );
    static final IonHandle  MAX_74 = 
        create( 0x0649, "MAX #74" );
    static final IonHandle MAX_74_MAX_NVR = 
        create( 0x480D, "Max (NVR)", MAX_74 );
    static final IonHandle MAX_74_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_74 );
    static final IonHandle  MAX_75 = 
        create( 0x064A, "MAX #75" );
    static final IonHandle MAX_75_MAX_NVR = 
        create( 0x480E, "Max (NVR)", MAX_75 );
    static final IonHandle MAX_75_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_75 );
    static final IonHandle  MAX_76 = 
        create( 0x064B, "MAX #76" );
    static final IonHandle MAX_76_MAX_NVR = 
        create( 0x480F, "Max (NVR)", MAX_76 );
    static final IonHandle MAX_76_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_76 );
    static final IonHandle  MAX_77 = 
        create( 0x064C, "MAX #77" );
    static final IonHandle MAX_77_MAX_NVR = 
        create( 0x4810, "Max (NVR)", MAX_77 );
    static final IonHandle MAX_77_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_77 );
    static final IonHandle  MAX_78 = 
        create( 0x064D, "MAX #78" );
    static final IonHandle MAX_78_MAX_NVR = 
        create( 0x4811, "Max (NVR)", MAX_78 );
    static final IonHandle MAX_78_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_78 );
    static final IonHandle  MAX_79 = 
        create( 0x064E, "MAX #79" );
    static final IonHandle MAX_79_MAX_NVR = 
        create( 0x4812, "Max (NVR)", MAX_79 );
    static final IonHandle MAX_79_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_79 );
    static final IonHandle  MAX_80 = 
        create( 0x064F, "MAX #80" );
    static final IonHandle MAX_80_MAX_NVR = 
        create( 0x4813, "Max (NVR)", MAX_80 );
    static final IonHandle MAX_80_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_80 );
    static final IonHandle  MAX_81 = 
        create( 0x0650, "MAX #81" );
    static final IonHandle MAX_81_MAX_NVR = 
        create( 0x4814, "Max (NVR)", MAX_81 );
    static final IonHandle MAX_81_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_81 );
    static final IonHandle  MAX_82 = 
        create( 0x0651, "MAX #82" );
    static final IonHandle MAX_82_MAX_NVR = 
        create( 0x4815, "Max (NVR)", MAX_82 );
    static final IonHandle MAX_82_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_82 );
    static final IonHandle  MAX_83 = 
        create( 0x0652, "MAX #83" );
    static final IonHandle MAX_83_MAX_NVR = 
        create( 0x4816, "Max (NVR)", MAX_83 );
    static final IonHandle MAX_83_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_83 );
    static final IonHandle  MAX_84 = 
        create( 0x0653, "MAX #84" );
    static final IonHandle MAX_84_MAX_NVR = 
        create( 0x4817, "Max (NVR)", MAX_84 );
    static final IonHandle MAX_84_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_84 );
    static final IonHandle  MAX_85 = 
        create( 0x0654, "MAX #85" );
    static final IonHandle MAX_85_MAX_NVR = 
        create( 0x4818, "Max (NVR)", MAX_85 );
    static final IonHandle MAX_85_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_85 );
    static final IonHandle  MAX_86 = 
        create( 0x0655, "MAX #86" );
    static final IonHandle MAX_86_MAX_NVR = 
        create( 0x4819, "Max (NVR)", MAX_86 );
    static final IonHandle MAX_86_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_86 );
    static final IonHandle  MAX_87 = 
        create( 0x0656, "MAX #87" );
    static final IonHandle MAX_87_MAX_NVR = 
        create( 0x481A, "Max (NVR)", MAX_87 );
    static final IonHandle MAX_87_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_87 );
    static final IonHandle  MAX_88 = 
        create( 0x0657, "MAX #88" );
    static final IonHandle MAX_88_MAX_NVR = 
        create( 0x481B, "Max (NVR)", MAX_88 );
    static final IonHandle MAX_88_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_88 );
    static final IonHandle  MAX_89 = 
        create( 0x0658, "MAX #89" );
    static final IonHandle MAX_89_MAX_NVR = 
        create( 0x481C, "Max (NVR)", MAX_89 );
    static final IonHandle MAX_89_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_89 );
    static final IonHandle  MAX_90 = 
        create( 0x0659, "MAX #90" );
    static final IonHandle MAX_90_MAX_NVR = 
        create( 0x481D, "Max (NVR)", MAX_90 );
    static final IonHandle MAX_90_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_90 );
    static final IonHandle  MAX_91 = 
        create( 0x065A, "MAX #91" );
    static final IonHandle MAX_91_MAX_NVR = 
        create( 0x481E, "Max (NVR)", MAX_91 );
    static final IonHandle MAX_91_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_91 );
    static final IonHandle  MAX_92 = 
        create( 0x065B, "MAX #92" );
    static final IonHandle MAX_92_MAX_NVR = 
        create( 0x481F, "Max (NVR)", MAX_92 );
    static final IonHandle MAX_92_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_92 );
    static final IonHandle  MAX_93 = 
        create( 0x065C, "MAX #93" );
    static final IonHandle MAX_93_MAX_NVR = 
        create( 0x4820, "Max (NVR)", MAX_93 );
    static final IonHandle MAX_93_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_93 );
    static final IonHandle  MAX_94 = 
        create( 0x065D, "MAX #94" );
    static final IonHandle MAX_94_MAX_NVR = 
        create( 0x4821, "Max (NVR)", MAX_94 );
    static final IonHandle MAX_94_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_94 );
    static final IonHandle  MAX_95 = 
        create( 0x065E, "MAX #95" );
    static final IonHandle MAX_95_MAX_NVR = 
        create( 0x4822, "Max (NVR)", MAX_95 );
    static final IonHandle MAX_95_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_95 );
    static final IonHandle  MAX_96 = 
        create( 0x065F, "MAX #96" );
    static final IonHandle MAX_96_MAX_NVR = 
        create( 0x4823, "Max (NVR)", MAX_96 );
    static final IonHandle MAX_96_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_96 );
    static final IonHandle  MAX_97 = 
        create( 0x0660, "MAX #97" );
    static final IonHandle MAX_97_MAX_NVR = 
        create( 0x4824, "Max (NVR)", MAX_97 );
    static final IonHandle MAX_97_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_97 );
    static final IonHandle  MAX_98 = 
        create( 0x0661, "MAX #98" );
    static final IonHandle MAX_98_MAX_NVR = 
        create( 0x4825, "Max (NVR)", MAX_98 );
    static final IonHandle MAX_98_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_98 );
    static final IonHandle  MAX_99 = 
        create( 0x0662, "MAX #99" );
    static final IonHandle MAX_99_MAX_NVR = 
        create( 0x4826, "Max (NVR)", MAX_99 );
    static final IonHandle MAX_99_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_99 );
    static final IonHandle  MAX_100 = 
        create( 0x0663, "MAX #100" );
    static final IonHandle MAX_100_MAX_NVR = 
        create( 0x4827, "Max (NVR)", MAX_100 );
    static final IonHandle MAX_100_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_100 );
    static final IonHandle  MAX_101 = 
        create( 0x0664, "MAX #101" );
    static final IonHandle MAX_101_MAX_NVR = 
        create( 0x4828, "Max (NVR)", MAX_101 );
    static final IonHandle MAX_101_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_101 );
    static final IonHandle  MAX_102 = 
        create( 0x0665, "MAX #102" );
    static final IonHandle MAX_102_MAX_NVR = 
        create( 0x4829, "Max (NVR)", MAX_102 );
    static final IonHandle MAX_102_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_102 );
    static final IonHandle  MAX_103 = 
        create( 0x0666, "MAX #103" );
    static final IonHandle MAX_103_MAX_NVR = 
        create( 0x482A, "Max (NVR)", MAX_103 );
    static final IonHandle MAX_103_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_103 );
    static final IonHandle  MAX_104 = 
        create( 0x0667, "MAX #104" );
    static final IonHandle MAX_104_MAX_NVR = 
        create( 0x482B, "Max (NVR)", MAX_104 );
    static final IonHandle MAX_104_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_104 );
    static final IonHandle  MAX_105 = 
        create( 0x0668, "MAX #105" );
    static final IonHandle MAX_105_MAX_NVR = 
        create( 0x482C, "Max (NVR)", MAX_105 );
    static final IonHandle MAX_105_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_105 );
    static final IonHandle  MAX_106 = 
        create( 0x0669, "MAX #106" );
    static final IonHandle MAX_106_MAX_NVR = 
        create( 0x482D, "Max (NVR)", MAX_106 );
    static final IonHandle MAX_106_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_106 );
    static final IonHandle  MAX_107 = 
        create( 0x066A, "MAX #107" );
    static final IonHandle MAX_107_MAX_NVR = 
        create( 0x482E, "Max (NVR)", MAX_107 );
    static final IonHandle MAX_107_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_107 );
    static final IonHandle  MAX_108 = 
        create( 0x066B, "MAX #108" );
    static final IonHandle MAX_108_MAX_NVR = 
        create( 0x482F, "Max (NVR)", MAX_108 );
    static final IonHandle MAX_108_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_108 );
    static final IonHandle  MAX_109 = 
        create( 0x066C, "MAX #109" );
    static final IonHandle MAX_109_MAX_NVR = 
        create( 0x4830, "Max (NVR)", MAX_109 );
    static final IonHandle MAX_109_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_109 );
    static final IonHandle  MAX_110 = 
        create( 0x066D, "MAX #110" );
    static final IonHandle MAX_110_MAX_NVR = 
        create( 0x4831, "Max (NVR)", MAX_110 );
    static final IonHandle MAX_110_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_110 );
    static final IonHandle  MAX_111 = 
        create( 0x066E, "MAX #111" );
    static final IonHandle MAX_111_MAX_NVR = 
        create( 0x4832, "Max (NVR)", MAX_111 );
    static final IonHandle MAX_111_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_111 );
    static final IonHandle  MAX_112 = 
        create( 0x066F, "MAX #112" );
    static final IonHandle MAX_112_MAX_NVR = 
        create( 0x4833, "Max (NVR)", MAX_112 );
    static final IonHandle MAX_112_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_112 );
    static final IonHandle  MAX_113 = 
        create( 0x0670, "MAX #113" );
    static final IonHandle MAX_113_MAX_NVR = 
        create( 0x4834, "Max (NVR)", MAX_113 );
    static final IonHandle MAX_113_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_113 );
    static final IonHandle  MAX_114 = 
        create( 0x0671, "MAX #114" );
    static final IonHandle MAX_114_MAX_NVR = 
        create( 0x4835, "Max (NVR)", MAX_114 );
    static final IonHandle MAX_114_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_114 );
    static final IonHandle  MAX_115 = 
        create( 0x0672, "MAX #115" );
    static final IonHandle MAX_115_MAX_NVR = 
        create( 0x4836, "Max (NVR)", MAX_115 );
    static final IonHandle MAX_115_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_115 );
    static final IonHandle  MAX_116 = 
        create( 0x0673, "MAX #116" );
    static final IonHandle MAX_116_MAX_NVR = 
        create( 0x4837, "Max (NVR)", MAX_116 );
    static final IonHandle MAX_116_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_116 );
    static final IonHandle  MAX_117 = 
        create( 0x0674, "MAX #117" );
    static final IonHandle MAX_117_MAX_NVR = 
        create( 0x4838, "Max (NVR)", MAX_117 );
    static final IonHandle MAX_117_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_117 );
    static final IonHandle  MAX_118 = 
        create( 0x0675, "MAX #118" );
    static final IonHandle MAX_118_MAX_NVR = 
        create( 0x4839, "Max (NVR)", MAX_118 );
    static final IonHandle MAX_118_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_118 );
    static final IonHandle  MAX_119 = 
        create( 0x0676, "MAX #119" );
    static final IonHandle MAX_119_MAX_NVR = 
        create( 0x483A, "Max (NVR)", MAX_119 );
    static final IonHandle MAX_119_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_119 );
    static final IonHandle  MAX_120 = 
        create( 0x0677, "MAX #120" );
    static final IonHandle MAX_120_MAX_NVR = 
        create( 0x483B, "Max (NVR)", MAX_120 );
    static final IonHandle MAX_120_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_120 );
    static final IonHandle  MAX_121 = 
        create( 0x0678, "MAX #121" );
    static final IonHandle MAX_121_MAX_NVR = 
        create( 0x483C, "Max (NVR)", MAX_121 );
    static final IonHandle MAX_121_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_121 );
    static final IonHandle  MAX_122 = 
        create( 0x0679, "MAX #122" );
    static final IonHandle MAX_122_MAX_NVR = 
        create( 0x483D, "Max (NVR)", MAX_122 );
    static final IonHandle MAX_122_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_122 );
    static final IonHandle  MAX_123 = 
        create( 0x067A, "MAX #123" );
    static final IonHandle MAX_123_MAX_NVR = 
        create( 0x483E, "Max (NVR)", MAX_123 );
    static final IonHandle MAX_123_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_123 );
    static final IonHandle  MAX_124 = 
        create( 0x067B, "MAX #124" );
    static final IonHandle MAX_124_MAX_NVR = 
        create( 0x483F, "Max (NVR)", MAX_124 );
    static final IonHandle MAX_124_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_124 );
    static final IonHandle  MAX_125 = 
        create( 0x067C, "MAX #125" );
    static final IonHandle MAX_125_MAX_NVR = 
        create( 0x4840, "Max (NVR)", MAX_125 );
    static final IonHandle MAX_125_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_125 );
    static final IonHandle  MAX_126 = 
        create( 0x067D, "MAX #126" );
    static final IonHandle MAX_126_MAX_NVR = 
        create( 0x4841, "Max (NVR)", MAX_126 );
    static final IonHandle MAX_126_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_126 );
    static final IonHandle  MAX_127 = 
        create( 0x067E, "MAX #127" );
    static final IonHandle MAX_127_MAX_NVR = 
        create( 0x4842, "Max (NVR)", MAX_127 );
    static final IonHandle MAX_127_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_127 );
    static final IonHandle  MAX_128 = 
        create( 0x067F, "MAX #128" );
    static final IonHandle MAX_128_MAX_NVR = 
        create( 0x4843, "Max (NVR)", MAX_128 );
    static final IonHandle MAX_128_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_128 );
    static final IonHandle  MAX_129 = 
        create( 0x900, "MAX #129" );
    static final IonHandle MAX_129_MAX_NVR = 
        create( 0x4844, "Max (NVR)", MAX_129 );
    static final IonHandle MAX_129_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_129 );
    static final IonHandle  MAX_130 = 
        create( 0x0901, "MAX #130" );
    static final IonHandle MAX_130_MAX_NVR = 
        create( 0x4845, "Max (NVR)", MAX_130 );
    static final IonHandle MAX_130_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_130 );
    static final IonHandle  MAX_131 = 
        create( 0x0902, "MAX #131" );
    static final IonHandle MAX_131_MAX_NVR = 
        create( 0x4846, "Max (NVR)", MAX_131 );
    static final IonHandle MAX_131_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_131 );
    static final IonHandle  MAX_132 = 
        create( 0x0903, "MAX #132" );
    static final IonHandle MAX_132_MAX_NVR = 
        create( 0x4847, "Max (NVR)", MAX_132 );
    static final IonHandle MAX_132_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_132 );
    static final IonHandle  MAX_133 = 
        create( 0x0904, "MAX #133" );
    static final IonHandle MAX_133_MAX_NVR = 
        create( 0x4848, "Max (NVR)", MAX_133 );
    static final IonHandle MAX_133_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_133 );
    static final IonHandle  MAX_134 = 
        create( 0x0905, "MAX #134" );
    static final IonHandle MAX_134_MAX_NVR = 
        create( 0x4849, "Max (NVR)", MAX_134 );
    static final IonHandle MAX_134_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_134 );
    static final IonHandle  MAX_135 = 
        create( 0x0906, "MAX #135" );
    static final IonHandle MAX_135_MAX_NVR = 
        create( 0x484A, "Max (NVR)", MAX_135 );
    static final IonHandle MAX_135_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_135 );
    static final IonHandle  MAX_136 = 
        create( 0x0907, "MAX #136" );
    static final IonHandle MAX_136_MAX_NVR = 
        create( 0x484B, "Max (NVR)", MAX_136 );
    static final IonHandle MAX_136_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_136 );
    static final IonHandle  MAX_137 = 
        create( 0x0908, "MAX #137" );
    static final IonHandle MAX_137_MAX_NVR = 
        create( 0x484C, "Max (NVR)", MAX_137 );
    static final IonHandle MAX_137_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_137 );
    static final IonHandle  MAX_138 = 
        create( 0x0909, "MAX #138" );
    static final IonHandle MAX_138_MAX_NVR = 
        create( 0x484D, "Max (NVR)", MAX_138 );
    static final IonHandle MAX_138_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_138 );
    static final IonHandle  MAX_139 = 
        create( 0x090A, "MAX #139" );
    static final IonHandle MAX_139_MAX_NVR = 
        create( 0x484E, "Max (NVR)", MAX_139 );
    static final IonHandle MAX_139_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_139 );
    static final IonHandle  MAX_140 = 
        create( 0x090B, "MAX #140" );
    static final IonHandle MAX_140_MAX_NVR = 
        create( 0x484F, "Max (NVR)", MAX_140 );
    static final IonHandle MAX_140_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_140 );
    static final IonHandle  MAX_141 = 
        create( 0x090C, "MAX #141" );
    static final IonHandle MAX_141_MAX_NVR = 
        create( 0x4850, "Max (NVR)", MAX_141 );
    static final IonHandle MAX_141_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_141 );
    static final IonHandle  MAX_142 = 
        create( 0x090D, "MAX #142" );
    static final IonHandle MAX_142_MAX_NVR = 
        create( 0x4851, "Max (NVR)", MAX_142 );
    static final IonHandle MAX_142_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_142 );
    static final IonHandle  MAX_143 = 
        create( 0x090E, "MAX #143" );
    static final IonHandle MAX_143_MAX_NVR = 
        create( 0x4852, "Max (NVR)", MAX_143 );
    static final IonHandle MAX_143_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_143 );
    static final IonHandle  MAX_144 = 
        create( 0x090F, "MAX #144" );
    static final IonHandle MAX_144_MAX_NVR = 
        create( 0x4853, "Max (NVR)", MAX_144 );
    static final IonHandle MAX_144_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_144 );
    static final IonHandle  MAX_145 = 
        create( 0x0910, "MAX #145" );
    static final IonHandle MAX_145_MAX_NVR = 
        create( 0x4854, "Max (NVR)", MAX_145 );
    static final IonHandle MAX_145_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_145 );
    static final IonHandle  MAX_146 = 
        create( 0x0911, "MAX #146" );
    static final IonHandle MAX_146_MAX_NVR = 
        create( 0x4855, "Max (NVR)", MAX_146 );
    static final IonHandle MAX_146_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_146 );
    static final IonHandle  MAX_147 = 
        create( 0x0912, "MAX #147" );
    static final IonHandle MAX_147_MAX_NVR = 
        create( 0x4856, "Max (NVR)", MAX_147 );
    static final IonHandle MAX_147_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_147 );
    static final IonHandle  MAX_148 = 
        create( 0x0913, "MAX #148" );
    static final IonHandle MAX_148_MAX_NVR = 
        create( 0x4857, "Max (NVR)", MAX_148 );
    static final IonHandle MAX_148_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_148 );
    static final IonHandle  MAX_149 = 
        create( 0x0914, "MAX #149" );
    static final IonHandle MAX_149_MAX_NVR = 
        create( 0x4858, "Max (NVR)", MAX_149 );
    static final IonHandle MAX_149_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_149 );
    static final IonHandle  MAX_150 = 
        create( 0x0915, "MAX #150" );
    static final IonHandle MAX_150_MAX_NVR = 
        create( 0x4859, "Max (NVR)", MAX_150 );
    static final IonHandle MAX_150_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_150 );
    static final IonHandle  MAX_151 = 
        create( 0x0916, "MAX #151" );
    static final IonHandle MAX_151_MAX_NVR = 
        create( 0x485A, "Max (NVR)", MAX_151 );
    static final IonHandle MAX_151_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_151 );
    static final IonHandle  MAX_152 = 
        create( 0x0917, "MAX #152" );
    static final IonHandle MAX_152_MAX_NVR = 
        create( 0x485B, "Max (NVR)", MAX_152 );
    static final IonHandle MAX_152_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_152 );
    static final IonHandle  MAX_153 = 
        create( 0x0918, "MAX #153" );
    static final IonHandle MAX_153_MAX_NVR = 
        create( 0x485C, "Max (NVR)", MAX_153 );
    static final IonHandle MAX_153_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_153 );
    static final IonHandle  MAX_154 = 
        create( 0x0919, "MAX #154" );
    static final IonHandle MAX_154_MAX_NVR = 
        create( 0x485D, "Max (NVR)", MAX_154 );
    static final IonHandle MAX_154_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_154 );
    static final IonHandle  MAX_155 = 
        create( 0x091A, "MAX #155" );
    static final IonHandle MAX_155_MAX_NVR = 
        create( 0x485E, "Max (NVR)", MAX_155 );
    static final IonHandle MAX_155_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_155 );
    static final IonHandle  MAX_156 = 
        create( 0x091B, "MAX #156" );
    static final IonHandle MAX_156_MAX_NVR = 
        create( 0x485F, "Max (NVR)", MAX_156 );
    static final IonHandle MAX_156_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_156 );
    static final IonHandle  MAX_157 = 
        create( 0x091C, "MAX #157" );
    static final IonHandle MAX_157_MAX_NVR = 
        create( 0x4860, "Max (NVR)", MAX_157 );
    static final IonHandle MAX_157_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_157 );
    static final IonHandle  MAX_158 = 
        create( 0x091D, "MAX #158" );
    static final IonHandle MAX_158_MAX_NVR = 
        create( 0x4861, "Max (NVR)", MAX_158 );
    static final IonHandle MAX_158_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_158 );
    static final IonHandle  MAX_159 = 
        create( 0x091E, "MAX #159" );
    static final IonHandle MAX_159_MAX_NVR = 
        create( 0x4862, "Max (NVR)", MAX_159 );
    static final IonHandle MAX_159_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_159 );
    static final IonHandle  MAX_160 = 
        create( 0x091F, "MAX #160" );
    static final IonHandle MAX_160_MAX_NVR = 
        create( 0x4863, "Max (NVR)", MAX_160 );
    static final IonHandle MAX_160_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_160 );
    static final IonHandle  MAX_161 = 
        create( 0x0920, "MAX #161" );
    static final IonHandle MAX_161_MAX_NVR = 
        create( 0x4864, "Max (NVR)", MAX_161 );
    static final IonHandle MAX_161_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_161 );
    static final IonHandle  MAX_162 = 
        create( 0x0921, "MAX #162" );
    static final IonHandle MAX_162_MAX_NVR = 
        create( 0x4865, "Max (NVR)", MAX_162 );
    static final IonHandle MAX_162_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_162 );
    static final IonHandle  MAX_163 = 
        create( 0x0922, "MAX #163" );
    static final IonHandle MAX_163_MAX_NVR = 
        create( 0x4866, "Max (NVR)", MAX_163 );
    static final IonHandle MAX_163_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_163 );
    static final IonHandle  MAX_164 = 
        create( 0x0923, "MAX #164" );
    static final IonHandle MAX_164_MAX_NVR = 
        create( 0x4867, "Max (NVR)", MAX_164 );
    static final IonHandle MAX_164_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_164 );
    static final IonHandle  MAX_165 = 
        create( 0x0924, "MAX #165" );
    static final IonHandle MAX_165_MAX_NVR = 
        create( 0x4868, "Max (NVR)", MAX_165 );
    static final IonHandle MAX_165_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_165 );
    static final IonHandle  MAX_166 = 
        create( 0x0925, "MAX #166" );
    static final IonHandle MAX_166_MAX_NVR = 
        create( 0x4869, "Max (NVR)", MAX_166 );
    static final IonHandle MAX_166_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_166 );
    static final IonHandle  MAX_167 = 
        create( 0x0926, "MAX #167" );
    static final IonHandle MAX_167_MAX_NVR = 
        create( 0x486A, "Max (NVR)", MAX_167 );
    static final IonHandle MAX_167_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_167 );
    static final IonHandle  MAX_168 = 
        create( 0x0927, "MAX #168" );
    static final IonHandle MAX_168_MAX_NVR = 
        create( 0x486B, "Max (NVR)", MAX_168 );
    static final IonHandle MAX_168_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_168 );
    static final IonHandle  MAX_169 = 
        create( 0x0928, "MAX #169" );
    static final IonHandle MAX_169_MAX_NVR = 
        create( 0x486C, "Max (NVR)", MAX_169 );
    static final IonHandle MAX_169_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_169 );
    static final IonHandle  MAX_170 = 
        create( 0x0929, "MAX #170" );
    static final IonHandle MAX_170_MAX_NVR = 
        create( 0x486D, "Max (NVR)", MAX_170 );
    static final IonHandle MAX_170_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_170 );
    static final IonHandle  MAX_171 = 
        create( 0x092A, "MAX #171" );
    static final IonHandle MAX_171_MAX_NVR = 
        create( 0x486E, "Max (NVR)", MAX_171 );
    static final IonHandle MAX_171_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_171 );
    static final IonHandle  MAX_172 = 
        create( 0x092B, "MAX #172" );
    static final IonHandle MAX_172_MAX_NVR = 
        create( 0x486F, "Max (NVR)", MAX_172 );
    static final IonHandle MAX_172_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_172 );
    static final IonHandle  MAX_173 = 
        create( 0x092C, "MAX #173" );
    static final IonHandle MAX_173_MAX_NVR = 
        create( 0x4870, "Max (NVR)", MAX_173 );
    static final IonHandle MAX_173_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_173 );
    static final IonHandle  MAX_174 = 
        create( 0x092D, "MAX #174" );
    static final IonHandle MAX_174_MAX_NVR = 
        create( 0x4871, "Max (NVR)", MAX_174 );
    static final IonHandle MAX_174_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_174 );
    static final IonHandle  MAX_175 = 
        create( 0x092E, "MAX #175" );
    static final IonHandle MAX_175_MAX_NVR = 
        create( 0x4872, "Max (NVR)", MAX_175 );
    static final IonHandle MAX_175_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_175 );
    static final IonHandle  MAX_176 = 
        create( 0x092F, "MAX #176" );
    static final IonHandle MAX_176_MAX_NVR = 
        create( 0x4873, "Max (NVR)", MAX_176 );
    static final IonHandle MAX_176_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_176 );
    static final IonHandle  MAX_177 = 
        create( 0x0930, "MAX #177" );
    static final IonHandle MAX_177_MAX_NVR = 
        create( 0x4874, "Max (NVR)", MAX_177 );
    static final IonHandle MAX_177_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_177 );
    static final IonHandle  MAX_178 = 
        create( 0x0931, "MAX #178" );
    static final IonHandle MAX_178_MAX_NVR = 
        create( 0x4875, "Max (NVR)", MAX_178 );
    static final IonHandle MAX_178_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_178 );
    static final IonHandle  MAX_179 = 
        create( 0x0932, "MAX #179" );
    static final IonHandle MAX_179_MAX_NVR = 
        create( 0x4876, "Max (NVR)", MAX_179 );
    static final IonHandle MAX_179_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_179 );
    static final IonHandle  MAX_180 = 
        create( 0x0933, "MAX #180" );
    static final IonHandle MAX_180_MAX_NVR = 
        create( 0x4877, "Max (NVR)", MAX_180 );
    static final IonHandle MAX_180_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_180 );
    static final IonHandle  MAX_181 = 
        create( 0x0934, "MAX #181" );
    static final IonHandle MAX_181_MAX_NVR = 
        create( 0x4878, "Max (NVR)", MAX_181 );
    static final IonHandle MAX_181_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_181 );
    static final IonHandle  MAX_182 = 
        create( 0x0935, "MAX #182" );
    static final IonHandle MAX_182_MAX_NVR = 
        create( 0x4879, "Max (NVR)", MAX_182 );
    static final IonHandle MAX_182_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_182 );
    static final IonHandle  MAX_183 = 
        create( 0x0936, "MAX #183" );
    static final IonHandle MAX_183_MAX_NVR = 
        create( 0x487A, "Max (NVR)", MAX_183 );
    static final IonHandle MAX_183_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_183 );
    static final IonHandle  MAX_184 = 
        create( 0x0937, "MAX #184" );
    static final IonHandle MAX_184_MAX_NVR = 
        create( 0x487B, "Max (NVR)", MAX_184 );
    static final IonHandle MAX_184_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_184 );
    static final IonHandle  MAX_185 = 
        create( 0x0938, "MAX #185" );
    static final IonHandle MAX_185_MAX_NVR = 
        create( 0x487C, "Max (NVR)", MAX_185 );
    static final IonHandle MAX_185_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_185 );
    static final IonHandle  MAX_186 = 
        create( 0x0939, "MAX #186" );
    static final IonHandle MAX_186_MAX_NVR = 
        create( 0x487D, "Max (NVR)", MAX_186 );
    static final IonHandle MAX_186_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_186 );
    static final IonHandle  MAX_187 = 
        create( 0x093A, "MAX #187" );
    static final IonHandle MAX_187_MAX_NVR = 
        create( 0x487E, "Max (NVR)", MAX_187 );
    static final IonHandle MAX_187_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_187 );
    static final IonHandle  MAX_188 = 
        create( 0x093B, "MAX #188" );
    static final IonHandle MAX_188_MAX_NVR = 
        create( 0x487F, "Max (NVR)", MAX_188 );
    static final IonHandle MAX_188_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_188 );
    static final IonHandle  MAX_189 = 
        create( 0x093C, "MAX #189" );
    static final IonHandle MAX_189_MAX_NVR = 
        create( 0x4880, "Max (NVR)", MAX_189 );
    static final IonHandle MAX_189_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_189 );
    static final IonHandle  MAX_190 = 
        create( 0x093D, "MAX #190" );
    static final IonHandle MAX_190_MAX_NVR = 
        create( 0x4881, "Max (NVR)", MAX_190 );
    static final IonHandle MAX_190_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_190 );
    static final IonHandle  MAX_191 = 
        create( 0x093E, "MAX #191" );
    static final IonHandle MAX_191_MAX_NVR = 
        create( 0x4882, "Max (NVR)", MAX_191 );
    static final IonHandle MAX_191_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_191 );
    static final IonHandle  MAX_192 = 
        create( 0x093F, "MAX #192" );
    static final IonHandle MAX_192_MAX_NVR = 
        create( 0x4883, "Max (NVR)", MAX_192 );
    static final IonHandle MAX_192_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_192 );
    static final IonHandle  MAX_193 = 
        create( 0x0940, "MAX #193" );
    static final IonHandle MAX_193_MAX_NVR = 
        create( 0x4884, "Max (NVR)", MAX_193 );
    static final IonHandle MAX_193_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_193 );
    static final IonHandle  MAX_194 = 
        create( 0x0941, "MAX #194" );
    static final IonHandle MAX_194_MAX_NVR = 
        create( 0x4885, "Max (NVR)", MAX_194 );
    static final IonHandle MAX_194_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_194 );
    static final IonHandle  MAX_195 = 
        create( 0x0942, "MAX #195" );
    static final IonHandle MAX_195_MAX_NVR = 
        create( 0x4886, "Max (NVR)", MAX_195 );
    static final IonHandle MAX_195_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_195 );
    static final IonHandle  MAX_196 = 
        create( 0x0943, "MAX #196" );
    static final IonHandle MAX_196_MAX_NVR = 
        create( 0x4887, "Max (NVR)", MAX_196 );
    static final IonHandle MAX_196_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_196 );
    static final IonHandle  MAX_197 = 
        create( 0x0944, "MAX #197" );
    static final IonHandle MAX_197_MAX_NVR = 
        create( 0x4888, "Max (NVR)", MAX_197 );
    static final IonHandle MAX_197_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_197 );
    static final IonHandle  MAX_198 = 
        create( 0x0945, "MAX #198" );
    static final IonHandle MAX_198_MAX_NVR = 
        create( 0x4889, "Max (NVR)", MAX_198 );
    static final IonHandle MAX_198_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_198 );
    static final IonHandle  MAX_199 = 
        create( 0x0946, "MAX #199" );
    static final IonHandle MAX_199_MAX_NVR = 
        create( 0x488A, "Max (NVR)", MAX_199 );
    static final IonHandle MAX_199_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_199 );
    static final IonHandle  MAX_200 = 
        create( 0x0947, "MAX #200" );
    static final IonHandle MAX_200_MAX_NVR = 
        create( 0x488B, "Max (NVR)", MAX_200 );
    static final IonHandle MAX_200_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_200 );
    static final IonHandle  MAX_201 = 
        create( 0x0948, "MAX #201" );
    static final IonHandle MAX_201_MAX_NVR = 
        create( 0x4B52, "Max (NVR)", MAX_201 );
    static final IonHandle MAX_201_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_201 );
    static final IonHandle  MAX_202 = 
        create( 0x0949, "MAX #202" );
    static final IonHandle MAX_202_MAX_NVR = 
        create( 0x4B53, "Max (NVR)", MAX_202 );
    static final IonHandle MAX_202_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_202 );
    static final IonHandle  MAX_203 = 
        create( 0x094A, "MAX #203" );
    static final IonHandle MAX_203_MAX_NVR = 
        create( 0x4B54, "Max (NVR)", MAX_203 );
    static final IonHandle MAX_203_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_203 );
    static final IonHandle  MAX_204 = 
        create( 0x094B, "MAX #204" );
    static final IonHandle MAX_204_MAX_NVR = 
        create( 0x4B55, "Max (NVR)", MAX_204 );
    static final IonHandle MAX_204_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_204 );
    static final IonHandle  MAX_205 = 
        create( 0x094C, "MAX #205" );
    static final IonHandle MAX_205_MAX_NVR = 
        create( 0x4B56, "Max (NVR)", MAX_205 );
    static final IonHandle MAX_205_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_205 );
    static final IonHandle  MAX_206 = 
        create( 0x094D, "MAX #206" );
    static final IonHandle MAX_206_MAX_NVR = 
        create( 0x4B57, "Max (NVR)", MAX_206 );
    static final IonHandle MAX_206_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_206 );
    static final IonHandle  MAX_207 = 
        create( 0x094E, "MAX #207" );
    static final IonHandle MAX_207_MAX_NVR = 
        create( 0x4B58, "Max (NVR)", MAX_207 );
    static final IonHandle MAX_207_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_207 );
    static final IonHandle  MAX_208 = 
        create( 0x094F, "MAX #208" );
    static final IonHandle MAX_208_MAX_NVR = 
        create( 0x4B59, "Max (NVR)", MAX_208 );
    static final IonHandle MAX_208_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_208 );
    static final IonHandle  MAX_209 = 
        create( 0x0950, "MAX #209" );
    static final IonHandle MAX_209_MAX_NVR = 
        create( 0x4B5A, "Max (NVR)", MAX_209 );
    static final IonHandle MAX_209_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_209 );
    static final IonHandle  MAX_210 = 
        create( 0x0951, "MAX #210" );
    static final IonHandle MAX_210_MAX_NVR = 
        create( 0x4B5B, "Max (NVR)", MAX_210 );
    static final IonHandle MAX_210_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_210 );
    static final IonHandle  MAX_211 = 
        create( 0x0952, "MAX #211" );
    static final IonHandle MAX_211_MAX_NVR = 
        create( 0x4B5C, "Max (NVR)", MAX_211 );
    static final IonHandle MAX_211_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_211 );
    static final IonHandle  MAX_212 = 
        create( 0x0953, "MAX #212" );
    static final IonHandle MAX_212_MAX_NVR = 
        create( 0x4B5D, "Max (NVR)", MAX_212 );
    static final IonHandle MAX_212_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_212 );
    static final IonHandle  MAX_213 = 
        create( 0x0954, "MAX #213" );
    static final IonHandle MAX_213_MAX_NVR = 
        create( 0x4B5E, "Max (NVR)", MAX_213 );
    static final IonHandle MAX_213_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_213 );
    static final IonHandle  MAX_214 = 
        create( 0x0955, "MAX #214" );
    static final IonHandle MAX_214_MAX_NVR = 
        create( 0x4B5F, "Max (NVR)", MAX_214 );
    static final IonHandle MAX_214_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_214 );
    static final IonHandle  MAX_215 = 
        create( 0x0956, "MAX #215" );
    static final IonHandle MAX_215_MAX_NVR = 
        create( 0x4B60, "Max (NVR)", MAX_215 );
    static final IonHandle MAX_215_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_215 );
    static final IonHandle  MAX_216 = 
        create( 0x0957, "MAX #216" );
    static final IonHandle MAX_216_MAX_NVR = 
        create( 0x4B61, "Max (NVR)", MAX_216 );
    static final IonHandle MAX_216_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_216 );
    static final IonHandle  MAX_217 = 
        create( 0x0958, "MAX #217" );
    static final IonHandle MAX_217_MAX_NVR = 
        create( 0x4B62, "Max (NVR)", MAX_217 );
    static final IonHandle MAX_217_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_217 );
    static final IonHandle  MAX_218 = 
        create( 0x0959, "MAX #218" );
    static final IonHandle MAX_218_MAX_NVR = 
        create( 0x4B63, "Max (NVR)", MAX_218 );
    static final IonHandle MAX_218_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_218 );
    static final IonHandle  MAX_219 = 
        create( 0x095A, "MAX #219" );
    static final IonHandle MAX_219_MAX_NVR = 
        create( 0x4B64, "Max (NVR)", MAX_219 );
    static final IonHandle MAX_219_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_219 );
    static final IonHandle  MAX_220 = 
        create( 0x095B, "MAX #220" );
    static final IonHandle MAX_220_MAX_NVR = 
        create( 0x4B65, "Max (NVR)", MAX_220 );
    static final IonHandle MAX_220_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_220 );
    static final IonHandle  MAX_221 = 
        create( 0x095C, "MAX #221" );
    static final IonHandle MAX_221_MAX_NVR = 
        create( 0x3F98, "Max (NVR)", MAX_221 );
    static final IonHandle MAX_221_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_221 );
    static final IonHandle  MAX_222 = 
        create( 0x095D, "MAX #222" );
    static final IonHandle MAX_222_MAX_NVR = 
        create( 0x3F99, "Max (NVR)", MAX_222 );
    static final IonHandle MAX_222_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_222 );
    static final IonHandle  MAX_223 = 
        create( 0x095E, "MAX #223" );
    static final IonHandle MAX_223_MAX_NVR = 
        create( 0x3F9A, "Max (NVR)", MAX_223 );
    static final IonHandle MAX_223_EVENT_EVR = 
        create( 0x3150, "Event (EVR)", MAX_223 );
    static final IonHandle  MAX_224 = 
        create( 0x095F, "MAX #224" );
    static final IonHandle MAX_224_MAX_NVR = 
        create( 0x3F9B, "Max (NVR)", MAX_224 );
    static final IonHandle MAX_224_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_224 );
    static final IonHandle  MAX_225 = 
        create( 0x0960, "MAX #225" );
    static final IonHandle MAX_225_MAX_NVR = 
        create( 0x3F9C, "Max (NVR)", MAX_225 );
    static final IonHandle MAX_225_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_225 );
    static final IonHandle  MAX_226 = 
        create( 0x0961, "MAX #226" );
    static final IonHandle MAX_226_MAX_NVR = 
        create( 0x3F9D, "Max (NVR)", MAX_226 );
    static final IonHandle MAX_226_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_226 );
    static final IonHandle  MAX_227 = 
        create( 0x0962, "MAX #227" );
    static final IonHandle MAX_227_MAX_NVR = 
        create( 0x3F9E, "Max (NVR)", MAX_227 );
    static final IonHandle MAX_227_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_227 );
    static final IonHandle  MAX_228 = 
        create( 0x0963, "MAX #228" );
    static final IonHandle MAX_228_MAX_NVR = 
        create( 0x3F9F, "Max (NVR)", MAX_228 );
    static final IonHandle MAX_228_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_228 );
    static final IonHandle  MAX_229 = 
        create( 0x0964, "MAX #229" );
    static final IonHandle MAX_229_MAX_NVR = 
        create( 0x3FA0, "Max (NVR)", MAX_229 );
    static final IonHandle MAX_229_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_229 );
    static final IonHandle  MAX_230 = 
        create( 0x0965, "MAX #230" );
    static final IonHandle MAX_230_MAX_NVR = 
        create( 0x3FA1, "Max (NVR)", MAX_230 );
    static final IonHandle MAX_230_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_230 );
    static final IonHandle  MAX_231 = 
        create( 0x0966, "MAX #231" );
    static final IonHandle MAX_231_MAX_NVR = 
        create( 0x3FA2, "Max (NVR)", MAX_231 );
    static final IonHandle MAX_231_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_231 );
    static final IonHandle  MAX_232 = 
        create( 0x0967, "MAX #232" );
    static final IonHandle MAX_232_MAX_NVR = 
        create( 0x3FA3, "Max (NVR)", MAX_232 );
    static final IonHandle MAX_232_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_232 );
    static final IonHandle  MAX_233 = 
        create( 0x0968, "MAX #233" );
    static final IonHandle MAX_233_MAX_NVR = 
        create( 0x3FA4, "Max (NVR)", MAX_233 );
    static final IonHandle MAX_233_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_233 );
    static final IonHandle  MAX_234 = 
        create( 0x0969, "MAX #234" );
    static final IonHandle MAX_234_MAX_NVR = 
        create( 0x3FA5, "Max (NVR)", MAX_234 );
    static final IonHandle MAX_234_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_234 );
    static final IonHandle  MAX_235 = 
        create( 0x096A, "MAX #235" );
    static final IonHandle MAX_235_MAX_NVR = 
        create( 0x3FA6, "Max (NVR)", MAX_235 );
    static final IonHandle MAX_235_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_235 );
    static final IonHandle  MAX_236 = 
        create( 0x096B, "MAX #236" );
    static final IonHandle MAX_236_MAX_NVR = 
        create( 0x3FA7, "Max (NVR)", MAX_236 );
    static final IonHandle MAX_236_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_236 );
    static final IonHandle  MAX_237 = 
        create( 0x096C, "MAX #237" );
    static final IonHandle MAX_237_MAX_NVR = 
        create( 0x3FA8, "Max (NVR)", MAX_237 );
    static final IonHandle MAX_237_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_237 );
    static final IonHandle  MAX_238 = 
        create( 0x096D, "MAX #238" );
    static final IonHandle MAX_238_MAX_NVR = 
        create( 0x3FA9, "Max (NVR)", MAX_238 );
    static final IonHandle MAX_238_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_238 );
    static final IonHandle  MAX_239 = 
        create( 0x096E, "MAX #239" );
    static final IonHandle MAX_239_MAX_NVR = 
        create( 0x3FAA, "Max (NVR)", MAX_239 );
    static final IonHandle MAX_239_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_239 );
    static final IonHandle  MAX_240 = 
        create( 0x096F, "MAX #240" );
    static final IonHandle MAX_240_MAX_NVR = 
        create( 0x3FAB, "Max (NVR)", MAX_240 );
    static final IonHandle MAX_240_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_240 );
    static final IonHandle  MAX_241 = 
        create( 0x0970, "MAX #241" );
    static final IonHandle MAX_241_MAX_NVR = 
        create( 0x3FAC, "Max (NVR)", MAX_241 );
    static final IonHandle MAX_241_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_241 );
    static final IonHandle  MAX_242 = 
        create( 0x0971, "MAX #242" );
    static final IonHandle MAX_242_MAX_NVR = 
        create( 0x3FAD, "Max (NVR)", MAX_242 );
    static final IonHandle MAX_242_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_242 );
    static final IonHandle  MAX_243 = 
        create( 0x0972, "MAX #243" );
    static final IonHandle MAX_243_MAX_NVR = 
        create( 0x3FAE, "Max (NVR)", MAX_243 );
    static final IonHandle MAX_243_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_243 );
    static final IonHandle  MAX_244 = 
        create( 0x0973, "MAX #244" );
    static final IonHandle MAX_244_MAX_NVR = 
        create( 0x3FAF, "Max (NVR)", MAX_244 );
    static final IonHandle MAX_244_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_244 );
    static final IonHandle  MAX_245 = 
        create( 0x0974, "MAX #245" );
    static final IonHandle MAX_245_MAX_NVR = 
        create( 0x3FB0, "Max (NVR)", MAX_245 );
    static final IonHandle MAX_245_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MAX_245 );
    static final IonHandle  MIN_1 = 
        create( 0x580, "MIN #1" );
    static final IonHandle MIN_1_MIN_NVR = 
        create( 0x58C0, "Min (NVR)", MIN_1 );
    static final IonHandle MIN_1_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_1 );
    static final IonHandle  MIN_2 = 
        create( 0x0581, "MIN #2" );
    static final IonHandle MIN_2_MIN_NVR = 
        create( 0x58C1, "Min (NVR)", MIN_2 );
    static final IonHandle MIN_2_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_2 );
    static final IonHandle  MIN_3 = 
        create( 0x0582, "MIN #3" );
    static final IonHandle MIN_3_MIN_NVR = 
        create( 0x58C2, "Min (NVR)", MIN_3 );
    static final IonHandle MIN_3_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_3 );
    static final IonHandle  MIN_4 = 
        create( 0x0583, "MIN #4" );
    static final IonHandle MIN_4_MIN_NVR = 
        create( 0x58C3, "Min (NVR)", MIN_4 );
    static final IonHandle MIN_4_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_4 );
    static final IonHandle  MIN_5 = 
        create( 0x0584, "MIN #5" );
    static final IonHandle MIN_5_MIN_NVR = 
        create( 0x58C4, "Min (NVR)", MIN_5 );
    static final IonHandle MIN_5_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_5 );
    static final IonHandle  MIN_6 = 
        create( 0x0585, "MIN #6" );
    static final IonHandle MIN_6_MIN_NVR = 
        create( 0x58C5, "Min (NVR)", MIN_6 );
    static final IonHandle MIN_6_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_6 );
    static final IonHandle  MIN_7 = 
        create( 0x0586, "MIN #7" );
    static final IonHandle MIN_7_MIN_NVR = 
        create( 0x58C6, "Min (NVR)", MIN_7 );
    static final IonHandle MIN_7_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_7 );
    static final IonHandle  MIN_8 = 
        create( 0x0587, "MIN #8" );
    static final IonHandle MIN_8_MIN_NVR = 
        create( 0x58C7, "Min (NVR)", MIN_8 );
    static final IonHandle MIN_8_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_8 );
    static final IonHandle  MIN_9 = 
        create( 0x0588, "MIN #9" );
    static final IonHandle MIN_9_MIN_NVR = 
        create( 0x58C8, "Min (NVR)", MIN_9 );
    static final IonHandle MIN_9_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_9 );
    static final IonHandle  MIN_10 = 
        create( 0x0589, "MIN #10" );
    static final IonHandle MIN_10_MIN_NVR = 
        create( 0x58C9, "Min (NVR)", MIN_10 );
    static final IonHandle MIN_10_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_10 );
    static final IonHandle  MIN_11 = 
        create( 0x058A, "MIN #11" );
    static final IonHandle MIN_11_MIN_NVR = 
        create( 0x58CA, "Min (NVR)", MIN_11 );
    static final IonHandle MIN_11_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_11 );
    static final IonHandle  MIN_12 = 
        create( 0x058B, "MIN #12" );
    static final IonHandle MIN_12_MIN_NVR = 
        create( 0x58CB, "Min (NVR)", MIN_12 );
    static final IonHandle MIN_12_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_12 );
    static final IonHandle  MIN_13 = 
        create( 0x058C, "MIN #13" );
    static final IonHandle MIN_13_MIN_NVR = 
        create( 0x58CC, "Min (NVR)", MIN_13 );
    static final IonHandle MIN_13_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_13 );
    static final IonHandle  MIN_14 = 
        create( 0x058D, "MIN #14" );
    static final IonHandle MIN_14_MIN_NVR = 
        create( 0x58CD, "Min (NVR)", MIN_14 );
    static final IonHandle MIN_14_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_14 );
    static final IonHandle  MIN_15 = 
        create( 0x058E, "MIN #15" );
    static final IonHandle MIN_15_MIN_NVR = 
        create( 0x58CE, "Min (NVR)", MIN_15 );
    static final IonHandle MIN_15_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_15 );
    static final IonHandle  MIN_16 = 
        create( 0x058F, "MIN #16" );
    static final IonHandle MIN_16_MIN_NVR = 
        create( 0x58CF, "Min (NVR)", MIN_16 );
    static final IonHandle MIN_16_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_16 );
    static final IonHandle  MIN_17 = 
        create( 0x0590, "MIN #17" );
    static final IonHandle MIN_17_MIN_NVR = 
        create( 0x58D0, "Min (NVR)", MIN_17 );
    static final IonHandle MIN_17_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_17 );
    static final IonHandle  MIN_18 = 
        create( 0x0591, "MIN #18" );
    static final IonHandle MIN_18_MIN_NVR = 
        create( 0x58D1, "Min (NVR)", MIN_18 );
    static final IonHandle MIN_18_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_18 );
    static final IonHandle  MIN_19 = 
        create( 0x0592, "MIN #19" );
    static final IonHandle MIN_19_MIN_NVR = 
        create( 0x58D2, "Min (NVR)", MIN_19 );
    static final IonHandle MIN_19_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_19 );
    static final IonHandle  MIN_20 = 
        create( 0x0593, "MIN #20" );
    static final IonHandle MIN_20_MIN_NVR = 
        create( 0x58D3, "Min (NVR)", MIN_20 );
    static final IonHandle MIN_20_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_20 );
    static final IonHandle  MIN_21 = 
        create( 0x0594, "MIN #21" );
    static final IonHandle MIN_21_MIN_NVR = 
        create( 0x58D4, "Min (NVR)", MIN_21 );
    static final IonHandle MIN_21_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_21 );
    static final IonHandle  MIN_22 = 
        create( 0x0595, "MIN #22" );
    static final IonHandle MIN_22_MIN_NVR = 
        create( 0x58D5, "Min (NVR)", MIN_22 );
    static final IonHandle MIN_22_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_22 );
    static final IonHandle  MIN_23 = 
        create( 0x0596, "MIN #23" );
    static final IonHandle MIN_23_MIN_NVR = 
        create( 0x58D6, "Min (NVR)", MIN_23 );
    static final IonHandle MIN_23_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_23 );
    static final IonHandle  MIN_24 = 
        create( 0x0597, "MIN #24" );
    static final IonHandle MIN_24_MIN_NVR = 
        create( 0x58D7, "Min (NVR)", MIN_24 );
    static final IonHandle MIN_24_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_24 );
    static final IonHandle  MIN_25 = 
        create( 0x0598, "MIN #25" );
    static final IonHandle MIN_25_MIN_NVR = 
        create( 0x58D8, "Min (NVR)", MIN_25 );
    static final IonHandle MIN_25_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_25 );
    static final IonHandle  MIN_26 = 
        create( 0x0599, "MIN #26" );
    static final IonHandle MIN_26_MIN_NVR = 
        create( 0x58D9, "Min (NVR)", MIN_26 );
    static final IonHandle MIN_26_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_26 );
    static final IonHandle  MIN_27 = 
        create( 0x059A, "MIN #27" );
    static final IonHandle MIN_27_MIN_NVR = 
        create( 0x58DA, "Min (NVR)", MIN_27 );
    static final IonHandle MIN_27_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_27 );
    static final IonHandle  MIN_28 = 
        create( 0x059B, "MIN #28" );
    static final IonHandle MIN_28_MIN_NVR = 
        create( 0x58DB, "Min (NVR)", MIN_28 );
    static final IonHandle MIN_28_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_28 );
    static final IonHandle  MIN_29 = 
        create( 0x059C, "MIN #29" );
    static final IonHandle MIN_29_MIN_NVR = 
        create( 0x58DC, "Min (NVR)", MIN_29 );
    static final IonHandle MIN_29_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_29 );
    static final IonHandle  MIN_30 = 
        create( 0x059D, "MIN #30" );
    static final IonHandle MIN_30_MIN_NVR = 
        create( 0x58DD, "Min (NVR)", MIN_30 );
    static final IonHandle MIN_30_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_30 );
    static final IonHandle  MIN_31 = 
        create( 0x059E, "MIN #31" );
    static final IonHandle MIN_31_MIN_NVR = 
        create( 0x58DE, "Min (NVR)", MIN_31 );
    static final IonHandle MIN_31_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_31 );
    static final IonHandle  MIN_32 = 
        create( 0x059F, "MIN #32" );
    static final IonHandle MIN_32_MIN_NVR = 
        create( 0x58DF, "Min (NVR)", MIN_32 );
    static final IonHandle MIN_32_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_32 );
    static final IonHandle  MIN_33 = 
        create( 0x05A0, "MIN #33" );
    static final IonHandle MIN_33_MIN_NVR = 
        create( 0x5E0A, "Min (NVR)", MIN_33 );
    static final IonHandle MIN_33_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_33 );
    static final IonHandle  MIN_34 = 
        create( 0x05A1, "MIN #34" );
    static final IonHandle MIN_34_MIN_NVR = 
        create( 0x5E0B, "Min (NVR)", MIN_34 );
    static final IonHandle MIN_34_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_34 );
    static final IonHandle  MIN_35 = 
        create( 0x05A2, "MIN #35" );
    static final IonHandle MIN_35_MIN_NVR = 
        create( 0x5E0C, "Min (NVR)", MIN_35 );
    static final IonHandle MIN_35_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_35 );
    static final IonHandle  MIN_36 = 
        create( 0x05A3, "MIN #36" );
    static final IonHandle MIN_36_MIN_NVR = 
        create( 0x5E0D, "Min (NVR)", MIN_36 );
    static final IonHandle MIN_36_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_36 );
    static final IonHandle  MIN_37 = 
        create( 0x05A4, "MIN #37" );
    static final IonHandle MIN_37_MIN_NVR = 
        create( 0x5E0E, "Min (NVR)", MIN_37 );
    static final IonHandle MIN_37_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_37 );
    static final IonHandle  MIN_38 = 
        create( 0x05A5, "MIN #38" );
    static final IonHandle MIN_38_MIN_NVR = 
        create( 0x5E0F, "Min (NVR)", MIN_38 );
    static final IonHandle MIN_38_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_38 );
    static final IonHandle  MIN_39 = 
        create( 0x05A6, "MIN #39" );
    static final IonHandle MIN_39_MIN_NVR = 
        create( 0x5E10, "Min (NVR)", MIN_39 );
    static final IonHandle MIN_39_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_39 );
    static final IonHandle  MIN_40 = 
        create( 0x05A7, "MIN #40" );
    static final IonHandle MIN_40_MIN_NVR = 
        create( 0x5E11, "Min (NVR)", MIN_40 );
    static final IonHandle MIN_40_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_40 );
    static final IonHandle  MIN_41 = 
        create( 0x05A8, "MIN #41" );
    static final IonHandle MIN_41_MIN_NVR = 
        create( 0x5E12, "Min (NVR)", MIN_41 );
    static final IonHandle MIN_41_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_41 );
    static final IonHandle  MIN_42 = 
        create( 0x05A9, "MIN #42" );
    static final IonHandle MIN_42_MIN_NVR = 
        create( 0x5E13, "Min (NVR)", MIN_42 );
    static final IonHandle MIN_42_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_42 );
    static final IonHandle  MIN_43 = 
        create( 0x05AA, "MIN #43" );
    static final IonHandle MIN_43_MIN_NVR = 
        create( 0x5E14, "Min (NVR)", MIN_43 );
    static final IonHandle MIN_43_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_43 );
    static final IonHandle  MIN_44 = 
        create( 0x05AB, "MIN #44" );
    static final IonHandle MIN_44_MIN_NVR = 
        create( 0x5E15, "Min (NVR)", MIN_44 );
    static final IonHandle MIN_44_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_44 );
    static final IonHandle  MIN_45 = 
        create( 0x05AC, "MIN #45" );
    static final IonHandle MIN_45_MIN_NVR = 
        create( 0x5E16, "Min (NVR)", MIN_45 );
    static final IonHandle MIN_45_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_45 );
    static final IonHandle  MIN_46 = 
        create( 0x05AD, "MIN #46" );
    static final IonHandle MIN_46_MIN_NVR = 
        create( 0x5E17, "Min (NVR)", MIN_46 );
    static final IonHandle MIN_46_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_46 );
    static final IonHandle  MIN_47 = 
        create( 0x05AE, "MIN #47" );
    static final IonHandle MIN_47_MIN_NVR = 
        create( 0x5E18, "Min (NVR)", MIN_47 );
    static final IonHandle MIN_47_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_47 );
    static final IonHandle  MIN_48 = 
        create( 0x05AF, "MIN #48" );
    static final IonHandle MIN_48_MIN_NVR = 
        create( 0x5E19, "Min (NVR)", MIN_48 );
    static final IonHandle MIN_48_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_48 );
    static final IonHandle  MIN_49 = 
        create( 0x05B0, "MIN #49" );
    static final IonHandle MIN_49_MIN_NVR = 
        create( 0x5E1A, "Min (NVR)", MIN_49 );
    static final IonHandle MIN_49_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_49 );
    static final IonHandle  MIN_50 = 
        create( 0x05B1, "MIN #50" );
    static final IonHandle MIN_50_MIN_NVR = 
        create( 0x5E1B, "Min (NVR)", MIN_50 );
    static final IonHandle MIN_50_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_50 );
    static final IonHandle  MIN_51 = 
        create( 0x05B2, "MIN #51" );
    static final IonHandle MIN_51_MIN_NVR = 
        create( 0x5E1C, "Min (NVR)", MIN_51 );
    static final IonHandle MIN_51_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_51 );
    static final IonHandle  MIN_52 = 
        create( 0x05B3, "MIN #52" );
    static final IonHandle MIN_52_MIN_NVR = 
        create( 0x5E1D, "Min (NVR)", MIN_52 );
    static final IonHandle MIN_52_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_52 );
    static final IonHandle  MIN_53 = 
        create( 0x05B4, "MIN #53" );
    static final IonHandle MIN_53_MIN_NVR = 
        create( 0x5E1E, "Min (NVR)", MIN_53 );
    static final IonHandle MIN_53_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_53 );
    static final IonHandle  MIN_54 = 
        create( 0x05B5, "MIN #54" );
    static final IonHandle MIN_54_MIN_NVR = 
        create( 0x5E1F, "Min (NVR)", MIN_54 );
    static final IonHandle MIN_54_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_54 );
    static final IonHandle  MIN_55 = 
        create( 0x05B6, "MIN #55" );
    static final IonHandle MIN_55_MIN_NVR = 
        create( 0x5E20, "Min (NVR)", MIN_55 );
    static final IonHandle MIN_55_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_55 );
    static final IonHandle  MIN_56 = 
        create( 0x05B7, "MIN #56" );
    static final IonHandle MIN_56_MIN_NVR = 
        create( 0x5E21, "Min (NVR)", MIN_56 );
    static final IonHandle MIN_56_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_56 );
    static final IonHandle  MIN_57 = 
        create( 0x05B8, "MIN #57" );
    static final IonHandle MIN_57_MIN_NVR = 
        create( 0x5E22, "Min (NVR)", MIN_57 );
    static final IonHandle MIN_57_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_57 );
    static final IonHandle  MIN_58 = 
        create( 0x05B9, "MIN #58" );
    static final IonHandle MIN_58_MIN_NVR = 
        create( 0x5E23, "Min (NVR)", MIN_58 );
    static final IonHandle MIN_58_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_58 );
    static final IonHandle  MIN_59 = 
        create( 0x05BA, "MIN #59" );
    static final IonHandle MIN_59_MIN_NVR = 
        create( 0x5E24, "Min (NVR)", MIN_59 );
    static final IonHandle MIN_59_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_59 );
    static final IonHandle  MIN_60 = 
        create( 0x05BB, "MIN #60" );
    static final IonHandle MIN_60_MIN_NVR = 
        create( 0x5E25, "Min (NVR)", MIN_60 );
    static final IonHandle MIN_60_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_60 );
    static final IonHandle  MIN_61 = 
        create( 0x05BC, "MIN #61" );
    static final IonHandle MIN_61_MIN_NVR = 
        create( 0x5F81, "Min (NVR)", MIN_61 );
    static final IonHandle MIN_61_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_61 );
    static final IonHandle  MIN_62 = 
        create( 0x05BD, "MIN #62" );
    static final IonHandle MIN_62_MIN_NVR = 
        create( 0x5F82, "Min (NVR)", MIN_62 );
    static final IonHandle MIN_62_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_62 );
    static final IonHandle  MIN_63 = 
        create( 0x05BE, "MIN #63" );
    static final IonHandle MIN_63_MIN_NVR = 
        create( 0x5F83, "Min (NVR)", MIN_63 );
    static final IonHandle MIN_63_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_63 );
    static final IonHandle  MIN_64 = 
        create( 0x05BF, "MIN #64" );
    static final IonHandle MIN_64_MIN_NVR = 
        create( 0x5F84, "Min (NVR)", MIN_64 );
    static final IonHandle MIN_64_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_64 );
    static final IonHandle  MIN_65 = 
        create( 0x05C0, "MIN #65" );
    static final IonHandle MIN_65_MIN_NVR = 
        create( 0x5F85, "Min (NVR)", MIN_65 );
    static final IonHandle MIN_65_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_65 );
    static final IonHandle  MIN_66 = 
        create( 0x05C1, "MIN #66" );
    static final IonHandle MIN_66_MIN_NVR = 
        create( 0x5F86, "Min (NVR)", MIN_66 );
    static final IonHandle MIN_66_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_66 );
    static final IonHandle  MIN_67 = 
        create( 0x05C2, "MIN #67" );
    static final IonHandle MIN_67_MIN_NVR = 
        create( 0x5F87, "Min (NVR)", MIN_67 );
    static final IonHandle MIN_67_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_67 );
    static final IonHandle  MIN_68 = 
        create( 0x05C3, "MIN #68" );
    static final IonHandle MIN_68_MIN_NVR = 
        create( 0x5F88, "Min (NVR)", MIN_68 );
    static final IonHandle MIN_68_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_68 );
    static final IonHandle  MIN_69 = 
        create( 0x05C4, "MIN #69" );
    static final IonHandle MIN_69_MIN_NVR = 
        create( 0x5F89, "Min (NVR)", MIN_69 );
    static final IonHandle MIN_69_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_69 );
    static final IonHandle  MIN_70 = 
        create( 0x05C5, "MIN #70" );
    static final IonHandle MIN_70_MIN_NVR = 
        create( 0x5F8A, "Min (NVR)", MIN_70 );
    static final IonHandle MIN_70_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_70 );
    static final IonHandle  MIN_71 = 
        create( 0x05C6, "MIN #71" );
    static final IonHandle MIN_71_MIN_NVR = 
        create( 0x5F8B, "Min (NVR)", MIN_71 );
    static final IonHandle MIN_71_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_71 );
    static final IonHandle  MIN_72 = 
        create( 0x05C7, "MIN #72" );
    static final IonHandle MIN_72_MIN_NVR = 
        create( 0x5F8C, "Min (NVR)", MIN_72 );
    static final IonHandle MIN_72_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_72 );
    static final IonHandle  MIN_73 = 
        create( 0x05C8, "MIN #73" );
    static final IonHandle MIN_73_MIN_NVR = 
        create( 0x5F8D, "Min (NVR)", MIN_73 );
    static final IonHandle MIN_73_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_73 );
    static final IonHandle  MIN_74 = 
        create( 0x05C9, "MIN #74" );
    static final IonHandle MIN_74_MIN_NVR = 
        create( 0x5F8E, "Min (NVR)", MIN_74 );
    static final IonHandle MIN_74_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_74 );
    static final IonHandle  MIN_75 = 
        create( 0x05CA, "MIN #75" );
    static final IonHandle MIN_75_MIN_NVR = 
        create( 0x5F8F, "Min (NVR)", MIN_75 );
    static final IonHandle MIN_75_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_75 );
    static final IonHandle  MIN_76 = 
        create( 0x05CB, "MIN #76" );
    static final IonHandle MIN_76_MIN_NVR = 
        create( 0x5F90, "Min (NVR)", MIN_76 );
    static final IonHandle MIN_76_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_76 );
    static final IonHandle  MIN_77 = 
        create( 0x05CC, "MIN #77" );
    static final IonHandle MIN_77_MIN_NVR = 
        create( 0x5F91, "Min (NVR)", MIN_77 );
    static final IonHandle MIN_77_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_77 );
    static final IonHandle  MIN_78 = 
        create( 0x05CD, "MIN #78" );
    static final IonHandle MIN_78_MIN_NVR = 
        create( 0x5F92, "Min (NVR)", MIN_78 );
    static final IonHandle MIN_78_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_78 );
    static final IonHandle  MIN_79 = 
        create( 0x05CE, "MIN #79" );
    static final IonHandle MIN_79_MIN_NVR = 
        create( 0x5F93, "Min (NVR)", MIN_79 );
    static final IonHandle MIN_79_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_79 );
    static final IonHandle  MIN_80 = 
        create( 0x05CF, "MIN #80" );
    static final IonHandle MIN_80_MIN_NVR = 
        create( 0x5F94, "Min (NVR)", MIN_80 );
    static final IonHandle MIN_80_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_80 );
    static final IonHandle  MIN_81 = 
        create( 0x05D0, "MIN #81" );
    static final IonHandle MIN_81_MIN_NVR = 
        create( 0x5F95, "Min (NVR)", MIN_81 );
    static final IonHandle MIN_81_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_81 );
    static final IonHandle  MIN_82 = 
        create( 0x05D1, "MIN #82" );
    static final IonHandle MIN_82_MIN_NVR = 
        create( 0x5F96, "Min (NVR)", MIN_82 );
    static final IonHandle MIN_82_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_82 );
    static final IonHandle  MIN_83 = 
        create( 0x05D2, "MIN #83" );
    static final IonHandle MIN_83_MIN_NVR = 
        create( 0x5F97, "Min (NVR)", MIN_83 );
    static final IonHandle MIN_83_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_83 );
    static final IonHandle  MIN_84 = 
        create( 0x05D3, "MIN #84" );
    static final IonHandle MIN_84_MIN_NVR = 
        create( 0x5F98, "Min (NVR)", MIN_84 );
    static final IonHandle MIN_84_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_84 );
    static final IonHandle  MIN_85 = 
        create( 0x05D4, "MIN #85" );
    static final IonHandle MIN_85_MIN_NVR = 
        create( 0x5F99, "Min (NVR)", MIN_85 );
    static final IonHandle MIN_85_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_85 );
    static final IonHandle  MIN_86 = 
        create( 0x05D5, "MIN #86" );
    static final IonHandle MIN_86_MIN_NVR = 
        create( 0x5F9A, "Min (NVR)", MIN_86 );
    static final IonHandle MIN_86_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_86 );
    static final IonHandle  MIN_87 = 
        create( 0x05D6, "MIN #87" );
    static final IonHandle MIN_87_MIN_NVR = 
        create( 0x5F9B, "Min (NVR)", MIN_87 );
    static final IonHandle MIN_87_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_87 );
    static final IonHandle  MIN_88 = 
        create( 0x05D7, "MIN #88" );
    static final IonHandle MIN_88_MIN_NVR = 
        create( 0x5F9C, "Min (NVR)", MIN_88 );
    static final IonHandle MIN_88_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_88 );
    static final IonHandle  MIN_89 = 
        create( 0x05D8, "MIN #89" );
    static final IonHandle MIN_89_MIN_NVR = 
        create( 0x5F9D, "Min (NVR)", MIN_89 );
    static final IonHandle MIN_89_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_89 );
    static final IonHandle  MIN_90 = 
        create( 0x05D9, "MIN #90" );
    static final IonHandle MIN_90_MIN_NVR = 
        create( 0x5F9E, "Min (NVR)", MIN_90 );
    static final IonHandle MIN_90_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_90 );
    static final IonHandle  MIN_91 = 
        create( 0x05DA, "MIN #91" );
    static final IonHandle MIN_91_MIN_NVR = 
        create( 0x3FB1, "Min (NVR)", MIN_91 );
    static final IonHandle MIN_91_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_91 );
    static final IonHandle  MIN_92 = 
        create( 0x05DB, "MIN #92" );
    static final IonHandle MIN_92_MIN_NVR = 
        create( 0x3FB2, "Min (NVR)", MIN_92 );
    static final IonHandle MIN_92_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_92 );
    static final IonHandle  MIN_93 = 
        create( 0x05DC, "MIN #93" );
    static final IonHandle MIN_93_MIN_NVR = 
        create( 0x3FB3, "Min (NVR)", MIN_93 );
    static final IonHandle MIN_93_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_93 );
    static final IonHandle  MIN_94 = 
        create( 0x05DD, "MIN #94" );
    static final IonHandle MIN_94_MIN_NVR = 
        create( 0x3FB4, "Min (NVR)", MIN_94 );
    static final IonHandle MIN_94_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_94 );
    static final IonHandle  MIN_95 = 
        create( 0x05DE, "MIN #95" );
    static final IonHandle MIN_95_MIN_NVR = 
        create( 0x3FB5, "Min (NVR)", MIN_95 );
    static final IonHandle MIN_95_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_95 );
    static final IonHandle  MIN_96 = 
        create( 0x05DF, "MIN #96" );
    static final IonHandle MIN_96_MIN_NVR = 
        create( 0x3FB6, "Min (NVR)", MIN_96 );
    static final IonHandle MIN_96_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_96 );
    static final IonHandle  MIN_97 = 
        create( 0x05E0, "MIN #97" );
    static final IonHandle MIN_97_MIN_NVR = 
        create( 0x3FB7, "Min (NVR)", MIN_97 );
    static final IonHandle MIN_97_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_97 );
    static final IonHandle  MIN_98 = 
        create( 0x05E1, "MIN #98" );
    static final IonHandle MIN_98_MIN_NVR = 
        create( 0x3FB8, "Min (NVR)", MIN_98 );
    static final IonHandle MIN_98_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_98 );
    static final IonHandle  MIN_99 = 
        create( 0x05E2, "MIN #99" );
    static final IonHandle MIN_99_MIN_NVR = 
        create( 0x3FB9, "Min (NVR)", MIN_99 );
    static final IonHandle MIN_99_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_99 );
    static final IonHandle  MIN_100 = 
        create( 0x05E3, "MIN #100" );
    static final IonHandle MIN_100_MIN_NVR = 
        create( 0x3FBA, "Min (NVR)", MIN_100 );
    static final IonHandle MIN_100_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_100 );
    static final IonHandle  MIN_101 = 
        create( 0x05E4, "MIN #101" );
    static final IonHandle MIN_101_MIN_NVR = 
        create( 0x3FBB, "Min (NVR)", MIN_101 );
    static final IonHandle MIN_101_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_101 );
    static final IonHandle  MIN_102 = 
        create( 0x05E5, "MIN #102" );
    static final IonHandle MIN_102_MIN_NVR = 
        create( 0x3FBC, "Min (NVR)", MIN_102 );
    static final IonHandle MIN_102_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_102 );
    static final IonHandle  MIN_103 = 
        create( 0x05E6, "MIN #103" );
    static final IonHandle MIN_103_MIN_NVR = 
        create( 0x3FBD, "Min (NVR)", MIN_103 );
    static final IonHandle MIN_103_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_103 );
    static final IonHandle  MIN_104 = 
        create( 0x05E7, "MIN #104" );
    static final IonHandle MIN_104_MIN_NVR = 
        create( 0x3FBE, "Min (NVR)", MIN_104 );
    static final IonHandle MIN_104_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_104 );
    static final IonHandle  MIN_105 = 
        create( 0x05E8, "MIN #105" );
    static final IonHandle MIN_105_MIN_NVR = 
        create( 0x3FBF, "Min (NVR)", MIN_105 );
    static final IonHandle MIN_105_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_105 );
    static final IonHandle  MIN_106 = 
        create( 0x05E9, "MIN #106" );
    static final IonHandle MIN_106_MIN_NVR = 
        create( 0x3FC0, "Min (NVR)", MIN_106 );
    static final IonHandle MIN_106_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_106 );
    static final IonHandle  MIN_107 = 
        create( 0x05EA, "MIN #107" );
    static final IonHandle MIN_107_MIN_NVR = 
        create( 0x3FC1, "Min (NVR)", MIN_107 );
    static final IonHandle MIN_107_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_107 );
    static final IonHandle  MIN_108 = 
        create( 0x05EB, "MIN #108" );
    static final IonHandle MIN_108_MIN_NVR = 
        create( 0x3FC2, "Min (NVR)", MIN_108 );
    static final IonHandle MIN_108_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_108 );
    static final IonHandle  MIN_109 = 
        create( 0x05EC, "MIN #109" );
    static final IonHandle MIN_109_MIN_NVR = 
        create( 0x3FC3, "Min (NVR)", MIN_109 );
    static final IonHandle MIN_109_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_109 );
    static final IonHandle  MIN_110 = 
        create( 0x05ED, "MIN #110" );
    static final IonHandle MIN_110_MIN_NVR = 
        create( 0x3FC4, "Min (NVR)", MIN_110 );
    static final IonHandle MIN_110_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", MIN_110 );

    public static final IonHandle  PM = 
        create( 0x100, "1S PM" );
    public static final IonHandle PM_VA_NVR = 
        create( 0x5800, "Va (NVR)", PM );
    public static final IonHandle PM_VB_NVR = 
        create( 0x5801, "Vb (NVR)", PM );
    public static final IonHandle PM_VC_NVR = 
        create( 0x5802, "Vc (NVR)", PM );
    public static final IonHandle PM_VLNAVE_NVR = 
        create( 0x5803, "Vlnave (NVR)", PM );
    public static final IonHandle PM_VAB_NVR = 
        create( 0x5804, "Vab (NVR)", PM );
    public static final IonHandle PM_VBC_NVR = 
        create( 0x5805, "Vbc (NVR)", PM );
    public static final IonHandle PM_VCA_NVR = 
        create( 0x5806, "Vca (NVR)", PM );
    public static final IonHandle PM_VLLAVE_NVR = 
        create( 0x5807, "Vllave (NVR)", PM );
    public static final IonHandle PM_IA_NVR = 
        create( 0x5808, "Ia (NVR)", PM );
    public static final IonHandle PM_IB_NVR = 
        create( 0x5809, "Ib (NVR)", PM );
    public static final IonHandle PM_IC_NVR = 
        create( 0x580A, "Ic (NVR)", PM );
    public static final IonHandle PM_IAVE_NVR = 
        create( 0x580B, "Iave (NVR)", PM );
    public static final IonHandle PM_KWA_NVR = 
        create( 0x580C, "KWa (NVR)", PM );
    public static final IonHandle PM_KWB_NVR = 
        create( 0x580D, "KWb (NVR)", PM );
    public static final IonHandle PM_KWC_NVR = 
        create( 0x580E, "KWc (NVR)", PM );
    public static final IonHandle PM_KW_TOTAL_NVR = 
        create( 0x580F, "KW total (NVR)", PM );
    public static final IonHandle PM_KVARA_NVR = 
        create( 0x5810, "KVARa (NVR)", PM );
    public static final IonHandle PM_KVARB_NVR = 
        create( 0x5811, "KVARb (NVR)", PM );
    public static final IonHandle  HS_PM = 
        create( 0x0102, "HS PM" );
    public static final IonHandle HS_PM_VA_NVR = 
        create( 0x5828, "Va (NVR)", HS_PM );
    public static final IonHandle HS_PM_VB_NVR = 
        create( 0x5829, "Vb (NVR)", HS_PM );
    public static final IonHandle HS_PM_VC_NVR = 
        create( 0x582A, "Vc (NVR)", HS_PM );
    public static final IonHandle HS_PM_VLNAVE_NVR = 
        create( 0x582B, "Vlnave (NVR)", HS_PM );
    public static final IonHandle HS_PM_VAB_NVR = 
        create( 0x582C, "Vab (NVR)", HS_PM );
    public static final IonHandle HS_PM_VBC_NVR = 
        create( 0x582D, "Vbc (NVR)", HS_PM );
    public static final IonHandle HS_PM_VCA_NVR = 
        create( 0x582E, "Vca (NVR)", HS_PM );
    public static final IonHandle HS_PM_VLLAVE_NVR = 
        create( 0x582F, "Vllave (NVR)", HS_PM );
    public static final IonHandle HS_PM_IA_NVR = 
        create( 0x5830, "Ia (NVR)", HS_PM );
    public static final IonHandle HS_PM_IB_NVR = 
        create( 0x5831, "Ib (NVR)", HS_PM );
    public static final IonHandle HS_PM_IC_NVR = 
        create( 0x5832, "Ic (NVR)", HS_PM );
    public static final IonHandle HS_PM_IAVE_NVR = 
        create( 0x5833, "Iave (NVR)", HS_PM );
    public static final IonHandle HS_PM_KWA_NVR = 
        create( 0x5834, "KWa (NVR)", HS_PM );
    public static final IonHandle HS_PM_KWB_NVR = 
        create( 0x5835, "KWb (NVR)", HS_PM );
    public static final IonHandle HS_PM_KWC_NVR = 
        create( 0x5836, "KWc (NVR)", HS_PM );
    public static final IonHandle HS_PM_KW_TOTAL_NVR = 
        create( 0x5837, "KW total (NVR)", HS_PM );
    public static final IonHandle HS_PM_KVARA_NVR = 
        create( 0x5838, "KVARa (NVR)", HS_PM );
    public static final IonHandle HS_PM_KVARB_NVR = 
        create( 0x5839, "KVARb (NVR)", HS_PM );
    public static final IonHandle  MV_PM = 
        create( 0x0101, "MV PM" );
    public static final IonHandle MV_PM_VA_NVR = 
        create( 0x5B23, "Va (NVR)", MV_PM );
    public static final IonHandle MV_PM_VB_NVR = 
        create( 0x5B24, "Vb (NVR)", MV_PM );
    public static final IonHandle MV_PM_VC_NVR = 
        create( 0x5B25, "Vc (NVR)", MV_PM );
    public static final IonHandle MV_PM_VLNAVE_NVR = 
        create( 0x5B26, "Vlnave (NVR)", MV_PM );
    public static final IonHandle MV_PM_VAB_NVR = 
        create( 0x5B27, "Vab (NVR)", MV_PM );
    public static final IonHandle MV_PM_VBC_NVR = 
        create( 0x5B28, "Vbc (NVR)", MV_PM );
    public static final IonHandle MV_PM_VCA_NVR = 
        create( 0x5B29, "Vca (NVR)", MV_PM );
    public static final IonHandle MV_PM_VLLAVE_NVR = 
        create( 0x5B2A, "Vllave (NVR)", MV_PM );
    public static final IonHandle MV_PM_IA_NVR = 
        create( 0x5B2B, "Ia (NVR)", MV_PM );
    public static final IonHandle MV_PM_IB_NVR = 
        create( 0x5B2C, "Ib (NVR)", MV_PM );
    public static final IonHandle MV_PM_IC_NVR = 
        create( 0x5B2D, "Ic (NVR)", MV_PM );
    public static final IonHandle MV_PM_IAVE_NVR = 
        create( 0x5B2E, "Iave (NVR)", MV_PM );
    public static final IonHandle MV_PM_KWA_NVR = 
        create( 0x5B2F, "KWa (NVR)", MV_PM );
    public static final IonHandle MV_PM_KWB_NVR = 
        create( 0x5B30, "KWb (NVR)", MV_PM );
    public static final IonHandle MV_PM_KWC_NVR = 
        create( 0x5B31, "KWc (NVR)", MV_PM );
    public static final IonHandle MV_PM_KW_TOTAL_NVR = 
        create( 0x5B32, "KW total (NVR)", MV_PM );
    public static final IonHandle MV_PM_KVARA_NVR = 
        create( 0x5B33, "KVARa (NVR)", MV_PM );
    public static final IonHandle MV_PM_KVARB_NVR = 
        create( 0x5B34, "KVARb (NVR)", MV_PM );
    
    public static final IonHandle  FAC_1 = 
        create( 0x3, "FAC #1" );
    public static final IonHandle FAC_1_DEVICE_TYPE_SR = 
        create( 0x1300, "Device Type (SR)", FAC_1 );
    public static final IonHandle FAC_1_COMPLIANCE_SR = 
        create( 0x1301, "Compliance (SR)", FAC_1 );
    public static final IonHandle FAC_1_OPTIONS_SR = 
        create( 0x1302, "Options (SR)", FAC_1 );
    public static final IonHandle FAC_1_REVISION_SR = 
        create( 0x1303, "Revision (SR)", FAC_1 );
    public static final IonHandle FAC_1_SERIAL_NUMBER_SR = 
        create( 0x1304, "Serial Number (SR)", FAC_1 );
    public static final IonHandle FAC_1_ION_VERSION_NBR = 
        create( 0x772E, "ION Version (NBR)", FAC_1 );
    public static final IonHandle FAC_1_TEMPLATE_SR = 
        create( 0x137D, "Template (SR)", FAC_1 );
    public static final IonHandle FAC_1_FACTORY_DEFAULT_TEMPLATE_SR = 
        create( 0x137E, "Factory Default Template (SR)", FAC_1 );
    public static final IonHandle FAC_1_OWNER_SR = 
        create( 0x1345, "Owner (SR)", FAC_1 );
    public static final IonHandle FAC_1_TAG1_SR = 
        create( 0x1346, "Tag1 (SR)", FAC_1 );
    public static final IonHandle FAC_1_TAG2_SR = 
        create( 0x1347, "Tag2 (SR)", FAC_1 );
    public static final IonHandle FAC_1_V_NOMINAL_NBR = 
        create( 0x71EB, "V Nominal (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I_NOMINAL_NBR = 
        create( 0x71EC, "I Nominal (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I4_NOMINAL_NBR = 
        create( 0x71ED, "I4 Nominal (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I20_NOMINAL_NBR = 
        create( 0x71EE, "I20 Nominal (NBR)", FAC_1 );
    public static final IonHandle FAC_1_V1_CAL_NBR = 
        create( 0x71EF, "V1 cal (NBR)", FAC_1 );
    public static final IonHandle FAC_1_V2_CAL_NBR = 
        create( 0x71F0, "V2 cal (NBR)", FAC_1 );
    public static final IonHandle FAC_1_V3_CAL_NBR = 
        create( 0x71F1, "V3 cal (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I1_CAL_NBR = 
        create( 0x71F2, "I1 cal (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I4_OFF_NBR = 
        create( 0x7205, "I4 off (NBR)", FAC_1 );
    public static final IonHandle FAC_1_V_FORCE_NBR = 
        create( 0x7206, "V force (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I_FORCE_NBR = 
        create( 0x7207, "I force (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I4_FORCE_NBR = 
        create( 0x7208, "I4 force (NBR)", FAC_1 );
    public static final IonHandle FAC_1_VX_FORCE_NBR = 
        create( 0x7209, "Vx force (NBR)", FAC_1 );
    public static final IonHandle FAC_1_VX1_CAL_NBR = 
        create( 0x720A, "Vx1 cal (NBR)", FAC_1 );
    public static final IonHandle FAC_1_VX2_CAL_NBR = 
        create( 0x720B, "Vx2 cal (NBR)", FAC_1 );
    public static final IonHandle FAC_1_VX3_CAL_NBR = 
        create( 0x720C, "Vx3 cal (NBR)", FAC_1 );
    public static final IonHandle FAC_1_VX4_CAL_NBR = 
        create( 0x720D, "Vx4 cal (NBR)", FAC_1 );
    public static final IonHandle FAC_1_VX1_DC_NBR = 
        create( 0x720E, "Vx1 dc (NBR)", FAC_1 );
    public static final IonHandle FAC_1_VX2_DC_NBR = 
        create( 0x720F, "Vx2 dc (NBR)", FAC_1 );
    public static final IonHandle FAC_1_VX3_DC_NBR = 
        create( 0x7210, "Vx3 dc (NBR)", FAC_1 );
    public static final IonHandle FAC_1_VX4_DC_NBR = 
        create( 0x7211, "Vx4 dc (NBR)", FAC_1 );
    public static final IonHandle FAC_1_NOM_FREQ_ENR = 
        create( 0x798B, "Nom Freq (ENR)", FAC_1 );
    public static final IonHandle FAC_1__V1NCAL_NBR = 
        create( 0x75AF, " V1NCAL (NBR)", FAC_1 );
    public static final IonHandle FAC_1__V2NCAL_NBR = 
        create( 0x75B0, " V2NCAL (NBR)", FAC_1 );
    public static final IonHandle FAC_1_V3NCAL_NBR = 
        create( 0x75B1, "V3NCAL (NBR)", FAC_1 );
    public static final IonHandle FAC_1_V4NCAL_NBR = 
        create( 0x75B2, "V4NCAL (NBR)", FAC_1 );
    public static final IonHandle FAC_1_V1OCAL__NBR = 
        create( 0x75B3, "V1OCAL  (NBR)", FAC_1 );
    public static final IonHandle FAC_1_V2OCAL__NBR = 
        create( 0x75B4, "V2OCAL  (NBR)", FAC_1 );
    public static final IonHandle FAC_1_V3OCAL_NBR = 
        create( 0x75B5, "V3OCAL (NBR)", FAC_1 );
    public static final IonHandle FAC_1_V4OCAL_NBR = 
        create( 0x75B6, "V4OCAL (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I1KCAL__NBR = 
        create( 0x75B7, "I1KCAL  (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I2KCAL__NBR = 
        create( 0x75B8, "I2KCAL  (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I3KCAL__NBR = 
        create( 0x75B9, "I3KCAL  (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I4KCAL__NBR = 
        create( 0x75BA, "I4KCAL  (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I5KCAL_NBR = 
        create( 0x75BB, "I5KCAL (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I1UCAL_NBR = 
        create( 0x75BC, "I1UCAL (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I2UCAL_NBR = 
        create( 0x75BD, "I2UCAL (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I3UCAL__NBR = 
        create( 0x75BE, "I3UCAL  (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I4UCAL__NBR = 
        create( 0x75BF, "I4UCAL  (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I5UCAL_NBR = 
        create( 0x75C0, "I5UCAL (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I1NCAL__NBR = 
        create( 0x75C1, "I1NCAL  (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I2NCAL_NBR = 
        create( 0x75C2, "I2NCAL (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I3NCAL__NBR = 
        create( 0x75C3, "I3NCAL  (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I4NCAL__NBR = 
        create( 0x75C4, "I4NCAL  (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I5NCAL_NBR = 
        create( 0x75C5, "I5NCAL (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I1OCAL__NBR = 
        create( 0x75C6, "I1OCAL  (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I2OCAL__NBR = 
        create( 0x75C7, "I2OCAL  (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I3OCALNBR = 
        create( 0x75C8, "I3OCAL(NBR)", FAC_1 );
    public static final IonHandle FAC_1_I4OCAL_NBR = 
        create( 0x75C9, "I4OCAL (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I5OCAL_NBR = 
        create( 0x75CA, "I5OCAL (NBR)", FAC_1 );
    public static final IonHandle FAC_1_V4FORCE_NBR = 
        create( 0x75CB, "V4FORCE (NBR)", FAC_1 );
    public static final IonHandle FAC_1_I5FORCE_NBR = 
        create( 0x75CC, "I5FORCE (NBR)", FAC_1 );
    public static final IonHandle FAC_1_NOMFREQNUM_NBR = 
        create( 0x426F, "NomFreqNum (NBR)", FAC_1 );
    public static final IonHandle FAC_1_DEVICE_NAMESPACE_SR = 
        create( 0x5743, "Device Namespace (SR)", FAC_1 );
    public static final IonHandle FAC_1_DEVICE_NAME_SR = 
        create( 0x5744, "Device Name (SR)", FAC_1 );

    public static final IonHandle  PRT_1 = 
        create( 0x980, "PRT #1" );
    public static final IonHandle PRT_1_TRIGGER_DR = 
        create( 0x688E, "Trigger (DR)", PRT_1 );
    public static final IonHandle PRT_1_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_1 );
    public static final IonHandle PRT_1_PERIOD_NBR = 
        create( 0x71BC, "Period (NBR)", PRT_1 );
    public static final IonHandle PRT_1_SYNC_MODE_ENR = 
        create( 0x7958, "Sync Mode (ENR)", PRT_1 );
    public static final IonHandle  PRT_2 = 
        create( 0x0981, "PRT #2" );
    public static final IonHandle PRT_2_TRIGGER_DR = 
        create( 0x688F, "Trigger (DR)", PRT_2 );
    public static final IonHandle PRT_2_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_2 );
    public static final IonHandle PRT_2_PERIOD_NBR = 
        create( 0x71BD, "Period (NBR)", PRT_2 );
    public static final IonHandle PRT_2_SYNC_MODE_ENR = 
        create( 0x7959, "Sync Mode (ENR)", PRT_2 );
    public static final IonHandle  PRT_3 = 
        create( 0x0982, "PRT #3" );
    public static final IonHandle PRT_3_TRIGGER_DR = 
        create( 0x6890, "Trigger (DR)", PRT_3 );
    public static final IonHandle PRT_3_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_3 );
    public static final IonHandle PRT_3_PERIOD_NBR = 
        create( 0x71BE, "Period (NBR)", PRT_3 );
    public static final IonHandle PRT_3_SYNC_MODE_ENR = 
        create( 0x795A, "Sync Mode (ENR)", PRT_3 );
    public static final IonHandle  PRT_4 = 
        create( 0x0983, "PRT #4" );
    public static final IonHandle PRT_4_TRIGGER_DR = 
        create( 0x6891, "Trigger (DR)", PRT_4 );
    public static final IonHandle PRT_4_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_4 );
    public static final IonHandle PRT_4_PERIOD_NBR = 
        create( 0x71BF, "Period (NBR)", PRT_4 );
    public static final IonHandle PRT_4_SYNC_MODE_ENR = 
        create( 0x795B, "Sync Mode (ENR)", PRT_4 );
    public static final IonHandle  PRT_5 = 
        create( 0x0984, "PRT #5" );
    public static final IonHandle PRT_5_TRIGGER_DR = 
        create( 0x6892, "Trigger (DR)", PRT_5 );
    public static final IonHandle PRT_5_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_5 );
    public static final IonHandle PRT_5_PERIOD_NBR = 
        create( 0x71C0, "Period (NBR)", PRT_5 );
    public static final IonHandle PRT_5_SYNC_MODE_ENR = 
        create( 0x795C, "Sync Mode (ENR)", PRT_5 );
    public static final IonHandle  PRT_6 = 
        create( 0x0985, "PRT #6" );
    public static final IonHandle PRT_6_TRIGGER_DR = 
        create( 0x6893, "Trigger (DR)", PRT_6 );
    public static final IonHandle PRT_6_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_6 );
    public static final IonHandle PRT_6_PERIOD_NBR = 
        create( 0x71C1, "Period (NBR)", PRT_6 );
    public static final IonHandle PRT_6_SYNC_MODE_ENR = 
        create( 0x795D, "Sync Mode (ENR)", PRT_6 );
    public static final IonHandle  PRT_7 = 
        create( 0x0986, "PRT #7" );
    public static final IonHandle PRT_7_TRIGGER_DR = 
        create( 0x6894, "Trigger (DR)", PRT_7 );
    public static final IonHandle PRT_7_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_7 );
    public static final IonHandle PRT_7_PERIOD_NBR = 
        create( 0x71C2, "Period (NBR)", PRT_7 );
    public static final IonHandle PRT_7_SYNC_MODE_ENR = 
        create( 0x795E, "Sync Mode (ENR)", PRT_7 );
    public static final IonHandle  PRT_8 = 
        create( 0x0987, "PRT #8" );
    public static final IonHandle PRT_8_TRIGGER_DR = 
        create( 0x6895, "Trigger (DR)", PRT_8 );
    public static final IonHandle PRT_8_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_8 );
    public static final IonHandle PRT_8_PERIOD_NBR = 
        create( 0x71C3, "Period (NBR)", PRT_8 );
    public static final IonHandle PRT_8_SYNC_MODE_ENR = 
        create( 0x795F, "Sync Mode (ENR)", PRT_8 );
    public static final IonHandle  PRT_9 = 
        create( 0x0988, "PRT #9" );
    public static final IonHandle PRT_9_TRIGGER_DR = 
        create( 0x6896, "Trigger (DR)", PRT_9 );
    public static final IonHandle PRT_9_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_9 );
    public static final IonHandle PRT_9_PERIOD_NBR = 
        create( 0x71C4, "Period (NBR)", PRT_9 );
    public static final IonHandle PRT_9_SYNC_MODE_ENR = 
        create( 0x7960, "Sync Mode (ENR)", PRT_9 );
    public static final IonHandle  PRT_10 = 
        create( 0x0989, "PRT #10" );
    public static final IonHandle PRT_10_TRIGGER_DR = 
        create( 0x6897, "Trigger (DR)", PRT_10 );
    public static final IonHandle PRT_10_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_10 );
    public static final IonHandle PRT_10_PERIOD_NBR = 
        create( 0x71C5, "Period (NBR)", PRT_10 );
    public static final IonHandle PRT_10_SYNC_MODE_ENR = 
        create( 0x7961, "Sync Mode (ENR)", PRT_10 );
    public static final IonHandle  PRT_11 = 
        create( 0x098A, "PRT #11" );
    public static final IonHandle PRT_11_TRIGGER_DR = 
        create( 0x6898, "Trigger (DR)", PRT_11 );
    public static final IonHandle PRT_11_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_11 );
    public static final IonHandle PRT_11_PERIOD_NBR = 
        create( 0x71C6, "Period (NBR)", PRT_11 );
    public static final IonHandle PRT_11_SYNC_MODE_ENR = 
        create( 0x7962, "Sync Mode (ENR)", PRT_11 );
    public static final IonHandle  PRT_12 = 
        create( 0x098B, "PRT #12" );
    public static final IonHandle PRT_12_TRIGGER_DR = 
        create( 0x6899, "Trigger (DR)", PRT_12 );
    public static final IonHandle PRT_12_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_12 );
    public static final IonHandle PRT_12_PERIOD_NBR = 
        create( 0x71C7, "Period (NBR)", PRT_12 );
    public static final IonHandle PRT_12_SYNC_MODE_ENR = 
        create( 0x7963, "Sync Mode (ENR)", PRT_12 );
    public static final IonHandle  PRT_13 = 
        create( 0x098C, "PRT #13" );
    public static final IonHandle PRT_13_TRIGGER_DR = 
        create( 0x689A, "Trigger (DR)", PRT_13 );
    public static final IonHandle PRT_13_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_13 );
    public static final IonHandle PRT_13_PERIOD_NBR = 
        create( 0x71C8, "Period (NBR)", PRT_13 );
    public static final IonHandle PRT_13_SYNC_MODE_ENR = 
        create( 0x7964, "Sync Mode (ENR)", PRT_13 );
    public static final IonHandle  PRT_14 = 
        create( 0x098D, "PRT #14" );
    public static final IonHandle PRT_14_TRIGGER_DR = 
        create( 0x689B, "Trigger (DR)", PRT_14 );
    public static final IonHandle PRT_14_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_14 );
    public static final IonHandle PRT_14_PERIOD_NBR = 
        create( 0x71C9, "Period (NBR)", PRT_14 );
    public static final IonHandle PRT_14_SYNC_MODE_ENR = 
        create( 0x7965, "Sync Mode (ENR)", PRT_14 );
    public static final IonHandle  PRT_15 = 
        create( 0x098E, "PRT #15" );
    public static final IonHandle PRT_15_TRIGGER_DR = 
        create( 0x689C, "Trigger (DR)", PRT_15 );
    public static final IonHandle PRT_15_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_15 );
    public static final IonHandle PRT_15_PERIOD_NBR = 
        create( 0x71CA, "Period (NBR)", PRT_15 );
    public static final IonHandle PRT_15_SYNC_MODE_ENR = 
        create( 0x7966, "Sync Mode (ENR)", PRT_15 );
    public static final IonHandle  PRT_16 = 
        create( 0x098F, "PRT #16" );
    public static final IonHandle PRT_16_TRIGGER_DR = 
        create( 0x689D, "Trigger (DR)", PRT_16 );
    public static final IonHandle PRT_16_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_16 );
    public static final IonHandle PRT_16_PERIOD_NBR = 
        create( 0x71CB, "Period (NBR)", PRT_16 );
    public static final IonHandle PRT_16_SYNC_MODE_ENR = 
        create( 0x7967, "Sync Mode (ENR)", PRT_16 );
    public static final IonHandle  PRT_17 = 
        create( 0x0990, "PRT #17" );
    public static final IonHandle PRT_17_TRIGGER_DR = 
        create( 0x689E, "Trigger (DR)", PRT_17 );
    public static final IonHandle PRT_17_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_17 );
    public static final IonHandle PRT_17_PERIOD_NBR = 
        create( 0x71CC, "Period (NBR)", PRT_17 );
    public static final IonHandle PRT_17_SYNC_MODE_ENR = 
        create( 0x7968, "Sync Mode (ENR)", PRT_17 );
    public static final IonHandle  PRT_18 = 
        create( 0x0991, "PRT #18" );
    public static final IonHandle PRT_18_TRIGGER_DR = 
        create( 0x689F, "Trigger (DR)", PRT_18 );
    public static final IonHandle PRT_18_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_18 );
    public static final IonHandle PRT_18_PERIOD_NBR = 
        create( 0x71CD, "Period (NBR)", PRT_18 );
    public static final IonHandle PRT_18_SYNC_MODE_ENR = 
        create( 0x7969, "Sync Mode (ENR)", PRT_18 );
    public static final IonHandle  PRT_19 = 
        create( 0x0992, "PRT #19" );
    public static final IonHandle PRT_19_TRIGGER_DR = 
        create( 0x68A0, "Trigger (DR)", PRT_19 );
    public static final IonHandle PRT_19_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_19 );
    public static final IonHandle PRT_19_PERIOD_NBR = 
        create( 0x71CE, "Period (NBR)", PRT_19 );
    public static final IonHandle PRT_19_SYNC_MODE_ENR = 
        create( 0x796A, "Sync Mode (ENR)", PRT_19 );
    public static final IonHandle  PRT_20 = 
        create( 0x0993, "PRT #20" );
    public static final IonHandle PRT_20_TRIGGER_DR = 
        create( 0x68A1, "Trigger (DR)", PRT_20 );
    public static final IonHandle PRT_20_EVENT_EVR = 
        create( 0x1100, "Event (EVR)", PRT_20 );
    public static final IonHandle PRT_20_PERIOD_NBR = 
        create( 0x71CF, "Period (NBR)", PRT_20 );
    public static final IonHandle PRT_20_SYNC_MODE_ENR = 
        create( 0x796B, "Sync Mode (ENR)", PRT_20 );

    public static final IonHandle  ELC_1 = 
        create( 0x0B80, "ELC #1" );
    public static final IonHandle ELC_1_EVENT_LOG_ELR = 
        create( 0x1000, "Event Log (ELR)", ELC_1 );
    public static final IonHandle ELC_1_EVENT_LOG_DEPTH_NBR = 
        create( 0x71E6, "Event Log Depth (NBR)", ELC_1 );
    public static final IonHandle ELC_1_ALARM_PRIORITY_NBR = 
        create( 0x71E7, "Alarm Priority (NBR)", ELC_1 );
    public static final IonHandle ELC_1_CUTOFF_NBR = 
        create( 0x7305, "Cutoff (NBR)", ELC_1 );
    
    private static Collection max = Arrays.asList( 
        new IonHandle [] {
        MAX_1_MAX_NVR, MAX_2_MAX_NVR, MAX_3_MAX_NVR, MAX_4_MAX_NVR, 
        MAX_5_MAX_NVR, MAX_6_MAX_NVR, MAX_7_MAX_NVR, MAX_8_MAX_NVR, 
        MAX_9_MAX_NVR, MAX_10_MAX_NVR, MAX_11_MAX_NVR, MAX_12_MAX_NVR, 
        MAX_13_MAX_NVR, MAX_14_MAX_NVR, MAX_15_MAX_NVR, MAX_16_MAX_NVR, 
        MAX_17_MAX_NVR, MAX_18_MAX_NVR, MAX_19_MAX_NVR, MAX_20_MAX_NVR, 
        MAX_21_MAX_NVR, MAX_22_MAX_NVR, MAX_23_MAX_NVR, MAX_24_MAX_NVR, 
        MAX_25_MAX_NVR, MAX_26_MAX_NVR, MAX_27_MAX_NVR, MAX_28_MAX_NVR, 
        MAX_29_MAX_NVR, MAX_30_MAX_NVR, MAX_31_MAX_NVR, MAX_32_MAX_NVR, 
        MAX_33_MAX_NVR, MAX_34_MAX_NVR, MAX_35_MAX_NVR, MAX_36_MAX_NVR, 
        MAX_37_MAX_NVR, MAX_38_MAX_NVR, MAX_39_MAX_NVR, MAX_40_MAX_NVR, 
        MAX_41_MAX_NVR, MAX_42_MAX_NVR, MAX_43_MAX_NVR, MAX_44_MAX_NVR, 
        MAX_45_MAX_NVR, MAX_46_MAX_NVR, MAX_47_MAX_NVR, MAX_48_MAX_NVR, 
        MAX_49_MAX_NVR, MAX_50_MAX_NVR, MAX_51_MAX_NVR, MAX_52_MAX_NVR, 
        MAX_53_MAX_NVR, MAX_54_MAX_NVR, MAX_55_MAX_NVR, MAX_56_MAX_NVR, 
        MAX_57_MAX_NVR, MAX_58_MAX_NVR, MAX_59_MAX_NVR, MAX_60_MAX_NVR, 
        MAX_61_MAX_NVR, MAX_62_MAX_NVR, MAX_63_MAX_NVR, MAX_64_MAX_NVR, 
        MAX_65_MAX_NVR, MAX_66_MAX_NVR, MAX_67_MAX_NVR, MAX_68_MAX_NVR, 
        MAX_69_MAX_NVR, MAX_70_MAX_NVR, MAX_71_MAX_NVR, MAX_72_MAX_NVR, 
        MAX_73_MAX_NVR, MAX_74_MAX_NVR, MAX_75_MAX_NVR, MAX_76_MAX_NVR, 
        MAX_77_MAX_NVR, MAX_78_MAX_NVR, MAX_79_MAX_NVR, MAX_80_MAX_NVR, 
        MAX_81_MAX_NVR, MAX_82_MAX_NVR, MAX_83_MAX_NVR, MAX_84_MAX_NVR, 
        MAX_85_MAX_NVR, MAX_86_MAX_NVR, MAX_87_MAX_NVR, MAX_88_MAX_NVR, 
        MAX_89_MAX_NVR, MAX_90_MAX_NVR, MAX_91_MAX_NVR, MAX_92_MAX_NVR, 
        MAX_93_MAX_NVR, MAX_94_MAX_NVR, MAX_95_MAX_NVR, MAX_96_MAX_NVR, 
        MAX_97_MAX_NVR, MAX_98_MAX_NVR, MAX_99_MAX_NVR, MAX_100_MAX_NVR, 
        MAX_101_MAX_NVR, MAX_102_MAX_NVR, MAX_103_MAX_NVR, MAX_104_MAX_NVR, 
        MAX_105_MAX_NVR, MAX_106_MAX_NVR, MAX_107_MAX_NVR, MAX_108_MAX_NVR, 
        MAX_109_MAX_NVR, MAX_110_MAX_NVR, MAX_111_MAX_NVR, MAX_112_MAX_NVR, 
        MAX_113_MAX_NVR, MAX_114_MAX_NVR, MAX_115_MAX_NVR, MAX_116_MAX_NVR, 
        MAX_117_MAX_NVR, MAX_118_MAX_NVR, MAX_119_MAX_NVR, MAX_120_MAX_NVR, 
        MAX_121_MAX_NVR, MAX_122_MAX_NVR, MAX_123_MAX_NVR, MAX_124_MAX_NVR, 
        MAX_125_MAX_NVR, MAX_126_MAX_NVR, MAX_127_MAX_NVR, MAX_128_MAX_NVR, 
        MAX_129_MAX_NVR, MAX_130_MAX_NVR, MAX_131_MAX_NVR, MAX_132_MAX_NVR, 
        MAX_133_MAX_NVR, MAX_134_MAX_NVR, MAX_135_MAX_NVR, MAX_136_MAX_NVR, 
        MAX_137_MAX_NVR, MAX_138_MAX_NVR, MAX_139_MAX_NVR, MAX_140_MAX_NVR, 
        MAX_141_MAX_NVR, MAX_142_MAX_NVR, MAX_143_MAX_NVR, MAX_144_MAX_NVR, 
        MAX_145_MAX_NVR, MAX_146_MAX_NVR, MAX_147_MAX_NVR, MAX_148_MAX_NVR, 
        MAX_149_MAX_NVR, MAX_150_MAX_NVR, MAX_151_MAX_NVR, MAX_152_MAX_NVR, 
        MAX_153_MAX_NVR, MAX_154_MAX_NVR, MAX_155_MAX_NVR, MAX_156_MAX_NVR, 
        MAX_157_MAX_NVR, MAX_158_MAX_NVR, MAX_159_MAX_NVR, MAX_160_MAX_NVR, 
        MAX_161_MAX_NVR, MAX_162_MAX_NVR, MAX_163_MAX_NVR, MAX_164_MAX_NVR, 
        MAX_165_MAX_NVR, MAX_166_MAX_NVR, MAX_167_MAX_NVR, MAX_168_MAX_NVR, 
        MAX_169_MAX_NVR, MAX_170_MAX_NVR, MAX_171_MAX_NVR, MAX_172_MAX_NVR, 
        MAX_173_MAX_NVR, MAX_174_MAX_NVR, MAX_175_MAX_NVR, MAX_176_MAX_NVR, 
        MAX_177_MAX_NVR, MAX_178_MAX_NVR, MAX_179_MAX_NVR, MAX_180_MAX_NVR, 
        MAX_181_MAX_NVR, MAX_182_MAX_NVR, MAX_183_MAX_NVR, MAX_184_MAX_NVR, 
        MAX_185_MAX_NVR, MAX_186_MAX_NVR, MAX_187_MAX_NVR, MAX_188_MAX_NVR, 
        MAX_189_MAX_NVR, MAX_190_MAX_NVR, MAX_191_MAX_NVR, MAX_192_MAX_NVR, 
        MAX_193_MAX_NVR, MAX_194_MAX_NVR, MAX_195_MAX_NVR, MAX_196_MAX_NVR, 
        MAX_197_MAX_NVR, MAX_198_MAX_NVR, MAX_199_MAX_NVR, MAX_200_MAX_NVR, 
        MAX_201_MAX_NVR, MAX_202_MAX_NVR, MAX_203_MAX_NVR, MAX_204_MAX_NVR, 
        MAX_205_MAX_NVR, MAX_206_MAX_NVR, MAX_207_MAX_NVR, MAX_208_MAX_NVR, 
        MAX_209_MAX_NVR, MAX_210_MAX_NVR, MAX_211_MAX_NVR, MAX_212_MAX_NVR, 
        MAX_213_MAX_NVR, MAX_214_MAX_NVR, MAX_215_MAX_NVR, MAX_216_MAX_NVR, 
        MAX_217_MAX_NVR, MAX_218_MAX_NVR, MAX_219_MAX_NVR, MAX_220_MAX_NVR, 
        MAX_221_MAX_NVR, MAX_222_MAX_NVR, MAX_223_MAX_NVR, MAX_224_MAX_NVR, 
        MAX_225_MAX_NVR, MAX_226_MAX_NVR, MAX_227_MAX_NVR, MAX_228_MAX_NVR, 
        MAX_229_MAX_NVR, MAX_230_MAX_NVR, MAX_231_MAX_NVR, MAX_232_MAX_NVR, 
        MAX_233_MAX_NVR, MAX_234_MAX_NVR, MAX_235_MAX_NVR, MAX_236_MAX_NVR, 
        MAX_237_MAX_NVR, MAX_238_MAX_NVR, MAX_239_MAX_NVR, MAX_240_MAX_NVR, 
        MAX_241_MAX_NVR, MAX_242_MAX_NVR, MAX_243_MAX_NVR, MAX_244_MAX_NVR, 
        MAX_245_MAX_NVR
        }   
    );

    private static Collection min = Arrays.asList( 
        new IonHandle [] {
        MIN_1_MIN_NVR, MIN_2_MIN_NVR, MIN_3_MIN_NVR, MIN_4_MIN_NVR, 
        MIN_5_MIN_NVR, MIN_6_MIN_NVR, MIN_7_MIN_NVR, MIN_8_MIN_NVR, 
        MIN_9_MIN_NVR, MIN_10_MIN_NVR, MIN_11_MIN_NVR, MIN_12_MIN_NVR, 
        MIN_13_MIN_NVR, MIN_14_MIN_NVR, MIN_15_MIN_NVR, MIN_16_MIN_NVR, 
        MIN_17_MIN_NVR, MIN_18_MIN_NVR, MIN_19_MIN_NVR, MIN_20_MIN_NVR, 
        MIN_21_MIN_NVR, MIN_22_MIN_NVR, MIN_23_MIN_NVR, MIN_24_MIN_NVR, 
        MIN_25_MIN_NVR, MIN_26_MIN_NVR, MIN_27_MIN_NVR, MIN_28_MIN_NVR, 
        MIN_29_MIN_NVR, MIN_30_MIN_NVR, MIN_31_MIN_NVR, MIN_32_MIN_NVR, 
        MIN_33_MIN_NVR, MIN_34_MIN_NVR, MIN_35_MIN_NVR, MIN_36_MIN_NVR, 
        MIN_37_MIN_NVR, MIN_38_MIN_NVR, MIN_39_MIN_NVR, MIN_40_MIN_NVR, 
        MIN_41_MIN_NVR, MIN_42_MIN_NVR, MIN_43_MIN_NVR, MIN_44_MIN_NVR, 
        MIN_45_MIN_NVR, MIN_46_MIN_NVR, MIN_47_MIN_NVR, MIN_48_MIN_NVR, 
        MIN_49_MIN_NVR, MIN_50_MIN_NVR, MIN_51_MIN_NVR, MIN_52_MIN_NVR, 
        MIN_53_MIN_NVR, MIN_54_MIN_NVR, MIN_55_MIN_NVR, MIN_56_MIN_NVR, 
        MIN_57_MIN_NVR, MIN_58_MIN_NVR, MIN_59_MIN_NVR, MIN_60_MIN_NVR, 
        MIN_61_MIN_NVR, MIN_62_MIN_NVR, MIN_63_MIN_NVR, MIN_64_MIN_NVR, 
        MIN_65_MIN_NVR, MIN_66_MIN_NVR, MIN_67_MIN_NVR, MIN_68_MIN_NVR, 
        MIN_69_MIN_NVR, MIN_70_MIN_NVR, MIN_71_MIN_NVR, MIN_72_MIN_NVR, 
        MIN_73_MIN_NVR, MIN_74_MIN_NVR, MIN_75_MIN_NVR, MIN_76_MIN_NVR, 
        MIN_77_MIN_NVR, MIN_78_MIN_NVR, MIN_79_MIN_NVR, MIN_80_MIN_NVR, 
        MIN_81_MIN_NVR, MIN_82_MIN_NVR, MIN_83_MIN_NVR, MIN_84_MIN_NVR, 
        MIN_85_MIN_NVR, MIN_86_MIN_NVR, MIN_87_MIN_NVR, MIN_88_MIN_NVR, 
        MIN_89_MIN_NVR, MIN_90_MIN_NVR, MIN_91_MIN_NVR, MIN_92_MIN_NVR, 
        MIN_93_MIN_NVR, MIN_94_MIN_NVR, MIN_95_MIN_NVR, MIN_96_MIN_NVR, 
        MIN_97_MIN_NVR, MIN_98_MIN_NVR, MIN_99_MIN_NVR, MIN_100_MIN_NVR, 
        MIN_101_MIN_NVR, MIN_102_MIN_NVR, MIN_103_MIN_NVR, MIN_104_MIN_NVR, 
        MIN_105_MIN_NVR, MIN_106_MIN_NVR, MIN_107_MIN_NVR, MIN_108_MIN_NVR, 
        MIN_109_MIN_NVR, MIN_110_MIN_NVR
        }
    );
    
    private static Collection integrator = Arrays.asList( 
        new IonHandle [] {
        INT_1_RESULT_NVR, INT_2_RESULT_NVR, INT_3_RESULT_NVR, INT_4_RESULT_NVR, 
        INT_5_RESULT_NVR, INT_6_RESULT_NVR, INT_7_RESULT_NVR, INT_8_RESULT_NVR, 
        INT_9_RESULT_NVR, INT_10_RESULT_NVR, INT_11_RESULT_NVR, INT_12_RESULT_NVR, 
        INT_13_RESULT_NVR, INT_14_RESULT_NVR, INT_15_RESULT_NVR, INT_16_RESULT_NVR, 
        INT_17_RESULT_NVR, INT_18_RESULT_NVR, INT_19_RESULT_NVR, INT_20_RESULT_NVR, 
        INT_21_RESULT_NVR, INT_22_RESULT_NVR, INT_23_RESULT_NVR, INT_24_RESULT_NVR, 
        INT_25_RESULT_NVR, INT_26_RESULT_NVR, INT_27_RESULT_NVR, INT_28_RESULT_NVR, 
        INT_29_RESULT_NVR, INT_30_RESULT_NVR, INT_31_RESULT_NVR, INT_32_RESULT_NVR, 
        INT_33_RESULT_NVR, INT_34_RESULT_NVR, INT_35_RESULT_NVR, INT_36_RESULT_NVR, 
        INT_37_RESULT_NVR, INT_38_RESULT_NVR, INT_39_RESULT_NVR, INT_40_RESULT_NVR, 
        INT_41_RESULT_NVR, INT_42_RESULT_NVR, INT_43_RESULT_NVR, INT_44_RESULT_NVR, 
        INT_45_RESULT_NVR, INT_46_RESULT_NVR, INT_47_RESULT_NVR, INT_48_RESULT_NVR, 
        INT_49_RESULT_NVR, INT_50_RESULT_NVR, INT_51_RESULT_NVR, INT_52_RESULT_NVR, 
        INT_53_RESULT_NVR, INT_54_RESULT_NVR, INT_55_RESULT_NVR, INT_56_RESULT_NVR, 
        INT_57_RESULT_NVR, INT_58_RESULT_NVR, INT_59_RESULT_NVR, INT_60_RESULT_NVR, 
        INT_61_RESULT_NVR, INT_62_RESULT_NVR, INT_63_RESULT_NVR, INT_64_RESULT_NVR, 
        INT_65_RESULT_NVR, INT_66_RESULT_NVR, INT_67_RESULT_NVR, INT_68_RESULT_NVR, 
        INT_69_RESULT_NVR, INT_70_RESULT_NVR, INT_71_RESULT_NVR, INT_72_RESULT_NVR, 
        INT_73_RESULT_NVR, INT_74_RESULT_NVR, INT_75_RESULT_NVR, INT_76_RESULT_NVR, 
        INT_77_RESULT_NVR, INT_78_RESULT_NVR, INT_79_RESULT_NVR, INT_80_RESULT_NVR, 
        INT_81_RESULT_NVR, INT_82_RESULT_NVR, INT_83_RESULT_NVR, INT_84_RESULT_NVR, 
        INT_85_RESULT_NVR, INT_86_RESULT_NVR, INT_87_RESULT_NVR, INT_88_RESULT_NVR, 
        INT_89_RESULT_NVR, INT_90_RESULT_NVR, INT_91_RESULT_NVR, INT_92_RESULT_NVR, 
        INT_93_RESULT_NVR, INT_94_RESULT_NVR, INT_95_RESULT_NVR, INT_96_RESULT_NVR, 
        INT_97_RESULT_NVR, INT_98_RESULT_NVR, INT_99_RESULT_NVR, INT_100_RESULT_NVR, 
        INT_101_RESULT_NVR, INT_102_RESULT_NVR, INT_103_RESULT_NVR, INT_104_RESULT_NVR, 
        INT_105_RESULT_NVR, INT_106_RESULT_NVR, INT_107_RESULT_NVR, INT_108_RESULT_NVR, 
        INT_109_RESULT_NVR, INT_110_RESULT_NVR
        }
        );

}
