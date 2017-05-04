/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.properties;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.ChannelValidationRuleOverriddenProperties;
import com.energyict.mdc.device.data.Device;

import java.util.Collections;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class ValidationOverriddenPropertiesEqualsContractTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;

    private static final String RULE_NAME = "VR01";
    private static final String VALIDATOR = "com...validator";

    @Mock
    private DataModel dataModel;
    @Mock
    private ValidationService validationService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Device device;
    @Mock
    private ReadingType readingType;

    private ChannelValidationRuleOverriddenProperties overriddenProperties;

    @Override
    protected Object getInstanceA() {
        if (overriddenProperties == null) {
            overriddenProperties = new ChannelValidationRuleOverriddenPropertiesImpl(dataModel, validationService, thesaurus)
                    .init(device, readingType, RULE_NAME, VALIDATOR, ValidationAction.FAIL);
            setId(overriddenProperties, ID);
        }
        return overriddenProperties;
    }

    private void setId(Object entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
    }

    @Override
    protected Object getInstanceEqualToA() {
        ChannelValidationRuleOverriddenProperties overriddenProperties = new ChannelValidationRuleOverriddenPropertiesImpl(dataModel, validationService, thesaurus)
                .init(device, readingType, RULE_NAME, VALIDATOR, ValidationAction.FAIL);
        setId(overriddenProperties, ID);
        return overriddenProperties;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        ChannelValidationRuleOverriddenProperties overriddenProperties = new ChannelValidationRuleOverriddenPropertiesImpl(dataModel, validationService, thesaurus)
                .init(device, readingType, RULE_NAME, VALIDATOR, ValidationAction.FAIL);
        setId(overriddenProperties, OTHER_ID);
        return Collections.singleton(overriddenProperties);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}
