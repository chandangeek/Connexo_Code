/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.NlsKey;

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

    private ComponentAndLayer(String component, String layer) {
        this.component = component;
        this.layer = layer;
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
}