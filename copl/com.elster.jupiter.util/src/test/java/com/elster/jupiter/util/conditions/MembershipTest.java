package com.elster.jupiter.util.conditions;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class MembershipTest {

    private Subquery subquery;

    @Test
    public void testSafetyOfCreation() {
        String[] fieldNames = {"a", "b"};
        Membership membership = new Membership(subquery, ListOperator.IN, fieldNames);

        fieldNames[1] = "c";

        assertThat(membership.getFieldNames()[1]).isEqualTo("b");
    }


}
