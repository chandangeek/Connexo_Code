package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.iec1107.abba1140.Calculate;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 3/01/12
 * Time: 15:34
 */

public final class ABBA230InstrumentationProfileEntry extends ABBA230LoadProfileEntry {

    int[] signOrQuadrant = new int[0];

    @Override
    public void start( ABBA230RegisterFactory registerFactory, ByteArrayInputStream bai, int nrOfChannels) throws IOException {
        this.signOrQuadrant = new int[this.nrOfChannels];
        super.start(registerFactory, bai, nrOfChannels);
    }

    @Override
    protected void readNewDayChangeConfValues(ByteArrayInputStream bai, int chMaskSize) throws IOException {
        // 16 bytes - 2 byte configuration for each channel
        super.readNewDayChangeConfValues(bai, 16);
        bai.skip(5);
    }

    @Override
    protected void readValues( ByteArrayInputStream bai ) throws ProtocolException {
        dataStatus = profileEntryType;
        for (int i = 0; i < nrOfChannels; i++) {
            byte[] valueBCD = new byte[4];

            for (int j = 0;j < 4; j++) {
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

    public int[] getSignOrQuadrant() {
        return signOrQuadrant;
    }
}