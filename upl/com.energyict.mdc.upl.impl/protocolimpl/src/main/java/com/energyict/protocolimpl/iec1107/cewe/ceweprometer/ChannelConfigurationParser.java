package com.energyict.protocolimpl.iec1107.cewe.ceweprometer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
 
import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;


public class ChannelConfigurationParser {
    
    /** shorthand for kWh */ 
    private final static Unit Wh   = Unit.get(BaseUnit.WATTHOUR);
    /** shorthand for kvah */
    private final static Unit vah  = Unit.get(BaseUnit.VOLTAMPEREHOUR);
    /** shorthand for kvarh */
    private final static Unit varh = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR);

    static Object [][] QUANTITY = new Object [][] {
        { Wh,                          "active Energy imp." },     
        { Wh,                          "active Energy exp." },         
        { varh,                        "reactive Energy imp." },   
        { varh,                        "reactive Energy exp." },       
        { varh,                        "reactive Energy ind." },       
        { varh,                        "reactive Energy cap." },   
        { varh,                        "reactive Energy QI" },     
        { varh,                        "reactive Energy QII" },        
        { varh,                        "reactive Energy QIII" },       
        { varh,                        "reactive Energy QIV." },   
        { vah,                         "apparent energy imp." },       
        { vah,                         "apparent energy exp." },   
        { Unit.get( BaseUnit.VOLT ),    "phase voltage" },          
        { Unit.get( BaseUnit.VOLT ),    "line to line voltage" }, 
        { Unit.get( BaseUnit.AMPERE ),  "current." },         
        { Unit.get( BaseUnit.WATT ),    "active power" },           
        { Unit.get( BaseUnit.WATT ),    "reactive power" },      
        { Unit.get( BaseUnit.WATT ),    "apparent power" },          
        { Unit.get( BaseUnit.HERTZ ),   "frequency" },        
        { Unit.getUndefined(),          "phase angle." },           
        { Unit.getUndefined(),          "power factor." },              
        { Unit.getUndefined(),          "THD voltage" },            
        { Unit.getUndefined(),          "THD current" },                
        { Unit.getUndefined(),          "external reg. 0" },        
        { Unit.getUndefined(),          "external reg. 1" },        
        { Unit.getUndefined(),          "external reg. 2" },        
        { Unit.getUndefined(),          "external reg. 3" },        
        { Unit.getUndefined(),          "external reg. 4" },        
        { Unit.getUndefined(),          "external reg. 5" },        
        { Unit.getUndefined(),          "external reg. 6" },        
        { Unit.getUndefined(),          "external reg. 7" },        
    };
    
    private static String [] PHASE = {
        "All phases",
        "L1",
        "L2",
        "L3"
    };
    
    List toChannelInfo(String data, int nrChannels){
        
        data = data.substring(1, data.length()-1);
        List channelInfos = new ArrayList();
        
        for( int idx = 0; idx < nrChannels; idx++ ) {
        
            String s = data.substring(idx*2, (idx*2)+2);
            
            byte bit1To5 = (byte)(Byte.parseByte(s,16) & 0x3F);
            byte bit6To7 = (byte)(Byte.parseByte(s,16) & 0xC0);
             
            Unit unit = (Unit)QUANTITY[bit1To5][0];
            String name = (String)QUANTITY[bit1To5][1] + " " + PHASE[bit6To7];
            
            ChannelInfo ci = new ChannelInfo(idx, name, unit);
            
            /* for energy & external registers the current reading of the
             * register is logged, this is cumulative ... */
            
            if( bit1To5 < 12 || bit1To5 > 22 )
                ci.setCumulativeWrapValue(new BigDecimal("1000000000"));
            
            channelInfos.add(ci);
            
        }
        
        return channelInfos;
        
    }
    
}
