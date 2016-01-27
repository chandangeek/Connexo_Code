package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import java.util.function.Supplier;

/**
 * Models all the exceptions indicating the object could not be found
 *
 * @author sva
 * @since 27/01/2016 - 8:47
 */
public class NoSuchElementException extends LocalizedException implements Supplier<NoSuchElementException> {

    protected NoSuchElementException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    @Override
    public NoSuchElementException get() {
        return this;
    }

    public static NoSuchElementException deviceWithMRIDNotFound(Thesaurus thesaurus, String mRID) {
        return new NoSuchElementException(thesaurus, MessageSeeds.NO_SUCH_DEVICE, mRID);
    }

    public static NoSuchElementException comTaskEnablementWithIdNotFound(Thesaurus thesaurus, long comTaskEnablementId) {
        return new NoSuchElementException(thesaurus, MessageSeeds.NO_SUCH_COMTASK_ENABLEMENT, comTaskEnablementId);
    }
}