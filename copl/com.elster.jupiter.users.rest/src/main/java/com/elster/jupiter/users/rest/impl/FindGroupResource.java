package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserInGroup;
import com.elster.jupiter.users.rest.GroupInfo;
import com.elster.jupiter.users.rest.UserInfos;
import com.elster.jupiter.users.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by dragos on 11/20/2015.
 */
@Path("/findgroups")
public class FindGroupResource {
    private final UserService userService;
    private final RestQueryService restQueryService;
    private final Thesaurus thesaurus;

    @Inject
    public FindGroupResource(UserService userService, RestQueryService restQueryService, Thesaurus thesaurus) {
        this.userService = userService;
        this.restQueryService = restQueryService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Path("/{group}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE})
    public GroupInfo getGroup(@PathParam("group") String group) {
        Optional<Group> found = userService.getGroup(group);
        if(found.isPresent()){
            return new GroupInfo(thesaurus, found.get());
        }

        return null;
    }

    @GET
    @Path("/{group}/users")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USER_ROLE,Privileges.Constants.VIEW_USER_ROLE})
    public UserInfos getGroupUsers(@PathParam("group") String group) {
        return new UserInfos(thesaurus, userService.getGroupMembers(group));
    }

}
