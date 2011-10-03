package com.elster.protocolimpl.dsfg.connection;

import com.elster.protocolimpl.dsfg.telegram.DataBlock;
import com.elster.protocolimpl.dsfg.telegram.DataElement;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;


public class DataElementTest {

	private Long newDateLong() {
		return (new Date().getTime() / 1000) * 1000;
	}
	
	@Test
	public void testSimpleConstructor1() {
		Long d = newDateLong();
		DataElement de = new DataElement("aaaaa", 123456789l, d, 1234l, 1);
		assertEquals("aaaaa", de.getAddress());
		assertEquals(123456789l, de.getValue());
		assertEquals(d, de.getDateLong());
		assertEquals(1234l, de.getOno().longValue());
		assertEquals(1, de.getState().intValue());
	}

	@Test
	public void testSimpleConstructor2() {
		Long d = newDateLong();
		DataElement de = new DataElement("aaaaa", 12345.6789d, d, 1234l, 1);
		assertEquals("aaaaa", de.getAddress());
		assertEquals(12345.6789d, de.getValue());
		assertEquals(d, de.getDateLong());
		assertEquals(1234l, de.getOno().longValue());
		assertEquals(1, de.getState().intValue());
	}

	@Test
	public void testSimpleConstructor3() {
		Long d = newDateLong();
		DataElement de = new DataElement("aaaaa", "123456789", d, 1234l, 1);
		assertEquals("aaaaa", de.getAddress());
		assertEquals("123456789", de.getValue());
		assertEquals(d, de.getDateLong());
		assertEquals(1234l, de.getOno().longValue());
		assertEquals(1, de.getState().intValue());
	}
	
	@Test
	public void testSmallConstructor() {
		DataElement de = new DataElement();
		assertEquals("", de.getAddress());
		assertEquals(null, de.getValue());
		assertEquals(null, de.getDate());
		assertEquals(null, de.getOno());
		assertEquals(null, de.getState());
	}
	
	@Test
	public void testStringConstructor1() {
		String data = "aaaaa" + DataBlock.SUS + "123456789" + DataBlock.SUS + "4BFC8EB0" + DataBlock.SUS +
		              "331" + DataBlock.SUS + "0";
		DataElement de = new DataElement(data);
		assertEquals("aaaaa", de.getAddress());
		assertEquals(123456789l, de.getValue());
		Date d = new Date(0x4BFC8EB0l * 1000);
		assertEquals(d.getTime(), de.getDate().getTime());
		assertEquals(331l, de.getOno().longValue());
		assertEquals(0, de.getState().intValue());
	}
	
	@Test
	public void testStringConstructor5A() {
		String data = "aaaaa" + DataBlock.SUS + "123456789" + DataBlock.SUS + "4BFC8EB0" + DataBlock.SUS +
		              "331" + DataBlock.SUS + "";
		DataElement de = new DataElement(data);
		assertEquals("aaaaa", de.getAddress());
		assertEquals(123456789l, de.getValue());
		Date d = new Date(0x4BFC8EB0l * 1000);
		assertEquals(d.getTime(), de.getDate().getTime());
		assertEquals(331l, de.getOno().longValue());
		assertEquals(null, de.getState());
	}

	@Test
	public void testStringConstructor5B() {
		String data = "aaaaa" + DataBlock.SUS + "123456789" + DataBlock.SUS + "4BFC8EB0" + DataBlock.SUS +
		              "331";
		DataElement de = new DataElement(data);
		assertEquals("aaaaa", de.getAddress());
		assertEquals(123456789l, de.getValue());
		Date d = new Date(0x4BFC8EB0l * 1000);
		assertEquals(d.getTime(), de.getDate().getTime());
		assertEquals(331l, de.getOno().longValue());
		assertEquals(null, de.getState());
	}
	
	@Test
	public void testStringConstructor4A() {
		String data = "aaaaa" + DataBlock.SUS + "123456789" + DataBlock.SUS + "4BFC8EB0" + DataBlock.SUS +
		              DataBlock.SUS + "";
		DataElement de = new DataElement(data);
		assertEquals("aaaaa", de.getAddress());
		assertEquals(123456789l, de.getValue());
		Date d = new Date(0x4BFC8EB0l * 1000);
		assertEquals(d.getTime(), de.getDate().getTime());
		assertEquals(null, de.getOno());
		assertEquals(null, de.getState());
	}

	@Test
	public void testStringConstructor4B() {
		String data = "aaaaa" + DataBlock.SUS + "123456789" + DataBlock.SUS + "4BFC8EB0" + DataBlock.SUS;
		DataElement de = new DataElement(data);
		assertEquals("aaaaa", de.getAddress());
		assertEquals(123456789l, de.getValue());
		Date d = new Date(0x4BFC8EB0l * 1000);
		assertEquals(d.getTime(), de.getDate().getTime());
		assertEquals(null, de.getOno());
		assertEquals(null, de.getState());
	}
	@Test
	public void testStringConstructor3A() {
		String data = "aaaaa" + DataBlock.SUS + "123456789" + DataBlock.SUS + DataBlock.SUS + DataBlock.SUS;
		DataElement de = new DataElement(data);
		assertEquals("aaaaa", de.getAddress());
		assertEquals(123456789l, de.getValue());
		assertEquals(null, de.getDate());
		assertEquals(null, de.getOno());
		assertEquals(null, de.getState());
	}

	@Test
	public void testStringConstructor3B() {
		String data = "aaaaa" + DataBlock.SUS + "123456789";
		DataElement de = new DataElement(data);
		assertEquals("aaaaa", de.getAddress());
		assertEquals(123456789l, de.getValue());
		assertEquals(null, de.getDate());
		assertEquals(null, de.getOno());
		assertEquals(null, de.getState());
	}
	
	@Test
	public void testStringConstructor2A() {
		String data = "aaaaa" + DataBlock.SUS + DataBlock.SUS + DataBlock.SUS + DataBlock.SUS;
		DataElement de = new DataElement(data);
		assertEquals("aaaaa", de.getAddress());
		assertEquals(null, de.getValue());
		assertEquals(null, de.getDate());
		assertEquals(null, de.getOno());
		assertEquals(null, de.getState());
	}

	@Test
	public void testStringConstructor2B() {
		String data = "aaaaa";
		DataElement de = new DataElement(data);
		assertEquals("aaaaa", de.getAddress());
		assertEquals(null, de.getValue());
		assertEquals(null, de.getDate());
		assertEquals(null, de.getOno());
		assertEquals(null, de.getState());
	}
	
	@Test
	public void testToString1() {
		String data = "aaaaa" + DataBlock.SUS + "123456789" + DataBlock.SUS + "4BFC8EB0" + DataBlock.SUS +
        "331" + DataBlock.SUS + "0";
		DataElement de = new DataElement(data);
		String result = de.toString();
		assertEquals(data, result);
	}
	
	@Test
	public void testToString2() {
		String data = "aaaaa";
		DataElement de = new DataElement(data);
		String result = de.toString();
		assertEquals(data, result);
	}
	
	@Test
	public void testToString3() {
		String data = "aaaaa" + DataBlock.SUS + DataBlock.SUS + DataBlock.SUS + DataBlock.SUS + "1";
		DataElement de = new DataElement(data);
		String result = de.toString();
		assertEquals(data, result);
	}
	
}