package com.energyict.protocolimplv2.abnt.common.field.parser;

import com.energyict.mdc.io.CommunicationException;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

/**
 * @author sva
 * @since 11/09/2014 - 14:34
 */
public class SimpleFieldParser<T extends AbstractField> implements FieldParser {

    private Class<T> fieldClazz;

    public SimpleFieldParser(Class<T> fieldClazz) {
        this.fieldClazz = fieldClazz;
    }

    public AbstractField<T> parse(byte[] rawData, int offset) throws ParsingException {
        return newInstanceOfFieldClass().parse(rawData, offset);
    }

    private AbstractField<T> newInstanceOfFieldClass() {
        try {
            return fieldClazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CommunicationException(MessageSeeds.GENERIC_JAVA_REFLECTION_ERROR, fieldClazz.getCanonicalName());
        }
    }
}