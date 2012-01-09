/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.commands;


import org.junit.*;

import java.io.IOException;

import static org.junit.Assert.fail;

/**
 * @author gna
 * @since 24-mrt-2010
 *
 */
public class ReadCommandTest {

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
	public final void checkResponseForErrorsTest(){
		String errorString = "(#0018)";
		AbstractCommand rc = new ReadCommand(null);
		try {
			rc.checkResponseForErrors(errorString);
		} catch (IOException e) {
			if(e.getMessage().indexOf("Error received during read : ") == -1){
				fail();
			}
		}
	}
}
