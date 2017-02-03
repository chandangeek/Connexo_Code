package com.energyict.protocolimpl.EMCO;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.EMCO.frame.RegisterRequestFrame;
import com.energyict.protocolimpl.EMCO.frame.RegisterResponseFrame;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 5/03/12
 * Time: 13:09
 */
public class EventLog  {

    private FP93 meterProtocol;
    private HashMap<String, String> faultFlags = new HashMap<String, String>();


    public EventLog(FP93 meterProtocol) {
        this.meterProtocol = meterProtocol;
        initFaultFlags();
    }

        private void initFaultFlags() {
        faultFlags.put("0", "Changed flag is set");
        faultFlags.put("1", "Communication fault (parity, overrun, noise)");
        faultFlags.put("2", "Power failure");
        faultFlags.put("3", "Relay output > 10 pulses/second");
        faultFlags.put("4", "Analog output out of range");
        faultFlags.put("5", "Flow input out of range");
        faultFlags.put("6", "Temperature input #1 out of range");
        faultFlags.put("7", "Temperature input #2 out of range");
        faultFlags.put("8", "Pressure input out of range");
        faultFlags.put("9", "A/D converter overrange");
        faultFlags.put("10", "RAM battery fault");
        faultFlags.put("11", "EEPROM checksum fault");
        faultFlags.put("12", "ROM checksum fault");
        faultFlags.put("13", "RAM read/write fault");
        faultFlags.put("14", "Unused");
        faultFlags.put("15", "Substitute input");
    }

    /**
     * Read out the 'Faults flags' register - create events for the flags set high.
     * @return
     */
    public ProfileData readEvents() throws IOException {
        try {
            RegisterMapping mapping = meterProtocol.getObisCodeMapper().searchRegisterMapping(91);    // Register to read out all the set fault flags - Note: When reading out, the register is cleared.
            RegisterRequestFrame requestFrame = new RegisterRequestFrame(meterProtocol.getDeviceID(), mapping.getObjectId());
            RegisterResponseFrame responseFrame = (RegisterResponseFrame) meterProtocol.getConnection().sendAndReceiveResponse(requestFrame);

            ProfileData profileData = new ProfileData();
            List<MeterEvent> meterEvents = getMeterEvents(responseFrame.getBitMask());
            meterEvents = shiftDates(meterEvents);
            profileData.setMeterEvents(meterEvents);

            return profileData;
        } catch (IOException e) {
            throw new IOException("Failure while reading out the events: " + e.getMessage());
        }
    }

    private List<MeterEvent> getMeterEvents(int bitMask) throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();

        if ((bitMask & 0x01) == 0x01) {
            meterEvents.add(new MeterEvent(getTimeFromTimeStampRegister(71), MeterEvent.CONFIGURATIONCHANGE, 0, faultFlags.get("0")));
        }
        if ((bitMask & 0x02) == 0x02) {
        }
        if ((bitMask & 0x04) == 0x04) {
            meterEvents.add(new MeterEvent(getTimeFromTimeStampRegister(74), MeterEvent.PHASE_FAILURE, 2, faultFlags.get("2")));
        }
        if ((bitMask & 0x08) == 0x08) {
            meterEvents.add(new MeterEvent(getTimeFromTimeStampRegister(75), MeterEvent.MEASUREMENT_SYSTEM_ERROR, 3, faultFlags.get("3")));
        }
        if ((bitMask & 0x10) == 0x10) {
            meterEvents.add(new MeterEvent(getTimeFromTimeStampRegister(76), MeterEvent.MEASUREMENT_SYSTEM_ERROR, 4, faultFlags.get("4")));
        }
        if ((bitMask & 0x20) == 0x20) {
            meterEvents.add(new MeterEvent(getTimeFromTimeStampRegister(77), MeterEvent.MEASUREMENT_SYSTEM_ERROR, 5, faultFlags.get("5")));
        }
        if ((bitMask & 0x40) == 0x40) {
            meterEvents.add(new MeterEvent(getTimeFromTimeStampRegister(78), MeterEvent.MEASUREMENT_SYSTEM_ERROR, 6, faultFlags.get("6")));
        }
        if ((bitMask & 0x80) == 0x80) {
            meterEvents.add(new MeterEvent(getTimeFromTimeStampRegister(79), MeterEvent.MEASUREMENT_SYSTEM_ERROR, 7, faultFlags.get("7")));
        }
        if ((bitMask & 0x100) == 0x100) {
            meterEvents.add(new MeterEvent(getTimeFromTimeStampRegister(80), MeterEvent.MEASUREMENT_SYSTEM_ERROR, 8, faultFlags.get("8")));
        }
        if ((bitMask & 0x200) == 0x200) {
            meterEvents.add(new MeterEvent(getTimeFromTimeStampRegister(81), MeterEvent.MEASUREMENT_SYSTEM_ERROR, 9, faultFlags.get("9")));
        }
        if ((bitMask & 0x400) == 0x400) {
            meterEvents.add(new MeterEvent(getTimeFromTimeStampRegister(82), MeterEvent.BATTERY_VOLTAGE_LOW, 10, faultFlags.get("10")));
        }
        if ((bitMask & 0x800) == 0x800) {
            meterEvents.add(new MeterEvent(getTimeFromTimeStampRegister(83), MeterEvent.RAM_MEMORY_ERROR, 11, faultFlags.get("11")));
        }
        if ((bitMask & 0x1000) == 0x1000) {
            meterEvents.add(new MeterEvent(getTimeFromTimeStampRegister(84), MeterEvent.ROM_MEMORY_ERROR, 12, faultFlags.get("12")));
        }
        if ((bitMask & 0x2000) == 0x2000) {
            meterEvents.add(new MeterEvent(getTimeFromTimeStampRegister(85), MeterEvent.RAM_MEMORY_ERROR, 13, faultFlags.get("13")));
        }

        return meterEvents;
    }

    public Date getTimeFromTimeStampRegister(int registerID) throws IOException{
        // Retrieve time of fault
        RegisterMapping mapping = meterProtocol.getObisCodeMapper().searchRegisterMapping(registerID);    // The Corresponding register containing the timestamp when the event occurred.
        RegisterRequestFrame requestFrame = new RegisterRequestFrame(meterProtocol.getDeviceID(), mapping.getObjectId());
        RegisterResponseFrame responseFrame = (RegisterResponseFrame) meterProtocol.getConnection().sendAndReceiveResponse(requestFrame);

        Calendar calendar = ProtocolUtils.getCleanGMTCalendar();
        if (responseFrame.getValue().longValue() != 0) {
            calendar.setTimeInMillis((315532800 + responseFrame.getValue().longValue()) * 1000);    // [ Number of seconds [1 jan 1970 - 1 jan 1980] + seconds since 1980 ] * 1000
        } else {
            throw new IOException("The time register doesn't contain a valid timestamp.");
        }

        return calendar.getTime();
    }

    /**
     * Make a proper string - describing all set fault flags - out of the given bitMask.
     */
    public String getEventDescriptions(int bitMask) throws IOException {
        String message = "";

        int BitMatcher = 1;
        for (int i = 0; i < 16; i++) {
            if ((bitMask & BitMatcher) == BitMatcher) {
                message += ",[" + i + "] " + faultFlags.get(Integer.toString(i));
            }
            BitMatcher = BitMatcher << 1;
        }
        String info = message.replaceFirst(",", "");

        if (info.length() > 256) {
            return info.substring(0, 256);
        }
        return info;
    }


    /**
     * Check the timestamps for the meter events, add a second to make the stamps different (if necessary)
     *
     * @param meterEvents the events
     * @return the fixed events
     */
    private List<MeterEvent> shiftDates(List<MeterEvent> meterEvents) {
        if (meterEvents.size() < 2) {
            return meterEvents;
        }
        List<MeterEvent> newMeterEvents = new ArrayList<MeterEvent>();
        newMeterEvents.add(meterEvents.get(0)); //add the first event
        MeterEvent newMeterEvent;

        for (int i = 1; i < meterEvents.size(); i++) {
            MeterEvent previousMeterEvent = meterEvents.get(i - 1);
            MeterEvent currentMeterEvent = meterEvents.get(i);
            if (currentMeterEvent.getTime().equals(previousMeterEvent.getTime())) {
                Date newDate = new Date(newMeterEvents.get(i - 1).getTime().getTime() + 1000); //add one second to make the timestamps different
                newMeterEvent = new MeterEvent(newDate, currentMeterEvent.getEiCode(), currentMeterEvent.getProtocolCode(), currentMeterEvent.getMessage());
            } else {
                newMeterEvent = currentMeterEvent;
            }
            newMeterEvents.add(i, newMeterEvent);
        }
        return newMeterEvents;
    }
}