package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.protocols.util.Calculate;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 3/01/12
 * Time: 15:35
 */

public class ABBA230LoadProfileEntry implements ABBA230ProfileEntry {

    ABBA230RegisterFactory registerFactory;
    ByteArrayInputStream bai;
    int nrOfChannels;
    boolean isValue = false;
    long date;
    int type;
    double[] values = new double[4];
    int status;
    int integrationPeriod;
    boolean isDst;
    byte[] channelMask;
    ProfileConfigRegister profileConfigRegister;

    public void start( ABBA230RegisterFactory registerFactory, ByteArrayInputStream bai, int nrOfChannels)
    throws IOException {

        this.registerFactory = registerFactory;
        this.bai = bai;
        this.nrOfChannels = nrOfChannels;
        this.values = new double[this.nrOfChannels];
        date = 0;
        init();
    }

    public void init()  throws IOException {
        // 1 byte
        type = ProtocolUtils.getVal(bai);

        if( isMarker() ) {

            if( isEndOfData() ) {
                return;
            }
            // 4 bytes
            date = (long)ProtocolUtils.getIntLE(bai)&0xFFFFFFFFL;

            if (isNewDay() || isConfigurationChange()) {
                // 2 bytes Channel configuration
                channelMask = new byte[2];
                channelMask[0] = (byte) bai.read();
                channelMask[1] = (byte) bai.read();

                // 1 byte
                int byte8 = ProtocolUtils.getVal(bai);
                integrationPeriod = registerFactory.getDataType().integrationPeriod.parse((byte) (byte8 & 0x0f));
                isDst = (byte8 & 0x80) > 0;
                profileConfigRegister = new LoadProfileConfigRegister();
                profileConfigRegister.loadConfig(registerFactory, channelMask);
                nrOfChannels = profileConfigRegister.getNumberRegisters();
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

    public int getType() {
        return type;
    }

    public int getIntegrationPeriod() {
        return integrationPeriod;
    }

    public byte[] getChannelmask() {
        return channelMask;
    }

    public ProfileConfigRegister getProfileConfig() {
       return profileConfigRegister;
    }

    public int getNumberOfChannels() {
        return nrOfChannels;
    }

    public boolean isDST() {
        return isDst;
    }

    public int getStatus() {
        return status;
    }

    public double[] getValues() {
        return  values;
    }

    public long getTime() {
        return date;
    }


    public boolean isMarker() {
        return ((type == POWERUP) || (type == CONFIGURATIONCHANGE) || (type == POWERDOWN) ||
                (type == NEWDAY) || (type == TIMECHANGE) || (type == DAYLIGHTSAVING) ||
                (type == PROFILECLEARED) || (type == FORCEDENDOFDEMAND) || (type == ENDOFDATA));
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
        StringBuffer strBuff = new StringBuffer();
        if (!isMarker()) {
            strBuff.append("Demanddata:\n");
            strBuff.append("   Status: 0x"+Integer.toHexString(status)+"\n");
            for(int i=0;i<values.length;i++) {
                strBuff.append("   Channel "+i+": "+values[i]+"\n");
            }
        } else if(isEndOfData()) {
            strBuff.append("End of data\n");
        } else {
            Calendar cal = ProtocolUtils.getCalendar(timeZone,date);
            strBuff.append("Marker: 0x"+Integer.toHexString(type)+" at "+cal.getTime()+" "+date+"\n");
            if (isNewDay() || isConfigurationChange() ) {
                strBuff.append("   ChannelMask: " + getChannelmask().toString());
                strBuff.append("   IntegrationTime: "+Integer.toString(getIntegrationPeriod()));
                strBuff.append("   dst: "+isDST());
            }
        }
        return strBuff.toString();
    }
}