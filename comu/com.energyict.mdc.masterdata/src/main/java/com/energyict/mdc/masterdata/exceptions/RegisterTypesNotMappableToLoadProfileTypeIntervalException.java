/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to add {@link RegisterType}s to a {@link LoadProfileType}
 * but one or all of the RegisterType could not be mapped to the LoadProfileType's interval.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (16:52)
 */
public class RegisterTypesNotMappableToLoadProfileTypeIntervalException extends LocalizedException {

    private final Collection<RegisterType> failedRegisterTypes;

    public RegisterTypesNotMappableToLoadProfileTypeIntervalException(Thesaurus thesaurus, LoadProfileType loadProfileType, Collection<RegisterType> registerTypes) {
        super(thesaurus, MessageSeeds.REGISTER_TYPES_AND_LOAD_PROFILE_TYPE_INTERVAL_NOT_SUPPORTED, toCommaSeparatedList(registerTypes));
        this.set("loadProfileType", loadProfileType);
        this.failedRegisterTypes = registerTypes;
    }

    public Collection<RegisterType> getFailedRegisterTypes() {
        return Collections.unmodifiableCollection(this.failedRegisterTypes);
    }

    private static String toCommaSeparatedList (Collection<RegisterType> registerTypes) {
        return registerTypes.stream().map(registerType -> registerType.getReadingType().getAliasName()).collect(Collectors.joining(", "));
    }

}