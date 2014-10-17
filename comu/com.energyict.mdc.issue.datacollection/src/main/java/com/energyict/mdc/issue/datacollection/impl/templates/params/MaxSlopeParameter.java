package com.energyict.mdc.issue.datacollection.impl.templates.params;

import com.elster.jupiter.issue.share.cep.NumberParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterControl;
import com.elster.jupiter.issue.share.cep.ParameterDefinition;
import com.elster.jupiter.issue.share.cep.controls.SimpleControl;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MaxSlopeParameter extends TranslatedParameter {
    private static List<String> PRIMARY_LIST = Collections.singletonList(ReadingTypeParameter.READING_TYPE_PARAMETER_KEY);
    private static final ParameterConstraint CONSTRAINT = new NumberParameterConstraint(false, 0, Integer.MAX_VALUE);

    private final MeteringService meteringService;
    private String defaultValue;
    private String suffix;

    public MaxSlopeParameter(Thesaurus thesaurus, MeteringService meteringService) {
        super(thesaurus);
        this.meteringService = meteringService;
        this.suffix = getString(MessageSeeds.PARAMETER_NAME_MAX_SLOPE_SUFFIX) + getString(MessageSeeds.PARAMETER_NAME_MAX_SLOPE_SUFFIX_PER_HOUR);
    }

    @Override
    public String getKey() {
        return "maxSlope";
    }

    @Override
    public ParameterControl getControl() {
        return SimpleControl.NUMBER_FIELD;
    }

    @Override
    public ParameterConstraint getConstraint() {
        return CONSTRAINT;
    }

    @Override
    public String getLabel() {
        return getString(MessageSeeds.PARAMETER_NAME_MAX_SLOPE);
    }

    @Override
    public String getSuffix() {
        return suffix;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public List<String> getDependOn() {
        return PRIMARY_LIST;
    }

    @Override
    public ParameterDefinition getValue(Map<String, Object> parameters) {
        MaxSlopeParameter result = clone();
        result.setDefaultValue(String.valueOf(parameters.get(getKey())));
        String cimReadingType = String.valueOf(parameters.get(ReadingTypeParameter.READING_TYPE_PARAMETER_KEY));
        if (!Checks.is(cimReadingType).emptyOrOnlyWhiteSpace()) {
            Optional<ReadingType> readingTypeRef = meteringService.getReadingType(cimReadingType);
            if (readingTypeRef.isPresent()) {
                ReadingType readingType = readingTypeRef.get();
                result.suffix = readingType.getUnit().getSymbol() + getString(MessageSeeds.PARAMETER_NAME_MAX_SLOPE_SUFFIX_PER_HOUR);
            }
        }
        return result;
    }

    protected MaxSlopeParameter clone() {
        return new MaxSlopeParameter(getThesaurus(), meteringService);
    }

    private void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

}