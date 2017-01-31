/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.nls.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.Thesaurus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Self made thesaurus cache implementation. caches thesaurus objects
 */
public class ThesaurusCache {
    private final Map<ThesaurusKey, Thesaurus> map = new HashMap<>();

    Optional<Thesaurus> get(String componentName, Layer layer) {
        ThesaurusKey key = new ThesaurusKey(layer, componentName);
        return map.containsKey(key)?Optional.of(map.get(key)):Optional.empty();
    }

    // to be called by REST PURGE method
    public Thesaurus purge(String componentName, Layer layer) {
        ThesaurusKey key = new ThesaurusKey(layer, componentName);
        return map.remove(key);
    }

    // to be called by REST PURGE method
    public void purge() {
        map.clear();
    }

    Thesaurus put(String componentName, Layer layer, Thesaurus thesaurus) {
        ThesaurusKey key = new ThesaurusKey(layer, componentName);
        return map.put(key, thesaurus);
    }

    private class ThesaurusKey {
        private final Layer layer;
        private final String component;

        ThesaurusKey(Layer layer, String component) {
            this.layer = layer;
            this.component = component;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ThesaurusKey that = (ThesaurusKey) o;

            if (layer != that.layer) {
                return false;
            }
            return component != null ? component.equals(that.component) : that.component == null;

        }

        @Override
        public int hashCode() {
            int result = layer != null ? layer.hashCode() : 0;
            result = 31 * result + (component != null ? component.hashCode() : 0);
            return result;
        }
    }
}
