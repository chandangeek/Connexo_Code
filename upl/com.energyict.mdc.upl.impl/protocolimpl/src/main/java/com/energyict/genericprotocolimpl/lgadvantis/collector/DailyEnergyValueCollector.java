package com.energyict.genericprotocolimpl.lgadvantis.collector;

import com.energyict.genericprotocolimpl.lgadvantis.*;
import com.energyict.obis.ObisCode;

public class DailyEnergyValueCollector implements Collector {
    
    public ReadResult getAll(Task task, RtuMessageLink messageLink) {
        
        ReadResult r = new ReadResult();
        
        ObisCode oc = ObisCode.fromString( "1.0.1.8.0.VZ" );
        oc = ObisCode.fromString( "1.0.1.8.1.VZ" );
        r.addAllRegisterValues( task.findRegisterValues( oc, true ) );
        oc = ObisCode.fromString( "1.0.1.8.2.VZ" );
        r.addAllRegisterValues( task.findRegisterValues( oc, true ) );
        oc = ObisCode.fromString( "1.0.1.8.3.VZ" );
        r.addAllRegisterValues( task.findRegisterValues( oc, true ) );
        oc = ObisCode.fromString( "1.0.1.8.4.VZ" );
        r.addAllRegisterValues( task.findRegisterValues( oc, true ) );
        oc = ObisCode.fromString( "1.0.1.8.5.VZ" );
        r.addAllRegisterValues( task.findRegisterValues( oc, true ) );
        oc = ObisCode.fromString( "1.0.1.8.6.VZ" );
        r.addAllRegisterValues( task.findRegisterValues( oc, true ) );

        return r;


    }
    
}
