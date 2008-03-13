package com.energyict.protocolimpl.iec1107.cewe.prometer;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

/** Registers with dimensions (=units) are stored in this format: 121.5*kW */

class UnitParser {
    
    static Unit parse(String id){

        if( "Wh".equals( id) ) 
            return Unit.get(BaseUnit.WATTHOUR);
        
        if( "kWh".equals( id) ) 
            return Unit.get(BaseUnit.WATTHOUR, 3);

        if( "MWh".equals( id) ) 
            return Unit.get(BaseUnit.WATTHOUR, 6);
                
        if( "W".equals( id) )  
            return Unit.get(BaseUnit.WATT);
        
        if( "kW".equals( id) )  
            return Unit.get(BaseUnit.WATT, 3);
        
        if ( "MW".equals( id) )
        	return Unit.get(BaseUnit.WATT, 6);
        
        if( "varh".equals(id)) 
            return Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR);
        
        if( "kvarh".equals(id)) 
            return Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 3 );
        
        if( "Mvarh".equals(id)) 
            return Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 6 );
        
        if( "var".equals(id))  
            return Unit.get(BaseUnit.VOLTAMPEREREACTIVE);
        
        if( "kvar".equals(id))  
            return Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 3 );

        if( "Mvar".equals(id))  
            return Unit.get(BaseUnit.VOLTAMPEREREACTIVE, 6 );
        
        if( "VA".equals(id))   
            return Unit.get(BaseUnit.VOLTAMPERE);
        
        if( "kVA".equals(id))   
            return Unit.get(BaseUnit.VOLTAMPERE, 3 );
        
        if( "MVA".equals(id))   
            return Unit.get(BaseUnit.VOLTAMPERE, 6 );

        if( "VAh".equals(id))  
            return Unit.get(BaseUnit.VOLTAMPEREHOUR);

        if( "kVAh".equals(id))  
            return Unit.get(BaseUnit.VOLTAMPEREHOUR, 3 );

        if( "MVAh".equals(id))  
            return Unit.get(BaseUnit.VOLTAMPEREHOUR, 6 );
        
        if( "m3".equals(id))    
            return Unit.get(BaseUnit.CUBICMETER, 0 );
        
        if( "m3/h".equals(id))  
            return Unit.get(BaseUnit.CUBICMETERPERHOUR, 0 );

        if( "min".equals(id))  
            return Unit.get(BaseUnit.MINUTE, 0 );
        
        throw new ApplicationException( "Unit " + id + " not supported " );
        
    }
    
}
