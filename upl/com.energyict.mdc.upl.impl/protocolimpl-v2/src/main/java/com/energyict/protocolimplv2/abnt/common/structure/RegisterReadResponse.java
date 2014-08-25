package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;
import com.energyict.protocolimplv2.abnt.common.frame.ResponseFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;

import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class RegisterReadResponse extends Data<RegisterReadResponse> {

    private static final Integer[] NULL_DATA_LENGTHS = new Integer[]{10, 54, 10, 54, 10, 83};
    private static final int LENGTH_OF_BCD_ENCODED_TOTALIZER = 5;

    private BcdEncodedField generalTotalChannel1;
    private BcdEncodedField totalAtBillingPointChannel1;
    private BcdEncodedField generalTotalChannel2;
    private BcdEncodedField totalAtBillingPointChannel2;
    private BcdEncodedField generalTotalChannel3;
    private BcdEncodedField totalAtBillingPointChannel3;

    public RegisterReadResponse(TimeZone timeZone) {
        super(ResponseFrame.RESPONSE_DATA_LENGTH, timeZone);
        this.generalTotalChannel1 = new BcdEncodedField(LENGTH_OF_BCD_ENCODED_TOTALIZER);
        this.totalAtBillingPointChannel1 = new BcdEncodedField(LENGTH_OF_BCD_ENCODED_TOTALIZER);
        this.generalTotalChannel2 = new BcdEncodedField(LENGTH_OF_BCD_ENCODED_TOTALIZER);
        this.totalAtBillingPointChannel2 = new BcdEncodedField(LENGTH_OF_BCD_ENCODED_TOTALIZER);
        this.generalTotalChannel3 = new BcdEncodedField(LENGTH_OF_BCD_ENCODED_TOTALIZER);
        this.totalAtBillingPointChannel3 = new BcdEncodedField(LENGTH_OF_BCD_ENCODED_TOTALIZER);
    }

    @Override
    public RegisterReadResponse parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;                           //TODO: warning: BCD encoded text will include '-' sign in case of reverse flow (capacitive energy channels)
        super.parse(rawData, ptr);

        generalTotalChannel1.parse(rawData, ptr);
        ptr += generalTotalChannel1.getLength();

        ptr += NULL_DATA_LENGTHS[0];

        totalAtBillingPointChannel1.parse(rawData, ptr);
        ptr += totalAtBillingPointChannel1.getLength();

        ptr += NULL_DATA_LENGTHS[1];

        generalTotalChannel2.parse(rawData, ptr);
        ptr += generalTotalChannel2.getLength();

        ptr += NULL_DATA_LENGTHS[2];

        totalAtBillingPointChannel2.parse(rawData, ptr);
        ptr += totalAtBillingPointChannel2.getLength();

        ptr += NULL_DATA_LENGTHS[3];

        generalTotalChannel3.parse(rawData, ptr);
        ptr += generalTotalChannel3.getLength();

        ptr += NULL_DATA_LENGTHS[4];

        totalAtBillingPointChannel3.parse(rawData, ptr);
        return this;
    }

    public BcdEncodedField getTotalAtBillingPointChannel1() {
        return totalAtBillingPointChannel1;
    }

    public BcdEncodedField getGeneralTotalChannel1() {
        return generalTotalChannel1;
    }

    public BcdEncodedField getGeneralTotalChannel2() {
        return generalTotalChannel2;
    }

    public BcdEncodedField getTotalAtBillingPointChannel2() {
        return totalAtBillingPointChannel2;
    }

    public BcdEncodedField getGeneralTotalChannel3() {
        return generalTotalChannel3;
    }

    public BcdEncodedField getTotalAtBillingPointChannel3() {
        return totalAtBillingPointChannel3;
    }
}