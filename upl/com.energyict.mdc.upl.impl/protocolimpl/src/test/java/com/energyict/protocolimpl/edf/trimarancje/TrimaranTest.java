package com.energyict.protocolimpl.edf.trimarancje;

import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocolimpl.edf.trimarancje.core.DataFactory;
import com.energyict.protocolimpl.edf.trimarancje.core.DemandData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class TrimaranTest {

    private Trimaran cje;
    private String trimaranProfile = "/com/energyict/protocolimpl/edf/trimaran/TrimaranProfile.bin";
    private String[] profile = {"/com/energyict/protocolimpl/edf/trimaran/cje/Profile1.bin", "/com/energyict/protocolimpl/edf/trimaran/cje/Profile2.bin",
            "/com/energyict/protocolimpl/edf/trimaran/cje/Profile3.bin", "/com/energyict/protocolimpl/edf/trimaran/cje/Profile4.bin",
            "/com/energyict/protocolimpl/edf/trimaran/cje/Profile5.bin", "/com/energyict/protocolimpl/edf/trimaran/cje/Profile6.bin",
            "/com/energyict/protocolimpl/edf/trimaran/cje/Profile7.bin", "/com/energyict/protocolimpl/edf/trimaran/cje/Profile8.bin",
            "/com/energyict/protocolimpl/edf/trimaran/cje/Profile9.bin", "/com/energyict/protocolimpl/edf/trimaran/cje/Profile10.bin",
            "/com/energyict/protocolimpl/edf/trimaran/cje/Profile11.bin", "/com/energyict/protocolimpl/edf/trimaran/cje/Profile12.bin",
            "/com/energyict/protocolimpl/edf/trimaran/cje/Profile13.bin", "/com/energyict/protocolimpl/edf/trimaran/cje/Profile14.bin",
            "/com/energyict/protocolimpl/edf/trimaran/cje/Profile15.bin", "/com/energyict/protocolimpl/edf/trimaran/cje/Profile16.bin",};
    private String test = "/com/energyict/protocolimpl/edf/trimaran/Profile1.bin";

    @Before
    public void setUp() throws Exception {
        cje = new Trimaran(mock(PropertySpecService.class), mock(NlsService.class));
        cje.init(null, null, TimeZone.getTimeZone("ECT"), null);
    }

    @After
    public void tearDown() throws Exception {
        cje.release();
    }

    @Test
    public void profileTest() {
        try {
            cje.setTrimaranProfile(new TrimaranProfile(cje));
            cje.setDataFactory(new DataFactory(cje));
            cje.getTrimaranProfile().setDemandData(new DemandData(cje.getDataFactory()));
            File file = new File(TrimaranTest.class.getResource(trimaranProfile).getFile());
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            cje.getTrimaranProfile().getDemandData().parse(data);

            //TODO you should check the values!!

//	        cje.getTrimaranProfile().incrementPointer();
//
//	        /* have to check two profiles now */
//	        cje.getTrimaranProfile().setDemandData(new DemandData(cje.getDataFactory()));
//	        cje.getTrimaranProfile().getDemandData().parse(data);
//
//			cje.getProfileData(null, null, false);
//			// if we can get this far it should be OK ...
        } catch (UnsupportedException e) {
            fail();
            e.printStackTrace();
        } catch (IOException e) {
            fail();
            e.printStackTrace();
        }
    }

    @Test
    /**
     *  this test has to check the V2 of the Trimaran meters
     */
    public void profileTest2() {
        cje.setTrimaranProfile(new TrimaranProfile(cje));
        cje.setDataFactory(new DataFactory(cje));
        cje.setMeterVersion("V2");
        try {
            for (int i = 0; i < profile.length; i++) {
                cje.getTrimaranProfile().setDemandData(new DemandData(cje.getDataFactory()));
                File file = new File(TrimaranTest.class.getResource(profile[i]).getFile());
                FileInputStream fis;
                fis = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                fis.close();
                cje.getTrimaranProfile().getDemandData().parse(data);
                cje.getTrimaranProfile().incrementPointer();

            }
            assertEquals(cje.getTrimaranProfile().getPointer(), 16);
            cje.getTrimaranProfile().setPointer(1);
            ChannelInfo ci = cje.getTrimaranProfile().getProfileData().getChannelInfos().get(0);
            assertEquals(ci.getUnit(), Unit.get("kW"));
        } catch (FileNotFoundException e) {
            fail();
            e.printStackTrace();
        } catch (IOException e) {
            fail();
            e.printStackTrace();
        }
    }

}
