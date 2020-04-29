package com.elster.jupiter.issue.impl.module;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.util.Arrays;
import java.util.stream.Collectors;

public class IncompleteEmailConfigException extends LocalizedException  {

    public IncompleteEmailConfigException(Thesaurus thesaurus, String... missedProperties){
        super(thesaurus, MessageSeeds.INCOMPLETE_MAIL_CONFIG,
                missedProperties != null ? Arrays.stream(missedProperties).collect(Collectors.joining("', '", "'", "'")) : "");
    }
}

