package com.energyict.protocolimpl.iec1107.cewe.prometer;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

/** Registers with dimensions (=units) are stored in this format: 121.5*kW */

class UnitParser {
    
    static Unit parse(String id){

    	if( "Wh".equalsIgnoreCase( id) ) 
            return Unit.get(BaseUnit.WATTHOUR);
        
        if( "kWh".equalsIgnoreCase( id) ) 
            return Unit.get(BaseUnit.WATTHOUR, 3);

        if( "MWh".equalsIgnoreCase( id) ) 
            return Unit.get(BaseUnit.WATTHOUR, 6);
                
        if( "W".equalsIgnoreCase( id) )  
            return Unit.get(BaseUnit.WATT);
        
        if( "kW".equalsIgnoreCase( id) )  
            return Unit.get(BaseUnit.WATT, 3);
        
        if ( "MW".equalsIgnoreCase( id) )
        	return Unit.get(BaseUnit.WATT, 6);
        
        if( "varh".equalsIgnoreCase(id)) 
            return Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR);
        
        if( "kvarh".equalsIgnoreCase(id)) 
            return Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 3 );
        
        if( "Mvarh".equalsIgnoreCase(id)) 
            return Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 6 );
        
        if( "var".equalsIgnoreCase(id))  
            return Unit.get(BaseUnit.VOLTAMPEREREACTIVE);
        
        if( "kvar".equalsIgnoreCase(id))  
            return Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3 );

        if( "Mvar".equalsIgnoreCase(id))  
            return Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 6 );
        
        if( "VA".equalsIgnoreCase(id))   
            return Unit.get(BaseUnit.VOLTAMPERE);
        
        if( "kVA".equalsIgnoreCase(id))   
            return Unit.get(BaseUnit.VOLTAMPERE, 3 );
        
        if( "MVA".equalsIgnoreCase(id))   
            return Unit.get(BaseUnit.VOLTAMPERE, 6 );

        if( "VAh".equalsIgnoreCase(id))  
            return Unit.get(BaseUnit.VOLTAMPEREHOUR);

        if( "kVAh".equalsIgnoreCase(id))  
            return Unit.get(BaseUnit.VOLTAMPEREHOUR, 3 );

        if( "MVAh".equalsIgnoreCase(id))  
            return Unit.get(BaseUnit.VOLTAMPEREHOUR, 6 );
        
        if( "m3".equalsIgnoreCase(id))    
            return Unit.get(BaseUnit.CUBICMETER, 0 );
        
        if( "m3/h".equalsIgnoreCase(id))  
            return Unit.get(BaseUnit.CUBICMETERPERHOUR, 0 );

        if( "min".equalsIgnoreCase(id))  
            return Unit.get(BaseUnit.MINUTE, 0 );
        
        throw new IllegalArgumentException( "Unit " + id + " not supported " );
        
    }
    
}
