package com.energyict.mdc.upl.migration;

import com.energyict.mdc.upl.properties.TypedProperties;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/01/2015 - 14:48
 */
public interface MigrateFromV1Protocol {

    /**
     * Requests to format the given TypedProperties to a proper form which can be used/understand by the DeviceProtocol.<br/>
     * The provided TypedProperties should be converted, so that the value of properties is converted to correct object type -
     * which should be extracted from the PropertySpecs of the DeviceProtocol (both general and dialect PropertySpecs should be taken into account).<br/>
     * <b>Remarks:</b><br/>
     * During this conversion only specific properties should be migrated. The provided TypedProperties are already partially formatted by ProtocolPropertiesMapper#formatLegacyProperties().
     * In general, properties of following types are already converted & should not be touched, unless you want to overwrite default conversion:
     * <ul>
     * <li>String</li>
     * <li>Boolean</li>
     * <li>BigDecimal</li>
     * <li>TimeDuration</li>
     * <li>HexString</li>
     * <li>TimeZoneInUse</li>
     * </ul>
     * If the property value could not be converted, the property should be dropped.
     *
     * @param legacyProperties The list of legacy TypedProperties
     * @return The updated list of TypedProperties, which now should be conform the PropertySpec definitions of the v2Protocol
     */
    TypedProperties formatLegacyProperties(TypedProperties legacyProperties);

}