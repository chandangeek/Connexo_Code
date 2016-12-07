package com.energyict.mdc.device.command;

import java.util.List;

public interface CommandRuleTemplate extends ServerCommandRule {

    long getVersion();

    void save();

    void delete();
}
