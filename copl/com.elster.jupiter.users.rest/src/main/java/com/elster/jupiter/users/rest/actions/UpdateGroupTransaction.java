package com.elster.jupiter.users.rest.actions;

import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.GroupInfo;

public class UpdateGroupTransaction extends UpdateMembership implements Transaction<Group> {

    public UpdateGroupTransaction(GroupInfo info, UserService userService) {
        super(info, userService);
    }

    @Override
    public Group perform() {
        final Group group = fetchGroup();
        validateUpdate(group);
        if(info.privileges.isEmpty()){
            Group groupNoRights = group;
            groupNoRights = doUpdateEmpty(group);
            return groupNoRights;
        }else {
            final Group removedGroup = doUpdateEmpty(group, info.privileges);
            info.privileges.stream().collect(Collectors.groupingBy(pi -> pi.applicationName))
                    .entrySet()
                    .stream()
                    .forEach(p -> doUpdate(p.getKey(), removedGroup));
            return removedGroup;
        }
    }

    private void validateUpdate(Group group) {
        if (group.getVersion() != info.version) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    private Group fetchGroup() {
        Optional<Group> group = userService.getGroup(info.id);
        if (group.isPresent()) {
            return group.get();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
