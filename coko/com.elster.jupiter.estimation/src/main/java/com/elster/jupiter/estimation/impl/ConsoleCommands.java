package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleProperties;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.PeriodicalScheduleExpressionParser;
import com.elster.jupiter.time.TemporalExpressionParser;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.CompositeScheduleExpressionParser;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
                //"osgi.command.function=estimateWithoutLambdas",
                "osgi.command.function=removeRuleSet",
                "osgi.command.function=removeRule",
                "osgi.command.function=updateRule",
                "osgi.command.function=createEstimationTask",
                "osgi.command.function=log"
        },
        immediate = true)
public class ConsoleCommands {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private volatile MeteringService meteringService;
    private volatile EstimationService estimationService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile CompositeScheduleExpressionParser scheduleExpressionParser;
    private volatile MeteringGroupsService meteringGroupsService;

    public void estimationBlocks(long meterId) {
        try {
            EstimationEngine estimationEngine = new EstimationEngine();
            Meter meter = meteringService.findMeter(meterId).orElseThrow(IllegalArgumentException::new);
            meter.getCurrentMeterActivation().ifPresent(meterActivation -> {
                meterActivation.getChannels().stream()
                        .flatMap(channel -> channel.getReadingTypes().stream())
                        .flatMap(readingType -> estimationEngine.findBlocksToEstimate(meterActivation, Range.<Instant>all(), readingType).stream())
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
            EstimationRule estimationRule = set.addRule(implementation, name)
                    .withReadingTypes(Stream.of(readingTypesCommaSeparated.split(","))
                            .map(meteringService::getReadingType)
                            .flatMap(Functions.asStream())
                            .collect(Collectors.toSet()))
                    .withProperties(toPropertyMap(implementation, properties))
                    .create();
            System.out.println(print(set));
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    private Map<String, Object> toPropertyMap(String implementation, String[] properties) {
        Estimator estimator = estimationService.getEstimator(implementation).orElseThrow(IllegalArgumentException::new);
        return Arrays.stream(properties)
                .map(string -> string.split(":"))
                .map(array -> Pair.of(array[0], array[1]))
                .map(pair -> pair.<Object>withLast((property, stringValue) ->
                        estimator.getPropertySpecs().stream()
                                .filter(spec -> spec.getName().equals(property))
                                .findFirst()
                                .map(spec -> spec.getValueFactory().fromStringValue(stringValue))
                                .orElseThrow(IllegalArgumentException::new)

                ))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
    }

    public void updateRule(long ruleSetId, long ruleId, String name, String implementation, String readingTypesCommaSeparated, String... properties) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            EstimationRuleSet set = estimationService.getEstimationRuleSet(ruleSetId).orElseThrow(IllegalArgumentException::new);
            EstimationRule rule = set.getRules().stream()
                    .filter(hasId(ruleId))
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);

            List<String> readingTypes = Stream.of(readingTypesCommaSeparated.split(","))
                    .collect(Collectors.toList());

            Map<String, Object> props = Stream.of(properties)
                    .map(string -> string.split(":"))
                    .collect(Collectors.toMap(
                            split -> split[0],
                            split -> rule.getPropertySpecs().stream()
                                    .filter(spec -> spec.getName().equals(split[0]))
                                    .map(spec -> spec.getValueFactory().fromStringValue(split[1]))
                                    .findFirst()
                                    .orElse(null)
                    ));

            set.updateRule(ruleId, name, rule.isActive(), readingTypes, props);

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
                    .filter(hasId(ruleId))
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

    private Predicate<EstimationRule> hasId(long ruleId) {
        return rule -> rule.getId() == ruleId;
    }


    private Map<String, Object> getProperties(List<PropertySpec> propertySpecs, String... properties) {
        Map<String, Object> props = Stream.of(properties)
                .map(string -> string.split(":"))

                .collect(Collectors.toMap(
                        split -> split[0],
                        split -> propertySpecs.stream()
                                .filter(spec -> spec.getName().equals(split[0]))
                                .map(spec -> spec.getValueFactory().fromStringValue(split[1]))
                                .findFirst()
                                .orElse(null)
                ));
        return props;
    }

    public void estimate(long meterId, String estimatorName, String... properties) {
        try {
            Optional<Estimator> optionalEstimatorTemplate = estimationService.getEstimator(estimatorName);
            if (!optionalEstimatorTemplate.isPresent()) {
                System.out.println("Estimator class '" + estimatorName + "' not found");
                return;
            }
            Map<String, Object> props = getProperties(optionalEstimatorTemplate.get().getPropertySpecs(), properties);
            Estimator estimator = estimationService.getEstimator(estimatorName, props).get();
            Meter meter = meteringService.findMeter(meterId).orElseThrow(IllegalArgumentException::new);
            Optional<? extends MeterActivation> meterActivationOptional = meter.getCurrentMeterActivation();
            if (!meterActivationOptional.isPresent()) {
                System.out.println("no meter activation present or meter " + meter.getName());
            } else {
                MeterActivation meterActivation = meterActivationOptional.get();
                for (Channel channel : meterActivation.getChannels()) {
                    estimate(channel, estimator, meterActivation);
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private void estimate(Channel channel, Estimator estimator, MeterActivation meterActivation) {
        EstimationEngine estimationEngine = new EstimationEngine();
        System.out.println("Handling channel id " + channel.getId());
        for (ReadingType readingType : channel.getReadingTypes()) {
            System.out.println("Handling reading type " + readingType.getAliasName());
            List<EstimationBlock> blocks = estimationEngine.findBlocksToEstimate(meterActivation, Range.<Instant>all(), readingType);
            estimator.init(Logger.getLogger(ConsoleCommands.class.getName()));
            EstimationResult result = estimator.estimate(blocks);
            List<EstimationBlock> estimated = result.estimated();
            List<EstimationBlock> remaining = result.remainingToBeEstimated();
            for (EstimationBlock block : estimated) {
                System.out.println("Start estimated block");
                for (Estimatable estimatable : block.estimatables()) {
                    System.out.println("Estimated value " + estimatable.getEstimation() + " for " + estimatable.getTimestamp());
                }
                System.out.println("End estimated block");
                System.out.println("");
            }
            for (EstimationBlock block : remaining) {
                System.out.println("Start remaining block");
                for (Estimatable estimatable : block.estimatables()) {
                    System.out.println("No estimated value for " + estimatable.getTimestamp());
                }
                System.out.println("End remaining block");
                System.out.println("");
            }
        }
    }

    /*public void estimate(long meterId, String estimatorName) {
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
    }*/

    /*public void estimate(long meterId) {
        Meter meter = meteringService.findMeter(meterId).orElseThrow(IllegalArgumentException::new);
        meter.getCurrentMeterActivation()
                .map(estimationService::estimate)
                .map(EstimationReport::getResults)
                .ifPresent(map -> {
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
    }*/

    public void createEstimationTask(String name, long nextExecution, String scheduleExpression, long groupId) {
        threadPrincipalService.set(() -> "console");
        try (TransactionContext context = transactionService.getContext()) {
            EstimationTask estimationTask = estimationService.newBuilder()
                    .setName(name)
                    .setApplication("Admin")
                    .setNextExecution(Instant.ofEpochMilli(nextExecution))
                    .setEndDeviceGroup(endDeviceGroup(groupId))
                    .setScheduleExpression(parse(scheduleExpression)).create();
            context.commit();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
    }

    private ScheduleExpression parse(String scheduleExpression) {
        return scheduleExpressionParser.parse(scheduleExpression).orElseThrow(IllegalArgumentException::new);
    }

    private EndDeviceGroup endDeviceGroup(long groupId) {
        return meteringGroupsService.findEndDeviceGroup(groupId).orElseThrow(IllegalArgumentException::new);
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

    @Reference
    public void setCronExpressionParser(CronExpressionParser cronExpressionParser) {
        CompositeScheduleExpressionParser composite = new CompositeScheduleExpressionParser();
        composite.add(cronExpressionParser);
        composite.add(PeriodicalScheduleExpressionParser.INSTANCE);
        composite.add(new TemporalExpressionParser());
        composite.add(Never.NEVER);
        this.scheduleExpressionParser = composite;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
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
