/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
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

    private final List<IThesaurus> components = new ArrayList<>();
    private final ThreadPrincipalService threadPrincipalService;

    public CompositeThesaurus(ThreadPrincipalService threadPrincipalService, IThesaurus... thesauruses) {
        this.threadPrincipalService = threadPrincipalService;
        this.components.addAll(Arrays.asList(thesauruses));
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
    public NlsMessageFormat getFormat(TranslationKey key) {
        return components.stream()
                .filter(th -> th.hasKey(key.getKey()))
                .map(th -> th.getFormat(key))
                .findAny()
                .orElseGet(() -> new NlsTranslationFormatImpl(this, nlsStringFor(key)));
    }

    private NlsString nlsStringFor(MessageSeed seed) {
        String key = seed.getKey();
        String defaultFormat = seed.getDefaultFormat();
        return NlsString.from(this, seed.getModule(), key, defaultFormat);
    }

    private NlsString nlsStringFor(TranslationKey translationKey) {
        String key = translationKey.getKey();
        String defaultFormat = translationKey.getDefaultFormat();
        return NlsString.from(this, NlsService.COMPONENTNAME, key, defaultFormat);
    }

    @Override
    public Map<String, String> getTranslationsForCurrentLocale() {
        return components.stream()
                .map(Thesaurus::getTranslationsForCurrentLocale)
                .collect(HashMap::new, HashMap::putAll, HashMap::putAll);
    }

    @Override
    public String interpolate(String messageTemplate, Context context) {
        return components.stream()
                .map(th -> th.interpolate(messageTemplate, context))
                .filter(result -> !messageTemplate.contains(result))
                .findAny()
                .orElseGet(() ->
                        components.stream()
                                .findFirst()
                                .map(th -> th.interpolate(messageTemplate, context))
                                .orElseThrow(IllegalStateException::new));
    }

    @Override
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        return components.stream()
                .map(th -> th.interpolate(messageTemplate, context, locale))
                .filter(result -> !messageTemplate.contains(result))
                .findAny()
                .orElseGet(() ->
                        components.stream()
                                .findFirst()
                                .map(th -> th.interpolate(messageTemplate, context, locale))
                                .orElseThrow(IllegalStateException::new));
    }

    @Override
    public Locale getLocale() {
        return threadPrincipalService.getLocale();
    }

    @Override
    public void invalidate() {
        components.forEach(IThesaurus::invalidate);
    }

    @Override
    public Thesaurus join(Thesaurus thesaurus) {
        if (thesaurus instanceof CompositeThesaurus) {
            components.addAll(((CompositeThesaurus) thesaurus).components);
            return this;
        }
        components.add((IThesaurus) thesaurus);
        return this;
    }

    @Override
    public DateTimeFormatter forLocale(DateTimeFormatter dateTimeFormatter) {
        return components.stream().findFirst().map(th -> th.forLocale(dateTimeFormatter)).orElseThrow(IllegalStateException::new);
    }

    @Override
    public boolean hasKey(String key){
        return this.components.stream().anyMatch(th -> th.hasKey(key));
    }
}
