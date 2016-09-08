package com.elster.us.protocolimplv2.sel;

import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_ID;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_DATE;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_TIME;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.elster.us.protocolimplv2.sel.utility.DateFormatHelper;
import com.elster.us.protocolimplv2.sel.utility.DateFormatHelper.*;

import com.elster.us.protocolimplv2.sel.frame.ResponseFrame;
import com.elster.us.protocolimplv2.sel.frame.data.DeviceIDReadResponseData;
import com.elster.us.protocolimplv2.sel.frame.data.MultiReadResponseData;
import com.elster.us.protocolimplv2.sel.frame.data.SingleReadResponseData;
import com.elster.us.protocolimplv2.sel.frame.data.TimeReadResponseData;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.channels.serial.modem.rxtx.RxTxAtModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioAtModemConnectionType;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.elster.us.protocolimplv2.sel.profiles.LoadProfileBuilder;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.security.NoOrPasswordSecuritySupport;

import com.elster.us.protocolimplv2.sel.Consts.*;

public class SEL implements DeviceProtocol {
  
  private SELConnection connection;
  private SELProperties properties = new SELProperties();
  private OfflineDevice offlineDevice;
  private SerialPortComChannel comChannel;
  Logger logger = Logger.getLogger(this.getClass().getName());
  private LoadProfileBuilder loadProfileBuilder;
  
  public SELConnection getConnection() {
    return connection;
  }
  
  public SELProperties getProperties() {
    return properties;
  }
  
  public OfflineDevice getOfflineDevice() { return offlineDevice; }
  
  private TimeZone getTimeZone() {
    if(properties.getTimezone() != null)
      return TimeZone.getTimeZone(properties.getTimezone());
    else
      return null;
  }
  
  @Override
  public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
    this.offlineDevice = offlineDevice;
    this.comChannel = (SerialPortComChannel) comChannel;
    connection = new SELConnection((SerialPortComChannel) comChannel, properties, logger);
    
  }
  
  @Override
  public void terminate() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void addProperties(TypedProperties typedProperties) {
    properties.setAllProperties(typedProperties);
  }

  @Override
  public String getVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<PropertySpec> getOptionalProperties() {
    return properties.getOptionalProperties();
  }

  @Override
  public List<PropertySpec> getRequiredProperties() {
    return properties.getRequiredProperties();
  }

  @Override
  public void daisyChainedLogOff() {
    // Not Implemented
  }

  @Override
  public void daisyChainedLogOn() {
    // Not Implemented
    
  }

  @Override
  public void logOff() {
    // no logoff just let commserver send ATH
  }

  @Override
  public void logOn() {
    connection.doConnect();
  }

  @Override
  public void setTime(Date arg0) {
    // Not Implemented
    
  }

  @Override
  public Date getTime() {
    ResponseFrame dateResponse = getConnection().readSingleRegisterValue(COMMAND_DATE);
    SingleReadResponseData data = (SingleReadResponseData)dateResponse.getData();
    String dateStr = data.getValue().trim();
    
    ResponseFrame timeResponse = getConnection().readSingleRegisterValue(COMMAND_TIME);
    TimeReadResponseData timData = (TimeReadResponseData)timeResponse.getData();
    String timeStr = timData.getValue().trim();
    
    Date d = null;
    try {
      d = new SimpleDateFormat("MM/dd/yyHH:mm:ss").parse(dateStr+timeStr);
    } catch (ParseException e1) {
      getLogger().warning("Failed to parse the date from the device: " + dateStr);
    }
    
    SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyHHmmss");
    String str = format.format(d);

    format.setTimeZone(TimeZone.getTimeZone(properties.getDeviceTimezone()));

    Date d1 = null;
    try {
        d1 = format.parse(str);
    } catch (ParseException e) {
        e.printStackTrace();
    }
    
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(properties.getDeviceTimezone()));
    cal.setTime(d1);
    TimeZone tz = getTimeZone(); //Get the timezone that we are running in
    if(tz != null)
      cal.setTimeZone(tz);
    else
      cal.setTimeZone(TimeZone.getDefault());

    return cal.getTime();
  }

  @Override
  public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
    properties.setAllProperties(dialectProperties);
    
  }

  @Override
  public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
    return Arrays.<DeviceProtocolDialect>asList(
        new NoParamsDeviceProtocolDialect());
  }

  @Override
  public String getSerialNumber() {
    ResponseFrame responseFrame = connection.readSingleRegisterValue(COMMAND_ID);
    return ((DeviceIDReadResponseData)responseFrame.getData()).getValue();
  }

  @Override
  public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfiles) {
    return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfiles);
  }

  @Override
  public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
    //return connection.readLoadProfileData(loadProfiles);
    return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
  }

  @Override
  public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
    return connection.readEvents(logBooks);
    //return Collections.emptyList();
  }

  @Override
  public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
    return connection.readRegisters(registers);
  }

  @Override
  public CollectedTopology getDeviceTopology() {
    return MdcManager.getCollectedDataFactory().createCollectedTopology(new DeviceIdentifierById(getOfflineDevice().getId()));
  }

  @Override
  public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String format(OfflineDevice arg0, OfflineDeviceMessage arg1, PropertySpec arg2, Object arg3) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<DeviceMessageSpec> getSupportedMessages() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String prepareMessageContext(OfflineDevice arg0, DeviceMessage arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DeviceProtocolCache getDeviceCache() {
    // Not implemented
    return null;
  }

  @Override
  public void setDeviceCache(DeviceProtocolCache arg0) {
    // Not implemented
    
  }

  @Override
  public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
    properties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
    
  }

  @Override
  public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
    return new NoOrPasswordSecuritySupport().getAuthenticationAccessLevels();
  }

  @Override
  public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
    return new NoOrPasswordSecuritySupport().getEncryptionAccessLevels();
  }

  @Override
  public List<PropertySpec> getSecurityProperties() {
    return new NoOrPasswordSecuritySupport().getSecurityProperties();
  }

  @Override
  public PropertySpec getSecurityPropertySpec(String name) {
    return new NoOrPasswordSecuritySupport().getSecurityPropertySpec(name);
  }

  @Override
  public String getSecurityRelationTypeName() {
    return new NoOrPasswordSecuritySupport().getSecurityRelationTypeName();
  }

  @Override
  public List<ConnectionType> getSupportedConnectionTypes() {
    List<ConnectionType> retVal = new ArrayList<>();
    retVal.add(new SioAtModemConnectionType());
    return retVal;
  }

  @Override
  public String getProtocolDescription() {
    return "SEL 734/735";
  }

  @Override
  public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
    return Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_SESSION);
  }
  
  public Logger getLogger() {
    return logger;
  }
  
  private LoadProfileBuilder getLoadProfileBuilder() {
    if (loadProfileBuilder == null) {
        this.loadProfileBuilder = new LoadProfileBuilder(this);
    }
    return loadProfileBuilder;
}

}
