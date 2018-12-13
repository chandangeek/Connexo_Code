/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class SimpleNlsKey implements NlsKey {

    private final String componentName;
    private final Layer layer;
    private final String key;
    private String defaultMessage;

    private SimpleNlsKey(String componentName, Layer layer, String key) {
        this.componentName = requireNonNull(componentName);
        this.layer = requireNonNull(layer);
        this.key = requireNonNull(key);
    }

    public static SimpleNlsKey key(String componentName, Layer layer, String key) {
        return new SimpleNlsKey(componentName, layer, key);
    }

    public SimpleNlsKey defaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
        return this;
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

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NlsKey)) {
            return false;
        }

        NlsKey that = (NlsKey) o;

        return componentName.equals(that.getComponent()) && layer == that.getLayer() && key.equals(that.getKey());

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
