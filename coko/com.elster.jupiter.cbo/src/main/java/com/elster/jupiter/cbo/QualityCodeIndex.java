/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.Pair;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.elster.jupiter.cbo.QualityCodeCategory.DATACOLLECTION;
import static com.elster.jupiter.cbo.QualityCodeCategory.DERIVED;
import static com.elster.jupiter.cbo.QualityCodeCategory.DIAGNOSTICS;
import static com.elster.jupiter.cbo.QualityCodeCategory.EDITED;
import static com.elster.jupiter.cbo.QualityCodeCategory.ESTIMATED;
import static com.elster.jupiter.cbo.QualityCodeCategory.POWERQUALITY;
import static com.elster.jupiter.cbo.QualityCodeCategory.PROJECTED;
import static com.elster.jupiter.cbo.QualityCodeCategory.QUESTIONABLE;
import static com.elster.jupiter.cbo.QualityCodeCategory.REASONABILITY;
import static com.elster.jupiter.cbo.QualityCodeCategory.TAMPER;
import static com.elster.jupiter.cbo.QualityCodeCategory.VALID;
import static com.elster.jupiter.cbo.QualityCodeCategory.VALIDATION;

public enum QualityCodeIndex {
    DATAVALID(VALID, 0, TranslationKeys.DATAVALID),
    VALIDATED(VALID, 1, TranslationKeys.VALIDATED),
    DIAGNOSTICSFLAG(DIAGNOSTICS, 0, TranslationKeys.DIAGNOSTICSFLAG),
    BATTERYLOW(DIAGNOSTICS, 1, TranslationKeys.BATTERYLOW),
    SENSORFAILURE(DIAGNOSTICS, 3, TranslationKeys.SENSORFAILURE),
    WATCHDOGFLAG(DIAGNOSTICS, 4, TranslationKeys.WATCHDOGFLAG),
    PARITYERROR(DIAGNOSTICS, 5, TranslationKeys.PARITYERROR),
    CRCERROR(DIAGNOSTICS, 6, TranslationKeys.CRCERROR),
    RAMCHECKSUMERROR(DIAGNOSTICS, 7, TranslationKeys.RAMCHECKSUMERROR),
    ROMCHECKSUMERROR(DIAGNOSTICS, 8, TranslationKeys.ROMCHECKSUMERROR),
    CLOCKERROR(DIAGNOSTICS, 9, TranslationKeys.CLOCKERROR),
    POWERQUALITYFLAG(POWERQUALITY, 0, TranslationKeys.POWERQUALITYFLAG),
    EXCESSIVEOUTAGECOUNT(POWERQUALITY, 1, TranslationKeys.EXCESSIVEOUTAGECOUNT),
    PQCOUNTER(POWERQUALITY, 2, TranslationKeys.PQCOUNTER),
    SERVICEDISCONNECTSWITCHING(POWERQUALITY, 3, TranslationKeys.SERVICEDISCONNECTSWITCHING),
    POWERFAIL(POWERQUALITY, 32, TranslationKeys.POWERFAIL),
    POWERDOWN(POWERQUALITY, 1001, TranslationKeys.POWERDOWN),
    POWERUP(POWERQUALITY, 1002, TranslationKeys.POWERUP),
    PHASEFAILURE(POWERQUALITY, 1003, TranslationKeys.PHASEFAILURE),
    REVENUEPROTECTION(TAMPER, 0, TranslationKeys.REVENUEPROTECTION),
    COVEROPENED(TAMPER, 1, TranslationKeys.COVEROPENED),
    LOGICALDISCONNECT(TAMPER, 2, TranslationKeys.LOGICALDISCONNECT),
    REVENUEPROTECTIONSUSPECT(TAMPER, 3, TranslationKeys.REVENUEPROTECTIONSUSPECT),
    REVERSEROTATION(TAMPER, 4, TranslationKeys.REVERSEROTATION),
    STATICDATAFLAG(TAMPER, 5, TranslationKeys.STATICDATAFLAG),
    ALARMFLAG(DATACOLLECTION, 0, TranslationKeys.ALARMFLAG),
    OVERFLOWCONDITIONDETECTED(DATACOLLECTION, 1, TranslationKeys.OVERFLOWCONDITIONDETECTED),
    PARTIALINTERVAL(DATACOLLECTION, 2, TranslationKeys.PARTIALINTERVAL),
    LONGINTERVAL(DATACOLLECTION, 3, TranslationKeys.LONGINTERVAL),
    SKIPPEDINTERVAL(DATACOLLECTION, 4, TranslationKeys.SKIPPEDINTERVAL),
    TESTDATA(DATACOLLECTION, 5, TranslationKeys.TESTDATA),
    CONFIGURATIONCHANGED(DATACOLLECTION, 6, TranslationKeys.CONFIGURATIONCHANGED),
    NOTRECORDING(DATACOLLECTION, 7, TranslationKeys.NOTRECORDING),
    RESETOCCURED(DATACOLLECTION, 8, TranslationKeys.RESETOCCURED),
    CLOCKCHANGED(DATACOLLECTION, 9, TranslationKeys.CLOCKCHANGED),
    LOADCONTROLOCCUREDD(DATACOLLECTION, 10, TranslationKeys.LOADCONTROLOCCUREDD),
    DSTINEFFECT(DATACOLLECTION, 16, TranslationKeys.DSTINEFFECT),
    CLOCKSETFORWARD(DATACOLLECTION, 64, TranslationKeys.CLOCKSETFORWARD),
    CLOCKSETBACKWARD(DATACOLLECTION, 128, TranslationKeys.CLOCKSETBACKWARD),
    FAILEDPROBEATTEMPT(DATACOLLECTION, 129, TranslationKeys.FAILEDPROBEATTEMPT),
    CUSTOMERRAD(DATACOLLECTION, 130, TranslationKeys.CUSTOMERRAD),
    MANUALREAD(DATACOLLECTION, 131, TranslationKeys.MANUALREAD),
    DSTCHANGEOCCURED(DATACOLLECTION, 259, TranslationKeys.DSTCHANGEOCCURED),
    CUSTOM_OTHER(DATACOLLECTION, 1001, TranslationKeys.CUSTOM_OTHER),
    CUSTOM_SHORTLONG(DATACOLLECTION, 1002, TranslationKeys.CUSTOM_SHORTLONG),
    DATAOUTSIDEEXPECTEDRANGE(REASONABILITY, 256, TranslationKeys.DATAOUTSIDEEXPECTEDRANGE),
    ERRORCODE(REASONABILITY, 257, TranslationKeys.ERRORCODE),
    SUSPECT(REASONABILITY, 258, TranslationKeys.SUSPECT),
    KNOWNMISSINGREAD(REASONABILITY, 259, TranslationKeys.KNOWNMISSINGREAD),
    VALIDATIONGENERIC(VALIDATION, 0, TranslationKeys.VALIDATIONGENERIC),
    ZEROUSAGE(VALIDATION, 1, TranslationKeys.ZEROUSAGE),
    USAGEONINACTIVEMETER(VALIDATION, 2, TranslationKeys.USAGEONINACTIVEMETER),
    USAGEABOVE(VALIDATION, 3, TranslationKeys.USAGEABOVE),
    USAGEBELOW(VALIDATION, 4, TranslationKeys.USAGEBELOW),
    USAGEABOVEPERCENT(VALIDATION, 5, TranslationKeys.USAGEABOVEPERCENT),
    USAGEBELOWPERCENT(VALIDATION, 6, TranslationKeys.USAGEBELOWPERCENT),
    TOUSUMCHECK(VALIDATION, 9, TranslationKeys.TOUSUMCHECK),
    EDITGENERIC(EDITED, 0, TranslationKeys.EDITGENERIC),
    ADDED(EDITED, 1, TranslationKeys.ADDED),
    REJECTED(EDITED, 3, TranslationKeys.REJECTED),
    ESTIMATEGENERIC(ESTIMATED, 0, TranslationKeys.ESTIMATEGENERIC),
    INDETERMINATE(QUESTIONABLE, 0, TranslationKeys.INDETERMINATE),
    ACCEPTED(QUESTIONABLE, 1, TranslationKeys.ACCEPTED),
    DETERMINISTIC(DERIVED, 0, TranslationKeys.DETERMINISTIC),
    INFERRED(DERIVED, 1, TranslationKeys.INFERRED),
    PROJECTEDGENERIC(PROJECTED, 0, TranslationKeys.PROJECTEDGENERIC);

    private static final Map<Pair<QualityCodeCategory, Integer>, QualityCodeIndex> INDICES =
            Arrays.stream(values())
                    .collect(Collectors.toMap(index -> Pair.of(index.category, index.index), Function.identity()));
    private final QualityCodeCategory category;
    private final int index;
    private TranslationKey translationKey;

    QualityCodeIndex(QualityCodeCategory category, int index, TranslationKey translationKey) {
        this.category = category;
        this.index = index;
        this.translationKey = translationKey;
    }

    public QualityCodeCategory category() {
        return category;
    }

    public int index() {
        return index;
    }

    public TranslationKey getTranslationKey() {
        return translationKey;
    }

    static Optional<QualityCodeIndex> get(QualityCodeCategory category, int index) {
        return Optional.ofNullable(INDICES.get(Pair.of(category, index)));
    }
}
