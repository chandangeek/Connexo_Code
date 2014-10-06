package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;

/**
 * Adds behavior to {@link LoadProfileService} that is specific
 * to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-03 (11:05)
 */
public interface ServerLoadProfileService extends LoadProfileService, ReferencePropertySpecFinderProvider {
}