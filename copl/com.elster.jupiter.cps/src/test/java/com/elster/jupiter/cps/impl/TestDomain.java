package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;

/**
 * An domain class that will be extended by the {@link CustomPropertySet}s
 * used in the test classes of this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-12 (10:20)
 */
public class TestDomain {

    public enum FieldNames {
        NAME("name"),
        DESCRIPTION("description");

        FieldNames(String name) {
            this.name = name;
        }

        private final String name;

        public String fieldName() {
            return name;
        }
    }

    // For persistence framework
    public TestDomain() {
        super();
    }

    // For testing purposes
    public TestDomain(long id) {
        this();
        this.id = id;
    }

    @SuppressWarnings("unused")
    private long id;
    private String name;
    private String description;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}