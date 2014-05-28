package com.elster.jupiter.issue.share.cep;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class ActionLoadFailedException extends LocalizedException {

    private static final long serialVersionUID = 7895612729401720690L;

    public ActionLoadFailedException(Thesaurus thesaurus, Object... args) {
        super(thesaurus, MessageSeeds.ISSUE_ACTION_CLASS_LOAD_FAIL, args);
    }
}
