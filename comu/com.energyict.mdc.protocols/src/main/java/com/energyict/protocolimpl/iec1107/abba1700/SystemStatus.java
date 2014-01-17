/*
 * SystemStatus.java
 *
 * Created on 15 juni 2004, 10:19
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
/**
 *
 * @author  Koen
 */
public class SystemStatus {

    long value;

    String[] flags = new String[]{
                "Backup register set corrupt",
                "Normal register backup used",
                "OB batt fail",
                "OB transient reset",
                "OB device failure",
                "Meter powerdown event",
                "Last powerdown incomplete",
                "Billing reset",
                "Powerup battery failure",
                "Elapsed batt failure",
                "ASIC intB event",
                "ASIC device failure",
                "RTC not incrementing",
                "RTC device failure",
                "Module B hotswap detected",
                "Module A hotswap detected",
                "Meter comms write session",
                "Reverse run",
                "Phase C overcurent",
                "Phase B overcurent",
                "Phase A overcurent",
                "Phase C failure",
                "Phase B failure",
                "Phase A failure",
                "spare",
                "spare",
                "spare",
                "spare",
                "spare",
                "Meter comms write event",
                "Time synchronisation",
                "Meter transient reset"};



    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("getValue()="+Long.toHexString(getValue())+"\n");
        for (int i=0;i<32;i++) {
           if (((getValue()&0xFFFFFF07L) & (0x01<<i))==(0x01<<i)) {
              strBuff.append(flags[31-i]+"\n");
           }
        }
        return strBuff.toString();
    }

    /** Creates a new instance of SystemStatus */
    public SystemStatus(byte[] data) throws IOException {
       setValue(ProtocolUtils.getIntLE(data,0,4));
    }

    /**
     * Getter for property value.
     * @return Value of property value.
     */
    public long getValue() {
        return value;
    }

    /**
     * Setter for property value.
     * @param value New value of property value.
     */
    public void setValue(long value) {
        this.value = value;
    }

}
