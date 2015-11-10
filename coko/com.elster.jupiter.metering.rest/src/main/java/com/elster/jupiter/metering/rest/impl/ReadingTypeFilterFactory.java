package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.ReadingTypeFilter;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Where;

public class ReadingTypeFilterFactory {

    public static ReadingTypeFilter from(JsonQueryFilter jsonQueryFilter) {
        Condition condition = Condition.TRUE;

        condition = jsonQueryFilter.hasProperty("aliasName") ?
                condition.and(Operator.LIKE.compare("aliasName", Where.toOracleSql(jsonQueryFilter.getString("aliasName")))) : condition;

        if (jsonQueryFilter.getBoolean("active") != null) {
            condition = jsonQueryFilter.getBoolean("active") ?
                    condition.and(Operator.isTrue("active")) :
                    condition.and(Operator.isFalse("active"));
        }

        condition = !jsonQueryFilter.getPropertyList("macroPeriod").isEmpty() ?
                condition.and(Operator.REGEXP_LIKE.compare("mRID", "^(" + String.join("|", jsonQueryFilter.getPropertyList("macroPeriod")) + ")")) : condition;

        condition = !jsonQueryFilter.getPropertyList("aggregate").isEmpty() ?
                condition.and(Operator.REGEXP_LIKE.compare("mRID", "^(\\d+\\.){1}(" + String.join("|", jsonQueryFilter.getPropertyList("aggregate")) + ")")) : condition;

        condition = !jsonQueryFilter.getPropertyList("measuringPeriod").isEmpty() ?
                condition.and(Operator.REGEXP_LIKE.compare("mRID", "^(\\d+\\.){2}(" + String.join("|", jsonQueryFilter.getPropertyList("measuringPeriod")) + ")")) : condition;

        condition = !jsonQueryFilter.getPropertyList("accumulation").isEmpty() ?
                condition.and(Operator.REGEXP_LIKE.compare("mRID", "^(\\d+\\.){3}(" + String.join("|", jsonQueryFilter.getPropertyList("accumulation")) + ")")) : condition;

        condition = !jsonQueryFilter.getPropertyList("flowDirection").isEmpty() ?
                condition.and(Operator.REGEXP_LIKE.compare("mRID", "^(\\d+\\.){4}(" + String.join("|", jsonQueryFilter.getPropertyList("flowDirection")) + ")")) : condition;

        condition = !jsonQueryFilter.getPropertyList("commodity").isEmpty() ?
                condition.and(Operator.REGEXP_LIKE.compare("mRID", "^(\\d+\\.){5}(" + String.join("|", jsonQueryFilter.getPropertyList("commodity")) + ")")) : condition;

        condition = !jsonQueryFilter.getPropertyList("measurementKind").isEmpty() ?
                condition.and(Operator.REGEXP_LIKE.compare("mRID", "^(\\d+\\.){6}(" + String.join("|", jsonQueryFilter.getPropertyList("measurementKind")) + ")")) : condition;

        condition = !jsonQueryFilter.getPropertyList("interHarmonicNumerator").isEmpty() ?
                condition.and(Operator.REGEXP_LIKE.compare("mRID", "^(\\d+\\.){7}(" + String.join("|", jsonQueryFilter.getPropertyList("interHarmonicNumerator")) + ")")) : condition;

        condition = !jsonQueryFilter.getPropertyList("interHarmonicDenominator").isEmpty() ?
                condition.and(Operator.REGEXP_LIKE.compare("mRID", "^(\\d+\\.){8}(" + String.join("|", jsonQueryFilter.getPropertyList("interHarmonicDenominator")) + ")")) : condition;

        condition = !jsonQueryFilter.getPropertyList("argumentNumerator").isEmpty() ?
                condition.and(Operator.REGEXP_LIKE.compare("mRID", "^(\\d+\\.){9}(" + String.join("|", jsonQueryFilter.getPropertyList("argumentNumerator")) + ")")) : condition;

        condition = !jsonQueryFilter.getPropertyList("argumentDenominator").isEmpty() ?
                condition.and(Operator.REGEXP_LIKE.compare("mRID", "^(\\d+\\.){10}(" + String.join("|", jsonQueryFilter.getPropertyList("argumentDenominator")) + ")")) : condition;

        condition = !jsonQueryFilter.getPropertyList("tou").isEmpty() ?
                condition.and(Operator.REGEXP_LIKE.compare("mRID", "^(\\d+\\.){11}(" + String.join("|", jsonQueryFilter.getPropertyList("tou")) + ")")) : condition;

        condition = !jsonQueryFilter.getPropertyList("cpp").isEmpty() ?
                condition.and(Operator.REGEXP_LIKE.compare("mRID", "^(\\d+\\.){12}(" + String.join("|", jsonQueryFilter.getPropertyList("cpp")) + ")")) : condition;

        condition = !jsonQueryFilter.getPropertyList("consumptionTier").isEmpty() ?
                condition.and(Operator.REGEXP_LIKE.compare("mRID", "^(\\d+\\.){13}(" + String.join("|", jsonQueryFilter.getPropertyList("consumptionTier")) + ")")) : condition;

        condition = !jsonQueryFilter.getPropertyList("phases").isEmpty() ?
                condition.and(Operator.REGEXP_LIKE.compare("mRID", "^(\\d+\\.){14}(" + String.join("|", jsonQueryFilter.getPropertyList("phases")) + ")")) : condition;

        condition = !jsonQueryFilter.getPropertyList("metricMultiplier").isEmpty() ?
                condition.and(Operator.REGEXP_LIKE.compare("mRID", "^(\\d+\\.){15}(" + String.join("|", jsonQueryFilter.getPropertyList("metricMultiplier")) + ")")) : condition;

        condition = !jsonQueryFilter.getPropertyList("unit").isEmpty() ?
                condition.and(Operator.REGEXP_LIKE.compare("mRID", "^(\\d+\\.){16}(" + String.join("|", jsonQueryFilter.getPropertyList("unit")) + ")")) : condition;

        condition = !jsonQueryFilter.getPropertyList("currency").isEmpty() ?
                condition.and(Operator.REGEXP_LIKE.compare("mRID", "^(\\d+\\.){17}(" + String.join("|", jsonQueryFilter.getPropertyList("currency")) + ")")) : condition;

        return new ReadingTypeFilter(condition);
    }

}
