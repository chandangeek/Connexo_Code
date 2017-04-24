/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * LogInterpreter.java
 *
 * Created on 30 maart 2004, 11:04
 */

package com.energyict.protocolimpl.pact.core.log;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class LogInterpreter {

	private List meterEvents; // of type MeterEvent

    /** Creates a new instance of LogInterpreter */
    public LogInterpreter(byte[] data, TimeZone timeZone) {
        meterEvents = new ArrayList();
        parse(data,timeZone);
    }

    private static final int LOG_HEADER=0;
    private static final int LOG_VALIDATOR=255;

    private void parse(byte[] data, TimeZone timeZone) {
        // parse the log data blocks and build a MeterEvent List
        int count=0;
        LogHeader lh = null;
        do {
           switch(ProtocolUtils.byte2int(data[count])) {
               case LOG_HEADER: {
                   lh = new LogHeader(ProtocolUtils.getSubArray2(data,count,8));
               } break; // LOG_HEADER

               case LOG_VALIDATOR: {

                   lh = null;
               } break; // LOG_VALIDATOR

               default: {
                   if (lh != null) {
                      MeterEvent me = LogMessage.getMeterEvent(lh, new LogEvent(ProtocolUtils.getSubArray2(data,count,8),timeZone));
                      if (me != null) {
						meterEvents.add(me);
					}
                   }

               } break; // events

           } // switch(ProtocolUtils.byte2int(data[count]))
        } while((count+=8) < (data.length-1));
    } // private void parse(byte[] data, TimeZone timeZone)

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        Iterator it = meterEvents.iterator();
        while(it.hasNext()) {
           MeterEvent me = (MeterEvent)it.next();
           strBuff.append(me.getTime()+", "+me.toString()+"\r\n");
        }
        return strBuff.toString();
    }

    /** Getter for property meterEvents.
     * @return Value of property meterEvents.
     *
     */
    public java.util.List getMeterEvents() {
        return meterEvents;
    }


}
