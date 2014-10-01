package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.DeviceService;

import com.elster.jupiter.nls.Thesaurus;

/**
 * Adds behavior to {@link DeviceService} that is specific
 * to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-28 (11:24)
 */
public interface ServerDeviceService extends DeviceService {

    public Thesaurus getThesaurus();

}