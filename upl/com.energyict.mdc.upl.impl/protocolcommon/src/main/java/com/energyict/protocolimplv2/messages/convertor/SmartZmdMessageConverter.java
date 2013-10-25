package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.Code;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.*;

import javax.xml.parsers.ParserConfigurationException;
import java.math.BigDecimal;
import java.util.*;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Represents a MessageConverter for the smart ZMD protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class SmartZmdMessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {

        // dst related
        registry.put(ConfigurationChangeDeviceMessage.EnableOrDisableDST, new EnableOrDisableDSTMessageEntry(enableDSTAttributeName));
        registry.put(ConfigurationChangeDeviceMessage.SetEndOfDST, new SetEndOfDSTMessageEntry(month, dayOfMonth, dayOfWeek, hour));
        registry.put(ConfigurationChangeDeviceMessage.SetStartOfDST, new SetStartOfDSTMessageEntry(month, dayOfMonth, dayOfWeek, hour));

        //Code table related
        registry.put(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME, new TimeOfUseMessageEntry(activityCalendarNameAttributeName, activityCalendarActivationDateAttributeName, activityCalendarCodeTableAttributeName));

        // reset messages
        registry.put(DeviceActionMessage.DEMAND_RESET, new DemandResetMessageEntry());
    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public SmartZmdMessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(enableDSTAttributeName)) {
            return ((Boolean) messageAttribute) ? "1" : "0";     //1: true, 0: false
        } else if (propertySpec.getName().equals(month)
                || propertySpec.getName().equals(dayOfMonth)
                || propertySpec.getName().equals(dayOfWeek)
                || propertySpec.getName().equals(hour)) {
            return String.valueOf(((BigDecimal) messageAttribute).intValue());
        } else if (propertySpec.getName().equals(activityCalendarNameAttributeName)) {
            return messageAttribute.toString();
        } else if (propertySpec.getName().equals(activityCalendarActivationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime()); //Millis since 1970
        } else if (propertySpec.getName().equals(activityCalendarCodeTableAttributeName)) {
            Code codeTable = (Code) messageAttribute;
            return String.valueOf(codeTable.getId()) + TimeOfUseMessageEntry.SEPARATOR + encode(codeTable); //The ID and the XML representation of the code table, separated by a |
        }
        return EMPTY_FORMAT;
    }

    /**
     * Return an XML representation of the code table.
     * The activation date and calendar name are set to 0, because they were stored in different message attributes.
     */
    private String encode(Code messageAttribute) {
        try {
            return CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(messageAttribute, 0, "0");
        } catch (ParserConfigurationException e) {
            throw MdcManager.getComServerExceptionFactory().createGeneralParseException(e);
        }
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}