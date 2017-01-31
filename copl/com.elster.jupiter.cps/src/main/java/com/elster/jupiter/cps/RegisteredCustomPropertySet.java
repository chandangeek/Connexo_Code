/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps;

import aQute.bnd.annotation.ProviderType;

import java.util.Set;

/**
 * Models a persistent version of a {@link CustomPropertySet} that was previously
 * registered on the {@link CustomPropertySetService}'s whiteboard.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-20 (16:11)
 */
@ProviderType
public interface RegisteredCustomPropertySet {

    long getId();

    /**
     * Tests if the actual {@link CustomPropertySet} is still
     * registered on the {@link CustomPropertySetService}'s whiteboard.
     * Since a RegisteredCustomPropertySet is a persistent version
     * of a CustomPropertySet, it is possible that the persistent
     * version is in the database but the actual is no longer
     * available after a server restart.
     * If this returns <code>true</code> then {@link #getCustomPropertySet()}
     * is guaranteed <strong>NOT</strong> to return <code>null</code>.
     *
     * @return A flag that checks if this CustomPropertySet is still registered
     *         on the CustomPropertySetService's whiteboard.
     */
    boolean isActive();

    /**
     * Gets the {@link CustomPropertySet} iff that is still
     * registered on the {@link CustomPropertySetService}'s whiteboard.
     *
     * @return The CustomPropertySet or <code>null</code> if the CustomPropertySet
     *         failed to register on the CustomPropertySetService's whiteboard
     *         after a server restart.
     */
    CustomPropertySet getCustomPropertySet();

    default String getCustomPropertySetId() {
        return getCustomPropertySet().getId();
    }

    Set<ViewPrivilege> getViewPrivileges();

    Set<EditPrivilege> getEditPrivileges();

    boolean isEditableByCurrentUser();

    boolean isViewableByCurrentUser();

    /**
     * Updates the view and edit privileges of this RegisteredCustomPropertySet.
     *
     * @param viewPrivileges The new view privileges
     * @param editPrivileges The new edit privileges
     */
    void updatePrivileges(Set<ViewPrivilege> viewPrivileges, Set<EditPrivilege> editPrivileges);

}