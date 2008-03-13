package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import java.util.Map;
import java.util.HashMap;

public class MeterAvailabilityStatus {
    
    private static Map all = new HashMap( );
    
    public final static MeterAvailabilityStatus METER_AVAILABLE = 
        add( 0, "Meter Available" );
    
    public final static MeterAvailabilityStatus METER_DISAPPEARED = 
        add( 1, "Meter Disappeared" );
    
    public final static MeterAvailabilityStatus METER_LOST = 
        add( 2, "Meter Lost" );
        
    
    private int id;
    private String description;
    
    private static MeterAvailabilityStatus add(int id, String dscr) {
        MeterAvailabilityStatus s = new MeterAvailabilityStatus(id, dscr);
        all.put(""+id, s);
        return s;
    }
    
    private MeterAvailabilityStatus(int id, String description) {
        this.id = id;
        this.description = description;
    }
    
    public static MeterAvailabilityStatus get( int id ) {
        return (MeterAvailabilityStatus) all.get( "" + id );
    }
    
    public int getId( ){
        return id;
    }
    
    public String getDescription( ){
        return description;
    }
    
    public String toString( ){
        return "MeterAvailablityStatus [" + description + "]";
    }
    
    public static void main(String [] args) {
        
        System.out.println(MeterAvailabilityStatus.get(0) );
        
        
    }
    
}
