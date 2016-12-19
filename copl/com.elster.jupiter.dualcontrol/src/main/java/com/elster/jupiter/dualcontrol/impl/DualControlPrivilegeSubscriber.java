package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.users.GrantRefusedException;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.dualcontrol.subscriber", service = {Subscriber.class}, property = {"name=" + DualControlService.COMPONENT_NAME}, immediate = true)
public class DualControlPrivilegeSubscriber implements Subscriber {

    private volatile UserService userService;

    @Override
    public void handle(Object notification, Object... notificationDetails) {
        if (notification instanceof User && notificationDetails.length == 1 && notificationDetails[0] instanceof Group) {
            User user = (User) notification;
            Group group = (Group) notificationDetails[0];

            Set<PrivilegeCategory> dualControlCategories = Stream.of(
                    user.getPrivileges().stream(),
                    group.getPrivileges(null).stream()
            )
                    .flatMap(Function.identity())
                    .map(Privilege::getCategory)
                    .filter(isDualControlApprove().or(isDualControlGrant()))
                    .collect(Collectors.toSet());
            if (dualControlCategories.stream().anyMatch(isDualControlApprove()) && dualControlCategories.stream().anyMatch(isDualControlGrant())) {
                throw new GrantRefusedException();
            }
        }
        if (notification instanceof Group && notificationDetails.length == 1 && notificationDetails[0] instanceof Privilege) {
            Group group = (Group) notification;
            Privilege privilege = (Privilege) notificationDetails[0];

            if (isDualControlApprove().test(privilege.getCategory())) {
                // no user in this group may already have dual control grant privilege
                boolean aUserAlreadyHasGrant = userService.findUsers(group)
                        .stream()
                        .map(User::getPrivileges)
                        .flatMap(Set::stream)
                        .map(Privilege::getCategory)
                        .anyMatch(isDualControlGrant());
                if (aUserAlreadyHasGrant) {
                    throw new GrantRefusedException();
                }
            }
            if (isDualControlGrant().test(privilege.getCategory())) {
                // no user in this group may already have dual control approve privilege
                boolean aUserAlreadyHasApprove = userService.findUsers(group)
                        .stream()
                        .map(User::getPrivileges)
                        .flatMap(Set::stream)
                        .map(Privilege::getCategory)
                        .anyMatch(isDualControlApprove());
                if (aUserAlreadyHasApprove) {
                    throw new GrantRefusedException();
                }
            }
        }
    }

    private Predicate<PrivilegeCategory> isDualControlApprove() {
        return category -> category.getName().equals(DualControlService.DUAL_CONTROL_APPROVE_CATEGORY);
    }

    private Predicate<PrivilegeCategory> isDualControlGrant() {
        return category -> category.getName().equals(DualControlService.DUAL_CONTROL_GRANT_CATEGORY);
    }

    @Override
    public Class<?>[] getClasses() {
        return new Class<?>[]{
                User.class,
                Group.class
        };
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
