package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.config.ComPort;

import java.time.Instant;

/**
 * Models the fact that a {@link com.energyict.mdc.engine.config.ComPort ComPort}
 * <i>(in- or outbound)</i> is a {@link ServerProcess} and is easily
 * wrappable by AOP.
 * <p/>
 * Copyrights EnergyICT
 * Date: 26/10/12
 * Time: 12:07
 */
interface ComPortServerProcess extends ServerProcess {

    /**
     * Gets the used ComPort.
     *
     * @return The ComPort
     */
    ComPort getComPort();

    /**
     * The name of the <i>Thread</i> this process is running in.
     *
     * @return The name of the process' thread
     */
    String getThreadName();

    /**
     * @return the number of threads used by the process.
     */
    public int getThreadCount();

    /**
     * Gets the instant in time of the last registered activity for this process.
     *
     * @return The instant in time
     */
    Instant getLastActivityTimestamp();

}