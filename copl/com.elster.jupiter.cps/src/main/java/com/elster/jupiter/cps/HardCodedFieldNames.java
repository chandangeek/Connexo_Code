/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.util.time.Interval;

import java.time.Instant;

/**
 * Documents the names of the fields of a {@link PersistentDomainExtension}
 * that are hard coded and expected to be available by the {@link CustomPropertySetService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-12 (08:55)
 */
public enum HardCodedFieldNames {

    /**
     * Holds a {@link Reference} to the {@link RegisteredCustomPropertySet}.
     * In other words:
     * <pre>
     *     <code>
     *         private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = Reference.absent();
     *     </code>
     * </pre>
     */
    CUSTOM_PROPERTY_SET {
        @Override
        public String javaName() {
            return "registeredCustomPropertySet";
        }

        @Override
        public String databaseName() {
            return "CPS";
        }

        @Override
        public Class fieldType() {
            return Reference.class; // Actually Reference<CustomPropertySet>
        }
    },
    /**
     * Holds a counter that is increased each time the entity is updated.
     * The initial value is 1.
     */
    VERSION {
        @Override
        public String javaName() {
            return "version";
        }

        @Override
        public String databaseName() {
            return "VERSIONCOUNT";
        }

        @Override
        public Class fieldType() {
            return String.class;
        }
    },
    /**
     * Holds the point in time on which the entity was created.
     */
    CREATION_TIME {
        @Override
        public String javaName() {
            return "createTime";
        }

        @Override
        public String databaseName() {
            return "CREATETIME";
        }

        @Override
        public Class fieldType() {
            return Instant.class;
        }
    },
    /**
     * Holds the point in time on which the entity was updated.
     */
    MODIFICATION_TIME {
        @Override
        public String javaName() {
            return "modTime";
        }

        @Override
        public String databaseName() {
            return "MODTIME";
        }

        @Override
        public Class fieldType() {
            return Instant.class;
        }
    },
    /**
     * Holds the name of the user that created/updated the entity.
     */
    USER_NAME {
        @Override
        public String javaName() {
            return "userName";
        }

        @Override
        public String databaseName() {
            return "USERNAME";
        }

        @Override
        public Class fieldType() {
            return String.class;
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