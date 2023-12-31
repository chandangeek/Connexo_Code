package com.energyict.protocolimpl.edf.trimaran2p.core;


import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P;
import com.energyict.protocolimpl.edf.trimarandlms.axdr.TrimaranDataContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TrimaranObjectFactoryTest {

    private Trimaran2P deuxP;
    private TrimaranObjectFactory tof;
    private EnergieIndexReader energieIndexReader = null;
    private EnergieIndex energieIndex = null;
    private Energies energies = null;

    @Mock
    private NlsService nlsService;
    @Mock
    private PropertySpecService propertySpecService;

    private Quantity expected = new Quantity(new BigDecimal(BigInteger.valueOf(328499700), 3), Unit.get("MWh"));

    @Before
    public void setUp() throws Exception {

        deuxP = new Trimaran2P(propertySpecService, nlsService);
        deuxP.init(null, null, TimeZone.getTimeZone("ECT"), null);
        tof = new TrimaranObjectFactory(deuxP);
    }

    @After
    public void tearDown() throws Exception {
    }

//	@Test
//	public void writeAccessPartielTest(){
//		try {
//			dialer = Utilities.getNewDialer();
//			tof.getTrimaran().doInit(dialer.getInputStream(), dialer.getOutputStream(), 0, 0, 0, 0, 0, null, dialer.getHalfDuplexController());
//			tof.getTrimaran().setDLMSPDUFactory(new DLMSPDUFactory(tof.getTrimaran()));
//			tof.getTrimaran().doValidateProperties(new Properties());
//			tof.getTrimaran().setAPSEFactory(new APSEPDUFactory(tof.getTrimaran(), tof.getTrimaran().getAPSEParameters()));
//			accessPartiel = new AccessPartiel(tof);
//			accessPartiel.setDateAccess(Calendar.getInstance().getTime());
//			accessPartiel.setNomAccess(1);
//			accessPartiel.write();
//		} catch (IOException e) {
//			e.printStackTrace();
//			fail();
//		} catch (LinkException e) {
//			e.printStackTrace();
//			fail();
//		}
//	}

    @Test
    public void readEnergyIndexTest() {
        File file;
        FileInputStream fis;
        TrimaranDataContainer dc = new TrimaranDataContainer();
        try {

            file = new File(this.getClass().getResource("/com/energyict/protocolimpl/edf/trimaran/EnergieIndexes.bin").getFile());
            fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            dc.parseObjectList(data, tof.getTrimaran().getLogger());

            energies = new Energies(dc, tof.getTrimaran().getTimeZone(), 56);
            energieIndexReader = new EnergieIndexReader(tof);
            energieIndexReader.setEnergie(energies);
            energieIndexReader.setVariableName(56);
            energieIndex = new EnergieIndex();
            energieIndex.addEnergie(energieIndexReader.getEnergie());

            if (energieIndex.energies.size() > 1) {
                fail();
            }
            Quantity testIxNRJact = energieIndex.getEnergie(56).getIxNRJact(0).add(energieIndex.getEnergie(56).getNRJact_Reste(0));
            assertEquals(expected.getAmount(), testIxNRJact.getAmount());

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

}
