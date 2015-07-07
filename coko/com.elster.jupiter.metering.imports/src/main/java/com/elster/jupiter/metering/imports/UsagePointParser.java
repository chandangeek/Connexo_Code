package com.elster.jupiter.metering.imports;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.metering.imports.impl.UsagePointFileInfo;
import com.elster.jupiter.nls.Thesaurus;

import java.util.List;

public interface UsagePointParser {

    public static final String MRID = "mRID";
    public static final String SERVICEKIND = "serviceKind";
    public static final String SERVICELOCATIONID = "serviceLocationID";
    public static final String NAME = "name";
    public static final String ALIASNAME = "aliasName";
    public static final String DESCRIPTION = "description";
    public static final String OUTAGEREGION = "outageregion";
    public static final String READCYCLE = "readcycle";
    public static final String READROUTE = "readroute";
    public static final String SERVICEPRIORITY = "servicePriority";
    public static final String ALLOWUPDATE = "allowUpdate";
    public static final String GROUNDED = "grounded";
    public static final String PHASECODE = "phaseCode";
    public static final String RATEDPOWERVALUE = "ratedPowerValue";
    public static final String RATEDPOWERMULTIPLIER = "ratedPowerMultiplier";
    public static final String RATEDPOWERUNIT = "ratedPowerUnit";
    public static final String RATEDCURRENTVALUE = "ratedCurrentValue";
    public static final String RATEDCURRENTMULTIPLIER = "ratedCurrentMultiplier";
    public static final String RATEDCURRENTUNIT = "ratedCurrentUnit";
    public static final String ESTIMATEDLOADVALUE = "estimatedLoadValue";
    public static final String ESTIMATEDLOADMULTIPLIER = "estimatedLoadMultiplier";
    public static final String ESTIMATEDLOADUNIT = "estimatedLoadUnit";
    public static final String NOMINALVOLTAGEVALUE = "nominalVoltageValue";
    public static final String NOMINALVOLTAGEMULTIPLIER = "nominalVoltageMultiplier";
    public static final String NOMINALVOLTAGEUNIT = "nominalVoltageUnit";

    List<UsagePointFileInfo> parse(FileImportOccurrence fileImportOccurrence, Thesaurus thesaurus);

    String getParserFormatExtensionName();
}