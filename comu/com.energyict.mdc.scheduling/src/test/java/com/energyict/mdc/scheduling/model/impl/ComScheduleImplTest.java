package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import java.util.Date;
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
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), new UtcInstant(new Date()));
        comSchedule.save();
        ComSchedule retrievedSchedule = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        assertThat(retrievedSchedule.getName()).isEqualTo("name");
        assertThat(retrievedSchedule.getTemporalExpression().getEvery()).isEqualTo(TEN_MINUTES);
        assertThat(retrievedSchedule.getTemporalExpression().getOffset()).isEqualTo(TWENTY_SECONDS);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.NOT_UNIQUE+"}", property = "name")
    public void testCanNotDuplicateName() throws Exception {
        inMemoryPersistence.getSchedulingService().newComSchedule("nameX", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), null).save();
        inMemoryPersistence.getSchedulingService().newComSchedule("nameX", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), null).save();
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+Constants.NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED_KEY+"}", property = "temporalExpression")
    public void testCanCreateWithoutTemporalExpression() throws Exception {
        inMemoryPersistence.getSchedulingService().newComSchedule("nameX", null, null).save();
    }

    @Test
    @Transactional
    public void testDeleteComSchedule() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), null);
        comSchedule.save();
        ComSchedule retrievedSchedule = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        retrievedSchedule.delete();
        assertThat(inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId())).isNull();
    }

    @Test
    @Transactional
    public void testUpdateComScheduleTemporalExpression() throws Exception {
        ComSchedule comSchedule = inMemoryPersistence.getSchedulingService().newComSchedule("name", temporalExpression(TEN_MINUTES, TWENTY_SECONDS), null);
        comSchedule.save();
        ComSchedule retrievedSchedule = inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId());
        retrievedSchedule.setTemporalExpression(temporalExpression(TEN_MINUTES, THIRTY_SECONDS));
        retrievedSchedule.save();

        assertThat(inMemoryPersistence.getSchedulingService().findSchedule(comSchedule.getId())).isNotNull();
    }

    private TemporalExpression temporalExpression(TimeDuration ... td) {
        if (td.length==1) {
            return new TemporalExpression(td[0]);
        } else {
            return new TemporalExpression(td[0], td[1]);
        }
    }
}
