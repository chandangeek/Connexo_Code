package com.energyict.protocolimpl.base;

import java.io.IOException;

/**
 * Contains LoadLimiting related functionality
 * <p/>
 * Created by IntelliJ IDEA.
 * User: gna
 * Date: 11-okt-2010
 * Time: 13:47:35
 * To change this template use File | Settings | File Templates.
 */
public interface LoadLimitController {

    /**
     * Write a given threshold value
     *
     * @param threshold the value of the threshold
     */
    void writeThresholdValue(long threshold) throws IOException;

    /**
     * Write the overThresholdDuration time
     *
     * @param seconds the amount of seconds the threshold can be exceeded before disconnecting
     */
    void writeThresholdOverDuration(int seconds) throws IOException;


}
