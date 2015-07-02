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

    private String mRID;
    private String serviceKind;
    private long serviceLocationID;
    private String name;
    private String aliasName;
    private String description;
    private String outageregion;
    private String readcycle;
    private String readroute;
    private String servicePriority;
    private String allowUpdate;
    private String grounded;
    private String phaseCode;
    private String ratedPowerValue;
    private int ratedPowerMultiplier;
    private String ratedPowerUnit;
    private String ratedCurrentValue;
    private int ratedCurrentMultiplier;
    private String ratedCurrentUnit;
    private String estimatedLoadValue;
    private int estimatedLoadMultiplier;
    private String estimatedLoadUnit;
    private String nominalVoltageValue;
    private int nominalVoltageMultiplier;
    private String nominalVoltageUnit;

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
                    setAttributes(csvRecord);
                    String value = processEnum(processComment(serviceKind).toUpperCase(), ServiceKind.class);
                    if (value.isEmpty()) {
                        failures++;
                        MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID.log(logger, thesaurus, csvRecord.getRecordNumber());
                        continue;
                    }
                    Optional<ServiceCategory> serviceCategory = meteringService.getServiceCategory(ServiceKind.valueOf(value));

                    value = processComment(mRID);
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

                    Optional<ServiceLocation> serviceLocation = meteringService.findServiceLocation(serviceLocationID);
                    if (serviceLocation.isPresent()) {
                        usagePoint.setVirtual(false);
                        usagePoint.setServiceLocation(serviceLocation.get());
                    } else {
                        usagePoint.setVirtual(true);
                        usagePoint.setServiceLocation(null);
                        MessageSeeds.IMPORT_USAGEPOINT_SERVICELOCATION_INVALID.log(logger, thesaurus, csvRecord.getRecordNumber());
                    }

                    if (!usagePointOptional.isPresent() || allowUpdate.equalsIgnoreCase("true")) {
                        usagePoint.setSdp(false);
                        if (!name.isEmpty()) {
                            usagePoint.setName(name);
                        }
                        if (!aliasName.isEmpty()) {
                            usagePoint.setAliasName(aliasName);
                        }
                        if (!description.isEmpty()) {
                            usagePoint.setDescription(description);
                        }
                        if (!outageregion.isEmpty()) {
                            usagePoint.setOutageRegion(outageregion);
                        }
                        if (!readcycle.isEmpty()) {
                            usagePoint.setReadCycle(readcycle);
                        }
                        if (!readroute.isEmpty()) {
                            usagePoint.setReadRoute(readroute);
                        }
                        if (!servicePriority.isEmpty()) {
                            usagePoint.setServicePriority(servicePriority);
                        }

                        UsagePointDetail usagePointDetail = usagePoint.getServiceCategory().newUsagePointDetail(usagePoint, clock.instant());
                        if (usagePointDetail instanceof ElectricityDetail) {
                            ElectricityDetail eDetail = (ElectricityDetail) usagePointDetail;
                            eDetail.setGrounded(grounded.equalsIgnoreCase("true"));
                            if (!phaseCode.isEmpty()) {
                                eDetail.setPhaseCode(PhaseCode.valueOf(phaseCode));
                            }
                            if (!ratedPowerValue.isEmpty()) {
                                eDetail.setRatedPower(Quantity.create(new BigDecimal(ratedPowerValue), ratedPowerMultiplier, processComment(ratedPowerUnit)));
                            }
                            if (!ratedCurrentValue.isEmpty()) {
                                eDetail.setRatedCurrent(Quantity.create(new BigDecimal(ratedCurrentValue), ratedCurrentMultiplier, processComment(ratedCurrentUnit)));
                            }
                            if (!estimatedLoadValue.isEmpty()) {
                                eDetail.setEstimatedLoad(Quantity.create(new BigDecimal(estimatedLoadValue), estimatedLoadMultiplier, processComment(estimatedLoadUnit)));
                            }
                            if (!nominalVoltageValue.isEmpty()) {
                                eDetail.setNominalServiceVoltage(Quantity.create(new BigDecimal(nominalVoltageValue), nominalVoltageMultiplier, processComment(nominalVoltageUnit)));
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

    private void setAttributes(CSVRecord csvRecord) {
        this.mRID = csvRecord.isMapped(MRID) ? csvRecord.get(MRID) : EMPTY;
        this.serviceKind = csvRecord.isMapped(SERVICEKIND) ? csvRecord.get(SERVICEKIND) : EMPTY;
        this.serviceLocationID = csvRecord.isMapped(SERVICELOCATIONID) ? processInt(csvRecord.get(SERVICELOCATIONID)) : 0;
        this.name = csvRecord.isMapped(NAME) ? processComment(csvRecord.get(NAME)) : EMPTY;
        this.aliasName = csvRecord.isMapped(ALIASNAME) ? processComment(csvRecord.get(ALIASNAME)) : EMPTY;
        this.description = csvRecord.isMapped(DESCRIPTION) ? processComment(csvRecord.get(DESCRIPTION)) : EMPTY;
        this.outageregion = csvRecord.isMapped(OUTAGEREGION) ? processComment(csvRecord.get(OUTAGEREGION)) : EMPTY;
        this.readcycle = csvRecord.isMapped(READCYCLE) ? processComment(csvRecord.get(READCYCLE)) : EMPTY;
        this.readroute = csvRecord.isMapped(READROUTE) ? processComment(csvRecord.get(READROUTE)) : EMPTY;
        this.servicePriority = csvRecord.isMapped(SERVICEPRIORITY) ? processComment(csvRecord.get(SERVICEPRIORITY)) : EMPTY;
        this.allowUpdate = csvRecord.isMapped(ALLOWUPDATE) ? processComment(csvRecord.get(ALLOWUPDATE)) : EMPTY;
        this.grounded = csvRecord.isMapped(GROUNDED) ? csvRecord.get(GROUNDED) : EMPTY;
        this.phaseCode = csvRecord.isMapped(PHASECODE) ? processEnum(processComment(csvRecord.get(PHASECODE)).toUpperCase(), PhaseCode.class) : EMPTY;
        this.ratedPowerValue = csvRecord.isMapped(RATEDPOWERVALUE) ? processComment(csvRecord.get(RATEDPOWERVALUE)) : EMPTY;
        this.ratedPowerMultiplier = csvRecord.isMapped(RATEDPOWERMULTIPLIER) ? processInt(csvRecord.get(RATEDPOWERMULTIPLIER)) : 0;
        this.ratedPowerUnit = csvRecord.isMapped(RATEDPOWERUNIT) ? processComment(csvRecord.get(RATEDPOWERUNIT)) : EMPTY;
        this.ratedCurrentValue = csvRecord.isMapped(RATEDCURRENTVALUE) ? processComment(csvRecord.get(RATEDCURRENTVALUE)) : EMPTY;
        this.ratedCurrentMultiplier = csvRecord.isMapped(RATEDCURRENTMULTIPLIER) ? processInt(csvRecord.get(RATEDCURRENTMULTIPLIER)) : 0;
        this.ratedCurrentUnit = csvRecord.isMapped(RATEDCURRENTUNIT) ? processComment(csvRecord.get(RATEDCURRENTUNIT)) : EMPTY;
        this.estimatedLoadValue = csvRecord.isMapped(ESTIMATEDLOADVALUE) ? processComment(csvRecord.get(ESTIMATEDLOADVALUE)) : EMPTY;
        this.estimatedLoadMultiplier = csvRecord.isMapped(ESTIMATEDLOADMULTIPLIER) ? processInt(csvRecord.get(ESTIMATEDLOADMULTIPLIER)) : 0;
        this.estimatedLoadUnit = csvRecord.isMapped(ESTIMATEDLOADUNIT) ? processComment(csvRecord.get(ESTIMATEDLOADUNIT)) : EMPTY;
        this.nominalVoltageValue = csvRecord.isMapped(NOMINALVOLTAGEVALUE) ? processComment(csvRecord.get(NOMINALVOLTAGEVALUE)) : EMPTY;
        this.nominalVoltageMultiplier = csvRecord.isMapped(NOMINALVOLTAGEMULTIPLIER) ? processInt(csvRecord.get(NOMINALVOLTAGEMULTIPLIER)) : 0;
        this.nominalVoltageUnit = csvRecord.isMapped(NOMINALVOLTAGEUNIT) ? processComment(csvRecord.get(NOMINALVOLTAGEUNIT)) : EMPTY;
    }
}