package com.elster.insight.usagepoint.data.rest.impl;

public class EstimationRuleInfoFactory {

//    private final EstimationService estimationService;
//    private final PropertyUtils propertyUtils;

//    @Inject
//    EstimationRuleInfoFactory(EstimationService estimationService, PropertyUtils propertyUtils) {
//        this.estimationService = estimationService;
//        this.propertyUtils = propertyUtils;
//    }

//    public EstimationRuleInfo createEstimationRuleInfo(Collection<? extends ReadingQuality> readingQualities) {
//        if (readingQualities.stream().map(ReadingQualityRecord.class::cast).noneMatch(ReadingQualityRecord::isSuspect)) {
//            return readingQualities.stream()
//                    .map(ReadingQualityRecord.class::cast)
//                    .filter(ReadingQualityRecord::hasEstimatedCategory)
//                    .findFirst()//because reading could be estimated by only one estimation rule
//                    .flatMap(readingQuality -> estimationService.findEstimationRuleByQualityType(readingQuality.getType()))
//                    .map(this::asInfo)
//                    .orElse(null);
//        }
//        return null;
//    }
//
//    private EstimationRuleInfo asInfo(EstimationRule estimationRule) {
//        EstimationRuleInfo info = new EstimationRuleInfo();
//        info.id = estimationRule.getId();
//        info.ruleSetId = estimationRule.getRuleSet().getId();
//        info.deleted = estimationRule.isObsolete();
//        info.name = estimationRule.getName();
//        info.properties = propertyUtils.convertPropertySpecsToPropertyInfos(estimationRule.getPropertySpecs(), estimationRule.getProps());
//        return info;
//    }
}
