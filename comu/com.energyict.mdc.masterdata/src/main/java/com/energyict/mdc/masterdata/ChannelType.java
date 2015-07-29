package com.energyict.mdc.masterdata;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.time.TimeDuration;

/**
 * Copyrights EnergyICT
 * Date: 7/15/14
 * Time: 9:53 AM
 */
@ProviderType
public interface ChannelType extends MeasurementType {

    TimeDuration getInterval();

    RegisterType getTemplateRegister();
}
