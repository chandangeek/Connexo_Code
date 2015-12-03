package com.energyict.protocolimplv2.ace4000;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link ACE4000DeviceProtocolDialectPropertyPersistenceSupport} component.
 */
public class ACE4000DeviceProtocolDialectPropertyPersistenceSupportTest {

    public static final int MAX_TABLE_NAME_LENGTH = 26;
    public static final int MAX_FOREIGN_KEY_NAME_LENGTH = 30;

    private ACE4000DeviceProtocolDialectPropertyPersistenceSupport getInstance() {
        return new ACE4000DeviceProtocolDialectPropertyPersistenceSupport();
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

}