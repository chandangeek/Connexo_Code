/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.properties.InvalidValueException;

import java.util.Arrays;
import java.util.List;

/**
 * Provides support to build a search for instances
 * of a {@link SearchDomain}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-27 (14:21)
 */
@ProviderType
public interface SearchBuilder<T> {

    public SearchDomain getDomain();

    /**
     * Starts the building process of a criterion
     * against the {@link SearchableProperty}
     * of the {@link SearchDomain} with the specified name.
     * Will throw an IllegalArgumentException if the SearchDomain
     * does not have a SearchableProperty with the specified name.
     *
     * @param propertyName The name of the SearchableProperty
     * @return The CriterionBuilder
     * @throws IllegalArgumentException
     */
    public default CriterionBuilder<T> where(String propertyName) {
        return where(
                    getDomain()
                        .getProperties()
                        .stream()
                        .filter(p -> p.hasName(propertyName))
                        .findAny()
                        .orElseThrow(() -> new IllegalArgumentException("SearchableProperty with name " + propertyName + " not found")));
    }

    /**
     * Starts the building process of a criterion
     * against the specified {@link SearchableProperty}.
     *
     * @param property The SearchableProperty
     * @return The CriterionBuilder
     */
    public CriterionBuilder<T> where(SearchableProperty property);

    /**
     * Adds another criterion against the {@link SearchableProperty}
     * of the {@link SearchDomain} with the specified name.
     * Will throw an IllegalArgumentException if the SearchDomain
     * does not have a SearchableProperty with the specified name.
     *
     * @param propertyName The name of the SearchableProperty
     * @return The CriterionBuilder
     */
    public default CriterionBuilder<T> and(String propertyName) {
        return where(propertyName);
    }

    /**
     * Adds another criterion against the specified {@link SearchableProperty}.
     *
     * @param property The SearchableProperty
     * @return The CriterionBuilder
     */
    public default CriterionBuilder<T> and(SearchableProperty property) {
        return where(property);
    }

    /**
     * Completes the building process and returns
     * a {@link Finder} to actually find the matching
     * instances of the {@link SearchDomain}.
     *
     * @return The Finder
     */
    public Finder<T> toFinder();

    /**
     * Gets the list of {@link SearchablePropertyCondition}s built by the builder
     * @return The list of SearchablePropertyConditions
     */
    List<SearchablePropertyCondition> getConditions();

    /**
     * Supports building criteria that must
     * hold for instances returned by the search
     * that is under construction.
     * All methods will return the same CriterionBuilder
     * to support method chaining.
     */
    public interface CriterionBuilder<T> {

        /**
         * Builds a criterion that checks that the target
         * {@link SearchableProperty} matches one of the specified values.
         * <p>
         * Will throw an {@link com.elster.jupiter.properties.InvalidValueException}
         * when one of the specified values is not compatible with the
         * property's {@link com.elster.jupiter.properties.PropertySpec specification}.
         * </p>
         *
         * @param values The List of value
         * @return The same SearchBuilder to support method chaining
         * @throws InvalidValueException Thrown on the first value that is not compatible
         *         with the property's specification
         */
        public default SearchBuilder<T> in(Object... values) throws InvalidValueException {
            return in(Arrays.asList(values));
        }

        /**
         * Builds a criterion that checks that the target
         * {@link SearchableProperty} matches one of the specified values.
         * <p>
         * Will throw an {@link com.elster.jupiter.properties.InvalidValueException}
         * when one of the specified values is not compatible with the
         * property's {@link com.elster.jupiter.properties.PropertySpec specification}.
         * </p>
         *
         * @param values The List of value
         * @return The same SearchBuilder to support method chaining
         * @throws InvalidValueException Thrown on the first value that is not compatible
         *         with the property's specification
         */
        public SearchBuilder<T> in(List<Object> values) throws InvalidValueException;

        /**
         * Builds a criterion that checks that the target
         * {@link SearchableProperty} does not match none of the specified values.
         * <p>
         * Will throw an {@link com.elster.jupiter.properties.InvalidValueException}
         * when one of the specified values is not compatible with the
         * property's {@link com.elster.jupiter.properties.PropertySpec specification}.
         * </p>
         *
         * @param values The List of value
         * @return The same SearchBuilder to support method chaining
         * @throws InvalidValueException Thrown on the first value that is not compatible
         *         with the property's specification
         */
        default SearchBuilder<T> notIn(Object... values) throws InvalidValueException {
            return notIn(Arrays.asList(values));
        }
        /**
         * Builds a criterion that checks that the target
         * {@link SearchableProperty} does not match none of the specified values.
         * <p>
         * Will throw an {@link com.elster.jupiter.properties.InvalidValueException}
         * when one of the specified values is not compatible with the
         * property's {@link com.elster.jupiter.properties.PropertySpec specification}.
         * </p>
         *
         * @param values The List of value
         * @return The same SearchBuilder to support method chaining
         * @throws InvalidValueException Thrown on the first value that is not compatible
         *         with the property's specification
         */
        SearchBuilder<T> notIn(List<Object> values) throws InvalidValueException;

        /**
         * Builds a criterion that checks that the target
         * {@link SearchableProperty} matches the specified value.
         * <p>
         * Will throw an {@link com.elster.jupiter.properties.InvalidValueException}
         * when the specified value is not compatible with the
         * property's {@link com.elster.jupiter.properties.PropertySpec specification}.
         * </p>
         *
         * @param value The value
         * @return The same SearchBuilder to support method chaining
         * @throws InvalidValueException Thrown on the first value that is not compatible
         *         with the property's specification
         */
        public SearchBuilder<T> isEqualTo(Object value) throws InvalidValueException;

        /**
         * Builds a criterion that checks that the target {@link SearchableProperty}
         * matches the specified value when character casing is ignored.
         * <p>
         * Will throw an {@link com.elster.jupiter.properties.InvalidValueException}
         * when the specified value is not compatible with the
         * property's {@link com.elster.jupiter.properties.PropertySpec specification}.
         * </p>
         *
         * @param value The value
         * @return The same SearchBuilder to support method chaining
         * @throws InvalidValueException Thrown on the first value that is not compatible
         *         with the property's specification
         */
        public SearchBuilder<T> isEqualToIgnoreCase(String value) throws InvalidValueException;

        /**
         * Builds a criterion that checks that the target
         * {@link SearchableProperty} does not match the specified value.
         * <p>
         * Will throw an {@link com.elster.jupiter.properties.InvalidValueException}
         * when the specified value is not compatible with the
         * property's {@link com.elster.jupiter.properties.PropertySpec specification}.
         * </p>
         *
         * @param value The value
         * @return The same SearchBuilder to support method chaining
         * @throws InvalidValueException Thrown on the first value that is not compatible
         *         with the property's specification
         */
        public SearchBuilder<T> isNotEqualTo(Object value) throws InvalidValueException;

        /**
         * Builds a criterion that checks that the target
         * {@link SearchableProperty} &lt; the specified value.
         * <p>
         * Will throw an {@link com.elster.jupiter.properties.InvalidValueException}
         * when the specified value is not compatible with the
         * property's {@link com.elster.jupiter.properties.PropertySpec specification}.
         * </p>
         *
         * @param value The value
         * @return The same SearchBuilder to support method chaining
         * @throws InvalidValueException Thrown on the first value that is not compatible
         *         with the property's specification
         */
        public SearchBuilder<T> isLessThan(Object value) throws InvalidValueException;

        /**
         * Builds a criterion that checks that the target
         * {@link SearchableProperty} &le; the specified value.
         * <p>
         * Will throw an {@link com.elster.jupiter.properties.InvalidValueException}
         * when the specified value is not compatible with the
         * property's {@link com.elster.jupiter.properties.PropertySpec specification}.
         * </p>
         *
         * @param value The value
         * @return The same SearchBuilder to support method chaining
         * @throws InvalidValueException Thrown on the first value that is not compatible
         *         with the property's specification
         */
        public SearchBuilder<T> isLessThanOrEqualTo(Object value) throws InvalidValueException;

        /**
         * Builds a criterion that checks that the target
         * {@link SearchableProperty} &gt; the specified value.
         * <p>
         * Will throw an {@link com.elster.jupiter.properties.InvalidValueException}
         * when the specified value is not compatible with the
         * property's {@link com.elster.jupiter.properties.PropertySpec specification}.
         * </p>
         *
         * @param value The value
         * @return The same SearchBuilder to support method chaining
         * @throws InvalidValueException Thrown on the first value that is not compatible
         *         with the property's specification
         */
        public SearchBuilder<T> isGreaterThan(Object value) throws InvalidValueException;

        /**
         * Builds a criterion that checks that the target
         * {@link SearchableProperty} &ge; the specified value.
         * <p>
         * Will throw an {@link com.elster.jupiter.properties.InvalidValueException}
         * when the specified value is not compatible with the
         * property's {@link com.elster.jupiter.properties.PropertySpec specification}.
         * </p>
         *
         * @param value The value
         * @return The same SearchBuilder to support method chaining
         * @throws InvalidValueException Thrown on the first value that is not compatible
         *         with the property's specification
         */
        public SearchBuilder<T> isGreaterThanOrEqualTo(Object value) throws InvalidValueException;

        /**
         * Builds a criterion that checks that the target {@link SearchableProperty}
         * matches the specified wildcard pattern.
         * <p>
         * Will throw an {@link com.elster.jupiter.properties.InvalidValueException}
         * when the specified value is not compatible with the
         * property's {@link com.elster.jupiter.properties.PropertySpec specification}.
         * </p>
         *
         * @param wildCardPattern The wild card pattern
         * @return The same SearchBuilder to support method chaining
         * @throws InvalidValueException Thrown on the first value that is not compatible
         *         with the property's specification
         */
        public SearchBuilder<T> like(String wildCardPattern) throws InvalidValueException;

        /**
         * Builds a criterion that checks that the target {@link SearchableProperty}
         * matches the specified wildcard pattern when character casing is ignored.
         * <p>
         * Will throw an {@link com.elster.jupiter.properties.InvalidValueException}
         * when the specified value is not compatible with the
         * property's {@link com.elster.jupiter.properties.PropertySpec specification}.
         * </p>
         *
         * @param wildCardPattern The wild card pattern
         * @return The same SearchBuilder to support method chaining
         * @throws InvalidValueException Thrown on the first value that is not compatible
         *         with the property's specification
         */
        public SearchBuilder<T> likeIgnoreCase(String wildCardPattern) throws InvalidValueException;

        /**
         * Builds a criterion that checks that the target {@link SearchableProperty}
         * matches the specified boolean value.
         * <p>
         * Will throw an {@link com.elster.jupiter.properties.InvalidValueException}
         * when the specified value is not compatible with the
         * property's {@link com.elster.jupiter.properties.PropertySpec specification}.
         * </p>
         *
         * @param value The boolean constant
         * @return The same SearchBuilder to support method chaining
         * @throws InvalidValueException Thrown on the first value that is not compatible
         *         with the property's specification
         */
        public SearchBuilder<T> is(Boolean value) throws InvalidValueException;

        /**
         * Builds a criterion that checks that the target {@link SearchableProperty}
         * is between two specified bounds
         * <p>
         * Will throw an {@link com.elster.jupiter.properties.InvalidValueException}
         * when the specified value is not compatible with the
         * property's {@link com.elster.jupiter.properties.PropertySpec specification}.
         * </p>
         *
         * @param min The lower bound
         * @param max The upper bound
         * @return The same SearchBuilder to support method chaining
         * @throws InvalidValueException Thrown on the first value that is not compatible
         *         with the property's specification
         */
        public SearchBuilder<T> isBetween(Object min, Object max) throws InvalidValueException;
    }
}