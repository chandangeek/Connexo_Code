package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.common.base.Optional;
import com.google.inject.Provider;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

class ThesaurusImpl implements Thesaurus {

    private final ThreadPrincipalService threadPrincipalService;
    private final Map<String, NlsKeyImpl> translations = new HashMap<>();
    private final Provider<NlsKeyImpl> nlsKeyProvider;
    private final DataModel dataModel;
    private Layer layer;
    private String component;

    @Inject
    public ThesaurusImpl(DataModel dataModel, ThreadPrincipalService threadPrincipalService, Provider<NlsKeyImpl> nlsKeyProvider) {
        this.dataModel = dataModel;
        this.threadPrincipalService = threadPrincipalService;
        this.nlsKeyProvider = nlsKeyProvider;
    }

    ThesaurusImpl init(String component, Layer layer) {
        this.component = component;
        this.layer = layer;
        return this;
    }

    private void initTranslations(String component, Layer layer) {
        translations.clear();
        for (NlsKeyImpl nlsKey : getNlsKeys(component, layer)) {
            translations.put(nlsKey.getKey(), nlsKey);
        }
    }

    private List<NlsKeyImpl> getNlsKeys(String componentName, Layer layer) {
        if (!dataModel.isInstalled()) {
            return Collections.emptyList();
        }
        Condition condition = Operator.EQUAL.compare("layer", layer).and(Operator.EQUAL.compare("componentName", componentName));
        return dataModel.query(NlsKeyImpl.class, NlsEntry.class).select(condition);
    }

    @Override
    public String getString(String key, String defaultMessage) {
        if (!translations.containsKey(key)) {
            initTranslations(component, layer);
            if (!translations.containsKey(key)) {
                return defaultMessage;
            }
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

    Locale getLocale() {
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
            NlsKey entryKey = entry.getKey();
            Optional<NlsKey> found = dataModel.mapper(NlsKey.class).getOptional(entryKey.getComponent(), entryKey.getLayer(), entryKey.getKey());
            NlsKeyImpl nlsKey = (NlsKeyImpl) found.or(nlsKeyProvider.get().init(entryKey));
            nlsKey.clearTranslations();
            for (Translation translation : entry.getValue()) {
                nlsKey.add(translation.getLocale(), translation.getTranslation());
                if (Locale.ENGLISH.equals(translation.getLocale())) {
                    nlsKey.setDefaultMessage(translation.getTranslation());
                }
            }
            nlsKey.save();
            this.translations.put(nlsKey.getKey(), nlsKey);
        }
    }

    @Override
    public NlsMessageFormat getFormat(MessageSeed seed) {
        return new NlsMessageFormatImpl(this, seed.getNumber(), nlsStringFor(seed), seed.getLevel());
    }

    private NlsString nlsStringFor(MessageSeed seed) {
        String key = seed.getKey();
        String defaultFormat = seed.getDefaultFormat();
        return NlsString.from(this, key, defaultFormat);
    }

    @Override
    public String getComponent() {
        return component;
    }

    @Override
    public Map<String, String> getTranslations() {
        if (translations.isEmpty()) {
            initTranslations(component, layer);
        }
        Map<String, String> map = new TreeMap<>();
        for (Map.Entry<String, NlsKeyImpl> entry : translations.entrySet()) {
            map.put(entry.getKey(), translate(entry.getValue()));
        }
        return map;
    }

    private String translate(NlsKeyImpl nlsKey) {
        return nlsKey.translate(getLocale()).or(nlsKey.getDefaultMessage());
    }
}
