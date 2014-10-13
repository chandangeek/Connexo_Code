package com.elster.jupiter.util.conditions;

import java.time.Instant;

import org.junit.*;

import com.elster.jupiter.util.time.*;

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
	
}
