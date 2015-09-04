package com.energyict.mdc.pluggable.exceptions;

import com.energyict.mdc.pluggable.PluggableClassType;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a {@link com.energyict.mdc.pluggable.PluggableClass}
 * but another one of the same type and name already exists.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (15:35)
 */
public class DuplicateNameException extends LocalizedException {

    public DuplicateNameException(MessageSeed messageSeed, Thesaurus thesaurus, String name, PluggableClassType type) {
        super(thesaurus, messageSeed, name, thesaurus.getString(type.name(), type.name()));
        this.set("name", name);
        this.set("pluggableClassType", type.name());
    }

}