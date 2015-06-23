package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.text.MessageFormat;
import java.util.Objects;

public enum DefaultTranslationKey implements TranslationKey {
    DEVICE_LIFE_CYCLE_STATE_SUCCESSFUL_CHANGED("DeviceLifeCycleStateSuccessfulChanged" , "Successfully changed device state to '{0}'"),
    PRE_TRANSITION_CHECKS_FAILED("PreTransitionChecksFailed" , "Pretransition checks failed"),
    ;

    private String key;
    private String defaultFormat;

    DefaultTranslationKey(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public String formatWith(Thesaurus thesaurus, Object... arguments){
        Objects.requireNonNull(thesaurus);
        String translated = thesaurus.getString(getKey(), getDefaultFormat());
        return new MessageFormat(translated).format(arguments, new StringBuffer(), null).toString();
    }

    public String translateWith(Thesaurus thesaurus){
        return thesaurus.getString(getKey(), getDefaultFormat());
    }
}
