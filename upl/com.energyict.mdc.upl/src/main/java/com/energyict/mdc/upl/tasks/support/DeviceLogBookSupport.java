package com.energyict.mdc.upl.tasks.support;

import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.Issue;

import com.energyict.protocol.LogBookReader;

import java.util.List;

/**
 * Provides functionality to collect LogBooks of a Device.
 */
public interface DeviceLogBookSupport {

    /**
     * Collect the meter events from the given <code>LogBooks</code> based on their {@link com.energyict.mdc.upl.meterdata.LogBook#getLastReading()}.
     * If for some reason a <code>LogBook</code> is not supported, a proper {@link ResultType resultType} <b>and</b>
     * {@link Issue issue} should be returned so proper logging of this action can be performed.
     * <p/>
     * In essence, the size of the returned <code>List</code> should be the same as the size of the given argument <code>List</code>.
     *
     * @param logBooks a required List of {@link LogBookReader}s to read from the Device.
     * @return a list of {@link CollectedLogBook}s
     */
    List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks);

}