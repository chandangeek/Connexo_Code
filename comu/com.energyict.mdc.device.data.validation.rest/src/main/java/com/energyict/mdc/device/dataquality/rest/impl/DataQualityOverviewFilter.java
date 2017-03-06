/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.rest.impl;

import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.Validator;
import com.energyict.mdc.device.config.DeviceType;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.energyict.mdc.device.dataquality.DeviceDataQualityService.DataQualityOverviewBuilder;
import static com.energyict.mdc.device.dataquality.DeviceDataQualityService.MetricSpecificationBuilder;

public enum DataQualityOverviewFilter {

    DEVICE_GROUP("deviceGroup") {
        @Override
        void apply(JsonQueryFilter filter, DataQualityOverviewBuilder overviewBuilder, ResourceHelper resourceHelper) {
            List<Long> ids = filter.getLongList(jsonName());
            if (ids.contains(null)) {
                throw resourceHelper.newException(MessageSeeds.INVALID_FILTER_FORMAT, jsonName(), "[<device group id>, ...]");
            }
            Set<EndDeviceGroup> deviceGroups = ids.stream()
                    .map(resourceHelper::findEndDeviceGroupOrThrowException)
                    .collect(Collectors.toSet());
            overviewBuilder.in(deviceGroups);
        }
    },
    DEVICE_TYPE("deviceType") {
        @Override
        void apply(JsonQueryFilter filter, DataQualityOverviewBuilder overviewBuilder, ResourceHelper resourceHelper) {
            List<Long> ids = filter.getLongList(jsonName());
            if (ids.contains(null)) {
                throw resourceHelper.newException(MessageSeeds.INVALID_FILTER_FORMAT, jsonName(), "[<device type id>, ...]");
            }
            Set<DeviceType> deviceTypes = ids.stream()
                    .map(resourceHelper::findDeviceTypeOrThrowException)
                    .collect(Collectors.toSet());
            overviewBuilder.of(deviceTypes);
        }
    },
    PERIOD("from&to") {
        @Override
        void applyIfPresent(JsonQueryFilter filter, DataQualityOverviewBuilder overviewBuilder, ResourceHelper resourceHelper) {
            if (filter.hasProperty("from") || filter.hasProperty("to")) {
                apply(filter, overviewBuilder, resourceHelper);
            }
        }

        @Override
        void apply(JsonQueryFilter filter, DataQualityOverviewBuilder overviewBuilder, ResourceHelper resourceHelper) {
            Instant from = filter.getInstant("from");
            Instant to = filter.getInstant("to");
            overviewBuilder.in(Ranges.closed(from, to));
        }
    },
    READING_QUALITY("readingQuality") {

        private Map<String, Consumer<DataQualityOverviewBuilder>> readingQualityTypes = ImmutableMap.of(
                "suspects", DataQualityOverviewBuilder::havingSuspects,
                "confirmed", DataQualityOverviewBuilder::havingConfirmed,
                "estimates", DataQualityOverviewBuilder::havingEstimates,
                "informatives", DataQualityOverviewBuilder::havingInformatives,
                "edited", DataQualityOverviewBuilder::havingEdited
        );

        @Override
        void apply(JsonQueryFilter filter, DataQualityOverviewBuilder overviewBuilder, ResourceHelper resourceHelper) {
            filter.getStringList(jsonName()).stream()
                    .map(key -> readingQualityTypes.getOrDefault(key, noSuchReadingQualityType(resourceHelper)))
                    .forEach(consumer -> consumer.accept(overviewBuilder));
        }

        private Consumer<DataQualityOverviewBuilder> noSuchReadingQualityType(ResourceHelper resourceHelper) {
            return builder -> {
                throw resourceHelper.newException(MessageSeeds.INVALID_FILTER_FORMAT, jsonName(),
                        "[" + readingQualityTypes.keySet().stream().collect(Collectors.joining(", ")) + "]");
            };
        }
    },
    VALIDATOR("validator") {
        @Override
        void apply(JsonQueryFilter filter, DataQualityOverviewBuilder overviewBuilder, ResourceHelper resourceHelper) {
            List<String> implementations = filter.getStringList(jsonName());
            if (implementations.contains(null)) {
                throw resourceHelper.newException(MessageSeeds.INVALID_FILTER_FORMAT, jsonName(), "[<validator>, ...]");
            }
            List<Validator> validators = implementations.stream()
                    .map(resourceHelper::findValidatorOrThrowException)
                    .collect(Collectors.toList());
            overviewBuilder.suspectedBy(validators);
        }
    },
    ESTIMATOR("estimator") {
        @Override
        void apply(JsonQueryFilter filter, DataQualityOverviewBuilder overviewBuilder, ResourceHelper resourceHelper) {
            List<String> implementations = filter.getStringList(jsonName());
            if (implementations.contains(null)) {
                throw resourceHelper.newException(MessageSeeds.INVALID_FILTER_FORMAT, jsonName(), "[<estimator>, ...]");
            }
            List<Estimator> estimators = implementations.stream()
                    .map(resourceHelper::findEstimatorOrThrowException)
                    .collect(Collectors.toList());
            overviewBuilder.estimatedBy(estimators);
        }
    },
    AMOUNT_OF_SUSPECTS("amountOfSuspects") {
        @Override
        void apply(JsonQueryFilter filter, DataQualityOverviewBuilder overviewBuilder, ResourceHelper resourceHelper) {
            super.applyFilterWithOperator(filter, overviewBuilder::suspects, resourceHelper);
        }
    },
    AMOUNT_OF_CONFIRMED("amountOfConfirmed") {
        @Override
        void apply(JsonQueryFilter filter, DataQualityOverviewBuilder overviewBuilder, ResourceHelper resourceHelper) {
            super.applyFilterWithOperator(filter, overviewBuilder::confirmed, resourceHelper);
        }
    },
    AMOUNT_OF_ESTIMATES("amountOfEstimates") {
        @Override
        void apply(JsonQueryFilter filter, DataQualityOverviewBuilder overviewBuilder, ResourceHelper resourceHelper) {
            super.applyFilterWithOperator(filter, overviewBuilder::estimates, resourceHelper);
        }
    },
    AMOUNT_OF_INFORMATIVES("amountOfInformatives") {
        @Override
        void apply(JsonQueryFilter filter, DataQualityOverviewBuilder overviewBuilder, ResourceHelper resourceHelper) {
            super.applyFilterWithOperator(filter, overviewBuilder::informatives, resourceHelper);
        }
    },
    AMOUNT_OF_EDITED("amountOfEdited") {
        @Override
        void apply(JsonQueryFilter filter, DataQualityOverviewBuilder overviewBuilder, ResourceHelper resourceHelper) {
            super.applyFilterWithOperator(filter, overviewBuilder::edited, resourceHelper);
        }
    };

    private String jsonName;

    DataQualityOverviewFilter(String jsonName) {
        this.jsonName = jsonName;
    }

    String jsonName() {
        return jsonName;
    }

    void applyIfPresent(JsonQueryFilter filter, DataQualityOverviewBuilder overviewBuilder, ResourceHelper resourceHelper) {
        if (filter.hasProperty(jsonName())) {
            apply(filter, overviewBuilder, resourceHelper);
        }
    }

    abstract void apply(JsonQueryFilter filter, DataQualityOverviewBuilder overviewBuilder, ResourceHelper resourceHelper);

    private void applyFilterWithOperator(JsonQueryFilter filter, Supplier<MetricSpecificationBuilder> specBuilderSupplier, ResourceHelper resourceHelper) {
        Pair<Operator, JsonNode> operatorWithValues = filter.getProperty(jsonName(),
                jsonNode -> parseRelationalOperator(jsonName(), jsonNode, resourceHelper));
        MetricSpecificationBuilder specBuilder = specBuilderSupplier.get();
        Operator operator = operatorWithValues.getFirst();
        JsonNode values = operatorWithValues.getLast();
        operator.apply(jsonName(), values, specBuilder, resourceHelper);
    }

    private Pair<Operator, JsonNode> parseRelationalOperator(String filterProperty, JsonNode jsonNode, ResourceHelper resourceHelper) {
        JsonNode operatorNode = jsonNode.get("operator");
        JsonNode criteriaNode = jsonNode.get("criteria");
        if (operatorNode == null || !operatorNode.isTextual() || criteriaNode == null || criteriaNode.isNull()) {
            throw resourceHelper.newException(MessageSeeds.INVALID_FILTER_FORMAT, filterProperty,
                    "{operator: " + Stream.of(Operator.values()).map(Operator::jsonName).collect(Collectors.joining("|")) + ", criteria: <values>}");
        }
        String operatorName = operatorNode.asText();
        Operator operator = Operator.findOperator(operatorName).orElseThrow(
                () -> resourceHelper.newException(MessageSeeds.UNSUPPORTED_OPERATOR, operatorName,
                        Stream.of(Operator.values()).map(Operator::jsonName).collect(Collectors.joining(", "))));
        return Pair.of(operator, criteriaNode);
    }

    enum Operator {
        EQUAL("=") {
            @Override
            void apply(String filterProperty, JsonNode criteriaNode, MetricSpecificationBuilder builder, ResourceHelper resourceHelper) {
                if (!criteriaNode.isNumber()) {
                    throw resourceHelper.newException(MessageSeeds.INVALID_OPERATOR_CRITERIA, jsonName(), filterProperty);
                }
                long value = criteriaNode.numberValue().longValue();
                builder.equalTo(value);
            }
        },
        BETWEEN("BETWEEN") {
            @Override
            void apply(String filterProperty, JsonNode criteriaNode, MetricSpecificationBuilder builder, ResourceHelper resourceHelper) {
                if (!criteriaNode.isArray() || criteriaNode.size() != 2 || !criteriaNode.get(0).isNumber() || !criteriaNode.get(1).isNumber()) {
                    throw resourceHelper.newException(MessageSeeds.INVALID_OPERATOR_CRITERIA, jsonName(), filterProperty);
                }
                long lowerBound = criteriaNode.get(0).numberValue().longValue();
                long upperBound = criteriaNode.get(1).numberValue().longValue();
                builder.inRange(Ranges.closed(lowerBound, upperBound));
            }
        },
        GREATER_THAN(">") {
            @Override
            void apply(String filterProperty, JsonNode criteriaNode, MetricSpecificationBuilder builder, ResourceHelper resourceHelper) {
                if (!criteriaNode.isNumber()) {
                    throw resourceHelper.newException(MessageSeeds.INVALID_OPERATOR_CRITERIA, jsonName(), filterProperty);
                }
                long value = criteriaNode.numberValue().longValue();
                builder.inRange(Range.greaterThan(value));
            }
        },
        LESS_THAN("<") {
            @Override
            void apply(String filterProperty, JsonNode criteriaNode, MetricSpecificationBuilder builder, ResourceHelper resourceHelper) {
                if (!criteriaNode.isNumber()) {
                    throw resourceHelper.newException(MessageSeeds.INVALID_OPERATOR_CRITERIA, jsonName(), filterProperty);
                }
                long value = criteriaNode.numberValue().longValue();
                builder.inRange(Range.lessThan(value));
            }
        };

        private String jsonName;

        Operator(String jsonName) {
            this.jsonName = jsonName;
        }

        String jsonName() {
            return this.jsonName;
        }

        abstract void apply(String filterProperty, JsonNode criteriaNode, MetricSpecificationBuilder builder, ResourceHelper resourceHelper);

        static Optional<Operator> findOperator(String name) {
            return Stream.of(values()).filter(operator -> operator.jsonName().equals(name)).findAny();
        }
    }
}
