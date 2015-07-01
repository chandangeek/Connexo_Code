package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.units.Quantity;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;
import java.util.logging.Logger;

public class UsagePointFileImporter implements FileImporter {
    private static final int ZERO = 0;
    private static final char MARKER = '#';
    private static final char DELIMITER = ',';
    private static final String EMPTY = "";
    private static final String MRID = "mRID";
    private static final String SERVICEKIND = "serviceKind";
    private static final String SERVICELOCATIONID = "serviceLocationID";
    private static final String NAME = "name";
    private static final String ALIASNAME = "aliasName";
    private static final String DESCRIPTION = "description";
    private static final String OUTAGEREGION = "outageregion";
    private static final String READCYCLE = "readcycle";
    private static final String READROUTE = "readroute";
    private static final String SERVICEPRIORITY = "servicePriority";
    private static final String ALLOWUPDATE = "allowUpdate";
    private static final String GROUNDED = "grounded";
    private static final String PHASECODE = "phaseCode";
    private static final String RATEDPOWERVALUE = "ratedPowerValue";
    private static final String RATEDPOWERMULTIPLIER = "ratedPowerMultiplier";
    private static final String RATEDPOWERUNIT = "ratedPowerUnit";
    private static final String RATEDCURRENTVALUE = "ratedCurrentValue";
    private static final String RATEDCURRENTMULTIPLIER = "ratedCurrentMultiplier";
    private static final String RATEDCURRENTUNIT = "ratedCurrentUnit";
    private static final String ESTIMATEDLOADVALUE = "estimatedLoadValue";
    private static final String ESTIMATEDLOADMULTIPLIER = "estimatedLoadMultiplier";
    private static final String ESTIMATEDLOADUNIT = "estimatedLoadUnit";
    private static final String NOMINALVOLTAGEVALUE = "nominalVoltageValue";
    private static final String NOMINALVOLTAGEMULTIPLIER = "nominalVoltageMultiplier";
    private static final String NOMINALVOLTAGEUNIT = "nominalVoltageUnit";

    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;

    public UsagePointFileImporter(Thesaurus thesaurus, MeteringService meteringService, Clock clock) {
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.meteringService = meteringService;
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        int failures = ZERO;
        int succeeded = ZERO;
        char delimiter = DELIMITER;
        Logger logger = fileImportOccurrence.getLogger();

        try (CSVParser csvParser = new CSVParser(new InputStreamReader(fileImportOccurrence.getContents()), CSVFormat.DEFAULT.withHeader().withIgnoreSurroundingSpaces(true).withDelimiter(delimiter).withCommentMarker(MARKER))) {
            for (CSVRecord csvRecord : csvParser) {
                try {
                    String value = processEnum(processComment(csvRecord.get(SERVICEKIND)).toUpperCase(), ServiceKind.class);
                    if (value.isEmpty()) {
                        failures++;
                        MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID.log(logger, thesaurus, csvRecord.getRecordNumber());
                        continue;
                    }
                    Optional<ServiceCategory> serviceCategory = meteringService.getServiceCategory(ServiceKind.valueOf(value));

                    value = processComment(csvRecord.get(MRID));
                    if (value.isEmpty()) {
                        failures++;
                        MessageSeeds.IMPORT_USAGEPOINT_MRID_INVALID.log(logger, thesaurus, csvRecord.getRecordNumber());
                        continue;
                    }
                    Optional<UsagePoint> usagePointOptional = meteringService.findUsagePoint(value);

                    UsagePoint usagePoint;
                    if (!usagePointOptional.isPresent()) {
                        usagePoint = serviceCategory.get().newUsagePoint(value);
                    } else {
                        usagePoint = usagePointOptional.get();
                        if (usagePoint.getServiceCategory().getId() != serviceCategory.get().getId()) {
                            failures++;
                            MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID.log(logger, thesaurus, csvRecord.getRecordNumber());
                            continue;
                        }
                    }

                    Optional<ServiceLocation> serviceLocation = meteringService.findServiceLocation(processInt(csvRecord.get(SERVICELOCATIONID)));
                    if (serviceLocation.isPresent()) {
                        usagePoint.setVirtual(false);
                        usagePoint.setServiceLocation(serviceLocation.get());
                    } else {
                        usagePoint.setVirtual(true);
                        usagePoint.setServiceLocation(null);
                        MessageSeeds.IMPORT_USAGEPOINT_SERVICELOCATION_INVALID.log(logger, thesaurus, csvRecord.getRecordNumber());
                    }

                    if (!usagePointOptional.isPresent() || csvRecord.get(ALLOWUPDATE).equalsIgnoreCase("true")) {
                        usagePoint.setSdp(false);
                        value = processComment(csvRecord.get(NAME));
                        if (!value.isEmpty()) {
                            usagePoint.setName(value);
                        }
                        value = processComment(csvRecord.get(ALIASNAME));
                        if (!value.isEmpty()) {
                            usagePoint.setAliasName(value);
                        }
                        value = processComment(csvRecord.get(DESCRIPTION));
                        if (!value.isEmpty()) {
                            usagePoint.setDescription(value);
                        }
                        value = processComment(csvRecord.get(OUTAGEREGION));
                        if (!value.isEmpty()) {
                            usagePoint.setOutageRegion(value);
                        }
                        value = processComment(csvRecord.get(READCYCLE));
                        if (!value.isEmpty()) {
                            usagePoint.setReadCycle(value);
                        }
                        value = processComment(csvRecord.get(READROUTE));
                        if (!value.isEmpty()) {
                            usagePoint.setReadRoute(value);
                        }
                        value = processComment(csvRecord.get(SERVICEPRIORITY));
                        if (!value.isEmpty()) {
                            usagePoint.setServicePriority(value);
                        }

                        UsagePointDetail usagePointDetail = usagePoint.getServiceCategory().newUsagePointDetail(usagePoint, clock.instant());
                        if (usagePointDetail instanceof ElectricityDetail) {
                            ElectricityDetail eDetail = (ElectricityDetail) usagePointDetail;
                            eDetail.setGrounded(csvRecord.get(GROUNDED).equalsIgnoreCase("true"));
                            value = processEnum(processComment(csvRecord.get(PHASECODE)).toUpperCase(), PhaseCode.class);
                            if (!value.isEmpty()) {
                                eDetail.setPhaseCode(PhaseCode.valueOf(value));
                            }
                            value = processComment(csvRecord.get(RATEDPOWERVALUE));
                            if (!value.isEmpty()) {
                                eDetail.setRatedPower(Quantity.create(new BigDecimal(value), processInt(csvRecord.get(RATEDPOWERMULTIPLIER)), processComment(csvRecord.get(RATEDPOWERUNIT))));
                            }
                            value = processComment(csvRecord.get(RATEDCURRENTVALUE));
                            if (!value.isEmpty()) {
                                eDetail.setRatedCurrent(Quantity.create(new BigDecimal(value), processInt(csvRecord.get(RATEDCURRENTMULTIPLIER)), processComment(csvRecord.get(RATEDCURRENTUNIT))));
                            }
                            value = processComment(csvRecord.get(ESTIMATEDLOADVALUE));
                            if (!value.isEmpty()) {
                                eDetail.setEstimatedLoad(Quantity.create(new BigDecimal(value), processInt(csvRecord.get(ESTIMATEDLOADMULTIPLIER)), processComment(csvRecord.get(ESTIMATEDLOADUNIT))));
                            }
                            value = processComment(csvRecord.get(NOMINALVOLTAGEVALUE));
                            if (!value.isEmpty()) {
                                eDetail.setNominalServiceVoltage(Quantity.create(new BigDecimal(value), processInt(csvRecord.get(NOMINALVOLTAGEMULTIPLIER)), processComment(csvRecord.get(NOMINALVOLTAGEUNIT))));
                            }
                            usagePoint.addDetail(usagePointDetail);
                        }

                        usagePoint.save();
                        succeeded++;
                    }
                } catch (IllegalArgumentException e) {
                    failures++;
                    MessageSeeds.IMPORT_USAGEPOINT_INVALIDDATA.log(logger, thesaurus, csvRecord.getRecordNumber());
                    MessageSeeds.IMPORT_USAGEPOINT_EXCEPTION.log(logger, thesaurus, e);
                }
            }
        } catch (Exception e) {
            failures++;
            MessageSeeds.IMPORT_USAGEPOINT_EXCEPTION.log(logger, thesaurus, e);
        } finally {
            if (failures == ZERO) {
                fileImportOccurrence.markSuccess(MessageSeeds.IMPORT_USAGEPOINT_SUCCEEDED.formatMessage(thesaurus, succeeded));
            } else if (succeeded == ZERO) {
                fileImportOccurrence.markFailure(MessageSeeds.IMPORT_USAGEPOINT_SUCCEEDED_WITH_FAILURES.formatMessage(thesaurus, succeeded, failures));
            } else {
                fileImportOccurrence.markSuccessWithFailures(MessageSeeds.IMPORT_USAGEPOINT_SUCCEEDED_WITH_FAILURES.formatMessage(thesaurus, succeeded, failures));
            }
        }
    }

    private String processComment(String record) {
        return record.charAt(0) == MARKER ? EMPTY : record;
    }

    private int processInt(String record) {
        return processComment(record).isEmpty() ? ZERO : Integer.parseInt(record);
    }

    private <E extends Enum<E>> String processEnum(String value, Class<E> enumClass) {
        for (E e : enumClass.getEnumConstants()) {
            if (e.name().equals(value)) {
                return e.name();
            }
        }
        return EMPTY;
    }
}