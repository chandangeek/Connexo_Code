package com.energyict.mdc.device.configuration.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.device.config.security.Privileges;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

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
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE_CONFIGURATION)
    public PagedInfoList getDeviceMessagePrivileges(@BeanParam QueryParameters queryParameters) {
        Multimap<DeviceMessageUserAction, Group> privilegesMap = ArrayListMultimap.create();

        for (Group group : userService.getGroups()) {
            group.getPrivileges().stream()
                .map(p -> DeviceMessageUserAction.forPrivilege(p.getName()))
                .filter(action -> action.isPresent())
                .forEach(action -> privilegesMap.put(action.get(), group));
        }
        List<DeviceMessagePrivilegeInfo> infos = new ArrayList<>();
        for (DeviceMessageUserAction action : DeviceMessageUserAction.values()) {
            infos.add(DeviceMessagePrivilegeInfo.from(action, privilegesMap.get(action), thesaurus));
        }
        return PagedInfoList.asJson("privileges", infos, queryParameters);
    }

}
