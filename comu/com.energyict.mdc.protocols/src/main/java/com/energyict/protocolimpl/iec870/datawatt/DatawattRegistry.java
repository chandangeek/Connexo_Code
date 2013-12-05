/*
 * DatawattRegistry.java
 *
 * Created on 2 juli 2003, 13:42
 */

package com.energyict.protocolimpl.iec870.datawatt;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class DatawattRegistry {

    DataWatt dataWatt=null;

    /** Creates a new instance of DatawattRegistry */
    public DatawattRegistry(DataWatt dataWatt) {
        this.dataWatt=dataWatt;
    }

    public Number getRegister(Channel channel) throws IOException {
        Iterator it;
        if (channel.isCounterInput()) {
            dataWatt.getApplicationFunction().counterInterrogationCommandASDU();
            List integratedTotals = dataWatt.getApplicationFunction().getIntegratedTotals();
            it = integratedTotals.iterator();
            while(it.hasNext()) {
               IntegratedTotal itot = (IntegratedTotal)it.next();
               if (itot.getChannel().isEqual(channel)) return itot.getValue();
            }
        }
        if (channel.isAnalogInput() || channel.isAnalogOutput()) {
            dataWatt.getApplicationFunction().interrogationCommandASDU();
            List measuredNormValues = dataWatt.getApplicationFunction().getMeasuredNormValues();
            it = measuredNormValues.iterator();
            while(it.hasNext()) {
               MeasuredNormValue me = (MeasuredNormValue)it.next();
               if (me.getChannel().isEqual(channel)) return me.getValue();
            }
        }
        if (channel.isDigitalInput()) {
            dataWatt.getApplicationFunction().interrogationCommandASDU();
            List singlePointInfos = dataWatt.getApplicationFunction().getSinglePointInfos();
            it = singlePointInfos.iterator();
            while(it.hasNext()) {
                SinglePointInfo sp = (SinglePointInfo)it.next();
                if (sp.getChannel().isEqual(channel)) return BigDecimal.valueOf((long)sp.getStatus());
            }
        }

        throw new IOException("DatawattRegistry, getRegister, register id="+channel.getChannelId()+" type="+channel.getChannelType()+" does not exist!");
    }


}
