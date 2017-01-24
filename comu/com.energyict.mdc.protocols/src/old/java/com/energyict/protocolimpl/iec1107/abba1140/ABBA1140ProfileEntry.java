package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.protocols.util.Calculate;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

/** @author  Koen */

public class ABBA1140ProfileEntry {

    // type attribute:
    // markers
    public static final int POWERUP=0xE5;
    public static final int CONFIGURATIONCHANGE=0xE8;
    public static final int POWERDOWN=0xE6;
    public static final int NEWDAY=0xE4;
    public static final int TIMECHANGE=0xEA;
    public static final int DAYLIGHTSAVING=0xED;
    public static final int LOADPROFILECLEARED=0xEB;
    public static final int FORCEDENDOFDEMAND=0xE9;
    // when last packet does not contain 64 (normal mode) or 256 (DS mode) bytes
    public static final int ENDOFDATA=0xFF;

    ABBA1140RegisterFactory registerFactory;
    ByteArrayInputStream bai;
    int nrOfChannels;
    boolean isValue = false;
    long date;
    int type;
    long[] values = new long[4];
    int status;
    int integrationPeriod;
    boolean isDst;
    int channelMask;
    LoadProfileConfigRegister loadProfileConfigRegister;

    ABBA1140ProfileEntry( ABBA1140RegisterFactory registerFactory, ByteArrayInputStream bai, int nrOfChannels)
    throws IOException {

        this.registerFactory = registerFactory;
        this.bai = bai;
        this.nrOfChannels = nrOfChannels;
        date = 0;

        init();

    }

    void init( )  throws IOException {
        // 1 byte
        type = ProtocolUtils.getVal(bai);

        if( isMarker() ) {

            if( isEndOfData() ) {
                return;
            }
            // 4 bytes
            date = (long)ProtocolUtils.getIntLE(bai)&0xFFFFFFFFL;

            if( isNewDay() || isConfigurationChange() ) {
                // 2 bytes
                channelMask = (int)ProtocolUtils.getShort(bai)&0xFFFF;
                // 1 byte
                int byte8 = ProtocolUtils.getVal(bai);
                integrationPeriod = registerFactory.getDataType().integrationPeriod.parse((byte)(byte8&0x0f));
                isDst = (byte8&0x80) > 0;
                loadProfileConfigRegister = new LoadProfileConfigRegister( registerFactory, channelMask );
                nrOfChannels = loadProfileConfigRegister.getNumberRegisters();
                return;
            }

        } else {
            /** If it is not a marker, it is profile data.
             * This means it starts with status */
            status = type;
            isValue = true;
            for (int i=0;i<nrOfChannels;i++) {
                long val = (int)Long.parseLong(Long.toHexString(ProtocolUtils.getLong(bai,3)));
                values[i] = (val/10) * Calculate.exp(val % 10);
            }
        }
    }

    protected int getType() {
        return type;
    }

    protected int getIntegrationPeriod() {
        return integrationPeriod;
    }

    protected int getChannelmask() {
        return channelMask;
    }

    LoadProfileConfigRegister getLoadProfileConfig() {
       return loadProfileConfigRegister;
    }

    protected int getNumberOfChannels() {
        return nrOfChannels;
    }

    protected boolean isDST() {
        return isDst;
    }

    protected int getStatus() {
        return status;
    }

    protected long[] getValues() {
        return  values;
    }

    protected long getTime() {
        return date;
    }


    boolean isMarker() {
        return ((type == POWERUP) || (type == CONFIGURATIONCHANGE) || (type == POWERDOWN) ||
                (type == NEWDAY) || (type == TIMECHANGE) || (type == DAYLIGHTSAVING) ||
                (type == LOADPROFILECLEARED) || (type == FORCEDENDOFDEMAND) || (type == ENDOFDATA));
    }

    boolean isNewDay() {
        return type == NEWDAY;
    }

    boolean isConfigurationChange(){
        return type == CONFIGURATIONCHANGE;
    }

    boolean isEndOfData(){
        return type == ENDOFDATA;
    }

    public String toString(TimeZone timeZone, boolean dst) {
        StringBuilder strBuff = new StringBuilder();
        if (!isMarker()) {
            strBuff.append("Demanddata:\n");
            strBuff.append("   Status: 0x").append(Integer.toHexString(status)).append("\n");
            for(int i=0;i<values.length;i++) {
                strBuff.append("   Channel ").append(i).append(": ").append(values[i]).append("\n");
            }
        } else if(isEndOfData()) {
            strBuff.append("End of data\n");
        } else {
            Calendar cal = ProtocolUtils.getCalendar(timeZone,date);
            strBuff.append("Marker: 0x").append(Integer.toHexString(type)).append(" at ").append(cal.getTime()).append(" ").append(date).append("\n");
            if (isNewDay() || isConfigurationChange() ) {
                strBuff.append("   ChannelMask: 0x").append(Integer.toHexString(getChannelmask()));
                strBuff.append("   IntegrationTime: ").append(Integer.toString(getIntegrationPeriod()));
                strBuff.append("   dst: ").append(isDST());
            }
        }
        return strBuff.toString();
    }

}
