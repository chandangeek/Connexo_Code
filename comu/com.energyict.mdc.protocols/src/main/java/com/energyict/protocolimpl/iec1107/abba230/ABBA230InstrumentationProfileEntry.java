/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

public class ABBA230InstrumentationProfileEntry implements ABBA230ProfileEntry {

    ABBA230RegisterFactory registerFactory;
    ByteArrayInputStream bai;
    int nrOfChannels;
    boolean isValue = false;
    long date;
    int type;
    double[] values = new double[0];
    int[] signOrQuadrant = new int[0];
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
        this.signOrQuadrant = new int[this.nrOfChannels];
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
                // 16 bytes - 2 byte configuration for each channel
                channelMask = new byte[16];
                for (int i = 0; i < 16; i++) {
                    channelMask[i] = (byte) bai.read();
                }

                // 1 byte
                int byte8 = ProtocolUtils.getVal(bai);
                integrationPeriod = registerFactory.getDataType().integrationPeriod.parse((byte) (byte8 & 0x0f));
                isDst = (byte8 & 0x80) > 0;
                profileConfigRegister = new InstrumentationProfileConfigRegister();
                profileConfigRegister.loadConfig(registerFactory, channelMask);
                nrOfChannels = profileConfigRegister.getNumberRegisters();

                // New Day structure specifies still 5 bytes. We just read those bytes out, but don't use them.
                for (int i=0;i<5;i++) {
                    bai.read();
                }
                return;
            }

        } else {
            /** If it is not a marker, it is profile data.
             * This means it starts with status */
            status = type;
            isValue = true;
            for (int i=0;i<nrOfChannels;i++) {
                byte[] valueBCD = new byte[4];

                for (int j=0;j<4; j++) {
                    valueBCD[j] = (byte) bai.read();
                }

                long signAndExponent = ParseUtils.getBCD2Long(valueBCD, 0, 1);
                int sign = (int) (signAndExponent / 10);
                int exponent = (int) signAndExponent % 10;

                long bcd2Long = ParseUtils.getBCD2Long(valueBCD, 1, 3);
                double value = bcd2Long * Math.pow(10, exponent);

                if (sign != 0) {
                    value = -value;
                }
                values[i] = value;
                /* In the normal case, the sign number indicates the sign of the value.
                 * 'Power Factor' channels are an exception to this rule.
                 *  In this case, the sign number indicates the quadrant of the Power Factor angle.
                */
                signOrQuadrant[i] = sign;
            }
        }
    }

    /**
     * This function will take into account the 'sign digit' of all channels who measure the 'Power factor'.
     * Those channels do not have a sign - the sign digit indicates the quadrant.
     *
     * @param channelValueConfigurations    the value configuration of the channels in use.
     */
    public void updatePowerFactorChannels(int[] channelValueConfigurations) {
        for (int i = 0; i< channelValueConfigurations.length; i++) {
            if (channelValueConfigurations[i] != 0) {   // 0 = the channel is not in use.
                if (channelValueConfigurations[i] == 3) {
                    values[i] = Math.abs(values[i]) / 1000;
                } else {
                    signOrQuadrant[i] = 0;
                }
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

    public int[] getSignOrQuadrant() {
        return signOrQuadrant;
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