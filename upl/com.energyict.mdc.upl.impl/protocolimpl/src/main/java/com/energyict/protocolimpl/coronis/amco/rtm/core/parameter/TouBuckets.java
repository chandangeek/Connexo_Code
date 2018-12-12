package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 8-apr-2011
 * Time: 14:54:59
 */
public class TouBuckets extends AbstractParameter {

    TouBuckets(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    private int numberOfTouBuckets = 0;
    private int startHourTOU1 = 0;
    private int startHourTOU2 = 0;
    private int startHourTOU3 = 0;
    private int startHourTOU4 = 0;
    private int startHourTOU5 = 0;
    private int startHourTOU6 = 0;

    public void setNumberOfTouBuckets(int numberOfTouBuckets) {
        this.numberOfTouBuckets = numberOfTouBuckets;
    }

    public void setStartHour(int number, int hour) {
        if (number == 1) {
            startHourTOU1 = hour;
        }
        if (number == 2) {
            startHourTOU2 = hour;
        }
        if (number == 3) {
            startHourTOU3 = hour;
        }
        if (number == 4) {
            startHourTOU4 = hour;
        }
        if (number == 5) {
            startHourTOU5 = hour;
        }
        if (number == 6) {
            startHourTOU6 = hour;
        }
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.TouBuckets;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        if ((data[0] & 0xFF) == 0xFF) {
            throw new WaveFlowException("Error writing the TOU bucket start hours, make sure there are no window overlaps");
        }
        numberOfTouBuckets = data[0] & 0xFF;
        startHourTOU1 = data[1] & 0xFF;
        startHourTOU2 = data[2] & 0xFF;
        startHourTOU3 = data[3] & 0xFF;
        startHourTOU4 = data[4] & 0xFF;
        startHourTOU5 = data[5] & 0xFF;
        startHourTOU6 = data[6] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) numberOfTouBuckets, (byte) startHourTOU1, (byte) startHourTOU2, (byte) startHourTOU3, (byte) startHourTOU4, (byte) startHourTOU5, (byte) startHourTOU6};
    }

    public void setStartHours(int[] startHours) {
        int i = 1;
        for (int startHour : startHours) {
            setStartHour(i, startHour);
            i++;
        }
    }
}
