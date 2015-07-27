package com.energyict.mdc.device.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.masterdata.RegisterType;

import com.elster.jupiter.validation.ValidationRule;

import java.time.Instant;
import java.util.List;

/**
 * Models the specification of a register.
 *
 * @author Geert
 */
@ProviderType
public interface RegisterSpec extends HasId {

    /**
     * Return the spec's <code>DeviceConfiguration</code>
     *
     * @return the DeviceConfiguration
     */
    public DeviceConfiguration getDeviceConfiguration();


    /**
     * Returns the register mapping for this spec
     *
     * @return the register mapping
     */
    public RegisterType getRegisterType();

    void setRegisterType(RegisterType registerType);

    /**
     * Tests if this RegisterSpec was marked by the user to contain textual data.
     * When this returns <code>true</code>, it will be safe to cast to {@link TextualRegisterSpec}.
     * I all other cases, it will be safe to cast to {@link NumericalRegisterSpec}.
     * <br>
     * Note that {@link com.elster.jupiter.metering.ReadingType}
     * has no flag or other indication that the data is textual.
     *
     * @return <code>true</code> iff the user indicates registers of this specification will contain textual data
     */
    public boolean isTextual();

    /**
     * Returns the spec's unit
     *
     * @return the unit
     */
    public Unit getUnit();

    /**
     * Returns the spec's obis code
     *
     * @return the obis code
     */
    public ObisCode getObisCode();

    public void setOverruledObisCode(ObisCode overruledObisCode);

    /**
     * Returns the obis code of the device.
     *
     * @return the obis code of the device
     */
    public ObisCode getDeviceObisCode();

    /**
     * Returns the receiver's last modification date
     *
     * @return the last modification date
     */
    public Instant getModificationDate();

    void validateDelete();

    void validateUpdate();

    void save();

    public List<ValidationRule> getValidationRules();

    ReadingType getReadingType();
}