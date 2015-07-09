package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.nls.Thesaurus;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.osgi.service.component.annotations.Component;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Component(immediate = true, service = UsagePointParser.class)
public class UsagePointParserCSVImpl implements UsagePointParser {

    private static final char MARKER = '#';
    private static final String EMPTY = "";
    private static final char DELIMITER = ',';
    private static final String CSV_EXTENSION = ".CSV";

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

    public UsagePointParserCSVImpl() {
    }

    @Override
    public List<UsagePointFileInfo> parse(FileImportOccurrence fileImportOccurrence, Thesaurus thesaurus) {
        List<UsagePointFileInfo> usagePointFileInfos = new ArrayList<>();
        char delimiter = DELIMITER;
        Logger logger = fileImportOccurrence.getLogger();

        try (CSVParser csvParser = new CSVParser(new InputStreamReader(fileImportOccurrence.getContents()), CSVFormat.DEFAULT.withHeader().withIgnoreSurroundingSpaces(true).withDelimiter(delimiter).withCommentMarker(MARKER))) {
            for (CSVRecord csvRecord : csvParser) {
                usagePointFileInfos.add(setAttributes(csvRecord));
            }
        } catch (Exception e) {
            MessageSeeds.IMPORT_USAGEPOINT_EXCEPTION.log(logger, thesaurus, e);
        }
        return usagePointFileInfos;
    }

    @Override
    public List<String> getParserFormatExtensionName() {
        return Arrays.asList(CSV_EXTENSION);
    }

    private String processComment(String record) {
        return record.isEmpty() || record.charAt(0) == MARKER ? EMPTY : record;
    }

    private int processInt(String record) {
        return processComment(record).isEmpty() ? 0 : Integer.parseInt(record);
    }

    private <E extends Enum<E>> String processEnum(String value, Class<E> enumClass) {
        for (E e : enumClass.getEnumConstants()) {
            if (e.name().equals(value)) {
                return e.name();
            }
        }
        return EMPTY;
    }

    private UsagePointFileInfo setAttributes(CSVRecord csvRecord) {
        UsagePointFileInfo usagePointFileInfo = new UsagePointFileInfo();
        usagePointFileInfo.setmRID(csvRecord.isMapped(MRID) ? processComment(csvRecord.get(MRID)) : EMPTY);
        usagePointFileInfo.setServiceKind(csvRecord.isMapped(SERVICEKIND) ? processEnum(processComment(csvRecord.get(SERVICEKIND)).toUpperCase(), ServiceKind.class) : EMPTY);
        usagePointFileInfo.setServiceLocationID(csvRecord.isMapped(SERVICELOCATIONID) ? processInt(csvRecord.get(SERVICELOCATIONID)) : 0L);
        usagePointFileInfo.setName(csvRecord.isMapped(NAME) ? processComment(csvRecord.get(NAME)) : EMPTY);
        usagePointFileInfo.setAliasName(csvRecord.isMapped(ALIASNAME) ? processComment(csvRecord.get(ALIASNAME)) : EMPTY);
        usagePointFileInfo.setDescription(csvRecord.isMapped(DESCRIPTION) ? processComment(csvRecord.get(DESCRIPTION)) : EMPTY);
        usagePointFileInfo.setOutageregion(csvRecord.isMapped(OUTAGEREGION) ? processComment(csvRecord.get(OUTAGEREGION)) : EMPTY);
        usagePointFileInfo.setReadcycle(csvRecord.isMapped(READCYCLE) ? processComment(csvRecord.get(READCYCLE)) : EMPTY);
        usagePointFileInfo.setReadroute(csvRecord.isMapped(READROUTE) ? processComment(csvRecord.get(READROUTE)) : EMPTY);
        usagePointFileInfo.setServicePriority(csvRecord.isMapped(SERVICEPRIORITY) ? processComment(csvRecord.get(SERVICEPRIORITY)) : EMPTY);
        usagePointFileInfo.setAllowUpdate(csvRecord.isMapped(ALLOWUPDATE) ? processComment(csvRecord.get(ALLOWUPDATE)) : EMPTY);
        usagePointFileInfo.setGrounded(csvRecord.isMapped(GROUNDED) ? csvRecord.get(GROUNDED) : EMPTY);
        usagePointFileInfo.setPhaseCode(csvRecord.isMapped(PHASECODE) ? processEnum(processComment(csvRecord.get(PHASECODE)).toUpperCase(), PhaseCode.class) : EMPTY);
        usagePointFileInfo.setRatedPowerValue(csvRecord.isMapped(RATEDPOWERVALUE) ? processComment(csvRecord.get(RATEDPOWERVALUE)) : EMPTY);
        usagePointFileInfo.setRatedPowerMultiplier(csvRecord.isMapped(RATEDPOWERMULTIPLIER) ? processInt(processComment(csvRecord.get(RATEDPOWERMULTIPLIER))) : 0);
        usagePointFileInfo.setRatedPowerUnit(csvRecord.isMapped(RATEDPOWERUNIT) ? processComment(csvRecord.get(RATEDPOWERUNIT)) : EMPTY);
        usagePointFileInfo.setRatedCurrentValue(csvRecord.isMapped(RATEDCURRENTVALUE) ? processComment(csvRecord.get(RATEDCURRENTVALUE)) : EMPTY);
        usagePointFileInfo.setRatedCurrentMultiplier(csvRecord.isMapped(RATEDCURRENTMULTIPLIER) ? processInt(processComment(csvRecord.get(RATEDCURRENTMULTIPLIER))) : 0);
        usagePointFileInfo.setRatedCurrentUnit(csvRecord.isMapped(RATEDCURRENTUNIT) ? processComment(csvRecord.get(RATEDCURRENTUNIT)) : EMPTY);
        usagePointFileInfo.setEstimatedLoadValue(csvRecord.isMapped(ESTIMATEDLOADVALUE) ? processComment(csvRecord.get(ESTIMATEDLOADVALUE)) : EMPTY);
        usagePointFileInfo.setEstimatedLoadMultiplier(csvRecord.isMapped(ESTIMATEDLOADMULTIPLIER) ? processInt(processComment(csvRecord.get(ESTIMATEDLOADMULTIPLIER))) : 0);
        usagePointFileInfo.setEstimatedLoadUnit(csvRecord.isMapped(ESTIMATEDLOADUNIT) ? processComment(csvRecord.get(ESTIMATEDLOADUNIT)) : EMPTY);
        usagePointFileInfo.setNominalVoltageValue(csvRecord.isMapped(NOMINALVOLTAGEVALUE) ? processComment(csvRecord.get(NOMINALVOLTAGEVALUE)) : EMPTY);
        usagePointFileInfo.setNominalVoltageMultiplier(csvRecord.isMapped(NOMINALVOLTAGEMULTIPLIER) ? processInt(processComment(csvRecord.get(NOMINALVOLTAGEMULTIPLIER))) : 0);
        usagePointFileInfo.setNominalVoltageUnit(csvRecord.isMapped(NOMINALVOLTAGEUNIT) ? processComment(csvRecord.get(NOMINALVOLTAGEUNIT)) : EMPTY);
        return usagePointFileInfo;
    }
}