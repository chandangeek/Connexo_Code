package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.messages;

import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;

import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessaging;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 26/02/13 - 12:06
 */
public class XemexMbusMessaging extends Dsmr23MbusMessaging {

    @Override
    public List getMessageCategories() {
        return new ArrayList<MessageCategorySpec>();
    }
}
