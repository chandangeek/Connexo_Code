package com.energyict.protocolimplv2.dlms.idis.iskra.mx382.events;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130StandardEventLog;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by cisac on 1/20/2016.
 */
public class Mx382StandardEventLog extends AM130StandardEventLog {

    public Mx382StandardEventLog(TimeZone timeZone, DataContainer dc) {
        super(timeZone, dc);
    }

    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        switch (eventId) {
            case 230:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FATAL_ERROR, eventId, "Fatal error detected"));
                break;
            case 231:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BILLING_ACTION, eventId, "Billing reset"));
                break;
            case 232:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Power down phase L1"));
                break;
            case 233:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Power down phase L2"));
                break;
            case 234:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PHASE_FAILURE, eventId, "Power down phase L3"));
                break;
            case 235:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Power restored phase L1"));
                break;
            case 236:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Power restored phase L2"));
                break;
            case 237:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Power restored phase L3"));
                break;
            case 238:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "No connection timeout"));
                break;
            case 239:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Prepay Token Enter Success"));
                break;
            case 240:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Prepay Token Enter Fail"));
                break;
            case 241:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Prepay Credit Expired"));
                break;
            case 242:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Prepay Emergency Credit Expired"));
                break;
            case 243:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Prepay Emergency Credit Activated"));
                break;

            default:
                super.buildMeterEvent(meterEvents, eventTimeStamp, eventId);
        }
    }
}
