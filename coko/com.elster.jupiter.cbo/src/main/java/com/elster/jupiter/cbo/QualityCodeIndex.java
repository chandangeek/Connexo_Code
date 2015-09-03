package com.elster.jupiter.cbo;

import com.elster.jupiter.cbo.impl.TranslationKeys;
import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.Optional;

import static com.elster.jupiter.cbo.QualityCodeCategory.*;

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
    PROJECTEDGENERIC(PROJECTED, 0, TranslationKeys.PROJECTEDGENERIC),;

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
        return Arrays.stream(values())
                .filter(codeIndex -> codeIndex.category == category)
                .filter(codeIndex -> codeIndex.index == index)
                .findFirst();
    }
}
