package com.elster.jupiter.kore.api.impl.servicecall;

import java.util.Arrays;
import java.util.List;

public class CommandRunStatusInfo {
    public long id;
    public CommandStatus status;
    public List<CommandRunStatusInfo> childrenCommands;

    public CommandRunStatusInfo() {
    }

    public CommandRunStatusInfo(long id, CommandStatus status, CommandRunStatusInfo ... childrenCommands) {
        this.id = id;
        this.status = status;
        this.childrenCommands = Arrays.asList(childrenCommands);
    }
}
