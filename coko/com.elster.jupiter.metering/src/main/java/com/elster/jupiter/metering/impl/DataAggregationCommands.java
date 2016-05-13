package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.CalculatedMetrologyContractData;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.time.RangeInstantBuilder;
import com.elster.jupiter.util.units.Quantity;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Component(name = "com.elster.jupiter.metering.aggregation.console", service = DataAggregationCommands.class, property = {
        "osgi.command.scope=dag",
        "osgi.command.function=aggregate",
        "osgi.command.function=activateMetrologyConfig",
        "osgi.command.function=linkMetrologyConfig",
        "osgi.command.function=setMultiplierValue",
        "osgi.command.function=matchingChannels",
        "osgi.command.function=showData"
}, immediate = true)
public class DataAggregationCommands {

    private volatile DataAggregationService dataAggregationService;
    private volatile MeteringService meteringService;
    private volatile MetrologyConfigurationService metrologyConfigurationService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;

    @Reference
    public void setDataAggregationService(DataAggregationService dataAggregationService) {
        this.dataAggregationService = dataAggregationService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setMetrologyConfigurationService(MetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void aggregate() {
        System.out.println("Usage: aggregate <usage point MRID> <contract purpose> <deliverable name> <start date>");
    }

    public void aggregate(String usagePointMRID, String contractPurpose, String deliverableName, String startDate) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            UsagePoint usagePoint = meteringService.findUsagePoint(usagePointMRID)
                    .orElseThrow(() -> new NoSuchElementException("No such usagepoint"));
            MetrologyConfiguration configuration = usagePoint.getMetrologyConfiguration()
                    .orElseThrow(() -> new NoSuchElementException("No metrology configuration"));
            MetrologyContract contract = configuration.getContracts().stream()
                    .filter(c -> c.getMetrologyPurpose().getName().equals(contractPurpose))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("No contract for purpose " + contractPurpose));
            ReadingTypeDeliverable deliverable = contract.getDeliverables().stream()
                    .filter(d -> d.getName().equals(deliverableName))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Deliverable not found on contract"));

            Instant start = ZonedDateTime.ofInstant(Instant.parse(startDate + "T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault()).toInstant();
            CalculatedMetrologyContractData data = dataAggregationService.calculate(usagePoint, contract, RangeInstantBuilder.closedOpenRange(start.toEpochMilli(), null));

            List<? extends BaseReadingRecord> dataForDeliverable = data.getCalculatedDataFor(deliverable);
            System.out.println("records found for deliverable:" + dataForDeliverable.size());
            context.commit();
        }
    }

    public void showData(String usagePointMRID, String contractPurpose, String deliverableName, String startDate) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            UsagePoint usagePoint = meteringService.findUsagePoint(usagePointMRID)
                    .orElseThrow(() -> new NoSuchElementException("No such usagepoint"));
            MetrologyConfiguration configuration = usagePoint.getMetrologyConfiguration()
                    .orElseThrow(() -> new NoSuchElementException("No metrology configuration"));
            MetrologyContract contract = configuration.getContracts().stream()
                    .filter(c -> c.getMetrologyPurpose().getName().equals(contractPurpose))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("No contract for purpose " + contractPurpose));
            ReadingTypeDeliverable deliverable = contract.getDeliverables().stream()
                    .filter(d -> d.getName().equals(deliverableName))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Deliverable not found on contract"));

            Instant start = ZonedDateTime.ofInstant(Instant.parse(startDate + "T00:00:00Z"), ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault()).toInstant();
            CalculatedMetrologyContractData data = dataAggregationService.calculate(usagePoint, contract, RangeInstantBuilder.closedOpenRange(start.toEpochMilli(), null));

            List<? extends BaseReadingRecord> dataForDeliverable = data.getCalculatedDataFor(deliverable);
            dataForDeliverable.forEach(this::showReading);
            System.out.println("records found for deliverable:" + dataForDeliverable.size());
            context.commit();
        }
    }

    private void showReading(BaseReadingRecord readingRecord) {
        System.out.println(readingRecord.getTimeStamp() + " : " + readingRecord.getValue());
    }

    private String getValue(BaseReadingRecord reading) {
        Quantity quantity = reading.getQuantity(reading.getReadingType());
        if (quantity != null) {
            return quantity.getValue().toString();
        } else {
            return "";
        }
    }

    public void activateMetrologyConfig(long metrologyConfigId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                    .orElseThrow(() -> new NoSuchElementException("No such metrology configuration"))
                    .activate();

            context.commit();
        }
    }

    public void linkMetrologyConfig(String usagePointMRID, long metrologyConfigId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            UsagePoint usagePoint = meteringService.findUsagePoint(usagePointMRID)
                    .orElseThrow(() -> new NoSuchElementException("No such usagepoint"));
            MetrologyConfiguration configuration = metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                    .orElseThrow(() -> new NoSuchElementException("No such metrology configuration"));
//            if (!metrologyConfigurationService.findLinkableMetrologyConfigurations(usagePoint).contains(configuration))
//                throw new IllegalArgumentException("Metrology configuration no linkable to usage point");
            usagePoint.apply(configuration);

            context.commit();
        }
    }

    public void setMultiplierValue(String meterMRID, String standardMultiplierType, long value) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            MeterActivation meterActivation = meteringService.findMeter(meterMRID).get().getCurrentMeterActivation().get();
            MultiplierType multiplierType = meteringService.getMultiplierType(MultiplierType.StandardType.valueOf(standardMultiplierType));
            meterActivation.setMultiplier(multiplierType, BigDecimal.valueOf(value));
            context.commit();
        }
    }

    public void matchingChannels(String meterMRID, long metrologyConfigId, String requirementName) {
        metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                .orElseThrow(() -> new NoSuchElementException("No such metrology configuration"))
                .getRequirements().stream()
                .filter(rq -> rq.getName().equals(requirementName))
                .findFirst().orElseThrow(() -> new NoSuchElementException("No such requirement"))
                .getMatchingChannelsFor(meteringService.findMeter(meterMRID)
                        .orElseThrow(() -> new NoSuchElementException("No such meter"))
                        .getCurrentMeterActivation()
                        .orElseThrow(() -> new NoSuchElementException("No current meter activation")))
                .stream()
                .map(ch -> ch.getMainReadingType().getMRID() + " " + ch.getMainReadingType().getFullAliasName())
                .forEach(System.out::println);
    }
}
