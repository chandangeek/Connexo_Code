package com.elster.jupiter.users;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;


public class LdapServerException extends LocalizedException{

    public LdapServerException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.NO_LDAP_FOUND);

    }
}
