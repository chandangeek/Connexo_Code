package com.energyict.protocols.messaging;

import com.energyict.mdc.common.BusinessObject;
import com.energyict.mdc.common.BusinessObjectFactory;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.UserFileShadow;

import java.io.InputStream;
import java.time.Instant;

/**
 * Implements {@link UserFile} to provide for an included user file as opposed to a user file that has been fetched
 * from the database. This is useful when we embed the file in the message instead of just sending the ID, as is done
 * when sending upgrades through the RTU+Server for instance (because we don't have access to the Oracle database there).
 *
 * @author alex
 */
final class IncludedUserFile implements UserFile {

    /**
     * The file contents.
     */
    private final String contents;

    /**
     * Creates a new included file specifying the contents.
     *
     * @param contents The contents of the file. Note that as this is a String, if you want to send a binary file
     *                 it has to be encoded first.
     */
    IncludedUserFile(final String contents) {
        this.contents = contents;
    }

    /**
     * {@inheritDoc}
     */
    public final String getExtension() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final String getFileName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final Instant getModDate() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final UserFileShadow getShadow() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isPictureFile() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isZipFile() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public final byte[] loadFileInByteArray() {
        return this.contents.getBytes();
    }

    /**
     * {@inheritDoc}
     */
    public final void update(final UserFileShadow shadow) {
    }

    /**
     * {@inheritDoc}
     */
    public final void updateContents(InputStream in) {
    }

    /**
     * {@inheritDoc}
     */
    public final String getExternalName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final String getName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final String getPath() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final void rename(final String name) {
    }

    /**
     * {@inheritDoc}
     */
    public final int getId() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public final void delete() {
    }

    /**
     * {@inheritDoc}
     */
    public final BusinessObjectFactory getFactory() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final String displayString() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final BusinessObject getBusinessObject() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final String getType() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean proxies(final BusinessObject obj) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isExecutable() {
        return false;
    }

}