package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.device.config.security.Privileges;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/devicemessageprivileges")
public class DeviceMessagePrivilegesResource {

    private final UserService userService;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceMessagePrivilegesResource(UserService userService, Thesaurus thesaurus) {
        this.userService = userService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getDeviceMessagePrivileges(@BeanParam JsonQueryParameters queryParameters) {
        Multimap<DeviceMessageUserAction, Group> privilegesMap = ArrayListMultimap.create();

        for (Group group : userService.getGroups()) {
            group.getPrivileges("MDC").stream()
                .map(p -> DeviceMessageUserAction.forPrivilege(p.getName()))
                .filter(action -> action.isPresent())
                .forEach(action -> privilegesMap.put(action.get(), group));
        }
        List<DeviceMessagePrivilegeInfo> infos = new ArrayList<>();
        for (DeviceMessageUserAction action : DeviceMessageUserAction.values()) {
            infos.add(DeviceMessagePrivilegeInfo.from(action, privilegesMap.get(action), thesaurus));
        }
        return PagedInfoList.fromPagedList("privileges", infos, queryParameters);
    }

}
