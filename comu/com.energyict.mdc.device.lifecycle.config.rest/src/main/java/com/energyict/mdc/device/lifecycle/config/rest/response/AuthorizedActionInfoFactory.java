package com.energyict.mdc.device.lifecycle.config.rest.response;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;

import javax.inject.Inject;
import java.util.Objects;

public class AuthorizedActionInfoFactory {

    private final Thesaurus thesaurus;

    @Inject
    public AuthorizedActionInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public AuthorizedActionInfo from(AuthorizedAction action){
        Objects.requireNonNull(action);
        return new AuthorizedActionInfo(thesaurus, action);
    }
}
