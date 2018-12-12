package com.energyict.protocolimpl.itron.vectron.basepages;

import com.energyict.protocolimpl.itron.protocol.ProtocolLink;
import com.energyict.protocolimpl.itron.vectron.Vectron;
import com.energyict.protocolimpl.itron.vectron.VectronProfile;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MassMemoryRecordBasePageTest {

    Logger logger = Logger.getLogger(this.getClass().getName());

    TimeZone timeZone = TimeZone.getTimeZone("PST8PDT");

    @Mock
    Vectron vectron;

    @Mock
    BasePagesFactory basePagesFactory = new BasePagesFactory(vectron);

    @Mock
    RealTimeBasePage    realTimeBasePage = new RealTimeBasePage(basePagesFactory);

    @Mock
    private ProtocolLink protocolLink;

    @Mock
    OperatingSetUpBasePage  operatingSetUpBasePage = new OperatingSetUpBasePage(basePagesFactory);

    @Mock
    private com.energyict.protocolimpl.itron.vectron.basepages.MassMemoryBasePages massMemoryBasePages;

    public void testParse0() throws Exception {
        MassMemoryBasePages pages = new MassMemoryBasePages(basePagesFactory);
        Calendar    cal = Calendar.getInstance(timeZone);
        when(realTimeBasePage.getCalendar()).thenReturn(cal);

        pages.setCurrentRecordNumber(1);

        when(basePagesFactory.getRealTimeBasePage()).thenReturn(realTimeBasePage);
        when(basePagesFactory.getMassMemoryBasePages()).thenReturn(pages);


        when(vectron.getTime()).thenReturn(new Date());
        when(vectron.getProfileInterval()).thenReturn(300);

        when(vectron.getBasePagesFactory()).thenReturn(basePagesFactory);

        pages.setNrOfChannels(2);
        pages.setProfileInterval(300);

        List massMemoryRecords = new ArrayList();

        MassMemoryRecordBasePage page = new MassMemoryRecordBasePage(basePagesFactory);
        massMemoryRecords.add(page);

        VectronProfile vectorProfile = new VectronProfile(vectron);

        Date lastReading = new Date();

        when(vectorProfile.readMassMemoryRecords(lastReading)).thenReturn(massMemoryRecords);
        when(vectorProfile.buildIntervalData(lastReading, false)).thenCallRealMethod();
        when(vectron.getBasePagesFactory().getRealTimeBasePage().getCalendar()).thenReturn(cal);

        vectorProfile.buildIntervalData(lastReading, false );

       // page.parse(data);
    }


    @Test
    public void testParseMemoryBaseRecord() throws Exception {
        when(protocolLink.getTimeZone()).thenReturn(timeZone);
        when(basePagesFactory.getProtocolLink()).thenReturn(protocolLink);

        when(massMemoryBasePages.getNrOfChannels()).thenReturn(2);
        when(basePagesFactory.getMassMemoryBasePages()).thenReturn(massMemoryBasePages);

        when(operatingSetUpBasePage.isDstEnabled()).thenReturn(false);
        when(basePagesFactory.getOperatingSetUpBasePage()).thenReturn(operatingSetUpBasePage);

        MassMemoryBasePages pageGood = new MassMemoryBasePages(basePagesFactory);
        byte[] dataGood = ProtocolTools.getBytesFromHexString("$00$00$00$50$05$2A$00$A8$FF$00$0F$00$39$33$31$36$52$35$20$20$20$20$20$20$20$20$00$90$14$00$00$00$00$00$73$C0$00$00$00$00$00$00$00$00$10$07$21$07$25$24$03$00$00$02$2E$4E$00$00$00$50$50$00$00$00$00");
        pageGood.parse(dataGood);
       // getLogger().info(pageGood.toString());

        MassMemoryBasePages pageBad = new MassMemoryBasePages(basePagesFactory);
        byte[] dataBad = ProtocolTools.getBytesFromHexString("$00$00$00$50$05$2A$00$A8$FF$00$0F$00$39$33$39$36$52$35$20$20$20$20$20$20$20$20$00$00$04$00$00$00$00$00$01$00$00$00$00$00$00$00$00$00$03$06$20$12$04$37$05$00$00$02$2E$4E$00$00$00$50$40$00$00$00$00");
        pageBad.parse(dataBad);
     //   getLogger().info(pageBad.toString());

        RealTimeBasePage realTimeBasePage1 = new RealTimeBasePage(basePagesFactory);
        byte[] realTimeData = ProtocolTools.getBytesFromHexString("$16$11$20$23$26$33$07");
        realTimeBasePage1.parse(realTimeData);
    }


    @Test
    public void testParseRealTimeBasePage() throws Exception {
        when(protocolLink.getTimeZone()).thenReturn(timeZone);
        when(basePagesFactory.getProtocolLink()).thenReturn(protocolLink);

        when(massMemoryBasePages.getNrOfChannels()).thenReturn(2);
        when(basePagesFactory.getMassMemoryBasePages()).thenReturn(massMemoryBasePages);

        when(operatingSetUpBasePage.isDstEnabled()).thenReturn(false);
        when(basePagesFactory.getOperatingSetUpBasePage()).thenReturn(operatingSetUpBasePage);

        RealTimeBasePage realTimeBasePageGood = new RealTimeBasePage(basePagesFactory);
        byte[] realTimeDataGood = ProtocolTools.getBytesFromHexString("$16$11$22$02$32$33$02");
        realTimeBasePageGood.parse(realTimeDataGood);


        RealTimeBasePage realTimeBasePageBad = new RealTimeBasePage(basePagesFactory);
        byte[] realTimeDataBad = ProtocolTools.getBytesFromHexString("$16$11$20$23$26$33$07");
        realTimeBasePageBad.parse(realTimeDataBad);

    }

    @Test
    public void testParseMassMemoryRecord() throws Exception {
        when(protocolLink.getTimeZone()).thenReturn(timeZone);
        when(basePagesFactory.getProtocolLink()).thenReturn(protocolLink);

        when(massMemoryBasePages.getNrOfChannels()).thenReturn(2);
        when(basePagesFactory.getMassMemoryBasePages()).thenReturn(massMemoryBasePages);

        when(operatingSetUpBasePage.isDstEnabled()).thenReturn(false);
        when(basePagesFactory.getOperatingSetUpBasePage()).thenReturn(operatingSetUpBasePage);

        MassMemoryRecordBasePage page = new MassMemoryRecordBasePage(basePagesFactory);
        byte[] data = ProtocolTools.getBytesFromHexString("$11$20$20$00$00$00$00$00$00$00$00$00$00$53$80$99$70$00$00$12$01$91$10$00$15$00$05$12$00$04$10$00$04$10$00$03$0E$00$03$11$00$04$11$00$04$11$00$03$10$00$04$0B$00$02$11$00$04$0D$00$03$10$00$03$0F$00$04$0E$00$03$0D$00$02$11$00$04$11$00$04$16$00$05$10$00$03$0F$00$04$0F$00$03$0C$00$02$0E$00$03$10$00$04$0F$00$03$0D$00$03$0F$00$03$0F$00$03$11$00$04$0F$00$03$10$00$04$08$00$01$11$00$04$10$00$04$10$00$03$0D$00$03$0E$00$03$0D$00$03$16$00$05$10$00$03$09$00$02$0F$00$03$11$00$04$0E$00$03$0A$00$02$10$00$03$0C$00$03$11$00$03$0F$00$04$11$00$03$0F$00$04$09$00$02$0C$00$02$0E$00$03$0F$00$04$0B$00$02$0A$00$02$11$00$04$0A$00$02");
        page.parse(data);
        assertTrue(page.getIntervalRecords()[0].getValues()[0].equals(BigDecimal.valueOf(21)));
        assertTrue(page.getIntervalRecords()[0].getValues()[1].equals(BigDecimal.valueOf(5)));

        getLogger().info(page.toString());
        //TODO: check what's in here, get a reliable reference
    }


    @Test
    public void testDecode() throws IOException {
        when(protocolLink.getTimeZone()).thenReturn(timeZone);
        when(basePagesFactory.getProtocolLink()).thenReturn(protocolLink);

        when(operatingSetUpBasePage.isDstEnabled()).thenReturn(true);
        when(basePagesFactory.getOperatingSetUpBasePage()).thenReturn(operatingSetUpBasePage);

        when(vectron.getLogger()).thenReturn(getLogger());
        when(vectron.getTime()).thenReturn(new Date());
        when(vectron.getProfileInterval()).thenReturn(300);

        when(vectron.getBasePagesFactory()).thenReturn(basePagesFactory);

        List massMemoryRecords = new ArrayList();



        //massMemoryRecords.add(getPage1());
        //massMemoryRecords.add(getPage2());

        int profileInterval = 5;
        int nrOfChannels = 2;
        int currentIntervalNr = 0;

        VectronProfile vectorProfile = new VectronProfile(vectron);

        logger.info(timeZone.toString());
        Calendar now = Calendar.getInstance(timeZone);
        logger.info(vectorProfile.format(now));

        when(massMemoryBasePages.getNrOfChannels()).thenReturn(2);
        when(basePagesFactory.getMassMemoryBasePages()).thenReturn(massMemoryBasePages);

        MassMemoryRecordBasePage pageOK = new MassMemoryRecordBasePage(basePagesFactory);
        byte[] data = ProtocolTools.getBytesFromHexString("$11$20$20$00$00$00$00$00$00$00$00$00$00$53$80$99$70$00$00$12$01$91$10$00$15$00$05$12$00$04$10$00$04$10$00$03$0E$00$03$11$00$04$11$00$04$11$00$03$10$00$04$0B$00$02$11$00$04$0D$00$03$10$00$03$0F$00$04$0E$00$03$0D$00$02$11$00$04$11$00$04$16$00$05$10$00$03$0F$00$04$0F$00$03$0C$00$02$0E$00$03$10$00$04$0F$00$03$0D$00$03$0F$00$03$0F$00$03$11$00$04$0F$00$03$10$00$04$08$00$01$11$00$04$10$00$04$10$00$03$0D$00$03$0E$00$03$0D$00$03$16$00$05$10$00$03$09$00$02$0F$00$03$11$00$04$0E$00$03$0A$00$02$10$00$03$0C$00$03$11$00$03$0F$00$04$11$00$03$0F$00$04$09$00$02$0C$00$02$0E$00$03$0F$00$04$0B$00$02$0A$00$02$11$00$04$0A$00$02");
        pageOK.parse(data);
        assertTrue(pageOK.getIntervalRecords()[0].getValues()[0].equals(BigDecimal.valueOf(21)));
        assertTrue(pageOK.getIntervalRecords()[0].getValues()[1].equals(BigDecimal.valueOf(5)));
        getLogger().info("OK page: "+pageOK.toString());

        MassMemoryRecordBasePage pageBad = new MassMemoryRecordBasePage(basePagesFactory);
        byte[] dataBad = ProtocolTools.getBytesFromHexString("$11$16$19$00$00$00$00$00$00$00$00$00$09$53$70$69$10$00$04$66$21$61$30$00$02$10$39$FF$00$38$00$10$38$FD$00$39$02$10$3A$01$10$3B$FB$00$38$00$10$38$01$10$39$01$10$38$0B$10$3A$08$10$38$03$10$38$06$10$38$07$10$39$08$10$3A$05$10$39$F9$00$38$E4$00$34$D8$00$35$C5$00$33$C1$00$34$C3$00$35$C0$00$36$BB$00$37$B7$00$36$B3$00$36$B1$00$39$AA$00$38$A5$00$38$A2$00$3B$A0$00$39$9F$00$37$94$00$37$91$00$36$92$00$36$90$00$38$8C$00$36$8F$00$38$90$00$36$8E$00$37$8D$00$36$8C$00$38$8C$00$38$81$00$35$76$00$25$70$00$22$65$00$1D$63$00$1D$63$00$1E$61$00$1B$5F$00$1C$5B$00$1B$5B$00$1C$5A$00$1D$59$00$1D$58$00$1C$57$00$1C$57$00$1C$57$00$1D");
        pageBad.parse(dataBad);
        getLogger().info("Bad page: " + pageBad.toString());

        massMemoryRecords.add(pageOK);
        massMemoryRecords.add(pageBad);
        massMemoryRecords.add(pageOK);
        //massMemoryRecords.add(getPage1());

        vectorProfile.parseMassMemoryRecords(massMemoryRecords, now, currentIntervalNr, profileInterval, nrOfChannels);

    }

    private IntervalRecord createRecord(int ch1, int ch2) throws IOException {
        BigDecimal[] values = new BigDecimal[2];
        values[0] = BigDecimal.valueOf(ch1);
        values[1] = BigDecimal.valueOf(ch2);

        IntervalRecord rec = new IntervalRecord(new byte[10], 0, 2);
        rec.setNrOfChannels(2);
        rec.setValues(values);
        return rec;

    }

    public MassMemoryRecordBasePage getPage1() throws IOException {
        MassMemoryRecordBasePage page = new MassMemoryRecordBasePage(basePagesFactory);
        Calendar cal0 = Calendar.getInstance(timeZone);
        page.setRecordNr(145);

        cal0.clear();
        cal0.set(2016, Calendar.NOVEMBER, 18, 8, 0 , 0);
        page.setCalendar(cal0);

        BigDecimal[] registerValues = new BigDecimal[2];
        registerValues[0] = BigDecimal.valueOf(2.4);
        registerValues[1] = BigDecimal.valueOf(5.5);

        page.setRegisterValues(registerValues);

        IntervalRecord[] intervalRecords = new IntervalRecord[60];
        intervalRecords[0]	= createRecord(38,29);
        intervalRecords[1]	= createRecord(39,29);
        intervalRecords[2]	= createRecord(38,28);
        intervalRecords[3]	= createRecord(38,29);
        intervalRecords[4]	= createRecord(39,28);
        intervalRecords[5]	= createRecord(38,30);
        intervalRecords[6]	= createRecord(41,31);
        intervalRecords[7]	= createRecord(40,31);
        intervalRecords[8]	= createRecord(39,30);
        intervalRecords[9]	= createRecord(41,31);
        intervalRecords[10]	= createRecord(38,29);
        intervalRecords[11]	= createRecord(39,29);
        intervalRecords[12]	= createRecord(41,30);
        intervalRecords[13]	= createRecord(38,30);
        intervalRecords[14]	= createRecord(38,29);
        intervalRecords[15]	= createRecord(39,29);
        intervalRecords[16]	= createRecord(38,28);
        intervalRecords[17]	= createRecord(39,28);
        intervalRecords[18]	= createRecord(41,29);
        intervalRecords[19]	= createRecord(38,28);
        intervalRecords[20]	= createRecord(39,29);
        intervalRecords[21]	= createRecord(38,29);
        intervalRecords[22]	= createRecord(40,30);
        intervalRecords[23]	= createRecord(40,29);
        intervalRecords[24]	= createRecord(40,30);
        intervalRecords[25]	= createRecord(40,30);
        intervalRecords[26]	= createRecord(39,28);
        intervalRecords[27]	= createRecord(38,29);
        intervalRecords[28]	= createRecord(39,28);
        intervalRecords[29]	= createRecord(39,28);
        intervalRecords[30]	= createRecord(40,29);
        intervalRecords[31]	= createRecord(40,31);
        intervalRecords[32]	= createRecord(39,29);
        intervalRecords[33]	= createRecord(41,30);
        intervalRecords[34]	= createRecord(38,29);
        intervalRecords[35]	= createRecord(38,27);
        intervalRecords[36]	= createRecord(39,27);
        intervalRecords[37]	= createRecord(37,28);
        intervalRecords[38]	= createRecord(39,28);
        intervalRecords[39]	= createRecord(38,27);
        intervalRecords[40]	= createRecord(38,27);
        intervalRecords[41]	= createRecord(39,27);
        intervalRecords[42]	= createRecord(40,29);
        intervalRecords[43]	= createRecord(38,30);
        intervalRecords[44]	= createRecord(41,31);
        intervalRecords[45]	= createRecord(38,30);
        intervalRecords[46]	= createRecord(39,29);
        intervalRecords[47]	= createRecord(39,29);
        intervalRecords[48]	= createRecord(40,28);
        intervalRecords[49]	= createRecord(40,28);
        intervalRecords[50]	= createRecord(40,30);
        intervalRecords[51]	= createRecord(40,28);
        intervalRecords[52]	= createRecord(41,27);
        intervalRecords[53]	= createRecord(41,27);
        intervalRecords[54]	= createRecord(52,41);
        intervalRecords[55]	= createRecord(57,49);
        intervalRecords[56]	= createRecord(58,50);
        intervalRecords[57]	= createRecord(60,51);
        intervalRecords[58]	= createRecord(65,50);
        intervalRecords[59]	= createRecord(71,49);

        page.setIntervalRecords(intervalRecords);

        return page;
    }
    public MassMemoryRecordBasePage getPage0() throws IOException {
        MassMemoryRecordBasePage page = new MassMemoryRecordBasePage(basePagesFactory);
        Calendar cal0 = Calendar.getInstance(timeZone);

        page.setRecordNr(146);

        cal0.clear();
        cal0.set(2015, Calendar.NOVEMBER, 30, 2, 0, 0);
        page.setCalendar(cal0);

        BigDecimal[] registerValues = new BigDecimal[2];
        registerValues[0] = BigDecimal.valueOf(2.4);
        registerValues[1] = BigDecimal.valueOf(5.5);

        page.setRegisterValues(registerValues);

        IntervalRecord[] intervalRecords = new IntervalRecord[60];
        intervalRecords[0] = createRecord(79, 47);
        intervalRecords[1] = createRecord(79, 47);
        intervalRecords[0]	=createRecord(79,47);
        intervalRecords[1]	=createRecord(94,48);
        intervalRecords[2]	=createRecord(176,54);
        intervalRecords[3]	=createRecord(176,52);
        intervalRecords[4]	=createRecord(164,52);
        intervalRecords[5]	=createRecord(0,0);
        intervalRecords[6]	=createRecord(0,0);
        intervalRecords[7]	=createRecord(0,0);
        intervalRecords[8]	=createRecord(0,0);
        intervalRecords[9]	=createRecord(0,0);
        intervalRecords[10]	=createRecord(0,0);
        intervalRecords[11]	=createRecord(0,0);
        intervalRecords[12]	=createRecord(0,0);
        intervalRecords[13]	=createRecord(0,0);
        intervalRecords[14]	=createRecord(0,0);
        intervalRecords[15]	=createRecord(0,0);
        intervalRecords[16]	=createRecord(0,0);
        intervalRecords[17]	=createRecord(0,0);
        intervalRecords[18]	=createRecord(0,0);
        intervalRecords[19]	=createRecord(0,0);
        intervalRecords[20]	=createRecord(0,0);
        intervalRecords[21]	=createRecord(0,0);
        intervalRecords[22]	=createRecord(0,0);
        intervalRecords[23]	=createRecord(0,0);
        intervalRecords[24]	=createRecord(0,0);
        intervalRecords[25]	=createRecord(0,0);
        intervalRecords[26]	=createRecord(0,0);
        intervalRecords[27]	=createRecord(0,0);
        intervalRecords[28]	=createRecord(0,0);
        intervalRecords[29]	=createRecord(0,0);
        intervalRecords[30]	=createRecord(0,0);
        intervalRecords[31]	=createRecord(0,0);
        intervalRecords[32]	=createRecord(0,0);
        intervalRecords[33]	=createRecord(0,0);
        intervalRecords[34]	=createRecord(0,0);
        intervalRecords[35]	=createRecord(0,0);
        intervalRecords[36]	=createRecord(0,0);
        intervalRecords[37]	=createRecord(0,0);
        intervalRecords[38]	=createRecord(0,0);
        intervalRecords[39]	=createRecord(0,0);
        intervalRecords[40]	=createRecord(0,0);
        intervalRecords[41]	=createRecord(0,0);
        intervalRecords[42]	=createRecord(0,0);
        intervalRecords[43]	=createRecord(0,0);
        intervalRecords[44]	=createRecord(0,0);
        intervalRecords[45]	=createRecord(0,0);
        intervalRecords[46]	=createRecord(0,0);
        intervalRecords[47]	=createRecord(0,0);
        intervalRecords[48]	=createRecord(0,0);
        intervalRecords[49]	=createRecord(0,0);
        intervalRecords[50]	=createRecord(0,0);
        intervalRecords[51]	=createRecord(0,0);
        intervalRecords[52]	=createRecord(0,0);
        intervalRecords[53]	=createRecord(0,0);
        intervalRecords[54]	=createRecord(0,0);
        intervalRecords[55]	=createRecord(0,0);
        intervalRecords[56]	=createRecord(0,0);
        intervalRecords[57]	=createRecord(0,0);
        intervalRecords[58]	=createRecord(0,0);
        intervalRecords[59]	=createRecord(0,0);

        page.setIntervalRecords(intervalRecords);

        return page;
    }

    public Logger getLogger() {
        return logger;
    }



    public MassMemoryRecordBasePage getPage2() throws IOException {
        MassMemoryRecordBasePage page = new MassMemoryRecordBasePage(basePagesFactory);
        Calendar cal0 = Calendar.getInstance(timeZone);
        page.setRecordNr(143);

        cal0.clear();
        cal0.set(2016, Calendar.NOVEMBER, 17, 22, 0 , 0);
        page.setCalendar(cal0);

        BigDecimal[] registerValues = new BigDecimal[2];
        registerValues[0] = BigDecimal.valueOf(2.4);
        registerValues[1] = BigDecimal.valueOf(5.5);

        page.setRegisterValues(registerValues);

        IntervalRecord[] intervalRecords = new IntervalRecord[60];
        intervalRecords[0]	= createRecord(254,55);
        intervalRecords[1]	= createRecord(250,53);
        intervalRecords[2]	= createRecord(250,54);
        intervalRecords[3]	= createRecord(246,54);
        intervalRecords[4]	= createRecord(246,55);
        intervalRecords[5]	= createRecord(237,55);
        intervalRecords[6]	= createRecord(236,54);
        intervalRecords[7]	= createRecord(235,54);
        intervalRecords[8]	= createRecord(232,55);
        intervalRecords[9]	= createRecord(230,55);
        intervalRecords[10]	= createRecord(230,57);
        intervalRecords[11]	= createRecord(228,55);
        intervalRecords[12]	= createRecord(227,58);
        intervalRecords[13]	= createRecord(224,56);
        intervalRecords[14]	= createRecord(216,56);
        intervalRecords[15]	= createRecord(217,56);
        intervalRecords[16]	= createRecord(219,57);
        intervalRecords[17]	= createRecord(220,58);
        intervalRecords[18]	= createRecord(217,58);
        intervalRecords[19]	= createRecord(217,58);
        intervalRecords[20]	= createRecord(208,58);
        intervalRecords[21]	= createRecord(199,57);
        intervalRecords[22]	= createRecord(194,57);
        intervalRecords[23]	= createRecord(194,57);
        intervalRecords[24]	= createRecord(189,57);
        intervalRecords[25]	= createRecord(187,56);
        intervalRecords[26]	= createRecord(186,57);
        intervalRecords[27]	= createRecord(187,57);
        intervalRecords[28]	= createRecord(180,57);
        intervalRecords[29]	= createRecord(174,59);
        intervalRecords[30]	= createRecord(169,57);
        intervalRecords[31]	= createRecord(165,58);
        intervalRecords[32]	= createRecord(162,55);
        intervalRecords[33]	= createRecord(156,40);
        intervalRecords[34]	= createRecord(149,39);
        intervalRecords[35]	= createRecord(144,34);
        intervalRecords[36]	= createRecord(141,34);
        intervalRecords[37]	= createRecord(139,32);
        intervalRecords[38]	= createRecord(144,32);
        intervalRecords[39]	= createRecord(144,31);
        intervalRecords[40]	= createRecord(147,31);
        intervalRecords[41]	= createRecord(147,32);
        intervalRecords[42]	= createRecord(146,33);
        intervalRecords[43]	= createRecord(153,38);
        intervalRecords[44]	= createRecord(153,40);
        intervalRecords[45]	= createRecord(151,42);
        intervalRecords[46]	= createRecord(149,40);
        intervalRecords[47]	= createRecord(143,40);
        intervalRecords[48]	= createRecord(138,40);
        intervalRecords[49]	= createRecord(138,41);
        intervalRecords[50]	= createRecord(135,40);
        intervalRecords[51]	= createRecord(136,41);
        intervalRecords[52]	= createRecord(138,42);
        intervalRecords[53]	= createRecord(140,39);
        intervalRecords[54]	= createRecord(131,42);
        intervalRecords[55]	= createRecord(128,40);
        intervalRecords[56]	= createRecord(125,38);
        intervalRecords[57]	= createRecord(121,38);
        intervalRecords[58]	= createRecord(120,39);
        intervalRecords[59]	= createRecord(125,39);

        page.setIntervalRecords(intervalRecords);

        return page;
    }
}