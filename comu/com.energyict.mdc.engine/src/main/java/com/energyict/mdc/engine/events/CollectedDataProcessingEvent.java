/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.events;

import com.energyict.mdc.issues.Issue;

import java.util.List;

public interface CollectedDataProcessingEvent extends ComServerEvent, LoggingEvent {

    @Override
    default Category getCategory (){
        return Category.COLLECTED_DATA_PROCESSING;
    }

    String getDescription();

    boolean hasIssues();

    List<Issue> getIssues();

}
