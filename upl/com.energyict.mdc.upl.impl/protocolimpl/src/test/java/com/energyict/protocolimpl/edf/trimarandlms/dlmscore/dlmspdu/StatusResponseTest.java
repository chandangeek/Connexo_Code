package com.energyict.protocolimpl.edf.trimarandlms.dlmscore.dlmspdu;


import com.energyict.cbo.Utils;
import com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P;
import org.junit.*;

import java.io.*;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class StatusResponseTest {
	
	private Trimaran2P deuxP;
	private FileInputStream fis;
	private File file;
	private String[] firm = {"/com/energyict/protocolimpl/edf/trimaran/080307000201StatusResponce.bin",
            "/com/energyict/protocolimpl/edf/trimaran/080735000184StatusResponce.bin",
            "/com/energyict/protocolimpl/edf/trimaran/089807000857StatusResponce.bin"};
	private String[] model = {"TRIMARAN 2", "TRIMARAN 2 M 4Q", "TRIMARAN 2"};
	private String[] resources = {"TEC_V1", "TEC_V12     ", "TEP"};
	private String[] vendor = {"CHAUVIN ARNOUX  MATRA COMMUNICATION", "CHAUVIN ARNOUX  MATRA COMMUNICATION", "CHAUVIN ARNOUX  MATRA COMMUNICATION"};
	private String[] serialNumber = {"0803070002010002", "0807350001840002", "0898070008570002"};

	@Before
	public void setUp() throws Exception {
		deuxP = new Trimaran2P();
		deuxP.init(null, null, TimeZone.getTimeZone("ECT"), null);
	}

	@After
	public void tearDown() throws Exception {
		deuxP.release();
		fis.close();
	}
	
	@Test
	public void parsePDUTest(){
		
		try {
			StatusResponse sr = new StatusResponse(deuxP.getDLMSPDUFactory());
			for(int f = 0; f < firm.length; f++){
				file = new File(Utils.class.getResource(firm[f]).getFile());
				fis = new FileInputStream(file);
				byte[] data=new byte[(int)file.length()];
				fis.read(data);
				fis.close();
				
				sr.parsePDU(data);
				
				System.out.println("SerialNumber: " + sr.getSerialNumber());
				System.out.println("Status: " + sr.getStatus());
				System.out.println("VDEType: " + sr.getVDEType());
				for(int i = 0; i < sr.getStatusIdentifies().length; i++){
					System.out.println("StatusIdentiefier " + i + ": " + sr.getStatusIdentifies()[i]);
					System.out.println("	Model: " + sr.getStatusIdentifies()[i].getModel());
					System.out.println("	Resources: " + sr.getStatusIdentifies()[i].getResources());
					System.out.println("	VendorName: " + sr.getStatusIdentifies()[i].getVendorName());
					System.out.println("	VersionNr: " + sr.getStatusIdentifies()[i].getVersionNr());

					assertEquals(sr.getStatusIdentifies()[i].getModel(), model[f]);
					assertEquals(sr.getStatusIdentifies()[i].getResources(), resources[f]);
					assertEquals(sr.getStatusIdentifies()[i].getVendorName(), vendor[f]);
					assertEquals(sr.getSerialNumber(), serialNumber[f]);
					
				}
				
			}
			
		} catch (FileNotFoundException e) {
			fail();
			e.printStackTrace();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
		
	}

}
