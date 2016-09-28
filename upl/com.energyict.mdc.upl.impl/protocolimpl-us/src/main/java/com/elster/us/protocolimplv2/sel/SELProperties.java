package com.elster.us.protocolimplv2.sel;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdw.core.TimeZoneInUse;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

public class SELProperties implements ConfigurationSupport {
  
  public final static String DEVICE_TIMEZONE = "deviceTimeZone";
  public final static String TIMEZONE = "Timezone";
  public final static String RETRIES = "Retries";
  public final static String DEVICE_PWD = "Password";

  private final static String DEFAULT_DEVICE_TIMEZONE = TimeZone.getDefault().getID();
  private final static String DEFAULT_TIMEZONE = TimeZone.getDefault().getID();;
  private final static int DEFAULT_RETRIES = 3;
  private final static String DEFAULT_DEVICE_PWD = "SEL";

  private TypedProperties properties;
  
  public SELProperties() {
    this(TypedProperties.empty());
  }

  public SELProperties(TypedProperties properties) {
    this.properties = properties;
  }
  
//  public void setAllProperties(TypedProperties properties) {
//    for (String propertyName : properties.propertyNames()) {
//      this.properties.put(propertyName, properties.getProperty(propertyName));
//    }
//  }
  
  public void setAllProperties(TypedProperties other) {
    properties.setAllProperties(other);
  }

  public String getDevicePassword() {
    String retVal = properties.getStringProperty(DEVICE_PWD);
    if (retVal == null) {
      retVal = DEFAULT_DEVICE_PWD;
    }
    return retVal;
  }
  
//  public String getDeviceTimezone() {
//    try {
//      String deviceTz = (String)properties.get(DEVICE_TIMEZONE);
//      return (deviceTz != null  && !deviceTz.isEmpty()) ? deviceTz : DEFAULT_DEVICE_TIMEZONE;
//    } catch (Throwable t) {
//      return DEFAULT_DEVICE_TIMEZONE;
//    }
//  }
  
  public String getDeviceTimezone() {
    try {
      TimeZoneInUse deviceTz = properties.getTypedProperty(DEVICE_TIMEZONE);
      return (deviceTz != null) ? deviceTz.getTimeZone().getID() : DEFAULT_DEVICE_TIMEZONE;
    } catch (Throwable t) {
      return DEFAULT_DEVICE_TIMEZONE;
    }
  }
  
  public String getTimezone() {
    try {
      String runningTz = properties.getStringProperty(TIMEZONE);
      return (runningTz != null) ? runningTz : DEFAULT_TIMEZONE;
    } catch (Throwable t) {
      return DEFAULT_TIMEZONE;
    }
  }
  
  public int getRetries() {
    try {
      String str = properties.getStringProperty(RETRIES);
      return Integer.parseInt(str);
    } catch (Throwable t) {
      return DEFAULT_RETRIES;
    }
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("SELProperties").append(" = {\r\n");
    sb.append("getRetries").append(" = {\r\n");
    sb.append("getTimezone").append(" = {\r\n");
    sb.append("getDeviceTimezone").append(" = {\r\n");
    return sb.toString();
  }
  
  @Override
  public List<PropertySpec> getRequiredProperties() {
    return Collections.EMPTY_LIST;
  }
  
  @Override
  public List<PropertySpec> getOptionalProperties() {
    List<PropertySpec> retVal = new ArrayList<PropertySpec>();
    //retVal.add(PropertySpecFactory.stringPropertySpec(DEVICE_TIMEZONE));
    retVal.add(PropertySpecFactory.bigDecimalPropertySpec(RETRIES));
    retVal.add(PropertySpecFactory.stringPropertySpec(TIMEZONE));
    retVal.add(PropertySpecFactory.stringPropertySpec(DEVICE_PWD));
    retVal.add(PropertySpecFactory.timeZoneInUseReferencePropertySpec(DEVICE_TIMEZONE));
    return retVal;
  }

}
