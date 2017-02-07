/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-28 (14:39)
 */
@Component(name = "com.energyict.mdc.device.data.search",
        service = SearchDevicesGogoCommand.class,
        property = {
                "osgi.command.scope=mdc.device.search",
                "osgi.command.function=lo",
                "osgi.command.function=listOptions",
                "osgi.command.function=search"},
        immediate = true)
@SuppressWarnings("unused")
public class SearchDevicesGogoCommand {

    private static final Pattern conditionPattern = Pattern.compile("([^<>=!]*)((?:!)?=|<(?:=)?|>(?:=)?|like)(.*)");
    private volatile SearchService searchService;
    private volatile TransactionService transactionService;

    public SearchDevicesGogoCommand() {
        super();
    }

    @Inject
    public SearchDevicesGogoCommand(SearchService searchService, TransactionService transactionService) {
        this();
        this.searchService = searchService;
        this.transactionService = transactionService;
    }

    @Reference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Activate
    public void activate() {
        System.out.println("Gogo commands to search for devices are deployed and ready");
    }

    /**
     * Alias for listOptions.
     * @see #listOptions()
     */
    @SuppressWarnings("unused")
    public void lo() {
        this.listOptions();
    }

    /**
     * List all the search options.
     */
    @SuppressWarnings("unused")
    public void listOptions() {
        System.out.println("Usage mdc.device.search:search <condition>[ <condition]*");
        System.out.println("      where condition is: <key><operator><value>");
        System.out.println("            key is one of the following");
        System.out.println("               " + this.getDeviceSearchDomain().getProperties().stream().map(this::toString).collect(Collectors.joining("\n               ")));
        System.out.println("            operator is " + Operator.allSymbols());
        System.out.println("            value is the String representation of the type of the key that was used");
        System.out.println("      Note that when the value type is String, wildcards * and ? are allowed");
    }

    private String toString(SearchableProperty property) {
        PropertySpec spec = property.getSpecification();
        if (spec.isReference()) {
            return spec.getName() + " reference to " + spec.getValueFactory().getValueType().getName();
        }
        else {
            return spec.getName() + " of type " + spec.getValueFactory().getValueType().getName();
        }
    }

    private SearchDomain getDeviceSearchDomain() {
        return this.searchService.getDomains()
                .stream()
                .filter(p -> p.getDomainClass().isAssignableFrom(Device.class))
                .findAny()
                .orElseThrow(() -> new RuntimeException("SearchDomain for com.energyict.mdc.device.data.Device not found"));
    }

    @SuppressWarnings("unused")
    public void search(String... conditions) {
        long queryStart = System.currentTimeMillis();
        SearchBuilder<Device> builder = this.searchService.search(Device.class);
        Stream
            .of(conditions)
            .map(this::toConditionSpecification)
            .forEach(spec -> this.addCondition(builder, spec));
        List<Device> devices = builder.toFinder().find();
        long queryEnd = System.currentTimeMillis();
        long renderingStart = System.currentTimeMillis();
        System.out.println(
                devices
                    .stream()
                    .map(this::toString)
                    .collect(Collectors.joining("\n")));
        long renderingEnd = System.currentTimeMillis();
        System.out.println("Found " + devices.size() + " matching device(s) in " + (queryEnd - queryStart) + " millis");
        System.out.println("Rendering them took " + (renderingEnd - renderingStart) + " millis");
    }

    private ConditionSpecification toConditionSpecification(String condition) {
        Matcher conditionMatcher = conditionPattern.matcher(condition);
        if (conditionMatcher.matches()) {
            List<String> keyOperatorAndValue = Arrays.asList(conditionMatcher.group(1), conditionMatcher.group(2), conditionMatcher.group(3));
            return new ConditionSpecification(keyOperatorAndValue);
        }
        else {
            throw new IllegalArgumentException("All key value conditions must be written as: <key><operator><value>");
        }
    }

    private SearchBuilder<Device> addCondition(SearchBuilder<Device> builder, ConditionSpecification spec) {
        try {
            switch (spec.getOperator()) {
                case EQUAL:
                    return builder.where(spec.getKey()).isEqualTo(spec.getValue());
                case NOTEQUAL:
                    return builder.where(spec.getKey()).isNotEqualTo(spec.getValue());
                case GREATERTHAN:
                    return builder.where(spec.getKey()).isGreaterThan(spec.getValue());
                case GREATERTHANOREQUAL:
                    return builder.where(spec.getKey()).isGreaterThanOrEqualTo(spec.getValue());
                case LESSTHAN:
                    return builder.where(spec.getKey()).isLessThan(spec.getValue());
                case LESSTHANOREQUAL:
                    return builder.where(spec.getKey()).isLessThanOrEqualTo(spec.getValue());
                case LIKE: {
                    return builder.where(spec.getKey()).like(spec.getValue());
                }
                default: {
                    throw new IllegalArgumentException("Unexpected or unsupported operator " + spec.getOperator());
                }
            }
        }
        catch (InvalidValueException e) {
            System.out.printf(String.valueOf(spec.getValue()) + " is not a valid value for property " + spec.getKey());
            e.printStackTrace(System.err);
            throw new IllegalArgumentException(e);
        }
    }

    private boolean isWildCard(Object value) {
        if (value instanceof String) {
            String s = (String) value;
            return s.contains("*") || s.contains("?");
        }
        else {
            return false;
        }
    }

    public void complexSearch(String deviceName) throws InvalidValueException {
        System.out.println(
                this.searchService
                        .search(Device.class)
                        .where("name").isEqualTo(deviceName)
                        .and("statusName").in(
                            DefaultState.IN_STOCK.getKey(),
                            DefaultState.DECOMMISSIONED.getKey())
                        .and("deviceConfigId").isEqualTo(97L)
                        .toFinder()
                        .stream()
                        .map(this::toString)
                        .collect(Collectors.joining("\n")));
    }

    private String toString(Device device) {
        return device.getName() + " in state " + device.getState().getName();
    }

    private enum Operator {
        EQUAL("="),
        NOTEQUAL("!="),
        GREATERTHAN(">"),
        LESSTHAN("<"),
        GREATERTHANOREQUAL(">="),
        LESSTHANOREQUAL("<="),
        LIKE("like");

        private final String symbol;
        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        public static Operator from(String symbol) {
            return Stream
                    .of(values())
                    .filter(o -> symbol.equals(o.getSymbol()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown or unsupported operator " + symbol));
        }

        public static String allSymbols() {
            return Stream
                    .of(values())
                    .map(Operator::getSymbol)
                    .collect(Collectors.joining(", "));
        }
    }

    private class ConditionSpecification {
        private final String key;
        private final Operator operator;
        private final String value;

        private ConditionSpecification(List<String> tokens) {
            this(tokens.get(0), tokens.get(1), tokens.get(2));
        }

        private ConditionSpecification(String key, String operator, String value) {
            this.key = key;
            this.operator = Operator.from(operator);
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Operator getOperator() {
            return operator;
        }

        public String getValue() {
            return value;
        }
    }

}