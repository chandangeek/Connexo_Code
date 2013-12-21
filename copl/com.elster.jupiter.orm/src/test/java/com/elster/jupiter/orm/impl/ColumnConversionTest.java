package com.elster.jupiter.orm.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.fields.impl.ColumnConversionImpl;

@RunWith(MockitoJUnitRunner.class)
public class ColumnConversionTest {
	
	@Mock
	ResultSet rs;

	@Test
	public void test() {
		for (ColumnConversion each : ColumnConversion.values()) {
			ColumnConversionImpl.valueOf(each.name());
		}
	}
	
	@Test
	public void testDate2Date() {
		Date date = new Date(110);
		assertThat(ColumnConversionImpl.DATE2DATE.convert("110")).isEqualTo(date);
		assertThat(ColumnConversionImpl.DATE2DATE.convertToDb(date)).isEqualTo(new java.sql.Date(110));
		try {
			when(rs.getDate(anyInt())).thenReturn(new java.sql.Date(110));
			assertThat(ColumnConversionImpl.DATE2DATE.convertFromDb(rs,5)).isEqualTo(date);
		} catch (SQLException ex) {
			assertThat(true).isFalse();
		}
	}
	
	@Test
	public void testTimestamp2Date() {
		Date date = new Date(110);
		assertThat(ColumnConversionImpl.TIMESTAMP2DATE.convert("110")).isEqualTo(date);
		assertThat(ColumnConversionImpl.TIMESTAMP2DATE.convertToDb(date)).isEqualTo(new java.sql.Date(110));
		try {
			when(rs.getTimestamp(anyInt())).thenReturn(new java.sql.Timestamp(110));
			assertThat(ColumnConversionImpl.TIMESTAMP2DATE.convertFromDb(rs,5)).isEqualTo(date);
		} catch (SQLException ex) {
			assertThat(true).isFalse();
		}
	}

}
