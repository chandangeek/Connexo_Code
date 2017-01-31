/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.comserver.logging;

import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link DescriptionBuilder} interface.
 * Uses the following defaults:
 * <ul>
 * <li>Prefixes the description with the simple class name of the object</li>
 * <li>Properties are separated by a ";"</li>
 * <li>Values of multi-value properties are separated by a ","</li>
 * <li>All properties are grouped by curly braces ({})</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-25 (09:42)
 */
public class DescriptionBuilderImpl implements DescriptionBuilder {

    /**
     * The separator between the name of a property and the value.
     */
    private static final String PROPERTY_NAME_VALUE_SEPARATOR = ": ";

    /**
     * The separator between the different properties.
     */
    private static final String PROPERTY_SEPARATOR = "; ";

    private String title;
    private List<PropertyDescriptionBuilder> propertyBuilders = new ArrayList<>();

    public DescriptionBuilderImpl (CanProvideDescriptionTitle target) {
        super();
        this.title = target.getDescriptionTitle();
    }

    @Override
    public void addLabel (String label) {
        this.propertyBuilders.add(new SimplePropertyDescriptionBuilder(new StringBuilder(label)));
    }

    @Override
    public StringBuilder addProperty (String propertyName) {
        StringBuilder propertyBuilder = new StringBuilder(propertyName);
        propertyBuilder.append(PROPERTY_NAME_VALUE_SEPARATOR);
        this.propertyBuilders.add(new SimplePropertyDescriptionBuilder(propertyBuilder));
        return propertyBuilder;
    }

    @Override
    public void addFormattedProperty (String propertyName, String propertyFormatPattern, Object... propertyValueParameters) {
        this.addProperty(propertyName).append(MessageFormat.format(propertyFormatPattern, propertyValueParameters));
    }

    @Override
    public PropertyDescriptionBuilder addListProperty (String propertyName) {
        PropertyDescriptionBuilderImpl propertyBuilder = new PropertyDescriptionBuilderImpl(propertyName);
        this.propertyBuilders.add(propertyBuilder);
        return propertyBuilder;
    }

    @Override
    public String toString () {
        Holder<String> separator = HolderBuilder.first("").andThen(PROPERTY_SEPARATOR);
        StringBuilder builder = new StringBuilder(this.title);
        if (!this.propertyBuilders.isEmpty()) {
            builder.append(" {");
            for (PropertyDescriptionBuilder propertyBuilder : this.propertyBuilders) {
                builder.append(separator.get());
                builder.append(propertyBuilder);
            }
            builder.append("}");
        }
        return builder.toString();
    }

    private class SimplePropertyDescriptionBuilder implements PropertyDescriptionBuilder {
        private StringBuilder builder;

        private SimplePropertyDescriptionBuilder (StringBuilder builder) {
            super();
            this.builder = builder;
        }

        @Override
        public String toString () {
            return this.builder.toString();
        }

        @Override
        public PropertyDescriptionBuilder next () {
            return this;
        }

        @Override
        public PropertyDescriptionBuilder append (CharSequence s) {
            this.builder.append(s);
            return this;
        }

        @Override
        public PropertyDescriptionBuilder append (String s) {
            this.builder.append(s);
            return this;
        }

        @Override
        public PropertyDescriptionBuilder append (Object o) {
            this.builder.append(o);
            return this;
        }

        @Override
        public PropertyDescriptionBuilder append (boolean b) {
            this.builder.append(b);
            return this;
        }

        @Override
        public PropertyDescriptionBuilder append (char c) {
            this.builder.append(c);
            return this;
        }

        @Override
        public PropertyDescriptionBuilder append (int i) {
            this.builder.append(i);
            return this;
        }

        @Override
        public PropertyDescriptionBuilder append (long l) {
            this.builder.append(l);
            return this;
        }

    }

    private class SingleValuePropertyDescriptionBuilder extends SimplePropertyDescriptionBuilder {
        private PropertyDescriptionBuilderImpl owner;
        private boolean notMarked = true;

        private SingleValuePropertyDescriptionBuilder (StringBuilder builder, PropertyDescriptionBuilderImpl owner) {
            super(builder);
            this.owner = owner;
        }

        @Override
        public PropertyDescriptionBuilder append (CharSequence s) {
            this.markInUse();
            super.append(s);
            return this.owner;
        }

        @Override
        public PropertyDescriptionBuilder append (String s) {
            this.markInUse();
            super.append(s);
            return this.owner;
        }

        @Override
        public PropertyDescriptionBuilder append (Object o) {
            this.markInUse();
            super.append(o);
            return this.owner;
        }

        @Override
        public PropertyDescriptionBuilder append (boolean b) {
            this.markInUse();
            super.append(b);
            return this.owner;
        }

        @Override
        public PropertyDescriptionBuilder append (char c) {
            this.markInUse();
            super.append(c);
            return this.owner;
        }

        @Override
        public PropertyDescriptionBuilder append (int i) {
            this.markInUse();
            super.append(i);
            return this.owner;
        }

        @Override
        public PropertyDescriptionBuilder append (long l) {
            this.markInUse();
            super.append(l);
            return this.owner;
        }

        private void markInUse () {
            if (this.notMarked) {
                this.owner.add(this);
            }
            this.notMarked = false;
        }
    }
    private class PropertyDescriptionBuilderImpl implements PropertyDescriptionBuilder {
        private String propertyName;
        private List<SingleValuePropertyDescriptionBuilder> valueBuilders = new ArrayList<>();
        private SingleValuePropertyDescriptionBuilder current;

        private PropertyDescriptionBuilderImpl (String propertyName) {
            super();
            this.propertyName = propertyName;
            this.current = new SingleValuePropertyDescriptionBuilder(new StringBuilder(), this);
        }

        private void add (SingleValuePropertyDescriptionBuilder builder) {
            this.valueBuilders.add(builder);
        }

        @Override
        public PropertyDescriptionBuilder next () {
            this.current = new SingleValuePropertyDescriptionBuilder(new StringBuilder(), this);
            return this.current;
        }

        @Override
        public PropertyDescriptionBuilder append (CharSequence s) {
            this.current.append(s);
            return this;
        }

        @Override
        public PropertyDescriptionBuilder append (String s) {
            this.current.append(s);
            return this;
        }

        @Override
        public PropertyDescriptionBuilder append (Object o) {
            this.current.append(o);
            return this;
        }

        @Override
        public PropertyDescriptionBuilder append (boolean b) {
            this.current.append(b);
            return this;
        }

        @Override
        public PropertyDescriptionBuilder append (char c) {
            this.current.append(c);
            return this;
        }

        @Override
        public PropertyDescriptionBuilder append (int i) {
            this.current.append(i);
            return this;
        }

        @Override
        public PropertyDescriptionBuilder append (long l) {
            this.current.append(l);
            return this;
        }

        @Override
        public String toString () {
            Holder<String> separator = HolderBuilder.first("").andThen(", ");
            StringBuilder builder = new StringBuilder(this.propertyName);
            builder.append(PROPERTY_NAME_VALUE_SEPARATOR);
            for (SimplePropertyDescriptionBuilder valueBuilder : this.valueBuilders) {
                builder.append(separator.get());
                builder.append(valueBuilder.toString());
            }
            return builder.toString();
        }
    }

}