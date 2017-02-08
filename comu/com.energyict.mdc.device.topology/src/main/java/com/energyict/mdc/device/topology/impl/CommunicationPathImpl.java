/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.CommunicationPath;

/**
 * Provides an implementation for the {@link CommunicationPath} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-08 (15:21)
 */
public abstract class CommunicationPathImpl implements CommunicationPath {

    private final Device source;
    private final Device target;

    public CommunicationPathImpl(Device source, Device target) {
        super();
        this.source = source;
        this.target = target;
    }


    @Override
    public Device getSource() {
        return source;
    }

    @Override
    public Device getTarget() {
        return target;
    }

}