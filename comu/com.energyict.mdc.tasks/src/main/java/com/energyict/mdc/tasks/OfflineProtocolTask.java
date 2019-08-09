package com.energyict.mdc.tasks;

import com.energyict.mdc.upl.offline.Offline;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 *  Represents an Offline version of an {@link ProtocolTask} which should contain all necessary information needed to perform the
 * {@link ProtocolTask} without the need to go to the database.
 *
 * @author sva
 * @since 7/10/2016 - 11:40
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface OfflineProtocolTask<T extends ProtocolTask> extends Offline {

}