package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.imports.UsagePointParser;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.*;
import java.util.logging.Logger;

public class UsagePointFileImporter implements FileImporter {

    private static final String CSV_EXTENSION = ".CSV";

    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile Map<String, UsagePointParser> parsers;

    private UsagePointParser usagePointParser;

    public UsagePointFileImporter(Thesaurus thesaurus, MeteringService meteringService, Clock clock, Map<String, UsagePointParser> parsers) {
        this.clock = clock;
        this.parsers = parsers;
        this.thesaurus = thesaurus;
        this.meteringService = meteringService;
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        Logger logger = fileImportOccurrence.getLogger();
        String fileExtension = getFileExtension(fileImportOccurrence.getFileName()).toUpperCase();

        if (parsers.containsKey(CSV_EXTENSION)) {
            usagePointParser = parsers.get(fileExtension);
        } else {
            MessageSeeds.IMPORT_USAGEPOINT_PARSER_INVALID.log(logger, thesaurus, fileExtension);
            return;
        }

        int failures = 0;
        int succeeded = 0;
        int lineNumber = 0;
        List<UsagePointFileInfo> usagePointFileInfos = usagePointParser.parse(fileImportOccurrence, thesaurus);

        try {
            for (UsagePointFileInfo usagePointFileInfo : usagePointFileInfos) {
                lineNumber = usagePointFileInfos.indexOf(usagePointFileInfo);

                if (usagePointFileInfo.getServiceKind().isEmpty()) {
                    failures++;
                    MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID.log(logger, thesaurus, lineNumber);
                    continue;
                }

                Optional<ServiceCategory> serviceCategory = meteringService.getServiceCategory(ServiceKind.valueOf(usagePointFileInfo.getServiceKind()));

                if (usagePointFileInfo.getmRID().isEmpty()) {
                    failures++;
                    MessageSeeds.IMPORT_USAGEPOINT_MRID_INVALID.log(logger, thesaurus, lineNumber);
                    continue;
                }

                Optional<UsagePoint> usagePointOptional = meteringService.findUsagePoint(usagePointFileInfo.getmRID());

                UsagePoint usagePoint;
                if (!usagePointOptional.isPresent()) {
                    usagePoint = serviceCategory.get().newUsagePoint(usagePointFileInfo.getmRID());
                } else {
                    usagePoint = usagePointOptional.get();
                    if (usagePoint.getServiceCategory().getId() != serviceCategory.get().getId()) {
                        failures++;
                        MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID.log(logger, thesaurus, lineNumber);
                        continue;
                    }
                }

                if (usagePointFileInfo.getServiceLocationID() > 0) {
                    Optional<ServiceLocation> serviceLocation = meteringService.findServiceLocation(usagePointFileInfo.getServiceLocationID());
                    if (serviceLocation.isPresent()) {
                        usagePoint.setVirtual(false);
                        usagePoint.setServiceLocation(serviceLocation.get());
                    } else {
                        usagePoint.setVirtual(true);
                        usagePoint.setServiceLocation(null);
                        MessageSeeds.IMPORT_USAGEPOINT_SERVICELOCATION_INVALID.log(logger, thesaurus, lineNumber);
                    }
                }

                if (!usagePointOptional.isPresent() || usagePointFileInfo.getAllowUpdate().equalsIgnoreCase("true")) {
                    usagePoint.setSdp(false);
                    if (!usagePointFileInfo.getName().isEmpty()) {
                        usagePoint.setName(usagePointFileInfo.getName());
                    }
                    if (!usagePointFileInfo.getAliasName().isEmpty()) {
                        usagePoint.setAliasName(usagePointFileInfo.getAliasName());
                    }
                    if (!usagePointFileInfo.getDescription().isEmpty()) {
                        usagePoint.setDescription(usagePointFileInfo.getDescription());
                    }
                    if (!usagePointFileInfo.getOutageregion().isEmpty()) {
                        usagePoint.setOutageRegion(usagePointFileInfo.getOutageregion());
                    }
                    if (!usagePointFileInfo.getReadcycle().isEmpty()) {
                        usagePoint.setReadCycle(usagePointFileInfo.getReadcycle());
                    }
                    if (!usagePointFileInfo.getReadroute().isEmpty()) {
                        usagePoint.setReadRoute(usagePointFileInfo.getReadroute());
                    }
                    if (!usagePointFileInfo.getReadcycle().isEmpty()) {
                        usagePoint.setServicePriority(usagePointFileInfo.getReadcycle());
                    }

                    UsagePointDetail usagePointDetail = usagePoint.getServiceCategory().newUsagePointDetail(usagePoint, clock.instant());
                    if (usagePointDetail instanceof ElectricityDetail) {
                        ElectricityDetail eDetail = (ElectricityDetail) usagePointDetail;
                        eDetail.setGrounded(usagePointFileInfo.getGrounded().equalsIgnoreCase("true"));
                        if (!usagePointFileInfo.getPhaseCode().isEmpty()) {
                            eDetail.setPhaseCode(PhaseCode.valueOf(usagePointFileInfo.getPhaseCode()));
                        }
                        if (!usagePointFileInfo.getRatedPowerValue().isEmpty()) {
                            eDetail.setRatedPower(Quantity.create(new BigDecimal(usagePointFileInfo.getRatedPowerValue()), usagePointFileInfo.getRatedPowerMultiplier(), usagePointFileInfo.getRatedPowerUnit()));
                        }
                        if (!usagePointFileInfo.getRatedCurrentValue().isEmpty()) {
                            eDetail.setRatedCurrent(Quantity.create(new BigDecimal(usagePointFileInfo.getRatedCurrentValue()), usagePointFileInfo.getRatedCurrentMultiplier(), usagePointFileInfo.getRatedCurrentUnit()));
                        }
                        if (!usagePointFileInfo.getEstimatedLoadValue().isEmpty()) {
                            eDetail.setEstimatedLoad(Quantity.create(new BigDecimal(usagePointFileInfo.getEstimatedLoadValue()), usagePointFileInfo.getEstimatedLoadMultiplier(), usagePointFileInfo.getEstimatedLoadUnit()));
                        }
                        if (!usagePointFileInfo.getNominalVoltageValue().isEmpty()) {
                            eDetail.setNominalServiceVoltage(Quantity.create(new BigDecimal(usagePointFileInfo.getNominalVoltageValue()), usagePointFileInfo.getNominalVoltageMultiplier(), usagePointFileInfo.getNominalVoltageUnit()));
                        }
                        usagePoint.addDetail(usagePointDetail);
                    }

                    usagePoint.save();
                    succeeded++;
                }
            }
        } catch (IllegalArgumentException e) {
            failures++;
            MessageSeeds.IMPORT_USAGEPOINT_INVALIDDATA.log(logger, thesaurus, lineNumber);
            MessageSeeds.IMPORT_USAGEPOINT_EXCEPTION.log(logger, thesaurus, e);
        } finally {
            if (failures == 0) {
                fileImportOccurrence.markSuccess(MessageSeeds.IMPORT_USAGEPOINT_SUCCEEDED.formatMessage(thesaurus, succeeded));
            } else if (succeeded == 0) {
                fileImportOccurrence.markFailure(MessageSeeds.IMPORT_USAGEPOINT_SUCCEEDED_WITH_FAILURES.formatMessage(thesaurus, succeeded, failures));
            } else {
                fileImportOccurrence.markSuccessWithFailures(MessageSeeds.IMPORT_USAGEPOINT_SUCCEEDED_WITH_FAILURES.formatMessage(thesaurus, succeeded, failures));
            }
        }

    }

    private String getFileExtension(String fileExtension) {
        int lastPointIndex = fileExtension.lastIndexOf('.');
        return lastPointIndex != -1 ? fileExtension.substring(lastPointIndex, fileExtension.length()) : "";
    }
}