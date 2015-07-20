package com.elster.jupiter.cps;

import java.util.Set;

/**
 * Models a {@link CustomPropertySet} that was previously
 * registered on the {@link CustomPropertySetService}'s whiteboard.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-20 (16:11)
 */
public interface RegisteredCustomPropertySet {

    long getId();

    CustomPropertySet getCustomPropertySet();

    Set<ViewPrivilege> getViewPrivileges();

    Set<EditPrivilege> getEditPrivileges();

    /**
     * Updates the view and edit privileges of this RegisteredCustomPropertySet.
     *
     * @param viewPrivileges The new view privileges
     * @param editPrivileges The new edit privileges
     */
    void updatePrivileges(Set<ViewPrivilege> viewPrivileges, Set<EditPrivilege> editPrivileges);

}