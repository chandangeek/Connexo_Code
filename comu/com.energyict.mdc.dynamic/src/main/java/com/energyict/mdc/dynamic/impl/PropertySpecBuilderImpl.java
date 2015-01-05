package com.energyict.mdc.dynamic.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.PropertySpecPossibleValuesImpl;
import com.elster.jupiter.properties.ValueFactory;

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
public class PropertySpecBuilderImpl implements PropertySpecBuilder {

    /**
     * The initial name that is used for a {@link PropertySpec}
     * that is still under construction. This name will remain
     * in effect until you call the {@link #name(String)} method.
     */
    private static final String INITIAL_SPEC_NAME = "UnderConstruction";

    private PropertySpecAccessor propertySpecAccessor;

    /**
     * Creates a new PropertySpecBuilder for values of the specified
     * domain that are managed with the speciied {@link ValueFactory}.
     *
     *
     * @param valueFactory The ValueFactory
     * @return The PropertySpecBuilder
     */
    public static PropertySpecBuilder forClass(ValueFactory valueFactory) {
        return new PropertySpecBuilderImpl(valueFactory);
    }

    @Override
    public PropertySpecBuilder name(String specName) {
        this.propertySpecAccessor.setName(specName);
        return this;
    }

    @Override
    public PropertySpecBuilder setDefaultValue(Object defaultValue) {
        this.propertySpecAccessor.setDefaultValue(defaultValue);
        return this;
    }

    @Override
    public PropertySpecBuilder markExhaustive() {
        this.propertySpecAccessor.markExhaustive();
        return this;
    }

    @Override
    public PropertySpecBuilder markRequired() {
        this.propertySpecAccessor.markRequired();
        return this;
    }

    @Override
    public PropertySpecBuilder addValues(Object... values) {
        this.propertySpecAccessor.addValues(values);
        return this;
    }

    @Override
    public PropertySpec finish() {
        PropertySpec finished = this.propertySpecAccessor.getPropertySpec();
        this.propertySpecAccessor = new BuildingProcessComplete(finished);
        return finished;
    }

    private PropertySpecBuilderImpl(ValueFactory valueFactory) {
        super();
        this.propertySpecAccessor = new BasicPropertySpecAccessor(new BasicPropertySpec(INITIAL_SPEC_NAME, valueFactory));
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
            this.propertySpec.setName(name);
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
        public void addValues (Object... values) {
            PropertySpecPossibleValues xPossibleValues = this.propertySpec.getPossibleValues();
            if (xPossibleValues == null) {
                PropertySpecPossibleValuesImpl possibleValues = new PropertySpecPossibleValuesImpl(false, Arrays.asList(values));
                this.propertySpec.setPossibleValues(possibleValues);
            }
            else {
                PropertySpecPossibleValuesImpl possibleValues = (PropertySpecPossibleValuesImpl) xPossibleValues;
                possibleValues.add(values);
            }
        }

        @Override
        public void markRequired () {
            this.propertySpec.setRequired(true);
        }

        @Override
        public void markExhaustive () {
            PropertySpecPossibleValues xPossibleValues = this.propertySpec.getPossibleValues();
            if (xPossibleValues == null) {
                PropertySpecPossibleValuesImpl possibleValues = new PropertySpecPossibleValuesImpl(true, new ArrayList());
                this.propertySpec.setPossibleValues(possibleValues);
            }
            else {
                PropertySpecPossibleValuesImpl possibleValues = (PropertySpecPossibleValuesImpl) xPossibleValues;
                possibleValues.setExhaustive(true);
            }
        }
    }

}