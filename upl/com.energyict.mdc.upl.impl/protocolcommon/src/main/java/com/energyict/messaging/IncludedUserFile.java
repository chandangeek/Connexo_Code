package com.energyict.messaging;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.TimePeriod;
import com.energyict.cpo.BusinessObject;
import com.energyict.cpo.BusinessObjectFactory;
import com.energyict.cpo.Dependent;
import com.energyict.cuo.core.DesktopDecorator;
import com.energyict.dynamicattributes.AttributeType;
import com.energyict.mdw.core.CopyContext;
import com.energyict.mdw.core.Folder;
import com.energyict.mdw.core.FolderMember;
import com.energyict.mdw.core.UserAction;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.relation.Relation;
import com.energyict.mdw.relation.RelationAttributeType;
import com.energyict.mdw.relation.RelationType;
import com.energyict.mdw.shadow.UserFileShadow;
import com.energyict.metadata.TypeId;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * Implements {@link com.energyict.mdw.core.UserFile} to provide for an included user file as opposed to a user file that has been fetched
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
    public final Folder getFolder() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final int getFolderId() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public final Date getModDate() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final ImageIcon getPicture() {
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
    public final boolean isInUse() {
        return false;
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
    public final void moveToFolder(final Folder folder) throws BusinessException, SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public final void update(final UserFileShadow shadow) throws SQLException, BusinessException {
    }

    /**
     * {@inheritDoc}
     */
    public final void updateContents(InputStream in) throws SQLException, BusinessException {
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
    public final String getFullName() {
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
    public final String getNameSeparator() {
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
    public final void rename(final String name) throws BusinessException, SQLException {
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
    public final boolean canDelete() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public final void delete() throws BusinessException, SQLException {
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public final Class getAspectType(final String name) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final String[] getCopyAspects() {
        return null;
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
    public final TypeId getTypeId() {
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
    public final Icon getIcon() {
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
    public final DesktopDecorator asDesktopDecorator() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final Dependent getOwner() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isAuthorized(final String action) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public final FolderMember copy(final CopyContext context) throws BusinessException, SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final Folder getContainer() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isInTree(final Folder folder) {
        return false;
    }

    /**
     *
     */
    public final boolean isAuthorized(final UserAction action) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isExecutable() {
        return false;
    }

    @SuppressWarnings({"unchecked"})
    public List<RelationType> getAvailableRelationTypes() {
        return Collections.EMPTY_LIST;
    }

    @SuppressWarnings({"unchecked"})
    public List<? extends AttributeType> getAvailableAttributeTypes() {
        return Collections.EMPTY_LIST;
    }

    @SuppressWarnings({"unchecked"})
    public List<Relation> getRelations(RelationAttributeType attrib, Date date, boolean includeObsolete) {
        return Collections.EMPTY_LIST;
    }

    @SuppressWarnings({"unchecked"})
    public List<Relation> getRelations(RelationAttributeType attrib, Date date, boolean includeObsolete, int fromRow, int toRow) {
        return Collections.EMPTY_LIST;
    }

    @SuppressWarnings({"unchecked"})
    public List<Relation> getAllRelations(RelationAttributeType attrib) {
        return Collections.EMPTY_LIST;
    }

    @SuppressWarnings({"unchecked"})
    public List<Relation> getRelations(RelationAttributeType attrib, TimePeriod period, boolean includeObsolete) {
        return Collections.EMPTY_LIST;
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public final void processFileAsStream(final Consumer<InputStream> consumer) throws SQLException {
		try (final ByteArrayInputStream stream = new ByteArrayInputStream(this.contents.getBytes())) {
			consumer.accept(stream);
		} catch (IOException e) {
			throw new ApplicationException("IO error while closing byte stream : [" + e.getMessage() + "]", e);
		}
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public final long getFileSize() throws SQLException {
		return this.contents.getBytes().length;
	}

    @Override
    public void lock() {
    }
}
