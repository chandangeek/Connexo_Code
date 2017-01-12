package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    CANT_COMBINE_WITH_GRANT(1, Constants.CANT_COMBINE_WITH_GRANT, "Can''t grant the ''{0}'' privilege to this role as one or more users with this role has the ''{1}'' privilege."),
    CANT_COMBINE_WITH_APPROVE(2, Constants.CANT_COMBINE_WITH_APPROVE, "Can''t grant the ''{0}'' privilege to this role as one or more users with this role already has an ''Approve'' privilege."),
    CANT_COMBINE_ROLES_WITH_PRIVILEGES_X_AND_Y(3, Constants.CANT_COMBINE_ROLES_WITH_PRIVILEGES_X_AND_Y, "For security reasons you can''t combine user roles with ''{0}'' and ''{1}'' privileges."),
    CANT_GRANT_ROLE(4, Constants.CANT_GRANT_ROLE, "Current user can''t grant/revoke the given role(s)");
    private final int number;
    private final String key;
    private final String format;

    MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = key;
        this.format = format;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return format;
    }

    @Override
    public String getModule() {
        return DualControlService.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

    public interface Constants {
        String CANT_COMBINE_WITH_GRANT = "dualControl.cantCombineWithGrant";
        String CANT_COMBINE_WITH_APPROVE = "dualControl.cantCombineWithApprove";
        String CANT_COMBINE_ROLES_WITH_PRIVILEGES_X_AND_Y = "dualControl.cantCombineRolesWithPrivilegesXandY";
        String CANT_GRANT_ROLE = "dualControl.cantGrantRole";
    }
}
