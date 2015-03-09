package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationReport;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleProperties;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.EstimatorFactory;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.streams.Functions;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.estimation.console",
        service = ConsoleCommands.class,
        property = {"osgi.command.scope=estimation",
                "osgi.command.function=estimationBlocks",
                "osgi.command.function=availableEstimators",
                "osgi.command.function=ruleSets",
                "osgi.command.function=createRuleSet",
                "osgi.command.function=addRule",
                "osgi.command.function=estimate",
                "osgi.command.function=estimateWithLambdas",
                "osgi.command.function=removeRuleSet",
                "osgi.command.function=removeRule"
        },
        immediate = true)
public class ConsoleCommands {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private volatile MeteringService meteringService;
    private volatile EstimationService estimationService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;

    public void estimationBlocks(long meterId) {
        try {
            EstimationEngine estimationEngine = new EstimationEngine();
            Meter meter = meteringService.findMeter(meterId).orElseThrow(IllegalArgumentException::new);
            meter.getCurrentMeterActivation().ifPresent(meterActivation -> {
                meterActivation.getChannels().stream()
                        .flatMap(channel -> channel.getReadingTypes().stream())
                        .flatMap(readingType -> estimationEngine.findBlocksToEstimate(meterActivation, readingType).stream())
                        .map(this::print)
                        .forEach(System.out::println);
            });
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void availableEstimators() {
        estimationService.getAvailableEstimators().stream()
                .peek(est -> System.out.println(est.getDefaultFormat()))
                .flatMap(est -> est.getPropertySpecs().stream())
                .map(spec -> spec.getName() + ' ' + spec.getValueFactory().getValueType().toString())
                .forEach(System.out::println);
    }

    public void createRuleSet(String name) {
        threadPrincipalService.set(() -> "Console");
        try {
            transactionService.execute(VoidTransaction.of(() -> {
                EstimationRuleSet estimationRuleSet = estimationService.createEstimationRuleSet(name);
                estimationRuleSet.save();
                System.out.println(print(estimationRuleSet));
            }));
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void ruleSets(String... name) {
        Set<String> names = new HashSet<>(Arrays.asList(name));
        estimationService.getEstimationRuleSets().stream()
                .filter(set -> names.isEmpty() || names.contains(set.getName()))
                .map(this::print)
                .forEach(System.out::println);
    }

    public void addRule(long ruleSetId, String name, String implementation, String readingTypesCommaSeparated, String... properties) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            EstimationRuleSet set = estimationService.getEstimationRuleSet(ruleSetId).orElseThrow(IllegalArgumentException::new);
            EstimationRule estimationRule = set.addRule(implementation, name);

            Stream.of(readingTypesCommaSeparated.split(","))
                    .map(meteringService::getReadingType)
                    .flatMap(Functions.asStream())
                    .forEach(estimationRule::addReadingType);
            Stream.of(properties)
                    .map(string -> string.split(":"))
                    .forEach(split -> {
                        estimationRule.getPropertySpecs().stream()
                                .filter(spec -> spec.getName().equals(split[0]))
                                .findFirst()
                                .ifPresent(spec -> {
                                    Object value = spec.getValueFactory().fromStringValue(split[1]);
                                    estimationRule.addProperty(split[0], value);
                                });
                    });
            set.save();
            System.out.println(print(set));
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void removeRuleSet(long ruleSetId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            EstimationRuleSet set = estimationService.getEstimationRuleSet(ruleSetId).orElseThrow(IllegalArgumentException::new);
            set.delete();
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void removeRule(long ruleSetId, long ruleId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            EstimationRuleSet set = estimationService.getEstimationRuleSet(ruleSetId).orElseThrow(IllegalArgumentException::new);
            set.getRules().stream()
                    .filter(rule -> rule.getId() == ruleId)
                    .findFirst()
                    .ifPresent(rule -> {
                        set.deleteRule(rule);
                        set.save();
                        context.commit();
                    });
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void estimate(long meterId, String estimatorName) {
        try {
            EstimatorFactory estimatorFactory = new EstimatorFactoryImpl();
            Estimator estimator = estimatorFactory.createTemplate(estimatorName);
            EstimationEngine estimationEngine = new EstimationEngine();
            Meter meter = meteringService.findMeter(meterId).orElseThrow(IllegalArgumentException::new);
            Optional<? extends MeterActivation> meterActivationOptional = meter.getCurrentMeterActivation();
            MeterActivation meterActivation = meter.getCurrentMeterActivation().orElseThrow(() -> new IllegalArgumentException("no meter activation present for meter " + meter.getName()));

            meterActivation.getChannels().stream()
                    .peek(channel -> System.out.println("Handling channel id " + channel.getId()))
                    .flatMap(channel -> channel.getReadingTypes().stream())
                    .peek(readingType -> System.out.println("Handling reading type " + readingType.getAliasName()))
                    .map(readingType -> estimationEngine.findBlocksToEstimate(meterActivation, readingType))
                    .peek(estimator::estimate)
                    .flatMap(Collection::stream)
                    .flatMap(block -> block.estimatables().stream())
                    .map(estimatable -> "Estimated value " + estimatable.getEstimation() + " for " + estimatable.getTimestamp())
                    .forEach(System.out::println);

        } catch (IllegalArgumentException e) {
            System.out.println("Estimator class '" + estimatorName + "' not found");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void estimate(long meterId) {
        Meter meter = meteringService.findMeter(meterId).orElseThrow(IllegalArgumentException::new);
        meter.getCurrentMeterActivation()
                .map(estimationService::estimate)
                .map(EstimationReport::getResults)
                .map(Stream::of)
                .orElseGet(Stream::empty)
                .forEach(map -> {
                    map.entrySet().stream()
                            .peek(entry -> System.out.println("ReadingType : " + entry.getKey().getMRID()))
                            .map(Map.Entry::getValue)
                            .forEach(result -> {
                                result.estimated().stream()
                                        .flatMap(block -> block.estimatables().stream())
                                        .map(estimatable -> "Estimated value " + estimatable.getEstimation() + " for " + estimatable.getTimestamp())
                                        .forEach(System.out::println);
                                result.remainingToBeEstimated().stream()
                                        .flatMap(block -> block.estimatables().stream())
                                        .map(estimatable -> "No estimated value for " + estimatable.getTimestamp())
                                        .forEach(System.out::println);
                            });
                });
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    private String print(EstimationBlock estimationBlock) {
        StringBuilder builder = new StringBuilder();
        builder.append("Block channel : ").append(estimationBlock.getChannel().getId()).append('\n')
                .append("  readingType : ").append(estimationBlock.getReadingType().getMRID()).append('\n');

        estimationBlock.estimatables().stream()
                .map(Estimatable::getTimestamp)
                .map(instant -> ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()))
                .map(FORMATTER::format)
                .forEach(formattedTime -> builder.append('\t').append(formattedTime).append('\n'));

        return builder.toString();
    }

    private String print(EstimationRuleSet ruleSet) {
        StringBuilder builder = new StringBuilder();
        builder.append(ruleSet.getId()).append(' ').append(ruleSet.getName()).append('\n');
        ruleSet.getRules().stream().forEach(rule -> this.appendRule(builder, rule));
        return builder.toString();
    }

    private void appendRule(StringBuilder builder, EstimationRule rule) {
        builder.append('\t').append(rule.getId()).append(' ').append(rule.getName()).append(" : ").append(rule.getImplementation()).append('\n');
        rule.getReadingTypes().stream()
                .sorted(Comparator.comparing(IdentifiedObject::getMRID))
                .forEach(rt -> builder.append('\t').append('\t').append(rt.getMRID()).append('\n'));
        rule.getProperties().stream()
                .sorted(Comparator.comparing(EstimationRuleProperties::getName))
                .forEach(prop -> builder.append('\t').append('\t').append(prop.getName()).append(" : ").append(prop.getValue()).append('\n'));
    }
}
