/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.NlsKey;

import com.google.common.base.MoreObjects;

import java.util.Objects;

/**
 * Models the combination of a component name and a Layer as a single component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-21 (15:03)
 */
final class ComponentAndLayer {
    private final String component;
    private final String layer;

    static ComponentAndLayer from(NlsKey nlsKey) {
        return new ComponentAndLayer(nlsKey.getComponent(), nlsKey.getLayer().name());
    }

    static ComponentAndLayer from(TranslationCsvEntry entry) {
        return new ComponentAndLayer(entry.component(), entry.layerAsString());
    }

    static ComponentAndLayer from(String component, String layer) {
        return new ComponentAndLayer(component, layer);
    }

    private ComponentAndLayer(String component, String layer) {
        this.component = component;
        this.layer = layer;
    }

    public String component() {
        return this.component;
    }

    public String layer() {
        return this.layer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ComponentAndLayer that = (ComponentAndLayer) o;
        return Objects.equals(component, that.component)
                && Objects.equals(layer, that.layer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(component, layer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("component", component)
                .add("layer", layer)
                .toString();
    }
}