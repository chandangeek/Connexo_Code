package com.elster.jupiter.orm.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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
		Instant instant = Instant.ofEpochMilli(110);
		assertThat(ColumnConversionImpl.DATE2INSTANT.convert("110")).isEqualTo(instant);
		assertThat(ColumnConversionImpl.DATE2INSTANT.convertToDb(instant)).isEqualTo(new java.sql.Date(110));
		try {
			when(rs.getDate(anyInt())).thenReturn(new java.sql.Date(110));
			assertThat(ColumnConversionImpl.DATE2INSTANT.convertFromDb(rs,5)).isEqualTo(instant);
		} catch (SQLException ex) {
			assertThat(true).isFalse();
		}
	}
	
	@Test
	public void testTimestamp2Date() {
		Instant instant = Instant.ofEpochMilli(110);
		assertThat(ColumnConversionImpl.TIMESTAMP2INSTANT.convert("110")).isEqualTo(instant);
		assertThat(ColumnConversionImpl.TIMESTAMP2INSTANT.convertToDb(instant)).isEqualTo(new java.sql.Timestamp(110));
		try {
			when(rs.getTimestamp(anyInt())).thenReturn(new java.sql.Timestamp(110));
			assertThat(ColumnConversionImpl.TIMESTAMP2INSTANT.convertFromDb(rs,5)).isEqualTo(instant);
		} catch (SQLException ex) {
			assertThat(true).isFalse();
		}
	}
	
	@Test
	public void testInstant() {
		Instant instant = Instant.ofEpochMilli(123456789L);
		assertThat(ColumnConversionImpl.NUMBER2INSTANT.convert("123456789")).isEqualTo(instant);
		assertThat(ColumnConversionImpl.NUMBER2INSTANT.convertToDb(instant)).isEqualTo(123456789L);
		try {
			when(rs.getLong(anyInt())).thenReturn(123456789L);
			assertThat(ColumnConversionImpl.NUMBER2INSTANT.convertFromDb(rs,5)).isEqualTo(instant);
		} catch (SQLException ex) {
			assertThat(true).isFalse();
		}
	}

	@Test
	public void testChar2Path() {
		Path path = Paths.get("/a/b/c/d");
		String convertedPath = path.toString();
		assertThat(ColumnConversionImpl.CHAR2PATH.convert(convertedPath)).isEqualTo(path);
		assertThat(ColumnConversionImpl.CHAR2PATH.convertToDb(path)).isEqualTo(convertedPath);
		try {
			when(rs.getString(anyInt())).thenReturn(convertedPath);
			assertThat(ColumnConversionImpl.CHAR2PATH.convertFromDb(rs,5)).isEqualTo(path);
		} catch (SQLException ex) {
			assertThat(true).isFalse();
		}
	}

}
