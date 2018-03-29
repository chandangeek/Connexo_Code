/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class FileImportServiceReferenceUsages {
    private final Map<Class<?>, Map<Class<? extends ValueFactory>, Map<String, PropertySpec>>> referencedClassesMap = new ConcurrentHashMap<>();

    void registerFileImportFactory(FileImporterFactory fileImporterFactory) {
        processReferencePropertySpecs(fileImporterFactory, this::registerPropertySpec);
    }

    void unregisterFileImportFactory(FileImporterFactory fileImporterFactory) {
        processReferencePropertySpecs(fileImporterFactory, this::unregisterPropertySpec);
    }

    private void processReferencePropertySpecs(FileImporterFactory fileImporterFactory, Consumer<PropertySpec> action) {
        fileImporterFactory.getPropertySpecs().stream()
                .filter(PropertySpec::isReference)
                .forEach(action);
    }

    private void registerPropertySpec(PropertySpec propertySpec) {
        referencedClassesMap
                .computeIfAbsent(propertySpec.getValueFactory().getValueType(), key -> new ConcurrentHashMap<>())
                .computeIfAbsent(propertySpec.getValueFactory().getClass(), key -> new ConcurrentHashMap<>())
                .put(propertySpec.getName(), propertySpec);
    }

    private void unregisterPropertySpec(PropertySpec propertySpec) {
        referencedClassesMap.computeIfPresent(propertySpec.getValueFactory().getValueType(), (valueType, valueFactoryMap) -> {
            valueFactoryMap.computeIfPresent(propertySpec.getValueFactory().getClass(), (valueFactory, propertySpecMap) -> {
                propertySpecMap.remove(propertySpec.getName());
                return propertySpecMap.isEmpty() ? null : propertySpecMap;
            });
            return valueFactoryMap.isEmpty() ? null : valueFactoryMap;
        });
    }

    Condition getUsedByPropertiesCondition(Object object) {
        return getUsingPropertySpecs(object).stream()
                .map(map -> {
                    Condition propertyValueCondition = map.values().stream()
                            .findFirst()
                            .map(PropertySpec::getValueFactory)
                            .map(vf -> vf.toStringValue(object))
                            .map(stringValue -> Where.where("stringValue").isEqualTo(stringValue))
                            .orElse(Condition.FALSE);
                    Condition propertyNameCondition = Where.where("name").in(new ArrayList<>(map.keySet()));
                    return propertyValueCondition.and(propertyNameCondition);
                })
                .reduce(Condition::or)
                .orElse(Condition.FALSE);
    }

    /**
     * @param object Object whose usage by property specs is checked.
     * @return A collection of property spec maps (property spec name to property spec instance), each property spec describing properties that may use the given object.
     * The property specs are grouped in maps by criterion of the same {@link ValueFactory) used. No empty map can be present in this collection.
     */
    private Collection<Map<String, PropertySpec>> getUsingPropertySpecs(Object object) {
        Class<?> theClass = object.getClass();
        return referencedClassesMap.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(theClass))
                .map(Map.Entry::getValue)
                .reduce(FileImportServiceReferenceUsages::merge)
                .map(Map::values)
                .orElseGet(Collections::emptyList);
    }

    private static <OK, IK, V> Map<OK, Map<IK, V>> merge(Map<OK, Map<IK, V>> map1, Map<OK, Map<IK, V>> map2) {
        return Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (m1, m2) -> {
                                Map<IK, V> newMap = new HashMap<>();
                                newMap.putAll(m1);
                                newMap.putAll(m2);
                                return newMap;
                        }));
    }
}
