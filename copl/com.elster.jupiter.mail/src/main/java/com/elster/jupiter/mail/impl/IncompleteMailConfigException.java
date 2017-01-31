/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mail.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.util.Arrays;
import java.util.stream.Collectors;

public class IncompleteMailConfigException extends LocalizedException {
    public IncompleteMailConfigException(Thesaurus thesaurus, String... missedProperties){
        super(thesaurus, MessageSeeds.INCOMPLETE_MAIL_CONFIG,
                missedProperties != null ? Arrays.stream(missedProperties).collect(Collectors.joining("', '", "'", "'")) : "");
    }
}
