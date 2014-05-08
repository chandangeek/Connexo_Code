package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.model.ComPort;

/**
 * Models the fact that a {@link com.energyict.mdc.engine.model.ComPort ComPort}
 * <i>(in- or outbound)</i> is a {@link ServerProcess} and is easily
 * wrappable by AOP.
 * <p/>
 * Copyrights EnergyICT
 * Date: 26/10/12
 * Time: 12:07
 */
public interface ComPortServerProcess extends ServerProcess {

    /**
     * Gets the used ComPort.
     *
     * @return The ComPort
     */
    public ComPort getComPort();

    /**
     * The name of the <i>Thread</i> this process is running in.
     *
     * @return The name of the process' thread
     */
    public String getThreadName();

    /**
     * Check whether the persistent {@link ComPort} has changed in the Database and apply if so.
     */
    public void checkAndApplyChanges();

}