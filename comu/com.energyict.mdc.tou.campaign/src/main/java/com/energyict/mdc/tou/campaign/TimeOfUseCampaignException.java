/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class TimeOfUseCampaignException extends LocalizedException {
    public TimeOfUseCampaignException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

    public TimeOfUseCampaignException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public TimeOfUseCampaignException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause) {
        super(thesaurus, messageSeed, cause);
    }

    public TimeOfUseCampaignException(Thesaurus thesaurus, MessageSeed messageSeed, Throwable cause, Object... args) {
        super(thesaurus, messageSeed, cause, args);
    }
}
