package com.elster.jupiter.kore.api.impl.servicecall;

import java.util.Arrays;
import java.util.List;

public class CommandRunStatusInfo {
    public long id;
    public CommandStatus status;
    public String system;
    public List<CommandRunStatusInfo> childrenCommands;

    public CommandRunStatusInfo() {
    }

    public CommandRunStatusInfo(long id, CommandStatus status, CommandRunStatusInfo... childrenCommands) {
        this.id = id;
        this.status = status;
        this.childrenCommands = Arrays.asList(childrenCommands);
    }

    public CommandRunStatusInfo(long id, CommandStatus status, String system) {
        this.id = id;
        this.status = status;
        this.system = system;

    }
}
