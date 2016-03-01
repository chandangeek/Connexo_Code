package com.elster.jupiter.servicecall;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class CannotDeleteServiceCallType extends LocalizedException {

    public CannotDeleteServiceCallType(Thesaurus thesaurus, MessageSeed messageSeed, ServiceCallType serviceCallType, ServiceCall callOfThatType) {
        super(thesaurus, messageSeed, thesaurus.getString(serviceCallType.getName() + ' ' + serviceCallType.getVersionName(), callOfThatType.getNumber()));
    }
}
