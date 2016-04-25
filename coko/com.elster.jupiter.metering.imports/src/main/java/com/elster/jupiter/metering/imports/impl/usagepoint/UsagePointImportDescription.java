package com.elster.jupiter.metering.imports.impl.usagepoint;


import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.imports.impl.CustomPropertySetDescription;
import com.elster.jupiter.metering.imports.impl.FieldParser;
import com.elster.jupiter.metering.imports.impl.FileImportDescription;
import com.elster.jupiter.metering.imports.impl.MeteringDataImporterContext;
import com.elster.jupiter.metering.imports.impl.fields.CommonField;
import com.elster.jupiter.metering.imports.impl.fields.FileImportField;
import com.elster.jupiter.metering.imports.impl.parsers.BigDecimalParser;
import com.elster.jupiter.metering.imports.impl.parsers.BooleanParser;
import com.elster.jupiter.metering.imports.impl.parsers.DateParser;
import com.elster.jupiter.metering.imports.impl.parsers.LiteralStringParser;
import com.elster.jupiter.metering.imports.impl.parsers.NumberParser;
import com.elster.jupiter.metering.imports.impl.parsers.QuantityParser;
import com.elster.jupiter.metering.imports.impl.parsers.YesNoAnswerParser;
import com.elster.jupiter.metering.imports.impl.properties.SupportedNumberFormat;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UsagePointImportDescription extends CustomPropertySetDescription implements FileImportDescription<UsagePointImportRecord> {

    private final DateParser dateParser;
    private final BigDecimalParser bigDecimalParser;
    private final QuantityParser quantityParser;
    private final LiteralStringParser stringParser;
    private final BooleanParser booleanParser;
    private final YesNoAnswerParser yesNoAnswerParser;
    private final MeteringDataImporterContext context;
    NumberParser numberParser;

    public UsagePointImportDescription(String dateFormat, String timeZone, SupportedNumberFormat numberFormat, MeteringDataImporterContext context) {
        this.dateParser = new DateParser(dateFormat, timeZone);
        this.bigDecimalParser = new BigDecimalParser(numberFormat);
        this.numberParser = new NumberParser(NumberFormat.getInstance(context.getThreadPrincipalService().getLocale()));
        this.stringParser = new LiteralStringParser();
        this.booleanParser = new BooleanParser();
        this.yesNoAnswerParser = new YesNoAnswerParser();
        this.quantityParser = new QuantityParser(bigDecimalParser, numberParser, stringParser);
        this.context = context;
    }

    @Override
    public UsagePointImportRecord getFileImportRecord() {
        return new UsagePointImportRecord();
    }

    @Override
    public Map<String, FileImportField<?>> getFields(UsagePointImportRecord record) {
        Map<String, FileImportField<?>> fields = new HashMap<>();
        //mRID
        fields.put("mRID", CommonField.withParser(stringParser)
                .withSetter(record::setmRID)
                .withName("mRID")
                .markMandatory()
                .build());
        //Service category
        fields.put("serviceKind", CommonField.withParser(stringParser)
                .withSetter(record::setServiceKind)
                .withName("serviceKind")
                .markMandatory()
                .build());
        fields.put("latitude", CommonField.withParser(stringParser)
                .withSetter(record::setLatitude)
                .withName("latitude")
                .build());
        fields.put("longitude", CommonField.withParser(stringParser)
                .withSetter(record::setLongitude)
                .withName("longitude")
                .build());
        fields.put("elevation", CommonField.withParser(stringParser)
                .withSetter(record::setElevation)
                .withName("elevation")
                .build());
        context.getMeteringService().getLocationTemplate().getTemplateMembers().stream()
                .sorted((t1,t2)->Integer.compare(t1.getRanking(),t2.getRanking()))
                .map(LocationTemplate.TemplateField::getName)
                .forEach(s-> {
                    if(context.getMeteringService().getLocationTemplate().getTemplateMembers().stream()
                            .filter(m->m.isMandatory()).anyMatch(m -> m.getName().equals(s))){
                        fields.put(s, CommonField.withParser(stringParser)
                                .withSetter(record::addLocation)
                                .withName(s)
                                .markMandatory()
                                .build());
                    }else{
                        fields.put(s, CommonField.withParser(stringParser)
                                .withSetter(record::addLocation)
                                .withName(s)
                                .build());
                    }
                });

        fields.put("name", CommonField.withParser(stringParser)
                .withSetter(record::setName)
                .withName("name")
                .build());
        fields.put("installationTime", CommonField.withParser(dateParser)
                .withSetter(record::setInstallationTime)
                .withName("installationTime")
                .build());
        fields.put("outageregion", CommonField.withParser(stringParser)
                .withSetter(record::setOutageRegion)
                .withName("outageregion")
                .build());
        fields.put("readroute", CommonField.withParser(stringParser)
                .withSetter(record::setReadRoute)
                .withName("readroute")
                .build());
        fields.put("servicePriority", CommonField.withParser(stringParser)
                .withSetter(record::setServicePriority)
                .withName("servicePriority")
                .build());
        fields.put("serviceDeliveryRemark", CommonField.withParser(stringParser)
                .withSetter(record::setServicePriority)
                .withName("serviceDeliveryRemark")
                .build());
        fields.put("allowUpdate", CommonField.withParser(booleanParser)
                .withSetter(record::setAllowUpdate)
                .withName("allowUpdate")
                .build());

        //Technical attributes
        fields.put("collar", CommonField.withParser(yesNoAnswerParser)
                .withSetter(record::setCollar)
                .withName("collar")
                .build());
        fields.put("grounded", CommonField.withParser(yesNoAnswerParser)
                .withSetter(record::setGrounded)
                .withName("grounded")
                .build());
        fields.put("phaseCode", CommonField.withParser(stringParser)
                .withSetter(record::setPhaseCode)
                .withName("phaseCode")
                .build());
        fields.put("ratedPower", CommonField.withParser(quantityParser)
                .withSetter(record::setRatedPower)
                .withName("ratedPower")
                .build());
        fields.put("ratedCurrent", CommonField.withParser(quantityParser)
                .withSetter(record::setRatedCurrent)
                .withName("ratedCurrent")
                .build());
        fields.put("estimatedLoad", CommonField.withParser(quantityParser)
                .withSetter(record::setEstimatedLoad)
                .withName("estimatedLoad")
                .build());
        fields.put("nominalVoltage", CommonField.withParser(quantityParser)
                .withSetter(record::setNominalVoltage)
                .withName("nominalVoltage")
                .build());
        fields.put("pressure", CommonField.withParser(quantityParser)
                .withSetter(record::setPressure)
                .withName("pressure")
                .build());
        fields.put("physicalCapacity", CommonField.withParser(quantityParser)
                .withSetter(record::setPhysicalCapacity)
                .withName("physicalCapacity")
                .build());
        fields.put("limiter", CommonField.withParser(yesNoAnswerParser)
                .withSetter(record::setLimiter)
                .withName("limiter")
                .build());
        fields.put("loadLimiterType", CommonField.withParser(stringParser)
                .withSetter(record::setLoadLimiterType)
                .withName("loadLimiterType")
                .build());
        fields.put("loadLimit", CommonField.withParser(quantityParser)
                .withSetter(record::setLoadLimit)
                .withName("loadLimit")
                .build());
        fields.put("bypass", CommonField.withParser(yesNoAnswerParser)
                .withSetter(record::setBypass)
                .withName("bypass")
                .build());
        fields.put("bypassStatus", CommonField.withParser(stringParser)
                .withSetter(record::setBypassStatus)
                .withName("bypassStatus")
                .build());
        fields.put("valve", CommonField.withParser(yesNoAnswerParser)
                .withSetter(record::setValve)
                .withName("valve")
                .build());
        fields.put("capped", CommonField.withParser(yesNoAnswerParser)
                .withSetter(record::setCap)
                .withName("capped")
                .build());
        fields.put("clamped", CommonField.withParser(yesNoAnswerParser)
                .withSetter(record::setClamp)
                .withName("clamped")
                .build());
        fields.put("interruptible", CommonField.withParser(yesNoAnswerParser)
                .withSetter(record::setInterruptible)
                .withName("interruptible")
                .build());

        //Custom property Sets
        fields.putAll(super.getCustomPropertySetFields(dateParser, record));

        return fields;
    }

    @Override
    public Map<Class, FieldParser> getParsers() {
        Map<Class, FieldParser> parsers = new HashMap<>();
        Arrays.asList(
                dateParser,
                bigDecimalParser,
                numberParser,
                stringParser,
                booleanParser,
                yesNoAnswerParser,
                quantityParser
        ).forEach(e -> parsers.put(e.getValueType(), e));
        return parsers;
    }


}
