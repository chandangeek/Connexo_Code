package com.elster.protocolimpl.dlms.profile.entrymgmt;

import com.elster.dlms.cosem.classes.class03.ScalerUnit;
import com.elster.dlms.cosem.classes.class03.Unit;
import com.elster.dlms.cosem.classes.class07.CaptureObjectDefinition;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;
import com.elster.dlms.types.basic.DlmsDate;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.basic.DlmsTime;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataDoubleLongUnsigned;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataUnsigned;
import com.elster.protocolimpl.dlms.profile.api.IArchiveLineChecker;
import com.elster.protocolimpl.dlms.profile.standardchecker.EvtDlmsChecker;
import com.elster.protocolimpl.dlms.profile.standardchecker.EvtUmi1Checker;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;

/**
 * User: heuckeg
 * Date: 16.04.13
 * Time: 09:38
 */
public class TestArchiveStructure2
{
    private final static String ARCHIVESTRUCTURE = "TST=0.0.1.0.0.255" + "," +
            "CHN0[C9]=7.0.13.83.0.255" + "," +
            "CHN1[C9]=7.0.12.81.0.255" + "," +
            "EVT_DLMS=0.2.96.10.1.255";

    private final static CaptureObjectDefinition[] a1profile60minDefinitions = {
            new CaptureObjectDefinition(8, new ObisCode(0,0,1,0,0,255), 2, 0),
            new CaptureObjectDefinition(1, new ObisCode(0,128,96,8,67,255), 2, 0),
            new CaptureObjectDefinition(3, new ObisCode(7,0,13,83,0,255), 2, 0),
            new CaptureObjectDefinition(3, new ObisCode(7,0,12,81,0,255), 2, 0),
            new CaptureObjectDefinition(3, new ObisCode(0,2,96,10,1,255), 2, 0),
    };

    private Locale savedLocale;

    @Before
    public void setup() {
        savedLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @After
    public void tearDown() {
        if (!Locale.getDefault().equals(savedLocale)) {
            Locale.setDefault(savedLocale);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        TimeZone.setDefault(null);  // Reset the default timezone to System timezone, cause else we stay in timezone GMT.
    }

    @SuppressWarnings({"deprecation"})
    @Test
    public void a1IntervalProfileTest() throws IOException
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        HashMap<String, IArchiveLineChecker> checkerList = new HashMap<String, IArchiveLineChecker>();
        checkerList.put("EVT_DLMS", new EvtDlmsChecker());

        TArchiveStructure2 profile60 = new TArchiveStructure2(ARCHIVESTRUCTURE, checkerList);
        assertEquals(4, profile60.getEntries().size());

        profile60.setCapturedObjectDefinitions(a1profile60minDefinitions);
        profile60.setScalerUnitArray(new ScalerUnit[] {null, null, new ScalerUnit(-2, Unit.CUBIC_METRE_CORRECTED_VOLUME), new ScalerUnit(-1, Unit.CUBIC_METRE_CORRECTED_VOLUME)});

        profile60.prepareForProcessing(null);

        List<ChannelInfo> channelInfo = profile60.buildChannelInfo();
        String s1 = "";
        for (ChannelInfo ci: channelInfo)
        {
            if (s1.length() > 0)
            {
                s1 += "\n";
            }
            s1 += String.format("%d: %s [%s] %s %g", ci.getChannelId(), ci.getName(), ci.getUnit().toString(), ci.isCumulative(), ci.getCumulativeWrapValue());
        }
        String s4 = "0: Channel 0 [Nm3] true 1.00000e+09" + "\n" +
                    "1: Channel 1 [Nm3] true 1.00000e+09";
        System.out.println(s1);
        assertEquals(s4, s1);

        // Beispielarchivzeile erzeugen
        DlmsDateTime ddt = new DlmsDateTime(new DlmsDate(2013, 4, 1), new DlmsTime(14, 0, 0, 0xFF), 0, 0);
        //DlmsData d0 = new DlmsDataOctetString(ddt.toBytes());
        DlmsData d1 = new DlmsDataDoubleLongUnsigned(1234567L);
        DlmsData d2 = new DlmsDataDoubleLongUnsigned(301L);
        DlmsData d3 = new DlmsDataDoubleLongUnsigned(0L);
        DlmsData d4 = new DlmsDataUnsigned(0x10);
        Object[] data = new Object[] {ddt, d1, d2, d3, d4};

        // zerlegen...
        IntervalData ivd = profile60.processProfileLine(data, TimeZone.getTimeZone("GMT+2"));
        String s2 = ivd.toString();
        String s5 = "Mon Apr 01 14:00:00 GMT 2013 0 2048 Values: 3.01 0 20480.0 0 2048";
        System.out.println(s2);
        assertEquals(s5, s2);
    }

    private final static String LOGSTRUCTURE = "TST=0.0.1.0.0.255" + "," +
            "EVT_UMI1=0.0.96.15.6.255";

    private final static CaptureObjectDefinition[] a1LogDefinitions = {
            new CaptureObjectDefinition(8, new ObisCode(0,0,1,0,0,255), 2, 0),
            new CaptureObjectDefinition(4, new ObisCode(0,0,96,15,6,255), 2, 0),
            new CaptureObjectDefinition(4, new ObisCode(0,0,96,15,6,255), 4, 0)
    };

    @Test
    public void a1LogProfileTest() throws IOException
    {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        HashMap<String, IArchiveLineChecker> checkerList = new HashMap<String, IArchiveLineChecker>();
        checkerList.put("EVT_UMI1", new EvtUmi1Checker());

        TArchiveStructure2 profile = new TArchiveStructure2(LOGSTRUCTURE, checkerList);
        assertEquals(2, profile.getEntries().size());

        profile.setCapturedObjectDefinitions(a1LogDefinitions);
        profile.prepareForProcessing(null);

        // Beispielarchivzeile erzeugen
        DlmsDateTime ddt = new DlmsDateTime(new DlmsDate(2013, 4, 1), new DlmsTime(14, 0, 0, 0xFF), 0, 0);
        DlmsData d0 = new DlmsDataOctetString(ddt.toBytes());
        DlmsData d1 = new DlmsDataUnsigned(0);
        DlmsData d2 = new DlmsDataOctetString(new byte[] {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte)0x80});

        Object[] data = new Object[] {d0, d1, d2};

        // zerlegen...
        MeterEvent me = profile.processLogLine(data, TimeZone.getTimeZone("GMT+2"));
        String s2 = me.toString();
        String s5 = "Verification Object Write ( $01 $02 $04 $08 $10 $20 $40 $80)";
        System.out.println(s2);
        assertEquals(s5, s2);
    }

    static class TArchiveStructure2 extends ArchiveStructure2
    {
        private CaptureObjectDefinition[] cod;
        private ScalerUnit[] sua;

        public TArchiveStructure2(final String structureString, HashMap<String, IArchiveLineChecker> checkerList) throws IOException
        {
            super(structureString, checkerList);
        }

        public void setCapturedObjectDefinitions(CaptureObjectDefinition[] cod)
        {
            this.cod = cod;
        }

        public void setScalerUnitArray(ScalerUnit[] sua)
        {
            this.sua = sua;
        }

        protected CapturedObjects getCapturedObjects(SimpleProfileObject profileObject) throws IOException
        {
            return new CapturedObjects(cod);
        }

        protected ScalerUnit getScalerUnitFromRelatedObject(SimpleProfileObject profileObject, int index) throws IOException
        {
            return sua[index];
        }
    }
}
