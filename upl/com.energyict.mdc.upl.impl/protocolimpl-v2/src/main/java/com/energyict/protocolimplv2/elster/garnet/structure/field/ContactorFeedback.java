package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 10:05
 */
public class ContactorFeedback extends AbstractField<ContactorFeedback> {

    public static final int LENGTH = 1;

    private int feedbackCode;

    public ContactorFeedback() {
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromIntLE(feedbackCode, LENGTH);
    }

    @Override
    public ContactorFeedback parse(byte[] rawData, int offset) throws ParsingException {
        feedbackCode = getIntFromBytesLE(rawData, offset, LENGTH);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getFeedbackCode() {
        return feedbackCode;
    }
}