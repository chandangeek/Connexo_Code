package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.orm.DataModel;
import com.google.common.base.Optional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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

    @Inject
    NlsKeyImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    NlsKeyImpl init(String componentName, Layer layer, String key) {
        this.componentName = requireNonNull(componentName);
        this.layer = requireNonNull(layer);
        this.key = requireNonNull(key);
        return this;
    }

    public NlsKeyImpl init(NlsKey key) {
        this.componentName = key.getComponent();
        this.layer = key.getLayer();
        this.key = key.getKey();
        this.defaultMessage = key.getDefaultMessage();
        return this;
    }

    void add(Locale locale , String translation) {
        entries.add(new NlsEntry(this, locale).translation(translation));
    }

    public void clearTranslations() {
        entries.clear();
    }

    void save() {
        dataModel.mapper(NlsKeyImpl.class).persist(this);
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
        Scorer scorer = new Scorer(requested);
        NlsEntry max = null;
        int maxScore = 0;
        for (NlsEntry candidate : entries) {
            int score = scorer.score(candidate.getLocale());
            if (score > maxScore) {
                max = candidate;
                maxScore = score;
            }
        }
        return maxScore == 0 ? Optional.<String>absent() : Optional.of(max.getTranslation());
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

}
