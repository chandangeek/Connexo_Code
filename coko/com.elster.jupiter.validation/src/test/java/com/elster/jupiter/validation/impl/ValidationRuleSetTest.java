/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;

import com.google.common.collect.ImmutableList;

import javax.inject.Provider;
import javax.validation.ValidatorFactory;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleSetTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;
    private static final String NAME = "name";
    private ValidationRuleSetImpl validationRuleSet;

    @Mock
    private DataMapper<IValidationRule> ruleFactory;
    @Mock
    private DataMapper<IValidationRuleSet> setFactory;
    @Mock
    private DataMapper<IValidationRuleSetVersion> ruleSetVersionFactory;
    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private ValidatorCreator validatorCreator;
    @Mock
    private DataMapper<ValidationRuleProperties> rulePropertiesSet;
    @Mock
    private DataMapper<ReadingTypeInValidationRule> readingTypeInValidationFactory;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private javax.validation.Validator validator;
    @Mock
    private QueryExecutor<IValidationRule> queryExecutor;
    @Mock
    private Clock clock;

    private Provider<ReadingTypeInValidationRuleImpl> readingTypeInRuleProvider = () -> new ReadingTypeInValidationRuleImpl(meteringService);
    private Provider<ValidationRuleImpl> ruleProvider = () -> new ValidationRuleImpl(dataModel, validatorCreator, thesaurus, meteringService, eventService, readingTypeInRuleProvider,clock);
    private Provider<ValidationRuleSetVersionImpl> versionProvider = () -> new ValidationRuleSetVersionImpl(dataModel, eventService, ruleProvider, clock);

    @Before
    public void setUp() {
        when(dataModel.mapper(IValidationRule.class)).thenReturn(ruleFactory);
        when(dataModel.mapper(IValidationRuleSet.class)).thenReturn(setFactory);
        when(dataModel.mapper(IValidationRuleSetVersion.class)).thenReturn(ruleSetVersionFactory);
        when(dataModel.mapper(ValidationRuleProperties.class)).thenReturn(rulePropertiesSet);
        when(dataModel.mapper(ReadingTypeInValidationRule.class)).thenReturn(readingTypeInValidationFactory);
        when(dataModel.query(IValidationRule.class, IValidationRuleSet.class, ValidationRuleProperties.class)).thenReturn(queryExecutor);
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(dataModel.getValidatorFactory().getValidator()).thenReturn(validator);
        when(clock.instant()).thenReturn(Instant.now());
        validationRuleSet = new ValidationRuleSetImpl(dataModel, eventService, versionProvider, clock).init(NAME, QualityCodeSystem.MDC, null);
    }
    @After
    public void tearDown() {
    }

    @Override
    protected Object getInstanceA() {
        if (validationRuleSet == null) {
            validationRuleSet = new ValidationRuleSetImpl(dataModel, eventService, versionProvider, clock).init(NAME, QualityCodeSystem.MDC, null);
            setId(validationRuleSet, ID);
        }
        return validationRuleSet;
    }

    private void setId(Object entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
    }

    @Override
    protected Object getInstanceEqualToA() {
        ValidationRuleSetImpl set = new ValidationRuleSetImpl(dataModel, eventService, versionProvider, clock).init(NAME, QualityCodeSystem.MDC, null);
        setId(set, ID);
        return set;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        ValidationRuleSetImpl set = new ValidationRuleSetImpl(dataModel, eventService, versionProvider, clock).init(NAME, QualityCodeSystem.MDC, null);
        setId(set, OTHER_ID);
        return ImmutableList.of(set);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    @Test
    public void testGetNameAfterCreation() {
        assertThat(validationRuleSet.getName()).isEqualTo(NAME);
    }

    @Test
    public void testGetApplicationNameGetQualityCodeSystem() {
        assertThat(validationRuleSet.getQualityCodeSystem()).isEqualTo(QualityCodeSystem.MDC);
        assertThat(validationRuleSet.init(NAME, QualityCodeSystem.MDM, null).getQualityCodeSystem())
                .isEqualTo(QualityCodeSystem.MDM);
        assertThat(validationRuleSet.init(NAME, QualityCodeSystem.NOTAPPLICABLE, null).getQualityCodeSystem())
                .isEqualTo(QualityCodeSystem.NOTAPPLICABLE);
    }

    @Test
    public void testPersist() {
        validationRuleSet.save();

        verify(dataModel).persist(validationRuleSet);
    }

    @Test
    public void testUpdate() {
        setId(validationRuleSet, ID);

        validationRuleSet.save();

        verify(dataModel).update(validationRuleSet);

    }

    @Test
    public void testPersistWithVersion() {
        validationRuleSet.addRuleSetVersion("description", Instant.now());

        validationRuleSet.save();

        verify(dataModel).persist(validationRuleSet);        
    }

    @Test
    public void testPersistWithVersions() {
        validationRuleSet.addRuleSetVersion("description1", Instant.now());
        validationRuleSet.addRuleSetVersion("description2", Instant.now());

        validationRuleSet.save();

        verify(dataModel).persist(validationRuleSet);
    }

    @Test
    public void testDeleteWithVersions() {
        IValidationRuleSetVersion version1 = validationRuleSet.addRuleSetVersion("description1", Instant.now());
        validationRuleSet.save();
        setId(validationRuleSet, ID);
        setId(version1, 1001L);

        validationRuleSet.delete();

        verify(setFactory).update(validationRuleSet);
        verify(ruleSetVersionFactory).update(version1);

        assertThat(validationRuleSet.getObsoleteDate()).isNotNull();
        assertThat(version1.getObsoleteDate()).isNotNull();
    }

    @Test
    public void testUpdateWithVersionsPerformsNecessaryDBOperations() {
        IValidationRuleSetVersion version1 = validationRuleSet.addRuleSetVersion("description1", Instant.now());
        IValidationRuleSetVersion version2 = validationRuleSet.addRuleSetVersion("description2", Instant.now());
        validationRuleSet.save();
        setId(validationRuleSet, ID);

        setId(version1, 1001L);
        setId(version2, 1002L);
        when(ruleSetVersionFactory.find()).thenReturn(Arrays.asList(version1, version2));

        validationRuleSet.deleteRuleSetVersion(version1);
        IValidationRuleSetVersion version3 = validationRuleSet.addRuleSetVersion("description3", Instant.now());

        validationRuleSet.save();

        verify(dataModel).update(validationRuleSet);
        assertThat(validationRuleSet.getRuleSetVersions()).hasSize(2).contains(version2, version3);

    }

    @Test
    public void testUpdateVersionAction() {
        IValidationRuleSetVersion version1 = validationRuleSet.addRuleSetVersion("description1", Instant.EPOCH);
        validationRuleSet.save();
        setId(validationRuleSet, ID);
        setId(version1, 1001L);
        when(ruleSetVersionFactory.find()).thenReturn(Arrays.asList(version1));
        validationRuleSet.save();
        assertThat(version1.getStartDate()).isEqualTo(Instant.EPOCH);

        version1 = validationRuleSet.updateRuleSetVersion(1001L, "description2", Instant.now());
        validationRuleSet.save();

        assertThat(validationRuleSet.getRuleSetVersions()).hasSize(1).contains(version1);
        assertThat(version1.getDescription()).isEqualTo("description2");
    }

    @Test
    public void testAddSecondRuleSetVersionWithNoneStartDate() {
        //COMU-3142
        Instant now = Instant.now();
        validationRuleSet.addRuleSetVersion("From now", now);
        validationRuleSet.addRuleSetVersion("Before now", null);

        List<IValidationRuleSetVersion> ruleSetVersions = validationRuleSet.getRuleSetVersions();

        assertThat(ruleSetVersions).hasSize(2);
        assertThat(ruleSetVersions.get(0).getNotNullStartDate()).isEqualTo(Instant.EPOCH);
        assertThat(ruleSetVersions.get(0).getNotNullEndDate()).isEqualTo(now);
        assertThat(ruleSetVersions.get(0).getDescription()).isEqualTo("Before now");
        assertThat(ruleSetVersions.get(1).getNotNullStartDate()).isEqualTo(now);
        assertThat(ruleSetVersions.get(1).getNotNullEndDate()).isEqualTo(Instant.MAX);
        assertThat(ruleSetVersions.get(1).getDescription()).isEqualTo("From now");
    }
}
