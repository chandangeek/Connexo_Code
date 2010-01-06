/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaranplus.core;


import static org.junit.Assert.assertArrayEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
/**
 * @author gna
 * @since 6-jan-2010
 *
 */
public class CourbeChargeTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void constructDecenniumTable(){
		CourbeCharge cc = new CourbeCharge(null);
		
		long currentTimeInMillis = Long.valueOf("915611294000");	// somewhere in 1999
		cc.setCurrentTime(currentTimeInMillis);
		cc.constructDecenniumTable();
		assertArrayEquals(new int[]{1990, 1991, 1992, 1993, 1994, 1995, 1996, 1997, 1998, 1999}, cc.getDecenniumYearTable());
		
		currentTimeInMillis = Long.valueOf("1262766495000");	// somewhere in 2010
		cc.setCurrentTime(currentTimeInMillis);
		cc.constructDecenniumTable();
		assertArrayEquals(new int[]{2010, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009}, cc.getDecenniumYearTable());
		
		currentTimeInMillis = Long.valueOf("1231230071000");	// somewhere in 2009
		cc.setCurrentTime(currentTimeInMillis);
		cc.constructDecenniumTable();
		assertArrayEquals(new int[]{2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009}, cc.getDecenniumYearTable());
		
		currentTimeInMillis = Long.valueOf("1010305694000");	// somewhere in 2002
		cc.setCurrentTime(currentTimeInMillis);
		cc.constructDecenniumTable();
		assertArrayEquals(new int[]{2000, 2001, 2002, 1993, 1994, 1995, 1996, 1997, 1998, 1999}, cc.getDecenniumYearTable());
		
		currentTimeInMillis = Long.valueOf("1578299294000");	// somewhere in 2020
		cc.setCurrentTime(currentTimeInMillis);
		cc.constructDecenniumTable();
		assertArrayEquals(new int[]{2020, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019}, cc.getDecenniumYearTable());
		
		currentTimeInMillis = Long.valueOf("1736152094000");	// somewhere in 2025
		cc.setCurrentTime(currentTimeInMillis);
		cc.constructDecenniumTable();
		assertArrayEquals(new int[]{2020, 2021, 2022, 2023, 2024, 2025, 2016, 2017, 2018, 2019}, cc.getDecenniumYearTable());
		
		currentTimeInMillis = Long.valueOf("4891825694000");	// somewhere in 2125
		cc.setCurrentTime(currentTimeInMillis);
		cc.constructDecenniumTable();
		assertArrayEquals(new int[]{2120, 2121, 2122, 2123, 2124, 2125, 2116, 2117, 2118, 2119}, cc.getDecenniumYearTable());
	}
	

}
