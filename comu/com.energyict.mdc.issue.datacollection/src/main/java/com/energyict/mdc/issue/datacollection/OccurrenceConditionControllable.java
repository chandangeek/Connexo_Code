package com.energyict.mdc.issue.datacollection;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.issue.datacollection.impl.event.EventDescription;

/**
 * Implementing this interface allows an issue to
 * be created when an occurrence condition within time period is met.
 */
@ProviderType
public interface OccurrenceConditionControllable {

    /**
     * Returns a description of event which is treated as an issue causing event.
     *
     * @return {@link EventDescription}
     */
    EventDescription getIssueCausingEvent();

    /**
     * Returns a description of event which is treated as an issue resolving event.
     *
     * @return {@link EventDescription}
     */
    EventDescription getIssueResolvingEvent();

}
