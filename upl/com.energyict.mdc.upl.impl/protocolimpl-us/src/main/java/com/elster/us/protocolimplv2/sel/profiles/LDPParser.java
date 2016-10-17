package com.elster.us.protocolimplv2.sel.profiles;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.elster.us.protocolimplv2.sel.profiles.structure.Event;
import com.elster.us.protocolimplv2.sel.profiles.structure.Interval;
import com.elster.us.protocolimplv2.sel.profiles.structure.LDPError;
import com.elster.us.protocolimplv2.sel.profiles.structure.LPData;
import com.elster.us.protocolimplv2.sel.profiles.structure.MeterConfiguration;
import com.elster.us.protocolimplv2.sel.profiles.structure.MeterStatus;
import com.elster.us.protocolimplv2.sel.profiles.structure.PresentValues;
import com.elster.us.protocolimplv2.sel.profiles.structure.SERData;

import static com.elster.us.protocolimplv2.sel.Consts.RECORD_METER_CONFIG;
import static com.elster.us.protocolimplv2.sel.Consts.RECORD_METER_STATUS;
import static com.elster.us.protocolimplv2.sel.Consts.RECORD_PRESENT_VALUES;
import static com.elster.us.protocolimplv2.sel.Consts.RECORD_LDP_DATA;
import static com.elster.us.protocolimplv2.sel.Consts.RECORD_SER_DATA;
import static com.elster.us.protocolimplv2.sel.Consts.RECORD_LDP_ERROR;

public class LDPParser {
  
  private int numberOfChannels;
  private String recorder = "COI";
  Logger logger = Logger.getLogger(this.getClass().getName());
  
  
  public LDPData parseYModemFile(DataInputStream dataInputStream) throws IOException {
    LDPData ldpData = new LDPData();
    List<LPData> lpRecord = new ArrayList<LPData>();
    List<SERData> serRecord = new ArrayList<SERData>();
    List<LDPError> lpErrRecord = new ArrayList<LDPError>();
    byte[] dataBlock;
    int len=0;
    
    try {
      while(dataInputStream.available() > 0) {
        byte recordType = (byte)dataInputStream.readShort();
        int recordSize = dataInputStream.readUnsignedShort();
        
        switch (recordType) {
        case RECORD_METER_CONFIG:
          dataBlock = new byte[recordSize];
          len = dataInputStream.read(dataBlock, 0, recordSize);
          ldpData.setMeterConfig(parseMeterConfig(dataBlock));
          break;
        case RECORD_METER_STATUS:
          dataBlock = new byte[recordSize];
          len = dataInputStream.read(dataBlock, 0, recordSize);
          ldpData.setMeterStatus(parseMeterStatus(dataBlock));
          break;
        case RECORD_PRESENT_VALUES:
          dataBlock = new byte[recordSize];
          len = dataInputStream.read(dataBlock, 0, recordSize);
          ldpData.setPresentValues(parsePresentValues(dataBlock));
          break;
        case RECORD_LDP_DATA:
          dataBlock = new byte[recordSize];
          len = dataInputStream.read(dataBlock, 0, recordSize);
          lpRecord.add(parseLDPData(dataBlock));
          break;
        case RECORD_SER_DATA:
          dataBlock = new byte[recordSize];
          len = dataInputStream.read(dataBlock, 0, recordSize);
          serRecord.add(parseSERData(dataBlock));
          break;
        case RECORD_LDP_ERROR:
          dataBlock = new byte[recordSize];
          len = dataInputStream.read(dataBlock, 0, recordSize);
          lpErrRecord.add(parseLDPError(dataBlock));
          break;
        default:
          //either invalid recordtype or EOF
          //System.out.println("unrecognized recordtype: " + String.format("%02X", recordType));
          break;
        }
      }
    } catch(EOFException e) {
      //end of file was reached
    }
    if(lpRecord.size() > 0)
      ldpData.setLpData(lpRecord);
    if(serRecord.size() > 0)
      ldpData.setSerData(serRecord);
    if(lpErrRecord.size() > 0)
      ldpData.setLdpError(lpErrRecord);
    
    return ldpData;
  }
  
  private LDPError parseLDPError(byte[] dataBlock) {
    // TODO Auto-generated method stub
    return null;
  }

  private SERData parseSERData(byte[] dataBlock) throws IOException {
    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(dataBlock, 0, dataBlock.length));
    SERData serdata = new SERData();
    List<Event> events = new ArrayList<Event>();
    Event event;
    int numEvents = (int) Math.floor(((dataBlock.length) / (10)));
    for(int i=0; i<numEvents; i++) {
      event = new Event();
      event.setYear(dis.readUnsignedShort());
      event.setJulianDay(dis.readUnsignedShort());
      event.setTenthsMillSecSinceMidnight(readUInt32(dis));
      event.setSerData(dis.readUnsignedShort());
      events.add(event);
    }
    serdata.setEvents(events);
    serdata.setCheckSum(dis.readUnsignedShort());
    return serdata;
  }

  private float[] parsePresentValues(byte[] dataBlock) throws IOException {
    float[] presentValues = new float[this.numberOfChannels];
    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(dataBlock, 0, dataBlock.length));
    for(int i=0; i < this.numberOfChannels; i++) {
      presentValues[i] = readUInt32(dis);
    }
    return presentValues;
  }

  private LPData parseLDPData(byte[] dataBlock) throws IOException {
    Float[] intvlValues;
    Long[] intvlValuesEOI;
    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(dataBlock, 0, dataBlock.length));
    LPData lpData = new LPData();
    List<Interval> intervals = new ArrayList<Interval>();
    Interval interval;
    int numIntervals = (int) Math.floor(((dataBlock.length) / (9 + (this.numberOfChannels * 4))));
    for(int i=0; i<numIntervals; i++) {
      interval = new Interval();
      intvlValues = new Float[this.numberOfChannels];
      intvlValuesEOI = new Long[this.numberOfChannels];
      interval.setStatus(dis.readByte() & 0xFF);
      interval.setYear(dis.readUnsignedShort());
      interval.setJulianDay(dis.readUnsignedShort());
      interval.setTenthsMillSecSinceMidnight(readUInt32(dis));
      for(int j=0; j<intvlValues.length; j++) {
        if(recorder.trim().equals("EOI"))
          intvlValuesEOI[j] = readUInt32(dis);
        else
          intvlValues[j] = dis.readFloat();
      }
      if(recorder.trim().equals("EOI"))
        interval.setChannelValues(intvlValuesEOI);
      else
        interval.setChannelValues(intvlValues);
      
      intervals.add(interval);
    }
    lpData.setIntervals(intervals);
    lpData.setCheckSum(dis.readUnsignedShort());
    lpData.setCalCheckSum(calcCheckSum(dataBlock));
    //TODO: figure out why checksum doesn't always validate but data is correct
    //validateChecksum(lpData.getCheckSum(), lpData.getCalCheckSum());
    
    return lpData;
  }

  private void validateChecksum(int checkSum, int binarySum) throws IOException {
    binarySum += 1; //the binary sum is always one off from checksum, not sure why
    if(checkSum != binarySum)
      logger.warning("Checksum error: checksum = " + checkSum + " binary sum = " + binarySum);
    else
      logger.info("Checksum = " + checkSum + " binary sum = " + binarySum);
  }

  private MeterStatus parseMeterStatus(byte[] dataBlock) {
    // TODO Auto-generated method stub
    return null;
  }

  
  private MeterConfiguration parseMeterConfig(byte[] dataBlock) throws IOException {
    byte[] buffer = new byte[33];
    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(dataBlock, 0, dataBlock.length));
    MeterConfiguration meterConfig = new MeterConfiguration();
    meterConfig.currYear = dis.readUnsignedShort();
    meterConfig.currJulianDay = dis.readUnsignedShort();
    //dis.read(buffer, 0, 4);
    meterConfig.currTenthsMillSecSinceMidnight = readUInt32(dis);
    meterConfig.timeSource = dis.readByte() & 0xFF;
    dis.read(buffer, 0, 10);
    meterConfig.startDSTConfigString = new String(buffer, 0, 10, "ASCII").trim();
    dis.read(buffer,0 ,10);
    meterConfig.stopDSTConfigString = new String(buffer, 0, 10, "ASCII").trim();
    meterConfig.dstForwardTime = dis.readUnsignedShort();
    meterConfig.dstBackwardTime = dis.readUnsignedShort();
    meterConfig.ctr = dis.readFloat();
    meterConfig.ctrn = dis.readFloat();
    meterConfig.ptr = dis.readFloat();
    dis.read(buffer, 0, 33);
    String fid = new String(buffer, 0, 33, "ASCII").trim();
    meterConfig.fid = fid;
    dis.read(buffer, 0, 22);
    meterConfig.mid = new String(buffer, 0, 22, "ASCII").trim();
    //handle slight variation between 734 and 735
    if(fid.contains("734")) {
      dis.read(buffer, 0, 22);
      meterConfig.tid = new String(buffer, 0, 22, "ASCII").trim();
    } else {
      dis.read(buffer, 0, 26);
      meterConfig.tid = new String(buffer, 0, 26, "ASCII").trim();
    }
    meterConfig.meterForm = dis.readByte() & 0xFF;
    meterConfig.serRecordsAvailable = dis.readUnsignedShort();
    meterConfig.firstLDPRecordYear = dis.readUnsignedShort();
    meterConfig.firstLDPRecordJulianDay = dis.readUnsignedShort();
    meterConfig.firstLDPRecordTenthsMillSecSinceMidnight = readUInt32(dis);
    meterConfig.lastLDPRecordYear = dis.readUnsignedShort();
    meterConfig.lastLDPRecordJulianDay = dis.readUnsignedShort();
    meterConfig.lastLDPRecordTenthsMillSecSinceMidnight = readUInt32(dis);
    meterConfig.numberLDPRecordsAvailable = readUInt32(dis);
    meterConfig.ldarSetting = dis.readUnsignedShort();
    this.numberOfChannels = dis.readUnsignedShort();
    meterConfig.numberLDPChannelsEnabled = this.numberOfChannels;
    //parse the channel names - ASCII list separated by NULL ('\O'), terminated by 2 NULL characters in a row.
    List<String> chnlNames = new ArrayList<String>();
    byte[] chnlBytes = new byte[10];
    byte b;
    boolean terminate = false;
    int index = 0;
    while(dis.available() != -1) {
      b = dis.readByte();
      if(b != 0) {
        chnlBytes[index] = b;
        index++;
        terminate = false;
      }else {
        if(!terminate) {
          chnlNames.add(new String(chnlBytes, "ASCII").trim());
          index = 0;
          chnlBytes = new byte[10];
          terminate = true;
        }else {
          break;
        }
      }
    }
    meterConfig.channelNames = chnlNames;
    //parse the recorder function (EOI or COI) - ASCII list separated by NULL ('\O'), terminated by 2 NULL characters in a row.
    List<String> recorderNames = new ArrayList<String>();
    byte[] recorderBytes = new byte[3];
    terminate = false;
    index = 0;
    while(dis.available() != -1) {
      b = dis.readByte();
      if(b != 0) {
        recorderBytes[index] = b;
        index++;
        terminate = false;
      }else {
        if(!terminate) {
          recorderNames.add(new String(recorderBytes, "ASCII").trim());
          index = 0;
          recorderBytes = new byte[3];
          terminate = true;
        }else {
          break;
        }
      }
    }
    setRecorderType(recorderNames);
    meterConfig.recorderNames = recorderNames;
    
    return meterConfig;
  }
  
  private static long readUInt32(DataInputStream in) throws IOException {
    int bytes = 4;

    long result = 0;
    for (int i = 0; i < bytes; i++){
      result = result << 8;
      result += in.read();
    }
    return result;
  }
  
  
  private int getIntFromUINT32(byte[] buffer) {
    return (buffer[0] & 0xFF) | (buffer[1] & 0xFF) << 8 | (buffer[2] & 0xFF) << 16 | (buffer[3] & 0xFF) << 24;
  }
  
  private void setRecorderType(List<String> recorderNames) {
    this.recorder = recorderNames.get(0);
  }
  
  private int calcCheckSum(byte[] dataBlock) {
    long checksum = 0;
    int length = dataBlock.length - 2; //ignore the last two bytes which represent checksum
    for(int i=0; i<length; i++) {
      checksum += (dataBlock[i] & 0xff);
    }
    checksum = checksum & 0xFFFFFFFFL; // truncate to 4 byte
    return (int) checksum;
  }

}
