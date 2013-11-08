package com.energyict.protocolimpl.coronis.amco.rtm.core.alarmframe;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Parent class containing methods used by all alarm frames instances.
 *
 * Copyrights EnergyICT
 * Date: 11-apr-2011
 * Time: 13:48:39
 */
abstract public class AbstractAlarmFrame {

    protected int status;
    protected int optionalValue;
    protected Date date;
    protected RTM rtm;

    public AbstractAlarmFrame(RTM rtm, int optionalValue, int status, Date date) {
        this.optionalValue = optionalValue;
        this.status = status;
        this.date = date;
        this.rtm = rtm;
    }

    /**
     * Generates a list of meter events based on the received info in the alarm frame.
     * @return list of meter events
     * @throws IOException
     */
    abstract public List<MeterEvent> getMeterEvents();

    /**
     * Checks if a certain bit in the status flag is set.
     * @param i number of the bit
     * @return true if set
     */
    protected boolean isBitSet(int i) {
        return (status & ((int) (Math.pow(2, i)))) > 0;
    }
}
