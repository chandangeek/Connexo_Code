/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.edf.trimaran2p.ObisCodeMapper;
import com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P;
import com.energyict.protocolimpl.edf.trimarandlms.axdr.DataContainer;

/**
 * @author gna
 *
 */
public class EnergiesTest {
	private Trimaran2P deuxP;
	private ObisCodeMapper ocm;
	private ObisCode oc1 = ObisCode.fromString("1.1.142.8.0.255");
	private ObisCode oc2 = ObisCode.fromString("1.1.143.8.0.255");
	private ObisCode oc3 = ObisCode.fromString("1.1.144.8.0.255");
	private ObisCode oc4 = ObisCode.fromString("1.1.145.8.0.255");
	private Quantity quan1 = new Quantity(new BigDecimal(new BigInteger("515691"), 3), Unit.get("Mvarh"));
	private Quantity quan2 = new Quantity(new BigDecimal(new BigInteger("176"), 3), Unit.get("Mvarh"));
	private Quantity quan3 = new Quantity(new BigDecimal(new BigInteger("231473"), 3), Unit.get("Mvarh"));
	private Quantity quan4 = new Quantity(new BigDecimal(new BigInteger("0"), 3), Unit.get("Mvarh"));
	private RegisterValue r1 = new RegisterValue(oc1, quan1, null, null);
	private RegisterValue r2 = new RegisterValue(oc2, quan2, null, null);
	private RegisterValue r3 = new RegisterValue(oc3, quan3, null, null);
	private RegisterValue r4 = new RegisterValue(oc4, quan4, null, null);
	
	FileInputStream fis;
	ObjectInputStream ois;
	File file;
	DataContainer dc;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		deuxP = new Trimaran2P();
		deuxP.init(null, null, TimeZone.getTimeZone("ECT"), null);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		deuxP.release();
		ois.close();
		fis.close();
	}

	@Test
	public void energiesConstructorTest(){
		
		ocm = new ObisCodeMapper(deuxP);
		deuxP.setMeterVersion("TEC");
		
		try {
			file = new File(EnergiesTest.class.getResource("/offlineFiles/trimaran/deuxp184/Energies184.bin").getFile());
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			
			dc = (DataContainer)ois.readObject();
			
			Energies energies = new Energies(dc, TimeZone.getTimeZone("ECT"), 56);
			
			
			ocm.setEnergie(energies);
			RegisterValue rv1 = ocm.getRegisterValue(oc1);
			RegisterValue rv2 = ocm.getRegisterValue(oc2);
			RegisterValue rv3 = ocm.getRegisterValue(oc3);
			RegisterValue rv4 = ocm.getRegisterValue(oc4);
			assertEquals(r1.getQuantity().getAmount(),rv1.getQuantity().getAmount());
			assertEquals(r2.getQuantity().getAmount(),rv2.getQuantity().getAmount());
			assertEquals(r3.getQuantity().getAmount(),rv3.getQuantity().getAmount());
			assertEquals(r4.getQuantity().getAmount(),rv4.getQuantity().getAmount());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		
		
	}
	
}
