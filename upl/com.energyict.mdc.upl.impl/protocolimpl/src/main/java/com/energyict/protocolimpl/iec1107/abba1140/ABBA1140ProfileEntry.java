package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.protocol.Calculate;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/** @author  Koen */

public class ABBA1140ProfileEntry {
    
    // type attribute:
    // markers
    static public final int POWERUP=0xE5;
    static public final int CONFIGURATIONCHANGE=0xE8;
    static public final int POWERDOWN=0xE6;
    static public final int NEWDAY=0xE4;
    static public final int TIMECHANGE=0xEA;
    static public final int DAYLIGHTSAVING=0xED;
    static public final int LOADPROFILECLEARED=0xEB;
    static public final int FORCEDENDOFDEMAND=0xE9;
    // when last packet does not contain 64 (normal mode) or 256 (DS mode) bytes
    static public final int ENDOFDATA=0xFF;
    private final boolean useExtendedProfileStatus;

    protected ABBA1140RegisterFactory registerFactory;
    protected ByteArrayInputStream bai;
    protected int nrOfChannels;
    protected boolean isValue = false;
    protected long date;
    protected int type;
    protected long[] values = new long[8];
    protected int status;
    protected int extendedStatus;
    protected int integrationPeriod;
    protected boolean isDst;
    protected int channelMask;
    protected LoadProfileConfigRegister loadProfileConfigRegister;
    protected List intervalValues = null;

    public ABBA1140ProfileEntry( ABBA1140RegisterFactory registerFactory, ByteArrayInputStream bai, int nrOfChannels, boolean useExtendedProfileStatus)
    throws IOException {
        
        this.registerFactory = registerFactory;
        this.bai = bai;
        this.nrOfChannels = nrOfChannels;
        this.date = 0;
        this.useExtendedProfileStatus = useExtendedProfileStatus;

        init();
    }
    
    private void init( )  throws IOException {
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
            if (useExtendedProfileStatus) {
                extendedStatus = ProtocolUtils.getVal(bai);
                status = ProtocolUtils.getVal(bai);
            } else {
                status = type;
            }
            isValue = true;
            for (int i = 0; i < nrOfChannels; i++) {
                long val = (int) Long.parseLong(Long.toHexString(ProtocolUtils.getLong(bai, 3)));
                values[i] = (val / 10) * Calculate.exp(val % 10);
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

    protected int getExtendedStatus() {
        return extendedStatus;
    }

    protected long[] getValues() {
        return  values;
    }
    
    protected long getTime() {
        return date;
    }
    
    
    protected boolean isMarker() {
        return ((type == POWERUP) || (type == CONFIGURATIONCHANGE) || (type == POWERDOWN) ||
                (type == NEWDAY) || (type == TIMECHANGE) || (type == DAYLIGHTSAVING) ||
                (type == LOADPROFILECLEARED) || (type == FORCEDENDOFDEMAND) || (type == ENDOFDATA));
    }
    
    protected boolean isNewDay() {
        return type == NEWDAY;
    }
    
    protected boolean isConfigurationChange(){
        return type == CONFIGURATIONCHANGE;
    }
    
    protected boolean isEndOfData(){
        return type == ENDOFDATA;
    }
    
    public String toString(TimeZone timeZone, boolean dst) {
        StringBuffer strBuff = new StringBuffer();
        if (!isMarker()) {
            strBuff.append("Demanddata:\n");
            strBuff.append("   Status: 0x"+Integer.toHexString(status)+"\n");
            if (useExtendedProfileStatus) {
                strBuff.append("   ExtendedStatus: 0x" + Integer.toHexString(extendedStatus) + "\n");
            }
            for(int i=0;i<values.length;i++) {
                strBuff.append("   Channel "+i+": "+values[i]+"\n");
            }
        } else if(isEndOfData()) {
            strBuff.append("End of data\n");
        } else {
            Calendar cal = ProtocolUtils.getCalendar(timeZone,date);
            strBuff.append("Marker: 0x"+Integer.toHexString(type)+" at "+cal.getTime()+" "+date+"\n");
            if (isNewDay() || isConfigurationChange() ) {
                strBuff.append("   ChannelMask: 0x"+Integer.toHexString(getChannelmask()));
                strBuff.append("   IntegrationTime: "+Integer.toString(getIntegrationPeriod()));
                strBuff.append("   dst: "+isDST());
            }
        }
        return strBuff.toString();
    }
    
}
