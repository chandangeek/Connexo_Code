package com.energyict.mdc.protocol.api.tasks.support;

import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;

import java.util.List;

/**
 * Provides functionality to collect LogBooks of a Device.
 */
public interface DeviceLogBookSupport {

    /**
     * Collect the meter events from the given <code>LogBooks</code> based on their last logbook reading.
     * If for some reason a <code>LogBook</code> is not supported, a proper {@link com.energyict.mdc.protocol.api.device.data.ResultType ResultType}
     * <b>and</b> {@link com.energyict.mdc.issues.Issue Issue} should be returned so proper logging of this action can be performed.
     * <p/>
     * In essence, the size of the returned <code>List</code> should be the same as the size of the given argument <code>List</code>.
     *
     * @param logBooks a required List of {@link LogBookReader}s to read from the Device.
     * @return a list of {@link CollectedLogBook}s
     */
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks);

}