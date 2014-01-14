package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.orm.DataModel;
import com.google.common.base.Optional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class NlsKey {

    private final DataModel dataModel;

    // persistent fields
	private String componentName;
	private String key;
    private Layer layer;
	private String defaultMessage;
    // composite association
    private List<NlsEntry> entries = new ArrayList<>();

    @Inject
    NlsKey(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    NlsKey init(String componentName, Layer layer, String key) {
        this.componentName = componentName;
        this.layer = layer;
        this.key = key;
        return this;
    }

    void add(Locale locale , String translation) {
        entries.add(new NlsEntry(this, locale).translation(translation));
    }

    public void save() {
        dataModel.mapper(NlsKey.class).persist(this);
    }

    String getComponent() {
        return componentName;
    }

    public Layer getLayer() {
        return layer;
    }

    public String getKey() {
        return key;
    }

    public void setDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public Optional<String> translate(Locale locale) {
        for (NlsEntry entry : entries) {
            if (entry.getLocale().equals(locale)) {
                return Optional.of(entry.getTranslation());
            }
        }
        return null;
    }
}
