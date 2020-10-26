/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.tasks;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class AddCertificateRequestDataTaskException extends LocalizedException {

    public AddCertificateRequestDataTaskException(Thesaurus thesaurus, MessageSeed messageSeed, String error) {
        super(thesaurus, messageSeed, error);
    }
}
