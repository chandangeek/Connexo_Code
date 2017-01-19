package com.energyict.mdc.engine.impl.core.logging;

import com.energyict.mdc.engine.impl.logging.Configuration;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.ComChannel;

/**
 * Defines all log messages for the {@link ComChannel} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-14 (09:09)
 */
public interface ComChannelLogger {

    /**
     * Logs that the specified bytes in hex format have
     * been written to a {@link ComChannel}.
     */
    @Configuration(format = "TX {0}", logLevel = LogLevel.TRACE)
    void bytesWritten(String hexBytes);

    /**
     * Logs that the specified bytes in hex format have
     * been read from a {@link ComChannel}.
     */
    @Configuration(format = "RX {0}", logLevel = LogLevel.TRACE)
    void bytesRead(String hexBytes);

}