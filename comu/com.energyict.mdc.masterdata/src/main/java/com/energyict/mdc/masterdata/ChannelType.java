package com.energyict.mdc.masterdata;

import com.energyict.mdc.common.TimeDuration;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 7/15/14
 * Time: 9:53 AM
 */
public interface ChannelType extends MeasurementType {

    TimeDuration getInterval();

    RegisterType getTemplateRegister();
}
