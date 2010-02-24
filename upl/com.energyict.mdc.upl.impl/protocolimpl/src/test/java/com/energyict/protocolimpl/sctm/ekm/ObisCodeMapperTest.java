package com.energyict.protocolimpl.sctm.ekm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.TimeZone;

import org.junit.Test;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.customerconfig.EDPRegisterConfig;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.siemens7ED62.SCTMDumpData;


public class ObisCodeMapperTest {

	@Test
	public final void getBillingPointTimestamp(){
		try {
			
			File file = new File(SCTMDumpData.class.getClassLoader().getResource("com/energyict/protocolimpl/siemens7ED62/dumpDataByteArray.bin").getFile());
			FileInputStream fis = new FileInputStream(file);
			byte[] content = new byte[(int) file.length()];
			fis.read(content);
			fis.close();
			
			SCTMDumpData dumpData = new SCTMDumpData(content,0);
			RegisterConfig regs = new EDPRegisterConfig();
			ObisCodeMapper ocm = new ObisCodeMapper(dumpData, TimeZone.getDefault(), regs, 1);
			
			ObisCode oc = ObisCode.fromString("255.1.255.8.1.VZ");
			RegisterValue rv = ocm.getRegisterValue(oc);
			assertEquals(new BigDecimal(new BigInteger("759210"), 2),rv.getQuantity().getAmount());
			assertEquals(Unit.get("kWh"),rv.getQuantity().getUnit());
			assertEquals(ProtocolUtils.parseDateTimeWithTimeZone("10-02-01 00:00", SCTMDumpData.ENERMET_DUMP_DATETIME_SIGNATURE, TimeZone.getDefault()), rv.getEventTime());
			

			oc = ObisCode.fromString("255.1.255.8.1.VZ-5");
			rv = ocm.getRegisterValue(oc);
			assertEquals(new BigDecimal(new BigInteger("724212"), 2),rv.getQuantity().getAmount());
			assertEquals(Unit.get("kWh"),rv.getQuantity().getUnit());
			assertEquals(ProtocolUtils.parseDateTimeWithTimeZone("09-09-01 00:00", SCTMDumpData.ENERMET_DUMP_DATETIME_SIGNATURE, TimeZone.getDefault()), rv.getEventTime());
			
			oc = ObisCode.fromString("255.1.255.6.1.VZ-5");
			rv = ocm.getRegisterValue(oc);
			assertEquals(new BigDecimal(new BigInteger("307"), 3),rv.getQuantity().getAmount());
			assertEquals(Unit.get("kW"),rv.getQuantity().getUnit());
			assertEquals(ProtocolUtils.parseDateTimeWithTimeZone("09-08-31 15:45", SCTMDumpData.ENERMET_DUMP_DATETIME_SIGNATURE, TimeZone.getDefault()), rv.getEventTime());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (ParseException e) {
			e.printStackTrace();
			fail();
		}
	}
}
