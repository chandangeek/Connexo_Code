package com.energyict.mdc.masterdata;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.masterdata.RegisterMapping;

/**
 * Copyrights EnergyICT
 * Date: 7/15/14
 * Time: 9:53 AM
 */
public interface ChannelType extends RegisterMapping {

    TimeDuration getInterval();

    RegisterMapping getTemplateRegister();
}
