package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.common.Global;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComScheduleBuilder;
import com.energyict.mdc.tasks.ComTask;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.assertThat;

public class ComScheduleImplTest extends PersistenceTest {

    private static final TimeDuration TEN_MINUTES = new TimeDuration("10 minutes");
    private static final TimeDuration TWENTY_SECONDS = new TimeDuration("20 seconds");
    private static final TimeDuration THIRTY_SECONDS = new TimeDuration("30 seconds");
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TestRule transactionalRule = new TransactionalRule(inMemoryPersistence.getTransactionService());

    @Test
    @Transactional
    public void testSimpleCreateAndRetrieveSchedule() throws Exception {
        ComScheduleBuilder comScheduleBuilder = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date()));
        comScheduleBuilder.mrid("xyz");
        ComSchedule comSchedule = comScheduleBuilder.build();
        comSchedule.save();
        ComSchedule retrievedSchedule = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        assertThat(retrievedSchedule.getName()).isEqualTo("name");
        assertThat(retrievedSchedule.getmRID()).isEqualTo("xyz");
        assertThat(retrievedSchedule.getTemporalExpression().getEvery()).isEqualTo(TEN_MINUTES);
        assertThat(retrievedSchedule.getTemporalExpression().getOffset()).isEqualTo(TWENTY_SECONDS);
    }

    @Test
    @Transactional
    public void testNameIsTrimmed() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name ", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).build();
        ComSchedule retrievedSchedule = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        assertThat(retrievedSchedule.getName()).isEqualTo("name");
    }

    @Test
    @Transactional
    public void testMRIDIsTrimmed() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name ", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).mrid(" mrid ").build();
        ComSchedule retrievedSchedule = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        assertThat(retrievedSchedule.getmRID()).isEqualTo("mrid");
    }

    @Test
    @Transactional
    public void testMRIDCanBuNull() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name ", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).mrid(null).build();
        ComSchedule retrievedSchedule = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        assertThat(retrievedSchedule.getmRID()).isNull();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.NOT_UNIQUE+"}", property = "name")
    public void testCanNotDuplicateName() throws Exception {
        inMemoryPersistence.getSchedulingService().newComSchedule("nameX", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).build();
        inMemoryPersistence.getSchedulingService().newComSchedule("nameX", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.TOO_LONG+"}", property = "name")
    public void testNameMaxLength() throws Exception {
        String illegalName = StringUtils.repeat("x", Global.DEFAULT_DB_STRING_LENGTH + 1);
        inMemoryPersistence.getSchedulingService().newComSchedule(illegalName, temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.TOO_LONG+"}", property = "mRID")
    public void testMridMaxLength() throws Exception {
        String illegalMrid = StringUtils.repeat("x", Global.DEFAULT_DB_STRING_LENGTH + 1);
        inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).mrid(illegalMrid).build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.NOT_UNIQUE+"}", property = "name")
    public void testCanNotDuplicateNameThroughUpdate() throws Exception {
        inMemoryPersistence.getSchedulingService().newComSchedule("nameX", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).build();
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("nameOK", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).build();
        comSchedule.setName("nameX");
        comSchedule.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.NOT_UNIQUE+"}", property = "mRID")
    public void testCanNotDuplicateMRID() throws Exception {
        inMemoryPersistence.getSchedulingService().newComSchedule("nameX", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).mrid("MRID").build();
        inMemoryPersistence.getSchedulingService().newComSchedule("nameY", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).mrid("MRID").build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.NOT_UNIQUE+"}", property = "mRID")
    public void testCanNotDuplicateMRIDThroughUpdate() throws Exception {
        inMemoryPersistence.getSchedulingService().newComSchedule("nameX", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).mrid("MRID").build();
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("nameY", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).mrid("MRID-OK").build();
        comSchedule.setmRID("MRID");
        comSchedule.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}", property = "startDate")
    public void testCanNotCreateWithoutStartDate() throws Exception {
        inMemoryPersistence.getSchedulingService().newComSchedule("nameX", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), null).build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}", property = "startDate")
    public void testCanNotUpdateWithoutStartDate() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("nameX", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).build();
        comSchedule.setStartDate(null);
        comSchedule.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}", property = "name")
    public void testCanNotCreateWithoutName() throws Exception {
        inMemoryPersistence.getSchedulingService().newComSchedule(null, temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).build();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}", property = "name")
    public void testCanNotUpdateWithoutName() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).build();
        comSchedule.setName(null);
        comSchedule.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED_KEY+"}", property = "temporalExpression")
    public void testCanCreateWithoutTemporalExpression() throws Exception {
        inMemoryPersistence.getSchedulingService().newComSchedule("nameX", null, null).build();
    }

    @Test
    @Transactional
    public void testDeleteComSchedule() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).build();
        ComSchedule retrievedSchedule = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        retrievedSchedule.delete();
        assertThat(inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId())).isNull();
    }

    @Test
    @Transactional
    public void testUpdateComScheduleTemporalExpression() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).build();
        ComSchedule retrievedSchedule = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        retrievedSchedule.setTemporalExpression(temporalExpression(TEN_MINUTES, THIRTY_SECONDS));
        retrievedSchedule.save();

        assertThat(inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId())).isNotNull();
    }

    @Test
    @Transactional
    public void testAddComTaskToUnusedSchedule() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).build();
        ComTask comTask1 = inMemoryPersistence.getTaskService().newComTask("task 1");
        comTask1.createStatusInformationTask();
        comTask1.save();

        comSchedule.addComTask(comTask1);

        ComSchedule retrieved = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getComTasks()).isNotEmpty();
        assertThat(retrieved.getComTasks().get(0).getId()).isEqualTo(comTask1.getId());
        assertThat(retrieved.getComTasks().get(0).getName()).isEqualTo(comTask1.getName());

    }

    @Test
    @Transactional
    public void testAddSecondComTaskToSchedule() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).build();
        ComTask comTask1 = inMemoryPersistence.getTaskService().newComTask("task 1");
        comTask1.createStatusInformationTask();
        comTask1.save();

        comSchedule.addComTask(comTask1);

        ComSchedule updating = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        ComTask comTask2 = inMemoryPersistence.getTaskService().newComTask("task 2");
        comTask2.createStatusInformationTask();
        comTask2.save();
        updating.addComTask(comTask2);

        ComSchedule retrieved = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getComTasks()).isNotEmpty().hasSize(2);
        assertThat(retrieved.getComTasks().get(1).getId()).isEqualTo(comTask2.getId());
        assertThat(retrieved.getComTasks().get(1).getName()).isEqualTo(comTask2.getName());

    }

    @Test
    @Transactional
    public void testDeleteComTaskFromSchedule() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date())).build();

        ComTask comTask1 = inMemoryPersistence.getTaskService().newComTask("task 1");
        comTask1.createStatusInformationTask();
        comTask1.save();
        comSchedule.addComTask(comTask1);

        ComTask comTask2 = inMemoryPersistence.getTaskService().newComTask("task 2");
        comTask2.createStatusInformationTask();
        comTask2.save();
        comSchedule.addComTask(comTask2);

        ComSchedule updating = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        ComTask task = inMemoryPersistence.getTaskService().findComTask(comTask2.getId());
        updating.removeComTask(task);

        ComSchedule retrieved = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getComTasks()).isNotEmpty().hasSize(1);
        assertThat(retrieved.getComTasks().get(0).getId()).isEqualTo(comTask1.getId());
        assertThat(retrieved.getComTasks().get(0).getName()).isEqualTo(comTask1.getName());
    }

    private TemporalExpression temporalExpression(TimeDuration ... td) {
        if (td.length==1) {
            return new TemporalExpression(td[0]);
        } else {
            return new TemporalExpression(td[0], td[1]);
        }
    }
}
