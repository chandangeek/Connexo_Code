package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;
import com.energyict.protocolimplv2.abnt.common.frame.ResponseFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class InstrumentationPageResponse extends Data<InstrumentationPageResponse> {

    public static final int METER_MODEL_LENGTH = 1;
    public static final int DATE_TIME_LENGTH = 6;
    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HHmmssddMMyy");
    public static final int NOT_SUPPORTED_DATA_LENGTH = 8;

    public static final Unit ANGLE_UNIT = Unit.get(BaseUnit.DEGREE);
    public static final Unit VOLTAGE_UNIT = Unit.get(BaseUnit.VOLT);
    public static final Unit CURRENT_UNIT = Unit.get(BaseUnit.AMPERE);
    public static final Unit ACTIVE_POWER_UNIT = Unit.get(BaseUnit.WATT);
    public static final Unit REACTIVE_POWER_UNIT = Unit.get(BaseUnit.VOLTAMPEREREACTIVE);
    public static final Unit APPARENT_POWER_UNIT = Unit.get(BaseUnit.VOLTAMPERE);
    public static final Unit UNDEFINED_UNIT = Unit.getUndefined();
    public static final Unit TEMPERATURE_UNIT = Unit.get(BaseUnit.DEGREE_CELSIUS);
    public static final Unit FREQUENCY_UNIT = Unit.get(BaseUnit.HERTZ);
    public static final Unit DISTORTION_UNIT = Unit.get(BaseUnit.PERCENT);

    private Map<InstrumentationPageFields, AbstractField> valueMap;

    public InstrumentationPageResponse(TimeZone timeZone) {
        super(ResponseFrame.RESPONSE_DATA_LENGTH, timeZone);
        this.valueMap = new HashMap<>();
    }

    @Override
    public InstrumentationPageResponse parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;
        super.parse(rawData, ptr);
        for (InstrumentationPageFields instrumentationPageField : InstrumentationPageFields.values()) {
            AbstractField field = instrumentationPageField.getFieldParser().parse(rawData, ptr);
            valueMap.put(instrumentationPageField, field);
            ptr += field.getLength();
        }
        return this;
    }

    protected Map<InstrumentationPageFields, AbstractField> getValueMap() {
        return valueMap;
    }

    /**
     * Getter for the {@link AbstractField} corresponding to the given {@link InstrumentationPageFields} key.
     *
     * @param field the InstrumentationPageFields key
     * @return the AbstractField, or null if the field could not be found
     */

    public AbstractField getField(InstrumentationPageFields field) {
        return getValueMap().get(field);
    }
}