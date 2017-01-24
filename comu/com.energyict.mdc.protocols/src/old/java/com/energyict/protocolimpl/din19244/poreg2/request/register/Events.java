package com.energyict.protocolimpl.din19244.poreg2.request.register;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.DinTimeParser;
import com.energyict.protocolimpl.din19244.poreg2.core.ExtendedValue;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterDataParser;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterGroupID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class to read out the events table.
 * Can generate a proper list of meter events based on the received data.
 * Copyrights EnergyICT
 * Date: 20-apr-2011
 * Time: 14:10:28
 */
public class Events extends AbstractRegister {

    private List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

    /**
     * Constructor to request all fields and registers
     */
    public Events(Poreg poreg) {
        super(poreg, 0, 0, 255, 4);
    }

    /**
     * Constructor to request a custom number of fields and registers
     */
    public Events(Poreg poreg, int registerAddress, int fieldAddress, int numberOfRegisters, int numberOfFields) {
        super(poreg, registerAddress, fieldAddress, numberOfRegisters, numberOfFields);
    }

    //Don't send continue commands for this!
    protected void doTheRequest() throws IOException {
        totalReceivedNumberOfRegisters = 0;
        corruptFrame = false;
        corruptCause = "";

        try {
            byte[] response = poreg.getConnection().doRequest(getRequestASDU(), getAdditionalBytes(), getExpectedResponseType(), getResponseASDU());
            response = validateAdditionalBytes(response);

            //Parse the rest
            parse(response);
        } catch (ProtocolConnectionException e) { //E.g. crc error. Do not catch severe IOExceptions
            corruptFrame = true;
            corruptCause = e.getMessage();
        }
    }

    public void parse(byte[] data) throws IOException {
        List<ExtendedValue> values = RegisterDataParser.parseData(data, getTotalReceivedNumberOfRegisters(), getReceivedNumberOfFields());

        int offset = 0;
        Date eventDate;
        int code;
        int subCode;
        long epoch;

        while (offset < values.size()) {
            code = values.get(offset++).getValue();
            if (code == 0) {
                break;
            }
            subCode = values.get(offset++).getValue();
            epoch = values.get(offset++).getValue();
            if (epoch == 0) {
                break;
            }
            eventDate = DinTimeParser.calcDate(poreg, epoch);
            offset++;   //Skip the additional information
            meterEvents.add(new MeterEvent(eventDate, getEiServerCode(code, subCode), code, getDescription(code, subCode)));
        }
    }

    private int getEiServerCode(int code, int subCode) {
        switch (code) {
            case 2:
                return MeterEvent.PROGRAM_MEMORY_ERROR;
            case 3:
                return MeterEvent.POWERDOWN;
            case 4:
                return MeterEvent.BATTERY_VOLTAGE_LOW;
            case 6:
                return MeterEvent.CLEAR_DATA;
            case 7:
                return MeterEvent.SETCLOCK;
            case 8:
                return MeterEvent.HARDWARE_ERROR;
            case 15:
                return MeterEvent.CONFIGURATIONCHANGE;
        }

        if (code > 127) {
            switch (subCode) {
                case 9:
                    return MeterEvent.PHASE_FAILURE;
                case 10:
                    return MeterEvent.POWERDOWN;
                case 11:
                    return MeterEvent.POWERUP;
                case 12:
                    return MeterEvent.SETCLOCK;
                case 16:
                    return MeterEvent.BATTERY_VOLTAGE_LOW;
                case 67:
                    return MeterEvent.POWERDOWN;
                case 95:
                    return MeterEvent.CONFIGURATIONCHANGE;
            }
        }
        return MeterEvent.OTHER;
    }

    private String getDescription(int code, int subCode) {
        String part1 = "";
        String part2 = "";

        switch (code) {
            case 1:
                part1 = "Initializing";
                switch (subCode) {
                    case 1:
                        part2 = "program restart";
                        break;
                    case 2:
                        part2 = "CPU warm start";
                        break;
                    case 3:
                        part2 = "CPU cold start";
                        break;
                    default:
                        part2 = "";
                        break;
                }
                break;
            case 2:
                part1 = "System self test error";
                switch (subCode) {
                    case 1:
                        part2 = "CPU program memory";
                        break;
                    case 2:
                        part2 = "CPU parameter memory";
                        break;
                    case 3:
                        part2 = "CPU data memory";
                        break;
                    default:
                        part2 = "";
                        break;
                }
                break;
            case 3:
                part1 = "Power supply error";
                switch (subCode) {
                    case 1:
                        part2 = "CPU power supply shutdown";
                        break;
                    case 2:
                        part2 = "CPU power supply disturbance";
                        break;
                    case 3:
                        part2 = "CPU power supply 1 defect";
                        break;
                    case 4:
                        part2 = "CPU power supply 2 defect";
                        break;
                    case 8:
                        part2 = "Power supply phase R OFF - IskraEmeco Extension";
                        break;
                    case 9:
                        part2 = "Power supply phase S OFF - IskraEmeco Extension";
                        break;
                    case 10:
                        part2 = "Power supply phase T OFF - IskraEmeco Extension";
                        break;
                    case 11:
                        part2 = "Power supply phase R ON -  IskraEmeco Extension";
                        break;
                    case 12:
                        part2 = "Power supply phase S ON -  IskraEmeco Extension";
                        break;
                    case 13:
                        part2 = "Power supply phase T ON -  IskraEmeco Extension";
                        break;
                    default:
                        part2 = "";
                        break;
                }
                break;
            case 4:
                part1 = "Battery error";
                break;
            case 5:
                part1 = "Data overrun";
                break;
            case 6:
                part1 = "Data loss";
                break;
            case 7:
                part1 = "Time messages";
                break;
            case 8:
                part1 = "Module error";
                break;
            case 9:
                part1 = "Impulse error";
                part2 = "on input " + subCode;
                break;
            case 10:
                part1 = "Serial input communication error";
                part2 = "on channel " + subCode;
                break;
            case 11:
                part1 = "External messages";
                break;
            case 12:
                part1 = "Impulse overrun on output";
                break;
            case 13:
                part1 = "Meter compare error";
                break;
            case 14:
                part1 = "Register overrun";
                break;
            case 15:
                part1 = "Parameter change";
                switch (subCode) {
                    case 1:
                        part2 = "CPU parameter change";
                        break;
                    case 16:
                        part2 = "memory card module parameter change";
                        break;
                    case 32:
                        part2 = "printer module parameter change";
                        break;
                    case 48:
                        part2 = "communication module parameter change";
                        break;
                    default:
                        part2 = "";
                        break;
                }
                break;
            case 17:
                part1 = "Manual entering";
                break;
            case 18:
                part1 = "Warning";
                break;
            case 19:
                part1 = "Disturbance";
                break;

            case 21:
                part1 = "Serial input meter exchange";
                part2 = "meter on communication channel " + subCode + " has been replaced";
                break;
        }

        if (code >= 128) {
            part1 = "Serial meter event";
            switch (subCode) {
                case 7:
                    part2 = "serial meter " + "(index " + (code - 128) + ") maintenance req.";
                    break;
                case 8:
                    part2 = "serial meter " + "(index " + (code - 128) + ") com. channel disconnected";
                    break;
                case 9:
                    part2 = "serial meter " + "(index " + (code - 128) + ") incorrect phase sequence";
                    break;
                case 10:
                    part2 = "serial meter " + "(index " + (code - 128) + ") power failure";
                    break;
                case 11:
                    part2 = "serial meter " + "(index " + (code - 128) + ") power ON";
                    break;
                case 12:
                    part2 = "serial meter " + "(index " + (code - 128) + ") clock change";
                    break;
                case 13:
                    part2 = "serial meter " + "(index " + (code - 128) + ") reset cumulate";
                    break;
                case 14:
                    part2 = "serial meter " + "(index " + (code - 128) + ") season change";
                    break;
                case 15:
                    part2 = "serial meter " + "(index " + (code - 128) + ") value dist";
                    break;
                case 16:
                    part2 = "serial meter " + "(index " + (code - 128) + ") low battery";
                    break;
                case 17:
                    part2 = "serial meter " + "(index " + (code - 128) + ") device disturbed";
                    break;

                case 67:
                    part2 = "serial meter " + "(communication channel " + (code - 128) + ") power failure";
                    break;
                case 77:
                    part2 = "serial meter " + "(communication channel " + (code - 128) + ") auxiliary bus";
                    break;
                case 81:
                    part2 = "serial meter " + "(communication channel " + (code - 128) + ") manual input";
                    break;
                case 82:
                    part2 = "serial meter " + "(communication channel " + (code - 128) + ") warning";
                    break;
                case 83:
                    part2 = "serial meter " + "(communication channel " + (code - 128) + ") error indication";
                    break;
                case 95:
                    part2 = "serial meter " + "(communication channel " + (code - 128) + ") parameter change";
                    break;
            }
        }
        return part1 + ", " + ("".equals(part2) ? ("sub code: " + subCode) : part2);
    }

    public List<MeterEvent> getMeterEvents() {
        return this.meterEvents;
    }

    public int getRegisterGroupID() {
        return RegisterGroupID.Events.getId();
    }
}