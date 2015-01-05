package com.elster.jupiter.properties;

/**
 * Provides building services for {@link PropertySpec}s.
 * All methods that contribute to aspects of the PropertySpec
 * under construction will return the same PropertySpecBuilder
 * to support method chaning.
 * Finally, the client code will call the finish method when all
 * aspects of the PropertySpec have been specified.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (13:13)
 */
public interface PropertySpecBuilder {

    /**
     * Sets the name of the {@link PropertySpec} that is being constructed.
     *
     * @param specName The name of the PropertySpec
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    public PropertySpecBuilder name(String specName);

    /**
     * Sets a default value for the {@link PropertySpec} under construction.
     * Setting a default value implies that the default value
     * will become one of the possible values of the PropertySpec.
     * Note that there is only one default and calling this method
     * a second time will overrule the previous default.
     * The previous value will remain in the list of possible values though.
     *
     * @param defaultValue The default value
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    public PropertySpecBuilder setDefaultValue (Object defaultValue);

    /**
     * Marks the list of possible values of the {@link PropertySpec}
     * under construction as an exhaustive list.
     *
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    public PropertySpecBuilder markExhaustive();

    /**
     * Marks the {@link PropertySpec} that is under construction as required.
     * The resulting PropertySpec will therefore return <code>true</code>
     * for the {@link PropertySpec#isRequired()} method.
     *
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    public PropertySpecBuilder markRequired();

    /**
     * Adds the specified values to the PropertySpec under construction.
     *
     * @param values The possible values
     * @return This PropertySpecBuilder to support method chaining while constructing
     */
    public PropertySpecBuilder addValues (Object... values);

    /**
     * Finishes the building process and returns the
     * {@link PropertySpec} as it was constructed so far.
     * Note that this stops the building process and
     * attempts to reuse this building will fail
     * with an {@link IllegalStateException} being thrown.
     *
     * @return The PropertySpec
     */
    public PropertySpec finish();


    interface PropertySpecAccessor {

        public PropertySpec getPropertySpec ();

        public void setName (String name);

        public void setDefaultValue (Object defaultValue);

        public void addValues (Object... values);

        public void markRequired ();

        public void markExhaustive ();
    }

    /**
     * Provides an implementation for the {@link PropertySpecAccessor} interface
     * that will be used once the building process is complete
     * and will throw {@link IllegalStateException} on every attempt to change
     * the {@link PropertySpec} that was built in previous steps.
     */
    static class BuildingProcessComplete implements PropertySpecAccessor {
        private PropertySpec propertySpec;

        public BuildingProcessComplete (PropertySpec propertySpec) {
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
        public void setDefaultValue (Object defaultValue) {
            this.notifyBuildingProcessComplete();
        }

        @Override
        public void addValues (Object... values) {
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