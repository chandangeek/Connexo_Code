package com.elster.jupiter.users;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class NoDefaultDomainException extends LocalizedException {

    public NoDefaultDomainException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.NO_DEFAULT_REALM);
    }
}
