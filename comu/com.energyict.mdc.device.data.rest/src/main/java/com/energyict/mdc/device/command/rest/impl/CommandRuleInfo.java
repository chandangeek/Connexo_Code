package com.energyict.mdc.device.command.rest.impl;

import java.util.ArrayList;
import java.util.List;

public class CommandRuleInfo {
    public long id;
    public String name;
    public boolean active;
    public long dayLimit;
    public long weekLimit;
    public long monthLimit;
    public String statusMessage;
    public long version;
    public List<CommandInfo> commands = new ArrayList<>();
}
