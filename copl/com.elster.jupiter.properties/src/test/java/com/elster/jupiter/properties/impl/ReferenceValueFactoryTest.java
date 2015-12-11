package com.elster.jupiter.properties.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;

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
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ReferenceValueFactory} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReferenceValueFactoryTest {

    @Mock
    private OrmService ormService;
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
    public void testDomainClass() {
        assertThat(this.getTestInstance(DomainWithLongPrimaryKey.class).getValueType()).isEqualTo(DomainWithLongPrimaryKey.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noDataModelsThatSupportsApiClass() {
        when(this.ormService.getDataModels()).thenReturn(Collections.emptyList());

        // Business method
        this.getTestInstanceWithValidation(DomainWithLongPrimaryKey.class);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void noDataModelThatSupportsApiClass() {
        doThrow(IllegalArgumentException.class).when(this.dataModel).mapper(any(Class.class));

        // Business method
        this.getTestInstanceWithValidation(DomainWithLongPrimaryKey.class);

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
    public void longPrimaryKeyTypeIsSupported() {
        when(this.table.maps(DomainWithLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");

        // Business method
        this.getTestInstanceWithValidation(DomainWithLongPrimaryKey.class);

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
    public void stringPrimaryKeyTypeIsSupported() {
        when(this.table.maps(DomainWithStringPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithStringPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");

        // Business method
        this.getTestInstanceWithValidation(DomainWithStringPrimaryKey.class);

        // Asserts: see expected exception rule
    }

    @Test
    public void fromStringValueForLongPrimaryKeyThatExists() {
        when(this.table.maps(DomainWithLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithLongPrimaryKey> testInstance = this.getTestInstance(DomainWithLongPrimaryKey.class);
        DomainWithLongPrimaryKey persistentValue = new DomainWithLongPrimaryKey();
        persistentValue.id = 3L;
        when(this.dataMapper.getOptional(3L)).thenReturn(Optional.of(persistentValue));

        // Business method
        DomainWithLongPrimaryKey value = testInstance.fromStringValue("3");

        // Asserts
        assertThat(value).isEqualTo(persistentValue);
    }

    @Test
    public void fromStringValueForLongPrimaryKeyThatDoesNotExist() {
        when(this.table.maps(DomainWithLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithLongPrimaryKey> testInstance = this.getTestInstance(DomainWithLongPrimaryKey.class);
        when(this.dataMapper.getOptional(anyLong())).thenReturn(Optional.empty());

        // Business method
        DomainWithLongPrimaryKey value = testInstance.fromStringValue("3");

        // Asserts
        assertThat(value).isNull();
    }

    @Test
    public void fromStringValueForIntegerPrimaryKeyThatExists() {
        when(this.table.maps(DomainWithIntegerPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithIntegerPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithIntegerPrimaryKey> testInstance = this.getTestInstance(DomainWithIntegerPrimaryKey.class);
        DomainWithIntegerPrimaryKey persistentValue = new DomainWithIntegerPrimaryKey();
        persistentValue.id = 3;
        when(this.dataMapper.getOptional(3)).thenReturn(Optional.of(persistentValue));

        // Business method
        DomainWithIntegerPrimaryKey value = testInstance.fromStringValue("3");

        // Asserts
        assertThat(value).isEqualTo(persistentValue);
    }

    @Test
    public void fromStringValueForIntegerPrimaryKeyThatDoesNotExist() {
        when(this.table.maps(DomainWithIntegerPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithIntegerPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithIntegerPrimaryKey> testInstance = this.getTestInstance(DomainWithIntegerPrimaryKey.class);
        when(this.dataMapper.getOptional(anyInt())).thenReturn(Optional.empty());

        // Business method
        DomainWithIntegerPrimaryKey value = testInstance.fromStringValue("3");

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
        when(this.table.maps(DomainWithLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithLongPrimaryKey> testInstance = this.getTestInstance(DomainWithLongPrimaryKey.class);
        DomainWithLongPrimaryKey persistentValue = new DomainWithLongPrimaryKey();
        persistentValue.id = 3L;

        // Business method
        String value = testInstance.toStringValue(persistentValue);

        // Asserts
        assertThat(value).isEqualTo("3");
    }

    @Test
    public void toStringValueForIntegerPrimaryKey() {
        when(this.table.maps(DomainWithIntegerPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithIntegerPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithIntegerPrimaryKey> testInstance = this.getTestInstance(DomainWithIntegerPrimaryKey.class);
        DomainWithIntegerPrimaryKey persistentValue = new DomainWithIntegerPrimaryKey();
        persistentValue.id = 3;

        // Business method
        String value = testInstance.toStringValue(persistentValue);

        // Asserts
        assertThat(value).isEqualTo("3");
    }

    @Test
    public void toStringValueForStringPrimaryKey() {
        when(this.table.maps(DomainWithStringPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithStringPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithStringPrimaryKey> testInstance = this.getTestInstance(DomainWithStringPrimaryKey.class);
        DomainWithStringPrimaryKey persistentValue = new DomainWithStringPrimaryKey();
        persistentValue.id = "3";

        // Business method
        String value = testInstance.toStringValue(persistentValue);

        // Asserts
        assertThat(value).isEqualTo("3");
    }

    @Test
    public void valueFromDatabaseForLongPrimaryKeyThatExists() {
        when(this.table.maps(DomainWithLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithLongPrimaryKey> testInstance = this.getTestInstance(DomainWithLongPrimaryKey.class);
        DomainWithLongPrimaryKey persistentValue = new DomainWithLongPrimaryKey();
        persistentValue.id = 3L;
        when(this.dataMapper.getOptional(3L)).thenReturn(Optional.of(persistentValue));

        // Business method
        DomainWithLongPrimaryKey value = testInstance.valueFromDatabase(3L);

        // Asserts
        assertThat(value).isEqualTo(persistentValue);
    }

    @Test
    public void valueFromDatabaseForLongPrimaryKeyThatDoesNotExist() {
        when(this.table.maps(DomainWithLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithLongPrimaryKey> testInstance = this.getTestInstance(DomainWithLongPrimaryKey.class);
        when(this.dataMapper.getOptional(anyLong())).thenReturn(Optional.empty());

        // Business method
        DomainWithLongPrimaryKey value = testInstance.valueFromDatabase(3L);

        // Asserts
        assertThat(value).isNull();
    }

    @Test
    public void valueFromDatabaseForIntegerPrimaryKeyThatExists() {
        when(this.table.maps(DomainWithIntegerPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithIntegerPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithIntegerPrimaryKey> testInstance = this.getTestInstance(DomainWithIntegerPrimaryKey.class);
        DomainWithIntegerPrimaryKey persistentValue = new DomainWithIntegerPrimaryKey();
        persistentValue.id = 3;
        when(this.dataMapper.getOptional(3)).thenReturn(Optional.of(persistentValue));

        // Business method
        DomainWithIntegerPrimaryKey value = testInstance.valueFromDatabase(3);

        // Asserts
        assertThat(value).isEqualTo(persistentValue);
    }

    @Test
    public void valueFromDatabaseForIntegerPrimaryKeyThatDoesNotExist() {
        when(this.table.maps(DomainWithIntegerPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithIntegerPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithIntegerPrimaryKey> testInstance = this.getTestInstance(DomainWithIntegerPrimaryKey.class);
        when(this.dataMapper.getOptional(anyInt())).thenReturn(Optional.empty());

        // Business method
        DomainWithIntegerPrimaryKey value = testInstance.valueFromDatabase(3);

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
        when(this.table.maps(DomainWithLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithLongPrimaryKey> testInstance = this.getTestInstance(DomainWithLongPrimaryKey.class);
        DomainWithLongPrimaryKey persistentValue = new DomainWithLongPrimaryKey();
        persistentValue.id = 3L;

        // Business method
        Object value = testInstance.valueToDatabase(persistentValue);

        // Asserts
        assertThat(value).isEqualTo(3L);
    }

    @Test
    public void valueToDatabaseForIntegerPrimaryKey() {
        when(this.table.maps(DomainWithIntegerPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithIntegerPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithIntegerPrimaryKey> testInstance = this.getTestInstance(DomainWithIntegerPrimaryKey.class);
        DomainWithIntegerPrimaryKey persistentValue = new DomainWithIntegerPrimaryKey();
        persistentValue.id = 3;

        // Business method
        Object value = testInstance.valueToDatabase(persistentValue);

        // Asserts
        assertThat(value).isEqualTo(3);
    }

    @Test
    public void valueToDatabaseForStringPrimaryKey() {
        when(this.table.maps(DomainWithStringPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithStringPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithStringPrimaryKey> testInstance = this.getTestInstance(DomainWithStringPrimaryKey.class);
        DomainWithStringPrimaryKey persistentValue = new DomainWithStringPrimaryKey();
        persistentValue.id = "3";

        // Business method
        Object value = testInstance.valueToDatabase(persistentValue);

        // Asserts
        assertThat(value).isEqualTo("3");
    }

    @Test
    public void isValidForNonPersistentEntityWithLongPrimaryKey() {
        when(this.table.maps(DomainWithLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithLongPrimaryKey> testInstance = this.getTestInstance(DomainWithLongPrimaryKey.class);
        DomainWithLongPrimaryKey value = new DomainWithLongPrimaryKey();

        // Business method
        boolean isValid = testInstance.isValid(value);

        // Asserts
        assertThat(isValid).isFalse();
    }

    @Test
    public void isValidForPersistentEntityWithLongPrimaryKey() {
        when(this.table.maps(DomainWithLongPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithLongPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithLongPrimaryKey> testInstance = this.getTestInstance(DomainWithLongPrimaryKey.class);
        DomainWithLongPrimaryKey value = new DomainWithLongPrimaryKey();
        value.id = 3L;

        // Business method
        boolean isValid = testInstance.isValid(value);

        // Asserts
        assertThat(isValid).isTrue();
    }

    @Test
    public void isValidForNonPersistentEntityWithIntegerPrimaryKey() {
        when(this.table.maps(DomainWithIntegerPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithIntegerPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithIntegerPrimaryKey> testInstance = this.getTestInstance(DomainWithIntegerPrimaryKey.class);
        DomainWithIntegerPrimaryKey value = new DomainWithIntegerPrimaryKey();

        // Business method
        boolean isValid = testInstance.isValid(value);

        // Asserts
        assertThat(isValid).isFalse();
    }

    @Test
    public void isValidForPersistentEntityWithIntegerPrimaryKey() {
        when(this.table.maps(DomainWithIntegerPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithIntegerPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithIntegerPrimaryKey> testInstance = this.getTestInstance(DomainWithIntegerPrimaryKey.class);
        DomainWithIntegerPrimaryKey value = new DomainWithIntegerPrimaryKey();
        value.id = 3;

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
        when(this.table.maps(DomainWithStringPrimaryKey.class)).thenReturn(true);
        when(this.dataModel.mapper(DomainWithStringPrimaryKey.class)).thenReturn(this.dataMapper);
        when(this.primaryKeyColumn.getFieldName()).thenReturn("id");
        ReferenceValueFactory<DomainWithStringPrimaryKey> testInstance = this.getTestInstance(DomainWithStringPrimaryKey.class);
        DomainWithStringPrimaryKey value = new DomainWithStringPrimaryKey();
        value.id = "3";

        // Business method
        boolean isValid = testInstance.isValid(value);

        // Asserts
        assertThat(isValid).isTrue();
    }

    private class DomainWithMultiValuePrimaryKey {
        private String processId;
        private String deploymentId;
    }

    private class DomainWithUnsupportedPrimaryKey {
        private Reference<Table> id;
    }

    private class DomainWithLongPrimaryKey {
        private long id;
    }

    private class DomainWithIntegerPrimaryKey {
        private int id;
    }

    private class DomainWithStringPrimaryKey {
        private String id;
    }

    private ReferenceValueFactory getTestInstance() {
        return new ReferenceValueFactory(this.ormService);
    }

    private <T> ReferenceValueFactory<T> getTestInstance(Class<T> domainClass) {
        return new ReferenceValueFactory<T>(this.ormService).init(domainClass);
    }

    private <T> ReferenceValueFactory<T> getTestInstanceWithValidation(Class<T> domainClass) {
        return new ReferenceValueFactory<T>(this.ormService).initWithValidation(domainClass);
    }

}