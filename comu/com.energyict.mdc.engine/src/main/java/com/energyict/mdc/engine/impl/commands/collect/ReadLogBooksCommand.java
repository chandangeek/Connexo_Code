package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.protocol.api.LogBookReader;

import java.util.List;

/**
 * Command to read {@link com.energyict.mdc.protocol.api.device.BaseLogBook}s from a Device
 *
 * @author sva
 * @since 07/12/12 - 16:26
 */
public interface ReadLogBooksCommand extends ComCommand {

    /**
     * Add a List of {@link LogBookReader} which need to be collected from the device
     *
     * @param logBooksToCollect the {@link LogBookReader}s to read out
     */
    public void addLogBooks(final List<LogBookReader> logBooksToCollect);

}
