package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.fields.impl.ColumnConversionImpl;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ColumnConversionTest {
	
	@Mock
	private ResultSet rs;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private ColumnImpl column;

	@Test
	public void test() {
		for (ColumnConversion each : ColumnConversion.values()) {
			ColumnConversionImpl.valueOf(each.name());
		}
	}
	
	@Test
	public void testDate2Date() {
		Instant instant = Instant.ofEpochMilli(110);
		assertThat(ColumnConversionImpl.DATE2INSTANT.convert(column, "110")).isEqualTo(instant);
		assertThat(ColumnConversionImpl.DATE2INSTANT.convertToDb(column, instant)).isEqualTo(new java.sql.Date(110));
		try {
			when(rs.getDate(anyInt())).thenReturn(new java.sql.Date(110));
			assertThat(ColumnConversionImpl.DATE2INSTANT.convertFromDb(column, rs, 5)).isEqualTo(instant);
		} catch (SQLException ex) {
			assertThat(true).isFalse();
		}
	}
	
	@Test
	public void testTimestamp2Date() {
		Instant instant = Instant.ofEpochMilli(110);
		assertThat(ColumnConversionImpl.TIMESTAMP2INSTANT.convert(column, "110")).isEqualTo(instant);
		assertThat(ColumnConversionImpl.TIMESTAMP2INSTANT.convertToDb(column, instant)).isEqualTo(new java.sql.Timestamp(110));
		try {
			when(rs.getTimestamp(anyInt())).thenReturn(new java.sql.Timestamp(110));
			assertThat(ColumnConversionImpl.TIMESTAMP2INSTANT.convertFromDb(column, rs, 5)).isEqualTo(instant);
		} catch (SQLException ex) {
			assertThat(true).isFalse();
		}
	}
	
	@Test
	public void testInstant() {
		Instant instant = Instant.ofEpochMilli(123456789L);
		assertThat(ColumnConversionImpl.NUMBER2INSTANT.convert(column, "123456789")).isEqualTo(instant);
		assertThat(ColumnConversionImpl.NUMBER2INSTANT.convertToDb(column, instant)).isEqualTo(123456789L);
		try {
			when(rs.getLong(anyInt())).thenReturn(123456789L);
			assertThat(ColumnConversionImpl.NUMBER2INSTANT.convertFromDb(column, rs, 5)).isEqualTo(instant);
		} catch (SQLException ex) {
			assertThat(true).isFalse();
		}
	}

	@Test
	public void testChar2Path() {
        FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
        when(column.getTable().getDataModel().getFileSystem()).thenReturn(fileSystem);

        Path path = fileSystem.getPath("/a/b/c/d");
		String convertedPath = path.toString();
		assertThat(ColumnConversionImpl.CHAR2PATH.convert(column, convertedPath)).isEqualTo(path);
		assertThat(ColumnConversionImpl.CHAR2PATH.convertToDb(column, path)).isEqualTo(convertedPath);
		try {
			when(rs.getString(anyInt())).thenReturn(convertedPath);
			assertThat(ColumnConversionImpl.CHAR2PATH.convertFromDb(column, rs, 5)).isEqualTo(path);
		} catch (SQLException ex) {
			assertThat(true).isFalse();
		}
	}

}
