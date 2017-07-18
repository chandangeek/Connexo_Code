/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.builders.UserBuilder;
import com.elster.jupiter.demo.impl.templates.DemoUserTpl;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Order;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class CreateDemoUserCommand extends CommandWithTransaction {

    private final static String APPLICATION = "MDC";
    private final static String DEMO_USER_ROLE = "Demo Users";
    private final static String DEMO_USER_ROLE_DESCRIPTION = "Demo Users only have 'view' privileges.";

    private UserService userService;
    private String userName;

    @Inject
    public CreateDemoUserCommand(UserService userService){
          this.userService = userService;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void run(){
        if (userName == null){
            throw new IllegalStateException("User name is not set");
        }
        //Create the 'Demo User' group
        userService.findGroup(DEMO_USER_ROLE).orElseGet(this::createAndGrantViewPrivilegesToGroup);
        //Create the user and make him a member of the 'Demo User' group
        UserBuilder builder = new DemoUserTpl(userName).get(new UserBuilder(userService)).withRoles(DEMO_USER_ROLE);
        builder.get();

        System.out.println("==> created 'Demo' user " + userName);
    }

    private Group createAndGrantViewPrivilegesToGroup(){
        Group group = userService.createGroup(DEMO_USER_ROLE, DEMO_USER_ROLE_DESCRIPTION);
        userService.getPrivilegeQuery().select(viewPrivilegesCondition(), Order.ascending("resource"))
                .forEach(x -> userService.grantGroupWithPrivilege(DEMO_USER_ROLE, APPLICATION, new String[] {x.getName()}));
        return group;
    }

    private Condition viewPrivilegesCondition(){
        Condition viewPrivileges = Operator.LIKEIGNORECASE.compare("name", "%.view.%");
        Condition specialCases = ListOperator.IN.contains("name", specialCases());
        return viewPrivileges.or(specialCases);
    }

    // As there is not a common strategy to name the privileges
    private List<String> specialCases(){
        return Arrays.asList("MTR_BROWSE_OWNUSAGEPOINT", "MTR_BROWSE_ANYUSAGEPOINT");
    }

}
