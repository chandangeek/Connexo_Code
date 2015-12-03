package com.energyict.protocolimplv2.security;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link AnsiC12PersistenceSupport} component.
 */
public class AnsiC12PersistenceSupportTest {

    public static final int MAX_TABLE_NAME_LENGTH = 26;
    public static final int MAX_FOREIGN_KEY_NAME_LENGTH = 30;

    private AnsiC12PersistenceSupport getInstance() {
        return new AnsiC12PersistenceSupport();
    }

    @Test
    public void componentNameIsNotNull() {
        assertThat(getInstance().componentName()).isNotNull();
    }

    @Test
    public void componentNameIsNotEmpty() {
        assertThat(getInstance().componentName()).isNotEmpty();
    }

    @Test
    public void componentNameSize() {
        assertThat(getInstance().componentName().length()).isLessThanOrEqualTo(3);
    }

    @Test
    public void tableNameIsNotNull() {
        assertThat(getInstance().tableName()).isNotNull();
    }

    @Test
    public void tableNameIsNotEmpty() {
        assertThat(getInstance().tableName()).isNotEmpty();
    }

    @Test
    public void tableNameSize() {
        assertThat(getInstance().tableName().length()).isLessThanOrEqualTo(MAX_TABLE_NAME_LENGTH);
    }

    @Test
    public void domainForeignKeyNameIsNotNull() {
        assertThat(getInstance().domainForeignKeyName()).isNotNull();
    }

    @Test
    public void domainForeignKeyNameIsNotEmpty() {
        assertThat(getInstance().domainForeignKeyName()).isNotEmpty();
    }

    @Test
    public void domainForeignKeyNameSize() {
        assertThat(getInstance().domainForeignKeyName().length()).isLessThanOrEqualTo(MAX_FOREIGN_KEY_NAME_LENGTH);
    }

    @Test
    public void propertySpecProviderForeignKeyNameIsNotNull() {
        assertThat(getInstance().propertySpecProviderForeignKeyName()).isNotNull();
    }

    @Test
    public void propertySpecProviderForeignKeyNameIsNotEmpty() {
        assertThat(getInstance().propertySpecProviderForeignKeyName()).isNotEmpty();
    }

    @Test
    public void propertySpecProviderForeignKeyNameSize() {
        assertThat(getInstance().propertySpecProviderForeignKeyName().length()).isLessThanOrEqualTo(MAX_FOREIGN_KEY_NAME_LENGTH);
    }

}