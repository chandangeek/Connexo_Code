package com.energyict.mdc.dashboard;

import com.energyict.mdc.device.config.DeviceType;

/**
 * Models one row of a {@link CommunicationTaskHeatMap} that focusses
 * on reporting {@link ComCommandCompletionCodeOverview}
 * for a single target.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-25 (15:03)
 */
public interface CommunicationTaskHeatMapRow extends Iterable<ComCommandCompletionCodeOverview> {

    public DeviceType getTarget();

}