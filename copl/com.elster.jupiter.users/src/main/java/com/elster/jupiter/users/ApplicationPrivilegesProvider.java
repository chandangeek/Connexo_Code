/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

import java.util.List;

public interface ApplicationPrivilegesProvider{
    List<String> getApplicationPrivileges();
    String getApplicationName();
}
