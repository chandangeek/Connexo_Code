package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;

/**
 * Adds behavior to {@link LogBookService} that is specific
 * to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-03 (11:05)
 */
public interface ServerLogBookService extends LogBookService, ReferencePropertySpecFinderProvider {
}