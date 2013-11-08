package com.energyict.protocolimpl.dlms.as220.emeter;

import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.Limiter;
import com.energyict.protocolimpl.base.LoadLimitController;
import com.energyict.protocolimpl.dlms.as220.AS220;

import java.io.IOException;

/**
 * LoadLimit implementation for the AS220 (AM500) devices
 *
 * Created by IntelliJ IDEA.
 * User: gna
 * Date: 11-okt-2010
 * Time: 13:52:56
 * To change this template use File | Settings | File Templates.
 */
public class AS220LoadLimitController implements LoadLimitController {

    private final AS220 as220;

    public AS220LoadLimitController(AS220 as220) {
        this.as220 = as220;
    }

    /**
     * Write a given threshold value
     *
     * @param threshold the value of the threshold
     */
    public void writeThresholdValue(long threshold) throws IOException {
        getLimiter().writeThresholdNormal(new Unsigned32(threshold));
    }

    /**
     * Write the overThresholdDuration time. The seconds should <b>always</b> be a multiple of 5. If not then we will truncate to the nearest
     * highest 5 second multiple.
     *
     * @param seconds the amount of seconds the threshold can be exceeded before disconnecting
     */
    public void writeThresholdOverDuration(int seconds) throws IOException {
        int secondsOfFive = 0;

        if(seconds > 1270){
            this.as220.getLogger().info("Threshold duration was > 1270s (" + seconds + "), duration will be limited to 1270s.");
            seconds = 1270;
    }

        if(seconds%5 == 0){
            secondsOfFive = seconds;
        } else {
            secondsOfFive = seconds-(seconds%5) + 5;
        }
        getLimiter().writeMinOverThresholdDuration(new Unsigned32(secondsOfFive));
    }

    /**
     * Get the private Limiter object
     * @return the current AS220
     * @throws IOException if we have an undefined ObjectReference 
     */
    private Limiter getLimiter() throws IOException {
        return this.as220.getCosemObjectFactory().getLimiter();
    }
}
