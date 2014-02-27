package com.elster.jupiter.users.rest.actions;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

public class DeletePrivilegeFromGroupTransaction implements Transaction<List<Privilege>> {
    private final long idGroup;
    private final String privilegeName;
    private final UserService userService;

    public DeletePrivilegeFromGroupTransaction(String privilege, long idGroup, UserService userService) {
        this.idGroup = idGroup;
        this.privilegeName = privilege;
        this.userService = userService;
    }

    @Override
    public List<Privilege> perform() {
        Group group = fetchGroup();
        Privilege privilege = fetchPrivilege();

        validateDelete(group, privilege);
        doDelete(group, privilege);

        return group.getPrivileges();
    }

    private void doDelete(Group group, Privilege privilege) {
        group.revoke(privilege);
    }

    private void validateDelete(Group group, Privilege privilege) {
        if(!group.hasPrivilege(privilege)){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    private Privilege fetchPrivilege() {
        Optional<Privilege> foundPrivilege = userService.getPrivilege(privilegeName);
        if(!foundPrivilege.isPresent()){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return foundPrivilege.get();
    }

    private Group fetchGroup() {
        Optional<Group> foundGroup = userService.getGroup(idGroup);
        if(!foundGroup.isPresent()){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return foundGroup.get();
    }
}
