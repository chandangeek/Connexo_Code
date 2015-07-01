package com.energyict.mdc.device.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.validation.ValidationRule;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.ObisCode;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.masterdata.LoadProfileType;

import java.util.List;

/**
 * Represents a LoadProfile specification modeled by a {@link LoadProfileType}
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/11/12
 * Time: 10:06
 */
@ProviderType
public interface LoadProfileSpec extends HasId {

    public long getId();

    public LoadProfileType getLoadProfileType();

    public DeviceConfiguration getDeviceConfiguration();

    public ObisCode getDeviceObisCode();

    public ObisCode getObisCode();

    public TimeDuration getInterval();

    public void setOverruledObisCode(ObisCode overruledObisCode);

    void validateDelete();

    void delete();

    void save();

    public List<ValidationRule> getValidationRules();

    public List<ChannelSpec> getChannelSpecs();

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
