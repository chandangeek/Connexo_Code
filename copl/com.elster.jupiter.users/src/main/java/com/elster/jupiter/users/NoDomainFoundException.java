package com.elster.jupiter.users;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class NoDomainFoundException extends LocalizedException {

    public NoDomainFoundException(Thesaurus thesaurus, String domain) {
        super(thesaurus, MessageSeeds.NO_REALM_FOUND, domain);

        // Set additional parameters here
        //set("domain", domain);
    }
}
