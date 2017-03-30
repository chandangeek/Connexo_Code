/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface RegisterType extends MeasurementType {

    /**
     * Returns the <code>RegisterGroup</code> the receiver belongs to
     *
     * @return the <code>RegisterGroup</code> the receiver belongs to
     */
    public List<RegisterGroup> getRegisterGroups();
}
