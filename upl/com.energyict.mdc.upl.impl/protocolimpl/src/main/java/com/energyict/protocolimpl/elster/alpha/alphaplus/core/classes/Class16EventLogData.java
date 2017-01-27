/*
 * Class16EventLogData.java
 *
 * Created on 25 juli 2005, 11:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.elster.alpha.core.classes.ClassParseUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author koen
 */
public class Class16EventLogData extends AbstractClass {
    
    private static final int EVENT_RECORD_LENGTH = 7;
    
    ClassIdentification classIdentification = new ClassIdentification(16,0,false);
    
    List meterEvents;
    
    /** Creates a new instance of Class15EventLogConfiguration */
    public Class16EventLogData(ClassFactory classFactory) {
        super(classFactory);
    }
    
    private static final int POWER_DOWN=0;
    private static final int POWER_UP=1;
    private static final int TIME_BEFORE=2;
    private static final int TIME_AFTER=3;
    private static final int TEST_MODE_START=4;
    private static final int TEST_MODE_STOP=5;
    private static final int DEMAND_RESET=6;
    private static final int EVENT_LOG_RESET=255;
    
    protected void parse(byte[] data) throws ProtocolException {
        
        if ((data.length%EVENT_RECORD_LENGTH) != 0) {
            throw new ProtocolException("Class16EventLogData, parse(), data length is not divideable by the event record length! ("+data.length+" % "+EVENT_RECORD_LENGTH+" = "+(data.length%EVENT_RECORD_LENGTH)+") !");
        }
        
        meterEvents = new ArrayList();
        int nrOfEvents = data.length / EVENT_RECORD_LENGTH;
        
        for (int event=0;event<nrOfEvents;event++) {
            
            int type = ProtocolUtils.getInt(data,event*EVENT_RECORD_LENGTH,1);
            Date date = ClassParseUtils.getDate6(data,event*EVENT_RECORD_LENGTH+1, classFactory.getAlpha().getTimeZone());
            
            switch(type) {
                case POWER_DOWN: {
                    meterEvents.add(new MeterEvent(date,MeterEvent.POWERDOWN,type));
                } break; // POWER_DOWN
                
                case POWER_UP: {
                    meterEvents.add(new MeterEvent(date,MeterEvent.POWERUP,type));
                } break; // POWER_UP
                
                case TIME_BEFORE: {
                    meterEvents.add(new MeterEvent(date,MeterEvent.SETCLOCK_BEFORE,type));
                } break; // TIME_BEFORE
                
                case TIME_AFTER: {
                    meterEvents.add(new MeterEvent(date,MeterEvent.SETCLOCK_AFTER,type));
                } break; // TIME_AFTER
                
                case TEST_MODE_START: {
                    meterEvents.add(new MeterEvent(date,MeterEvent.TEST_MODE_START,type));
                } break;  // TEST_MODE_START
                case TEST_MODE_STOP: {
                    meterEvents.add(new MeterEvent(date,MeterEvent.TEST_MODE_STOP,type));
                } break; // TEST_MODE_STOP
                
                case DEMAND_RESET: {
                    meterEvents.add(new MeterEvent(date,MeterEvent.MAXIMUM_DEMAND_RESET,type));
                } break; // DEMAND_RESET
                
                case EVENT_LOG_RESET: {
                    meterEvents.add(new MeterEvent(date,MeterEvent.CLEAR_DATA,type));
                } break; // EVENT_LOG_RESET
                
                default: {
                    meterEvents.add(new MeterEvent(date,MeterEvent.OTHER,type));
                } break; // default
                
            } // switch(type)
        } // for (int event=0;event<nrOfEvents;event++)
    }
    
    protected ClassIdentification getClassIdentification() {
        return classIdentification;
    }

    public List getMeterEvents() {
        return meterEvents;
    }
    
}
