package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class ThesaurusImpl implements IThesaurus {

    private static final Pattern VALIDATION_KEY = Pattern.compile("^\\{(.*)\\}$");

    private final ThreadPrincipalService threadPrincipalService;
    private final Map<String, NlsKeyImpl> translations = new HashMap<>();
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
    public String getStringBeyondComponent(String key, String defaultMessage) {
        if (translations.containsKey(key)) {
            return translations.get(key).translate(getLocale()).orElse(defaultMessage);
        }
        Condition condition = Operator.EQUAL.compare("key", key);
        return dataModel.query(NlsKeyImpl.class, NlsEntry.class).select(condition).stream()
                .findFirst()
                .flatMap(k -> k.translate(getLocale()))
                .orElse(defaultMessage);
    }

    @Override
    public String getStringBeyondComponent(Locale locale, String key, String defaultMessage) {
        if (translations.containsKey(key)) {
            return translations.get(key).translate(locale).orElse(defaultMessage);
        }
        Condition condition = Operator.EQUAL.compare("key", key);
        return dataModel.query(NlsKeyImpl.class, NlsEntry.class).select(condition).stream()
                .findFirst()
                .flatMap(k -> k.translate(locale))
                .orElse(defaultMessage);
    }

    @Override
    public String getString(String key, String defaultMessage) {
        if (translations.isEmpty()) {
            initTranslations(component, layer);
        }
        if (!translations.containsKey(key)) {
            if (!translations.containsKey(key)) {
                return defaultMessage;
            }
        }
        return translations.get(key).translate(getLocale()).orElse(defaultMessage);
    }

    @Override
    public String getString(Locale locale, String key, String defaultMessage) {
        if (!translations.containsKey(key)) {
            return defaultMessage;
        }
        return translations.get(key).translate(locale).orElse(defaultMessage);
    }

    public Locale getLocale() {
        return threadPrincipalService.getLocale();
    }

    @Override
    public void addTranslations(Iterable<? extends Translation> translations) {
        Map<NlsKey, List<Translation>> map = new HashMap<>();
        for (Translation translation : translations) {
            if (!map.containsKey(translation.getNlsKey())) {
                map.put(translation.getNlsKey(), new ArrayList<>());
            }
            map.get(translation.getNlsKey()).add(translation);
        }
        for (Map.Entry<NlsKey, List<Translation>> entry : map.entrySet()) {
            NlsKey entryKey = entry.getKey();
            Optional<NlsKey> found = dataModel.mapper(NlsKey.class).getOptional(entryKey.getComponent(), entryKey.getLayer(), entryKey.getKey());
            NlsKeyImpl nlsKey = (NlsKeyImpl) found.orElseGet(() -> nlsKeyProvider.get().init(entryKey));
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

    void createNewTranslationKeys(TranslationKeyProvider provider) {
        initTranslations(component, layer);
        List<NlsKey> newKeys = provider.getKeys().stream().filter(tk -> !translations.containsKey(tk.getKey())).map(this::newNlsKey).collect(Collectors.toList());

        if (!newKeys.isEmpty()) {
            // remove duplicate keys
            Set<String> uniqueIds = new HashSet<>();
            List<NlsKey> uniqueKeys = new ArrayList<>();

            for (NlsKey key : newKeys) {
                if (uniqueIds.add(key.getKey())) {
                    uniqueKeys.add(key);
                }
            }

            dataModel.mapper(NlsKey.class).persist(uniqueKeys);
            initTranslations(component, layer);
        }

    }

    private NlsKey newNlsKey(TranslationKey translationKey) {
        NlsKeyImpl nlsKey = nlsKeyProvider.get().init(component, layer, translationKey.getKey());
        nlsKey.setDefaultMessage(translationKey.getDefaultFormat());
        nlsKey.add(Locale.ENGLISH, translationKey.getDefaultFormat());
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
        return NlsString.from(this, key, defaultFormat);
    }

    private NlsString nlsStringFor(TranslationKey translationKey) {
        String key = translationKey.getKey();
        String defaultFormat = translationKey.getDefaultFormat();
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
        return new CompositeThesaurus(threadPrincipalService, this, thesaurus);
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
}
