package com.energyict.mdc.device.data;

import com.energyict.mdc.common.ApplicationComponent;
import com.energyict.mdc.device.config.PartialConnectionTask;

/**
 * Defines the behavior of an {@link ApplicationComponent}
 * that is capable of finding {@link PartialConnectionTask}s.
 * <p>
 * Todo: this interface can and must be removed as soon as JP-809 is resolved
 * </p>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-12 (13:29)
 */
public interface PartialConnectionTaskFactory {

    public PartialConnectionTask findPartialConnectionTask (long id);

}