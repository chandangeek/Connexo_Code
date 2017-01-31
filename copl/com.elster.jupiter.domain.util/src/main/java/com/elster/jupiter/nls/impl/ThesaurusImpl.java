/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.inject.Provider;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class ThesaurusImpl implements IThesaurus {

    private static final Pattern VALIDATION_KEY = Pattern.compile("^\\{(.*)\\}$");

    private final ThreadPrincipalService threadPrincipalService;
    private Map<String, NlsKeyImpl> translations;
    private final Provider<NlsKeyImpl> nlsKeyProvider;
    private final DataModel dataModel;
    private Layer layer;
    private String component;

    @Inject
    ThesaurusImpl(DataModel dataModel, ThreadPrincipalService threadPrincipalService, Provider<NlsKeyImpl> nlsKeyProvider) {
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
        this.translations = new HashMap<>();
        for (NlsKeyImpl nlsKey : getNlsKeys(component, layer)) {
            translations.put(nlsKey.getKey(), nlsKey);
        }
    }

    private void updateTranslations(List<NlsKeyImpl> newKeys) {
        this.ensureTranslationsLoaded();
        for (NlsKeyImpl nlsKey : newKeys) {
            translations.put(nlsKey.getKey(), nlsKey);
        }
    }

    private List<NlsKeyImpl> getNlsKeys(String componentName, Layer layer) {
        Condition condition = Operator.EQUAL.compare("layer", layer).and(Operator.EQUAL.compare("componentName", componentName));
        return dataModel.query(NlsKeyImpl.class, NlsEntry.class).select(condition);
    }

    @Override
    public String getString(String key, String defaultMessage) {
        return this.getString(getLocale(), key, defaultMessage);
    }

    @Override
    public String getString(Locale locale, String key, String defaultMessage) {
        this.ensureTranslationsLoaded();
        if (!translations.containsKey(key)) {
            return defaultMessage;
        }
        return translations.get(key).translate(locale).orElse(defaultMessage);
    }

    private void ensureTranslationsLoaded() {
        if (translations == null) {
            initTranslations(component, layer);
        }
    }

    public Locale getLocale() {
        return threadPrincipalService.getLocale();
    }

    @Override
    public void invalidate() {
        translations = null;
    }

    void createNewTranslationKeys(TranslationKeyProvider provider, Languages languages) {
        List<NlsKeyImpl> newKeys = new ArrayList<>();
        List<NlsKeyImpl> updateCandidates = new ArrayList<>();
        initTranslations(this.component, this.layer);
        provider.getKeys().forEach(translation -> {
            NlsKeyImpl nlsKey = this.translations.get(translation.getKey());
            if (nlsKey == null) {
                newKeys.add(newNlsKey(translation.getKey(), translation.getDefaultFormat()));
            } else {
                updateCandidates.add(nlsKey);
            }
        });
        this.addNewTranslations(newKeys, languages);
        this.updateExistingTranslations(updateCandidates, languages);
    }

    private void addNewTranslations(List<NlsKeyImpl> nlsKeys, Languages languages) {
        if (!nlsKeys.isEmpty()) {
            Set<String> uniqueIds = new HashSet<>();
            List<NlsKeyImpl> uniqueKeys = nlsKeys.stream().filter(key -> uniqueIds.add(key.getKey())).collect(Collectors.toList());
            languages.addTranslationsTo(uniqueKeys);
            dataModel.mapper(NlsKeyImpl.class).persist(uniqueKeys);
            updateTranslations(nlsKeys);
        }
    }

    private void updateExistingTranslations(List<NlsKeyImpl> nlsKeys, Languages languages) {
        nlsKeys.forEach(nlsKey -> this.updateExisting(nlsKey, languages));
    }

    private void updateExisting(NlsKeyImpl nlsKey, Languages languages) {
        languages.addTranslationsTo(nlsKey);
    }

    private NlsKeyImpl newNlsKey(String key, String defaultFormat) {
        NlsKeyImpl nlsKey = nlsKeyProvider.get().init(component, layer, key);
        nlsKey.setDefaultMessage(defaultFormat);
        nlsKey.add(Locale.ENGLISH, defaultFormat);
        return nlsKey;
    }

    @Override
    public NlsMessageFormat getFormat(MessageSeed seed) {
        return new NlsMessageFormatImpl(this, seed.getNumber(), nlsStringFor(seed), seed.getLevel());
    }

    @Override
    public NlsMessageFormat getFormat(TranslationKey key) {
        return new NlsTranslationFormatImpl(this, nlsStringFor(key));
    }

    private NlsString nlsStringFor(MessageSeed seed) {
        String key = seed.getKey();
        String defaultFormat = seed.getDefaultFormat();
        return NlsString.from(this, seed.getModule(), key, defaultFormat);
    }

    private NlsString nlsStringFor(TranslationKey translationKey) {
        String key = translationKey.getKey();
        String defaultFormat = translationKey.getDefaultFormat();
        return NlsString.from(this, getComponent(), key, defaultFormat);
    }

    @Override
    public String getComponent() {
        return component;
    }

    @Override
    public Map<String, String> getTranslationsForCurrentLocale() {
        this.ensureTranslationsLoaded();
        Map<String, String> map = new TreeMap<>();
        for (Map.Entry<String, NlsKeyImpl> entry : translations.entrySet()) {
            map.put(entry.getKey(), translate(entry.getValue()));
        }
        return map;
    }

    @Override
    public String interpolate(String messageTemplate, Context context) {
        String key = messageTemplate;
        Matcher matcher = VALIDATION_KEY.matcher(messageTemplate);
        if (matcher.matches()) {
            key = matcher.group(1);
        }
        String pattern = getString(key, key);
        for (Map.Entry<String, Object> entry : context.getConstraintDescriptor().getAttributes().entrySet()) {
            pattern = pattern.replaceAll("\\Q{" + entry.getKey() + "}\\E", Objects.toString(entry.getValue()));
        }
        return MessageFormat.format(pattern, context.getValidatedValue());
    }

    @Override
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        String key = messageTemplate;
        Matcher matcher = VALIDATION_KEY.matcher(messageTemplate);
        if (matcher.matches()) {
            key = matcher.group(1);
        }
        return MessageFormat.format(getString(locale, key, key), context.getValidatedValue());
    }

    @Override
    public Thesaurus join(Thesaurus thesaurus) {
        if (thesaurus instanceof CompositeThesaurus) {
            return thesaurus.join(this);
        }
        return new CompositeThesaurus(threadPrincipalService, this, (IThesaurus) thesaurus);
    }

    @Override
    public DateTimeFormatter forLocale(DateTimeFormatter dateTimeFormatter) {
        if (Objects.equals(dateTimeFormatter.getLocale(), getLocale())) {
            return dateTimeFormatter;
        }
        return dateTimeFormatter.withLocale(getLocale());
    }

    private String translate(NlsKeyImpl nlsKey) {
        return nlsKey.translate(getLocale()).orElse(nlsKey.getDefaultMessage());
    }

    @Override
    public boolean hasKey(String key){
        this.ensureTranslationsLoaded();
        return this.translations.entrySet()
                .stream()
                .filter(e -> e.getKey().equals(key))
                .findFirst()
                .isPresent();
    }

}