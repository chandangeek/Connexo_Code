package com.energyict.mdc.device.config;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;

/**
 * Represents a LoadProfile specification modeled by a {@link LoadProfileType}
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/11/12
 * Time: 10:06
 */
public interface LoadProfileSpec {

    public LoadProfileType getLoadProfileType();

    public DeviceConfiguration getDeviceConfig();

    public ObisCode getDeviceObisCode();

    public ObisCode getObisCode();

    public TimeDuration getInterval();

    public void setLoadProfileType(LoadProfileType loadProfileType);

    public void setOverruledObisCode(ObisCode overruledObisCode);

    public void setDeviceConfiguration(DeviceConfiguration deviceConfiguration);

    public void save ();

    public void delete ();

}
