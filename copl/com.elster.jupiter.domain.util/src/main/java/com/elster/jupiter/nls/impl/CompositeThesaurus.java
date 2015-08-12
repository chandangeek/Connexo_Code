package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class CompositeThesaurus implements IThesaurus {

    private final List<Thesaurus> components = new ArrayList<>();
    private final ThreadPrincipalService threadPrincipalService;

    public CompositeThesaurus(ThreadPrincipalService threadPrincipalService, Thesaurus... thesauruses) {
        this.threadPrincipalService = threadPrincipalService;
        this.components.addAll(Arrays.asList(thesauruses));
    }

    @Override
    public String getStringBeyondComponent(String key, String defaultMessage) {
        return components.stream().findFirst().map(thesaurus -> thesaurus.getStringBeyondComponent(key, defaultMessage)).orElse(defaultMessage);
    }

    @Override
    public String getStringBeyondComponent(Locale locale, String key, String defaultMessage) {
        return components.stream().findFirst().map(thesaurus -> thesaurus.getStringBeyondComponent(locale, key, defaultMessage)).orElse(defaultMessage);
    }

    @Override
    public String getString(String key, String defaultMessage) {
        return components.stream()
                .map(th -> th.getString(key, null))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(defaultMessage);
    }

    @Override
    public String getString(Locale locale, String key, String defaultMessage) {
        return components.stream()
                .map(th -> th.getString(locale, key, null))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(defaultMessage);
    }

    @Override
    public String getComponent() {
        return null;
    }

    @Override
    public NlsMessageFormat getFormat(MessageSeed seed) {
        return components.stream()
                .filter(th -> th.getComponent().equals(seed.getModule()))
                .map(th -> th.getFormat(seed))
                .findAny()
                .orElseGet(() -> new NlsMessageFormatImpl(this, seed.getNumber(), nlsStringFor(seed), seed.getLevel()));

    }

    @Override
    public NlsMessageFormat getSimpleFormat(TranslationKey key) {
        return components.stream()
                .map(th -> th.getSimpleFormat(key))
                .findAny()
                .orElseGet(() -> new NlsTranslationFormatImpl(this, nlsStringFor(key)));
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
    public void addTranslations(Iterable<? extends Translation> translations) {
        components.stream().findFirst().ifPresent(th -> th.addTranslations(translations));
    }

    @Override
    public Map<String, String> getTranslations() {
        return components.stream()
                .map(Thesaurus::getTranslations)
                .collect(HashMap::new, (m1, m2) -> m1.putAll(m2), (m1, m2) -> m1.putAll(m2));
    }

    @Override
    public String interpolate(String messageTemplate, Context context) {
        return components.stream().findFirst().map(th -> th.interpolate(messageTemplate, context)).orElseThrow(IllegalStateException::new);
    }

    @Override
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        return components.stream().findFirst().map(th -> th.interpolate(messageTemplate, context, locale)).orElseThrow(IllegalStateException::new);
    }

    @Override
    public Locale getLocale() {
        return threadPrincipalService.getLocale();
    }

    @Override
    public Thesaurus join(Thesaurus thesaurus) {
        if (thesaurus instanceof CompositeThesaurus) {
            components.addAll(((CompositeThesaurus) thesaurus).components);
            return this;
        }
        components.add(thesaurus);
        return this;
    }

    @Override
    public DateTimeFormatter forLocale(DateTimeFormatter dateTimeFormatter) {
        return components.stream().findFirst().map(th -> th.forLocale(dateTimeFormatter)).orElseThrow(IllegalStateException::new);
    }
}
