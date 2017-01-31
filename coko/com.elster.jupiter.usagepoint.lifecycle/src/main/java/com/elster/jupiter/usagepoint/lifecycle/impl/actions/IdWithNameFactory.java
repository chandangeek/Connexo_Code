/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.properties.HasIdAndName;

import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;


public class IdWithNameFactory<T> extends AbstractValueFactory<HasIdAndName> {
    static class IdWithNameImpl extends HasIdAndName {
        private String id;
        private String name;

        public IdWithNameImpl(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    private final Function<T, String> keyExtractor;
    private final Function<T, String> nameExtractor;
    private final Function<String, T> objectFetcher;

    /**
     * Creates a factory which can convert a {@code T} instance to a {@link HasIdAndName} instance.
     *
     * @param keyExtractor extract a {@link String} key from {@code T} instance (will be called only if {@code T} instance is not null).
     * @param nameExtractor extract a display name from {@code T} instance (will be called only if {@code T} instance is not null).
     * @param objectFetcher converts a {@link String} key to {@code T} instance (key is always not null).
     */
    public IdWithNameFactory(Function<T, String> keyExtractor, Function<T, String> nameExtractor, Function<String, T> objectFetcher) {
        this.keyExtractor = nullSafe(Objects.requireNonNull(keyExtractor));
        this.nameExtractor = nullSafe(Objects.requireNonNull(nameExtractor));
        this.objectFetcher = nullSafe(Objects.requireNonNull(objectFetcher));
    }

    private static <T, R> Function<T, R> nullSafe(Function<T, R> original) {
        return obj -> obj == null ? null : original.apply(obj);
    }

    @Override
    public HasIdAndName fromStringValue(String stringValue) {
        T obj = this.objectFetcher.apply(stringValue);
        return new IdWithNameImpl(this.keyExtractor.apply(obj), this.nameExtractor.apply(obj));
    }

    @Override
    public String toStringValue(HasIdAndName object) {
        return object != null ? object.getId().toString() : null;
    }

    @Override
    public Class<HasIdAndName> getValueType() {
        return HasIdAndName.class;
    }

    @Override
    public int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public HasIdAndName valueFromDatabase(Object object) {
        return fromStringValue(object != null ? object.toString() : null);
    }

    @Override
    public Object valueToDatabase(HasIdAndName object) {
        return object == null || object.getId() == null ? null : object.getId();
    }

    /**
     * Converts a {@code T} instance into {@link HasIdAndName} object via {@code keyExtractor} and {@code nameExtractor}, which were specified in constructor.
     *
     * @param obj a {@code T} instance to convert
     * @return a non-null HasIdAndName instance (which can have null values in id, name or both)
     */
    public HasIdAndName wrap(T obj) {
        return new IdWithNameImpl(this.keyExtractor.apply(obj), this.nameExtractor.apply(obj));
    }

    /**
     * Converts a {@link HasIdAndName} instance into {@code T} object via {@code objectFetcher}, which was specified in constructor.
     *
     * @param obj a {@code HasIdAndName} instance to convert
     * @return a {@code T} instance (can be null)
     */
    public T unwrap(HasIdAndName obj) {
        if (obj == null || obj.getId() == null) {
            return null;
        }
        if (!(obj instanceof IdWithNameImpl)) {
            Logger.getLogger(IdWithNameFactory.class.getSimpleName()).warning("It is possible that you are trying to unwrap object instance of wrong type.");
        }
        return this.objectFetcher.apply(obj.getId().toString());
    }
}
