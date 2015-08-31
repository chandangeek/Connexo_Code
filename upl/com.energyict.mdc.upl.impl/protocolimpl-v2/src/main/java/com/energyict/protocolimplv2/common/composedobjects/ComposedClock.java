package com.energyict.protocolimplv2.common.composedobjects;

import com.energyict.dlms.DLMSAttribute;

/**
 * The ComposedClock is just a ValueObject that holds the {@link DLMSAttribute} from a Clock object
 * @author sva
 * @since 31/08/2015 - 16:07
 */
public class ComposedClock implements ComposedObject {

    private final DLMSAttribute timeAttribute;


    public ComposedClock(DLMSAttribute timeAttribute) {
        this.timeAttribute = timeAttribute;
    }

    public DLMSAttribute getTimeAttribute() {
        return timeAttribute;
    }
}