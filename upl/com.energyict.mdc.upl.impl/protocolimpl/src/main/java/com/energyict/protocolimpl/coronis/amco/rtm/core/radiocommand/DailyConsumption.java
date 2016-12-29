package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.SamplingPeriod;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 30-apr-2011
 * Time: 23:59:08
 */
public class DailyConsumption extends AbstractRadioCommand {

    public DailyConsumption(PropertySpecService propertySpecService, RTM rtm) {
        super(propertySpecService, rtm);
    }

    private int profileInterval;
    private int numberOfPorts;
    private Integer[] dailyReadingsA = new Integer[5];
    private Integer[] dailyReadingsB = new Integer[5];
    private Integer[] dailyReadingsC = new Integer[5];
    private Integer[] dailyReadingsD = new Integer[5];
    private int[] currentIndexes;
    private Date lastLoggedValue;

    public Date getLastLoggedValue() {
        return lastLoggedValue;
    }

    public int getNumberOfPorts() {
        return numberOfPorts;
    }

    public int[] getCurrentIndexes() {
        return currentIndexes;
    }

    public List<Integer> getDailyReadings(int port) {
        switch (port) {
            case 0:
                return Arrays.asList(dailyReadingsA);
            case 1:
                return Arrays.asList(dailyReadingsB);
            case 2:
                return Arrays.asList(dailyReadingsC);
            case 3:
                return Arrays.asList(dailyReadingsD);
            default:
                return Arrays.asList(dailyReadingsA);
        }
    }

    @Override
    public void parse(byte[] data) throws IOException {
        parse(data, null);
    }

    public void parse(byte[] data, byte[] radioAddress) throws IOException {
        getGenericHeader().setRadioAddress(radioAddress);
        getGenericHeader().parse(data);
        numberOfPorts = getGenericHeader().getOperationMode().readNumberOfPorts();
        int offset = 23;    //Skip the generic header

        TimeZone timeZone = getRTM().getTimeZone();
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }
        lastLoggedValue = TimeDateRTCParser.parse(data, offset, 7, timeZone).getTime();
        offset += 7;

        SamplingPeriod period = new SamplingPeriod(getPropertySpecService(), getRTM());
        period.parse(ProtocolTools.getSubArray(data, offset, offset + 1));
        int multiplier = data[offset + 2] & 0xFF;
        profileInterval = multiplier * period.getSamplingPeriodInSeconds();

        offset += 7; //Skip data logging parameters

        currentIndexes = new int[numberOfPorts];

        for (int portId = 0; portId < numberOfPorts; portId++) {
            currentIndexes[portId] = ProtocolTools.getIntFromBytes(data, offset, 4);
            offset+=4;
        }

        for (int i = 0; i < 5; i++) {
            int value = ProtocolTools.getIntFromBytes(data, offset, 4);
            if (value == -1) {
                throw new WaveFlowException("Daily indexes not stored");
            }
            dailyReadingsA[i] = value;
            offset += 4;
        }

        if (numberOfPorts > 1) {
            for (int i = 0; i < 5; i++) {
                dailyReadingsB[i] = ProtocolTools.getIntFromBytes(data, offset, 4);
                offset += 4;
            }
        }

        if (numberOfPorts > 2) {
            for (int i = 0; i < 5; i++) {
                dailyReadingsC[i] = ProtocolTools.getIntFromBytes(data, offset, 4);
                offset += 4;
            }
        }

        if (numberOfPorts > 3) {
            for (int i = 0; i < 5; i++) {
                dailyReadingsD[i] = ProtocolTools.getIntFromBytes(data, offset, 4);
                offset += 4;
            }
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.DailyReading;
    }

    public int getProfileInterval() {
        return profileInterval;
    }
}
