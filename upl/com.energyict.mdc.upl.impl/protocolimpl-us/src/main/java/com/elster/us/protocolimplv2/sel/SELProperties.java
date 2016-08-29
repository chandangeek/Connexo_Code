package com.elster.us.protocolimplv2.sel;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SELProperties implements ConfigurationSupport {
  
  public final static String TIMEOUT = "tmo";
  public final static String RETRIES = "Retries";
  public final static String DEVICE_PWD = "Password";

  private final static int DEFAULT_TIMEOUT = 5000;
  private final static int DEFAULT_RETRIES = 3;
  private final static String DEFAULT_DEVICE_PWD = "SEL";
  private final static String DEFAULT_DEVICE_ID = "99";
  
  private TypedProperties properties;
  
  public SELProperties() {
    this(TypedProperties.empty());
  }

  public SELProperties(TypedProperties properties) {
    this.properties = properties;
  }
  
  public int getTimeout() {
    return properties.getIntegerProperty(TIMEOUT, new BigDecimal(DEFAULT_TIMEOUT)).intValue();
  }

  public int getRetries() {
    return properties.getIntegerProperty(RETRIES, new BigDecimal(DEFAULT_RETRIES)).intValue();
  }

  public String getDevicePassword() {
    String retVal = properties.getStringProperty(DEVICE_PWD);
    if (retVal == null) {
        retVal = DEFAULT_DEVICE_PWD;
    }
    return retVal;
  }

  public String getDeviceId() {
      // TODO: where is "DevideId" defined?
      String retVal = properties.getStringProperty("DevideId");
      if (retVal == null) {
          retVal = DEFAULT_DEVICE_ID;
      }
      return retVal;
  }

  public String getSerialNumber() {
      // TODO: where is "SerialNumber" defined?
      return properties.getStringProperty("SerialNumber");
  }
  
  public void setAllProperties(TypedProperties other) {
      properties.setAllProperties(other);
  }
  
  
  @Override
  public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("SELProperties").append(" = {\r\n");
      sb.append("getTimeout").append(" = {\r\n");
      sb.append("getRetries").append(" = {\r\n");
      sb.append("getDevicePassword").append(" = {\r\n");
      sb.append("getDeviceId").append(" = {\r\n");
      sb.append("getSerialNumber").append(" = {\r\n");
      return sb.toString();
  }

  @Override
  public List<PropertySpec> getRequiredProperties() {
      List<PropertySpec> retVal = new ArrayList<>();

      retVal.add(PropertySpecFactory.bigDecimalPropertySpec(TIMEOUT));
      retVal.add(PropertySpecFactory.bigDecimalPropertySpec(RETRIES));
      retVal.add(PropertySpecFactory.stringPropertySpec(DEVICE_PWD));
      return retVal;
  }

  @Override
  public List<PropertySpec> getOptionalProperties() {
      return Collections.EMPTY_LIST;
  }

}
