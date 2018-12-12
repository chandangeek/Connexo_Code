package com.energyict.protocolimpl.sctm.ekm;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.customerconfig.EDPRegisterConfig;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.siemens7ED62.SCTMDumpData;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class ObisCodeMapperTest {

	@Test
	public final void getBillingPointTimestamp(){
		try {
			
			File file = new File(SCTMDumpData.class.getClassLoader().getResource("com/energyict/protocolimpl/siemens7ED62/dumpDataByteArray.bin").getFile());
			File file2 = new File(SCTMDumpData.class.getClassLoader().getResource("com/energyict/protocolimpl/siemens7ED62/dumpDataByteArray3.bin").getFile());

			FileInputStream fis = new FileInputStream(file);
			byte[] content = new byte[(int) file.length()];
			fis.read(content);
			fis.close();
			
            FileInputStream fis2 = new FileInputStream(file2);
			byte[] content2 = new byte[(int) file2.length()];
			fis2.read(content2);
			fis2.close();

			SCTMDumpData dumpData = new SCTMDumpData(content,0);
			RegisterConfig regs = new EDPRegisterConfig();
			ObisCodeMapper ocm = new ObisCodeMapper(dumpData, TimeZone.getDefault(), regs, 1);
            ocm.setBillingTimeStampId("40*");

			SCTMDumpData dumpData2 = new SCTMDumpData(content2,0);
			RegisterConfig regs2 = new EDPRegisterConfig();
			ObisCodeMapper ocm2 = new ObisCodeMapper(dumpData2, TimeZone.getDefault(), regs2, 1);
            ocm2.setBillingTimeStampId("10");

            ObisCode oc = ObisCode.fromString("255.1.255.255.48.VZ");
            RegisterValue rv = ocm2.getRegisterValue(oc);
            assertEquals(7, rv.getQuantity().getAmount().intValue());
            assertEquals(Unit.get("kvarh"), rv.getQuantity().getUnit());
            assertEquals(ProtocolUtils.parseDateTimeWithTimeZone("11-11-01 00:00", SCTMDumpData.ENERMET_DUMP_DATETIME_SIGNATURE, TimeZone.getDefault()), rv.getEventTime());

            oc = ObisCode.fromString("255.1.255.8.1.VZ");
			rv = ocm.getRegisterValue(oc);
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
