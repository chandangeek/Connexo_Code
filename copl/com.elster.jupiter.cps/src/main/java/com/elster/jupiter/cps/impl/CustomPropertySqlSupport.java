/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.units.Quantity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides support to generate SQL to obtain raw values from a table holding
 * values of a {@link com.elster.jupiter.cps.CustomPropertySet}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-31 (16:42)
 */
public final class CustomPropertySqlSupport {

    public static List<String> columnNamesFor(ActiveCustomPropertySet activeCustomPropertySet, PropertySpec propertySpec) {
        PersistenceSupport persistenceSupport = activeCustomPropertySet.getCustomPropertySet().getPersistenceSupport();
        if (propertySpec.getValueFactory().getValueType().equals(Quantity.class)) {
            String baseName = persistenceSupport.columnNameFor(propertySpec);
            return Arrays.asList(
                        baseName + "VALUE",
                        baseName + "MULTIPLIER");
        } else {
            return Collections.singletonList(persistenceSupport.columnNameFor(propertySpec));
        }
    }

    public static String toValueSelectClauseExpression(ActiveCustomPropertySet activeCustomPropertySet, PropertySpec propertySpec) {
        PersistenceSupport persistenceSupport = activeCustomPropertySet.getCustomPropertySet().getPersistenceSupport();
        if (propertySpec.getValueFactory().getValueType().equals(Quantity.class)) {
            String baseName = "cps." + persistenceSupport.columnNameFor(propertySpec);
            return baseName + "VALUE * POWER(10, " + baseName + "MULTIPLIER)";
        } else {
            return "cps." + persistenceSupport.columnNameFor(propertySpec);
        }
    }

    // Hide utility class constructor
    private CustomPropertySqlSupport() {}

}