package com.energyict.mdc.engine.events;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.issues.Issue;

/**
 * Copyrights EnergyICT
 * Date: 16/02/2016
 * Time: 9:55
 */
public interface CollectedDataProcessingEvent extends ComServerEvent {

    @Override
    default Category getCategory (){
        return Category.COLLECTED_DATA_PROCESSING;
    }

    String getDescription();
    boolean hasIssue();
    Issue getIssue();

}
