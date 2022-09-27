package com.energyict.mdc.protocol.inbound.mbus.factory.mappings;

import com.energyict.protocol.MeterEvent;


/**
 * Byte 	Bit 	Meaning 	                            Explanation
 * -------------------------------------------------------------------------------------------
 * EF1	    b0	    Memorized Backflow
 * 	        b1	    Leakage
 * 	        b2	    Stuck meter         	                no consumption
 * 	        b3	    Underflow
 * 	        b4	    Overflow                                (memorized burst)
 * 	        b5	    Magnetic fraud	                        Memorized magnetic flag
 * 	        b6	    Unclipping / Wire cut (sensor failure)	Razor tamper or Alisa coil damaged
 * 	        b7	    Frost risk
 *
 * EF2	    b0	    Actual Removal
 * 	        b1	    High temp
 * 	        b2	    Clock sync
 * 	        b3	    Actual Magnetic field flag
 * 	        b4	    Battery Usage Indicator	                 Set if BUI is ABOVE or CRITICAL
 * 	        b5	    Actual burst
 * 	        b6	    Reversed meter
 * 	        b7	    Actual backflow
 */
public enum ErrorFlagsMapping {

    // first error-field (EF1) bits
    MEMORIZED_BACKFLOW      (1, 0, "Memorized back-flow", MeterEvent.OTHER),
    LEAKAGE                 (1, 1, "Leakage", MeterEvent.OTHER),
    STUCK_METER             (1, 2, "Stuck meter (no consumption)", MeterEvent.OTHER),
    UNDERFLOW               (1, 3, "Underflow", MeterEvent.OTHER),
    OVERFLOW                (1, 4, "Overflow", MeterEvent.OTHER),
    MAGNETIC_FRAUD          (1, 5, "Magnetic fraud", MeterEvent.OTHER),
    UNCLIPPING              (1, 6, "Unclipping", MeterEvent.OTHER),
    FROST_RISK              (1, 7, "Frost risk", MeterEvent.OTHER),

    // second error-field (EF2) bits
    ACTUAL_REMOVAL          (2, 0, "Actual Removal", MeterEvent.OTHER),
    HIGH_TEMP               (2, 1, "High temp", MeterEvent.OTHER),
    CLOCK_SYNC              (2, 2, "Clock sync", MeterEvent.OTHER),
    MAGNETIC_FIELD_FLAG     (2, 3, "Actual Magnetic field flag", MeterEvent.OTHER),
    BATTERY_USAGE_INDICATOR (2, 4, "Battery usage indicator above or critical", MeterEvent.OTHER),
    ACTUAL_BURST            (2, 5, "Actual burst", MeterEvent.OTHER),
    REVERSED_METER          (2, 6, "Reversed meter", MeterEvent.OTHER),
    ACTUAL_BACKFLOW         (2, 7, "Actual back-flow", MeterEvent.OTHER),

    // third error-field (EF3) bits
    EF3_B0                  (3, 0, "Reserved EF3/B0", MeterEvent.OTHER),
    EF3_B1                  (3, 1, "Reserved EF3/B1", MeterEvent.OTHER),
    EF3_B2                  (3, 2, "Reserved EF3/B2", MeterEvent.OTHER),
    EF3_B3                  (3, 3, "Reserved EF3/B3", MeterEvent.OTHER),
    EF3_B4                  (3, 4, "Reserved EF3/B4", MeterEvent.OTHER),
    EF3_B5                  (3, 5, "Reserved EF3/B5", MeterEvent.OTHER),
    EF3_B6                  (3, 6, "Reserved EF3/B6", MeterEvent.OTHER),
    EF3_B7                  (3, 7, "Reserved EF3/B7", MeterEvent.OTHER),

    ;



    private final int ef;
    private final int bit;
    private final String message;
    private final int eventCode;


    ErrorFlagsMapping(int ef, int bit, String message, int eventCode) {
        this.ef = ef;   
        this.bit = bit;
        this.message = message;
        this.eventCode = eventCode;
    }

    public int getEf() {
        return ef;
    }

    public int getBit() {
        return bit;
    }

    public String getMessage() {
        return message;
    }

    public int getEventCode() {
        return eventCode;
    }
}
