/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable;

import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

/**
 * Models the required behavior of a component that is depending on
 * the creation/updating/deletion of {@link DeviceProtocolPluggableClass}es.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (16:52)
 */
public interface DeviceProtocolPluggableClassDependent {

    /**
     * Notifies this dependent that the specified {@link DeviceProtocolPluggableClass}
     * is about to be deleted and can throw a {@link com.elster.jupiter.util.exception.BaseException}
     * to veto the deletion.
     *
     * @param obsolete The DeviceProtocolPluggableClass
     */
    public void notifyDelete (DeviceProtocolPluggableClass obsolete);

}