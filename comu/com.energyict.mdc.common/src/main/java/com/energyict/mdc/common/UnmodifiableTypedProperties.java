/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.Set;

/**
 * A subclass of TypedProperties which is unmodifiable.
 *
 * @author Joost Bruneel (jbr), Rudi Vankeirsbilck (rudi)
 * @since 2012-05-10 (14:11)
 */
public class UnmodifiableTypedProperties extends TypedProperties {

    private final TypedProperties delegate;

    public UnmodifiableTypedProperties(TypedProperties delegate) {
        this.delegate = delegate;
        super.setAllProperties(delegate);
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAllProperties(TypedProperties otherTypedProperties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getProperty(String propertyName) {
        return delegate.getProperty(propertyName);
    }

    @Override
    public <T> T getTypedProperty(String propertyName) {
        return delegate.getTypedProperty(propertyName);
    }

    @Override
    public Object getProperty(String propertyName, Object defaultValue) {
        return delegate.getProperty(propertyName, defaultValue);
    }

    @Override
    public <T> T getTypedProperty(String propertyName, T defaultValue) {
        return delegate.getTypedProperty(propertyName, defaultValue);
    }

    @Override
    public void removeProperty(String propertyName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypedProperties clone() {
        return delegate.clone();
    }

    @Override
    public int localSize() {
        return delegate.localSize();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public String getStringProperty(String propertyName) {
        return delegate.getStringProperty(propertyName);
    }

    @Override
    public BigDecimal getIntegerProperty(String propertyName) {
        return delegate.getIntegerProperty(propertyName);
    }

    @Override
    public BigDecimal getIntegerProperty(String key, BigDecimal defaultValue) {
        return delegate.getIntegerProperty(key, defaultValue);
    }

    @Override
    public Set<String> localPropertyNames() {
        return delegate.localPropertyNames();
    }

    @Override
    public Set<String> propertyNames() {
        return delegate.propertyNames();
    }

    @Override
    public boolean hasLocalValueFor(String propertyName) {
        return delegate.hasLocalValueFor(propertyName);
    }

    @Override
    public boolean isLocalValueFor(Object value, String propertyName) {
        return delegate.isLocalValueFor(value, propertyName);
    }

    @Override
    public boolean hasInheritedValueFor(String propertyName) {
        return delegate.hasInheritedValueFor(propertyName);
    }

    @Override
    public boolean isInheritedValueFor(Object value, String propertyName) {
        return delegate.isInheritedValueFor(value, propertyName);
    }

    @Override
    public boolean hasValueFor(String propertyName) {
        return delegate.hasValueFor(propertyName);
    }

    @Override
    public boolean isValueFor(Object value, String propertyName) {
        return delegate.isValueFor(value, propertyName);
    }

    @Override
    public Properties toStringProperties() {
        return delegate.toStringProperties();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return delegate.equals(other);
    }

    @Override
    public TypedProperties getInheritedProperties() {
        return delegate.getInheritedProperties();
    }

    @Override
    public TypedProperties getUnmodifiableView() {
        return this;
    }

    @Override
    public Object getLocalValue(String propertyName) {
        return this.delegate.getLocalValue(propertyName);
    }

    @Override
    public Object getInheritedValue(String propertyName) {
        return this.delegate.getInheritedValue(propertyName);
    }

}