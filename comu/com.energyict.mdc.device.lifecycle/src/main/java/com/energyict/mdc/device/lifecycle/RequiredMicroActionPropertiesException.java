package com.energyict.mdc.device.lifecycle;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Models the exceptional situation that occurs when
 * an {@link com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction}
 * is executed by the user but some of the required {@link PropertySpec}s
 * of the {@link com.energyict.mdc.device.lifecycle.config.MicroCheck}s
 * that are configured on the action are missing.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-23 (16:50)
 */
public class RequiredMicroActionPropertiesException extends DeviceLifeCycleActionViolationException {

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