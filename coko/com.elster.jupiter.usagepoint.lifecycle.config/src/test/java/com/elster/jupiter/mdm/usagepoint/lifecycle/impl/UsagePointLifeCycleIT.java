package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycle;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleBuilder;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointLifeCycleIT extends BaseTestIT {

    @Test
    @Transactional
    public void testCanCreateSimpleLifeCycle() {
        UsagePointLifeCycleBuilder builder = get(UsagePointLifeCycleService.class).newUsagePointLifeCycle("Test");
        UsagePointLifeCycle lifeCycle = builder.complete();

        assertThat(lifeCycle.getId()).isGreaterThan(0L);
        assertThat(lifeCycle.getName()).isEqualTo("Test");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{finite.state.machine.unique.name}")
    public void testCanNotCreateLifeCycleWithTheSameNameTwice() {
        UsagePointLifeCycleBuilder builder = get(UsagePointLifeCycleService.class).newUsagePointLifeCycle("Test");
        builder.complete();

        builder = get(UsagePointLifeCycleService.class).newUsagePointLifeCycle("Test");
        builder.complete();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "name", messageId = "{CanNotBeEmpty}")
    public void testCanNotCreateLifeCycleWithEmptyName() {
        UsagePointLifeCycleBuilder builder = get(UsagePointLifeCycleService.class).newUsagePointLifeCycle("");
        builder.complete();
    }
}
