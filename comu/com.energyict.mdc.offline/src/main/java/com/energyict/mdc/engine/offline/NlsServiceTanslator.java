package com.energyict.mdc.engine.offline;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.IThesaurus;
import com.energyict.mdc.engine.offline.core.Translator;

public class NlsServiceTanslator implements Translator {

    public static final String MISSING_RESOURCE_PREFIX = "MR";
    public static final String NO_RESOURCE_PREFIX = "NR";

    private Thesaurus thesaurus;

    public NlsServiceTanslator(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public boolean hasTranslation(String key) {
        String translation = getTranslation(key);
        return !translation.startsWith(MISSING_RESOURCE_PREFIX) && !translation.startsWith(NO_RESOURCE_PREFIX);
    }

    @Override
    public String getTranslation(String key) {
        return thesaurus.getString(key, MISSING_RESOURCE_PREFIX + key);
    }

    @Override
    public String getTranslation(String key, boolean flagError) {
        return thesaurus.getString(key, flagError ? MISSING_RESOURCE_PREFIX + key : key);
    }

    @Override
    public String getTranslation(String key, String defaultValue) {
        return thesaurus.getString(key, defaultValue);
    }

    @Override
    public String getErrorCode(String messageId) {
        return getTranslation(messageId);
    }

    @Override
    public String getErrorMsg(String key) {
        return getTranslation(key);
    }

    @Override
    public String getCustomTranslation(String key) {
        return getTranslation(key);
    }



}
