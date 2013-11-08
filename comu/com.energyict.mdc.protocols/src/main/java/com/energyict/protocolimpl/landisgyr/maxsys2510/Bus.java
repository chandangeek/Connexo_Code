package com.energyict.protocolimpl.landisgyr.maxsys2510;

import java.util.TreeMap;

class Bus {

    static TreeMap map;
    
    static final Bus MTR_INPUT_BUS =  new Bus( 'I', "MTR_INPUT_BUS" ); 
    static final Bus TOTALIZATION_BUS = new Bus( 'T', "TOTALIZATION_BUS" ); 
    static final Bus KOMPENSATION_BUS = new Bus( 'K', "KOMPENSATION_BUS" );  
    static final Bus XFORM_BUS = new Bus( 'X', "XFORM_BUS" ); 
    static final Bus RATE_BUS = new Bus( 'R', "RATE_BUS" ); 
    static final Bus CONTROL_TOU_BUS = new Bus( 'C', "CONTROL_TOU_BUS" ); 
    static final Bus LOAD_CNTRL_BUS = new Bus( 'L', "LOAD_CNTRL_BUS" ); 
    static final Bus DISPLAY_BUS  = new Bus( 'D', "DISPLAY_BUS" ); 
    static final Bus MISC_FUNC_BUS = new Bus( 'M', "MISC_FUNC_BUS" ); 
    static final Bus SUMM_CH_BUS = new Bus( 'S', "SUMM_CH_BUS" );
    
    static Bus get( char id ) {
        if( map == null ){
            map = new TreeMap();
            map.put( new Character( 'I' ), MTR_INPUT_BUS ); 
            map.put( new Character( 'T' ), TOTALIZATION_BUS ); 
            map.put( new Character( 'K' ), KOMPENSATION_BUS );  
            map.put( new Character( 'X' ), XFORM_BUS ); 
            map.put( new Character( 'R' ), RATE_BUS ); 
            map.put( new Character( 'C' ), CONTROL_TOU_BUS ); 
            map.put( new Character( 'L' ), LOAD_CNTRL_BUS ); 
            map.put( new Character( 'D' ), DISPLAY_BUS ); 
            map.put( new Character( 'M' ), MISC_FUNC_BUS ); 
            map.put( new Character( 'S' ), SUMM_CH_BUS);
        }
        return (Bus)map.get( new Character( id ) ); 
    }
    
    char id;
    String description;
    
    Bus( char id, String description ) {
        this.id = id;
        this.description = description;
    }

    char getId() {
        return id;
    }
    
    String getDescription() {
        return description;
    }

    public String toString() {
        return "Bus[ " + id + " " + description + "]";
    }
    
}
