package com.elster.jupiter.dualcontrol;

import org.osgi.service.useradmin.User;

public interface UserOperation {
    User getUser();

    UserAction getAction();
}
