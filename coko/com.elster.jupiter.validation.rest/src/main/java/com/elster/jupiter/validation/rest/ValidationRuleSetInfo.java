package com.elster.jupiter.validation.rest;

import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationVersionStatus;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Comparator;
import java.util.Optional;

@XmlRootElement
public class ValidationRuleSetInfo {

    public long id;
	public String name;
	public String description;
    public Long startDate;
    public Long endDate;
    public int numberOfVersions;
    public Boolean hasCurrent;

	public ValidationRuleSetInfo(ValidationRuleSet validationRuleSet) {
        id = validationRuleSet.getId();
        name = validationRuleSet.getName();
        description = validationRuleSet.getDescription();
        hasCurrent = false;
        validationRuleSet.getRuleSetVersions().stream()
                .filter(v -> ValidationVersionStatus.CURRENT.equals(v.getStatus()))
                .findFirst()
                .ifPresent(ver -> {
                    Optional.ofNullable(ver.getStartDate()).ifPresent(sd-> this.startDate = sd.toEpochMilli());
                    Optional.ofNullable(ver.getEndDate()).ifPresent(ed-> this.endDate = ed.toEpochMilli());
                    this.hasCurrent = true;
                });
        numberOfVersions = validationRuleSet.getRuleSetVersions().size();
    }

    public ValidationRuleSetInfo() {
    }

    public static Comparator<ValidationRuleSetInfo> VALIDATION_RULESET_NAME_COMPARATOR
            = new Comparator<ValidationRuleSetInfo>() {

        public int compare(ValidationRuleSetInfo ruleset1, ValidationRuleSetInfo ruleset2) {
            if(ruleset1 == null || ruleset1.name == null || ruleset2 == null || ruleset2.name == null) {
                throw new IllegalArgumentException("Ruleset information is missed");
            }
            return ruleset1.name.compareToIgnoreCase(ruleset2.name);
        }
    };
}
