package com.energyict.mdc.device.command.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandRuleInfo {
    public long id;
    public String name;
    public boolean active;
    public long dayLimit;
    public long weekLimit;
    public long monthLimit;
    public long version;
    public String statusMessage;
    public List<CommandInfo> commands = new ArrayList<>();
    public DualControlInfo dualControl;
    public EnumSet<AvailableActions> availableActions;
    public List<CurrentCountInfo> currentCounts;
}
