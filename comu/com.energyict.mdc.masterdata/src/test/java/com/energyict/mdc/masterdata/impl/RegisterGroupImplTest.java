package com.energyict.mdc.masterdata.impl;

import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.InvalidateCacheRequest;
import com.elster.jupiter.pubsub.Publisher;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Arrays;
import java.util.Set;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link RegisterGroupImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-06 (11:27)
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterGroupImplTest {

    private static final long REGISTER_GROUP_ID  = 9779L;
    private static final long REGISTER_TYPE1_ID = 97L;
    private static final long REGISTER_TYPE2_ID = 101L;
    private static final long REGISTER_TYPE3_ID = 103L;

    @Mock
    private Validator validator;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private DataModel dataModel;
    @Mock
    private DataMapper<RegisterGroup> dataMapper;
    @Mock
    private EventService eventService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Publisher publisher;
    @Mock
    private RegisterType registerType1;
    @Mock
    private RegisterType registerType2;
    @Mock
    private RegisterType registerType3;

    @Before
    public void initializeMocks() {
        when(this.registerType1.getId()).thenReturn(REGISTER_TYPE1_ID);
        when(this.registerType2.getId()).thenReturn(REGISTER_TYPE2_ID);
        when(this.registerType3.getId()).thenReturn(REGISTER_TYPE3_ID);
        Set noConstraintViolations = mock(Set.class);
        when(noConstraintViolations.isEmpty()).thenReturn(true);
        when(noConstraintViolations.size()).thenReturn(0);
        when(this.validator.validate(any(), anyVararg())).thenReturn(noConstraintViolations);
        when(this.validatorFactory.getValidator()).thenReturn(this.validator);
        this.initializeMockedDataModel();
    }

    private void initializeMockedDataModel() {
        when(this.dataModel.getInstance(RegisterTypeInGroup.class)).thenReturn(new RegisterTypeInGroup());
        when(this.dataModel.getValidatorFactory()).thenReturn(this.validatorFactory);
        when(this.dataModel.mapper(RegisterGroup.class)).thenReturn(this.dataMapper);
    }

    private void resetMockedDataModel() {
        reset(this.dataModel);
        this.initializeMockedDataModel();
    }

    @Test
    public void addRegisterTypePublishesClearCacheEvent() {
        RegisterGroupImpl testInstance = this.getTestInstance();

        // Business method
        testInstance.addRegisterType(this.registerType1);

        // Asserts
        verify(this.publisher).publish(any(InvalidateCacheRequest.class));
        verify(this.dataModel, never()).touch(testInstance);
    }

    @Test
    public void addRegisterTypeToPersistentGroupPublishesClearCacheEventAndTouch() {
        RegisterGroupImpl testInstance = this.getPersistentTestInstance();

        // Business method
        testInstance.addRegisterType(this.registerType1);

        // Asserts
        verify(this.publisher).publish(any(InvalidateCacheRequest.class));
        verify(this.dataModel).touch(testInstance);
    }

    @Test
    public void addMultipleRegisterTypesPublishesOnlyOneClearCacheEvent() {
        RegisterGroupImpl testInstance = this.getTestInstance();

        // Business method
        testInstance.addRegisterType(this.registerType1);
        testInstance.addRegisterType(this.registerType2);

        // Asserts
        verify(this.publisher).publish(any(InvalidateCacheRequest.class));
        verify(this.dataModel, never()).touch(testInstance);
    }

    @Test
    public void addMultipleRegisterTypesToPersistentGroupPublishesOnlyOneClearCacheEventAndTouch() {
        RegisterGroupImpl testInstance = this.getPersistentTestInstance();

        // Business method
        testInstance.addRegisterType(this.registerType1);
        testInstance.addRegisterType(this.registerType2);

        // Asserts
        verify(this.publisher).publish(any(InvalidateCacheRequest.class));
        verify(this.dataModel).touch(testInstance);
    }

    @Test
    public void removeRegisterTypePublishesClearCacheEvent() {
        RegisterGroupImpl testInstance = this.getTestInstance();
        testInstance.addRegisterType(this.registerType1);
        testInstance.addRegisterType(this.registerType2);
        testInstance.save();
        reset(this.publisher);

        // Business method
        testInstance.removeRegisterType(this.registerType1);

        // Asserts
        verify(this.publisher).publish(any(InvalidateCacheRequest.class));
        verify(this.dataModel, never()).touch(testInstance);
    }

    @Test
    public void removeRegisterTypeFromPersistentGroupPublishesClearCacheEventAndTouch() {
        RegisterGroupImpl testInstance = this.getPersistentTestInstance();
        testInstance.addRegisterType(this.registerType1);
        testInstance.addRegisterType(this.registerType2);
        testInstance.save();
        reset(this.publisher);
        reset(this.dataModel);

        // Business method
        testInstance.removeRegisterType(this.registerType1);

        // Asserts
        verify(this.publisher).publish(any(InvalidateCacheRequest.class));
        verify(this.dataModel).touch(testInstance);
    }

    @Test
    public void removeMultipleRegisterTypesPublishesOnlyOneClearCacheEvent() {
        RegisterGroupImpl testInstance = this.getTestInstance();
        testInstance.addRegisterType(this.registerType1);
        testInstance.addRegisterType(this.registerType2);
        testInstance.addRegisterType(this.registerType3);
        testInstance.save();
        reset(this.publisher);

        // Business method
        testInstance.removeRegisterType(this.registerType1);
        testInstance.removeRegisterType(this.registerType2);

        // Asserts
        verify(this.publisher).publish(any(InvalidateCacheRequest.class));
        verify(this.dataModel, never()).touch(testInstance);
    }

    @Test
    public void removeMultipleRegisterTypesFromPersistentGroupPublishesOnlyOneClearCacheEventAndTouch() {
        RegisterGroupImpl testInstance = this.getPersistentTestInstance();
        testInstance.addRegisterType(this.registerType1);
        testInstance.addRegisterType(this.registerType2);
        testInstance.addRegisterType(this.registerType3);
        testInstance.save();
        reset(this.publisher);
        reset(this.dataModel);

        // Business method
        testInstance.removeRegisterType(this.registerType1);
        testInstance.removeRegisterType(this.registerType2);

        // Asserts
        verify(this.publisher).publish(any(InvalidateCacheRequest.class));
        verify(this.dataModel).touch(testInstance);
    }

    @Test
    public void updateRegisterTypesPublishesClearCacheEvent() {
        RegisterGroupImpl testInstance = this.getTestInstance();
        testInstance.addRegisterType(this.registerType1);
        testInstance.addRegisterType(this.registerType2);
        testInstance.save();
        reset(this.publisher);

        // Business method
        testInstance.updateRegisterTypes(Arrays.asList(this.registerType2, this.registerType3));
        testInstance.save();

        // Asserts
        verify(this.publisher).publish(any(InvalidateCacheRequest.class));
        verify(this.dataModel, never()).touch(testInstance);
    }

    @Test
    public void updateRegisterTypesForPersistentGroupPublishesOnlyOneClearCacheEventAndTouch() {
        RegisterGroupImpl testInstance = this.getPersistentTestInstance();
        testInstance.addRegisterType(this.registerType1);
        testInstance.addRegisterType(this.registerType2);
        testInstance.save();
        reset(this.publisher);
        this.resetMockedDataModel();

        // Business method
        testInstance.updateRegisterTypes(Arrays.asList(this.registerType2));
        testInstance.save();

        // Asserts
        verify(this.publisher).publish(any(InvalidateCacheRequest.class));
        verify(this.dataModel).touch(testInstance);
    }

    @Test
    public void deletePublishesClearCacheEvent() {
        RegisterGroupImpl testInstance = this.getTestInstance();
        testInstance.addRegisterType(this.registerType1);
        testInstance.addRegisterType(this.registerType2);
        testInstance.save();
        reset(this.publisher);

        // Business method
        testInstance.delete();

        // Asserts
        verify(this.publisher, never()).publish(any(InvalidateCacheRequest.class));
        verify(this.dataModel, never()).touch(testInstance);
    }

    @Test
    public void deletePersistentGroupPublishesClearCacheEvent() {
        RegisterGroupImpl testInstance = this.getPersistentTestInstance();
        testInstance.addRegisterType(this.registerType1);
        testInstance.addRegisterType(this.registerType2);
        testInstance.save();
        reset(this.publisher);
        this.resetMockedDataModel();

        // Business method
        testInstance.delete();

        // Asserts
        verify(this.publisher).publish(any(InvalidateCacheRequest.class));
        verify(this.dataModel, never()).touch(testInstance);
    }

    @Test
    public void deletePublishesValidateDeleteEventFirst() {
        RegisterGroupImpl testInstance = this.getTestInstance();

        // Business method
        testInstance.delete();

        // Asserts
        verify(this.eventService).postEvent(EventType.REGISTERGROUP_VALIDATEDELETE.topic(), testInstance);
    }

    private RegisterGroupImpl getTestInstance() {
        return new RegisterGroupImpl(this.dataModel, this.eventService, this.thesaurus, this.publisher);
    }

    private RegisterGroupImpl getPersistentTestInstance() {
        return new PersistentRegisterGroupImpl(this.dataModel, this.eventService, this.thesaurus, this.publisher);
    }

    private class PersistentRegisterGroupImpl extends RegisterGroupImpl {
        public PersistentRegisterGroupImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Publisher publisher) {
            super(dataModel, eventService, thesaurus, publisher);
        }

        public long getId() {
            return REGISTER_GROUP_ID;
        }
    }
}