package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.PersistenceTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link com.energyict.mdc.tasks.impl.ComTaskImpl} component.
 *
 * @author gna
 * @since 7/05/12 - 11:46
 */
public class ComTaskImplTest extends PersistenceTest {

    private static final boolean STORE_DATA_TRUE = true;
    private static final String COM_TASK_NAME = "UniqueComTaskName";

    private ComTask createSimpleComTask() {
        ComTask comTask = getTaskService().createComTask();
        comTask.setName(COM_TASK_NAME);
        comTask.setStoreData(STORE_DATA_TRUE);
        comTask.save();
        return comTask;
    }

//    private ComTaskShadow createComTaskShadowWithoutViolations () {
//        ComTaskShadow shadow = createSimpleComTask();
//        shadow.addProtocolTask(BasicCheckTaskImplTest.createBasicCheckTaskShadow());
//        return shadow;
//    }
//
//    private ComTask createComTaskWithBasicAndClockTask () throws BusinessException, SQLException {
//        return createSimpleComTask(createComTaskShadowWithBasicAndClockTask());
//    }
//
//    private ComTaskShadow createComTaskShadowWithBasicAndClockTask () {
//        ComTaskShadow shadow = createSimpleComTask();
//        BasicCheckTaskShadow basicCheckTaskShadow = BasicCheckTaskImplTest.createBasicCheckTaskShadow();
//        ClockTaskShadow clockTaskShadow = ClockTaskImplTest.createClockTaskShadow();
//        shadow.setProtocolTaskShadows(new ShadowList<>(Arrays.asList(basicCheckTaskShadow, clockTaskShadow)));
//        return shadow;
//    }

    @Test
    public void testGetTypeDoesNotReturnServerBasedClassName () {
        ComTask comTask = getTaskService().createComTask();

        // Business method
        String type = comTask.getType();

        // Asserts
        assertThat(type).doesNotContain(".Server");
    }

}
