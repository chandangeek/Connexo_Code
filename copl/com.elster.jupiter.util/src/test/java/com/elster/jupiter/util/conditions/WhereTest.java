package com.elster.jupiter.util.conditions;

import java.util.Date;

import org.junit.*;

import com.elster.jupiter.util.time.*;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;

public class WhereTest {

	@Test
	public void testInfiniteIn() {
		Interval interval = new Interval(null,null);
		assertThat(where("test").inOpen(interval)).isEqualTo(Condition.TRUE);
		assertThat(where("test").inClosed(interval)).isEqualTo(Condition.TRUE);
		assertThat(where("test").inOpenClosed(interval)).isEqualTo(Condition.TRUE);
		assertThat(where("test").inClosedOpen(interval)).isEqualTo(Condition.TRUE);
	}
	
	@Test
	public void testFiniteIn() {
		Interval interval = new Interval(new Date(0),new Date());
		assertThat(where("test").inOpen(interval).toString()).matches(".*test.*>[ ?].*AND.*test.*<[ ?].*");
		assertThat(where("test").inOpenClosed(interval).toString()).matches(".*test.*>[ ?].*AND.*test.*<=[ ?].*");
		assertThat(where("test").inClosedOpen(interval).toString()).matches(".*test.*>=[ ?].*AND.*test.*<[ ?].*");
		assertThat(where("test").inClosed(interval).toString()).matches(".*test.*>=[ ?].*AND.*test.*<=[ ?].*");
	}
	
	@Test
	public void testCurrentAt() {
		Date date = new Date();
		assertThat(where("test").isCurrentAt(date).toString()).matches(".*test\\.start\\s*>=[ ?].*AND.*test\\.stop.\\s*<[ ?].*");
	}
	
}
