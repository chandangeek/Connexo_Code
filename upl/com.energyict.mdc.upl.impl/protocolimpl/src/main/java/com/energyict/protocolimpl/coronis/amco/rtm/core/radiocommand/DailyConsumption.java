package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.OperatingMode;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.SamplingPeriod;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 30-apr-2011
 * Time: 23:59:08
 */
public class DailyConsumption extends AbstractRadioCommand {

    public DailyConsumption(RTM rtm) throws IOException {
        super(rtm);
    }

    private int profileInterval;
    private int numberOfPorts;
    private Integer[] dailyReadingsA = new Integer[5];
    private Integer[] dailyReadingsB = new Integer[5];
    private Integer[] dailyReadingsC = new Integer[5];
    private Integer[] dailyReadingsD = new Integer[5];
    private Date lastLoggedValue;

    public Date getLastLoggedValue() {
        return lastLoggedValue;
    }

    public int getNumberOfPorts() {
        return numberOfPorts;
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
        operationMode = ProtocolTools.getIntFromBytes(data, 1, 2);
        numberOfPorts = new OperatingMode(getRTM(), operationMode).readNumberOfPorts();
        int offset = 23;    //Skip the generic header

        lastLoggedValue = TimeDateRTCParser.parse(data, offset, 7, getRTM().getTimeZone()).getTime();
        offset += 7;

        SamplingPeriod period = new SamplingPeriod(getRTM());
        period.parse(ProtocolTools.getSubArray(data, offset, offset + 1));
        int multiplier = data[offset + 2] & 0xFF;
        profileInterval = multiplier * period.getSamplingPeriodInSeconds();

        offset += 7; //Skip data logging parameters

        offset += 4 * numberOfPorts;     //The current values per port are sent (each 4 bytes)

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
