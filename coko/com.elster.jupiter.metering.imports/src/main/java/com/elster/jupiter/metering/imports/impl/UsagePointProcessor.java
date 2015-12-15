package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class UsagePointProcessor {

    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;

    public UsagePointProcessor(Clock clock, Thesaurus thesaurus, MeteringService meteringService) {
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.meteringService = meteringService;
    }

    public void process(List<UsagePointFileInfo> usagePointFileInfos, FileImportOccurrence fileImportOccurrence) {
        int failures = usagePointFileInfos.size() > 0 ? 0 : 1;
        int succeeded = 0;
        int lineNumber = 0;
        Logger logger = fileImportOccurrence.getLogger();
        try {
            for (UsagePointFileInfo usagePointFileInfo : usagePointFileInfos) {
                lineNumber++;
                boolean isUsagePointCreatedOrUpdated = createOrUpdateUsagePoint(usagePointFileInfo, logger, lineNumber);
                if (isUsagePointCreatedOrUpdated) {
                    succeeded++;
                } else {
                    failures++;
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

    public boolean createOrUpdateUsagePoint(UsagePointFileInfo usagePointFileInfo, Logger logger, int lineNumber) {
        if (usagePointFileInfo.getServiceKind().isEmpty()) {
            MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID.log(logger, thesaurus, lineNumber);
            return false;
        }
        if (usagePointFileInfo.getmRID().isEmpty()) {
            MessageSeeds.IMPORT_USAGEPOINT_MRID_INVALID.log(logger, thesaurus, lineNumber);
            return false;
        }
        UsagePoint usagePoint;
        boolean isUpdatable = usagePointFileInfo.getAllowUpdate().equalsIgnoreCase("true");
        Optional<UsagePoint> usagePointOptional = meteringService.findUsagePoint(usagePointFileInfo.getmRID());
        Optional<ServiceCategory> serviceCategory = meteringService.getServiceCategory(ServiceKind.valueOf(usagePointFileInfo.getServiceKind()));
        if (usagePointOptional.isPresent()) {
            usagePoint = usagePointOptional.get();
            if (usagePoint.getServiceCategory().getId() != serviceCategory.get().getId()) {
                MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID.log(logger, thesaurus, lineNumber);
                return false;
            }
        } else {
            usagePoint = serviceCategory.get().newUsagePoint(usagePointFileInfo.getmRID()).create();
            isUpdatable = true;
        }
        if (isUpdatable) {
            usagePoint.setSdp(false);
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
            usagePoint.update();
        }
        return true;
    }
}