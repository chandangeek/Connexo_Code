/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.impl.servicecall;

import java.util.Arrays;
import java.util.List;

public class CommandRunStatusInfo {
    public String id;
    public CommandStatus status;
    public String system;
    public List<CommandRunStatusInfo> childrenCommands;

    public CommandRunStatusInfo() {
    }

    public CommandRunStatusInfo(String id, CommandStatus status, CommandRunStatusInfo... childrenCommands) {
        this.id = id;
        this.status = status;
        this.childrenCommands = Arrays.asList(childrenCommands);
    }

    public CommandRunStatusInfo(String id, CommandStatus status, String system) {
        this.id = id;
        this.status = status;
        this.system = system;

    }
}
