/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class RequiredMicroActionPropertiesException extends UsagePointLifeCycleActionViolationException {

    private final Thesaurus thesaurus;
    private final MessageSeed messageSeed;
    private final Set<String> propertySpecNames;

    public RequiredMicroActionPropertiesException(Thesaurus thesaurus, MessageSeed messageSeed, Set<String> propertySpecNames) {
        super();
        this.thesaurus = thesaurus;
        this.messageSeed = messageSeed;
        this.propertySpecNames = propertySpecNames;
    }

    @Override
    public String getLocalizedMessage() {
        return this.thesaurus.getFormat(this.messageSeed).format(this.propertySpecNamesAsCommaSeparatedList());
    }

    public Set<String> getViolatedPropertySpecNames(){
        return Collections.unmodifiableSet(this.propertySpecNames);
    }

    private String propertySpecNamesAsCommaSeparatedList() {
        return this.propertySpecNames
                .stream()
                .collect(Collectors.joining(", "));
    }

}