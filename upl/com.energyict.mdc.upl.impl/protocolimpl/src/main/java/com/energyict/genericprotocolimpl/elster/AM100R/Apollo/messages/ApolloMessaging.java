package com.energyict.genericprotocolimpl.elster.AM100R.Apollo.messages;

import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.genericprotocolimpl.elster.AM100R.Apollo.ApolloMeter;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.dlms.as220.parsing.CodeTableXml;
import com.energyict.protocolimpl.messages.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains functionality to perfom message handling for the {@link com.energyict.genericprotocolimpl.elster.AM100R.Apollo.ApolloMeter}
 * <p/>
 * <pre>
 * Copyrights EnergyICT
 * Date: 16-mrt-2011
 * Time: 16:52:48
 * </pre>
 */
public class ApolloMessaging extends GenericMessaging {

    private final ApolloMeter protocol;

    public ApolloMessaging(final ApolloMeter protocol) {
        this.protocol = protocol;
    }

    /**
     * Abstract method to define your message categories *
     */
    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList();
        categories.add(getActivityCalendarCategory());
        categories.add(getTestCategory());
        return categories;
    }

    @Override
    public MessageCategorySpec getActivityCalendarCategory() {
        MessageCategorySpec catActivityCal = new MessageCategorySpec(RtuMessageCategoryConstants.ACTICITYCALENDAR);
        MessageSpec msgSpec = addTimeOfUse(RtuMessageKeyIdConstants.ACTIVITYCALENDAR, RtuMessageConstant.TOU_ACTIVITY_CAL, false);
        catActivityCal.addMessageSpec(msgSpec);
        msgSpec = addActivateCalendarMsg(RtuMessageKeyIdConstants.ACTIVATEACTIVITYCALENDAR, RtuMessageConstant.TOU_ACTIVATE_CALENDAR, false);
        catActivityCal.addMessageSpec(msgSpec);
        return catActivityCal;
    }

    /**
     * Creates a MessageSpec to add ActivityCalendar functionality
     *
     * @param keyId    - id for the MessageSpec
     * @param tagName  - name for the MessageSpec
     * @param advanced - indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    @Override
    protected MessageSpec addTimeOfUse(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(msgVal);
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
                RtuMessageConstant.TOU_ACTIVITY_DATE, false);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(
                RtuMessageConstant.TOU_ACTIVITY_CODE_TABLE, true);
        tagSpec.add(msgAttrSpec);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    /**
     * Creates a MessageSpec to add an Activate a passive ActivityCalendar
     *
     * @param keyId    the id for the MessageSpec
     * @param tagName  the name for the MessageSpec
     * @param advanced indicates whether it's an advanced message or not
     * @return the newly created MessageSpec
     */
    protected MessageSpec addActivateCalendarMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(msgVal);
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(
                RtuMessageConstant.TOU_ACTIVITY_DATE, false);
        tagSpec.add(msgAttrSpec);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    @Override
    public String writeTag(final MessageTag msgTag) {
        if (msgTag.getName().equals(RtuMessageConstant.TOU_ACTIVITY_CAL)) {

            StringBuilder builder = new StringBuilder();
            addOpeningTag(builder, msgTag.getName());
            long activationDate = 0;
            int codeTableId = -1;
            for (Object maObject : msgTag.getAttributes()) {
                MessageAttribute ma = (MessageAttribute) maObject;
                if (ma.getSpec().getName().equals(RtuMessageConstant.TOU_ACTIVITY_DATE)) {
                    if (ma.getValue() != null && ma.getValue().length() != 0) {
                        activationDate = Long.valueOf(ma.getValue());
                    }
                } else if (ma.getSpec().getName().equals(RtuMessageConstant.TOU_ACTIVITY_CODE_TABLE)) {
                    if (ma.getValue() != null && ma.getValue().length() != 0) {
                        codeTableId = Integer.valueOf(ma.getValue());
                    }
                }
            }

            try {
                String xmlContent = CodeTableXml.parseActivityCalendarAndSpecialDayTable(codeTableId, activationDate);
                builder.append(ProtocolTools.compress(xmlContent));
            } catch (ParserConfigurationException e) {
                return null;
            } catch (IOException e) {
                return null;
            }
            addClosingTag(builder, msgTag.getName());
            return builder.toString();
        } else if (msgTag.getName().equals(RtuMessageConstant.TOU_SPECIAL_DAYS)) {

            StringBuilder builder = new StringBuilder();
            addOpeningTag(builder, msgTag.getName());
            int codeTableId = -1;
            for (Object maObject : msgTag.getAttributes()) {
                MessageAttribute ma = (MessageAttribute) maObject;
                if (ma.getSpec().getName().equals(RtuMessageConstant.TOU_SPECIAL_DAYS_CODE_TABLE)) {
                    if (ma.getValue() == null || ma.getValue().length() == 0) {
                        return null;
                    } else {
                        codeTableId = Integer.valueOf(ma.getValue());
                    }
                }
            }

            try {
                String xmlContent = CodeTableXml.parseActivityCalendarAndSpecialDayTable(codeTableId, 0);
                builder.append(ProtocolTools.compress(xmlContent));
            } catch (ParserConfigurationException e) {
                return null;
            } catch (IOException e) {
                return null;
            }
            addClosingTag(builder, msgTag.getName());
            return builder.toString();

        } else {
            return super.writeTag(msgTag);
        }
    }

}
