/*
 * UserFileShadow.java
 *
 * Created on 27 december 2003, 10:36
 */

package com.energyict.mdc.protocol.api;

import com.energyict.mdc.common.ObjectShadow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;

import static com.elster.jupiter.util.Checks.is;

/**
 * User File Shadow
 *
 * @author Geert
 */
public class UserFileShadow extends ObjectShadow {

    private int id = 0;
    private String name;
    private String extension;
    private File file;
    private Instant modDate;

    /**
     * Creates a new instance of FileShadow
     */
    public UserFileShadow() {
    }

    /**
     * creates a new instance
     *
     * @param userFile object to shadow
     */
    public UserFileShadow(UserFile userFile) {
        this();
        this.id = userFile.getId();
        this.name = userFile.getName();
        extension = userFile.getExtension();
        modDate = userFile.getModDate();
        try {
            file = File.createTempFile("userfile", "." + userFile.getExtension());
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(userFile.loadFileInByteArray());
            fos.close();
            file.deleteOnExit();
        } catch (IOException ex) {
        }
    }

    /**
     * Getter for property extension.
     *
     * @return Value of property extension.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Setter for property extension.
     *
     * @param extension New value of property extension.
     */
    public void setExtension(String extension) {
        this.extension = extension;
        markDirty();
    }

    /**
     * Getter for property file.
     *
     * @return Value of property file.
     */
    public File getFile() {
        return file;
    }

    /**
     * Setter for property file.
     *
     * @param file New value of property file.
     */
    public void setFile(File file) {
        this.file = file;
        markDirty();
    }

    /**
     * Getter for property modDate.
     *
     * @return Value of property modDate.
     */
    public Instant getModDate() {
        return modDate;
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

        UserFileShadow otherNamedObjectShadow = (UserFileShadow)other;
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

    /**
     * Returns the id of the object shadowed by the receiver
     *
     * @return the shadowed object's id
     */
    public int getId() {
        return id;
    }

    /**
     * Setter for the property id.
     * This method is reserved for internal use by business objects.
     * Should not be used by external applications,
     * since object ids are generated automatically by the system.
     *
     * @param id new value for the property id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * prepares the receiver for cloning
     */
    public void prepareCloning() {
        super.prepareCloning();
        this.id = 0;
    }
}