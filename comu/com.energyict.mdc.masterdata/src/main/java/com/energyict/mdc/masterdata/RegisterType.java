package com.energyict.mdc.masterdata;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 7/16/14
 * Time: 9:35 AM
 */
@ProviderType
public interface RegisterType extends MeasurementType {

    /**
     * Returns the <code>RegisterGroup</code> the receiver belongs to
     *
     * @return the <code>RegisterGroup</code> the receiver belongs to
     */
    public List<RegisterGroup> getRegisterGroups();
}
