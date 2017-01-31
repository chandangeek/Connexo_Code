/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.pluggable.Pluggable;
import com.energyict.mdc.pluggable.PluggableClassType;

/**
 * Models the exceptional situation that occurs when a pluggable
 * class is being registered as a particular type
 * but the class does not actually implement the
 * interface that relates to the pluggable class type.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (10:11)
 */
public class PluggableClassLacksRelatedInterfaceException extends LocalizedException {

    public <PI extends Pluggable> PluggableClassLacksRelatedInterfaceException(Thesaurus thesaurus, PluggableClassType type, Class<PI> pluggableInterface, Pluggable pluggable) {
        super(
            thesaurus,
            MessageSeeds.PLUGGABLE_CLASS_LACKS_RELATED_INTERFACE,
            thesaurus.getString(type.name(), type.name()),
            pluggableInterface.getCanonicalName(),
            pluggable.getClass().getCanonicalName());
        this.set("pluggableClassType", type);
        this.set("pluggableInterface", pluggableInterface);
        this.set("pluggableClass", pluggable.getClass());
    }

}