package com.energyict.mdc.device.alarms.impl.event;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import com.energyict.mdc.device.alarms.impl.i18n.MessageSeeds;

import java.util.Arrays;
import java.util.stream.Collectors;

public class IncompleteEmailConfigurationException extends LocalizedException  {

    public IncompleteEmailConfigurationException(Thesaurus thesaurus, String... missedProperties){
        super(thesaurus, MessageSeeds.INCOMPLETE_MAIL_CONFIG,
                missedProperties != null ? Arrays.stream(missedProperties).collect(Collectors.joining("', '", "'", "'")) : "");
    }
}