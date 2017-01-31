/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.conditions;

import com.elster.jupiter.util.time.Interval;

import java.time.Instant;

import org.junit.Test;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;

public class WhereTest {

	@Test
	public void testInfiniteIn() {
		Interval interval = Interval.of(null,null);
		assertThat(where("test").inOpen(interval)).isEqualTo(Condition.TRUE);
		assertThat(where("test").inClosed(interval)).isEqualTo(Condition.TRUE);
		assertThat(where("test").inOpenClosed(interval)).isEqualTo(Condition.TRUE);
		assertThat(where("test").inClosedOpen(interval)).isEqualTo(Condition.TRUE);
	}

	@Test
	public void testFiniteIn() {
		Interval interval = Interval.of(Instant.EPOCH, Instant.now());
		assertThat(where("test").inOpen(interval).toString()).matches(".*test.*>[ ?].*AND.*test.*<[ ?].*");
		assertThat(where("test").inOpenClosed(interval).toString()).matches(".*test.*>[ ?].*AND.*test.*<=[ ?].*");
		assertThat(where("test").inClosedOpen(interval).toString()).matches(".*test.*>=[ ?].*AND.*test.*<[ ?].*");
		assertThat(where("test").inClosed(interval).toString()).matches(".*test.*>=[ ?].*AND.*test.*<=[ ?].*");
	}

	@Test
	public void testCurrentAt() {
		Instant date = Instant.now();
		assertThat(where("test").isEffective(date).toString()).matches(".*test\\.start\\s*<=[ ?].*AND.*test\\.end.\\s*>[ ?].*");
	}

    @Test
    public void testLikeSqlUnderscore() throws Exception {
        assertThat(Where.toOracleSql("A_C")).isEqualTo("A\\_C");
    }

    @Test
    public void testLikeSqlPercent() throws Exception {
        assertThat(Where.toOracleSql("%AC")).isEqualTo("\\%AC");
    }

    @Test
    public void testLikeSqlHat() throws Exception {
        assertThat(Where.toOracleSql("^Z")).isEqualTo("^Z");
    }

    @Test
    public void testLikeSqlExclamation() throws Exception {
        assertThat(Where.toOracleSql("A!X")).isEqualTo("A!X");
    }

    @Test
    public void testLikeSqlBrackets() throws Exception {
        assertThat(Where.toOracleSql("A[X]Z")).isEqualTo("A[X]Z");
    }

    @Test
    public void testLikeSqlNonStartingAstrix() throws Exception {
        assertThat(Where.toOracleSql("A*")).isEqualTo("A%");
    }

    @Test
    public void testLikeSqlEverything() throws Exception {
        assertThat(Where.toOracleSql("*")).isEqualTo("%");
    }

    @Test
    public void testLikeSqlStartingAstrix() throws Exception {
        assertThat(Where.toOracleSql("*Z")).isEqualTo("%Z");
    }

    @Test
    public void testLikeSqlInterAstrix() throws Exception {
        assertThat(Where.toOracleSql("A*C")).isEqualTo("A%C");
    }

    @Test
    public void testLikeSqlEscapedAstrix() throws Exception {
        assertThat(Where.toOracleSql("ABC\\*XYZ")).isEqualTo("ABC*XYZ");
    }

    @Test
    public void testLikeSqlNonStartingQuestion() throws Exception {
        assertThat(Where.toOracleSql("A?")).isEqualTo("A_");
    }

    @Test
    public void testLikeSqlOne() throws Exception {
        assertThat(Where.toOracleSql("?")).isEqualTo("_");
    }

    @Test
    public void testLikeSqlStartingQuestion() throws Exception {
        assertThat(Where.toOracleSql("?Z")).isEqualTo("_Z");
    }

    @Test
    public void testLikeSqlInterQuestion() throws Exception {
        assertThat(Where.toOracleSql("A?C")).isEqualTo("A_C");
    }

    @Test
    public void testLikeSqlStartAstrixEndQuestion() throws Exception {
        assertThat(Where.toOracleSql("*A?")).isEqualTo("%A_");
    }

    @Test
    public void testLikeSqlInterAstrixAndQuestion() throws Exception {
        assertThat(Where.toOracleSql("Z*A?Z")).isEqualTo("Z%A_Z");
    }

    @Test
    public void testLikeSqlEscapedAstrixCombination() throws Exception {
        assertThat(Where.toOracleSql("Z\\**")).isEqualTo("Z*%");
    }

    @Test
    public void testLikeSqlEscapedQuestion() throws Exception {
        assertThat(Where.toOracleSql("ABC\\?XYZ")).isEqualTo("ABC?XYZ");
    }

    @Test
    public void testLikeSqlEscapedExclamationAstrixCombination() throws Exception {
        assertThat(Where.toOracleSql("!*")).isEqualTo("!%");
    }

    @Test
    public void testLikeSqlMultipleEscapedExclamation() throws Exception {
        assertThat(Where.toOracleSql("!!!")).isEqualTo("!!!");
    }

}