/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.impl;

import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.ValueFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides building services for {@link PropertySpec}s.
 * All methods for building will return the same PropertySpecBuilder
 * to support method chaining while building.
 * As an example:<pre><code>
 *     PropertySpec&lt;String&gt; propertySpec =
 *          PropertySpecBuilder.
 *              forClass(String.class, new StringFactory()).
 *              name("exampleStringProperty").
 *              finish();
 * </code></pre>
 * <p>
 * Once the building process is finished, i.e. you have called
 * the {@link #finish()} method, you will not be able to restart
 * the building process to avoid that the PropertySpec
 * that was built is passed onto other processes
 * while the building process continues. The builder will
 * throw an {@link IllegalStateException} to signal that.
 * </p>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-20 (09:06)
 */
class PropertySpecBuilderImpl<T> implements PropertySpecBuilder<T> {

    private PropertySpecAccessor propertySpecAccessor;

    /**
     * Creates a new PropertySpecBuilder for values of the specified
     * domain that are managed with the speciied {@link ValueFactory}.
     *
     * @param valueFactory The ValueFactory
     */
    PropertySpecBuilderImpl(ValueFactory<T> valueFactory) {
        super();
        this.propertySpecAccessor = new BasicPropertySpecAccessor(new BasicPropertySpec(valueFactory));
    }

    /**
     * Creates a new PropertySpecBuilder for the BasicPropertySpec
     * that was already partially initialized.
     *
     * @param valueFactory The ValueFactory
     * @param partiallyInitialized The partially intialized BasicPropertySpec
     */
    PropertySpecBuilderImpl(ValueFactory<T> valueFactory, BasicPropertySpec partiallyInitialized) {
        super();
        this.propertySpecAccessor = new BasicPropertySpecAccessor(partiallyInitialized);
    }

    PropertySpecBuilderImpl<T> setNameAndDescription(NameAndDescription nameAndDescription) {
        this.propertySpecAccessor.setName(nameAndDescription.getName(), nameAndDescription.getDisplayName());
        this.propertySpecAccessor.setDescription(nameAndDescription.getDescription());
        return this;
    }

    @Override
    public PropertySpecBuilder<T> setDefaultValue(Object defaultValue) {
        this.propertySpecAccessor.setDefaultValue(defaultValue);
        return this;
    }

    @Override
    public PropertySpecBuilder<T> markExhaustive() {
        this.markExhaustive(PropertySelectionMode.UNSPECIFIED);
        return this;
    }

    @Override
    public PropertySpecBuilder<T> markExhaustive(PropertySelectionMode selectionMode) {
        this.propertySpecAccessor.markExhaustive(selectionMode);
        return this;
    }

    @Override
    public PropertySpecBuilder<T> markEditable() {
        this.propertySpecAccessor.markEditable();
        return this;
    }

    @Override
    public PropertySpecBuilder<T> markMultiValued(String separator) {
        this.propertySpecAccessor.markMultiValued(separator);
        return this;
    }

    @Override
    public PropertySpecBuilder<T> markRequired() {
        this.propertySpecAccessor.markRequired();
        return this;
    }

    @Override
    public PropertySpecBuilder<T> addValues(T... values) {
        this.propertySpecAccessor.addValues(Arrays.asList(values));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PropertySpecBuilder<T> addValues(List<T> values) {
        this.propertySpecAccessor.addValues((List<Object>) values);
        return this;
    }

    @Override
    public PropertySpec finish() {
        PropertySpec finished = this.propertySpecAccessor.getPropertySpec();
        this.propertySpecAccessor = new BuildingProcessComplete(finished);
        return finished;
    }

    private interface PropertySpecAccessor {

        PropertySpec getPropertySpec();

        void setName(String name);

        void setName(String name, String displayName);

        void setDescription(String description);

        void setDefaultValue(Object defaultValue);

        void addValues(List<Object> values);

        void markRequired();

        void markMultiValued(String separator);

        void markExhaustive(PropertySelectionMode selectionMode);

        void markEditable();

    }

    private class BasicPropertySpecAccessor implements PropertySpecAccessor {
        private BasicPropertySpec propertySpec;

        private BasicPropertySpecAccessor (BasicPropertySpec propertySpec) {
            super();
            this.propertySpec = propertySpec;
        }

        @Override
        public PropertySpec getPropertySpec () {
            return this.propertySpec;
        }

        @Override
        public void setName (String name) {
            this.setName(name, name);
        }

        @Override
        public void setName(String name, String displayName) {
            this.propertySpec.setName(name);
            this.propertySpec.setDisplayName(displayName);
        }

        @Override
        public void setDescription(String description) {
            this.propertySpec.setDescription(description);
        }

        @Override
        public void setDefaultValue (Object defaultValue) {
            PropertySpecPossibleValues xPossibleValues = this.propertySpec.getPossibleValues();
            if (xPossibleValues == null) {
                PropertySpecPossibleValuesImpl possibleValues = new PropertySpecPossibleValuesImpl(defaultValue, false);
                this.propertySpec.setPossibleValues(possibleValues);
            }
            else {
                PropertySpecPossibleValuesImpl possibleValues = (PropertySpecPossibleValuesImpl) xPossibleValues;
                possibleValues.setDefault(defaultValue);
            }
        }

        @Override
        public void addValues (List<Object> values) {
            if (!values.isEmpty()) {
                PropertySpecPossibleValues xPossibleValues = this.propertySpec.getPossibleValues();
                if (xPossibleValues == null) {
                    PropertySpecPossibleValuesImpl possibleValues = new PropertySpecPossibleValuesImpl(false, values);
                    this.propertySpec.setPossibleValues(possibleValues);
                }
                else {
                    PropertySpecPossibleValuesImpl possibleValues = (PropertySpecPossibleValuesImpl) xPossibleValues;
                    values.forEach(possibleValues::add);
                }
            }
        }

        @Override
        public void markRequired () {
            this.propertySpec.setRequired(true);
        }

        @Override
        public void markMultiValued(String separator) {
            this.propertySpec.setMultiValued(true, separator);
        }

        @Override
        public void markExhaustive (PropertySelectionMode selectionMode) {
            PropertySpecPossibleValues xPossibleValues = this.propertySpec.getPossibleValues();
            if (xPossibleValues == null) {
                PropertySpecPossibleValuesImpl possibleValues = new PropertySpecPossibleValuesImpl(true, new ArrayList<>());
                possibleValues.setExhaustive(true);
                possibleValues.setSelectionMode(selectionMode);
                this.propertySpec.setPossibleValues(possibleValues);
            }
            else {
                PropertySpecPossibleValuesImpl possibleValues = (PropertySpecPossibleValuesImpl) xPossibleValues;
                possibleValues.setExhaustive(true);
                possibleValues.setSelectionMode(selectionMode);
            }
        }

        @Override
        public void markEditable() {
            PropertySpecPossibleValues xPossibleValues = this.propertySpec.getPossibleValues();
            if (xPossibleValues == null) {
                PropertySpecPossibleValuesImpl possibleValues = new PropertySpecPossibleValuesImpl(true, new ArrayList<>());
                possibleValues.setEditable(true);
                this.propertySpec.setPossibleValues(possibleValues);
            } else {
                PropertySpecPossibleValuesImpl possibleValues = (PropertySpecPossibleValuesImpl) xPossibleValues;
                possibleValues.setEditable(true);
            }
        }
    }

    /**
     * Provides an implementation for the {@link PropertySpecAccessor} interface
     * that will be used once the building process is complete
     * and will throw {@link IllegalStateException} on every attempt to change
     * the {@link PropertySpec} that was built in previous steps.
     */
    private class BuildingProcessComplete implements PropertySpecAccessor {
        private PropertySpec propertySpec;

        BuildingProcessComplete (PropertySpec propertySpec) {
            super();
            this.propertySpec = propertySpec;
        }

        @Override
        public PropertySpec getPropertySpec () {
            return this.propertySpec;
        }

        @Override
        public void setName (String name) {
            this.notifyBuildingProcessComplete();
        }

        @Override
        public void setName(String name, String displayName) {
            this.notifyBuildingProcessComplete();
        }

        @Override
        public void setDescription(String description) {
            this.notifyBuildingProcessComplete();
        }

        @Override
        public void setDefaultValue (Object defaultValue) {
            this.notifyBuildingProcessComplete();
        }

        @Override
        public void addValues (List<Object> values) {
            this.notifyBuildingProcessComplete();
        }

        @Override
        public void markRequired () {
            this.notifyBuildingProcessComplete();
        }

        @Override
        public void markMultiValued(String separator) {
            this.notifyBuildingProcessComplete();
        }

        @Override
        public void markExhaustive (PropertySelectionMode selectionMode) {
            this.notifyBuildingProcessComplete();
        }

        @Override
        public void markEditable() {
            this.notifyBuildingProcessComplete();
        }

        private void notifyBuildingProcessComplete () {
            throw new IllegalStateException("PropertySpec building process is complete, use another builder if you want to construct another PropertySpec!");
        }
    }

}