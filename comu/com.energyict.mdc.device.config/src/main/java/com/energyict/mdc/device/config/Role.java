package com.energyict.mdc.device.config;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.NamedBusinessObject;
import com.energyict.mdc.common.UserAction;
import com.energyict.mdc.common.TypeId;

import java.sql.SQLException;
import java.util.List;

/**
 * A Role specifies a list of {@link Privilege Privilege} objects.
 * {@link Privilege Privilege} objects are actions that a
 * {@link User User} can perform on a list of object types.
 *
 * @author Steven Willems.
 * @since Jun 2, 2009.
 */
public interface Role extends NamedBusinessObject {

    /**
     * Check if this Role is authorized to perform a given action
     * on a certain type.
     *
     * @param action The action to perform.
     * @param typeId The type.
     * @return true if the action may be executed on the type, false otherwise.
     */
    boolean isAuthorized(UserAction action, TypeId typeId);

    /**
     * Add a {@link Privilege Privilege} to this Role.
     *
     * @param action The {@link UserAction Action} part of the {@link Privilege Privilege}.
     * @param type   The {@link TypeId TypeId} part of the {@link Privilege Privilege}.
     * @return The role with the added {@link Privilege Privilege}.
     * @throws BusinessException Thrown when the privilege already exists in the Role.
     */
    Role addPrivilege(UserAction action, TypeId type) throws BusinessException, SQLException;

    /**
     * Remove a {@link Privilege Privilege} from this Role.
     *
     * @param action The {@link UserAction Action} part of the {@link Privilege Privilege}.
     * @param type   The {@link TypeId TypeId} part of the {@link Privilege Privilege}.
     * @return The role with the removed {@link Privilege Privilege}.
     * @throws BusinessException Thrown when the privilege is not found.
     */
    Role removePrivilege(UserAction action, TypeId type) throws BusinessException, SQLException;

    /**
     * Check if the role has an predefined admin type != 0.
     *
     * @return True for Administrator or SuperUser role, false otherwise.
     * @deprecated use isAuthorized(UserAction action, TypeId typeId) method
     */
    @Deprecated
    boolean isAdminRole();

    /**
     * Get the unmodifiable list of {@link Privilege} objects
     * for this Role.
     *
     * @return an unmodifiable list, empty list if no privileges are defined for this list.
     */
    List<Privilege> getPrivileges();



    /**
     * Not part of public API
     *
     * @return true if role is the predefined SUPERUSER role
     */
    boolean isSuperuserRole();
}
