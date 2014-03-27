package com.energyict.mdc.dynamic.impl;

import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecBuilder;
import com.energyict.mdc.dynamic.PropertySpecPossibleValues;
import com.energyict.mdc.dynamic.LegacyReferenceFactory;
import com.energyict.mdc.dynamic.ValueFactory;

import java.util.ArrayList;
import java.util.Arrays;

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
public class PropertySpecBuilderImpl<T> implements PropertySpecBuilder<T> {

    /**
     * The initial name that is used for a {@link PropertySpec}
     * that is still under construction. This name will remain
     * in effect until you call the {@link #name(String)} method.
     */
    private static final String INITIAL_SPEC_NAME = "UnderConstruction";

    private PropertySpecAccessor<T> propertySpecAccessor;

    /**
     * Creates a new PropertySpecBuilder for values of the specified
     * domain that are managed with the speciied {@link ValueFactory}.
     *
     *
     * @param valueFactory The ValueFactory
     * @return The PropertySpecBuilder
     */
    public static <D> PropertySpecBuilder<D> forClass(ValueFactory<D> valueFactory) {
        return new PropertySpecBuilderImpl<>(valueFactory);
    }

    /**
     * Creates a new PropertySpecBuilder for {@link IdBusinessObject}s that are
     * managed with the specified {@link IdBusinessObjectFactory}.
     *
     * @param factory The factory that provides persistence services for the reference type
     * @return The PropertySpecBuilder
     */
    @SuppressWarnings("unchecked")
    public static <D extends IdBusinessObject> PropertySpecBuilder<D> forReference (IdBusinessObjectFactory<D> factory) {
        ValueFactory<D> referenceFactory = new LegacyReferenceFactory<>(factory);
        return new PropertySpecBuilderImpl<>(referenceFactory);
    }

    @Override
    public PropertySpecBuilder<T> name(String specName) {
        this.propertySpecAccessor.setName(specName);
        return this;
    }

    @Override
    public PropertySpecBuilder<T> setDefaultValue(T defaultValue) {
        this.propertySpecAccessor.setDefaultValue(defaultValue);
        return this;
    }

    @Override
    public PropertySpecBuilder<T> markExhaustive() {
        this.propertySpecAccessor.markExhaustive();
        return this;
    }

    @Override
    public PropertySpecBuilder<T> markRequired() {
        this.propertySpecAccessor.markRequired();
        return this;
    }

    @Override
    public PropertySpecBuilder<T> addValues(T... values) {
        this.propertySpecAccessor.addValues(values);
        return this;
    }

    @Override
    public PropertySpec<T> finish() {
        PropertySpec<T> finished = this.propertySpecAccessor.getPropertySpec();
        this.propertySpecAccessor = new BuildingProcessComplete<>(finished);
        return finished;
    }

    private PropertySpecBuilderImpl(ValueFactory<T> valueFactory) {
        super();
        this.propertySpecAccessor = new BasicPropertySpecAccessor<>(new BasicPropertySpec<>(INITIAL_SPEC_NAME, valueFactory));
    }

    private interface PropertySpecAccessor<T> {

        public PropertySpec<T> getPropertySpec ();

        public void setName (String name);

        public void setDefaultValue (T defaultValue);

        public void addValues (T... values);

        public void markRequired ();

        public void markExhaustive ();
    }

    private class BasicPropertySpecAccessor<T> implements PropertySpecAccessor<T> {
        private BasicPropertySpec<T> propertySpec;

        private BasicPropertySpecAccessor (BasicPropertySpec<T> propertySpec) {
            super();
            this.propertySpec = propertySpec;
        }

        @Override
        public PropertySpec<T> getPropertySpec () {
            return this.propertySpec;
        }

        @Override
        public void setName (String name) {
            this.propertySpec.setName(name);
        }

        @Override
        public void setDefaultValue (T defaultValue) {
            PropertySpecPossibleValues<T> xPossibleValues = this.propertySpec.getPossibleValues();
            if (xPossibleValues == null) {
                PropertySpecPossibleValuesImpl<T> possibleValues = new PropertySpecPossibleValuesImpl<>(defaultValue, false);
                this.propertySpec.setPossibleValues(possibleValues);
            }
            else {
                PropertySpecPossibleValuesImpl<T> possibleValues = (PropertySpecPossibleValuesImpl<T>) xPossibleValues;
                possibleValues.setDefault(defaultValue);
            }
        }

        @Override
        public void addValues (T... values) {
            PropertySpecPossibleValues<T> xPossibleValues = this.propertySpec.getPossibleValues();
            if (xPossibleValues == null) {
                PropertySpecPossibleValuesImpl<T> possibleValues = new PropertySpecPossibleValuesImpl<>(false, Arrays.asList(values));
                this.propertySpec.setPossibleValues(possibleValues);
            }
            else {
                PropertySpecPossibleValuesImpl<T> possibleValues = (PropertySpecPossibleValuesImpl<T>) xPossibleValues;
                possibleValues.add(values);
            }
        }

        @Override
        public void markRequired () {
            this.propertySpec.setRequired(true);
        }

        @Override
        public void markExhaustive () {
            PropertySpecPossibleValues<T> xPossibleValues = this.propertySpec.getPossibleValues();
            if (xPossibleValues == null) {
                PropertySpecPossibleValuesImpl<T> possibleValues = new PropertySpecPossibleValuesImpl<>(true, new ArrayList<T>());
                this.propertySpec.setPossibleValues(possibleValues);
            }
            else {
                PropertySpecPossibleValuesImpl<T> possibleValues = (PropertySpecPossibleValuesImpl<T>) xPossibleValues;
                possibleValues.setExhaustive(true);
            }
        }
    }

    /**
     * Provides an implementation for the {@link PropertySpecAccessor} interface
     * that will be used once the building process is complete
     * and will throw {@link IllegalStateException} on every attempt to change
     * the {@link PropertySpec} that was built in previous steps.
     */
    private class BuildingProcessComplete<T> implements PropertySpecAccessor<T> {
        private PropertySpec<T> propertySpec;

        private BuildingProcessComplete (PropertySpec<T> propertySpec) {
            super();
            this.propertySpec = propertySpec;
        }

        @Override
        public PropertySpec<T> getPropertySpec () {
            return this.propertySpec;
        }

        @Override
        public void setName (String name) {
            this.notifyBuildingProcessComplete();
        }

        @Override
        public void setDefaultValue (T defaultValue) {
            this.notifyBuildingProcessComplete();
        }

        @Override
        public void addValues (T... values) {
            this.notifyBuildingProcessComplete();
        }

        @Override
        public void markRequired () {
            this.notifyBuildingProcessComplete();
        }

        @Override
        public void markExhaustive () {
            this.notifyBuildingProcessComplete();
        }

        private void notifyBuildingProcessComplete () {
            throw new IllegalStateException("PropertySpec building process is complete, use another builder if you want to construct another PropertySpec!");
        }

    }

}