package com.elster.jupiter.metering;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Where;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class ReadingTypeFilter {


    private Condition condition;

    public ReadingTypeFilter()
    {
        this.condition = Condition.TRUE;
    }

    public Condition getCondition() {
        return condition;
    }

    public void addFullAliasNameCondition(String name){
        condition =  condition.and(Operator.LIKE.compare("fullAliasName", Where.toOracleSql(name)));
    }

    public void addMRIDCondition(String mRID) {
        condition = condition.and(Operator.LIKE.compare("mRID", Where.toOracleSql(mRID)));
    }

    public void addSelectedReadingTypesCondition(List<String> values) {
        values.forEach(e -> condition = condition.and(Operator.LIKE.compare("mRID", e)).not());
    }

    public void addEquidistantCondition(boolean equidistant) {
        condition = equidistant ?
                condition.and(Operator.isTrue("equidistant")) :
                condition.and(Operator.isFalse("equidistant"));
    }

    public void addActiveCondition(boolean active){
        condition = active ?
                condition.and(Operator.isTrue("active")) :
                condition.and(Operator.isFalse("active"));
    }

    public void addCodedValueCondition(String fieldName, List<String> values){
        condition = condition.and(Arrays.stream(ReadingTypeFields.values()).filter(candidate -> candidate.getName().equalsIgnoreCase(fieldName))
                .findFirst().map(e -> e.getRegexpCondition(values)).orElse(Condition.TRUE));
    }

    public void addCodedValueCondition(String fieldName, String value) {
        condition = condition.and(Arrays.stream(ReadingTypeFields.values()).filter(candidate -> candidate.getName().equalsIgnoreCase(fieldName))
                .findFirst().map(e -> e.getRegexpCondition(Collections.singletonList(value))).orElse(Condition.TRUE));
    }

    public enum ReadingTypeFields{
        MACRO_PERIOD("macroPeriod",0),
        AGGREAGTE("aggregate",1),
        MEASUREMENT_PERIOD("measurementPeriod",2),
        ACCUMULATION("accumulation",3),
        FLOW_DIRECTION("flowDirection",4),
        COMMODITY("commodity",5),
        MEASUREMENT_KIND("measurementKind",6),
        INTERHARMONIC_NUMERATOR("interHarmonicNumerator",7),
        INTERHARMONIC_DENOMINATOR("interHarmonicDenominator",8),
        ARGUMENT_NUMERATOR("argumentNumerator",9),
        ARGUMENT_DENOMINATOR("argumentDenominator",10),
        TIME_OF_USE("tou",11),
        CPP("cpp",12),
        CONSUMPTION_TIER("consumptionTier",13),
        PHASES("phases",14),
        MULTIPLIER("multiplier",15),
        UNIT("unit",16),
        CURRENCY("currency",17);

        public String getName() {
            return name;
        }

        private String name;
        private int offset;

        ReadingTypeFields(String name, int offset) {
            this.name = name;
            this.offset = offset;
        }

        public Condition getRegexpCondition(List<String> values){
            Condition condition = Condition.TRUE;
            if(!values.isEmpty()) {
                condition = condition.and(where("mRID").matches("^(\\d+\\.){" + offset + "}(" + String.join("|", values) + ")\\.", ""));
            }
            return condition;
        }
    }
}
