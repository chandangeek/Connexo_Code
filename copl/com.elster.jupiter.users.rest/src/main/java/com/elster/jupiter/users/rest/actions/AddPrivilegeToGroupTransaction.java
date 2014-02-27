package com.elster.jupiter.users.rest.actions;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

public class AddPrivilegeToGroupTransaction implements Transaction<List<Privilege>> {
    private final long idGroup;
    private final String privilege;
    private final UserService userService;

    public AddPrivilegeToGroupTransaction(String privilege, long idGroup, UserService userService) {
        this.idGroup = idGroup;
        this.privilege = privilege;
        this.userService = userService;
    }

    @Override
    public List<Privilege> perform() {
        Optional<Group> foundGroup = userService.getGroup(idGroup);
        Optional<Privilege> foundPrivilege = userService.getPrivilege(privilege);

        if(foundGroup.isPresent() && foundPrivilege.isPresent()){
            foundGroup.get().grant(foundPrivilege.get());

            return foundGroup.get().getPrivileges();
        }

        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
