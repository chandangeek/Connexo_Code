package com.energyict.mdc.common;

import static com.elster.jupiter.util.Checks.*;

/**
 * Represents a shadow for a named business object
 *
 * @author Karel
 */
public class NamedObjectShadow extends IdObjectShadow {

    String name;

    /**
     * Creates a new instance of NamedObjectShadow
     */
    public NamedObjectShadow() {
    }

    /**
     * Creates a new instance of NamedObjectShadow
     *
     * @param id the object's id
     */
    public NamedObjectShadow(int id) {
        super(id);
    }

    /**
     * Creates a new instance of NamedObjectShadow
     *
     * @param id   the object's id
     * @param name the object's name
     */
    public NamedObjectShadow(int id, String name) {
        super(id);
        this.name = name;
    }

    /**
     * getter for property name
     *
     * @return value for property name
     */
    public String getName() {
        return name;
    }

    /**
     * setter for property name
     *
     * @param name new value for property name
     */
    public void setName(String name) {
        this.name = (name == null ? null : name.trim());
        markDirty();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        NamedObjectShadow otherNamedObjectShadow = (NamedObjectShadow)other;
        if (otherNamedObjectShadow.getId() != 0 && getId() != 0) {
            return otherNamedObjectShadow.getId() == getId();
        }
        else {
            if (!is(otherNamedObjectShadow.getName()).emptyOrOnlyWhiteSpace() && !is(getName()).emptyOrOnlyWhiteSpace()) {
                return otherNamedObjectShadow.getName().equals(getName())
                    && otherNamedObjectShadow.getId() == this.getId();
            }
            else {
                return is(otherNamedObjectShadow.getName()).emptyOrOnlyWhiteSpace()
                    && is(getName()).emptyOrOnlyWhiteSpace()
                    && super.equals(otherNamedObjectShadow);
            }
        }
    }

    @Override
    public int hashCode() {
        if (getId() != 0) {
            return getId();
        }
        else {
            if (!is(getName()).emptyOrOnlyWhiteSpace()) {
                return getName().hashCode();
            }
            else {
                return super.hashCode();
            }
        }
    }

}
