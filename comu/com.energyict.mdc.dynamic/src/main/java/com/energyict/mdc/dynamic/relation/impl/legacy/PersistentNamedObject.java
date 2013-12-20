package com.energyict.mdc.dynamic.relation.impl.legacy;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.DuplicateException;
import com.energyict.mdc.common.NamedBusinessObject;

import java.sql.SQLException;
import java.util.Objects;

/**
 * Replaces the PersistentNamedObject class from the mdw persistence framework
 * wiring most of the methods to the new Jupiter-Kore persistence framework.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-17 (13:15)
 */
public abstract class PersistentNamedObject extends PersistentIdObject implements NamedBusinessObject {

    private String name = "";

    protected PersistentNamedObject() {
        super();
    }

    public PersistentNamedObject(String name) {
        super();
        Objects.requireNonNull(name);
        this.name = name;
    }

    @Override
    public String displayString() {
        return this.getName();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Provides a default implementation of getExternalName.
     * Subclasses can (should override).
     * Defaults to getName()
     *
     * @return the external name
     */
    public String getExternalName() {
        return getName();
    }

    /**
     * Checks if the name does not contain invalid characters.
     *
     * @param newName the new name to validate
     * @throws BusinessException if a business error occurred
     */
    protected void validate(String newName) throws BusinessException {
        if (newName == null) {
            throw new BusinessException("nameCantBeEmpty", "Name cannot be empty");
        }
        if (newName.trim().isEmpty()) {
            throw new BusinessException("nameCantBeBlank", "Name cannot be blank");
        }
        String invalidChars = getInvalidCharacters();
        for (int i = 0; i < newName.length(); i++) {
            if (invalidChars.indexOf(newName.charAt(i)) != -1) {
                throw new BusinessException("nameXcontainsInvalidChars", "The name \"{0}\" contains invalid characters", newName);
            }
        }
    }

    /**
     * Tests if the new name does not violate the name uniqueness constraints.
     *
     * @param newName the new name
     * @throws DuplicateException if uniqueness constraint violated
     */
    protected void validateConstraint(String newName) throws DuplicateException {
    }

    /**
     * Returns invalid characters for the object's name space.
     *
     * @return String of invalid characters
     */
    protected String getInvalidCharacters() {
        return "";
    }

    public void rename(String newName) throws BusinessException, SQLException {
        if (newName.equals(getName())) {
            return;
        }
        validate(newName);
        validateConstraint(newName);
        setName(newName);
        this.post();
    }

}