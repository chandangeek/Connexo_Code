package com.energyict.mdc.upl;

import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.security.DeviceSecuritySupport;
import com.energyict.mdc.upl.tasks.support.DeviceBasicSupport;
import com.energyict.mdc.upl.tasks.support.DeviceClockSupport;
import com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;
import com.energyict.mdc.upl.tasks.support.DeviceStatusInformationSupport;
import com.energyict.mdc.upl.tasks.support.DeviceTopologySupport;

/**
 * Defines an Interface between the Data Collection System and a Device.
 * The interface can both be used at operational time and at configuration time.
 * <p>
 * Note that this is the current and preferred interface and that
 * {@link MeterProtocol} and {@link SmartMeterProtocol} are
 * legacy interfaces and are in de-facto deprecated.
 * </p>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-14 (10:04)
 */
public interface DeviceProtocol
        extends HasDynamicProperties, DeviceProtocolDialectSupport,
                DeviceBasicSupport, DeviceAccessSupport, DeviceClockSupport,
                DeviceLoadProfileSupport, DeviceRegisterSupport, DeviceLogBookSupport,
                DeviceStatusInformationSupport, DeviceMessageSupport, DeviceSecuritySupport,
                DeviceTopologySupport, DeviceCachingSupport, DeviceDescriptionSupport {

    /**
     * Gets the implementation version.
     *
     * @return The version
     */
    String getVersion();

}