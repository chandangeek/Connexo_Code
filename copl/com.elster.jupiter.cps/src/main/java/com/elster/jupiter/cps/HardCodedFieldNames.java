package com.elster.jupiter.cps;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.util.time.Interval;

/**
 * Documents the names of the fields of a {@link PersistentDomainExtension}
 * that are hard coded and expected to be available by the {@link CustomPropertySetService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-12 (08:55)
 */
public enum HardCodedFieldNames {

    /**
     * Holds a {@link Reference} to the {@link CustomPropertySet}.
     * In other words:
     * <pre>
     *     <code>
     *         private Reference<CustomPropertySet> customPropertySet = Reference.absent();
     *     </code>
     * </pre>
     */
    CUSTOM_PROPERTY_SET {
        @Override
        public String javaName() {
            return "customPropertySet";
        }

        @Override
        public String databaseName() {
            return "cps";
        }

        @Override
        public Class fieldType() {
            return Reference.class; // Actually Reference<CustomPropertySet>
        }
    },
    /**
     * Required only when the {@link CustomPropertySet} is versioned
     * and holds the period in time during which the extended
     * properties are effective.
     *
     * @see CustomPropertySet#isVersioned()
     */
    INTERVAL {
        @Override
        public String javaName() {
            return "interval";
        }

        @Override
        public String databaseName() {
            return this.javaName();
        }

        @Override
        public Class fieldType() {
            return Interval.class;
        }
    };

    /**
     * Gets the name of the hard coded field as it is expected by the {@link CustomPropertySetService}.
     *
     * @return The name of the field
     */
    public abstract String javaName();

    /**
     * Gets the name of the database column that holds the value of this field.
     *
     * @return The name of the field
     */
    public abstract String databaseName();

    /**
     * Gets the type of the hard coded field as it is expected by the {@link CustomPropertySetService}.
     *
     * @return The type of the field
     */
    public abstract Class fieldType();

}