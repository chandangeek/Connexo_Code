/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides code reuse opportunities for components that
 * need to convert themselves to CompositeDataSupport.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (16:59)
 */
abstract class CanConvertToCompositeDataSupport {

    private List<CompositeDataItemAccessor> accessors;
    private String[] accessorNames;

    interface ValueProvider {
        Object getValue();
    }

    CanConvertToCompositeDataSupport() {
        super();
    }

    protected abstract CompositeType getCompositeType ();

    /**
     * Converts this to CompositeData.
     *
     * @return The CompositeData
     */
    CompositeData toCompositeData() {
        this.ensureAccessors();
        try {
            return new CompositeDataSupport(
                        this.getCompositeType(),
                        this.accessorNames,
                        this.values());
        }
        catch (OpenDataException e) {
            throw CodingException.compositeDataCreation(this.getClass(), e, MessageSeeds.COMPOSITE_TYPE_CREATION);
        }
    }

    private Object[] values () {
        this.ensureAccessors();
        Object[] values = new Object[this.accessors.size()];
        int valueIndex = 0;
        for (CompositeDataItemAccessor accessor : this.accessors) {
            values[valueIndex] = accessor.getValue();
            valueIndex++;
        }
        return values;
    }

    /**
     * Ensures that the accessors map is initialize properly,
     * i.e. that all CompositeData items have an accessor.
     */
    private void ensureAccessors () {
        if (this.accessors == null) {
            this.accessors = new ArrayList<>();
            this.initializeAccessors(this.accessors);
            this.accessorNames = new String[this.accessors.size()];
            int accessorNameIndex = 0;
            for (CompositeDataItemAccessor accessor : this.accessors) {
                this.accessorNames[accessorNameIndex] = accessor.getItemName();
                accessorNameIndex++;
            }
        }
    }

    protected abstract void initializeAccessors (List<CompositeDataItemAccessor> accessors);

    class CompositeDataItemAccessor implements ValueProvider {
        private String itemName;
        private ValueProvider valueProvider;

        CompositeDataItemAccessor(String itemName, ValueProvider valueProvider) {
            super();
            this.itemName = itemName;
            this.valueProvider = valueProvider;
        }

        String getItemName() {
            return itemName;
        }

        @Override
        public Object getValue () {
            return this.valueProvider.getValue();
        }

    }

}