/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p;


import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.energyict.cbo.Utils;
import com.energyict.protocolimpl.edf.trimaran2p.core.TrimaranObjectFactory;
import com.energyict.protocolimpl.edf.trimarandlms.common.Register;

/**
 * @author gna
 *
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
	
	@Test
	public void registerFactoryTest(){
		
		File file;
		FileInputStream fis;
		ObjectInputStream ois;
		ArrayList buildedRegisters = new ArrayList();
		ArrayList actualRegisters = new ArrayList();
		try {
			rf = new RegisterFactory(deuxP);
			
			file = new File(Utils.class.getResource("/offlineFiles/BuildedRegisters.bin").getFile());
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
