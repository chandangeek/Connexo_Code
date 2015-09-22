package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.users.*;
import com.elster.jupiter.util.conditions.*;

import javax.inject.Inject;
import java.util.*;

/**
 * Purpose for this command is to install a User having the 'Read Only' role: only 'view' privileges.
 * Copyrights EnergyICT
 * Date: 17/09/2015
 * Time: 9:46
 */
public class CreateDemoUserCommand {

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

        Group group = userService.findGroup(DEMO_USER_ROLE).orElseGet(this::createAndGrantViewPrivilegesToGroup);

        User user  = userService.findUser(userName).orElseGet(() -> userService.createUser(this.userName, "Demo User"));
        // Make the user a member of the 'Demo Users' group with all its privileges
        user.join(group);
        System.out.println("==> created 'Demo' user " + userName);
    }

    private Group createAndGrantViewPrivilegesToGroup(){
        Group group = userService.createGroup(DEMO_USER_ROLE, DEMO_USER_ROLE_DESCRIPTION);
        userService.getPrivilegeQuery().select(viewPrivilegesCondition(), Order.ascending("resource")).stream().forEach(x -> userService.grantGroupWithPrivilege(DEMO_USER_ROLE, APPLICATION, new String[] {x.getName()}));
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
