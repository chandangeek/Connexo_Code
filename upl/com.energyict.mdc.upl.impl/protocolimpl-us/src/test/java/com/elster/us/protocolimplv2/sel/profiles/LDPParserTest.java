package com.elster.us.protocolimplv2.sel.profiles;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import com.elster.us.protocolimplv2.sel.utility.EventMapper;

public class LDPParserTest {
  @Test
  @Ignore
  public void testParser() throws IOException {
    File lpFile = new File("src/test/files/LDP1_DATA.BIN");
    LDPParser ldpParser = new LDPParser();
    LDPData results = ldpParser.parseYModemFile(new DataInputStream(new FileInputStream(lpFile)));
    assertEquals(results.getMeterConfig().getCurrYear(), 2016);
    assertEquals(results.getMeterConfig().getCurrJulianDay(), 83);
    assertEquals(results.getMeterConfig().getCurrTenthsMillSecSinceMidnight(), 570835044);
    assertEquals(results.getMeterConfig().getTimeSource(), 1);
    assertEquals(results.getMeterConfig().getStartDSTConfigString(), "-OO1203---");
    assertEquals(results.getMeterConfig().getStopDSTConfigString(), "-OF1-11---");
    assertEquals(results.getMeterConfig().getDstForwardTime(), 0);
    assertEquals(results.getMeterConfig().getDstBackwardTime(), 0);
    assertEquals(results.getMeterConfig().getCtr(), 40.0, .1);
    assertEquals(results.getMeterConfig().getCtrn(), 1.0, .1);
    assertEquals(results.getMeterConfig().getPtr(), 60.0, .1);
    assertEquals(results.getMeterConfig().getFid(), "SEL-735-R103-V0-Z002002-D20120608");
    assertEquals(results.getMeterConfig().getMid(), "FEEDER 4");
    assertEquals(results.getMeterConfig().getTid(), "STATION D");
    assertEquals(results.getMeterConfig().getMeterForm(), 1);
    assertEquals(results.getMeterConfig().getSerRecordsAvailable(), 512);
    assertEquals(results.getMeterConfig().getFirstLDPRecordYear(), 2016);
    assertEquals(results.getMeterConfig().getFirstLDPRecordJulianDay(), 83);
    assertEquals(results.getMeterConfig().getFirstLDPRecordTenthsMillSecSinceMidnight(), 540000000);
    assertEquals(results.getMeterConfig().getLastLDPRecordYear(), 2016);
    assertEquals(results.getMeterConfig().getLastLDPRecordJulianDay(), 83);
    assertEquals(results.getMeterConfig().getLastLDPRecordTenthsMillSecSinceMidnight(), 555000000);
    assertEquals(results.getMeterConfig().getNumberLDPRecordsAvailable(), 6);
    assertEquals(results.getMeterConfig().getLdarSetting(), 300);
    assertEquals(results.getMeterConfig().getNumberLDPChannelsEnabled(), 2);
    assertEquals(results.getMeterConfig().getChannelNames().get(0), "WH3_DEL");
    assertEquals(results.getMeterConfig().getChannelNames().get(1), "QH3_DEL");
    
    assertEquals(results.getLpData().get(0).getIntervals().get(0).getStatus(), 0);
    assertEquals(results.getLpData().get(0).getIntervals().get(0).getYear(), 2016);
    assertEquals(results.getLpData().get(0).getIntervals().get(0).getJulianDay(), 83);
    assertEquals(results.getLpData().get(0).getIntervals().get(0).getTenthsMillSecSinceMidnight(), 540000000);
    assertEquals(results.getLpData().get(0).getIntervals().get(0).getChannelValues()[0].floatValue(), 5.428663, .01);
    assertEquals(results.getLpData().get(0).getIntervals().get(0).getChannelValues()[1].floatValue(), 2.920776, .01);
    assertEquals(results.getLpData().get(0).getCheckSum(), 1774);
    assertEquals(results.getLpData().get(0).getCalCheckSum(), 1773);
    //checksum is 06 ee -> 1774
    //binary sum: 1773
    
    assertEquals(results.getLpData().get(1).getIntervals().get(0).getStatus(), 127);
    assertEquals(results.getLpData().get(1).getIntervals().get(0).getYear(), 2016);
    assertEquals(results.getLpData().get(1).getIntervals().get(0).getJulianDay(), 83);
    assertEquals(results.getLpData().get(1).getIntervals().get(0).getTenthsMillSecSinceMidnight(), 543000000);
    assertEquals(results.getLpData().get(1).getIntervals().get(0).getChannelValues()[0].floatValue(), 5.366466, .01);
    assertEquals(results.getLpData().get(1).getIntervals().get(0).getChannelValues()[1].floatValue(), 2.526273, .01);
    
    assertEquals(results.getLpData().get(1).getIntervals().get(1).getStatus(), 32);
    assertEquals(results.getLpData().get(1).getIntervals().get(1).getYear(), 2016);
    assertEquals(results.getLpData().get(1).getIntervals().get(1).getJulianDay(), 83);
    assertEquals(results.getLpData().get(1).getIntervals().get(1).getTenthsMillSecSinceMidnight(), 546000000);
    assertEquals(results.getLpData().get(1).getIntervals().get(1).getChannelValues()[0].floatValue(), 5.174322, .01);
    assertEquals(results.getLpData().get(1).getIntervals().get(1).getChannelValues()[1].floatValue(), 2.556568, .01);
    
    assertEquals(results.getLpData().get(1).getIntervals().get(2).getStatus(), 0);
    assertEquals(results.getLpData().get(1).getIntervals().get(2).getYear(), 2016);
    assertEquals(results.getLpData().get(1).getIntervals().get(2).getJulianDay(), 83);
    assertEquals(results.getLpData().get(1).getIntervals().get(2).getTenthsMillSecSinceMidnight(), 549000000);
    assertEquals(results.getLpData().get(1).getIntervals().get(2).getChannelValues()[0].floatValue(), 5.373793, .01);
    assertEquals(results.getLpData().get(1).getIntervals().get(2).getChannelValues()[1].floatValue(), 2.560836, .01);
    
    assertEquals(results.getLpData().get(1).getIntervals().get(3).getStatus(), 0);
    assertEquals(results.getLpData().get(1).getIntervals().get(3).getYear(), 2016);
    assertEquals(results.getLpData().get(1).getIntervals().get(3).getJulianDay(), 83);
    assertEquals(results.getLpData().get(1).getIntervals().get(3).getTenthsMillSecSinceMidnight(), 552000000);
    assertEquals(results.getLpData().get(1).getIntervals().get(3).getChannelValues()[0].floatValue(), 5.794771, .01);
    assertEquals(results.getLpData().get(1).getIntervals().get(3).getChannelValues()[1].floatValue(), 3.273112, .01);
    
    assertEquals(results.getLpData().get(1).getIntervals().get(4).getStatus(), 0);
    assertEquals(results.getLpData().get(1).getIntervals().get(4).getYear(), 2016);
    assertEquals(results.getLpData().get(1).getIntervals().get(4).getJulianDay(), 83);
    assertEquals(results.getLpData().get(1).getIntervals().get(4).getTenthsMillSecSinceMidnight(), 555000000);
    assertEquals(results.getLpData().get(1).getIntervals().get(4).getChannelValues()[0].floatValue(), 5.467003, .01);
    assertEquals(results.getLpData().get(1).getIntervals().get(4).getChannelValues()[1].floatValue(), 2.835624, .01);
    assertEquals(results.getLpData().get(1).getCheckSum(), 8558);
    assertEquals(results.getLpData().get(1).getCalCheckSum(), 8557);
    //checksum is 21 6e
    
   
  }
  
  @Test
  public void testChecksum() throws IOException {
    File lpFile = new File("src/test/files/LDP_DATA_FEEDER1.BIN");
    LDPParser ldpParser = new LDPParser();
    LDPData results = ldpParser.parseYModemFile(new DataInputStream(new FileInputStream(lpFile)));
    
    assertEquals(results.getLpData().get(0).getCheckSum(), 1986);
    assertEquals(results.getLpData().get(0).getCalCheckSum(), 1985);
    
    //assertEquals(results.getLpData().get(1).getCheckSum(), 36338);
    //assertEquals(results.getLpData().get(1).getCalCheckSum(), 36337);
    
    //assertEquals(results.getLpData().get(2).getCheckSum(), 5);
    //assertEquals(results.getLpData().get(2).getCalCheckSum(), 5);
    
   
  }
  
  @Test
  public void testSERData() throws IOException {
    File lpFile = new File("src/test/files/LDP_DATA_SER.BIN");
    LDPParser ldpParser = new LDPParser();
    LDPData results = ldpParser.parseYModemFile(new DataInputStream(new FileInputStream(lpFile)));
    assertEquals(results.getSerData().size(), 7);
    assertEquals(EventMapper.mapEventId(results.getSerData().get(0).getEvents().get(0).getMeterWordBit()), "SALARM");
    assertEquals(EventMapper.mapEventId(results.getSerData().get(0).getEvents().get(1).getMeterWordBit()), EventMapper.EVENT_UNDEFINED);
  }

}
