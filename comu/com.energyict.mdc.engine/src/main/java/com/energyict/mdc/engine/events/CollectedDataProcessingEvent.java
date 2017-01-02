package com.energyict.mdc.engine.events;

import com.energyict.mdc.upl.issue.Issue;

import java.util.List;

/**
 * {@link ComServerEvent} related to data storage
 * Published once a DeviceCommand has been executed and the data coming from the device has been stored in the database.
 *
 * Copyrights EnergyICT
 * Date: 16/02/2016
 * Time: 9:55
 */
public interface CollectedDataProcessingEvent extends ComServerEvent, LoggingEvent {

    @Override
    default Category getCategory (){
        return Category.COLLECTED_DATA_PROCESSING;
    }

    String getDescription();

    boolean hasIssues();

    List<Issue> getIssues();

}
