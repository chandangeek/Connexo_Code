/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.DefaultBeanService;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ReferenceValueFactory} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReferenceValueFactoryTest {

    @Mock
    private OrmService ormService;
    private BeanService beanService = new DefaultBeanService();
    @Mock
    private DataModel dataModel;
    @Mock
    private Table table;
    @Mock
    private Column primaryKeyColumn;
    @Mock
    private DataMapper dataMapper;

    @Before
    public void initializeMocks() {
        when(this.ormService.getDataModels()).thenReturn(Collections.singletonList(this.dataModel));
        doReturn(Collections.singletonList(this.table)).when(this.dataModel).getTables();
        when(this.table.getPrimaryKeyColumns()).thenReturn(Collections.singletonList(this.primaryKeyColumn));
    }

    @Test
    public void isReference() {
        assertThat(this.getTestInstance().isReference()).isTrue();
    }

    @Test
    public void testDomainClass() {
        assertThat(this.getTestInstance(DomainWithRawLongPrimaryKey.class).getValueType()).isEqualTo(DomainWithRawLongPrimaryKey.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noDataModelsThatSupportsApiClass() {
        when(this.ormService.getDataModels()).thenReturn(Collections.emptyList());

        // Business method
        this.getTestInstanceWithValidation(DomainWithRawLongPrimaryKey.class);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void noDataModelThatSupportsApiClass() {
        doThrow(IllegalArgumentException.class).when(this.dataModel).mapper(any(Class.class));

        // Business method
        this.getTestInstanceWithValidation(DomainWithRawLongPrimaryKey.class);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void noSupportForMultiValuePrimaryKey() {
        when(this.table.maps(DomainWithMultiValuePrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithMultiValuePrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.table.getPrimaryKeyColumns()).thenReturn(Arrays.asList(mock(Column.class), mock(Column.class)));

        // Business method
        this.getTestInstanceWithValidation(DomainWithMultiValuePrimaryKey.class);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void unsupportedPrimaryKeyType() {
        when(this.table.maps(DomainWithUnsupportedPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithUnsupportedPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");

        // Business method
        this.getTestInstanceWithValidation(DomainWithUnsupportedPrimaryKey.class);

        // Asserts: see expected exception rule
    }

    @Test
    public void rawLongPrimaryKeyTypeIsSupported() {
        when(this.table.maps(DomainWithRawLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithRawLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");

        // Business method
        this.getTestInstanceWithValidation(DomainWithRawLongPrimaryKey.class);

        // Asserts: see expected exception rule
    }

    @Test
    public void rawLongPrimaryKeyTypeIsSupportedOnInterface() {
        when(this.table.maps(HasRawLongId.class)).thenReturn(true);
        when(this.dataModel.mapper(HasRawLongId.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");

        // Business method
        this.getTestInstanceWithValidation(HasRawLongId.class);

        // Asserts: see expected exception rule
    }

    @Test
    public void longPrimaryKeyTypeIsSupported() {
        when(this.table.maps(DomainWithLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");

        // Business method
        this.getTestInstanceWithValidation(DomainWithLongPrimaryKey.class);

        // Asserts: see expected exception rule
    }

    @Test
    public void longPrimaryKeyTypeIsSupportedOnInterface() {
        when(this.table.maps(HasLongId.class)).thenReturn(true);
        when(this.dataModel.mapper(HasLongId.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");

        // Business method
        this.getTestInstanceWithValidation(HasLongId.class);

        // Asserts: see expected exception rule
    }

    @Test
    public void intPrimaryKeyTypeIsSupported() {
        when(this.table.maps(DomainWithIntPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithIntPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");

        // Business method
        this.getTestInstanceWithValidation(DomainWithIntPrimaryKey.class);

        // Asserts: see expected exception rule
    }

    @Test
    public void intPrimaryKeyTypeIsSupportedOnInterface() {
        when(this.table.maps(HasRawIntId.class)).thenReturn(true);
        when(this.dataModel.mapper(HasRawIntId.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");

        // Business method
        this.getTestInstanceWithValidation(HasRawIntId.class);

        // Asserts: see expected exception rule
    }

    @Test
    public void integerPrimaryKeyTypeIsSupported() {
        when(this.table.maps(DomainWithIntegerPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithIntegerPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");

        // Business method
        this.getTestInstanceWithValidation(DomainWithIntegerPrimaryKey.class);

        // Asserts: see expected exception rule
    }

    @Test
    public void integerPrimaryKeyTypeIsSupportedOnIntegererface() {
        when(this.table.maps(HasIntegerId.class)).thenReturn(true);
        when(this.dataModel.mapper(HasIntegerId.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");

        // Business method
        this.getTestInstanceWithValidation(HasIntegerId.class);

        // Asserts: see expected exception rule
    }

    @Test
    public void stringPrimaryKeyTypeIsSupported() {
        when(this.table.maps(DomainWithStringPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithStringPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");

        // Business method
        this.getTestInstanceWithValidation(DomainWithStringPrimaryKey.class);

        // Asserts: see expected exception rule
    }

    @Test
    public void stringPrimaryKeyTypeIsSupportedOnInterface() {
        when(this.table.maps(HasStringId.class)).thenReturn(true);
        when(this.dataModel.mapper(HasStringId.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");

        // Business method
        this.getTestInstanceWithValidation(HasStringId.class);

        // Asserts: see expected exception rule
    }

    @Test
    public void fromStringValueForLongPrimaryKeyThatExists() {
        when(this.table.maps(DomainWithRawLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithRawLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithRawLongPrimaryKey> testInstance = this.getTestInstance(DomainWithRawLongPrimaryKey.class);
        DomainWithRawLongPrimaryKey persistentValue = new DomainWithRawLongPrimaryKey();
        persistentValue.id = 3L;
        when(this.dataMapper.getOptional(3L)).thenReturn(Optional.of(persistentValue));

        // Business method
        DomainWithRawLongPrimaryKey value = testInstance.fromStringValue("3");

        // Asserts
        assertThat(value).isEqualTo(persistentValue);
    }

    @Test
    public void fromStringValueForLongPrimaryKeyThatDoesNotExist() {
        when(this.table.maps(DomainWithRawLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithRawLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithRawLongPrimaryKey> testInstance = this.getTestInstance(DomainWithRawLongPrimaryKey.class);
        when(this.dataMapper.getOptional(anyLong())).thenReturn(Optional.empty());

        // Business method
        DomainWithRawLongPrimaryKey value = testInstance.fromStringValue("3");

        // Asserts
        assertThat(value).isNull();
    }

    @Test
    public void fromStringValueForIntegerPrimaryKeyThatExists() {
        when(this.table.maps(DomainWithIntPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithIntPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithIntPrimaryKey> testInstance = this.getTestInstance(DomainWithIntPrimaryKey.class);
        DomainWithIntPrimaryKey persistentValue = new DomainWithIntPrimaryKey();
        persistentValue.id = 3;
        when(this.dataMapper.getOptional(3)).thenReturn(Optional.of(persistentValue));

        // Business method
        DomainWithIntPrimaryKey value = testInstance.fromStringValue("3");

        // Asserts
        assertThat(value).isEqualTo(persistentValue);
    }

    @Test
    public void fromStringValueForIntegerPrimaryKeyThatDoesNotExist() {
        when(this.table.maps(DomainWithIntPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithIntPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithIntPrimaryKey> testInstance = this.getTestInstance(DomainWithIntPrimaryKey.class);
        when(this.dataMapper.getOptional(anyInt())).thenReturn(Optional.empty());

        // Business method
        DomainWithIntPrimaryKey value = testInstance.fromStringValue("3");

        // Asserts
        assertThat(value).isNull();
    }

    @Test
    public void fromStringValueForStringPrimaryKeyThatExists() {
        when(this.table.maps(DomainWithStringPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithStringPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithStringPrimaryKey> testInstance = this.getTestInstance(DomainWithStringPrimaryKey.class);
        DomainWithStringPrimaryKey persistentValue = new DomainWithStringPrimaryKey();
        persistentValue.id = "3";
        when(this.dataMapper.getOptional("3")).thenReturn(Optional.of(persistentValue));

        // Business method
        DomainWithStringPrimaryKey value = testInstance.fromStringValue("3");

        // Asserts
        assertThat(value).isEqualTo(persistentValue);
    }

    @Test
    public void fromStringValueForStringPrimaryKeyThatDoesNotExist() {
        when(this.table.maps(DomainWithStringPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithStringPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithStringPrimaryKey> testInstance = this.getTestInstance(DomainWithStringPrimaryKey.class);
        when(this.dataMapper.getOptional(anyString())).thenReturn(Optional.empty());

        // Business method
        DomainWithStringPrimaryKey value = testInstance.fromStringValue("3");

        // Asserts
        assertThat(value).isNull();
    }

    @Test
    public void toStringValueForLongPrimaryKey() {
        DomainWithRawLongPrimaryKey persistentValue = new DomainWithRawLongPrimaryKey();
        persistentValue.id = 3L;
        when(this.table.maps(DomainWithRawLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithRawLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        when(this.primaryKeyColumn.getDatabaseValue(persistentValue)).thenReturn(persistentValue.id);
        ReferenceValueFactory<DomainWithRawLongPrimaryKey> testInstance = this.getTestInstance(DomainWithRawLongPrimaryKey.class);

        // Business method
        String value = testInstance.toStringValue(persistentValue);

        // Asserts
        assertThat(value).isEqualTo("3");
    }

    @Test
    public void toStringValueForIntegerPrimaryKey() {
        DomainWithIntPrimaryKey persistentValue = new DomainWithIntPrimaryKey();
        persistentValue.id = 3;
        when(this.table.maps(DomainWithIntPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithIntPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        when(this.primaryKeyColumn.getDatabaseValue(persistentValue)).thenReturn(persistentValue.id);
        ReferenceValueFactory<DomainWithIntPrimaryKey> testInstance = this.getTestInstance(DomainWithIntPrimaryKey.class);

        // Business method
        String value = testInstance.toStringValue(persistentValue);

        // Asserts
        assertThat(value).isEqualTo("3");
    }

    @Test
    public void toStringValueForStringPrimaryKey() {
        DomainWithStringPrimaryKey persistentValue = new DomainWithStringPrimaryKey();
        persistentValue.id = "3";
        when(this.table.maps(DomainWithStringPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithStringPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        when(this.primaryKeyColumn.getDatabaseValue(persistentValue)).thenReturn(persistentValue.id);
        ReferenceValueFactory<DomainWithStringPrimaryKey> testInstance = this.getTestInstance(DomainWithStringPrimaryKey.class);

        // Business method
        String value = testInstance.toStringValue(persistentValue);

        // Asserts
        assertThat(value).isEqualTo("3");
    }

    @Test
    public void valueFromDatabaseForLongPrimaryKeyThatExists() {
        when(this.table.maps(DomainWithRawLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithRawLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithRawLongPrimaryKey> testInstance = this.getTestInstance(DomainWithRawLongPrimaryKey.class);
        DomainWithRawLongPrimaryKey persistentValue = new DomainWithRawLongPrimaryKey();
        persistentValue.id = 3L;
        when(this.dataMapper.getOptional(3L)).thenReturn(Optional.of(persistentValue));

        // Business method
        DomainWithRawLongPrimaryKey value = testInstance.valueFromDatabase(3L);

        // Asserts
        assertThat(value).isEqualTo(persistentValue);
    }

    @Test
    public void valueFromDatabaseForLongPrimaryKeyThatDoesNotExist() {
        when(this.table.maps(DomainWithRawLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithRawLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithRawLongPrimaryKey> testInstance = this.getTestInstance(DomainWithRawLongPrimaryKey.class);
        when(this.dataMapper.getOptional(anyLong())).thenReturn(Optional.empty());

        // Business method
        DomainWithRawLongPrimaryKey value = testInstance.valueFromDatabase(3L);

        // Asserts
        assertThat(value).isNull();
    }

    @Test
    public void valueFromDatabaseForIntegerPrimaryKeyThatExists() {
        when(this.table.maps(DomainWithIntPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithIntPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithIntPrimaryKey> testInstance = this.getTestInstance(DomainWithIntPrimaryKey.class);
        DomainWithIntPrimaryKey persistentValue = new DomainWithIntPrimaryKey();
        persistentValue.id = 3;
        when(this.dataMapper.getOptional(3)).thenReturn(Optional.of(persistentValue));

        // Business method
        DomainWithIntPrimaryKey value = testInstance.valueFromDatabase(3);

        // Asserts
        assertThat(value).isEqualTo(persistentValue);
    }

    @Test
    public void valueFromDatabaseForIntegerPrimaryKeyThatDoesNotExist() {
        when(this.table.maps(DomainWithIntPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithIntPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithIntPrimaryKey> testInstance = this.getTestInstance(DomainWithIntPrimaryKey.class);
        when(this.dataMapper.getOptional(anyInt())).thenReturn(Optional.empty());

        // Business method
        DomainWithIntPrimaryKey value = testInstance.valueFromDatabase(3);

        // Asserts
        assertThat(value).isNull();
    }

    @Test
    public void valueFromDatabaseForStringPrimaryKeyThatExists() {
        when(this.table.maps(DomainWithStringPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithStringPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithStringPrimaryKey> testInstance = this.getTestInstance(DomainWithStringPrimaryKey.class);
        DomainWithStringPrimaryKey persistentValue = new DomainWithStringPrimaryKey();
        persistentValue.id = "3";
        when(this.dataMapper.getOptional("3")).thenReturn(Optional.of(persistentValue));

        // Business method
        DomainWithStringPrimaryKey value = testInstance.valueFromDatabase("3");

        // Asserts
        assertThat(value).isEqualTo(persistentValue);
    }

    @Test
    public void valueFromDatabaseForStringPrimaryKeyThatDoesNotExist() {
        when(this.table.maps(DomainWithStringPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithStringPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithStringPrimaryKey> testInstance = this.getTestInstance(DomainWithStringPrimaryKey.class);
        when(this.dataMapper.getOptional(anyString())).thenReturn(Optional.empty());

        // Business method
        DomainWithStringPrimaryKey value = testInstance.valueFromDatabase("3");

        // Asserts
        assertThat(value).isNull();
    }

    @Test
    public void valueToDatabaseForLongPrimaryKey() {
        DomainWithRawLongPrimaryKey persistentValue = new DomainWithRawLongPrimaryKey();
        persistentValue.id = 3L;
        when(this.table.maps(DomainWithRawLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithRawLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        when(this.primaryKeyColumn.getDatabaseValue(persistentValue)).thenReturn(persistentValue.id);
        ReferenceValueFactory<DomainWithRawLongPrimaryKey> testInstance = this.getTestInstance(DomainWithRawLongPrimaryKey.class);

        // Business method
        Object value = testInstance.valueToDatabase(persistentValue);

        // Asserts
        assertThat(value).isEqualTo(3L);
    }

    @Test
    public void valueToDatabaseForIntegerPrimaryKey() {
        DomainWithIntPrimaryKey persistentValue = new DomainWithIntPrimaryKey();
        persistentValue.id = 3;
        when(this.table.maps(DomainWithIntPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithIntPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        when(this.primaryKeyColumn.getDatabaseValue(persistentValue)).thenReturn(persistentValue.id);
        ReferenceValueFactory<DomainWithIntPrimaryKey> testInstance = this.getTestInstance(DomainWithIntPrimaryKey.class);

        // Business method
        Object value = testInstance.valueToDatabase(persistentValue);

        // Asserts
        assertThat(value).isEqualTo(3);
    }

    @Test
    public void valueToDatabaseForStringPrimaryKey() {
        DomainWithStringPrimaryKey persistentValue = new DomainWithStringPrimaryKey();
        persistentValue.id = "3";
        when(this.table.maps(DomainWithStringPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithStringPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        when(this.primaryKeyColumn.getDatabaseValue(persistentValue)).thenReturn(persistentValue.id);
        ReferenceValueFactory<DomainWithStringPrimaryKey> testInstance = this.getTestInstance(DomainWithStringPrimaryKey.class);

        // Business method
        Object value = testInstance.valueToDatabase(persistentValue);

        // Asserts
        assertThat(value).isEqualTo("3");
    }

    @Test
    public void isValidForNonPersistentEntityWithLongPrimaryKey() {
        when(this.table.maps(DomainWithRawLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithRawLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithRawLongPrimaryKey> testInstance = this.getTestInstance(DomainWithRawLongPrimaryKey.class);
        DomainWithRawLongPrimaryKey value = new DomainWithRawLongPrimaryKey();

        // Business method
        boolean isValid = testInstance.isValid(value);

        // Asserts
        assertThat(isValid).isFalse();
    }

    @Test
    public void isValidForPersistentEntityWithLongPrimaryKey() {
        DomainWithRawLongPrimaryKey value = new DomainWithRawLongPrimaryKey();
        value.id = 3L;
        when(this.table.maps(DomainWithRawLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithRawLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        when(this.primaryKeyColumn.getDatabaseValue(value)).thenReturn(value.id);
        ReferenceValueFactory<DomainWithRawLongPrimaryKey> testInstance = this.getTestInstance(DomainWithRawLongPrimaryKey.class);

        // Business method
        boolean isValid = testInstance.isValid(value);

        // Asserts
        assertThat(isValid).isTrue();
    }

    @Test
    public void isValidForNonPersistentEntityWithIntegerPrimaryKey() {
        when(this.table.maps(DomainWithIntPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithIntPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithIntPrimaryKey> testInstance = this.getTestInstance(DomainWithIntPrimaryKey.class);
        DomainWithIntPrimaryKey value = new DomainWithIntPrimaryKey();

        // Business method
        boolean isValid = testInstance.isValid(value);

        // Asserts
        assertThat(isValid).isFalse();
    }

    @Test
    public void isValidForPersistentEntityWithIntegerPrimaryKey() {
        DomainWithIntPrimaryKey value = new DomainWithIntPrimaryKey();
        value.id = 3;
        when(this.table.maps(DomainWithIntPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithIntPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        when(this.primaryKeyColumn.getDatabaseValue(value)).thenReturn(value.id);
        ReferenceValueFactory<DomainWithIntPrimaryKey> testInstance = this.getTestInstance(DomainWithIntPrimaryKey.class);

        // Business method
        boolean isValid = testInstance.isValid(value);

        // Asserts
        assertThat(isValid).isTrue();
    }

    @Test
    public void isValidForNonPersistentEntityWithStringPrimaryKey() {
        when(this.table.maps(DomainWithStringPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithStringPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithStringPrimaryKey> testInstance = this.getTestInstance(DomainWithStringPrimaryKey.class);
        DomainWithStringPrimaryKey value = new DomainWithStringPrimaryKey();

        // Business method
        boolean isValid = testInstance.isValid(value);

        // Asserts
        assertThat(isValid).isFalse();
    }

    @Test
    public void isValidForPersistentEntityWithStringPrimaryKey() {
        DomainWithStringPrimaryKey value = new DomainWithStringPrimaryKey();
        value.id = "3";
        when(this.table.maps(DomainWithStringPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithStringPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        when(this.primaryKeyColumn.getDatabaseValue(value)).thenReturn(value.id);
        ReferenceValueFactory<DomainWithStringPrimaryKey> testInstance = this.getTestInstance(DomainWithStringPrimaryKey.class);

        // Business method
        boolean isValid = testInstance.isValid(value);

        // Asserts
        assertThat(isValid).isTrue();
    }

    @Test
    public void bindPreparedStatement() throws SQLException {
        DomainWithStringPrimaryKey value = new DomainWithStringPrimaryKey();
        value.id = "3";
        when(this.table.maps(DomainWithStringPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithStringPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        when(this.primaryKeyColumn.getDatabaseValue(value)).thenReturn(value.id);
        ReferenceValueFactory<DomainWithStringPrimaryKey> testInstance = this.getTestInstance(DomainWithStringPrimaryKey.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        // Business method
        testInstance.bind(preparedStatement, 1, value);

        // Asserts
        verify(preparedStatement).setObject(1, value.id);
    }

    private interface HasRawLongId {
        long getId();
    }

    private interface HasRawIntId {
        int getId();
    }

    private interface HasLongId {
        Long getId();
    }

    private interface HasIntegerId {
        Integer getId();
    }

    private interface HasStringId {
        String getId();
    }

    private class DomainWithMultiValuePrimaryKey {
        private String processId;
        private String deploymentId;

        public String getProcessId() {
            return processId;
        }

        public String getDeploymentId() {
            return deploymentId;
        }
    }

    private class DomainWithUnsupportedPrimaryKey {
        private Reference<Table> id;

        public Reference<Table> getId() {
            return id;
        }
    }

    private class DomainWithRawLongPrimaryKey implements HasRawLongId {
        private long id;

        @Override
        public long getId() {
            return id;
        }
    }

    private class DomainWithIntPrimaryKey implements HasRawIntId {
        private int id;

        @Override
        public int getId() {
            return id;
        }
    }

    private class DomainWithLongPrimaryKey implements HasLongId {
        private Long id;

        @Override
        public Long getId() {
            return id;
        }
    }

    private class DomainWithIntegerPrimaryKey implements HasIntegerId {
        private Integer id;

        @Override
        public Integer getId() {
            return id;
        }
    }

    private class DomainWithStringPrimaryKey implements HasStringId {
        private String id;

        @Override
        public String getId() {
            return id;
        }
    }

    private ReferenceValueFactory getTestInstance() {
        return new ReferenceValueFactory(this.ormService, beanService);
    }

    private <T> ReferenceValueFactory<T> getTestInstance(Class<T> domainClass) {
        return new ReferenceValueFactory<T>(this.ormService, beanService).init(domainClass);
    }

    private <T> ReferenceValueFactory<T> getTestInstanceWithValidation(Class<T> domainClass) {
        return new ReferenceValueFactory<T>(this.ormService, beanService).initWithValidation(domainClass);
    }

}