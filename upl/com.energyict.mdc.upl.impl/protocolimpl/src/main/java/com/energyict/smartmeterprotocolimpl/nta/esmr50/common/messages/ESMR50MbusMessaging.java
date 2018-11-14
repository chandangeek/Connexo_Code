package com.energyict.smartmeterprotocolimpl.nta.esmr50.common.messages;

import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocolimpl.messages.RtuMessageCategoryConstants;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.messages.RtuMessageKeyIdConstants;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessaging;

import java.util.List;
@Deprecated
public class ESMR50MbusMessaging extends Dsmr23MbusMessaging implements MessageProtocol{

    public List getMessageCategories() {

        List<MessageCategorySpec> categories = super.getMessageCategories();

        MessageCategorySpec catMBusFirmwareUpgrade = getFirmwareUpgradeCategory();

        categories.add(catMBusFirmwareUpgrade);
        return categories;
    }

    public MessageCategorySpec getFirmwareUpgradeCategory() {
        MessageCategorySpec catFirmware = new MessageCategorySpec(RtuMessageCategoryConstants.FIRMWARE);
        MessageSpec msgSpec = addFirmwareMsg(RtuMessageKeyIdConstants.FIRMWARE, RtuMessageConstant.FIRMWARE_UPGRADE, false, true);
        catFirmware.addMessageSpec(msgSpec);
        return catFirmware;
    }


}
