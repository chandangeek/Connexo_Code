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
  public final static String MAX_INTERVAL_RETRIEVAL_IN_DAYS = "MaxIntervalRetrievalInDays";
  public final static String LP_RECORDER = "LoadProfileRecorder"; //supported types are COI(Change-Over-Interval) or EOI(End-Of-Interval)
  public final static String LEVEL_E_PWD = "LevelEPassword";

  private final static String DEFAULT_DEVICE_TIMEZONE = TimeZone.getDefault().getID();
  private final static String DEFAULT_TIMEZONE = TimeZone.getDefault().getID();;
  private final static int DEFAULT_RETRIES = 3;
  private final static String DEFAULT_DEVICE_PWD = "SEL";
  private final static int DEFAULT_MAX_INTERVAL_RETRIEVAL_IN_DAYS = 7;
  private final static String DEFAULT_LP_RECORDER = "COI"; //supported types are COI(Change-Over-Interval) or EOI(End-Of-Interval)
  private final static String DEFAULT_LEVEL_E_PWD = "BLONDEL";

  private TypedProperties properties;

  public SELProperties() {
    this(TypedProperties.empty());
  }

  public SELProperties(TypedProperties properties) {
    this.properties = properties;
  }


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

  public String getLevelEPassword() {
    String retVal = properties.getStringProperty(LEVEL_E_PWD);
    if (retVal == null || retVal.isEmpty()) {
      retVal = DEFAULT_LEVEL_E_PWD;
    }
    return retVal;
  }

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

  public int getMaxIntervalRetrievalInDays() {
    try {
      return properties.getIntegerProperty(MAX_INTERVAL_RETRIEVAL_IN_DAYS, new BigDecimal(DEFAULT_MAX_INTERVAL_RETRIEVAL_IN_DAYS)).toBigInteger().intValue();
    } catch (Throwable t) {
      return DEFAULT_MAX_INTERVAL_RETRIEVAL_IN_DAYS;
    }
  }

  public String getLPRecorder() {
    String retVal = properties.getStringProperty(LP_RECORDER);
    if(!retVal.equalsIgnoreCase("EOI")) {
      retVal = DEFAULT_LP_RECORDER;
    } else {
      retVal = "EOI";
    }
    return retVal;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("SELProperties").append(" = {\r\n");
    sb.append("getRetries").append(" = {\r\n");
    sb.append("getTimezone").append(" = {\r\n");
    sb.append("getDeviceTimezone").append(" = {\r\n");
    sb.append("getMaxIntervalRetrievalInDays").append(" = {\r\n");
    sb.append("getLPRecorder").append(" = {\r\n");
    sb.append("getLevelERecorder").append(" = {\r\n");
    sb.append("").append(" = {\r\n");
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
    retVal.add(PropertySpecFactory.bigDecimalPropertySpec(MAX_INTERVAL_RETRIEVAL_IN_DAYS));
    retVal.add(PropertySpecFactory.stringPropertySpec(LP_RECORDER));
    retVal.add(PropertySpecFactory.stringPropertySpec(LEVEL_E_PWD));
    return retVal;
  }

}
