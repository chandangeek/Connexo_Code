package com.energyict.protocolimpl.dlms.siemenszmd;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/04/2015 - 17:10
 */
public class ReverseActiveEnergyLogBook {

    private static final ObisCode NEGATIVE_ACTIVE_ENERGY_LOGBOOK = ObisCode.fromString("1.1.99.98.137.255");
    private static final ObisCode VOLTAGE_PHASE1_REGISTER = ObisCode.fromString("1.1.32.7.0.255");

    private final CosemObjectFactory cosemObjectFactory;

    Map<CapturedObject, Unit> cachedUnits;

    public ReverseActiveEnergyLogBook(CosemObjectFactory cosemObjectFactory) {
        this.cosemObjectFactory = cosemObjectFactory;
    }

    public List<MeterEvent> readEvents(Calendar fromCalendar, Calendar toCalendar) throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        DedicatedEventLogSimple dedicatedEventLogSimple;
        try {
            dedicatedEventLogSimple = cosemObjectFactory.getDedicatedEventLogSimple(NEGATIVE_ACTIVE_ENERGY_LOGBOOK);
        } catch (ProtocolException e) {
            return meterEvents; //Logbook does not exist
        }

        DataContainer buffer;
        try {
            buffer = readBuffer(dedicatedEventLogSimple);
        } catch (DataAccessResultException e) {
            if (e.getCode() == DataAccessResultCode.OBJECT_UNDEFINED || e.getCode() == DataAccessResultCode.OBJECT_UNAVAILABLE) {
                return meterEvents; //Logbook does not exist
            } else {
                throw e;
            }
        }
        List<CapturedObject> capturedObjects = readCapturedObjects(dedicatedEventLogSimple);

        List<CapturedObject> startEnergyRegisters = new ArrayList<CapturedObject>();
        List<CapturedObject> stopEnergyRegisters = new ArrayList<CapturedObject>();
        List<Unit> startEnergyUnits = new ArrayList<Unit>();
        List<Unit> stopEnergyUnits = new ArrayList<Unit>();

        //Check the captured objects to see if there's energy registers (optional)
        while (!isVoltageRegister(capturedObjects, 2 + startEnergyRegisters.size())) {
            CapturedObject energyRegister = capturedObjects.get(2 + startEnergyRegisters.size());
            startEnergyRegisters.add(energyRegister);
            startEnergyUnits.add(getUnit(energyRegister));
        }
        while (!isVoltageRegister(capturedObjects, 11 + startEnergyRegisters.size() + stopEnergyRegisters.size())) {
            CapturedObject energyRegister = capturedObjects.get(11 + startEnergyRegisters.size() + stopEnergyRegisters.size());
            stopEnergyRegisters.add(energyRegister);
            stopEnergyUnits.add(getUnit(energyRegister));
        }

        for (int index = 0; index < buffer.getRoot().getNrOfElements(); index++) {

            int offset = 0;
            DataStructure structure = buffer.getRoot().getStructure(index);
            int entryNumber = structure.getInteger(offset++);   //Ignore

            Date started = structure.getOctetString(offset++).toUTCDate();

            //Up to 3 start energy register values, optional
            List<BigDecimal> startEnergyRegisterValues = new ArrayList<BigDecimal>();
            for (int energyRegisterIndex = 0; energyRegisterIndex < startEnergyRegisters.size(); energyRegisterIndex++) {
                startEnergyRegisterValues.add(structure.getBigDecimalValue(offset++));
            }

            BigDecimal voltage1Start = structure.getBigDecimalValue(offset++);
            BigDecimal voltage2Start = structure.getBigDecimalValue(offset++);
            BigDecimal voltage3Start = structure.getBigDecimalValue(offset++);
            BigDecimal current1Start = structure.getBigDecimalValue(offset++);
            BigDecimal current2Start = structure.getBigDecimalValue(offset++);
            BigDecimal current3Start = structure.getBigDecimalValue(offset++);
            BigDecimal currentNStart = structure.getBigDecimalValue(offset++);
            BigDecimal powerFactorStart = structure.getBigDecimalValue(offset++);

            Date stopped = structure.getOctetString(offset++).toUTCDate();

            //Up to 3 stop energy register values, optional
            List<BigDecimal> stopEnergyRegisterValues = new ArrayList<BigDecimal>();
            for (int energyRegisterIndex = 0; energyRegisterIndex < stopEnergyRegisters.size(); energyRegisterIndex++) {
                stopEnergyRegisterValues.add(structure.getBigDecimalValue(offset++));
            }

            BigDecimal voltage1Stop = structure.getBigDecimalValue(offset++);
            BigDecimal voltage2Stop = structure.getBigDecimalValue(offset++);
            BigDecimal voltage3Stop = structure.getBigDecimalValue(offset++);
            BigDecimal current1Stop = structure.getBigDecimalValue(offset++);
            BigDecimal current2Stop = structure.getBigDecimalValue(offset++);
            BigDecimal current3Stop = structure.getBigDecimalValue(offset++);
            BigDecimal currentNStop = structure.getBigDecimalValue(offset++);
            BigDecimal powerFactorStop = structure.getBigDecimalValue(offset++);

            BigDecimal duration = structure.getBigDecimalValue(offset++);
            BigDecimal phase = structure.getBigDecimalValue(offset);

            Unit voltageUnit = getUnit(capturedObjects.get(2 + startEnergyRegisters.size()));
            Unit currentUnit = getUnit(capturedObjects.get(5 + startEnergyRegisters.size()));

            if (started.after(fromCalendar.getTime()) && started.before(toCalendar.getTime())) {
                StringBuilder startEventDescription = createFullDescription(voltageUnit, currentUnit, true, startEnergyRegisterValues, voltage1Start, voltage2Start, voltage3Start, current1Start, current2Start, current3Start, currentNStart, powerFactorStart, duration, phase, startEnergyUnits);
                meterEvents.add(new MeterEvent(started, MeterEvent.REVERSE_RUN, 0, startEventDescription.toString()));
            }

            if (stopped.after(fromCalendar.getTime()) && stopped.before(toCalendar.getTime())) {
                StringBuilder stopEventDescription = createFullDescription(voltageUnit, currentUnit, false, stopEnergyRegisterValues, voltage1Stop, voltage2Stop, voltage3Stop, current1Stop, current2Stop, current3Stop, currentNStop, powerFactorStop, duration, phase, stopEnergyUnits);
                meterEvents.add(new MeterEvent(stopped, MeterEvent.REVERSE_RUN, 0, stopEventDescription.toString()));
            }
        }

        return meterEvents;
    }

    private boolean isVoltageRegister(List<CapturedObject> capturedObjects, int offset) {
        return capturedObjects.get(offset).getObisCode().equals(VOLTAGE_PHASE1_REGISTER);
    }

    protected List<CapturedObject> readCapturedObjects(DedicatedEventLogSimple dedicatedEventLogSimple) throws IOException {
        return dedicatedEventLogSimple.getCaptureObjects();
    }

    /**
     * Reads the full buffer of the logbook, since it does not support selective access
     */
    protected DataContainer readBuffer(DedicatedEventLogSimple dedicatedEventLogSimple) throws IOException {
        return dedicatedEventLogSimple.getBuffer();
    }

    /**
     * Read out the unit of a given register, cache the result.
     */
    public Unit getUnit(final CapturedObject capturedObject) throws IOException {
        if (getCachedUnits().get(capturedObject) == null) {
            Unit unit;
            final ObisCode obis = capturedObject.getObisCode();
            final DLMSClassId classId = DLMSClassId.findById(capturedObject.getClassId());

            final int attr = capturedObject.getAttributeIndex();
            switch (classId) {
                case REGISTER: {
                    unit = cosemObjectFactory.getRegister(obis).getScalerUnit().getEisUnit();
                    break;
                }

                case EXTENDED_REGISTER: {
                    if (attr == ExtendedRegisterAttributes.VALUE.getAttributeNumber()) {
                        unit = cosemObjectFactory.getExtendedRegister(obis).getScalerUnit().getEisUnit();
                    } else if (attr == ExtendedRegisterAttributes.CAPTURE_TIME.getAttributeNumber()) {
                        unit = Unit.get("ms");
                    } else {
                        unit = Unit.getUndefined();
                    }
                    break;
                }

                case DEMAND_REGISTER: {
                    if (attr == DemandRegisterAttributes.CURRENT_AVG_VALUE.getAttributeNumber()) {
                        unit = cosemObjectFactory.getDemandRegister(obis).getScalerUnit().getEisUnit();
                    } else if (attr == DemandRegisterAttributes.LAST_AVG_VALUE.getAttributeNumber()) {
                        unit = cosemObjectFactory.getDemandRegister(obis).getScalerUnit().getEisUnit();
                    } else if (attr == DemandRegisterAttributes.CAPTURE_TIME.getAttributeNumber()) {
                        unit = Unit.get("ms");
                    } else {
                        unit = Unit.getUndefined();
                    }
                    break;
                }

                default: {
                    unit = Unit.getUndefined();
                    break;
                }
            }

            getCachedUnits().put(capturedObject, unit);
            return unit;
        } else {
            return getCachedUnits().get(capturedObject);
        }
    }

    private Map<CapturedObject, Unit> getCachedUnits() {
        if (cachedUnits == null) {
            cachedUnits = new HashMap<CapturedObject, Unit>();
        }
        return cachedUnits;
    }

    private StringBuilder createFullDescription(Unit voltageUnit, Unit currentUnit, boolean start, List<BigDecimal> energyRegisterValues, BigDecimal voltage1, BigDecimal voltage2, BigDecimal voltage3, BigDecimal current1, BigDecimal current2, BigDecimal current3, BigDecimal currentN, BigDecimal powerFactor, BigDecimal duration, BigDecimal phase, List<Unit> energyRegisterUnits) {
        StringBuilder eventDescription = new StringBuilder();
        eventDescription.append(start ? "Reverse run start. " : "Reverse run end. ");

        if (!energyRegisterValues.isEmpty()) {
            eventDescription.append("E:");
            for (int index = 0; index < energyRegisterValues.size(); index++) {
                if (index != 0) {
                    eventDescription.append(",");
                }
                eventDescription.append(energyRegisterValues.get(index).toString()).append(energyRegisterUnits.get(index).toString());
            }
            eventDescription.append(";");
        }

        eventDescription.append("V:")
                .append(voltage1)
                .append(",")
                .append(voltage2)
                .append(",")
                .append(voltage3)
                .append(voltageUnit)
                .append(";")
                .append("I:")
                .append(current1)
                .append(",")
                .append(current2)
                .append(",")
                .append(current3)
                .append(",")
                .append(currentN)
                .append(currentUnit)
                .append(";")
                .append("PF:")
                .append(powerFactor)
                .append("%")
                .append(";")
                .append("D:")
                .append(duration)
                .append("s")
                .append(";")
                .append("P:")
                .append(phase);
        return eventDescription;
    }
}