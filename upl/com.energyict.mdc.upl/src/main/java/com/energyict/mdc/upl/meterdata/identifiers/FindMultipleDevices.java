package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.mdc.upl.meterdata.Device;

/**
 * Provides additional identification services for {@link Device}s
 * for identifier types that are not guaranteed to be unique
 * and may therefore return multiple Devices.
 * <p>
 * Copyrights EnergyICT
 * Date: 9/25/13
 * Time: 11:05 AM
 */
public interface FindMultipleDevices extends DeviceIdentifier {
}