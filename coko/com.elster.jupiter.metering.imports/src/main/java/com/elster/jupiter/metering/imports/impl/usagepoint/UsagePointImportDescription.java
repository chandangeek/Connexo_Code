/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl.usagepoint;


import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.imports.impl.CustomPropertySetRecord;
import com.elster.jupiter.fileimport.csvimport.FieldParser;
import com.elster.jupiter.fileimport.csvimport.FileImportDescription;
import com.elster.jupiter.metering.imports.impl.MeteringDataImporterContext;
import com.elster.jupiter.fileimport.csvimport.exceptions.ValueParserException;
import com.elster.jupiter.fileimport.csvimport.fields.CommonField;
import com.elster.jupiter.fileimport.csvimport.fields.FileImportField;
import com.elster.jupiter.metering.imports.impl.parsers.BigDecimalParser;
import com.elster.jupiter.metering.imports.impl.parsers.BooleanParser;
import com.elster.jupiter.metering.imports.impl.parsers.InstantParser;
import com.elster.jupiter.metering.imports.impl.parsers.LiteralStringParser;
import com.elster.jupiter.metering.imports.impl.parsers.NumberParser;
import com.elster.jupiter.metering.imports.impl.parsers.QuantityParser;
import com.elster.jupiter.metering.imports.impl.parsers.YesNoAnswerParser;
import com.elster.jupiter.metering.imports.impl.properties.SupportedNumberFormat;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class UsagePointImportDescription implements FileImportDescription<UsagePointImportRecord> {

    private final InstantParser instantParser;
    private final BigDecimalParser bigDecimalParser;
    private final QuantityParser quantityParser;
    private final LiteralStringParser stringParser;
    private final BooleanParser booleanParser;
    private final YesNoAnswerParser yesNoAnswerParser;
    private final MeteringDataImporterContext context;
    private NumberParser numberParser;

    UsagePointImportDescription(String dateFormat, String timeZone, SupportedNumberFormat numberFormat, MeteringDataImporterContext context) {
        this.instantParser = new InstantParser(dateFormat, timeZone);
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
        fields.put("id", CommonField.withParser(stringParser)
                .withSetter(record::setUsagePointIdentifier)
                .withName("id")
                .markMandatory()
                .build());
        fields.put("serviceKind", CommonField.withParser(stringParser)
                .withSetter(record::setServiceKind)
                .withName("serviceKind")
                .markMandatory()
                .build());
        fields.put("isSdp", CommonField.withParser(booleanParser)
                .withSetter(record::setSdp)
                .withName("isSdp")
                .build());
        fields.put("isVirtual", CommonField.withParser(booleanParser)
                .withSetter(record::setVirtual)
                .withName("isVirtual")
                .build());
        fields.put("installationTime", CommonField.withParser(instantParser)
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
                .withSetter(record::setServiceDeliveryRemark)
                .withName("serviceDeliveryRemark")
                .build());
        fields.put("allowUpdate", CommonField.withParser(booleanParser)
                .withSetter(record::setAllowUpdate)
                .withName("allowUpdate")
                .build());
        fields.put("metrologyConfiguration", CommonField.withParser(stringParser)
                .withSetter(record::setMetrologyConfiguration)
                .withName("metrologyConfiguration")
                .build());
        fields.put("metrologyConfigurationTime", CommonField.withParser(instantParser)
                .withSetter(record::setMetrologyConfigurationApplyTime)
                .withName("metrologyConfigurationTime")
                .build());

        addTechnicalAttributesFields(fields, record);
        addLocationFields(fields, record);
        addCustomPropertySetFields(fields, record);
        return fields;
    }

    private void addTechnicalAttributesFields(Map<String, FileImportField<?>> fields, UsagePointImportRecord record) {
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
    }

    private void addLocationFields(Map<String, FileImportField<?>> fields, UsagePointImportRecord record) {
        if (context.insightInstalled()) {
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
                    .sorted((t1, t2) -> Integer.compare(t1.getRanking(), t2.getRanking()))
                    .map(LocationTemplate.TemplateField::getName)
                    .forEach(s -> {
                        fields.put(s, CommonField.withParser(stringParser)
                                .withSetter(record::addLocation)
                                .withName(s)
                                .build());
                    });
        }
    }

    public void addCustomPropertySetFields(Map<String, FileImportField<?>> fields, UsagePointImportRecord record) {
        fields.put("customPropertySetTime", CommonField
                .withParser(instantParser)
                .build());
        fields.put("customPropertySetValue", CommonField
                .withParser(new FieldParser<Map<RegisteredCustomPropertySet, CustomPropertySetRecord>>() {
                    @Override
                    public Class<Map<RegisteredCustomPropertySet, CustomPropertySetRecord>> getValueType() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Map<RegisteredCustomPropertySet, CustomPropertySetRecord> parse(String value) throws
                            ValueParserException {
                        throw new UnsupportedOperationException();
                    }
                })
                .withSetter(record::setCustomPropertySets)
                .build());
    }

    @Override
    public Map<Class, FieldParser> getParsers() {
        Map<Class, FieldParser> fieldParsers = new HashMap<>();
        Arrays.asList(
                instantParser,
                bigDecimalParser,
                numberParser,
                stringParser,
                booleanParser,
                yesNoAnswerParser,
                quantityParser
        ).forEach(fieldParser -> fieldParsers.put(fieldParser.getValueType(), fieldParser));
        return fieldParsers;
    }
}
