package com.energyict.mdc.device.config;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;

/**
 * Represents a LoadProfile specification modeled by a {@link LoadProfileType}
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/11/12
 * Time: 10:06
 */
public interface LoadProfileSpec extends HasId {

    public long getId();

    public LoadProfileType getLoadProfileType();

    public DeviceConfiguration getDeviceConfiguration();

    public ObisCode getDeviceObisCode();

    public ObisCode getObisCode();

    public TimeDuration getInterval();

    public void setLoadProfileType(LoadProfileType loadProfileType);

    public void setOverruledObisCode(ObisCode overruledObisCode);

    public void setDeviceConfiguration(DeviceConfiguration deviceConfiguration);

    void validateDelete();

    void delete();

    void save();

    /**
     * Defines the behavior for a component that is interested
     * to know about the completion of the building
     * process of a LoadProfileSpec.
     */
    public interface BuildingCompletionListener {
        /**
         * Notifies the listener that the building process completed.
         *
         * @param loadProfileSpec The LoadProfileSpec that was completed by the building process
         */
        public void loadProfileSpecBuildingProcessCompleted(LoadProfileSpec loadProfileSpec);

    }

    /**
     * Defines a Builder interface to construct a {@link LoadProfileSpec}
     */
    interface LoadProfileSpecBuilder {

        void notifyOnAdd(BuildingCompletionListener buildingCompletionListener);

        LoadProfileSpecBuilder setOverruledObisCode(ObisCode overruledObisCode);

        /**
         * Does final validation and <i>creates</i> the {@link LoadProfileSpec}
         * @return the LoadProfileSpec
         */
        LoadProfileSpec add();
    }

    /**
     * Defines an <i>update</i> component to update a {@link LoadProfileSpec} implementation
     */
    interface LoadProfileSpecUpdater {

        LoadProfileSpecUpdater setOverruledObisCode(ObisCode overruledObisCode);

        /**
         * Updates the LoadProfileSpec, preferably via his DeviceConfiguration
         */
        void update();
    }
}
