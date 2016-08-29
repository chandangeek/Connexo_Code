package com.elster.us.protocolimplv2.sel;


//import static com.elster.us.protocolimplv2.sel.utility.ResponseValueHelper.getNumericValue;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_RD;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_RE;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_RG;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_SF;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_SN;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_WD;
import static com.elster.us.protocolimplv2.sel.Consts.CONTROL_ACK;
import static com.elster.us.protocolimplv2.sel.Consts.CONTROL_CRC;
import static com.elster.us.protocolimplv2.sel.Consts.CONTROL_ENQ;
import static com.elster.us.protocolimplv2.sel.Consts.CONTROL_EOT;
import static com.elster.us.protocolimplv2.sel.Consts.CONTROL_ETX;
import static com.elster.us.protocolimplv2.sel.Consts.CONTROL_RS;
import static com.elster.us.protocolimplv2.sel.Consts.CONTROL_SOH;
import static com.elster.us.protocolimplv2.sel.Consts.CONTROL_STX;
import static com.elster.us.protocolimplv2.sel.Consts.CONTROL_CR;
import static com.elster.us.protocolimplv2.sel.Consts.STR_VQ;
import static com.elster.us.protocolimplv2.sel.Consts.ACC0;
import static com.elster.us.protocolimplv2.sel.Consts.ACC1;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_ACC;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_REG;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_LP;
import static com.elster.us.protocolimplv2.sel.Consts.ENTER_PASWD;
import static com.elster.us.protocolimplv2.sel.Consts.ENCODING;
import static com.elster.us.protocolimplv2.sel.Consts.LDP_DATE_FORMAT;
import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_DIRECTION_DELIVERED;
import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_DIRECTION_RECEIVED;
import static com.elster.us.protocolimplv2.sel.utility.ByteArrayHelper.*;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.elster.us.protocolimplv2.sel.utility.ObisCodeMapper;
import com.elster.us.protocolimplv2.sel.utility.UnitMapper;
import com.elster.us.protocolimplv2.sel.frame.RequestFrame;
import com.elster.us.protocolimplv2.sel.frame.ResponseFrame;
import com.elster.us.protocolimplv2.sel.frame.data.BasicData;
import com.elster.us.protocolimplv2.sel.frame.data.ExtendedData;
import com.elster.us.protocolimplv2.sel.frame.data.RegisterReadResponseData;
import com.elster.us.protocolimplv2.sel.profiles.LDPData;
import com.elster.us.protocolimplv2.sel.profiles.LDPParser;
import com.elster.us.protocolimplv2.sel.profiles.LoadProfileEIServerFormatter;
import com.elster.us.protocolimplv2.sel.registers.RegisterData;
import com.elster.us.protocolimplv2.sel.registers.RegisterParser;
import com.elster.us.protocolimplv2.sel.utility.YModem;
import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.DefaultDeviceRegister;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifierById;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.DataParseException;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierById;

public class SELConnection {
  
  private final static int CRC_LENGTH = 4;
  private final SELProperties properties;
  private Logger logger;
  private SerialPortComChannel comChannel;
  private boolean connected = false;
  
  private String lastCommandSent;
  
  public SELConnection(SerialPortComChannel comChannel, SELProperties properties, Logger logger) {
    this.comChannel = comChannel;
    this.properties = properties;
    this.logger = logger;
  }
  
  public List<ResponseFrame> sendAndReceiveFrames(RequestFrame toSend) {
    sendFrame(toSend);
    return receiveResponse();
}

public ResponseFrame sendAndReceiveFrame(RequestFrame toSend) {
    List<ResponseFrame> responseFrames = sendAndReceiveFrames(toSend);
    if (responseFrames.size() == 1) {
        return responseFrames.get(0);
    } else {
        throw CommunicationException.unexpectedResponse(new IOException("Unexpected number of response frames received: " + responseFrames.size() + ", expecting 1"));
    }
}

private ResponseFrame sendSignOn() {
    // Construct the data
    BasicData data = new BasicData(properties.getDevicePassword());
    // Construct the frame, including the data
    RequestFrame snFrame = new RequestFrame(data);
    // Keep a record of the last command we sent to the device
    lastCommandSent = COMMAND_SN;
    // Send the frame and handle the response
    sendFrame(snFrame);
    delay();
    return receiveUnformattedResponse();
}

public List<ResponseFrame> getEvents(Date startDate, Date endDate, int recordsPerPacket) {
    /*
        This is the code for EM "event multiple read", as (supposedly)
        supported by the mini-max device

        // Construct the command params
        ByteArrayOutputStream commandParams = new ByteArrayOutputStream();
        // Always send a comma char here according to spec.
        String recPerPacPadded = String.format("%03d", recordsPerPacket);
        commandParams.write(getBytes(recPerPacPadded));

        commandParams.write(CONTROL_STX);
        commandParams.write('0');
        commandParams.write(',');
        String startDateStr = "120115";
        String startTimeStr = "000000";
        String endDateStr = "210116";
        String endTimeStr = "235959";
        commandParams.write(getBytes(startDateStr));
        commandParams.write(',');
        commandParams.write(getBytes(startTimeStr));
        commandParams.write(',');
        commandParams.write(getBytes(endDateStr));
        commandParams.write(',');
        commandParams.write(getBytes(endTimeStr));
        // Construct the data
        ExtendedData data = new ExtendedData(getBytes(COMMAND_EM), commandParams.toByteArray());
        // Construct the frame, including the data
        RequestFrame snFrame = new RequestFrame(data);
        // Keep a record of the last command we sent to the device
        lastCommandSent = COMMAND_EM;
        // Send the frame and handle the response
        return sendAndReceiveFrames(snFrame);
    */

    // Construct the command params
    ByteArrayOutputStream commandParams = new ByteArrayOutputStream();

    commandParams.write(CONTROL_STX);
    // TODO: can be 0, 1 or 2
    commandParams.write('2');
    // Construct the data
    ExtendedData data = new ExtendedData(getBytes(COMMAND_RE), commandParams.toByteArray());
    // Construct the frame, including the data
    RequestFrame snFrame = new RequestFrame(data);
    // Keep a record of the last command we sent to the device
    lastCommandSent = COMMAND_RE;
    // Send the frame and handle the response
    return sendAndReceiveFrames(snFrame);
}

private ResponseFrame sendSignOff() {
    BasicData data = new BasicData(getBytes(COMMAND_SF));
    RequestFrame snFrame = new RequestFrame(data);
    lastCommandSent = COMMAND_SF;
    return sendAndReceiveFrame(snFrame);
}

public void sendFrame(RequestFrame frame) {
    comChannel.startWriting();
    comChannel.write(frame.toByteArray());
}

public void sendSELASCIICommand(String asciiCommand) {
  try {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    outputStream.write(getBytes(asciiCommand));
    outputStream.write(getBytes("\r"));
    comChannel.startWriting();
    comChannel.write(outputStream.toByteArray());
  } catch (IOException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
}

public List<ResponseFrame> receiveResponseFrames() {

    List<ResponseFrame> receivedFrames = new ArrayList<ResponseFrame>();

    byte b = 0x00;
    // Find the start of the sequence of frames
    while (b != CONTROL_SOH) {
        comChannel.startReading();
        b = (byte)comChannel.read();
        if (b == -1) {
            IOException ioe = new IOException("End of stream when reading from device");
            throw ConnectionCommunicationException.unexpectedIOException(ioe);
        }
    }

    ByteArrayOutputStream receivedBytes = new ByteArrayOutputStream();
    while (b != CONTROL_EOT) {
        comChannel.startReading();
        b = (byte)comChannel.read();
        if (b == -1) {
            IOException ioe = new IOException("End of stream when reading from device");
            throw ConnectionCommunicationException.unexpectedIOException(ioe);
        }
        if (b != CONTROL_ETX) {
            receivedBytes.write(b);
        } else {
            byte[] crc = readCrc();
            ResponseFrame response = new ResponseFrame(receivedBytes.toByteArray(), lastCommandSent);
            if (checkCrc(response.generateCRC(), crc)) {
                // add the frame to be returned
                receivedFrames.add(response);
                comChannel.startReading();
                b = (byte)comChannel.read();
                if (b == CONTROL_RS) {
                    // Receiving multiple packets...
                    // send ack
                    comChannel.startWriting();
                    comChannel.write(CONTROL_ACK);
                } else {
                    // it will be an EOT and the loop will terminate
                }
            } else {
                // TODO: error condition
                System.out.println();
            }
        }
    }
    return receivedFrames;
}


public List<ResponseFrame> receiveResponse() {
  List<ResponseFrame> receivedFrames = new ArrayList<ResponseFrame>();
  byte b = 0x00;
  comChannel.startReading();
  //Find the start of the sequence of frames
  for(int retries=0; retries < 3; retries++) {
    delay();
    while (b != CONTROL_STX && comChannel.available() > 0) {
        b = (byte)comChannel.read();
        if (b == -1) {
            IOException ioe = new IOException("End of stream when reading from device");
            throw ConnectionCommunicationException.unexpectedIOException(ioe);
        }
    }
    if(b == CONTROL_STX || retries == 3)
      break;
  }
  ByteArrayOutputStream receivedBytes = new ByteArrayOutputStream();
  for(int retries=0; retries < 3; retries++) {
    delay();
    while (b != CONTROL_ETX && comChannel.available() > 0) {
        b = (byte)comChannel.read();
        if (b == -1) {
            IOException ioe = new IOException("End of stream when reading from device");
            throw ConnectionCommunicationException.unexpectedIOException(ioe);
        }
        if (b != CONTROL_ETX) {
            receivedBytes.write(b);
        } else {
            ResponseFrame response = new ResponseFrame(receivedBytes.toByteArray(), lastCommandSent);
            // add the frame to be returned
            receivedFrames.add(response); 
        }
    }
    if(b == CONTROL_ETX || retries >= 3)
      break;
  }
  return receivedFrames;
}


public List<ResponseFrame> receiveConnectResponse() {
  List<ResponseFrame> receivedFrames = new ArrayList<ResponseFrame>();
  ByteArrayOutputStream receivedBytes = new ByteArrayOutputStream();
  byte b = 0x00;
  boolean transmit = false;
  // Find the start of the sequence of frames
  comChannel.startReading();
  while (comChannel.available() > 0) {
    b = (byte)comChannel.read();
    if (b == -1) {
        IOException ioe = new IOException("End of stream when reading from device");
        throw ConnectionCommunicationException.unexpectedIOException(ioe);
    }
    
    if(b == CONTROL_STX) { //start of msg
      transmit = true;
      receivedBytes = new ByteArrayOutputStream();
    } else {
      if (transmit) {
        if (b != CONTROL_ETX) {
          receivedBytes.write(b);
        } else {
            transmit = false;
            ResponseFrame response = new ResponseFrame(receivedBytes.toByteArray(), lastCommandSent);
            receivedFrames.add(response); 
        }
      }
    }
  }
return receivedFrames;
}

public ResponseFrame receiveUnformattedResponse() {
  ByteArrayOutputStream receivedBytes = new ByteArrayOutputStream();
  byte b = 0x00;
  comChannel.startReading();
  while (comChannel.available() > 0) {
    b = (byte)comChannel.read();
//    if (b == -1) {
//        IOException ioe = new IOException("End of stream when reading from device");
//        throw ConnectionCommunicationException.unexpectedIOException(ioe);
//    }
    receivedBytes.write(b);
  }
  return new ResponseFrame(receivedBytes.toByteArray(), lastCommandSent);
}

private boolean checkCrc(byte[] receivedBytes, byte[] crc) {
    if (arraysEqual(receivedBytes, crc)) {
        return true;
    } else {
        return false;
    }
}

private byte[] readCrc() {
    // Read the next 4 bytes from the InputStream
    byte[] crcBytes = new byte[CRC_LENGTH];
    comChannel.startReading();
    int bytesRead = comChannel.read(crcBytes);
    if (bytesRead != CRC_LENGTH) {
        IOException ioe = new IOException("Failed to read CRC from input stream");
        // TODO: is this the right exception?
        throw CommunicationException.unexpectedResponse(ioe);
    }
    return crcBytes;
}


public void sendSpecialCommand(byte b) {
    comChannel.startWriting();
    comChannel.write(b);
}

public byte receiveSpecialCommand() {
    comChannel.startReading();
    return (byte)comChannel.read();
}

public void doConnect() {
  lastCommandSent = COMMAND_SN;
  while (!connected) {
    // Wait 1 second
    try {
        Thread.sleep(1000);
    } catch (InterruptedException ie) {
        throw ConnectionCommunicationException.protocolConnectFailed(ie);
    }

    // verify established communication response
    receiveConnectResponse();
    // send ACC to enable Access Level 1
    sendSELASCIICommand(COMMAND_ACC);
    delay();
    receiveResponse();
    // send password
    ResponseFrame signOnResponse = sendSignOn();
    if (getString(signOnResponse.getData().getCommandCode()).contains(ACC1)) {
        connected = true;
        break;
    } else {
        IOException ioe = new IOException(signOnResponse.getError());
        throw ConnectionCommunicationException.protocolConnectFailed(ioe);
    }
  }
}

private void delay() {
  try {
    Thread.sleep(2000);
  } catch (InterruptedException ie) {
      throw ConnectionCommunicationException.protocolConnectFailed(ie);
  }
}

public void doDisconnect() {
    ResponseFrame signOffResponse = sendSignOff();
    connected = false;
    if (!signOffResponse.isOK()) {
        throw CommunicationException.protocolDisconnectFailed(new IOException(signOffResponse.getError()));
    }
}

public ResponseFrame readMultipleRegisterValues(List<String> registers) {
    ByteArrayOutputStream requestParams = new ByteArrayOutputStream();
    requestParams.write(CONTROL_STX);
    int count = 0;
    for (String str : registers) {
        try {
            requestParams.write(getBytes(str));
        } catch (IOException ioe) {
            // TODO: is this the correct type of exception?
            throw ConnectionCommunicationException.unexpectedIOException(ioe);
        }
        if (count < registers.size() - 1) {
            requestParams.write(',');
            count++;
        }
    }
    ExtendedData data = new ExtendedData(getBytes(COMMAND_RG), requestParams.toByteArray());
    RequestFrame toSend = new RequestFrame(data);
    lastCommandSent = COMMAND_RG;
    return sendAndReceiveFrame(toSend);
}

//public ResponseFrame readSingleRegisterValue(String register) {
//    ByteArrayOutputStream requestParams = new ByteArrayOutputStream();
//    //requestParams.write(CONTROL_STX);
//
//    try {
//        requestParams.write(getBytes(register));
//    } catch (IOException e) {
//        // TODO
//        e.printStackTrace();
//    }
//    ExtendedData data = new ExtendedData(getBytes(COMMAND_RD), requestParams.toByteArray());
//
//    RequestFrame toSend = new RequestFrame(data);
//    lastCommandSent = COMMAND_RD;
//    return sendAndReceiveFrame(toSend);
//}

public ResponseFrame readSingleRegisterValue(String command) {
  ByteArrayOutputStream requestParams = new ByteArrayOutputStream();

  try {
      requestParams.write(getBytes(command));
  } catch (IOException e) {
      // TODO
      e.printStackTrace();
  }
  BasicData data = new BasicData(requestParams.toByteArray());

  RequestFrame toSend = new RequestFrame(data);
  lastCommandSent = command;
  return sendAndReceiveFrame(toSend);
}

public List<CollectedRegister> readRegisters(List<OfflineRegister> list) {
  List<CollectedRegister> retVal = new ArrayList<CollectedRegister>();
  Unit unit = null;
  String description = null;
  String description2 = null;
  String strUnit = null;
  String strDirection = null;
  Date eventDate = null;
  ByteArrayOutputStream requestParams = new ByteArrayOutputStream();
  try {
      requestParams.write(getBytes(COMMAND_REG));
  } catch (IOException e) {
      // TODO
      e.printStackTrace();
  }
  BasicData data = new BasicData(requestParams.toByteArray());

  RequestFrame toSend = new RequestFrame(data);
  lastCommandSent = COMMAND_REG;
  ResponseFrame response = sendAndReceiveFrame(toSend);
  String strResponse = ((RegisterReadResponseData)response.getData()).getValue();
  RegisterParser regParser = new RegisterParser();
  List<RegisterData> registerDatas = regParser.parse(strResponse);
  
  for (int i = 0; i < list.size(); i++) {
    String obisCode = list.get(i).getObisCode().getValue();
    if (obisCode.equals(ObisCodeMapper.OBIS_KWH_DELIVERED)) {
      description = "W DEL";
      description2 = "IN";
      strUnit = "Wh";
      strDirection = OBJECT_DIRECTION_DELIVERED;
    } else if (obisCode.equals(ObisCodeMapper.OBIS_KVARH_DELIVERED)) {
      description = "Q DEL";
      description2 = "IN";
      strUnit = "VARh";
      strDirection = OBJECT_DIRECTION_DELIVERED;
    } else if (obisCode.equals(ObisCodeMapper.OBIS_KVARH_RECEIVED)) {
      description = "OUT";
      strUnit = "VARh";
      strDirection = OBJECT_DIRECTION_RECEIVED;
    } else if (obisCode.equals(ObisCodeMapper.OBIS_KWH_RECEIVED)) {
      description = "OUT";
      strUnit = "Wh";
      strDirection = OBJECT_DIRECTION_RECEIVED;
    } else {
        //TODO: unsupported register
    }
    
    //get register data that matches unit and direction
    RegisterData desiredRegister = null;
    for(RegisterData registerData : registerDatas) {
      if(registerData.getUnit().contains(strUnit) && (registerData.getDescription().equals(description) || registerData.getDescription().equals(description2)) && registerData.getDirection().equals(strDirection)){
        desiredRegister = registerData;
        UnitMapper.setEnergyUnits(registerData.getUnit(), logger);
        unit = UnitMapper.getEnergyUnits();
        break;
      }
    }

    RegisterIdentifier registerIdentifier = new RegisterIdentifierById(list.get(i).getRegisterId(), list.get(i).getObisCode());
    CollectedRegister register = new DefaultDeviceRegister(registerIdentifier);
    retVal.add(register);
    
    if(desiredRegister != null) {
      Quantity quantity = new Quantity(desiredRegister.getBucket("3P").getValue(), unit);
      register.setCollectedData(quantity);
      register.setReadTime(desiredRegister.getTimestamp());
    }
  }
  return retVal;
}


public LDPData readLoadProfileData(LoadProfileReader lpr, int intervalLength) {
  //FILE READ ldp_data.bin MM/DD/YYYY HH:MM:SS MM/DD/YYYY HH:MM:SS

    LDPData results = null;
    Date start = lpr.getStartReadingTime();
    if (start != null) { //read the previous interval in order to calculate the COI for meters that record EOI
      start = getEOIPreviousIntervalDate(lpr.getStartReadingTime(), intervalLength);
    }
    sendSELASCIICommand(getLoadProfileCommand(start, lpr.getEndReadingTime()));
    delay();
    ResponseFrame response = receiveUnformattedResponse();
    if (getString(response.getData().getCommandCode()).contains("Ready to send file")) {
      sendSpecialCommand(CONTROL_CRC);
      String yFile = "LDP_DATA.BIN";
      YModem yModem = new YModem(comChannel);
      try {
        yModem.receiveFileName();
        yModem.receive(yFile);
        File lpFile = new File(yFile);
        LDPParser ldpParser = new LDPParser();
        results = ldpParser.parseYModemFile(new DataInputStream(new FileInputStream(lpFile))); 
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } else {
      throw CommunicationException.unexpectedResponse(new IOException("Unexpected response: " + getString(response.getData().getCommandCode()) + ", expecting Ready to send file"));
    }
    return results;
}

public LDPData readLoadProfileConfig() {
  LDPData results = null;
  sendSELASCIICommand("FILE READ ldp_data.bin");
  lastCommandSent = COMMAND_LP;
  delay();
  ResponseFrame response = receiveUnformattedResponse();
  if (getString(response.getData().getCommandCode()).contains("Ready to send file")) {
    sendSpecialCommand(CONTROL_CRC);
    String yFile = "LDP_DATA.BIN";
    YModem yModem = new YModem(comChannel);
    try {
      yModem.receiveFileName();
      yModem.receive(yFile);
      File lpFile = new File(yFile);
      LDPParser ldpParser = new LDPParser();
      results = ldpParser.parseYModemFile(new DataInputStream(new FileInputStream(lpFile)));  
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  } else {
    throw CommunicationException.unexpectedResponse(new IOException("Unexpected response: " + getString(response.getData().getCommandCode()) + ", expecting Ready to send file"));
  }
  return results;
}


private String getLoadProfileCommand(Date start, Date end) {
  if(end == null) {
    end = getEndDate();
  }
  if(start == null) { 
    start = getStartDate();
  }
  DateFormat df = new SimpleDateFormat(LDP_DATE_FORMAT);
  StringBuilder sb = new StringBuilder("FILE READ");
  sb.append(" ldp_data.bin ");
  sb.append(df.format(start));
  sb.append(" " + df.format(end));
  return sb.toString();
}

private Date getEndDate() {
  Calendar now = Calendar.getInstance();
  //now.set(Calendar.HOUR_OF_DAY, 0);
  //now.set(Calendar.MINUTE, 0);
  //now.set(Calendar.SECOND, 0);
  return now.getTime();
}

private Date getStartDate() {
  Calendar now = Calendar.getInstance();
  //now.set(Calendar.HOUR_OF_DAY, 0);
  //now.set(Calendar.MINUTE, 0);
  //now.set(Calendar.SECOND, 0);
  now.add(Calendar.DATE, -5);
  return now.getTime();
}

private Date getEOIPreviousIntervalDate(Date start, int intervalLength) {
  Calendar cal = Calendar.getInstance();
  cal.setTime(start);
  cal.add(Calendar.SECOND, -intervalLength);
  return cal.getTime();
}

public ResponseFrame writeSingleRegisterValue(String register, String toWrite) {
    ByteArrayOutputStream requestParams = new ByteArrayOutputStream();
    try {
        requestParams.write(',');
        requestParams.write(getBytes(properties.getDevicePassword()));
        requestParams.write(CONTROL_STX);
        requestParams.write(getBytes(register));
        requestParams.write(',');
        requestParams.write(getBytes(toWrite));
    } catch (IOException ioe) {
        throw DataParseException.ioException(ioe);
    }

    ExtendedData data = new ExtendedData(getBytes(COMMAND_WD), requestParams.toByteArray());
    RequestFrame toSend = new RequestFrame(data);
    lastCommandSent = COMMAND_WD;
    return sendAndReceiveFrame(toSend);
}

}
