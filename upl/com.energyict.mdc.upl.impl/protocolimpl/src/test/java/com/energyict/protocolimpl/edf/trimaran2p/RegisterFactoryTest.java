/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p;


import com.energyict.cbo.Utils;
import com.energyict.protocolimpl.edf.trimaran2p.core.TrimaranObjectFactory;
import com.energyict.protocolimpl.edf.trimarandlms.common.Register;
import org.junit.*;

import java.io.*;
import java.util.ArrayList;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author gna
 * FIXME: You need to rebuild the resources with the fixed SerialVersionUID of the Register
 */
public class RegisterFactoryTest {
	
	private Trimaran2P deuxP;
	private TrimaranObjectFactory tof;
	private RegisterFactory rf;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		deuxP = new Trimaran2P();
		deuxP.init(null, null, TimeZone.getTimeZone("ECT"), null);
		tof = new TrimaranObjectFactory(deuxP);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

    /**
     * Test is ignored in 8.11 for serialization reasons.
     * Resources should be rewritten with the current versions
     */
    @Ignore
	@Test
	public void registerFactoryTest(){
		
		File file;
		FileInputStream fis;
		ObjectInputStream ois;
		ArrayList buildedRegisters = new ArrayList();
		ArrayList actualRegisters = new ArrayList();
		deuxP.setMeterVersion("TEC");
		try {
			rf = new RegisterFactory(deuxP);
			
			file = new File(Utils.class.getResource("/com/energyict/protocolimpl/edf/trimaran/BuildedRegisters.bin").getFile());
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			buildedRegisters = (ArrayList)ois.readObject();
			ois.close();
			fis.close();
			
			actualRegisters = (ArrayList) rf.getRegisters();
			
			assertEquals(buildedRegisters.size(), actualRegisters.size());
			
			for(int i = 0; i < buildedRegisters.size(); i++){
				assertEquals(((Register)buildedRegisters.get(i)).getObisCode(), ((Register)actualRegisters.get(i)).getObisCode());
				assertEquals(((Register)buildedRegisters.get(i)).getVariableName().getCode(), ((Register)actualRegisters.get(i)).getVariableName().getCode());
				assertEquals(((Register)buildedRegisters.get(i)).getDescription(), ((Register)actualRegisters.get(i)).getDescription());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail();
		}
	}

}
