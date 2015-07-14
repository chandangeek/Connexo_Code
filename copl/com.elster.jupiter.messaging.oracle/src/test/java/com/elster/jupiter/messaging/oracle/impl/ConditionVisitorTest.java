package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.conditions.Where;
import org.junit.Test;

import java.util.Arrays;

import static com.elster.jupiter.messaging.DestinationSpec.whereCorrelationId;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bbl on 14/07/2015.
 */
public class ConditionVisitorTest {

    @Test
    public void testConditionsFilter() {
        Condition like = whereCorrelationId().like("com/energyict/mdc/connectiontask/%");
        ConditionVisitor vistor = new ConditionVisitor();
        like.visit(vistor);

        assertThat(vistor.toString()).isEqualToIgnoringCase("corrid like 'com/energyict/mdc/connectiontask/%' ESCAPE '\\'");

        like = whereCorrelationId().isEqualTo("com/energyict/mdc/connectiontask/PRIORITY_UPDATED");
        vistor = new ConditionVisitor();
        like.visit(vistor);

        assertThat(vistor.toString()).isEqualToIgnoringCase("corrid = 'com/energyict/mdc/connectiontask/PRIORITY_UPDATED'");

        like = whereCorrelationId().between("a").and("b");
        vistor = new ConditionVisitor();
        like.visit(vistor);

        assertThat(vistor.toString()).isEqualToIgnoringCase("corrid between 'a' and 'b'");
    }

    @Test
    public void testComposites() {
        Condition like = whereCorrelationId().like("com/energyict/mdc/connectiontask/%");
        Condition equal = whereCorrelationId().isEqualTo("com/energyict/mdc/connectiontask/PRIORITY_UPDATED");
        ConditionVisitor vistor = new ConditionVisitor();
        like.and(equal).visit(vistor);

        assertThat(vistor.toString()).isEqualToIgnoringCase("(corrid like 'com/energyict/mdc/connectiontask/%' ESCAPE '\\' and corrid = 'com/energyict/mdc/connectiontask/PRIORITY_UPDATED')");

        vistor = new ConditionVisitor();
        like.or(equal).visit(vistor);

        assertThat(vistor.toString()).isEqualToIgnoringCase("(corrid like 'com/energyict/mdc/connectiontask/%' ESCAPE '\\' or corrid = 'com/energyict/mdc/connectiontask/PRIORITY_UPDATED')");
    }

    @Test
    public void testNot() {
        Condition like = whereCorrelationId().like("com/energyict/mdc/connectiontask/%");
        ConditionVisitor vistor = new ConditionVisitor();
        like.not().visit(vistor);

        assertThat(vistor.toString()).isEqualToIgnoringCase(" NOT (corrid like 'com/energyict/mdc/connectiontask/%' ESCAPE '\\')");
    }

    @Test
    public void testTrueFalse() {
        ConditionVisitor visitor = new ConditionVisitor();
        Condition.TRUE.visit(visitor);

        assertThat(visitor.toString()).isEqualToIgnoringCase(" 1 = 1 ");

        visitor = new ConditionVisitor();
        Condition.FALSE.visit(visitor);

        assertThat(visitor.toString()).isEqualToIgnoringCase(" 1 = 0 ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalFilter() {
        Condition condition = Where.where("test").isEqualTo("blabla");
        condition.visit(new ConditionVisitor());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedContainsCondition() {
        Condition membership = whereCorrelationId().in(Arrays.asList("a", "b"));
        membership.visit(new ConditionVisitor());

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedMembershipCondition() {
        Subquery query = null;
        ListOperator.IN.contains(query).visit(new ConditionVisitor());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedExistsCondition() {
        Subquery query = null;
        ListOperator.exists(query).visit(new ConditionVisitor());

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedEffectiveCondition() {
        whereCorrelationId().isEffective().visit(new ConditionVisitor());
    }


}