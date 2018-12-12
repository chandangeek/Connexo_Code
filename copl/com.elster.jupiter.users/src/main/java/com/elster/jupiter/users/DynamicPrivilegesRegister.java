/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;


import aQute.bnd.annotation.ConsumerType;

import java.util.List;

@ConsumerType
public interface DynamicPrivilegesRegister {

    List<String> getPrivileges(String application);
}
