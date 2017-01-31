/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Checks;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

final class NlsKeyImpl implements NlsKey {

    private final DataModel dataModel;

    // persistent fields
    private String componentName;
    private String key;
    private Layer layer;
    private String defaultMessage;
    // composite association
    private List<NlsEntry> entries = new ArrayList<>();
    private boolean persistent = true;

    @Inject
    NlsKeyImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    NlsKeyImpl init(String componentName, Layer layer, String key) {
        this.componentName = requireNonNull(componentName);
        this.layer = requireNonNull(layer);
        this.key = requireNonNull(key);
        this.persistent = false;
        return this;
    }

    public NlsKeyImpl init(NlsKey key) {
        this.componentName = key.getComponent();
        this.layer = key.getLayer();
        this.key = key.getKey();
        this.defaultMessage = key.getDefaultMessage();
        this.persistent = false;
        return this;
    }

    void add(Locale locale, String translation) {
        NlsEntry existingEntry = this.bestScore(locale);
        if (existingEntry == null) {
            this.entries.add(new NlsEntry(this, locale).translation(translation));
        } else {
            if (existingEntry.getTranslation().equals(translation)) {
                return;
            } else {
                existingEntry.translation(translation);
            }
        }
        if (this.persistent) {
            this.save();
        }
    }

    public void clearTranslations() {
        entries.clear();
    }

    void save() {
        if (persistent) {
            dataModel.mapper(NlsKeyImpl.class).update(this);
        } else {
            dataModel.mapper(NlsKeyImpl.class).persist(this);
        }
        this.persistent = true;
    }

    @Override
    public String getComponent() {
        return componentName;
    }

    @Override
    public Layer getLayer() {
        return layer;
    }

    @Override
    public String getKey() {
        return key;
    }

    void setDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }

    Optional<String> translate(Locale requested) {
        NlsEntry max = this.bestScore(requested);
        return max == null ? fallBack(requested) : Optional.of(max.getTranslation());
    }

    private NlsEntry bestScore(Locale locale) {
        Scorer scorer = new Scorer(locale);
        NlsEntry bestCandidate = null;
        int maxScore = 0;
        for (NlsEntry candidate : entries) {
            int score = scorer.score(candidate.getLocale());
            if (score > maxScore) {
                bestCandidate = candidate;
                maxScore = score;
            }
        }
        return bestCandidate;
    }

    private Optional<String> fallBack(Locale requested) {
        return Locale.ENGLISH.equals(requested) ? Checks.is(getDefaultMessage()).emptyOrOnlyWhiteSpace()? Optional.empty() : Optional.of(getDefaultMessage()) : translate(Locale.ENGLISH);
    }

    private static class Scorer {

        private final String requestedLanguageTag;

        Scorer(Locale requested) {
            this.requestedLanguageTag = requested.toLanguageTag();
        }

        public int score(Locale candidate) {
            String candidateTag = candidate.toLanguageTag();
            if (requestedLanguageTag.startsWith(candidateTag)) {
                return candidateTag.length();
            }
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NlsKey)) {
            return false;
        }

        NlsKey nlsKey = (NlsKey) o;

        return componentName.equals(nlsKey.getComponent()) && layer == nlsKey.getLayer() && key.equals(nlsKey.getKey());

    }

    @Override
    public int hashCode() {
        return Objects.hash(componentName, layer, key);
    }

    @Override
    public String toString() {
        return "NlsKey{" +
                "componentName='" + componentName + '\'' +
                ", layer=" + layer +
                ", key='" + key + '\'' +
                '}';
    }
}
