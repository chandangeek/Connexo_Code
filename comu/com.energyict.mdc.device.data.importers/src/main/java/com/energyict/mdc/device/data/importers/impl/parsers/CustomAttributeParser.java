package com.energyict.mdc.device.data.importers.impl.parsers;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.fileimport.csvimport.FieldParser;
import com.elster.jupiter.fileimport.csvimport.exceptions.ValueParserException;
import com.elster.jupiter.properties.PropertySpec;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomAttributeParser implements FieldParser {

    CustomPropertySetService customPropertySetService;
    Map<Class, FieldParser> parsers = new HashMap<>();
    List<RegisteredCustomPropertySet> customPropertySets;
    private LiteralStringParser literalStringParser;
    private DateParser dateParser;
    private QuantityParser quantityParser;
    private BigDecimalParser bigDecimalParser;

    public CustomAttributeParser(CustomPropertySetService customPropertySetService, LiteralStringParser literalStringParser, DateParser dateParser, QuantityParser quantityParser, BigDecimalParser bigDecimalParser) {
        this.customPropertySetService = customPropertySetService;
        this.literalStringParser = literalStringParser;
        this.dateParser = dateParser;
        this.quantityParser = quantityParser;
        this.bigDecimalParser = bigDecimalParser;
        parsers.put(literalStringParser.getValueType(), literalStringParser);
        parsers.put(dateParser.getValueType(), dateParser);
        parsers.put(quantityParser.getValueType(), quantityParser);
        parsers.put(bigDecimalParser.getValueType(), bigDecimalParser);
        customPropertySets = customPropertySetService.findActiveCustomPropertySets();
    }

    @Override
    public Class getValueType() {
        return Object.class;
    }

    @Override
    public Object parse(String value) throws ValueParserException {
        return literalStringParser.parse(value);
    }

    public Object parseCustomAttribute(String header, String value) {
        for (RegisteredCustomPropertySet rset : customPropertySets) {
            if (header.contains(rset.getCustomPropertySet().getId())) {
                CustomPropertySet set = rset.getCustomPropertySet();
                if (header.equalsIgnoreCase(set.getId() + ".versionId")
                        || header.equalsIgnoreCase(set.getId() + ".startTime")
                        || header.equalsIgnoreCase(set.getId() + ".endTime")) {
                    return value.equalsIgnoreCase("NaN") ? Instant.EPOCH : dateParser.parse(value).toInstant();
                }

                for (Object spec : set.getPropertySpecs()) {
                    if (spec instanceof PropertySpec && header.equalsIgnoreCase(set.getId() + "." + ((PropertySpec) spec).getName())) {
                        FieldParser parser = parsers.get(((PropertySpec) spec).getValueFactory().getValueType());
                        if (parser != null) {
                            return parser.parse(value);
                        } else {
                            return ((PropertySpec) spec).getValueFactory().fromStringValue(value);
                        }
                    }
                }
            }
        }
        return parse(value);
    }
}