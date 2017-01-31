/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.util.Set;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to save properties on a {@link com.energyict.mdc.pluggable.PluggableClass}
 * that are not known to the PluggableClass, i.e. the PluggableClass
 * does not have {@link com.energyict.mdc.dynamic.PropertySpec}s for these properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-24 (15:00)
 */
public class UnknownPluggableClassPropertiesException extends LocalizedException {

    public UnknownPluggableClassPropertiesException(Thesaurus thesaurus, Set<String> unsupportedPropertyNames, String className) {
        super(thesaurus, MessageSeeds.NOT_A_PLUGGABLE_PROPERTY, setToString(unsupportedPropertyNames), className);
    }

    private static String setToString(Set<String> propertyNames) {
        boolean notFirst = false;
        StringBuilder builder = new StringBuilder();
        for (String propertyName : propertyNames) {
            if (notFirst) {
                builder.append(", ");
            }
            builder.append(propertyName);
            notFirst = true;
        }
        return builder.toString();
    }

}