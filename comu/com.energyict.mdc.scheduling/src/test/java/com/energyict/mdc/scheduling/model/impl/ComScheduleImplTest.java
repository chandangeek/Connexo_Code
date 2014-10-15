package com.energyict.mdc.scheduling.model.impl;

import com.energyict.mdc.common.Global;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComScheduleBuilder;
import com.energyict.mdc.tasks.ComTask;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.transaction.TransactionContext;
import com.google.common.base.Optional;

import org.apache.commons.lang3.StringUtils;

import java.time.Instant;

import org.junit.*;
import org.junit.rules.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ComScheduleImplTest extends PersistenceTest {

    private static final TimeDuration TEN_MINUTES = new TimeDuration("10 minutes");
    private static final TimeDuration TWENTY_SECONDS = new TimeDuration("20 seconds");
    private static final TimeDuration THIRTY_SECONDS = new TimeDuration("30 seconds");

    private static ComTask simpleComTask;
    static{
        try (TransactionContext context = inMemoryPersistence.getTransactionService().getContext()){
            simpleComTask = inMemoryPersistence.getTaskService().newComTask("Simple task");
            simpleComTask.createStatusInformationTask();
            simpleComTask.save();
            context.commit();
        }
    }

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TestRule transactionalRule = new TransactionalRule(inMemoryPersistence.getTransactionService());

    @Test
    @Transactional
    public void testSimpleCreateAndRetrieveSchedule() throws Exception {
        ComScheduleBuilder comScheduleBuilder = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now());
        comScheduleBuilder.mrid("xyz");
        ComSchedule comSchedule = comScheduleBuilder.build();
        comSchedule.addComTask(simpleComTask);

        // Business method
        comSchedule.save();

        // Asserts
        Optional<ComSchedule> retrievedSchedule = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        assertThat(retrievedSchedule.isPresent()).isTrue();
        assertThat(retrievedSchedule.get().getName()).isEqualTo("name");
        assertThat(retrievedSchedule.get().getmRID()).isEqualTo("xyz");
        assertThat(retrievedSchedule.get().getTemporalExpression().getEvery()).isEqualTo(TEN_MINUTES);
        assertThat(retrievedSchedule.get().getTemporalExpression().getOffset()).isEqualTo(TWENTY_SECONDS);
        assertThat(retrievedSchedule.get().isObsolete()).isFalse();
    }

    @Test
    @Transactional
    public void testNameIsTrimmed() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name ", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();
        Optional<ComSchedule> retrievedSchedule = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        assertThat(retrievedSchedule.isPresent()).isTrue();
        assertThat(retrievedSchedule.get().getName()).isEqualTo("name");
    }

    @Test
    @Transactional
    public void testMRIDIsTrimmed() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name ", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).mrid(" mrid ").build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();
        Optional<ComSchedule> retrievedSchedule = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        assertThat(retrievedSchedule.isPresent()).isTrue();
        assertThat(retrievedSchedule.get().getmRID()).isEqualTo("mrid");
    }

    @Test
    @Transactional
    public void testMRIDCanBuNull() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name ", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).mrid(null).build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();
        Optional<ComSchedule> retrievedSchedule = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        assertThat(retrievedSchedule.isPresent()).isTrue();
        assertThat(retrievedSchedule.get().getmRID()).isNull();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.NOT_UNIQUE+"}", property = "name")
    public void testCanNotDuplicateName() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("nameX", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();

        comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("nameX", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.TOO_LONG+"}", property = "name")
    public void testNameMaxLength() throws Exception {
        String illegalName = StringUtils.repeat("x", Global.DEFAULT_DB_STRING_LENGTH + 1);
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule(illegalName, temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.TOO_LONG+"}", property = "mRID")
    public void testMridMaxLength() throws Exception {
        String illegalMrid = StringUtils.repeat("x", Global.DEFAULT_DB_STRING_LENGTH + 1);
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).mrid(illegalMrid).build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.NOT_UNIQUE+"}", property = "name")
    public void testCanNotDuplicateNameThroughUpdate() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("nameX", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();

        comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("nameOK", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();
        comSchedule.setName("nameX");
        comSchedule.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.NOT_UNIQUE+"}", property = "mRID")
    public void testCanNotDuplicateMRID() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("nameX", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).mrid("MRID").build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();
        comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("nameY", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).mrid("MRID").build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.NOT_UNIQUE+"}", property = "mRID")
    public void testCanNotDuplicateMRIDThroughUpdate() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("nameX", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).mrid("MRID").build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();

        comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("nameY", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).mrid("MRID-OK").build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();
        comSchedule.setmRID("MRID");
        comSchedule.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}", property = "startDate")
    public void testCanNotCreateWithoutStartDate() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("nameX", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), null).build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}", property = "startDate")
    public void testCanNotUpdateWithoutStartDate() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("nameX", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).build();
        comSchedule.setStartDate(null);
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}", property = "name")
    public void testCanNotCreateWithoutName() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule(null, temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.CAN_NOT_BE_EMPTY+"}", property = "name")
    public void testCanNotUpdateWithoutName() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).build();
        comSchedule.setName(null);
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED_KEY+"}", property = "nextExecutionSpecs.temporalExpression")
    public void testCanCreateWithoutTemporalExpression() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("nameX", null, Instant.now()).build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();
    }

    @Test
    @Transactional
    public void testDeleteComSchedule() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();
        Optional<ComSchedule> retrievedSchedule = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        retrievedSchedule.get().delete();
        assertThat(inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId()).isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testUpdateComScheduleTemporalExpression() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();
        Optional<ComSchedule> retrievedSchedule = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        retrievedSchedule.get().setTemporalExpression(temporalExpression(TEN_MINUTES, THIRTY_SECONDS));
        retrievedSchedule.get().save();

        assertThat(inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId())).isNotNull();
    }

    @Test
    @Transactional
    public void testAddComTaskToUnusedSchedule() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).build();
        ComTask comTask1 = inMemoryPersistence.getTaskService().newComTask("task 1");
        comTask1.createStatusInformationTask();
        comTask1.save();

        comSchedule.addComTask(comTask1);
        comSchedule.save();

        Optional<ComSchedule> retrieved = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.isPresent()).isTrue();
        assertThat(retrieved.get().getComTasks()).isNotEmpty();
        assertThat(retrieved.get().getComTasks().get(0).getId()).isEqualTo(comTask1.getId());
        assertThat(retrieved.get().getComTasks().get(0).getName()).isEqualTo(comTask1.getName());

    }

    @Test
    @Transactional
    public void testAddSecondComTaskToSchedule() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).build();
        ComTask comTask1 = inMemoryPersistence.getTaskService().newComTask("task 1");
        comTask1.createStatusInformationTask();
        comTask1.save();

        comSchedule.addComTask(comTask1);
        comSchedule.save();

        Optional<ComSchedule> updating = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        ComTask comTask2 = inMemoryPersistence.getTaskService().newComTask("task 2");
        comTask2.createStatusInformationTask();
        comTask2.save();
        updating.get().addComTask(comTask2);
        updating.get().save();

        Optional<ComSchedule> retrieved = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.isPresent()).isTrue();
        assertThat(retrieved.get().getComTasks()).isNotEmpty().hasSize(2);
        assertThat(retrieved.get().getComTasks().get(1).getId()).isEqualTo(comTask2.getId());
        assertThat(retrieved.get().getComTasks().get(1).getName()).isEqualTo(comTask2.getName());

    }

    @Test
    @Transactional
    public void testDeleteComTaskFromSchedule() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).build();

        ComTask comTask1 = inMemoryPersistence.getTaskService().newComTask("task 1");
        comTask1.createStatusInformationTask();
        comTask1.save();
        comSchedule.addComTask(comTask1);
        comSchedule.save();

        ComTask comTask2 = inMemoryPersistence.getTaskService().newComTask("task 2");
        comTask2.createStatusInformationTask();
        comTask2.save();
        comSchedule.addComTask(comTask2);
        comSchedule.save();

        Optional<ComSchedule> updating = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        ComTask task = inMemoryPersistence.getTaskService().findComTask(comTask2.getId());
        updating.get().removeComTask(task);
        updating.get().save();

        Optional<ComSchedule> retrieved = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.isPresent()).isTrue();
        assertThat(retrieved.get().getComTasks()).isNotEmpty().hasSize(1);
        assertThat(retrieved.get().getComTasks().get(0).getId()).isEqualTo(comTask1.getId());
        assertThat(retrieved.get().getComTasks().get(0).getName()).isEqualTo(comTask1.getName());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.COM_TASK_USAGES_NOT_FOUND+"}", property = "comTaskUsages")
    public void testCreateComScheduleWithouComTask() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).build();
        comSchedule.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.COM_TASK_USAGES_NOT_FOUND+"}", property = "comTaskUsages")
    public void testSaveComScheduleWithouComTask() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();

        ComSchedule updating = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId()).get();
        updating.removeComTask(simpleComTask);
        updating.save();
    }

    @Test
    @Transactional
    public void testMakeObsolete() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("testMakeObsolete", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), Instant.now()).build();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();

        // Business method
        comSchedule.makeObsolete();

        // Asserts
        assertThat(comSchedule.isObsolete()).isTrue();
    }

    private TemporalExpression temporalExpression(TimeDuration ... td) {
        if (td.length==1) {
            return new TemporalExpression(td[0]);
        } else {
            return new TemporalExpression(td[0], td[1]);
        }
    }

}