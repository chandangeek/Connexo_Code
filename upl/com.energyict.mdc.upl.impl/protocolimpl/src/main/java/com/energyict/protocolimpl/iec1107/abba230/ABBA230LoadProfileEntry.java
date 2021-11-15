package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.iec1107.abba1140.Calculate;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

enum ParsingLPEntryStage
{
    StageType,
    StageDate,
    StageNewDayChangeConfValues;
}

/**
 * Copyrights EnergyICT
 * User: Dmitry Borisov
 * Date: 12/11/21
 * Time: 18:00
 */
public class ABBA230LoadProfileEntry implements ABBA230ProfileEntry {

    /** */
    ABBA230RegisterFactory registerFactory;
    /** */
    int nrOfChannels;
    /** Unix-epoch based date */
    long date;
    /** Profile entry type if it is marker */
    int profileEntryType;
    /** Profile entry type if it is values */
    int dataStatus; // ? if not marker store this
    /** If interval type is not marker it's should contain values */
    double[] values = new double[4];
    /** ??? */
    int integrationPeriod;
    /** ??? */
    boolean isDst;
    /** */
    byte[] channelMask;
    /** */
    ProfileConfigRegister profileConfigRegister;

    @Override
    public void start( ABBA230RegisterFactory registerFactory, ByteArrayInputStream bai, int nrOfChannels) throws IOException {
        this.registerFactory    = registerFactory;
        this.nrOfChannels       = nrOfChannels;
        this.values             = new double[this.nrOfChannels];
        date                    = 0;
        dataStatus              = -1;
        profileEntryType        = -1;
        init(bai);
    }

    protected void readNewDayChangeConfValues(ByteArrayInputStream bai, int chMaskSize) throws IOException {
        // 2 bytes Channel configuration
        channelMask = new byte[chMaskSize];
        bai.read(channelMask, 0, chMaskSize);

        // 1 byte
        int byte8               = ProtocolUtils.getVal(bai);
        integrationPeriod       = registerFactory.getDataType().integrationPeriod.parse((byte) (byte8 & 0x0f));
        isDst                   = (byte8 & 0x80) > 0;
        profileConfigRegister   = new LoadProfileConfigRegister();
        profileConfigRegister.loadConfig(registerFactory, channelMask);
        nrOfChannels = profileConfigRegister.getNumberRegisters();
    }

    protected void readValues( ByteArrayInputStream bai ) throws ProtocolException {
        // If it is not a marker, it is profile data. This means it starts with status
        dataStatus = profileEntryType;
        for (int i = 0; i < nrOfChannels; i++) {
            long val = (int)Long.parseLong(Long.toHexString(ProtocolUtils.getLong(bai,3))); // ???
            values[i] = (val/10) * Calculate.exp(val%10);
        }
    }

    protected void finiteStateDataParser(ByteArrayInputStream bai, ParsingLPEntryStage stage ) throws IOException {
        switch (stage) {
            case StageType:
                profileEntryType = ProtocolUtils.getVal(bai);
                if(Marker.fromInt(profileEntryType) != null) {
                    if (Marker.ENDOFDATA.getMarker() == profileEntryType) {
                        return;
                    }
                    finiteStateDataParser(bai, ParsingLPEntryStage.StageDate); // after type should follow date.
                }
                else {
                    readValues(bai);
                }
                break;
            case StageDate:
                date = (long) ProtocolUtils.getIntLE(bai) & 0xFFFFFFFFL;
                finiteStateDataParser(bai, ParsingLPEntryStage.StageNewDayChangeConfValues); // wait for values after date
                break;
            case StageNewDayChangeConfValues:
                if (Marker.NEWDAY.getMarker() == profileEntryType ||
                    Marker.CONFIGURATIONCHANGE.getMarker() == profileEntryType) {
                    readNewDayChangeConfValues(bai, 2);
                }
                break;
        }
    }

    protected void init(ByteArrayInputStream bai) throws IOException {
        finiteStateDataParser(bai, ParsingLPEntryStage.StageType);
    }

    @Override
    public int getProfileEntryType() {
        return profileEntryType;
    }

    @Override
    public int getIntegrationPeriod() {
        return integrationPeriod;
    }

    @Override
    public byte[] getChannelMask() {
        return channelMask;
    }

    @Override
    public ProfileConfigRegister getProfileConfig() {
       return profileConfigRegister;
    }

    @Override
    public int getNumberOfChannels() {
        return nrOfChannels;
    }

    @Override
    public boolean isDST() {
        return isDst;
    }

    @Override
    public int getDataStatus() {
        return dataStatus;
    }

    @Override
    public double[] getValues() {
        return  values;
    }

    @Override
    public long getTime() {
        return date;
    }

    @Override
    public String toString(TimeZone timeZone, boolean dst) {
        StringBuffer strBuff = new StringBuffer();
        Marker entry_marker = Marker.fromInt(profileEntryType);

        if (null == entry_marker ) {
            strBuff.append("Demand data:\n");
            strBuff.append("   Status: 0x" + Integer.toHexString(dataStatus) + "\n");
            for(int i = 0; i < values.length; i++) {
                strBuff.append("   Channel " + i + ": " + values[i] + "\n");
            }
        } else if(Marker.ENDOFDATA == entry_marker) {
            strBuff.append("End of data\n");
        } else {
            Calendar cal = ProtocolUtils.getCalendar(timeZone, date);
            strBuff.append("Marker: 0x" + Integer.toHexString(entry_marker.getMarker()) + " at " + cal.getTime() + " " + date + "\n");
            if (Marker.NEWDAY == entry_marker || Marker.CONFIGURATIONCHANGE == entry_marker) {
                strBuff.append("   ChannelMask: "       + getChannelMask().toString());
                strBuff.append("   IntegrationTime: "   + getIntegrationPeriod());
                strBuff.append("   dst: "               + isDST());
            }
        }
        return strBuff.toString();
    }
}