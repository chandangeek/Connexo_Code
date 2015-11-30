package com.energyict.mdc.protocol.api;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.NamedBusinessObject;

import java.io.InputStream;
import java.sql.SQLException;
import java.time.Instant;

/**
 * Represent a file
 *
 * @author Geert
 */
public interface UserFile extends NamedBusinessObject {

    /**
     * Returns the file extension
     *
     * @return the extension
     */
    String getExtension();

    /**
     * Returns the file's name
     *
     * @return the file name
     */
    String getFileName();

    /**
     * Returns the file contents as a byte array
     *
     * @return the file contents
     */
    byte[] loadFileInByteArray();

    /**
     * Returns a shadow initialized with the receiver
     *
     * @return a shadow object
     */
    UserFileShadow getShadow();

    /**
     * updates the receiver
     *
     * @param shadow contains the new attributes values
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business exception occurred
     */
    void update(UserFileShadow shadow) throws SQLException, BusinessException;

    /**
     * Updates the receiver's contents
     *
     * @param in contains the new contents
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business error occurred
     */
    void updateContents(InputStream in) throws SQLException, BusinessException;

    /**
     * Tests if this file is a picture file (jpg, jpeg, png, gif)
     *
     * @return true if a picture file, false otherwise.
     */
    boolean isPictureFile();

    /**
     * Returns the receiver's modification date
     *
     * @return the receiver's modification date
     */
    Instant getModDate();

    /**
     * Tests if this file is a zip file (zip)
     *
     * @return true if a picture file, false otherwise.
     */
    boolean isZipFile();

    /**
     * Tests if this file is executable (based on its file extension)
     *
     * @return true if file is executable, false if not.
     */
    boolean isExecutable();
}
