package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Save;

import javax.validation.GroupSequence;
import javax.validation.groups.Default;

@GroupSequence({Default.class, Save.Create.class, Save.Update.class})
public interface ValidationRuleConstraintsSequence {
}
