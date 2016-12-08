package com.energyict.mdc.device.command.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.DualControlChangeInfo;
import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRulePendingUpdate;

import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<DualControlChangeInfo> changes;


}
