package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.attributes.ScheduleManagerAttributes;
import com.energyict.dlms.cosem.methods.ScheduleManagerMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/06/2015 - 13:50
 */
public class ScheduleManager extends AbstractCosemObject {

    private static final ObisCode DEFAULT_OBISCODE = ObisCode.fromString("0.0.128.0.15.255");

    /**
     * Creates a new instance of AbstractCosemObject
     */
    public ScheduleManager(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static ObisCode getDefaultObisCode() {
        return DEFAULT_OBISCODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.SCHEDULE_MANAGER.getClassId();
    }

    /**
     * The set of known schedules for this concentrator.
     */
    public Array readSchedules() throws IOException {
        return readDataType(ScheduleManagerAttributes.SCHEDULES, Array.class);
    }

    /**
     * The current state of the scheduler
     */
    public TypeEnum readSchedulerState() throws IOException {
        return readDataType(ScheduleManagerAttributes.SCHEDULER_STATE, TypeEnum.class);
    }

    public void writeSchedulerState(TypeEnum state) throws IOException {
        write(ScheduleManagerAttributes.SCHEDULER_STATE, state);
    }

    /**
     * Adds a schedule to the set of known schedules for this concentrator.
     */
    public void addSchedule(Structure schedule) throws IOException {
        methodInvoke(ScheduleManagerMethods.ADD_SCHEDULE, schedule);
    }

    /**
     * Removes a schedule from the set of known schedules for this concentrator.
     */
    public void removeSchedule(long id) throws IOException {
        methodInvoke(ScheduleManagerMethods.REMOVE_SCHEDULE, new Unsigned32(id));
    }

    /**
     * Updates the schedule structure with the same ID as passed in the structure.
     */
    public void updateSchedule(Structure schedule) throws IOException {
        methodInvoke(ScheduleManagerMethods.UPDATE_SCHEDULE, schedule);
    }
}