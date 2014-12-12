package com.energyict.protocolimplv2.nta.dsmr23;

import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.mdw.cpo.PropertySpecFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties.*;

/**
 * A collection of general DLMS properties.
 * These properties are not related to the security or the protocol dialects.
 * The parsing and the usage of the property values is done in implementations of {@link com.energyict.dlms.protocolimplv2.DlmsSessionProperties}
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/10/13
 * Time: 15:41
 * Author: khe
 */
public class DlmsConfigurationSupport {
//
//    private static final boolean DEFAULT_VALIDATE_INVOKE_ID = true;
//
////    @Override
////    public List<PropertySpec> getRequiredProperties() {
////        return Collections.emptyList();
////    }
////
////    @Override
////    public List<PropertySpec> getOptionalProperties() {
////        return Arrays.asList(
////                this.forcedDelayPropertySpec(),
////                this.maxRecPduSizePropertySpec(),
////                this.bulkRequestPropertySpec(),
////                this.cipheringTypePropertySpec(),
////                this.ntaSimulationToolPropertySpec(),
////                this.fixMbusHexShortIdPropertySpec(),
////                this.manufacturerPropertySpec(),
////                this.conformanceBlockValuePropertySpec(),
////                this.requestTimeZonePropertySpec(),
////                this.timeZonePropertySpec(),
////                this.validateInvokeIdPropertySpec(),
////                this.deviceId());
////    }
//
//    protected PropertySpec timeZonePropertySpec() {
//        return PropertySpecFactory.timeZoneInUseReferencePropertySpec(TIMEZONE);
//    }
//
//    protected PropertySpec validateInvokeIdPropertySpec() {
//        return PropertySpecFactory.notNullableBooleanPropertySpec(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID);
//    }
//
//    protected PropertySpec deviceId() {
//        return PropertySpecFactory.stringPropertySpec(DEVICE_ID, DEFAULT_DEVICE_ID);
//    }
//
//    protected PropertySpec requestTimeZonePropertySpec() {
//        return PropertySpecFactory.notNullableBooleanPropertySpec(REQUEST_TIMEZONE);
//    }
//
//    protected PropertySpec forcedDelayPropertySpec() {
//        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(
//                FORCED_DELAY,
//                new TimeDuration(DEFAULT_FORCED_DELAY.intValue() / 1000));
//    }
//
//    protected PropertySpec conformanceBlockValuePropertySpec() {
//        return PropertySpecFactory.bigDecimalPropertySpec(CONFORMANCE_BLOCK_VALUE, BigDecimal.valueOf(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK));
//    }
//
//    protected PropertySpec manufacturerPropertySpec() {
//        return PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(MANUFACTURER, DEFAULT_MANUFACTURER, "WKP", "ISK", "LGZ", "SLB", "ActarisPLCC", "SLB::SL7000");
//    }
//
//    protected PropertySpec maxRecPduSizePropertySpec() {
//        return PropertySpecFactory.bigDecimalPropertySpec(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE);
//    }
//
//    protected PropertySpec bulkRequestPropertySpec() {
//        return PropertySpecFactory.notNullableBooleanPropertySpec(BULK_REQUEST);
//    }
//
//    protected PropertySpec cipheringTypePropertySpec() {
//        return PropertySpecFactory.bigDecimalPropertySpec(CIPHERING_TYPE, DEFAULT_CIPHERING_TYPE);
//    }
//
//    protected PropertySpec ntaSimulationToolPropertySpec() {
//        return PropertySpecFactory.notNullableBooleanPropertySpec(NTA_SIMULATION_TOOL);
//    }
//
//    protected PropertySpec fixMbusHexShortIdPropertySpec() {
//        return PropertySpecFactory.notNullableBooleanPropertySpec(FIX_MBUS_HEX_SHORT_ID);
//    }
//
//    @Override
//    public List<PropertySpec> getPropertySpecs() {
//        return null;
//    }
//
//    @Override
//    public PropertySpec getPropertySpec(String s) {
//        return null;
//    }
}
