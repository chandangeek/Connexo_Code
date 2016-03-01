package com.elster.jupiter.metering.imports.impl.usagepoint.usagepointimport;


import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.imports.impl.usagepoint.FileImportDescription;
import com.elster.jupiter.metering.imports.impl.usagepoint.MeteringDataImporterContext;
import com.elster.jupiter.metering.imports.impl.usagepoint.exceptions.ValueParserException;
import com.elster.jupiter.metering.imports.impl.usagepoint.fields.CommonField;
import com.elster.jupiter.metering.imports.impl.usagepoint.fields.FileImportField;
import com.elster.jupiter.metering.imports.impl.usagepoint.parsers.*;
import com.elster.jupiter.metering.imports.impl.usagepoint.properties.SupportedNumberFormat;
import com.elster.jupiter.util.YesNoAnswer;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsagePointImportDescription implements FileImportDescription<UsagePointImportRecord> {

    private final DateParser dateParser;
    private final BigDecimalParser bigDecimalParser;
    private final MeteringDataImporterContext context;

    public UsagePointImportDescription(String dateFormat, String timeZone, SupportedNumberFormat numberFormat, MeteringDataImporterContext context) {
        this.dateParser = new DateParser(dateFormat, timeZone);
        this.bigDecimalParser = new BigDecimalParser(numberFormat);
        this.context = context;
    }

    @Override
    public UsagePointImportRecord getFileImportRecord() {
        return new UsagePointImportRecord();
    }

    @Override
    public Map<String, FileImportField<?>> getFields(UsagePointImportRecord record) {
        Map<String, FileImportField<?>> fields = new HashMap<>();
        LiteralStringParser stringParser = new LiteralStringParser();
        BooleanParser booleanParser = new BooleanParser();
        NumberParser numberParser = new NumberParser();
        YesNoAnswerParser yesNoAnswerParser = new YesNoAnswerParser();
        //mRID
        fields.put("mRID",CommonField.withParser(stringParser)
                .withSetter(record::setmRID)
                .withName("mRID")
                .markMandatory()
                .build());
        //Service category
        fields.put("serviceKind",CommonField.withParser(stringParser)
                .withSetter(record::setServiceKind)
                .withName("serviceKind")
                .markMandatory()
                .build());
        fields.put("serviceLocationID",CommonField.withParser(numberParser)
                .withSetter(number -> record.setServiceLocationID(number != null ? number.longValue() : null))
                .withName("serviceLocationID")
                .build());
        fields.put("name",CommonField.withParser(stringParser)
                .withSetter(record::setName)
                .withName("name")
                .build());
        fields.put("installationTime",CommonField.withParser(dateParser)
                .withSetter(record::setInstallationTime)
                .withName("installationTime")
                .markMandatory()
                .build());
        fields.put("serviceLocationString",CommonField.withParser(stringParser)
                .withSetter(record::setServiceLocationString)
                .withName("serviceLocationString")
                .build());
        fields.put("outageregion",CommonField.withParser(stringParser)
                .withSetter(record::setOutageRegion)
                .withName("outageregion")
                .build());
        fields.put("readroute",CommonField.withParser(stringParser)
                .withSetter(record::setReadRoute)
                .withName("readroute")
                .build());
        fields.put("servicePriority",CommonField.withParser(stringParser)
                .withSetter(record::setServicePriority)
                .withName("servicePriority")
                .build());
        fields.put("serviceDeliveryRemark",CommonField.withParser(stringParser)
                .withSetter(record::setServicePriority)
                .withName("serviceDeliveryRemark")
                .build());
        fields.put("allowUpdate",CommonField.withParser(booleanParser)
                .withSetter(record::setAllowUpdate)
                .withName("allowUpdate")
                .build());

        //Technical attributes
        fields.put("collar",CommonField.withParser(yesNoAnswerParser)
                .withSetter(record::setCollar)
                .withName("collar")
                .build());
        fields.put("grounded",CommonField.withParser(booleanParser)
                .withSetter(record::setGrounded)
                .withName("grounded")
                .build());
        fields.put("phaseCode",CommonField.withParser(stringParser)
                .withSetter(record::setPhaseCode)
                .withName("phaseCode")
                .build());
        fields.put("ratedPowerValue",CommonField.withParser(bigDecimalParser)
                .withSetter(record::setRatedPowerValue)
                .withName("ratedPowerValue")
                .build());
        fields.put("ratedPowerMultiplier",CommonField.withParser(numberParser)
                .withSetter(number -> record.setRatedPowerMultiplier(number != null ? number.intValue() : null))
                .withName("ratedPowerMultiplier")
                .build());
        fields.put("ratedPowerUnit",CommonField.withParser(stringParser)
                .withSetter(record::setRatedPowerUnit)
                .withName("ratedPowerUnit")
                .build());
        fields.put("ratedCurrentValue",CommonField.withParser(bigDecimalParser)
                .withSetter(record::setRatedCurrentValue)
                .withName("ratedCurrentValue")
                .build());
        fields.put("ratedCurrentMultiplier",CommonField.withParser(numberParser)
                .withSetter(number -> record.setRatedCurrentMultiplier(number != null ? number.intValue() : null))
                .withName("ratedCurrentMultiplier")
                .build());
        fields.put("ratedCurrentUnit",CommonField.withParser(stringParser)
                .withSetter(record::setRatedCurrentUnit)
                .withName("ratedCurrentUnit")
                .build());
        fields.put("estimatedLoadValue",CommonField.withParser(bigDecimalParser)
                .withSetter(record::setEstimatedLoadValue)
                .withName("estimatedLoadValue")
                .build());
        fields.put("estimatedLoadMultiplier",CommonField.withParser(numberParser)
                .withSetter(number -> record.setEstimatedLoadMultiplier(number != null ? number.intValue() : null))
                .withName("estimatedLoadMultiplier")
                .build());
        fields.put("estimatedLoadUnit",CommonField.withParser(stringParser)
                .withSetter(record::setEstimatedLoadUnit)
                .withName("estimatedLoadUnit")
                .build());
        fields.put("nominalVoltageValue",CommonField.withParser(bigDecimalParser)
                .withSetter(record::setNominalVoltageValue)
                .withName("nominalVoltageValue")
                .build());
        fields.put("nominalVoltageMultiplier",CommonField.withParser(numberParser)
                .withSetter(number -> record.setNominalVoltageMultiplier(number != null ? number.intValue() : null))
                .withName("nominalVoltageMultiplier")
                .build());
        fields.put("nominalVoltageUnit",CommonField.withParser(stringParser)
                .withSetter(record::setNominalVoltageUnit)
                .withName("nominalVoltageUnit")
                .build());
        fields.put("pressureValue",CommonField.withParser(bigDecimalParser)
                .withSetter(record::setPressureValue)
                .withName("pressureValue")
                .build());
        fields.put("pressureMultiplier",CommonField.withParser(numberParser)
                .withSetter(number -> record.setPressureMultiplier(number != null ? number.intValue() : null))
                .withName("pressureMultiplier")
                .build());
        fields.put("pressureUnit",CommonField.withParser(stringParser)
                .withSetter(record::setPressureUnit)
                .withName("pressureUnit")
                .build());
        fields.put("physicalCapacityValue",CommonField.withParser(bigDecimalParser)
                .withSetter(record::setPhysicalCapacityValue)
                .withName("physicalCapacityValue")
                .build());
        fields.put("physicalCapacityMultiplier",CommonField.withParser(numberParser)
                .withSetter(number -> record.setPhysicalCapacityMultiplier(number != null ? number.intValue() : null))
                .withName("physicalCapacityMultiplier")
                .build());
        fields.put("physicalCapacityUnit",CommonField.withParser(stringParser)
                .withSetter(record::setPhysicalCapacityUnit)
                .withName("physicalCapacityUnit")
                .build());
        fields.put("limiter",CommonField.withParser(booleanParser)
                .withSetter(record::setLimiter)
                .withName("limiter")
                .build());
        fields.put("loadLimiterType",CommonField.withParser(stringParser)
                .withSetter(record::setLoadLimiterType)
                .withName("loadLimiterType")
                .build());
        fields.put("loadLimitValue",CommonField.withParser(bigDecimalParser)
                .withSetter(record::setLoadLimitValue)
                .withName("loadLimitValue")
                .build());
        fields.put("loadLimitMultiplier",CommonField.withParser(numberParser)
                .withSetter(number -> record.setLoadLimitMultiplier(number != null ? number.intValue() : null))
                .withName("loadLimitMultiplier")
                .build());
        fields.put("loadLimitUnit",CommonField.withParser(stringParser)
                .withSetter(record::setLoadLimitUnit)
                .withName("loadLimitUnit")
                .build());
        fields.put("bypass",CommonField.withParser(yesNoAnswerParser)
                .withSetter(record::setBypass)
                .withName("bypass")
                .build());
        fields.put("bypassStatus",CommonField.withParser(stringParser)
                .withSetter(record::setBypassStatus)
                .withName("bypassStatus")
                .build());
        fields.put("valve",CommonField.withParser(yesNoAnswerParser)
                .withSetter(record::setValve)
                .withName("valve")
                .build());
        fields.put("capped",CommonField.withParser(yesNoAnswerParser)
                .withSetter(record::setCap)
                .withName("capped")
                .build());
        fields.put("clamped",CommonField.withParser(yesNoAnswerParser)
                .withSetter(record::setClamp)
                .withName("clamped")
                .build());
        fields.put("interruptible",CommonField.withParser(booleanParser)
                .withSetter(record::setInterruptible)
                .withName("interruptible")
                .build());
        //Custom property Sets
        fields.put("customPropertySetValue",CommonField
                .withParser((FieldParser<Map<CustomPropertySet, CustomPropertySetValues>>) value -> {throw new UnsupportedOperationException();})
                .withSetter(record::setCustomPropertySetValues)
                .build());

        return fields;
    }



}
