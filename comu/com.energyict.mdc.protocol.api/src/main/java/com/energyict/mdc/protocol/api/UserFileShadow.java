/*
 * UserFileShadow.java
 *
 * Created on 27 december 2003, 10:36
 */

package com.energyict.mdc.protocol.api;

import com.energyict.mdc.common.NamedObjectShadow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * User File Shadow
 *
 * @author Geert
 */
public class UserFileShadow extends NamedObjectShadow {

    private String extension;
    private File file;
    private Date modDate;

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
        super(userFile.getId(), userFile.getName());
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
    public Date getModDate() {
        return modDate;
    }
}