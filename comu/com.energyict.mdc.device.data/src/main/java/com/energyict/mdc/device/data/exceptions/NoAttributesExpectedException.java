package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when
 * an attempt is made to add properties to a object that
 * use a pluggable class that does not define any properties.
 * That is done from the process that manages the properties of pluggable classes.
 * So if this happens, it is clear that that process is buggy.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (16:25)
 */
public class NoAttributesExpectedException extends LocalizedException {

    public NoAttributesExpectedException(Thesaurus thesaurus, String propertyName, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
        this.set("propertyName", propertyName);
    }

}