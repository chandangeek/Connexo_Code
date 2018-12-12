/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.dualcontrol.Privileges;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.PrivilegeThesaurus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.GrantRefusedException;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.dualcontrol.subscriber", service = {Subscriber.class}, property = {"name=" + DualControlService.COMPONENT_NAME}, immediate = true)
public class DualControlPrivilegeSubscriber implements Subscriber {

    private volatile UserService userService;
    private volatile Thesaurus thesaurus;
    private PrivilegeThesaurus privilegeThesaurus;
    private volatile ThreadPrincipalService threadPrincipalService;

    public DualControlPrivilegeSubscriber() {
        //for OSGI purposes
    }

    @Inject
    public DualControlPrivilegeSubscriber(Thesaurus thesaurus, PrivilegeThesaurus privilegeThesaurus, ThreadPrincipalService threadPrincipalService) {
        this.thesaurus = thesaurus;
        this.privilegeThesaurus = privilegeThesaurus;
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public void handle(Object notification, Object... notificationDetails) {
        if (notification instanceof User && notificationDetails.length == 2 && notificationDetails[0] instanceof Group && notificationDetails[1] instanceof Boolean) {
            User user = (User) notification;
            Group group = (Group) notificationDetails[0];
            boolean isJoin = (Boolean) notificationDetails[1];

            Set<Privilege> dualControlPrivileges = Stream.of(
                    user.getPrivileges().stream(),
                    group.getPrivileges(null).stream()
            )
                    .flatMap(Function.identity())
                    .filter(isDualControlApprove().or(isDualControlGrant()))
                    .collect(Collectors.toSet());
            if (dualControlPrivileges.stream().anyMatch(isDualControlApprove()) && dualControlPrivileges.stream().anyMatch(isDualControlGrant()) && isJoin) {
                Privilege approvePrivilege = dualControlPrivileges.stream().filter(isDualControlApprove()).findFirst().get();
                Privilege grantPrivilege = dualControlPrivileges.stream().filter(isDualControlGrant()).findFirst().get();
                throw new GrantRefusedException(MessageSeeds.CANT_COMBINE_ROLES_WITH_PRIVILEGES_X_AND_Y, "roles",
                        privilegeThesaurus.translatePrivilegeKey(grantPrivilege.getName()), privilegeThesaurus.translatePrivilegeKey(approvePrivilege.getName()));
            }
            if (!canCurrentUserGrantRole(group)) {
                throw new GrantRefusedException(MessageSeeds.CANT_GRANT_ROLE, "role");
            }
        }
        if (notification instanceof Group && notificationDetails.length == 1 && notificationDetails[0] instanceof Privilege) {
            Group group = (Group) notification;
            Privilege privilege = (Privilege) notificationDetails[0];

            if (isDualControlApprove().test(privilege)) {
                // no user in this group may already have dual control grant privilege
                boolean aUserAlreadyHasGrant = userService.findUsers(group)
                        .stream()
                        .map(User::getPrivileges)
                        .flatMap(Set::stream)
                        .anyMatch(isDualControlGrant());
                if (aUserAlreadyHasGrant) {
                    throw new GrantRefusedException(MessageSeeds.CANT_COMBINE_WITH_GRANT, "privileges"
                            , privilegeThesaurus.translatePrivilegeKey(privilege.getName()), privilegeThesaurus.translatePrivilegeKey(Privileges.GRANT_DUAL_CONTROL_APPROVAL.getKey()));
                }
            }
            if (isDualControlGrant().test(privilege)) {
                // no user in this group may already have dual control approve privilege
                boolean aUserAlreadyHasApprove = userService.findUsers(group)
                        .stream()
                        .map(User::getPrivileges)
                        .flatMap(Set::stream)
                        .anyMatch(isDualControlApprove());
                if (aUserAlreadyHasApprove) {
                    throw new GrantRefusedException(MessageSeeds.CANT_COMBINE_WITH_APPROVE, "privileges"
                            , privilegeThesaurus.translatePrivilegeKey(privilege.getName()));
                }
            }
        }
    }

    private Predicate<Privilege> isDualControlApprove() {
        return privilege -> privilege.getCategory().getName().equals(DualControlService.DUAL_CONTROL_APPROVE_CATEGORY);
    }

    private Predicate<Privilege> isDualControlGrant() {
        return privilege -> privilege.getCategory().getName().equals(DualControlService.DUAL_CONTROL_GRANT_CATEGORY);
    }

    private boolean canCurrentUserGrantRole(Group group) {
        Principal principal = threadPrincipalService.getPrincipal();
        User currentUser;
        if ((principal instanceof User)) {
            currentUser = (User) principal;
        } else {
            return principal.getName().equals("Installer") || principal.getName().equals("console");
        }

        boolean canGrantNormalPrivileges = currentUser.getPrivileges()
                .stream()
                .filter(privilege -> privilege.getName().equals(com.elster.jupiter.users.security.Privileges.ADMINISTRATE_USER_ROLE.getKey()))
                .findAny()
                .isPresent();
        boolean canGrantDualControlPrivileges = currentUser.getPrivileges().stream()
                .filter(privilege -> privilege.getName().equals(Privileges.GRANT_DUAL_CONTROL_APPROVAL.getKey()))
                .findAny()
                .isPresent();

        boolean groupContainsDualControlPrivileges = group.getPrivileges().entrySet().stream()
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .filter(privilege -> privilege.getCategory().getName().equals(DualControlService.DUAL_CONTROL_APPROVE_CATEGORY))
                .findAny()
                .isPresent();

        return canGrantNormalPrivileges && !groupContainsDualControlPrivileges || canGrantDualControlPrivileges && groupContainsDualControlPrivileges;
    }

    @Override
    public Class<?>[] getClasses() {
        return new Class<?>[]{
                User.class,
                Group.class
        };
    }

    @Reference(name = "AUserService")
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference(name = "ZNlsService")
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DualControlService.COMPONENT_NAME, Layer.DOMAIN.DOMAIN).join(userService.getThesaurus());
        this.privilegeThesaurus = nlsService.getPrivilegeThesaurus();
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }
}
