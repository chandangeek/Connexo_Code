package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.MessageSeed;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.google.inject.Provider;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ThesaurusImpl implements Thesaurus {

    private final ThreadPrincipalService threadPrincipalService;
    private final Map<String, NlsKeyImpl> translations = new HashMap<>();
    private final Provider<NlsKeyImpl> nlsKeyProvider;
    private String component;

    @Inject
    public ThesaurusImpl(ThreadPrincipalService threadPrincipalService, Provider<NlsKeyImpl> nlsKeyProvider) {
        this.threadPrincipalService = threadPrincipalService;
        this.nlsKeyProvider = nlsKeyProvider;
    }

    ThesaurusImpl init(String component, List<NlsKeyImpl> nlsKeys) {
        this.component = component;
        for (NlsKeyImpl nlsKey : nlsKeys) {
            translations.put(nlsKey.getKey(), nlsKey);
        }
        return this;
    }

    @Override
    public String getString(String key, String defaultMessage) {
        if (!translations.containsKey(key)) {
            return defaultMessage;
        }
        return translations.get(key).translate(getLocale()).or(defaultMessage);
    }

    @Override
    public String getString(Locale locale, String key, String defaultMessage) {
        if (!translations.containsKey(key)) {
            return defaultMessage;
        }
        return translations.get(key).translate(locale).or(defaultMessage);
    }

    private Locale getLocale() {
        return threadPrincipalService.getLocale();
    }

    @Override
    public void addTranslations(Iterable<? extends Translation> translations) {
        Map<NlsKey, List<Translation>> map = new HashMap<>();
        for (Translation translation : translations) {
            if (!map.containsKey(translation.getNlsKey())) {
                map.put(translation.getNlsKey(), new ArrayList<Translation>());
            }
            map.get(translation.getNlsKey()).add(translation);
        }
        for (Map.Entry<NlsKey, List<Translation>> entry : map.entrySet()) {
            NlsKeyImpl nlsKey = nlsKeyProvider.get().init(entry.getKey());
            for (Translation translation : entry.getValue()) {
                nlsKey.add(translation.getLocale(), translation.getTranslation());
            }
            nlsKey.save();
        }
    }


    @Override
    public NlsKey getTranslations(Locale local) {
        //TODO automatically generated method body, provide implementation.
        return null;
    }

    @Override
    public String getComponent() {
        //TODO automatically generated method body, provide implementation.
        return null;
    }

    @Override
    public NlsMessageFormat getFormat(MessageSeed seed) {
        //TODO automatically generated method body, provide implementation.
        return null;
    }
}
