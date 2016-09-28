package com.elster.us.protocolimplv2.sel;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.tasks.DeviceProtocolDialect;

public class SELSerialDeviceProtocolDialect implements DeviceProtocolDialect {
  
  //public static final String RETRIES = "Retries";
  public static final String TIMEOUT = "tmo";
  //public final static String ROUND_TRIP_CORRECTION = "RoundTripCorrection";
  //public static final String ADDRESSING_MODE = "AddresssingMode";
  //public static final String INFORMATION_FIELD_SIZE = "InformationFieldSize";

  public static final int DEFAULT_TIMEOUT = 30;
  //public final static int DEFAULT_ROUND_TRIP_CORRECTION = 0;
  //public static final int DEFAULT_RETRIES = 3;
  
  @Override
  public String getDeviceProtocolDialectName() {
      return "SerialModemSELDialect";
  }

  @Override
  public String getDisplayName() {
      return "Serial SEL 734-735";
  }

  @Override
  public List<PropertySpec> getRequiredProperties() {
      return Collections.emptyList();
  }

  @Override
  public List<PropertySpec> getOptionalProperties() {
      return Arrays.asList(
              //this.addressingModePropertySpec(),
              //this.informationFieldSizePropertySpec(),
              this.timeoutPropertySpec()
              //this.retriesPropertySpec(),
              //this.roundTripCorrectionPropertySpec()
      );
  }

//  private PropertySpec addressingModePropertySpec() {
//      return PropertySpecFactory.bigDecimalPropertySpecWithValues(BigDecimal.valueOf(2), ADDRESSING_MODE, BigDecimal.valueOf(1), BigDecimal.valueOf(2), BigDecimal.valueOf(4));
//  }
//
//  private PropertySpec informationFieldSizePropertySpec() {
//      return PropertySpecFactory.bigDecimalPropertySpec(INFORMATION_FIELD_SIZE, BigDecimal.valueOf(128));
//  }
//
//  private PropertySpec retriesPropertySpec() {
//      return PropertySpecFactory.bigDecimalPropertySpec(RETRIES, BigDecimal.valueOf(DEFAULT_RETRIES));
//  }

  private PropertySpec timeoutPropertySpec() {
      return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(TIMEOUT, new TimeDuration(DEFAULT_TIMEOUT));
  }

//  private PropertySpec roundTripCorrectionPropertySpec() {
//      return PropertySpecFactory.bigDecimalPropertySpec(ROUND_TRIP_CORRECTION, BigDecimal.valueOf(DEFAULT_ROUND_TRIP_CORRECTION));
//  }

//  @Override
//  public PropertySpec getPropertySpec(String name) {
//      switch (name) {
//          case ADDRESSING_MODE:
//              return this.addressingModePropertySpec();
//          case INFORMATION_FIELD_SIZE:
//              return this.informationFieldSizePropertySpec();
//          case RETRIES:
//              return this.retriesPropertySpec();
//          case TIMEOUT:
//              return this.timeoutPropertySpec();
//          case ROUND_TRIP_CORRECTION:
//              return this.roundTripCorrectionPropertySpec();
//          default:
//              return null;
//      }
//  }
  
  @Override
  public PropertySpec getPropertySpec(String name) {
      switch (name) {
          case TIMEOUT:
              return this.timeoutPropertySpec();
          default:
              return null;
      }
  }

  @Override
  public boolean isRequiredProperty(String name) {
      for (PropertySpec propertySpec : getRequiredProperties()) {
          if (propertySpec.getName().equalsIgnoreCase(name)) {
              return true;
          }
      }
      return false;
  }

}
