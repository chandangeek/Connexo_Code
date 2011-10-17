package com.energyict.protocolimpl.dlms.idis;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.dlms.idis.events.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 19/09/11
 * Time: 15:42
 */
public class MBusProfileDataReader extends ProfileDataReader {

    private static ObisCode MBUS_EVENT_LOG = ObisCode.fromString("0.0.99.98.3.255");
    private static ObisCode MBUS_CONTROL_LOG = ObisCode.fromString("0.0.24.5.0.255");

    public MBusProfileDataReader(IDIS idis) {
        super(idis);
    }

    private ObisCode getMBusControlLogObisCode() {
        return ProtocolTools.setObisCodeField(MBUS_CONTROL_LOG, 1, (byte) idis.getGasSlotId());
    }

    protected List<MeterEvent> getMeterEvents(Calendar fromCal, Calendar toCal) throws IOException {
        List<MeterEvent> events = new ArrayList<MeterEvent>();
        events.addAll(getMBusEventLog(fromCal, toCal));
        events.addAll(getMBusControlLog(fromCal, toCal));
        return events;
    }

    protected List<ChannelInfo> getChannelInfo(List<CapturedObject> capturedObjects) throws IOException {
        List<ChannelInfo> infos = new ArrayList<ChannelInfo>();
        int counter = 0;

        for (CapturedObject capturedObject : capturedObjects) {
            if (capturedObject.getClassId() == DLMSClassId.REGISTER.getClassId()) {
                ObisCode obisCode = capturedObject.getLogicalName().getObisCode();
                Quantity quantity = idis.readRegister(obisCode).getQuantity();
                Unit unit = Unit.get("");
                if (quantity != null) {
                    unit = quantity.getUnit();
                }

                infos.add(new ChannelInfo(counter, obisCode.toString(), unit));
                counter++;
            }
        }
        return infos;
    }

    private List<MeterEvent> getMBusControlLog(Calendar fromCal, Calendar toCal) {
        try {
            DataContainer mBusControlLogDC = idis.getCosemObjectFactory().getProfileGeneric(getMBusControlLogObisCode()).getBuffer(fromCal, toCal);
            AbstractEvent mBusControlLog;
            switch (idis.getGasSlotId()) {
                case 1:
                    mBusControlLog = new MBusControlLog1(idis.getTimeZone(), mBusControlLogDC);
                    break;
                case 2:
                    mBusControlLog = new MBusControlLog2(idis.getTimeZone(), mBusControlLogDC);
                    break;
                case 3:
                    mBusControlLog = new MBusControlLog3(idis.getTimeZone(), mBusControlLogDC);
                    break;
                case 4:
                    mBusControlLog = new MBusControlLog4(idis.getTimeZone(), mBusControlLogDC);
                    break;
                default:
                    return new ArrayList<MeterEvent>();
            }
            return mBusControlLog.getMeterEvents();
        } catch (IOException e) {
            idis.getLogger().log(Level.WARNING, "MBus control log is not supported by the device:" + e.getMessage());
            return new ArrayList<MeterEvent>();
        }
    }

    private List<MeterEvent> getMBusEventLog(Calendar fromCal, Calendar toCal) {
        try {
            DataContainer mBusEventLogDC = idis.getCosemObjectFactory().getProfileGeneric(MBUS_EVENT_LOG).getBuffer(fromCal, toCal);
            MBusEventLog mBusEventLog = new MBusEventLog(idis.getTimeZone(), mBusEventLogDC);
            return mBusEventLog.getMeterEvents();
        } catch (IOException e) {
            idis.getLogger().log(Level.WARNING, "MBus event log is not supported by the device:" + e.getMessage());
            return new ArrayList<MeterEvent>();
        }
    }
}