package com.energyict.mdc.device.data.rest.impl;

import java.util.*;

public class MonitorValidationInfo {


    public ValidationStatusInfo validationStatus;
    public Long total;
    public List<DetailedValidationRuleSetInfo> detailedRuleSets;
    public List<DetailedValidationLoadProfileInfo> detailedValidationLoadProfile;
    public List<DetailedValidationRegisterInfo> detailedValidationRegister;

    public MonitorValidationInfo(List<DataValidationStatus> dataValidationStatuses, ValidationStatusInfo validationStatus) {
        total = 0L;
        this.validationStatus = validationStatus;
        this.detailedRuleSets = getSuspectReasonMap(dataValidationStatuses);
    }

    public MonitorValidationInfo(Map<LoadProfile, List<DataValidationStatus>> loadProfileStatus , Map<NumericalRegister, List<DataValidationStatus>> registerStatus, ValidationStatusInfo validationStatus) {
        total = loadProfileStatus.entrySet().stream().flatMap(m -> m.getValue().stream()).collect(Collectors.counting()) +
                registerStatus.entrySet().stream().flatMap(m -> m.getValue().stream()).collect(Collectors.counting());

        List<DataValidationStatus> dataValidationStatuses = loadProfileStatus.entrySet().stream()
                .flatMap(m -> m.getValue().stream()).collect(Collectors.toList());
        dataValidationStatuses.addAll(registerStatus.entrySet().stream()
                .flatMap(m -> m.getValue().stream()).collect(Collectors.toList()));

        this.validationStatus = validationStatus;
        this.detailedValidationLoadProfile = new ArrayList<>();
        this.detailedValidationRegister = new ArrayList<>();
        loadProfileStatus.entrySet().stream()
                .sorted((lps1, lps2) -> lps1.getKey().getLoadProfileSpec().getLoadProfileType().getName().compareTo(lps2.getKey().getLoadProfileSpec().getLoadProfileType().getName()))
                .forEach(lp -> {
                    this.detailedValidationLoadProfile.add(new DetailedValidationLoadProfileInfo(lp.getKey(), new Long(lp.getValue().size())));
                });
        registerStatus.entrySet().stream()
                .sorted((regs1, regs2) -> regs1.getKey().getRegisterSpec().getReadingType().getFullAliasName().compareTo(regs2.getKey().getRegisterSpec().getReadingType().getFullAliasName()))
                .forEach( reg -> {
                    this.detailedValidationRegister.add(new DetailedValidationRegisterInfo(reg.getKey(),new Long(reg.getValue().size())));
                });
    }

    public MonitorValidationInfo() {
    }

}
